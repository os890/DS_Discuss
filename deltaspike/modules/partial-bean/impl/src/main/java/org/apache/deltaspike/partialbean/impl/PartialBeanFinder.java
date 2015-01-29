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
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.enterprise.inject.Typed;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.util.ServiceUtils;
import org.apache.deltaspike.partialbean.api.PartialBeanBinding;
import org.apache.deltaspike.partialbean.spi.ArchiveFinder;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.archive.Archive;
import org.apache.xbean.finder.archive.FileArchive;
import org.apache.xbean.finder.archive.JarArchive;

/**
 * The PartialBeanFinder is required to find the partial beans in BeforeBeanDiscovery to register the proxy class
 * as a completely new AnnotatedType.
 * 
 * We tried to switch the java class to the proxy type in the AnnotatedType via ProcessAnnotatedType
 * but this doesn't work in Weld.
 */
@Typed
public class PartialBeanFinder
{
    private final Map<Class<? extends Annotation>, List<Class<?>>> partialBeanMapping =
            new HashMap<Class<? extends Annotation>, List<Class<?>>>();
    private final Map<Class<? extends Annotation>, Class<? extends InvocationHandler>> partialBeanHandlerMapping =
            new HashMap<Class<? extends Annotation>, Class<? extends InvocationHandler>>();
    private final ArchiveFinder archiveFinder;
    
    public PartialBeanFinder()
    {
        List<ArchiveFinder> archiveFinders = ServiceUtils.loadServiceImplementations(ArchiveFinder.class);
        if (archiveFinders.size() > 0)
        {
            if (archiveFinders.size() > 1)
            {
                throw new IllegalStateException("Multiple " + ArchiveFinder.class.getName() 
                    + " implementations found. Please use Deactivatable to deactivate other implementations.");
            }
            
            archiveFinder = archiveFinders.get(0);
        }
        else
        {
            archiveFinder = new DefaultArchiveFinder();
        }
    }
    
    
    public Class<? extends InvocationHandler> getInvocationHanderClass(
            Class<? extends Annotation> partialBeanBindingClass)
    {
        return partialBeanHandlerMapping.get(partialBeanBindingClass);
    }
    
    /**
     * Find all partial bean classes on the classpath.
     *
     * @return
     */
    public Map<Class<? extends Annotation>, List<Class<?>>> find()
    {
        ClassLoader classLoader = ClassUtils.getClassLoader(this);
        Set<URL> deploymentUrls = archiveFinder.findArchives(classLoader);

        for (URL url : deploymentUrls)
        {
            Archive archive;

            if (url.getProtocol().equals("file") && url.toExternalForm().contains(".jar"))
            {
                archive = new JarArchive(classLoader, url);
            }
            else if (url.getProtocol().equals("jar"))
            {
                archive = new JarArchive(classLoader, url);
            }
            else if (url.getProtocol().equals("vfs"))
            {
                archive = new VfsArchive(classLoader, url);
            }
            else if (url.getProtocol().equals("file"))
            {
                archive = new FileArchive(classLoader, url);
            }
            // ignore unsupported/unkown url types like "archive:*" or "bundle:*"
            else
            {
                continue;
            }

            AnnotationFinder finder = new AnnotationFinder(archive);

            // search for all annotations which are annotated with @PartialBeanBinding
            List<Class<?>> partialBeanBindings = finder.findAnnotatedClasses(PartialBeanBinding.class);
            for (Class<?> partialBeanBinding : partialBeanBindings)
            {
                if (!Annotation.class.isAssignableFrom(partialBeanBinding))
                {
                    throw new IllegalStateException(partialBeanBinding.getName() + "is annotated with @"
                            + PartialBeanBinding.class.getName() + " but it's only allowed on annotations.");
                }

                Class<? extends Annotation> partialBeanBindingAnnotation =
                        (Class<? extends Annotation>) partialBeanBinding;
                
                // search for all classes which are annotated with the binding
                List<Class<?>> annotatedClasses =
                        finder.findAnnotatedClasses(partialBeanBindingAnnotation);

                for (Class<?> annotatedClass : annotatedClasses)
                {
                    if (annotatedClass.isInterface() || Modifier.isAbstract(annotatedClass.getModifiers()))
                    {
                        List<Class<?>> partialBeans = partialBeanMapping.get(partialBeanBindingAnnotation);
                        
                        if (partialBeans == null)
                        {
                            partialBeans = new ArrayList<Class<?>>();
                            partialBeanMapping.put(partialBeanBindingAnnotation, partialBeans);
                        }
                        
                        partialBeans.add(annotatedClass);
                    }
                    else if (InvocationHandler.class.isAssignableFrom(annotatedClass))
                    {
                        Class<? extends InvocationHandler> alreadyFoundHandler =
                                partialBeanHandlerMapping.get(partialBeanBindingAnnotation);
                        if (alreadyFoundHandler != null)
                        {
                            throw new IllegalStateException("Multiple handlers found for " +
                                    partialBeanBindingAnnotation.getName() + " (" +
                                    alreadyFoundHandler.getName() + " and " + annotatedClass.getName() + ")");
                        }
                        
                        partialBeanHandlerMapping.put(partialBeanBindingAnnotation,
                                (Class<? extends InvocationHandler>) annotatedClass);
                    }
                    else
                    {
                        throw new IllegalStateException(annotatedClass.getName() + " is annotated with @" +
                                partialBeanBindingAnnotation.getName() + " and therefore has to be " +
                                "an abstract class, an interface or an implementation of " +
                                InvocationHandler.class.getName());
                    }
                }
            }
        }

        return partialBeanMapping;
    }


}
