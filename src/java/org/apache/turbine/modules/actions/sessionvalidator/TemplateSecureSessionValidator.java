package org.apache.turbine.modules.actions.sessionvalidator;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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

// Turbine Classes
import org.apache.turbine.TurbineConstants;
import org.apache.turbine.util.RunData;
import org.apache.turbine.services.resources.TurbineResources;

/**
 * SessionValidator that requires login for use with the WebMacroSite
 * Service.
 *
 * <br>
 *
 * The WebMacroSite Service requires a different Session Validator
 * because of the way it handles screens.  If you use the WebMacroSite
 * Service with the DefaultSessionValidator, users will be able to
 * bypass login by directly addressing the template using
 * template/index.wm.  This is because WebMacroSitePage looks for the
 * keyword "template" in the Path information and if it finds it will
 * reset the screen using it's lookup mechanism and thereby bypass
 * Login.
 *
 * Note that you will need to set the template.login property to the
 * login template.
 *
 * @author <a href="mailto:john.mcnally@clearink.com">John D. McNally</a>
 * @author <a href="mailto:mbryson@mont.mindspring.com">Dave Bryson</a>
 * @version $Id$
 */
public class TemplateSecureSessionValidator extends SessionValidator
{
    /**
     * doPerform is virtually identical to DefaultSessionValidator
     * except that it calls template methods instead of bare screen
     * methods. For example, it uses <code>setScreenTemplate</code> to
     * load the tr.props TEMPLATE_LOGIN instead of the default's
     * setScreen to TurbineContants.SCREEN_LOGIN.
     *
     * @see DefaultSessionValidator     * @param data Turbine information.
     * @exception Exception, a generic exception.
     */
    public void doPerform( RunData data ) throws Exception
    {
        /*
         * Pull user from session.
         */
        data.populate();

        /*
         * This is the secure sessionvalidator, so user must be logged in.
         */
        if ( (data.getUser() == null) || (! data.getUser().hasLoggedIn()) )
        {
            /*
             * Only set the message if nothing else has already set it
             * (e.g. the LogoutUser action).
             */
            if (data.getMessage() == null)
            {
                data.setMessage(TurbineResources.getString(
                    TurbineConstants.LOGIN_MESSAGE));
            }

            /*
             * Set the screen template to the login page.
             */
            data.getTemplateInfo().setScreenTemplate(
                TurbineResources.getString(TurbineConstants.TEMPLATE_LOGIN) );

            /*
             * We're not doing any actions buddy! (except action.login which
             * will have been performed already)
             */
            data.setAction(null);
        }

        /*
         * Make sure we have some way to return a response.
         */
        if ( !data.hasScreen() &&
              data.getTemplateInfo().getScreenTemplate() == null )
        {
            String template = TurbineResources.getString(
                TurbineConstants.TEMPLATE_HOMEPAGE);

            if (template != null)
            {
                data.getTemplateInfo().setScreenTemplate(template);
            }
            else
            {
                data.setScreen(TurbineResources.getString(
                    TurbineConstants.SCREEN_HOMEPAGE));
            }
        }

        /*
         * The session_access_counter can be placed as a hidden field in
         * forms.  This can be used to prevent a user from using the
         * browsers back button and submitting stale data.
         * FIXME!! a template needs to be written to use this with templates.
         */
        else if ( data.getParameters().containsKey("_session_access_counter") )
        {
            /*
             * See comments in screens.error.InvalidState.
             */
            if ( data.getParameters().getInt("_session_access_counter") <
                 (((Integer)data.getUser().getTemp("_session_access_counter"))
                 .intValue()-1) )
            {
                if (data.getTemplateInfo().getScreenTemplate() != null)
                {
                    data.getUser().setTemp( "prev_template",
                        data.getTemplateInfo().getScreenTemplate() );
                    data.getTemplateInfo().setScreenTemplate(
                        TurbineResources.getString(
                            TurbineConstants.TEMPLATE_INVALID_STATE) );
                }
                else
                {
                    data.setScreen( TurbineResources.getString(
                        TurbineConstants.SCREEN_INVALID_STATE) );
                    data.getUser().setTemp( "prev_screen", data.getScreen() );
                }
                data.getUser()
                    .setTemp( "prev_parameters", data.getParameters() );
                data.setAction( "" );
            }
        }

        /*
         * We do not want to allow both a screen and template parameter.
         * The template parameter is dominant.
         */
        if ( data.getTemplateInfo().getScreenTemplate() != null )
        {
            data.setScreen(null);
        }
    }

    /**
     * By default, this is true. It says that we require a new session
     * in order to allow people to access the system. We accomplish
     * this by doing a redirect and using the HttpSession spec.
     *
     * @param data Turbine information.
     * @return True if we require a new session in order to allow
     * people to access the system.
     */
    public boolean requiresNewSession(RunData data)
    {
        return true;
    }
}
