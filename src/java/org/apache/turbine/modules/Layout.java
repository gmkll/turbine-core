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

/**
 * This is the base class that defines what a Layout module is.
 *
 * @author <a href="mailto:mbryson@mont.mindspring.com">Dave Bryson</a>
 * @author <a href="mailto:peter@courcoux.biz">Peter Courcoux</a>
 * @version $Id$
 */
public abstract class Layout
    extends Assembler
{
    /** Prefix for layout related classes and templates */
    public static final String PREFIX = "layouts";

    /** Property for the size of the layout cache if caching is on */
    public static final String CACHE_SIZE_KEY = "layout.cache.size";

    /** The default size for the layout cache */
    public static final int CACHE_SIZE_DEFAULT = 10;

    /** Represents Layout Objects */
    public static final String NAME = "layout";

    /**
     * @see org.apache.turbine.modules.Assembler#getPrefix()
     */
    @Override
    public String getPrefix()
    {
        return PREFIX;
    }

    /**
     * A subclass must override this method to perform itself.  The
     * Action can also set the screen that is associated with PipelineData.
     * @param pipelineData Turbine information.
     * @throws Exception a generic exception.
     */
    protected abstract void doBuild(PipelineData pipelineData) throws Exception;

    /**
     * Subclasses can override this method to add additional
     * functionality.  This method is protected to force clients to
     * use ActionLoader to perform an Action.
     *
     * @param pipelineData Turbine information.
     * @throws Exception a generic exception.
     */
    protected void build(PipelineData pipelineData) throws Exception
    {
        doBuild(pipelineData);
    }
}
