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

package org.apache.deltaspike.jsf.api.literal;

import org.apache.deltaspike.jsf.api.config.view.Folder;

import javax.enterprise.util.AnnotationLiteral;

/**
 * Literal for {@link Folder}
 */
//TODO remove null trick once we can merge with default values and the tests pass
public class FolderLiteral extends AnnotationLiteral<Folder> implements Folder
{
    private static final long serialVersionUID = 2582580975876369665L;

    private final String name;

    public FolderLiteral(boolean virtual)
    {
        if (virtual)
        {
            this.name = null;
        }
        else
        {
            this.name = "";
        }
    }

    public FolderLiteral(String name)
    {
        this.name = name;
    }

    @Override
    public String name()
    {
        return this.name;
    }

    /*
    * generated
    */

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        FolderLiteral that = (FolderLiteral) o;

        if (name != null ? !name.equals(that.name) : that.name != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return name != null ? name.hashCode() : 0;
    }
}
