package org.apache.turbine.modules;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.util.RunData;

/**
 * This is an interface that defines what an Assembler is. All the current
 * modules extend off of this class. It is currently empty and future use is yet
 * to be determined.
 *
 * @author <a href="mailto:mbryson@mont.mindspring.com">Dave Bryson</a>
 * @author <a href="mailto:peter@courcoux.biz">Peter Courcoux</a>
 * @version $Id$
 */
public abstract class Assembler
{
    /**
     * This can go once RunData is replaced...
     *
     * @param pipelineData Turbine request data
     * @return RunData extracted from PipelineData
     */
    public final RunData getRunData(PipelineData pipelineData)
    {
        if (!(pipelineData instanceof RunData))
        {
            throw new RuntimeException("Can't cast to rundata from pipeline data.");
        }
        return (RunData) pipelineData;
    }

    /**
     * Abstract method to provide the prefix for module related classes and
     * templates
     *
     * @return the prefix
     */
    public abstract String getPrefix();
}
