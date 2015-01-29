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
package org.apache.deltaspike.partialbean.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;
import org.apache.deltaspike.partialbean.api.PartialBeanBinding;

@ApplicationScoped
public class PartialBeanBindingExtension implements Extension, Deactivatable
{
    private Boolean isActivated = true;
    private IllegalStateException definitionError;

    protected void init(@Observes BeforeBeanDiscovery beforeBeanDiscovery)
    {
        this.isActivated = ClassDeactivationUtils.isActivated(getClass());

        if (!this.isActivated)
        {
            return;
        }

        // find partial beans, create a proxy class and register it
        // we need to do this in BeforeBeanDiscovery - please check the javadoc on the PartialBeanFinder class
        try
        {
            PartialBeanFinder partialBeanFinder = new PartialBeanFinder();
            Map<Class<? extends Annotation>, List<Class<?>>> partialBeanMapping = partialBeanFinder.find();
            
            for (Map.Entry<Class<? extends Annotation>, List<Class<?>>> entry : partialBeanMapping.entrySet())
            {
                Class<? extends Annotation> partialBeanBindingClass = entry.getKey();
                Class<? extends InvocationHandler> invocationHandlerClass =
                        partialBeanFinder.getInvocationHanderClass(partialBeanBindingClass);

                if (entry.getValue() != null && entry.getValue().size() > 0)
                {
                    if (invocationHandlerClass == null)
                    {
                        this.definitionError = new IllegalStateException("A class which implements " +
                            InvocationHandler.class.getName() + " and is annotated with @" +
                            partialBeanBindingClass.getName() + " is needed as a handler." +
                            " See the documentation about @" +
                            PartialBeanBinding.class.getName() + ".");
                        return;
                    }

                    for (Class<?> partialBeanClass : entry.getValue())
                    {
                        Class<?> proxyClass =
                                PartialBeanProxyFactory.getProxyClass(partialBeanClass, invocationHandlerClass);
                        AnnotatedType<?> annotatedType =
                                new AnnotatedTypeBuilder().readFromType(proxyClass).create();
                        beforeBeanDiscovery.addAnnotatedType(annotatedType);
                    }
                }
            }
        }
        catch (IllegalStateException e)
        {
            this.definitionError = e;
        }
    }

    public <X> void addDefinitionError(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager)
    {
        if (!this.isActivated)
        {
            return;
        }

        if (this.definitionError != null)
        {
            afterBeanDiscovery.addDefinitionError(this.definitionError);
        }
    }
}