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
package org.apache.deltaspike.test.security.impl.authentication;

import org.apache.deltaspike.security.api.Identity;
import org.apache.deltaspike.security.api.credential.Credential;
import org.apache.deltaspike.security.api.credential.LoginCredential;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class ShopClient
{
    @Inject
    private LoginCredential loginCredential;

    @Inject
    private Identity identity;

    @Inject
    private Shop shop;

    public Identity.AuthenticationResult login(String userName, final String password)
    {
        this.loginCredential.setUserId(userName);
        //TODO discuss #setSecurityToken
        this.loginCredential.setCredential(new Credential<String>() {
            @Override
            public String getValue()
            {
                return password;
            }
        });

        return this.identity.login();
    }

    public void logout()
    {
        this.identity.logout();
    }

    public String requestNewProduct(String customText)
    {
        return this.shop.sendInquiry(new NewProductInquiry(customText));
    }
}
