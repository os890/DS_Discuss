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

import org.apache.deltaspike.core.api.config.view.ViewConfig;

import javax.enterprise.inject.Model;

@Model
public class PageBean009
{
    public Class<? extends Pages> adminIndex()
    {
        return Pages.Admin.Index.class;
    }

    public Class<? extends Pages.Private> privateIndex()
    {
        return Pages.Private.Index.class;
    }

    public Class<? extends Pages.Private> privateOverview()
    {
        return Pages.Private.Overview.class;
    }

    public Class<? extends ViewConfig> publicIndex()
    {
        return Pages.Public.Index.class;
    }

    public Class<? extends ViewConfig> publicHome()
    {
        return Pages.Public.Home.class;
    }

    public Class<? extends ViewConfig> publicOverview()
    {
        return Pages.Public.Overview.class;
    }

    public Class<? extends ViewConfig> publicAbout()
    {
        return Pages.Public.About.class;
    }

    public Class<? extends Pages> reportIndex()
    {
        return Pages.Report.Index.class;
    }

    public Class<? extends Pages> reportOverview()
    {
        return Pages.Report.Overview.class;
    }
}
