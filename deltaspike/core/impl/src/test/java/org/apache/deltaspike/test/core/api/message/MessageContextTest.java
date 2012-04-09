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
package org.apache.deltaspike.test.core.api.message;

import org.apache.deltaspike.core.api.message.LocaleResolver;
import org.apache.deltaspike.core.api.message.MessageContext;
import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.apache.deltaspike.core.impl.message.MessageBundleExtension;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link MessageContext}
 */
@RunWith(Arquillian.class)
public class MessageContextTest
{
    @Inject
    private SimpleMessage simpleMessage;

    @Inject
    private MessageContext messageContext;

    /**
     * X TODO creating a WebArchive is only a workaround because JavaArchive
     * cannot contain other archives.
     */
    @Deployment
    public static WebArchive deploy()
    {
        new BeanManagerProvider()
        {
            @Override
            public void setTestMode()
            {
                super.setTestMode();
            }
        }.setTestMode();

        JavaArchive testJar = ShrinkWrap
                .create(JavaArchive.class, "messageTest.jar")
                .addPackage("org.apache.deltaspike.test.core.api.message")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        return ShrinkWrap
                .create(WebArchive.class, "messageContextTest.war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive())
                .addAsLibraries(testJar)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsServiceProvider(Extension.class,
                        MessageBundleExtension.class);
    }

    @Test
    public void testSimpleMessage()
    {
        assertEquals("Welcome to DeltaSpike",
                this.simpleMessage.welcomeTo(this.messageContext, "DeltaSpike").toString());
    }

    @Test
    public void resolveTextTest()
    {
        LocaleResolver localeResolver = new TestLocalResolver();

        String messageText = this.messageContext.config()
                .use()
                    .localeResolver(localeResolver)
                    .messageResolver(new TestMessageResolver(localeResolver))
                .create().message().text("{hello}").toText();

        assertEquals("test message", messageText);
    }

    @Test
    public void resolveGermanMessageTextTest()
    {
        LocaleResolver localeResolver = new TestGermanLocaleResolver();
        String messageText = this.messageContext.config()
                .use()
                    .localeResolver(localeResolver)
                    .messageResolver(new TestMessageResolver(localeResolver))
                .create().message().text("{hello}").toText();

        assertEquals("Test Nachricht", messageText);
    }

    @Test
    public void createInvalidMessageTest()
    {
        String messageText = this.messageContext.message().text("{xyz123}").toText();

        assertEquals("???xyz123???", messageText);
    }

    @Test
    public void createInvalidMessageWithArgumentsTest()
    {
        String messageText = this.messageContext.message().text("{xyz123}").argument("123").argument("456").argument("789").toText();

        assertEquals("???xyz123??? (123,456,789)", messageText);
    }
}
