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
package org.apache.deltaspike.test.jpa.api.transactional.getRollbackOnly.multipleinjection.nested;

import org.apache.deltaspike.jpa.api.Transactional;
import org.apache.deltaspike.test.jpa.api.shared.Second;
import org.apache.deltaspike.test.jpa.api.shared.TestException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

@ApplicationScoped
public class NestedTransactionBean
{
    @Inject
    private @Second EntityManager secondEntityManager;

    @Transactional
    public void executeInTransaction()
    {
        secondEntityManager.getTransaction().setRollbackOnly();
    }
}
