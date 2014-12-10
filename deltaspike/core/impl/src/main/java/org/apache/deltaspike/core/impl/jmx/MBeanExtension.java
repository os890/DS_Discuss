/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.deltaspike.core.impl.jmx;

import org.apache.deltaspike.core.api.config.base.CoreBaseConfig;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.jmx.JmxBroadcaster;
import org.apache.deltaspike.core.api.jmx.MBean;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.BeanUtils;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessManagedBean;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.annotation.Annotation;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class MBeanExtension implements Extension, Deactivatable
{
    private static final Logger LOGGER = Logger.getLogger(MBeanExtension.class.getName());

    private final Map<Class<?>, DynamicMBeanWrapper> wrappers = new ConcurrentHashMap<Class<?>, DynamicMBeanWrapper>();

    private final Collection<ObjectName> objectNames = new ArrayList<ObjectName>();

    private Boolean isActivated = true;

    protected void init(@Observes BeforeBeanDiscovery beforeBeanDiscovery)
    {
        isActivated = ClassDeactivationUtils.isActivated(getClass());
    }

    protected void processBean(@Observes final ProcessManagedBean<?> bean, final BeanManager bm) throws Exception
    {
        if (!isActivated)
        {
            return;
        }

        MBean mBeanAnnotation = bean.getAnnotated().getAnnotation(MBean.class);
        if (mBeanAnnotation != null)
        {
            registerObject(bean, mBeanAnnotation, bm);
        }
    }

    protected void shutdown(@Observes final BeforeShutdown shutdown) throws Exception
    {
        if (!isActivated)
        {
            return;
        }

        final MBeanServer mBeanServer = mBeanServer();
        for (ObjectName objectName : objectNames)
        {
            mBeanServer.unregisterMBean(objectName);
            LOGGER.info("Unregistered MBean " + objectName.getCanonicalName());
        }
        objectNames.clear();
    }

    private void registerObject(final ProcessManagedBean<?> bean,
                                final MBean mBeanAnnotation,
                                final BeanManager bm) throws Exception
    {
        final Class<?> clazz = bean.getAnnotatedBeanClass().getJavaClass();

        String objectNameValue = mBeanAnnotation.objectName();
        if (objectNameValue.isEmpty())
        {
            String name = mBeanAnnotation.name();

            if (name.isEmpty())
            {
                name = clazz.getName();
            }

            String category = mBeanAnnotation.category().trim();

            if (category.startsWith("{") && category.endsWith("}"))
            {
                category = ConfigResolver.getPropertyValue(
                    category.substring(1, category.length() - 1), "org.apache.deltaspike");
            }

            objectNameValue = category + ":type=MBeans,name=" + name;
        }

        final ObjectName objectName = new ObjectName(objectNameValue);

        final boolean normalScoped = isNormalScope(bean.getAnnotated().getAnnotations(), bm);
        final Annotation[] qualifiers = qualifiers(bean.getAnnotatedBeanClass(), bm);
        final DynamicMBeanWrapper mbean = new DynamicMBeanWrapper(clazz, normalScoped, qualifiers);
        final MBeanServer server = mBeanServer();
        if (server.isRegistered(objectName) && CoreBaseConfig.MBean.AUTO_UNREGISTER.getValue())
        {
            server.unregisterMBean(objectName);
        }
        server.registerMBean(mbean, objectName);

        objectNames.add(objectName);
        wrappers.put(clazz, mbean);

        LOGGER.info("Registered MBean " + objectName.getCanonicalName());
    }

    private Annotation[] qualifiers(final AnnotatedType<?> annotatedBeanClass, final BeanManager bm)
    {
        final Set<Annotation> qualifiers = BeanUtils.getQualifiers(bm, annotatedBeanClass.getAnnotations());
        return qualifiers.toArray(new Annotation[qualifiers.size()]);
    }

    // annotated doesn't always contain inherited annotations
    // TODO we have to check the origin of this issue
    private boolean isNormalScope(final Set<Annotation> annotations, final BeanManager bm)
    {
        for (Annotation annotation : annotations)
        {
            if (bm.isNormalScope(annotation.annotationType()))
            {
                return true;
            }
        }
        return false;
    }

    JmxBroadcaster getBroadcasterFor(final Class<?> clazz)
    {
        return wrappers.get(clazz);
    }

    private MBeanServer mBeanServer()
    {
        return ManagementFactory.getPlatformMBeanServer();
    }
}