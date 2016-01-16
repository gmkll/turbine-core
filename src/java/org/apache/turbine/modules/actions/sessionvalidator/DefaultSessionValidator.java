package org.apache.turbine.modules.actions.sessionvalidator;

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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.turbine.Turbine;
import org.apache.turbine.TurbineConstants;
import org.apache.turbine.annotation.TurbineConfiguration;
import org.apache.turbine.annotation.TurbineService;
import org.apache.turbine.om.security.User;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.services.security.SecurityService;
import org.apache.turbine.util.RunData;

/**
 * The SessionValidator attempts to retrieve the User object from the
 * Servlet API session that is associated with the request.  If the
 * data cannot be retrieved, it is handled here.  If the user has not
 * been marked as being logged into the system, the user is rejected
 * and the screen is set to the screen.homepage value in
 * TurbineResources.properties.
 *
 * <p>
 *
 * Other systems generally have a database table which stores this
 * information, but we take advantage of the Servlet API here to save
 * a hit to the database for each and every connection that a user
 * makes.
 *
 * <p>
 *
 * This action is special in that it should only be executed by the
 * Turbine servlet.
 *
 * @author <a href="mailto:mbryson@mont.mindspring.com">Dave Bryson</a>
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @author <a href="mailto:peter@courcoux.biz">Peter Courcoux</a>
 * @version $Id$
 */
public class DefaultSessionValidator
    extends SessionValidator
{
    /** Logging */
    private static Log log = LogFactory.getLog(DefaultSessionValidator.class);

    @TurbineService
    private SecurityService security;

    @TurbineConfiguration( TurbineConstants.LOGIN_MESSAGE )
    private String loginMessage;

    @TurbineConfiguration( TurbineConstants.SCREEN_LOGIN )
    private String screenLogin;

    @TurbineConfiguration( TurbineConstants.LOGIN_MESSAGE_NOSCREEN )
    private String loginMessageNoScreen;

    @TurbineConfiguration( TurbineConstants.SCREEN_HOMEPAGE )
    private String screenHomepage;

    @TurbineConfiguration( TurbineConstants.SCREEN_INVALID_STATE )
    private String screenInvalidState;

    /**
     * Execute the action.  The default is to populate the PipelineData
     * object and, if the user is unknown, to force a login screen (as
     * set in the tr.props).
     *
     * @see org.apache.turbine.modules.screens.error.InvalidState
     * @param pipelineData Turbine PipelineData context information.
     * @throws Exception The anonymous user could not be obtained
     *         from the security service
     */
    @Override
    public void doPerform(PipelineData pipelineData)
            throws Exception
    {
        RunData data = getRunData(pipelineData);
        // Pull user from session.
        data.populate();

        // The user may have not logged in, so create a "guest/anonymous" user.
        if (data.getUser() == null)
        {
            log.debug("Fixing up empty User Object!");
            User anonymousUser = security.getAnonymousUser();
            data.setUser(anonymousUser);
            data.save();
        }

        // Make sure the User has logged into the system.
        if (!data.getUser().hasLoggedIn())
        {
            // only set the message if nothing else has already set it
            // (e.g. the LogoutUser action).
            if (StringUtils.isEmpty(data.getMessage()))
            {
                data.setMessage(loginMessage);
            }

            // set the screen to be the login page
            data.setScreen(screenLogin);

            // We're not doing any actions buddy! (except action.login which
            // will have been performed already)
            data.setAction(null);
        }

        if (!data.hasScreen())
        {
            data.setMessage(loginMessageNoScreen);
            data.setScreen(screenHomepage);
        }

        if (data.getParameters().containsKey("_session_access_counter"))
        {
            // See comments in screens.error.InvalidState.
            if (data.getParameters().getInt("_session_access_counter")
                    < (((Integer) data.getUser().getTemp(
                    "_session_access_counter")).intValue() - 1))
            {
                data.getUser().setTemp("prev_screen", data.getScreen());
                data.getUser().setTemp("prev_parameters", data.getParameters());
                data.setScreen(screenInvalidState);
                data.setAction("");
            }
        }

        // Comply with Turbine 4.0 standards
        pipelineData.get(Turbine.class).put(User.class, data.getUser());
    }
}
