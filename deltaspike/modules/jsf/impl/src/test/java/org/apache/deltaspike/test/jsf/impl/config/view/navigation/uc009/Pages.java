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
package org.apache.deltaspike.test.jsf.impl.config.view.navigation.uc009;

import org.apache.deltaspike.jsf.api.config.view.Folder;
import org.apache.deltaspike.jsf.api.config.view.Page;
import org.apache.deltaspike.core.api.config.view.ViewConfig;

import static org.apache.deltaspike.jsf.api.config.view.Page.NavigationMode.FORWARD;
import static org.apache.deltaspike.jsf.api.config.view.Page.NavigationMode.REDIRECT;
import static org.apache.deltaspike.jsf.api.config.view.Page.ViewParameterMode.EXCLUDE;
import static org.apache.deltaspike.jsf.api.config.view.Page.ViewParameterMode.INCLUDE;

/**
 * rules - overview:
 */
//TODO re-visit rules for meta-data overruling - more complex due to inherited folder-meta-data (via nesting)
@Page(navigation = REDIRECT)
public interface Pages extends ViewConfig
{
    @Folder(inheritConfig = true)
    @Page(viewParams = INCLUDE)
    interface Public extends ViewConfig
    {
        //inherits viewParams = INCLUDE (due to '@Folder(inheritConfig = true)')
        class Index implements ViewConfig
        {
        }

        //inherits but overrides 'viewParams'
        @Page(viewParams = EXCLUDE)
        class Overview implements ViewConfig
        {
        }

        //inherits viewParams = INCLUDE (due to '@Folder(inheritConfig = true)')
        //inherits navigation = REDIRECT (due to 'implements Pages')
        class About implements Pages
        {
        }

        //inherits viewParams = INCLUDE (due to '@Folder(inheritConfig = true)')
        //inherits but overrides 'navigation'
        @Page(navigation = FORWARD)
        class Home implements Pages
        {
        }
    }

    @Folder(inheritConfig = true)
    @Page(navigation = FORWARD, viewParams = INCLUDE)
    interface Private extends Pages
    {
        //inherits viewParams = INCLUDE (due to '@Folder(inheritConfig = true)')
        //inherits navigation = FORWARD (folder-meta-data overrules inherited meta-data)
        class Index implements Private
        {
        }

        //a blank/default @Page shouldn't have an impact
        //inherits viewParams = INCLUDE (due to '@Folder(inheritConfig = true)')
        //inherits navigation = FORWARD (folder-meta-data overrules inherited meta-data)
        @Page
        class Overview implements Private
        {
        }
    }

    @Folder(inheritConfig = true)
    @Page(navigation = FORWARD, viewParams = INCLUDE)
    interface Admin extends Pages
    {
        //inherits viewParams = INCLUDE (due to '@Folder(inheritConfig = true)')
        //inherits navigation = REDIRECT (folder-meta-data overruled by directly implemented interface)
        class Index implements Pages
        {
        }

        //invalid - 'navigation' overruled by 2 different interfaces
        //class About implements Admin, Pages
    }

    @Folder(name = "reports")
    //TODO discuss if it should be applied for pages without view-config overruled by hardcoded param for implicit nav.
    @Page(navigation = FORWARD, viewParams = INCLUDE)
    interface Report extends Pages
    {
        //inherits viewParams = INCLUDE (due to 'implements Report')
        //inherits navigation = FORWARD (due to 'implements Report')
        class Index implements Report
        {
        }

        //inherits navigation = REDIRECT (due to 'implements Pages')
        class Overview implements Pages
        {
        }
    }
}
