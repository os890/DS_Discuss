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
package org.apache.deltaspike.core.impl.invocationhandler;

import org.apache.deltaspike.core.api.invocationhandler.annotation.InvocationHandlerBinding;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.util.bean.BeanBuilder;
import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;

import javax.enterprise.context.NormalScope;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InvocationHandlerBindingExtension implements Extension, Deactivatable
{
    private static final Logger LOG = Logger.getLogger(InvocationHandlerBindingExtension.class.getName());

    private Boolean isActivated = true;
    private Map<PartialBeanEntry, Class<? extends Annotation>> partialBeans =
            new HashMap<PartialBeanEntry, Class<? extends Annotation>>();
    private Map<BindingEntry, Class<? extends InvocationHandler>> partialBeanHandlers =
            new HashMap<BindingEntry, Class<? extends InvocationHandler>>();

    private IllegalStateException definitionError;

    protected void init(@Observes BeforeBeanDiscovery beforeBeanDiscovery)
    {
        this.isActivated = ClassDeactivationUtils.isActivated(getClass());

        if (this.isActivated)
        {
            this.isActivated = ClassUtils.tryToLoadClassForName("javassist.util.proxy.ProxyFactory") != null;

            if (!this.isActivated && LOG.isLoggable(Level.WARNING))
            {
                LOG.warning("@" + InvocationHandlerBinding.class.getName() +
                    " deactivated because Javassist is missing.");
            }
        }
    }

    public <X> void findInvocationHandlerBindings(@Observes ProcessAnnotatedType<X> pat, BeanManager beanManager)
    {
        if (!this.isActivated || this.definitionError != null)
        {
            return;
        }

        Class<X> beanClass = pat.getAnnotatedType().getJavaClass();
        Class<? extends Annotation> bindingAnnotationClass = getInvocationHandlerBindingAnnotationClass(pat);

        if (bindingAnnotationClass == null)
        {
            return;
        }

        if ((beanClass.isInterface() || Modifier.isAbstract(beanClass.getModifiers())))
        {
            this.partialBeans.put(
                new PartialBeanEntry(beanClass, pat.getAnnotatedType().getAnnotations(), beanManager),
                    bindingAnnotationClass);
        }
        else if (InvocationHandler.class.isAssignableFrom(beanClass))
        {
            validateInvocationHandler(
                    beanClass, bindingAnnotationClass, pat.getAnnotatedType().getAnnotations(), beanManager);

            this.partialBeanHandlers.put(
                    new BindingEntry(bindingAnnotationClass, pat.getAnnotatedType().getAnnotations(), beanManager),
                    (Class<? extends InvocationHandler>) beanClass);
        }
        else
        {
            this.definitionError = new IllegalStateException(beanClass.getName() + " is annotated with @" +
                bindingAnnotationClass.getName() + " and therefore has to be " +
                "an abstract class, an interface or an implementation of " + InvocationHandler.class.getName());
        }
    }

    public <X> void createBeans(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager)
    {
        if (!this.isActivated)
        {
            return;
        }

        if (this.definitionError != null)
        {
            afterBeanDiscovery.addDefinitionError(this.definitionError);
            return;
        }

        for (Map.Entry<PartialBeanEntry, Class<? extends Annotation>> partialBeanEntry : this.partialBeans.entrySet())
        {
            Bean partialBean = createPartialBean(partialBeanEntry.getKey(), partialBeanEntry.getValue(), beanManager);

            if (partialBean != null)
            {
                afterBeanDiscovery.addBean(partialBean);
            }
            else
            {
                afterBeanDiscovery.addDefinitionError(new IllegalStateException("A class which implements " +
                        InvocationHandler.class.getName() + " and is annotated with @" +
                        partialBeanEntry.getValue().getName() + " is needed as a handler for " +
                        partialBeanEntry.getKey().getPartialBeanClass().getName() + ". See the documentation about @" +
                        InvocationHandlerBinding.class.getName() + "."));
            }
        }

        this.partialBeans.clear();
        this.partialBeanHandlers.clear();
    }

    protected <T> Bean<T> createPartialBean(PartialBeanEntry partialBeanEntry,
                                            Class<? extends Annotation> bindingAnnotationClass,
                                            BeanManager beanManager)
    {
        Class<T> beanClass = (Class<T>)partialBeanEntry.getPartialBeanClass();

        BindingEntry bindingEntry =
            new BindingEntry(bindingAnnotationClass, partialBeanEntry.getPartialBeanQualifiers(), beanManager);

        Class<? extends InvocationHandler> invocationHandlerClass = partialBeanHandlers.get(bindingEntry);

        if (invocationHandlerClass == null)
        {
            return null;
        }

        AnnotatedType<T> annotatedType = new AnnotatedTypeBuilder<T>().readFromType(beanClass).create();

        BeanBuilder<T> beanBuilder = new BeanBuilder<T>(beanManager)
                .readFromType(annotatedType)
                .beanLifecycle(new PartialBeanLifecycle(
                        beanClass, annotatedType.getAnnotations(), invocationHandlerClass, beanManager));

        return beanBuilder.create();
    }

    protected <X> Class<? extends Annotation> getInvocationHandlerBindingAnnotationClass(ProcessAnnotatedType<X> pat)
    {
        for (Annotation annotation : pat.getAnnotatedType().getAnnotations())
        {
            if (annotation.annotationType().isAnnotationPresent(InvocationHandlerBinding.class))
            {
                return annotation.annotationType();
            }
        }

        return null;
    }

    protected <X> void validateInvocationHandler(Class<X> beanClass,
                                                 Class<? extends Annotation> bindingAnnotationClass,
                                                 Set<Annotation> annotations,
                                                 BeanManager beanManager)
    {
        List<Annotation> qualifiers = findQualifierAnnotations(annotations, beanManager);
        BindingEntry bindingEntry = new BindingEntry(bindingAnnotationClass, qualifiers, beanManager);

        Class<? extends InvocationHandler> alreadyFoundHandler = this.partialBeanHandlers.get(bindingEntry);
        if (alreadyFoundHandler != null)
        {
            this.definitionError = new IllegalStateException("Multiple handlers found for " +
                    bindingAnnotationClass.getName() + " (" +
                    alreadyFoundHandler.getName() + " and " + beanClass.getName() + ")");
        }

        for (Annotation annotation : annotations)
        {
            if (beanManager.isNormalScope(annotation.annotationType()))
            {
                return;
            }
        }

        //at least we have to restrict dependent-scoped beans (we wouldn't be able to destroy such handlers properly).
        this.definitionError = new IllegalStateException(beanClass.getName() + " needs to be normal-scoped. " +
            "(= Scopes annotated with @" + NormalScope.class.getName() + ")");
    }


    private List<Annotation> findQualifierAnnotations(Collection<Annotation> annotations, BeanManager beanManager)
    {
        List<Annotation> qualifiers = new ArrayList<Annotation>();

        for (Annotation annotation : annotations)
        {
            if (beanManager.isQualifier(annotation.annotationType()))
            {
                qualifiers.add(annotation);
            }
        }
        return qualifiers;
    }

    private class BindingEntry
    {
        private final Class<? extends Annotation> bindingAnnotationClass;
        private final List<Annotation> qualifiers;

        private BindingEntry(Class<? extends Annotation> bindingAnnotationClass,
                             Collection<Annotation> annotations,
                             BeanManager beanManager)
        {
            this.bindingAnnotationClass = bindingAnnotationClass;
            this.qualifiers = findQualifierAnnotations(annotations, beanManager);
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof BindingEntry))
            {
                return false;
            }

            BindingEntry that = (BindingEntry) o;

            if (!bindingAnnotationClass.equals(that.bindingAnnotationClass))
            {
                return false;
            }
            if (!qualifiers.equals(that.qualifiers))
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = bindingAnnotationClass.hashCode();
            result = 31 * result + qualifiers.hashCode();
            return result;
        }
    }

    private class PartialBeanEntry
    {
        private final Class<?> partialBeanClass;
        private List<Annotation> partialBeanQualifiers;

        private PartialBeanEntry(Class<?> partialBeanClass,
                                 Set<Annotation> partialBeanQualifiers,
                                 BeanManager beanManager)
        {
            this.partialBeanClass = partialBeanClass;
            this.partialBeanQualifiers = findQualifierAnnotations(partialBeanQualifiers, beanManager);
        }

        public Class<?> getPartialBeanClass()
        {
            return partialBeanClass;
        }

        public List<Annotation> getPartialBeanQualifiers()
        {
            return partialBeanQualifiers;
        }
    }
}
