package org.apache.turbine.modules.screens;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import org.apache.commons.logging.Log;

import org.apache.commons.lang.StringUtils;

import org.apache.ecs.ConcreteElement;

import org.apache.turbine.TurbineConstants;
import org.apache.turbine.services.jsp.TurbineJsp;
import org.apache.turbine.services.template.TurbineTemplate;
import org.apache.turbine.util.RunData;

/**
 * Base JSP Screen that should be subclassed by screens that want to
 * use JSP.  Subclasses should override the doBuildTemplate() method.
 *
 * @author <a href="mailto:john.mcnally@clearink.com">John D. McNally</a>
 * @author Frank Y. Kim
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @version $Id$
 */
public class BaseJspScreen
        extends TemplateScreen
{
    /** The prefix for lookup up screen pages */
    private String prefix = TurbineConstants.SCREEN_PREFIX + "/";

    /**
     * Method that sets up beans and forward the request to the JSP.
     *
     * @param data Turbine information.
     * @return null - the JSP sends the information.
     * @exception Exception, a generic exception.
     */
    public ConcreteElement buildTemplate(RunData data)
            throws Exception
    {
        String screenTemplate = data.getTemplateInfo().getScreenTemplate();
        // get the name of the JSP we want to use
        String templateName
            = TurbineTemplate.getScreenTemplateName(screenTemplate);

        // The Template Service could not find the Screen
        if (StringUtils.isEmpty(templateName))
        {
            log.error("Screen " + screenTemplate + " not found!");
            throw new Exception("Could not find screen for " + screenTemplate);
        }

        // let service know whether we are using a layout
        TurbineJsp.handleRequest(data, prefix + templateName,
                                 getLayout(data) == null);

        return null;
    }

    /**
     * Method to be overidden by subclasses to include data in beans, etc.
     *
     * @param data, the Rundata object
     * @exception Exception, a generic exception.
     */
    protected void doBuildTemplate(RunData data)
        throws Exception
    {
    }
}
