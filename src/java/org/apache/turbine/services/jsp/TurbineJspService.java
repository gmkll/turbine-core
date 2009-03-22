package org.apache.turbine.services.jsp;


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


import java.io.File;
import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.turbine.Turbine;
import org.apache.turbine.services.InitializationException;
import org.apache.turbine.services.pull.ApplicationTool;
import org.apache.turbine.services.pull.tools.TemplateLink;
import org.apache.turbine.services.template.BaseTemplateEngineService;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.TurbineException;

/**
 * This is a Service that can process JSP templates from within a Turbine
 * screen.
 *
 * @author <a href="mailto:john.mcnally@clearink.com">John D. McNally</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 */
public class TurbineJspService
        extends BaseTemplateEngineService
        implements JspService
{
    /** The base path[s] prepended to filenames given in arguments */
    private String[] templatePaths;

    /** The relative path[s] prepended to filenames */
    private String[] relativeTemplatePaths;

    /** The buffer size for the output stream. */
    private int bufferSize;

    /** Logging */
    private static Log log = LogFactory.getLog(TurbineJspService.class);

    /**
     * Load all configured components and initialize them. This is
     * a zero parameter variant which queries the Turbine Servlet
     * for its config.
     *
     * @throws InitializationException Something went wrong in the init
     *         stage
     */
    public void init()
        throws InitializationException
    {
        try
        {
            initJsp();
            registerConfiguration(JspService.JSP_EXTENSION);
            setInit(true);
        }
        catch (Exception e)
        {
            throw new InitializationException(
                "TurbineJspService failed to initialize", e);
        }
    }

    /**
     * Performs early initialization of this Turbine service.
     *
     * @param config The ServletConfiguration from Turbine
     *
     * @throws InitializationException Something went wrong when starting up.
     * @deprecated use init() instead.
     */
    public void init(ServletConfig config)
        throws InitializationException
    {
        init();
    }

    /**
     * Adds some convenience objects to the request.  For example an instance
     * of TemplateLink which can be used to generate links to other templates.
     *
     * @param data the turbine rundata object
     */
    public void addDefaultObjects(RunData data)
    {
        HttpServletRequest req = data.getRequest();

        //
        // This is a place where an Application Pull Tool is used
        // in a regular Java Context. We have no Pull Service with the
        // Jsp Paging stuff, but we can run our Application Tool by Hand:
        //
        ApplicationTool templateLink = new TemplateLink();
        templateLink.init(data);

        req.setAttribute(LINK, templateLink);
        req.setAttribute(RUNDATA, data);
    }

    /**
     * Returns the default buffer size of the JspService
     *
     * @return The default buffer size.
     */
    public int getDefaultBufferSize()
    {
        return bufferSize;
    }

    /**
     * executes the JSP given by templateName.
     *
     * @param data A RunData Object
     * @param templateName the filename of the template.
     * @throws TurbineException Any exception thrown while processing will be
     *         wrapped into a TurbineException and rethrown.
     */
    public void handleRequest(RunData data, String templateName)
        throws TurbineException
    {
        handleRequest(data, templateName, false);
    }

    /**
     * executes the JSP given by templateName.
     *
     * @param data A RunData Object
     * @param templateName the filename of the template.
     * @param isForward whether to perform a forward or include.
     * @throws TurbineException Any exception trown while processing will be
     *         wrapped into a TurbineException and rethrown.
     */
    public void handleRequest(RunData data, String templateName, boolean isForward)
        throws TurbineException
    {
        /** template name with relative path */
        String relativeTemplateName = getRelativeTemplateName(templateName);

        if (StringUtils.isEmpty(relativeTemplateName))
        {
            throw new TurbineException(
                "Template " + templateName + " not found in template paths");
        }

        // get the RequestDispatcher for the JSP
        RequestDispatcher dispatcher = data.getServletContext()
            .getRequestDispatcher(relativeTemplateName);
        
        try
        {
            if (isForward)
            {
                // forward the request to the JSP
                dispatcher.forward(data.getRequest(), data.getResponse());
            }
            else
            {
                data.getResponse().getWriter().flush();
                // include the JSP
                dispatcher.include(data.getRequest(), data.getResponse());
            }
        }
        catch (Exception e)
        {
            // as JSP service is in Alpha stage, let's try hard to send the
            // error message to the browser, to speed up debugging
            try
            {
                data.getResponse().getWriter().print("Error encountered processing a template: "
                    + templateName);
                e.printStackTrace(data.getResponse().getWriter());
            }
            catch (IOException ignored)
            {
            }

            // pass the exception to the caller according to the general
            // contract for tamplating services in Turbine
            throw new TurbineException(
                "Error encountered processing a template: " + templateName, e);
        }
    }

    /**
     * This method sets up the template cache.
     */
    private void initJsp()
        throws Exception
    {
        Configuration config = getConfiguration();

        // Set relative paths from config.
        // Needed for javax.servlet.RequestDispatcher
        relativeTemplatePaths = config.getStringArray(TEMPLATE_PATH_KEY);

        // Use Turbine Servlet to translate the template paths.
        templatePaths = new String [relativeTemplatePaths.length];
        for (int i=0; i < relativeTemplatePaths.length; i++)
        {
            relativeTemplatePaths[i] = warnAbsolute(relativeTemplatePaths[i]);

            templatePaths[i] = Turbine.getRealPath(relativeTemplatePaths[i]);
        }

        bufferSize = config.getInt(JspService.BUFFER_SIZE_KEY,
            JspService.BUFFER_SIZE_DEFAULT);
    }

    /**
     * Determine whether a given template is available on the
     * configured template pathes.
     *
     * @param template The name of the requested Template
     * @return True if the template is available.
     */
    public boolean templateExists(String template)
    {
        for (int i = 0; i < templatePaths.length; i++)
        {
            if (templateExists(templatePaths[i], template))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine whether a given template exists on the supplied
     * template path. This service ATM only supports file based
     * templates so it simply checks for file existence.
     *
     * @param path The absolute (file system) template path
     * @param template The name of the requested Template
     * @return True if the template is available.
     */
    private boolean templateExists(String path, String template)
    {
        return new File(path, template).exists();
    }

    /**
     * Searchs for a template in the default.template path[s] and
     * returns the template name with a relative path which is
     * required by <a href="http://java.sun.com/products/servlet/2.3/javadoc/javax/servlet/ServletContext.html#getRequestDispatcher(java.lang.String)">
     * javax.servlet.RequestDispatcher</a>
     *
     * @param template
     * @return String
     */
    public String getRelativeTemplateName(String template)
    {
        template = warnAbsolute(template);

        // Find which template path the template is in
        // We have a 1:1 match between relative and absolute
        // pathes so we can use the index for translation.
        for (int i = 0; i < templatePaths.length; i++)
        {
            if (templateExists(templatePaths[i], template))
            {
                return relativeTemplatePaths[i] + "/" + template;
            }
        }
        return null;
    }

    /**
     * Warn if a template name or path starts with "/".
     *
     * @param template The template to test
     * @return The template name with a leading / stripped off
     */
    private String warnAbsolute(String template)
    {
        if (template.startsWith("/"))
        {
            log.warn("Template " + template
                + " has a leading /, which is wrong!");
            return template.substring(1);
        }
        return template;
    }
}
