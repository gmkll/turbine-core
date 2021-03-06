package org.apache.turbine.services.session;


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


import java.util.Collection;

import javax.servlet.http.HttpSession;

import org.apache.turbine.annotation.TurbineService;
import org.apache.turbine.om.security.User;
import org.apache.turbine.services.pull.ApplicationTool;

/**
 * A pull tool for accessing the SessionService from a velocity template.
 *
 * @author <a href="mailto:quintonm@bellsouth.net">Quinton McCombs</a>
 * @version $Id$
 */
public class SessionTool
        implements ApplicationTool
{
    /**
     * The session service.
     */
    @TurbineService
    private SessionService sessionService;

    @Override
    public void init(Object o)
    {
        // empty
    }

    @Override
    public void refresh()
    {
        // empty
    }

    /**
     * Gets a list of the active sessions
     *
     * @return List of HttpSession objects
     */
    public Collection<HttpSession> getActiveSessions()
    {
        return sessionService.getActiveSessions();
    }

    /**
     * Adds a session to the current list.  This method should only be
     * called by the listener.
     *
     * @param session Session to add
     */
    public void addSession(HttpSession session)
    {
        sessionService.addSession(session);
    }

    /**
     * Removes a session from the current list.  This method should only be
     * called by the listener.
     *
     * @param session Session to remove
     */
    public void removeSession(HttpSession session)
    {
        sessionService.removeSession(session);
    }

    /**
     * Determines if a given user is currently logged in.  The actual
     * implementation of the User object must implement the equals()
     * method.  By default, Torque based objects (liek TurbineUser)
     * have an implementation of equals() that will compare the
     * result of getPrimaryKey().
     *
     * @param user User to check for
     * @return true if the user is logged in on one of the
     * active sessions.
     */
    public boolean isUserLoggedIn(User user)
    {
        return sessionService.isUserLoggedIn(user);
    }

    /**
     * Gets a collection of all user objects representing the users currently
     * logged in.  This will exclude any instances of anonymous user that
     * Turbine will use before the user actually logs on.
     *
     * @return collection of org.apache.turbine.om.security.User objects
     */
    public Collection<User> getActiveUsers()
    {
        return sessionService.getActiveUsers();
    }

    /**
     * Gets the User object of the the specified HttpSession.
     *
     * @param session
     * @return the user from the session
     */
    public User getUserFromSession(HttpSession session)
    {
        return sessionService.getUserFromSession(session);
    }

    /**
     * Get a collection of all session on which the given user
     * is logged in.
     *
     * @param user the user
     * @return Collection of HtttSession objects
     */
    public Collection<HttpSession> getSessionsForUser(User user)
    {
        return sessionService.getSessionsForUser(user);
    }
}
