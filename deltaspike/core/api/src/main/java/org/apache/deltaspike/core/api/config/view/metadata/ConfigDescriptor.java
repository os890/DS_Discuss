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
package org.apache.deltaspike.core.api.config.view.metadata;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Descriptor for type-safe configs
 */
public interface ConfigDescriptor
{
    //needed e.g. for folder nodes which aren't view-configs
    Class getConfigClass();

    /**
     * Custom meta-data which is configured for the entry. It allows to provide and resolve custom meta-data annotated
     * with {@link org.apache.deltaspike.core.api.config.view.metadata.annotation.ViewMetaData}
     *
     * @return custom meta-data of the current entry
     */
    List<Annotation> getMetaData();

    /**
     * Custom meta-data which is configured for the entry. It allows to provide and resolve custom meta-data annotated
     * with {@link org.apache.deltaspike.core.api.config.view.metadata.annotation.ViewMetaData}
     *
     * @param target target type
     * @return custom meta-data for the given type of the current entry
     */
    <T extends Annotation> List<T> getMetaData(Class<T> target);

    //if the callback doesn't return a value e.g. @PreRenderView
    CallbackDescriptor getCallbackDescriptor(Class<? extends Annotation> metaDataType,
                                             Class<? extends Annotation> callbackType);

    //only needed if the result is needed e.g. in case of @Secured
    <T extends CallbackDescriptor> T getCallbackDescriptor(Class<? extends Annotation> metaDataType,
                                                           Class<? extends Annotation> callbackType,
                                                           Class<? extends T> executorType);
}
