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
package org.apache.deltaspike.partialbean.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import org.apache.xbean.finder.archive.Archive;
import org.apache.xbean.finder.archive.FileArchive;
import org.apache.xbean.finder.archive.JarArchive;
import org.jboss.vfs.VirtualFile;

public class VfsArchive implements Archive
{
    private Archive archive;

    public VfsArchive(ClassLoader classLoader, URL url)
    {
        try
        {
            URLConnection vfsConnection = url.openConnection();
            VirtualFile vf = (VirtualFile) vfsConnection.getContent();
            File contentsFile = vf.getPhysicalFile();

            if (url.toExternalForm().contains(".jar"))
            {
                // for a jar file, the directory contains the "contents" directory and the original jar file
                for (File file : contentsFile.getParentFile().listFiles())
                {
                    if (file.isFile())
                    {
                        archive = new JarArchive(classLoader, file.toURI().toURL());
                    }
                }

                if (archive == null)
                {
                    throw new RuntimeException("Could not get original jar file from vfs!");
                }
            }
            else
            {
                archive = new FileArchive(classLoader, contentsFile.toURI().toURL());
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not handle vfs url!", e);
        }
    }

    @Override
    public InputStream getBytecode(String className) throws IOException, ClassNotFoundException
    {
        return archive.getBytecode(className);
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException
    {
        return archive.loadClass(className);
    }

    @Override
    public Iterator<Entry> iterator()
    {
        return archive.iterator();
    }
}
