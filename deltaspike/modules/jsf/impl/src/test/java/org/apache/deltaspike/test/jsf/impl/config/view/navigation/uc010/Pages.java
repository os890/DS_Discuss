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
package org.apache.deltaspike.test.jsf.impl.config.view.navigation.uc010;

import org.apache.deltaspike.jsf.api.config.view.PageParameter;
import org.apache.deltaspike.core.api.config.view.ViewConfig;

public interface Pages extends ViewConfig
{
    @PageParameter(key = "param1", value = "staticValue2")
    class Index implements Pages
    {
    }

    @PageParameter.List({
            @PageParameter(key = "param1", value = "staticValue2"),
            @PageParameter(key = "param2", value = "#{pageBean010.currentValue}")
    })
    class Overview implements Pages
    {
    }
}
