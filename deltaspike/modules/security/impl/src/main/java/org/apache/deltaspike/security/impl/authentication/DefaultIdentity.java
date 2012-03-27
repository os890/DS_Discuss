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
package org.apache.deltaspike.security.impl.authentication;

import org.apache.deltaspike.security.api.Identity;
import org.apache.deltaspike.security.api.User;
import org.apache.deltaspike.security.api.authentication.AuthenticationException;
import org.apache.deltaspike.security.api.authentication.UnexpectedCredentialException;
import org.apache.deltaspike.security.api.authentication.events.AlreadyLoggedInEvent;
import org.apache.deltaspike.security.api.authentication.events.LoggedInEvent;
import org.apache.deltaspike.security.api.authentication.events.LoginFailedEvent;
import org.apache.deltaspike.security.api.authentication.events.PostAuthenticateEvent;
import org.apache.deltaspike.security.api.authentication.events.PostLoggedOutEvent;
import org.apache.deltaspike.security.api.authentication.events.PreAuthenticateEvent;
import org.apache.deltaspike.security.api.authentication.events.PreLoggedOutEvent;
import org.apache.deltaspike.security.api.credential.LoginCredential;
import org.apache.deltaspike.security.spi.authentication.Authenticator;
import org.apache.deltaspike.security.spi.authentication.Authenticator.AuthenticationStatus;
import org.apache.deltaspike.security.spi.authentication.AuthenticatorSelector;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

/**
 * Default Identity implementation
 */
@SuppressWarnings("UnusedDeclaration")
@SessionScoped
public class DefaultIdentity implements Identity
{
    private static final long serialVersionUID = 3696702275353144429L;

    @Inject
    @SuppressWarnings("NonSerializableFieldInSerializableClass")
    private AuthenticatorSelector authenticatorSelector;

    @Inject
    @SuppressWarnings("NonSerializableFieldInSerializableClass")
    private BeanManager beanManager;

    @Inject
    @SuppressWarnings("NonSerializableFieldInSerializableClass")
    private LoginCredential loginCredential;

    /**
     * Flag indicating whether we are currently authenticating
     */
    private boolean authenticating;

    private User user;

    public boolean isLoggedIn() 
    {
        // If there is a user set, then the user is logged in.
        return this.user != null;
    }

    @Override
    public User getUser()
    {
        return this.user;
    }

    @Override
    public AuthenticationResult login() 
    {
        try 
        {
            if (isLoggedIn())
            {
                if (isAuthenticationRequestWithDifferentUserId())
                {
                    throw new UnexpectedCredentialException("active user: " + this.user.getId() +
                            " provided credentials: " + this.loginCredential.getUserId());
                }

                beanManager.fireEvent(new AlreadyLoggedInEvent());
                return AuthenticationResult.SUCCESS;
            }

            boolean success = authenticate();

            if (success) 
            {
                beanManager.fireEvent(new LoggedInEvent()); //X TODO beanManager.fireEvent(new LoggedInEvent(user));
                return AuthenticationResult.SUCCESS;
            }

            beanManager.fireEvent(new LoginFailedEvent(null));
            return AuthenticationResult.FAILED;
        } 
        catch (Exception ex) 
        {
            //X TODO discuss special handling of UnexpectedCredentialException
            beanManager.fireEvent(new LoginFailedEvent(ex));

            return AuthenticationResult.EXCEPTION;
        }
    }

    private boolean isAuthenticationRequestWithDifferentUserId()
    {
        return isLoggedIn() && this.loginCredential.getUserId() != null &&
                !this.loginCredential.getUserId().equals(this.user.getId());
    }

    protected boolean authenticate() throws AuthenticationException 
    {
        if (authenticating) 
        {
            authenticating = false; //X TODO discuss it
            throw new IllegalStateException("Authentication already in progress.");
        }

        try 
        {
            authenticating = true;

            beanManager.fireEvent(new PreAuthenticateEvent());

            Authenticator activeAuthenticator = authenticatorSelector.getSelectedAuthenticator();

            activeAuthenticator.authenticate();

            if (activeAuthenticator.getStatus() == null) 
            {
                throw new AuthenticationException("Authenticator must return a valid authentication status");
            }

            if (activeAuthenticator.getStatus() == AuthenticationStatus.SUCCESS)
            {
                postAuthenticate(activeAuthenticator);
                this.user = activeAuthenticator.getUser();
                return true;
            }
        } 
        catch (Exception ex) 
        {
            if (ex instanceof AuthenticationException)
            {
                throw (AuthenticationException) ex;
            } 
            else 
            {
                throw new AuthenticationException("Authentication failed.", ex);
            }
        }
        finally
        {
            authenticating = false;
        }
        return false;
    }
    
    protected void postAuthenticate(Authenticator activeAuthenticator)
    {
        activeAuthenticator.postAuthenticate();

        if (!activeAuthenticator.getStatus().equals(AuthenticationStatus.SUCCESS))
        {
            return;
        }

        beanManager.fireEvent(new PostAuthenticateEvent());
    }

    @Override
    public void logout() 
    {
        logout(true);
    }

    protected void logout(boolean invalidateLoginCredential)
    {
        if (isLoggedIn())
        {
            beanManager.fireEvent(new PreLoggedOutEvent(this.user));

            PostLoggedOutEvent postLoggedOutEvent = new PostLoggedOutEvent(this.user);

            unAuthenticate(invalidateLoginCredential);

            beanManager.fireEvent(postLoggedOutEvent);
        }
    }

    /**
     * Resets all security state and loginCredential
     */
    private void unAuthenticate(boolean invalidateLoginCredential)
    {
        this.user = null;

        if (invalidateLoginCredential)
        {
            loginCredential.invalidate();
        }
    }
}
