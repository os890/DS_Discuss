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

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import org.apache.deltaspike.core.util.ExceptionUtils;
import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;
import org.apache.deltaspike.core.util.metadata.builder.ContextualLifecycle;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

class PartialBeanLifecycle<T, H extends InvocationHandler> implements ContextualLifecycle<T>
{
    private final Class<? extends T> proxyClass;

    private final InjectionTarget<H> handlerInjectionTarget;
    private final InjectionTarget<T> partialBeanInjectionTarget;

    PartialBeanLifecycle(Class<T> partialBeanClass, Class<H> handlerClass, BeanManager beanManager)
    {
        AnnotatedTypeBuilder<H> handlerTypeBuilder = new AnnotatedTypeBuilder<H>().readFromType(handlerClass);
        this.handlerInjectionTarget = beanManager.createInjectionTarget(handlerTypeBuilder.create());

        ProxyFactory factory = new ProxyFactory();

        if (partialBeanClass.isInterface())
        {
            this.partialBeanInjectionTarget = null;

            factory.setInterfaces(new Class[]{ partialBeanClass });
        }
        else
        {
            AnnotatedTypeBuilder<T> partialBeanTypeBuilder =
                new AnnotatedTypeBuilder<T>().readFromType(partialBeanClass);
            this.partialBeanInjectionTarget = beanManager.createInjectionTarget(partialBeanTypeBuilder.create());

            factory.setSuperclass(partialBeanClass);
        }

        factory.setFilter(new MethodFilter()
        {
            public boolean isHandled(Method method)
            {
                return !"finalize".equals(method.getName());
            }
        });

        this.proxyClass = ((Class<?>) factory.createClass()).asSubclass(partialBeanClass);
    }

    public T create(Bean bean, CreationalContext creationalContext)
    {
        try
        {
            H handlerInstance = this.handlerInjectionTarget.produce(creationalContext);
            this.handlerInjectionTarget.inject(handlerInstance, creationalContext);
            this.handlerInjectionTarget.postConstruct(handlerInstance);

            T instance = this.proxyClass.newInstance();

            if (this.partialBeanInjectionTarget != null)
            {
                this.partialBeanInjectionTarget.inject(instance, creationalContext);
                this.partialBeanInjectionTarget.postConstruct(instance);
            }

            PartialBeanMethodHandler<H> methodHandler = new PartialBeanMethodHandler<H>(handlerInstance);
            ((ProxyObject) instance).setHandler(methodHandler);
            return (T)instance;
        }
        catch (Exception e)
        {
            ExceptionUtils.throwAsRuntimeException(e);
        }
        //can't happen
        return null;
    }

    public void destroy(Bean<T> bean, T instance, CreationalContext<T> creationalContext)
    {
        if (this.partialBeanInjectionTarget != null)
        {
            this.partialBeanInjectionTarget.preDestroy(instance);
        }

        H handerInstance = (H) ((PartialBeanMethodHandler)((ProxyObject) instance).getHandler()).getHandlerInstance();
        this.handlerInjectionTarget.preDestroy(handerInstance);
        //injectionTarget.dispose(handerInstance); //currently producers aren't supported
        creationalContext.release();
    }
}