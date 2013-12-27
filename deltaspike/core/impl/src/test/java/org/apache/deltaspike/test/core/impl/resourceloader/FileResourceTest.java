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
package org.apache.deltaspike.test.core.impl.resourceloader;


import org.apache.deltaspike.core.api.literal.ExternalResourceLiteral;
import org.apache.deltaspike.core.api.resoureloader.FileSystemStorage;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@RunWith(Arquillian.class)
public class FileResourceTest
{
    private static String tempFileName;

    @BeforeClass
    public static void createTempFile()
    {
        String javaiotmpdir = System.getProperty("java.io.tmpdir","/tmp");
        File tmpDir = new File(javaiotmpdir);
        try
        {
            File f = File.createTempFile("props",System.currentTimeMillis()+"",tmpDir);
            FileWriter fw = new FileWriter(f);
            fw.write("some.propertykey=somevalue");
            fw.close();
            tempFileName = f.getAbsolutePath();
            f.deleteOnExit();
        }
        catch (IOException e)
        {

        }
    }

    @Deployment
    public static Archive<?> createResourceLoaderArchive()
    {
        Archive<?> arch = ShrinkWrap.create(WebArchive.class, "resourceloader.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive());
        return arch;
    }

    @Inject
    @Any
    private Instance<InputStream> inputStreamInst;

    @Inject
    @Any
    private Instance<Properties> propsInst;

    @Test
    public void testInputStream() throws IOException
    {
        InputStream inputStream = inputStreamInst
                .select(new ExternalResourceLiteral(FileSystemStorage.class, tempFileName)).get();
        Assert.assertNotNull(inputStream);
        Properties p = new Properties();
        p.load(inputStream);
        Assert.assertEquals("somevalue", p.getProperty("some.propertykey", "wrong answer"));
    }

    @Test
    public void testProperties()
    {
        Properties props = this.propsInst.select(new ExternalResourceLiteral(FileSystemStorage.class,tempFileName))
                .get();
        Assert.assertEquals("somevalue", props.getProperty("some.propertykey", "wrong answer"));
    }
}