package org.apache.turbine.services.ui;

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

import org.apache.turbine.services.Service;
import org.apache.turbine.util.ServerData;

/**
 * The UI service provides for shared access to User Interface (skin) files,
 * as well as the ability for non-default skin files to inherit properties from 
 * a default skin.  Use TurbineUI to access skin properties from your screen 
 * classes and action code. UITool is provided as a pull tool for accessing 
 * skin properties from your templates.
 * 
 * <p>Skins are lazy loaded in that they are not loaded until first used.
 * 
 * @author <a href="mailto:seade@backstagetech.com.au">Scott Eade</a>
 * @version $Id$
 * @see UIService
 * @see UITool
 */
public interface UIService extends Service
{
    /**
     * The service identifier.
     */
    public String SERVICE_NAME = "UIService";
    
    /**
     * Refresh all skins.
     */
    public void refresh();

    /**
     * Refresh a particular skin.
     * 
     * @param skinName the name of the skin to clear.
     */
    public void refresh(String skinName);

    /**
     * Provide access to the list of available skin names.
     * 
     * @return the available skin names.
     */
    public String[] getSkinNames();

    /**
     * Get the name of the default skin name for the web application from the 
     * TurbineResources.propertiess file. If the property is not present the 
     * name of the default skin will be returned.  Note that the web application
     * skin name may be something other than default, in which case its 
     * properties will default to the skin with the name "default".
     * 
     * @return the name of the default skin for the web application.
     */
    public String getWebappSkinName();

    /**
     * Retrieve a skin property from the named skin.  If the property is not 
     * defined in the named skin the value for the default skin will be 
     * provided.  If the named skin does not exist then the skin configured for 
     * the webapp will be used.  If the webapp skin does not exist the default
     * skin will be used.  If the default skin does not exist then 
     * <code>null</code> will be returned.
     * 
     * @param skinName the name of the skin to retrieve the property from.
     * @param key the key to retrieve from the skin.
     * @return the value of the property for the named skin (defaulting to the 
     * default skin), the webapp skin, the default skin or <code>null</code>,
     * depending on whether or not the property or skins exist.
     */
    public String get(String skinName, String key);

    /**
     * Retrieve the URL for an image that is part of a skin. The images are 
     * stored in the WEBAPP/resources/ui/skins/[SKIN]/images directory.
     *
     * <p>Use this if for some reason your server name, server scheme, or server 
     * port change on a per request basis. I'm not sure if this would happend in 
     * a load balanced situation. I think in most cases the image(String image)
     * method would probably be enough, but I'm not absolutely positive.
     * 
     * @param skinName the name of the skin to retrieve the image from.
     * @param imageId the id of the image whose URL will be generated.
     * @param serverData the serverData to use as the basis for the URL.
     */
    public String image(String skinName, String imageId, ServerData serverData);

    /**
     * Retrieve the URL for an image that is part of a skin. The images are 
     * stored in the WEBAPP/resources/ui/skins/[SKIN]/images directory.
     * 
     * @param skinName the name of the skin to retrieve the image from.
     * @param imageId the id of the image whose URL will be generated.
     */
    public String image(String skinName, String imageId);

    /**
     * Retrieve the URL for the style sheet that is part of a skin. The style is 
     * stored in the WEBAPP/resources/ui/skins/[SKIN] directory with the 
     * filename skin.css
     *
     * <p>Use this if for some reason your server name, server scheme, or server 
     * port change on a per request basis. I'm not sure if this would happend in 
     * a load balanced situation. I think in most cases the style() method would 
     * probably be enough, but I'm not absolutely positive.
     * 
     * @param skinName the name of the skin to retrieve the style sheet from.
     * @param serverData the serverData to use as the basis for the URL.
     */
    public String getStylecss(String skinName, ServerData serverData);

    /**
     * Retrieve the URL for the style sheet that is part of a skin. The style is 
     * stored in the WEBAPP/resources/ui/skins/[SKIN] directory with the 
     * filename skin.css
     * 
     * @param skinName the name of the skin to retrieve the style sheet from.
     */
    public String getStylecss(String skinName);

}