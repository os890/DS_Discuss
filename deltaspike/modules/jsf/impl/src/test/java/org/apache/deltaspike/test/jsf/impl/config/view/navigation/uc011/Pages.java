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
package org.apache.deltaspike.test.jsf.impl.config.view.navigation.uc011;

import org.apache.deltaspike.core.api.config.view.Matches;
import org.apache.deltaspike.core.api.config.view.ViewConfig;

@CustomStaticQuota(perDay = 10000) //only gets picked up via meta-data-inheritance
public interface Pages
{
    interface Public extends ViewConfig, ViewQuota.PDF, ViewQuota.XML, ZIP
    {
        @CustomUrlMapping("/item/#{item}/")
        class Item implements Public
        {
        }
    }

    //folder - because it's of type ViewConfig
    interface Private extends ViewConfig
    {
    }

    //with interfaces users have a chance to find the usage(s) via std. ide-support (enums would appear unused)

    //@Matches could lead e.g. to meta-data inheritance until outermost interface which is not(!) of type ViewConfig,
    //because rules can't be used for navigation -> they don't need to be of type ViewConfig
    //pro: shorter syntax (compared to 'interface XML extends ViewQuota')
    //con: inheritance path can't be followed just by following the type hierarchy
    interface ViewQuota //technically not(!) needed (see ZIP) - just for better grouping
    {
        @Matches(pattern = "*.xml")
        interface XML
        {
        }

        @Matches(pattern = "*.pdf")
        @CustomStaticQuota(perDay = 100)
                //overrule quota
        interface PDF
        {
        }
    }

    @Matches(pattern = "*.zip")
    interface ZIP
    {
    }
}
