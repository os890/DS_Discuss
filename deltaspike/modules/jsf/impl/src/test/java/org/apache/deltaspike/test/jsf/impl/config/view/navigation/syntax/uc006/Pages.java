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
package org.apache.deltaspike.test.jsf.impl.config.view.navigation.syntax.uc006;

import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.apache.deltaspike.jsf.api.config.view.Page;

import static org.apache.deltaspike.jsf.api.config.view.Page.Extension.FACES;
import static org.apache.deltaspike.jsf.api.config.view.Page.Extension.JSF;
import static org.apache.deltaspike.jsf.api.config.view.Page.Extension.XHTML;
import static org.apache.deltaspike.jsf.api.config.view.Page.NavigationMode.FORWARD;
import static org.apache.deltaspike.jsf.api.config.view.Page.NavigationMode.REDIRECT;
import static org.apache.deltaspike.jsf.api.config.view.Page.ViewParameterMode.INCLUDE;

@Page(navigation = REDIRECT, extension = JSF)
interface Pages extends ViewConfig
{
    //inherits navigation = REDIRECT and extension = JSF
    class Index implements Pages
    {
    }

    //inherits navigation = REDIRECT and extension = JSF
    interface Admin extends Pages
    {
        //inherits navigation = REDIRECT
        @Page(viewParams = INCLUDE, extension = FACES)
        interface Statistics extends Admin
        {
            //inherits navigation = REDIRECT and viewParams = INCLUDE and extension = FACES
            @Page class Index implements Statistics
            {
            }

            //inherits navigation = REDIRECT and extension = JSF
            class Home implements Admin
            {
            }
        }

        //inherits navigation = REDIRECT
        @Page(extension = XHTML) class Index implements Admin
        {
        }

        //inherits but overrules navigation and extension = JSF
        @Page(navigation = FORWARD) class Home implements Admin
        {
        }
    }
}
