package org.apache.turbine.services.template.mapper;


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


import org.apache.commons.lang.StringUtils;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.services.template.TemplateEngineService;
import org.apache.turbine.services.template.TemplateService;

/**
 * This is a pretty simple mapper which returns template pathes for
 * a supplied template name. This path can be used by the TemplateEngine
 * to access a certain resource to actually render the template.
 *
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @version $Id$
 */

public class ScreenTemplateMapper
    extends BaseTemplateMapper
    implements Mapper
{
    /**
     * Default C'tor. If you use this C'tor, you must use
     * the bean setter to set the various properties needed for
     * this mapper before first usage.
     */
    public ScreenTemplateMapper()
    {
        super();
    }

    /**
     * Check, whether the provided name exists. Returns null
     * if the screen does not exist.
     *
     * @param template The template name.
     * @return The matching screen name.
     */
    @Override
    public String doMapping(String template)
    {
        String [] components = StringUtils.split(template, String.valueOf(TemplateService.TEMPLATE_PARTS_SEPARATOR));

        // Last element decides, which template Service to use...
        TemplateService templateService = (TemplateService)TurbineServices.getInstance().getService(TemplateService.SERVICE_NAME);
        TemplateEngineService tes = templateService.getTemplateEngineService(components[components.length - 1]);

        String templatePackage = StringUtils.join(components, String.valueOf(separator));

        // But the Templating service must look for the name with prefix
        StringBuilder testPath = new StringBuilder();
        if (StringUtils.isNotEmpty(prefix))
        {
            testPath.append(prefix);
            testPath.append(separator);
        }
        testPath.append(templatePackage);

        return (tes != null && tes.templateExists(testPath.toString()))
            ? templatePackage
            : null;
    }
}




