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
package org.apache.deltaspike.test.jsf.impl.config.view.navigation.syntax.uc002;

import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigDescriptor;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.spi.config.view.ViewConfigNode;
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
    public void testSimpleMetaDataTreeWithMetaData()
    {
        this.viewConfigExtension.addPageDefinition(SimplePageConfig002.class);

        ViewConfigNode node = this.viewConfigExtension.findNode(SimplePageConfig002.class);

        Assert.assertNotNull(node);
        Assert.assertNotNull(node.getParent());
        Assert.assertNull(node.getParent().getParent());

        Assert.assertNotNull(node.getChildren());
        Assert.assertEquals(0, node.getChildren().size());

        Assert.assertNotNull(node.getMetaData());
        Assert.assertEquals(1, node.getMetaData().size());

        Assert.assertNotNull(node.getInheritedMetaData());
        Assert.assertEquals(0, node.getInheritedMetaData().size());
    }

    @Test
    public void testSimpleViewConfigWithMetaData()
    {
        this.viewConfigExtension.addPageDefinition(SimplePageConfig002.class);

        ViewConfigResolver viewConfigResolver = this.viewConfigExtension.createViewConfigResolver();
        ViewConfigDescriptor viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(SimplePageConfig002.class);

        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertEquals("/simplePageConfig002.xhtml", viewConfigDescriptor.getViewId());
        Assert.assertEquals(SimplePageConfig002.class, viewConfigDescriptor.getViewConfig());
        Assert.assertNotNull(viewConfigDescriptor.getMetaData());

        Assert.assertEquals(1, viewConfigDescriptor.getMetaData().size());
    }
}
