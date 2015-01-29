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

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import org.apache.deltaspike.partialbean.spi.ArchiveFinder;
import org.apache.xbean.finder.ClassLoaders;

public class DefaultArchiveFinder implements ArchiveFinder
{
    @Override
    public Set<URL> findArchives(ClassLoader loader)
    {
        try
        {
            Set<URL> deploymentUrls = ClassLoaders.findUrls(loader);

            filterExcludedUrls(deploymentUrls);

            return deploymentUrls;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    protected void filterExcludedUrls(Set<URL> classPathUrls)
    {
        Iterator<URL> it = classPathUrls.iterator();
        while (it.hasNext())
        {
            URL url = it.next();
            String path = url.toExternalForm();
            int knownJarIdx = isKnownPath(path);
            if (knownJarIdx > 0 && knownJarIdx < path.indexOf(".jar"))
            {
                it.remove();
            }
        }
    }

    protected static int isKnownPath(String path)
    {
        for (String p : Arrays.asList(
                "/jre/lib",
                "/Contents/Home/",
                "/dt.jar",
                "/tools.jar",
                "/asm-",
                "/javassist",
                "/xbean-",
                "/jconsole.jar",
                "/geronimo-",
                "/commons-",
                "/arquillian-core",
                "/arquillian-junit",
                "/arquillian-protocol",
                "/arquillian-transaction",
                "/arquillian-testenricher-cdi",
                "/arquillian-testenricher-ejb",
                "/arquillian-testenricher-msc",
                "/arquillian-testenricher-osgi",
                "/arquillian-testenricher-initialcontext",
                "/arquillian-testenricher-resource",
                "/arquillian-tomee-archive-appender",
                "/xx-arquillian-tomee-bean-discoverer",
                "/quartz-openejb-shade-",
                "/istack-commons-runtime",
                "/mbean-annotation-api-",
                "/javaee-api-",
                "/sxc-runtime-",
                "/swizzle-stream-",
                "/FastInfoset-",
                "/websocket-api",
                "/jaxb-api-osgi",
                "/jaxb-core",
                "/jaxb-impl",
                "/hsqldb-",
                "/sxc-jaxb-core",
                "/bootstrap.jar",
                "/tomee-",
                "/openejb-",
                "/bsh-",
                "/shrinkwrap-",
                "/junit-",
                "/testng-",
                "/openjpa-",
                "/bcel",
                "/hamcrest",
                "/mysql-connector",
                "/testng",
                "/idea_rt",
                "/eclipse",
                "/jcommander",
                "/tomcat",
                "/catalina",
                "/jasper",
                "/jsp-api",
                "/myfaces-",
                "/servlet-api",
                "/log4j-",
                "/wildfly-clustering",
                "/wildfly-jsf",
                "/wildfly-ejb",
                "/wildfly-security",
                "/wildfly-jberet",
                "/undertow-websockets",
                "/undertow-servlet",
                "/undertow-core",
                "/jastow-",
                "/jul-to-slf4j",
                "/jbossws-",
                "/jboss-ejb-client",
                "/jboss-vfs",
                "/jboss-as-security",
                "/jboss-as-web-",
                "/jboss-as-ee-",
                "/jboss-invocation",
                "/jboss-as-ejb3-",
                "/jboss-interceptor-core",
                "/jbossweb-",
                "/jboss-logging",
                "/jboss-classfilewriter",
                "/resteasy-",
                "/picketbox-",
                "/jberet-core",
                "/jackson-",
                "/javax",
                "/annotation-api",
                "/el-api",
                "/mojarra",
                "/jsf-impl",
                "/jsf-api",
                "/jboss-iiop",
                "/sisu-guice-",
                "/sisu-inject-",
                "/aether-",
                "/plexus-",
                "/openwebbeans-",
                "/weld",
                "/guava",
                "/hibernate",
                "/slf4j",
                "/bval",
                "/owb-arquillian-",
                "/xmlsec-",
                "/maven-artifact"))
        {
            int i = path.indexOf(p);
            if (i > 0)
            {
                return i;
            }
        }
        return -1;
    }
}
