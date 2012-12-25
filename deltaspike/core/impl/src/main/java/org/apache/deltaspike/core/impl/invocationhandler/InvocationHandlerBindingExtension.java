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
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.util.ExceptionUtils;
import org.apache.deltaspike.core.util.bean.ImmutableBeanWrapper;
import org.apache.deltaspike.core.util.bean.ImmutablePassivationCapableBeanWrapper;
import org.apache.deltaspike.core.util.bean.WrappingBeanBuilder;
import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.inject.Inject;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InvocationHandlerBindingExtension implements Extension, Deactivatable
{
    private Boolean isActivated = true;
    private Map<Class<?>, Class<? extends Annotation>> partialBeans =
            new HashMap<Class<?>, Class<? extends Annotation>>();
    private Map<Class<? extends Annotation>, Class<? extends InvocationHandler>> partialBeanHandlers =
            new HashMap<Class<? extends Annotation>, Class<? extends InvocationHandler>>();

    protected void init(@Observes BeforeBeanDiscovery beforeBeanDiscovery)
    {
        isActivated = ClassDeactivationUtils.isActivated(getClass());
    }

    public <X> void findInvocationHandlerBindings(@Observes ProcessAnnotatedType<X> processAnnotatedType)
    {
        if (!isActivated)
        {
            return;
        }

        Class<X> beanClass = processAnnotatedType.getAnnotatedType().getJavaClass();
        Class<? extends Annotation> invocationHandlerBindingAnnotationClass =
                getInvocationHandlerBindingAnnotation(processAnnotatedType);

        if (invocationHandlerBindingAnnotationClass == null)
        {
            return;
        }

        if ((beanClass.isInterface() || Modifier.isAbstract(beanClass.getModifiers())))
        {
            this.partialBeans.put(beanClass, invocationHandlerBindingAnnotationClass);
        }
        else if (InvocationHandler.class.isAssignableFrom(beanClass))
        {
            validateInvocationHandler(beanClass, invocationHandlerBindingAnnotationClass);

            this.partialBeanHandlers.put(
                    invocationHandlerBindingAnnotationClass,
                    (Class<? extends InvocationHandler>) beanClass);
            processAnnotatedType.veto();
        }
    }

    public <X> void createBeans(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager)
    {
        if (!isActivated)
        {
            return;
        }

        for (Map.Entry<Class<?>, Class<? extends Annotation>> partialBeanEntry : this.partialBeans.entrySet())
        {
            afterBeanDiscovery.addBean(
                    createPartialBean(partialBeanEntry.getKey(), partialBeanEntry.getValue(), beanManager));
        }

        this.partialBeans.clear();
        this.partialBeanHandlers.clear();
    }

    private <T> Bean<T> createPartialBean(Class<T> beanClass,
                                          Class<? extends Annotation> bindingAnnotation,
                                          BeanManager bm)
    {
        Class<? extends InvocationHandler> invocationHandlerClass = partialBeanHandlers.get(bindingAnnotation);

        AnnotatedType<T> annotatedType = new AnnotatedTypeBuilder<T>().readFromType(beanClass).create();

        Set<InjectionPoint> injectionPoints = new HashSet<InjectionPoint>(); //TODO currently not supported
        WrappingBeanBuilder<T> beanBuilder =
                new ProxyAwareBeanBuilder<T>(beanClass, invocationHandlerClass, injectionPoints, bm)
                .readFromType(annotatedType);

        return beanBuilder.create();
    }

    private static class ProxyAwareBeanBuilder<T> extends WrappingBeanBuilder<T>
    {
        private final Class<T> beanClass;
        private Class<? extends InvocationHandler> handlerClass;
        private final Set<InjectionPoint> injectionPoints;

        public ProxyAwareBeanBuilder(Class<T> beanClass,
                                     Class<? extends InvocationHandler> handlerClass,
                                     Set<InjectionPoint> injectionPoints,
                                     BeanManager beanManager)
        {
            super(null, beanManager);
            this.beanClass = beanClass;
            this.handlerClass = handlerClass;
            this.injectionPoints = injectionPoints;
        }

        @Override
        public ImmutableBeanWrapper<T> create()
        {
            if (isPassivationCapable())
            {
                return new ImmutablePassivationCapableBeanWrapper<T>(createProxyAwareBean(),
                        getName(), getQualifiers(), getScope(), getStereotypes(), getTypes(), isAlternative(),
                        isNullable(), getToString(), getId());
            }
            else
            {
                return new ImmutableBeanWrapper<T>(createProxyAwareBean(), getName(), getQualifiers(), getScope(),
                        getStereotypes(), getTypes(), isAlternative(), isNullable(), getToString());
            }
        }

        private Bean<T> createProxyAwareBean()
        {
            return new Bean<T>()
            {
                @Override
                public Class<?> getBeanClass()
                {
                    return beanClass;
                }

                @Override
                public T create(CreationalContext<T> context)
                {
                    T result = createProxy(beanClass);

                    injectFields(((Proxy) result).getInvocationHandler(result));

                    //TODO call post-construct callback

                    return result;
                }

                private void injectFields(InvocationHandler invocationHandler)
                {
                    //TODO only scan once
                    for (Field field : invocationHandler.getClass().getDeclaredFields())
                    {
                        if (!field.isAnnotationPresent(Inject.class))
                        {
                            continue;
                        }

                        field.setAccessible(true);

                        Annotation[] qualifierList = filterQualifiers(field.getAnnotations());
                        Object objectRef = BeanProvider.getContextualReference(field.getType(), qualifierList);

                        try
                        {
                            field.set(invocationHandler, objectRef);
                        }
                        catch (Exception e)
                        {
                            ExceptionUtils.throwAsRuntimeException(e);
                        }
                    }
                }

                private Annotation[] filterQualifiers(Annotation[] annotations)
                {
                    List<Annotation> qualifiers = new ArrayList<Annotation>();

                    for (Annotation annotation: annotations)
                    {
                        if (annotation.annotationType().isAnnotationPresent(Qualifier.class))
                        {
                            qualifiers.add(annotation);
                        }
                    }

                    if (qualifiers.isEmpty())
                    {
                        return new Annotation[] {};
                    }

                    return qualifiers.toArray(new Annotation[qualifiers.size()]);
                }

                @Override
                public void destroy(T instance, CreationalContext<T> context)
                {
                    //TODO call pre-destroy callback
                    context.release();
                }

                //TODO proxy for abstract classes e.g. via javassist
                private <T> T createProxy(Class<T> type)
                {
                    InvocationHandler invocationHandler = createInvocationHandler(handlerClass);
                    return type.cast(Proxy.newProxyInstance(ClassUtils.getClassLoader(null),
                            new Class<?>[]{type}, invocationHandler));
                }

                private InvocationHandler createInvocationHandler(Class<? extends InvocationHandler> invocationHandler)
                {
                    try
                    {
                        return invocationHandler.newInstance();
                    }
                    catch (Exception e)
                    {
                        ExceptionUtils.throwAsRuntimeException(e);
                    }
                    //can't happen
                    return null;
                }

                @Override
                public Set<InjectionPoint> getInjectionPoints()
                {
                    return injectionPoints;
                }

                @Override
                public Set<Type> getTypes()
                {
                    return ProxyAwareBeanBuilder.this.getTypes();
                }

                @Override
                public Set<Annotation> getQualifiers()
                {
                    return ProxyAwareBeanBuilder.this.getQualifiers();
                }

                @Override
                public Class<? extends Annotation> getScope()
                {
                    return ProxyAwareBeanBuilder.this.getScope();
                }

                @Override
                public String getName()
                {
                    return ProxyAwareBeanBuilder.this.getName();
                }

                @Override
                public boolean isNullable()
                {
                    return ProxyAwareBeanBuilder.this.isNullable();
                }

                @Override
                public Set<Class<? extends Annotation>> getStereotypes()
                {
                    return ProxyAwareBeanBuilder.this.getStereotypes();
                }

                @Override
                public boolean isAlternative()
                {
                    return ProxyAwareBeanBuilder.this.isAlternative();
                }
            };
        }
    }

    private <X> Class<? extends Annotation> getInvocationHandlerBindingAnnotation(
            ProcessAnnotatedType<X> processAnnotatedType)
    {
        for (Annotation annotation : processAnnotatedType.getAnnotatedType().getAnnotations())
        {
            if (annotation.annotationType().isAnnotationPresent(InvocationHandlerBinding.class))
            {
                return annotation.annotationType();
            }
        }

        return null;
    }

    private <X> void validateInvocationHandler(Class<X> beanClass, Class<? extends Annotation> bindingAnnotationClass)
    {
        Class<? extends InvocationHandler> prevFoundHandler = this.partialBeanHandlers.get(bindingAnnotationClass);
        if (prevFoundHandler != null)
        {
            throw new IllegalStateException("Multiple handlers found for " +
                    bindingAnnotationClass.getName() + " (" +
                    prevFoundHandler.getName() + " and " + beanClass.getName() + ")");
        }
    }
}
