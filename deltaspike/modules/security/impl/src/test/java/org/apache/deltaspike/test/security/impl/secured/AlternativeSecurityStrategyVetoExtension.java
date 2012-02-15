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
package org.apache.deltaspike.test.security.impl.secured;

import org.apache.deltaspike.test.security.impl.customsecured.AlternativeSecurityStrategy;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

/**
 * @deprecated it's just a workaround for an arquillian issue - we have to remove it as soon as it is fixed
 *             see {@link org.apache.deltaspike.test.security.impl.customsecured.AlternativeSecurityStrategy}
 */
public class AlternativeSecurityStrategyVetoExtension implements Extension
{
    protected void vetoAlternativeSecurityStrategy(@Observes ProcessAnnotatedType<Object> processAnnotatedType)
    {
        if (processAnnotatedType.getAnnotatedType().getJavaClass().equals(AlternativeSecurityStrategy.class))
        {
            processAnnotatedType.veto();
        }
    }
}
