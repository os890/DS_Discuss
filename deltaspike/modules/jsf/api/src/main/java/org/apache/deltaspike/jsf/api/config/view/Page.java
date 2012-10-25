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
package org.apache.deltaspike.jsf.api.config.view;

import org.apache.deltaspike.core.api.config.view.ViewMetaData;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Optional annotation to specify page specific meta-data
 */

//don't use @Inherited
@Target(TYPE)
@Retention(RUNTIME)
@Documented

@ViewMetaData
public @interface Page
{
    /**
     * Allows to specify a custom (file-)name for the page represented by the view-config
     *
     * @return name of the page
     */
    String name() default "";

    /**
     * Allows to specify the (file-)extension for the page represented by the view-config
     *
     * @return extension of the page
     */
    String extension() default Extension.DEFAULT;

    /**
     * Allows to specify navigation-mode which should be used to navigate to the page represented by the view-config
     *
     * @return navigation-mode which should be used to navigate to the page represented by the view-config
     */
    NavigationMode navigation() default NavigationMode.DEFAULT;

    /**
     * for including view params in jsf2
     *
     * @return the strategy which should be used by jsf2 for handling view-parameters (for the navigation)
     */
    ViewParameterMode viewParams() default ViewParameterMode.DEFAULT;

    /**
     * Extension of the markup file
     */
    public interface Extension
    {
        String DEFAULT = "";
        String XHTML = "xhtml";
        String JSF = "jsf";
        String FACES = "faces";
        String JSP = "jsp";
    }

    /**
     * Type of the navigation which should be used by the {@link javax.faces.application.NavigationHandler}
     */
    public enum NavigationMode
    {
        DEFAULT, FORWARD, REDIRECT
    }

    /**
     * Mode specifies if JSF2 should include view-params
     */
    public enum ViewParameterMode
    {
        DEFAULT, INCLUDE, EXCLUDE
    }
}
