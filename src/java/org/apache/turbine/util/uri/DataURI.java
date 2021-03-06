package org.apache.turbine.util.uri;


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


import org.apache.turbine.util.RunData;
import org.apache.turbine.util.ServerData;

/**
 * This class can convert a simple link into a turbine relative
 * URL. It should be used to convert references for images, style
 * sheets and similar references.
 *
 * The resulting links have no query data or path info. If you need
 * this, use TurbineURI or TemplateURI.
 *
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @version $Id$
 *
 */

public class DataURI
        extends BaseURI
{
    /**
     * Empty C'tor. Uses Turbine.getDefaultServerData().
     *
     */
    public DataURI()
    {
        super();
    }

    /**
     * Constructor with a RunData object
     *
     * @param runData A RunData object
     */
    public DataURI(RunData runData)
    {
        super(runData);
    }

    /**
     * Constructor, set explicit redirection
     *
     * @param runData A RunData object
     * @param redirect True if redirection allowed.
     */
    public DataURI(RunData runData, boolean redirect)
    {
        super(runData, redirect);
    }

    /**
     * Constructor with a ServerData object
     *
     * @param serverData A ServerData object
     */
    public DataURI(ServerData serverData)
    {
        super(serverData);
    }

    /**
     * Constructor, set explicit redirection
     *
     * @param serverData A ServerData object
     * @param redirect True if redirection allowed.
     */
    public DataURI(ServerData serverData, boolean redirect)
    {
        super(serverData, redirect);
    }


    /**
     * Content Tool wants to be able to turn the encoding
     * of the servlet container off. After calling this method,
     * the encoding will not happen any longer.
     */
    public void clearResponse()
    {
        setResponse(null);
    }

    /**
     * Builds the URL with all of the data URL-encoded as well as
     * encoded using HttpServletResponse.encodeUrl(). The resulting
     * URL is absolute; it starts with http/https...
     *
     * <p>
     * <pre>
     * TurbineURI tui = new TurbineURI (data, "UserScreen");
     * tui.addPathInfo("user","jon");
     * tui.getAbsoluteLink();
     * </pre>
     *
     *  The above call to getAbsoluteLink() would return the String:
     *
     * <p>
     * http://www.server.com/servlets/Turbine/screen/UserScreen/user/jon
     *
     * @return A String with the built URL.
     */
    public String getAbsoluteLink()
    {
        StringBuilder output = new StringBuilder();

        getSchemeAndPort(output);
        getContextAndScript(output);

        if (hasReference())
        {
            output.append('#');
            output.append(getReference());
        }

        //
        // Encode Response does all the fixup for the Servlet Container
        //
        return encodeResponse(output.toString());
    }

    /**
     * Builds the URL with all of the data URL-encoded as well as
     * encoded using HttpServletResponse.encodeUrl(). The resulting
     * URL is relative to the webserver root.
     *
     * <p>
     * <pre>
     * TurbineURI tui = new TurbineURI (data, "UserScreen");
     * tui.addPathInfo("user","jon");
     * tui.getRelativeLink();
     * </pre>
     *
     *  The above call to getRelativeLink() would return the String:
     *
     * <p>
     * /servlets/Turbine/screen/UserScreen/user/jon
     *
     * @return A String with the built URL.
     */
    public String getRelativeLink()
    {
        StringBuilder output = new StringBuilder();

        getContextAndScript(output);

        if (hasReference())
        {
            output.append('#');
            output.append(getReference());
        }

        //
        // Encode Response does all the fixup for the Servlet Container
        //
        return encodeResponse(output.toString());
    }

    /**
     * toString() simply calls getAbsoluteLink. You should not use this in your
     * code unless you have to. Use getAbsoluteLink.
     *
     * @return This URI as a String
     *
     */
    @Override
    public String toString()
    {
        return getAbsoluteLink();
    }
}
