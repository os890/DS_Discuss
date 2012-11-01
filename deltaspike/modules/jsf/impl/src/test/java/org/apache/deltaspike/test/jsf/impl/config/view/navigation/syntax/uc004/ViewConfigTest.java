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
package org.apache.deltaspike.test.jsf.impl.config.view.navigation.syntax.uc004;

import org.apache.deltaspike.core.api.config.view.metadata.ConfigDescriptor;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigDescriptor;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.spi.config.view.ViewConfigNode;
import org.apache.deltaspike.jsf.api.config.view.Folder;
import org.apache.deltaspike.jsf.api.config.view.Page;
import org.apache.deltaspike.jsf.impl.config.view.ViewConfigExtension;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ViewConfigTest
{
    private ViewConfigExtension viewConfigExtension;

    @Before
    public void before()
    {
        this.viewConfigExtension = new ViewConfigExtension();
    }

    @After
    public void after()
    {
        this.viewConfigExtension.freeViewConfigCache(null);
    }

    @Test
    //only (all) real annotations are available here -> e.g. #getInheritedMetaData never returns a result here
    //currently #getInheritedMetaData doesn't get tested explicitly
    //the (merged) result gets exposed via the ViewConfigResolver
    //TODO discuss if #getInheritedMetaData should be tested as well by exposing the transformed tree for tests
    public void testMetaDataInheritanceInTree()
    {
        this.viewConfigExtension.addPageDefinition(Pages.Index.class);
        this.viewConfigExtension.addPageDefinition(Pages.Admin.Index.class);
        this.viewConfigExtension.addPageDefinition(Pages.Admin.Home.class);
        this.viewConfigExtension.addPageDefinition(Pages.Admin.Statistics.Index.class);
        this.viewConfigExtension.addPageDefinition(Pages.Admin.Statistics.Home.class);

        ViewConfigNode node = this.viewConfigExtension.findNode(Pages.class);

        Assert.assertNotNull(node);
        Assert.assertNotNull(node.getParent()); //Root
        Assert.assertNull(node.getParent().getParent());

        Assert.assertNotNull(node.getChildren());
        Assert.assertEquals(2, node.getChildren().size());

        Assert.assertNotNull(node.getMetaData());
        Assert.assertEquals(0, node.getMetaData().size());

        Assert.assertNotNull(node.getInheritedMetaData());
        Assert.assertEquals(0, node.getInheritedMetaData().size());


        node = this.viewConfigExtension.findNode(Pages.Admin.class);

        Assert.assertNotNull(node);
        Assert.assertNotNull(node.getParent()); //Admin
        Assert.assertNotNull(node.getParent().getParent()); //Root
        Assert.assertNull(node.getParent().getParent().getParent());

        Assert.assertNotNull(node.getChildren());
        Assert.assertEquals(3, node.getChildren().size());

        Assert.assertNotNull(node.getMetaData());
        Assert.assertEquals(1, node.getMetaData().size());
        Assert.assertEquals(Page.NavigationMode.REDIRECT, ((Page) node.getMetaData().iterator().next()).navigation());
        Assert.assertEquals(Page.ViewParameterMode.INCLUDE, ((Page) node.getMetaData().iterator().next()).viewParams());
        Assert.assertEquals("", ((Page) node.getMetaData().iterator().next()).name());
        Assert.assertEquals(Page.Extension.DEFAULT, ((Page) node.getMetaData().iterator().next()).extension());

        Assert.assertNotNull(node.getInheritedMetaData());
        Assert.assertEquals(0, node.getInheritedMetaData().size());


        node = this.viewConfigExtension.findNode(Pages.Admin.Statistics.class);

        Assert.assertNotNull(node);
        Assert.assertNotNull(node.getParent()); //Admin
        Assert.assertNotNull(node.getParent().getParent()); //Pages
        Assert.assertNotNull(node.getParent().getParent().getParent()); //Root
        Assert.assertNull(node.getParent().getParent().getParent().getParent());

        Assert.assertNotNull(node.getChildren());
        Assert.assertEquals(2, node.getChildren().size());

        Assert.assertNotNull(node.getMetaData());
        Assert.assertEquals(0, node.getMetaData().size());

        Assert.assertNotNull(node.getInheritedMetaData());
        Assert.assertEquals(0, node.getInheritedMetaData().size());


        node = this.viewConfigExtension.findNode(Pages.Index.class);

        Assert.assertNotNull(node);
        Assert.assertNotNull(node.getParent()); //Pages
        Assert.assertNotNull(node.getParent().getParent()); //Root
        Assert.assertNull(node.getParent().getParent().getParent());

        Assert.assertNotNull(node.getChildren());
        Assert.assertEquals(0, node.getChildren().size());

        Assert.assertNotNull(node.getMetaData());
        Assert.assertEquals(1, node.getMetaData().size());
        Assert.assertEquals(Page.NavigationMode.DEFAULT, ((Page) node.getMetaData().iterator().next()).navigation());
        Assert.assertEquals(Page.ViewParameterMode.DEFAULT, ((Page) node.getMetaData().iterator().next()).viewParams());
        Assert.assertEquals("home", ((Page) node.getMetaData().iterator().next()).name());
        Assert.assertEquals("jsp", ((Page) node.getMetaData().iterator().next()).extension());

        Assert.assertNotNull(node.getInheritedMetaData());
        Assert.assertEquals(0, node.getInheritedMetaData().size());


        node = this.viewConfigExtension.findNode(Pages.Admin.Statistics.Index.class);

        Assert.assertNotNull(node);
        Assert.assertNotNull(node.getParent()); //Statistics
        Assert.assertNotNull(node.getParent().getParent()); //Admin
        Assert.assertNotNull(node.getParent().getParent().getParent()); //Pages
        Assert.assertNotNull(node.getParent().getParent().getParent().getParent()); //Root
        Assert.assertNull(node.getParent().getParent().getParent().getParent().getParent());

        Assert.assertNotNull(node.getChildren());
        Assert.assertEquals(0, node.getChildren().size());

        Assert.assertNotNull(node.getMetaData());
        Assert.assertEquals(1, node.getMetaData().size());
        Assert.assertEquals(Page.NavigationMode.DEFAULT, ((Page) node.getMetaData().iterator().next()).navigation());
        Assert.assertEquals(Page.ViewParameterMode.DEFAULT, ((Page) node.getMetaData().iterator().next()).viewParams());
        Assert.assertEquals("", ((Page) node.getMetaData().iterator().next()).name());
        Assert.assertEquals(Page.Extension.DEFAULT, ((Page) node.getMetaData().iterator().next()).extension());

        Assert.assertNotNull(node.getInheritedMetaData());
        Assert.assertEquals(0, node.getInheritedMetaData().size());


        node = this.viewConfigExtension.findNode(Pages.Admin.Statistics.Home.class);

        Assert.assertNotNull(node);
        Assert.assertNotNull(node.getParent()); //Statistics
        Assert.assertNotNull(node.getParent().getParent()); //Admin
        Assert.assertNotNull(node.getParent().getParent().getParent()); //Pages
        Assert.assertNotNull(node.getParent().getParent().getParent().getParent()); //Root
        Assert.assertNull(node.getParent().getParent().getParent().getParent().getParent());

        Assert.assertNotNull(node.getChildren());
        Assert.assertEquals(0, node.getChildren().size());

        Assert.assertNotNull(node.getMetaData());
        Assert.assertEquals(0, node.getMetaData().size());

        Assert.assertNotNull(node.getInheritedMetaData());
        Assert.assertEquals(0, node.getInheritedMetaData().size()); //not processed at this point - node was just added


        node = this.viewConfigExtension.findNode(Pages.Admin.Index.class);

        Assert.assertNotNull(node);
        Assert.assertNotNull(node.getParent()); //Admin
        Assert.assertNotNull(node.getParent().getParent()); //Pages
        Assert.assertNotNull(node.getParent().getParent().getParent()); //Root
        Assert.assertNull(node.getParent().getParent().getParent().getParent());

        Assert.assertNotNull(node.getChildren());
        Assert.assertEquals(0, node.getChildren().size());

        Assert.assertNotNull(node.getMetaData());
        Assert.assertEquals(0, node.getMetaData().size());

        Assert.assertNotNull(node.getInheritedMetaData());
        Assert.assertEquals(0, node.getInheritedMetaData().size());  //not processed at this point - node was just added


        node = this.viewConfigExtension.findNode(Pages.Admin.Home.class);

        Assert.assertNotNull(node);
        Assert.assertNotNull(node.getParent()); //Admin
        Assert.assertNotNull(node.getParent().getParent()); //Pages
        Assert.assertNotNull(node.getParent().getParent().getParent()); //Root
        Assert.assertNull(node.getParent().getParent().getParent().getParent());

        Assert.assertNotNull(node.getChildren());
        Assert.assertEquals(0, node.getChildren().size());

        Assert.assertNotNull(node.getMetaData());
        Assert.assertEquals(1, node.getMetaData().size());
        Assert.assertEquals(Page.NavigationMode.FORWARD, ((Page) node.getMetaData().iterator().next()).navigation());
        Assert.assertEquals(Page.ViewParameterMode.DEFAULT, ((Page) node.getMetaData().iterator().next()).viewParams());

        Assert.assertNotNull(node.getInheritedMetaData());
        Assert.assertEquals(0, node.getInheritedMetaData().size());  //not processed at this point - node was just added
    }

    @Test
    public void testMetaDataInheritanceInViewConfig()
    {
        this.viewConfigExtension.addPageDefinition(Pages.Index.class);
        this.viewConfigExtension.addPageDefinition(Pages.Admin.Index.class);
        this.viewConfigExtension.addPageDefinition(Pages.Admin.Home.class);
        this.viewConfigExtension.addPageDefinition(Pages.Admin.Statistics.Index.class);
        this.viewConfigExtension.addPageDefinition(Pages.Admin.Statistics.Home.class);

        ViewConfigResolver viewConfigResolver = this.viewConfigExtension.createViewConfigResolver();
        ConfigDescriptor configDescriptor = viewConfigResolver.getConfigDescriptor(Pages.class);

        Assert.assertNotNull(configDescriptor);
        Assert.assertNotNull(configDescriptor.getConfigClass());
        Assert.assertEquals(Pages.class, configDescriptor.getConfigClass());

        Assert.assertNotNull(configDescriptor.getMetaData());
        Assert.assertEquals(1, configDescriptor.getMetaData().size());
        Assert.assertEquals(1, configDescriptor.getMetaData(Folder.class).size());
        Assert.assertEquals("/pages/", configDescriptor.getMetaData(Folder.class).iterator().next().name());


        configDescriptor = viewConfigResolver.getConfigDescriptor(Pages.Admin.class);

        Assert.assertNotNull(configDescriptor);
        Assert.assertNotNull(configDescriptor.getConfigClass());
        Assert.assertEquals(Pages.Admin.class, configDescriptor.getConfigClass());

        Assert.assertNotNull(configDescriptor.getMetaData());
        Assert.assertEquals(2, configDescriptor.getMetaData().size());
        Assert.assertEquals(1, configDescriptor.getMetaData(Folder.class).size());
        Assert.assertEquals("/pages/admin/", configDescriptor.getMetaData(Folder.class).iterator().next().name());
        Assert.assertEquals(1, configDescriptor.getMetaData(Page.class).size());
        Assert.assertEquals(Page.NavigationMode.REDIRECT, configDescriptor.getMetaData(Page.class).iterator().next().navigation());
        Assert.assertEquals(Page.ViewParameterMode.INCLUDE, configDescriptor.getMetaData(Page.class).iterator().next().viewParams());
        //the following is correct because it's @Page at the folder level:
        Assert.assertEquals("", configDescriptor.getMetaData(Page.class).iterator().next().name());
        Assert.assertEquals("", configDescriptor.getMetaData(Page.class).iterator().next().basePath());
        Assert.assertEquals("xhtml", configDescriptor.getMetaData(Page.class).iterator().next().extension());


        configDescriptor = viewConfigResolver.getConfigDescriptor(Pages.Admin.Statistics.class);

        Assert.assertNotNull(configDescriptor);
        Assert.assertNotNull(configDescriptor.getConfigClass());
        Assert.assertEquals(Pages.Admin.Statistics.class, configDescriptor.getConfigClass());

        Assert.assertNotNull(configDescriptor.getMetaData());
        Assert.assertEquals(1, configDescriptor.getMetaData().size());
        Assert.assertEquals(1, configDescriptor.getMetaData(Folder.class).size());
        Assert.assertEquals("/pages/admin/statistics/", configDescriptor.getMetaData(Folder.class).iterator().next().name());


        ViewConfigDescriptor viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Index.class);

        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/pages/home.jsp", viewConfigDescriptor.getViewId());
        Assert.assertEquals(Pages.Index.class, viewConfigDescriptor.getViewConfig());

        Assert.assertNotNull(viewConfigDescriptor.getMetaData());
        Assert.assertEquals(1, viewConfigDescriptor.getMetaData().size());
        Assert.assertEquals(1, viewConfigDescriptor.getMetaData(Page.class).size());
        Assert.assertEquals(Page.NavigationMode.FORWARD, viewConfigDescriptor.getMetaData(Page.class).iterator().next().navigation());
        Assert.assertEquals(Page.ViewParameterMode.EXCLUDE, viewConfigDescriptor.getMetaData(Page.class).iterator().next().viewParams());
        Assert.assertEquals("home", viewConfigDescriptor.getMetaData(Page.class).iterator().next().name());
        Assert.assertEquals("jsp", viewConfigDescriptor.getMetaData(Page.class).iterator().next().extension());


        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Admin.Statistics.Index.class);

        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/pages/admin/statistics/index.xhtml", viewConfigDescriptor.getViewId());
        Assert.assertEquals(Pages.Admin.Statistics.Index.class, viewConfigDescriptor.getViewConfig());

        Assert.assertNotNull(viewConfigDescriptor.getMetaData());
        Assert.assertEquals(1, viewConfigDescriptor.getMetaData().size());
        Assert.assertEquals(1, viewConfigDescriptor.getMetaData(Page.class).size());
        Assert.assertEquals(Page.NavigationMode.FORWARD, viewConfigDescriptor.getMetaData(Page.class).iterator().next().navigation());
        Assert.assertEquals(Page.ViewParameterMode.EXCLUDE, viewConfigDescriptor.getMetaData(Page.class).iterator().next().viewParams());
        Assert.assertEquals("index", viewConfigDescriptor.getMetaData(Page.class).iterator().next().name());
        Assert.assertEquals("xhtml", viewConfigDescriptor.getMetaData(Page.class).iterator().next().extension());


        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Admin.Statistics.Home.class);

        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/pages/admin/statistics/home.xhtml", viewConfigDescriptor.getViewId());
        Assert.assertEquals(Pages.Admin.Statistics.Home.class, viewConfigDescriptor.getViewConfig());

        Assert.assertNotNull(viewConfigDescriptor.getMetaData());
        Assert.assertEquals(1, viewConfigDescriptor.getMetaData().size());
        Assert.assertEquals(1, viewConfigDescriptor.getMetaData(Page.class).size());
        Assert.assertEquals(Page.NavigationMode.REDIRECT, viewConfigDescriptor.getMetaData(Page.class).iterator().next().navigation());
        Assert.assertEquals(Page.ViewParameterMode.INCLUDE, viewConfigDescriptor.getMetaData(Page.class).iterator().next().viewParams());
        Assert.assertEquals("home", viewConfigDescriptor.getMetaData(Page.class).iterator().next().name());
        Assert.assertEquals("xhtml", viewConfigDescriptor.getMetaData(Page.class).iterator().next().extension());


        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Admin.Index.class);

        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/pages/admin/index.xhtml", viewConfigDescriptor.getViewId());
        Assert.assertEquals(Pages.Admin.Index.class, viewConfigDescriptor.getViewConfig());

        Assert.assertNotNull(viewConfigDescriptor.getMetaData());
        Assert.assertEquals(1, viewConfigDescriptor.getMetaData().size());
        Assert.assertEquals(1, viewConfigDescriptor.getMetaData(Page.class).size());
        Assert.assertEquals(Page.NavigationMode.REDIRECT, viewConfigDescriptor.getMetaData(Page.class).iterator().next().navigation());
        Assert.assertEquals(Page.ViewParameterMode.INCLUDE, viewConfigDescriptor.getMetaData(Page.class).iterator().next().viewParams());
        Assert.assertEquals("index", viewConfigDescriptor.getMetaData(Page.class).iterator().next().name());
        Assert.assertEquals("xhtml", viewConfigDescriptor.getMetaData(Page.class).iterator().next().extension());


        viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(Pages.Admin.Home.class);

        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/pages/admin/home.xhtml", viewConfigDescriptor.getViewId());
        Assert.assertEquals(Pages.Admin.Home.class, viewConfigDescriptor.getViewConfig());

        Assert.assertNotNull(viewConfigDescriptor.getMetaData());
        Assert.assertEquals(1, viewConfigDescriptor.getMetaData().size());
        Assert.assertEquals(1, viewConfigDescriptor.getMetaData(Page.class).size());
        Assert.assertEquals(Page.NavigationMode.FORWARD, viewConfigDescriptor.getMetaData(Page.class).iterator().next().navigation());
        Assert.assertEquals(Page.ViewParameterMode.INCLUDE, viewConfigDescriptor.getMetaData(Page.class).iterator().next().viewParams());
        Assert.assertEquals("home", viewConfigDescriptor.getMetaData(Page.class).iterator().next().name());
        Assert.assertEquals("xhtml", viewConfigDescriptor.getMetaData(Page.class).iterator().next().extension());
    }
}
