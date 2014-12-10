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
package org.apache.deltaspike.test.scheduler.custom;

import org.apache.deltaspike.core.spi.activation.ClassDeactivator;
import org.apache.deltaspike.core.spi.config.ConfigSource;
import org.apache.deltaspike.scheduler.impl.SchedulerBaseConfig;

import java.util.HashMap;
import java.util.Map;

public class CustomConfigSource implements ConfigSource
{
    private Map<String, String> config = new HashMap<String, String>()
    {{
        put(SchedulerBaseConfig.Job.JOB_CLASS_NAME.getKey(), CustomJob.class.getName());
        put(ClassDeactivator.class.getName(), QuartzDeactivator.class.getName());
    }};

    @Override
    public int getOrdinal()
    {
        return 1001;
    }

    @Override
    public Map<String, String> getProperties()
    {
        return this.config;
    }

    @Override
    public String getPropertyValue(String key)
    {
        return this.config.get(key);
    }

    @Override
    public String getConfigName()
    {
        return "scheduler-test-config";
    }

    @Override
    public boolean isScannable()
    {
        return true;
    }
}
