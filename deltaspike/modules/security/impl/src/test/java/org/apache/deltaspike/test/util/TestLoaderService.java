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
package org.apache.deltaspike.test.util;

import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.webbeans.spi.LoaderService;

import javax.enterprise.inject.spi.Extension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TODO remove it as soon as arquillian supports correct resource handling
 *
 * @deprecated workaround for arquillian issue (extensions without physical service-loader config file)
 */
public class TestLoaderService implements LoaderService
{
    private LoaderService defaultImplementation = ClassUtils
            .tryToInstantiateClassForName("org.apache.webbeans.service.DefaultLoaderService", LoaderService.class);

    private static List<Class<? extends Extension>> additionalExtensions = new ArrayList<Class<? extends Extension>>();

    public static void useAdditionalExtensions(Class<? extends Extension>... extension)
    {
        additionalExtensions.clear();
        additionalExtensions.addAll(Arrays.asList(extension));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> List<T> load(Class<T> serviceType)
    {
        return load(serviceType, ClassUtils.getClassLoader(null));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> List<T> load(Class<T> serviceType, ClassLoader classLoader)
    {
        List<T> result = this.defaultImplementation.load(serviceType);

        if (Extension.class.isAssignableFrom(serviceType))
        {
            for (Class<? extends Extension> additionalExtension : additionalExtensions)
            {
                T additionalService = (T) ClassUtils.tryToInstantiateClass(additionalExtension);

                if (additionalService != null)
                {
                    result.add(additionalService);
                }
            }
        }

        return result;
    }
}
