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
package org.apache.deltaspike.test.jpa.api.transactional.getRollbackOnly.multipleinjection.auto;

import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.util.ProjectStageProducer;
import org.apache.deltaspike.jpa.impl.transaction.context.TransactionContextExtension;
import org.apache.deltaspike.test.jpa.api.shared.TestEntityManager;
import org.apache.deltaspike.test.jpa.api.shared.TestEntityTransaction;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

//different classes needed due to arquillian restriction
@RunWith(Arquillian.class)
public class MultipleEntityManagerInjectionRollbackOnly1Test
{
    @Inject
    private MultiTransactionBean multiTransactionBean;

    @Inject
    private TestEntityManagerProducer entityManagerProducer;

    @Deployment
    public static WebArchive deploy()
    {
        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, "autoInjectionRollbackOnly1Test.jar")
                .addPackage(ArchiveUtils.SHARED_PACKAGE)
                .addPackage(MultipleEntityManagerInjectionRollbackOnly1Test.class.getPackage().getName())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        return ShrinkWrap.create(WebArchive.class)
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndJpaArchive())
                .addAsLibraries(testJar)
                .addAsServiceProvider(Extension.class, TransactionContextExtension.class)
                .addAsWebInfResource(ArchiveUtils.getBeansXml(), "beans.xml");
    }

    @Before
    public void init()
    {
        ProjectStageProducer.setProjectStage(ProjectStage.UnitTest);
    }

    @Test
    public void autoInjectionRollbackOnly1Test()
    {
        TestEntityManager defaultEntityManager = entityManagerProducer.getDefaultEntityManager();
        TestEntityManager firstEntityManager = entityManagerProducer.getFirstEntityManager();
        TestEntityManager secondEntityManager = entityManagerProducer.getSecondEntityManager();

        Assert.assertNotNull(defaultEntityManager);
        TestEntityTransaction defaultTransaction = (TestEntityTransaction) (defaultEntityManager).getTransaction();

        Assert.assertEquals(false, defaultEntityManager.isFlushed());
        Assert.assertEquals(false, defaultTransaction.isActive());
        Assert.assertEquals(false, defaultTransaction.isStarted());
        Assert.assertEquals(false, defaultTransaction.isCommitted());
        Assert.assertEquals(false, defaultTransaction.isRolledBack());

        Assert.assertNotNull(firstEntityManager);
        TestEntityTransaction firstTransaction = (TestEntityTransaction) (firstEntityManager).getTransaction();

        Assert.assertEquals(false, firstEntityManager.isFlushed());
        Assert.assertEquals(false, firstTransaction.isActive());
        Assert.assertEquals(false, firstTransaction.isStarted());
        Assert.assertEquals(false, firstTransaction.isCommitted());
        Assert.assertEquals(false, firstTransaction.isRolledBack());

        Assert.assertNotNull(secondEntityManager);
        TestEntityTransaction secondTransaction = (TestEntityTransaction) (secondEntityManager).getTransaction();

        Assert.assertEquals(false, secondEntityManager.isFlushed());
        Assert.assertEquals(false, secondTransaction.isActive());
        Assert.assertEquals(false, secondTransaction.isStarted());
        Assert.assertEquals(false, secondTransaction.isCommitted());
        Assert.assertEquals(false, secondTransaction.isRolledBack());

        multiTransactionBean.executeInTransactionRollback1();

        Assert.assertEquals(true, defaultEntityManager.isFlushed());
        Assert.assertEquals(false, defaultTransaction.isActive());
        Assert.assertEquals(true, defaultTransaction.isStarted());
        Assert.assertEquals(false, defaultTransaction.isCommitted());
        Assert.assertEquals(true, defaultTransaction.isRolledBack());

        Assert.assertEquals(true, firstEntityManager.isFlushed());
        Assert.assertEquals(false, firstTransaction.isActive());
        Assert.assertEquals(true, firstTransaction.isStarted());
        Assert.assertEquals(false, firstTransaction.isCommitted());
        Assert.assertEquals(true, firstTransaction.isRolledBack());

        Assert.assertEquals(true, secondEntityManager.isFlushed());
        Assert.assertEquals(false, secondTransaction.isActive());
        Assert.assertEquals(true, secondTransaction.isStarted());
        Assert.assertEquals(false, secondTransaction.isCommitted());
        Assert.assertEquals(true, secondTransaction.isRolledBack());
    }
}
