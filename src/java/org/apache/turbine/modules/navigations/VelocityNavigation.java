package org.apache.turbine.modules.navigations;


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


import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.apache.turbine.modules.Navigation;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.services.template.TurbineTemplate;
import org.apache.turbine.services.velocity.TurbineVelocity;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;

/**
 * VelocityNavigation.  This screen relies on the VelocityPage
 * being set as the default page.  The doBuildTemplate() assumes the
 * user has put the template filename in the RunData parameter and set
 * it to the value of the template file to execute.  Specialized
 * Navigations screens should extend this class and override the
 * doBuildTemplate( data , context) method.
 *
 * @author <a href="mailto:mbryson@mont.mindspring.com">Dave Bryson</a>
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @author <a href="mailto:peter@courcoux.biz">Peter Courcoux</a>
 * @version $Id$
 */
public class VelocityNavigation
        extends TemplateNavigation
{
    /** The prefix for lookup up navigation pages */
    private final String prefix = Navigation.PREFIX + "/";

    /**
     * Velocity Navigations extending this class should overide this
     * method to perform any particular business logic and add
     * information to the context.
     *
     * @deprecated Use PipelineData version instead.
     * @param data Turbine information.
     * @param context Context for web pages.
     * @exception Exception, a generic exception.
     */
    @Deprecated
    protected void doBuildTemplate(RunData data,
                                   Context context)
            throws Exception
    {
        // empty
    }

    /**
     * Velocity Navigations extending this class should overide this
     * method to perform any particular business logic and add
     * information to the context.
     *
     * @param data Turbine information.
     * @param context Context for web pages.
     * @exception Exception, a generic exception.
     */
    protected void doBuildTemplate(PipelineData pipelineData,
                                   Context context)
            throws Exception
    {
        // empty
    }

    /**
     * Needs to be implemented to make TemplateNavigation like us.
     * The actual method that you should override is the one with the
     * context in the parameter list.
     *
     * @deprecated Use PipelineData version instead.
     * @param data Turbine information.
     * @exception Exception, a generic exception.
     */
    @Deprecated
    @Override
    protected void doBuildTemplate(RunData data)
            throws Exception
    {
        doBuildTemplate(data, TurbineVelocity.getContext(data));
    }


    /**
     * Needs to be implemented to make TemplateNavigation like us.
     * The actual method that you should override is the one with the
     * context in the parameter list.
     *
     * @param pipelineData Turbine information.
     * @exception Exception, a generic exception.
     */
    @Override
    protected void doBuildTemplate(PipelineData pipelineData)
            throws Exception
    {
        doBuildTemplate(pipelineData, TurbineVelocity.getContext(pipelineData));
    }

    /**
     * This Builds the Velocity template.
     *
     * @deprecated Use PipelineData version instead.
     * @param data Turbine information.
     * @return A ConcreteElement.
     * @exception Exception, a generic exception.
     */
    @Deprecated
    @Override
    public ConcreteElement buildTemplate(RunData data)
            throws Exception
    {
        Context context = TurbineVelocity.getContext(data);

        String navigationTemplate = data.getTemplateInfo().getNavigationTemplate();
        String templateName
                = TurbineTemplate.getNavigationTemplateName(navigationTemplate);

        StringElement output = new StringElement();
        output.setFilterState(false);
        output.addElement(TurbineVelocity
                .handleRequest(context, prefix + templateName));
        return output;
    }

    /**
     * This Builds the Velocity template.
     *
     * @param data Turbine information.
     * @return A ConcreteElement.
     * @exception Exception, a generic exception.
     */
    @Override
    public ConcreteElement buildTemplate(PipelineData pipelineData)
            throws Exception
    {
        RunData data = getRunData(pipelineData);
        Context context = TurbineVelocity.getContext(pipelineData);

        String navigationTemplate = data.getTemplateInfo().getNavigationTemplate();
        String templateName
                = TurbineTemplate.getNavigationTemplateName(navigationTemplate);

        StringElement output = new StringElement();
        output.setFilterState(false);
        output.addElement(TurbineVelocity
                .handleRequest(context, prefix + templateName));
        return output;
    }


}
