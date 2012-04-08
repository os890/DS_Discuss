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
import org.apache.deltaspike.core.api.message.MessageResolver;
import org.apache.deltaspike.core.util.PropertyFileUtils;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

@Dependent
public class TestMessageResolver implements MessageResolver
{
    private static final long serialVersionUID = -7977480064291950923L;

    private ResourceBundle messageBundle;

    protected TestMessageResolver()
    {
    }

    @Inject
    public TestMessageResolver(LocaleResolver localeResolver)
    {
        ResourceBundle resolvedBundle;
        try
        {
            resolvedBundle = PropertyFileUtils
                .getResourceBundle(TestMessages.class.getName(), localeResolver.getLocale());
        }
        catch (MissingResourceException e)
        {
            //X TODO log it
            resolvedBundle = null;
        }

        this.messageBundle = resolvedBundle;
    }

    @Override
    public String getMessage(String messageDescriptor)
    {
        if (this.messageBundle != null && messageDescriptor != null &&
                messageDescriptor.startsWith("{") && messageDescriptor.endsWith("}"))
        {
            messageDescriptor = messageDescriptor.substring(1, messageDescriptor.length() - 1);
            try
            {
                return this.messageBundle.getString(messageDescriptor);
            }
            catch (MissingResourceException e)
            {
                return messageDescriptor;
            }
        }

        return messageDescriptor;
    }
}
