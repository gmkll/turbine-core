package org.apache.turbine.services.session;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.http.HttpSession;

import org.apache.turbine.om.security.User;
import org.apache.turbine.services.TurbineBaseService;

/**
 * The SessionService allows thread-safe access to the current
 * sessions of the current context.  The session objects that are
 * cached by this service are obtained through a listener, which must
 * be configured via your web application's <code>web.xml</code>
 * deployement descriptor as follows:
 *
 * <blockquote><code><pre>
 * &lt;listener&gt;
 *   &lt;listener-class&gt;
 *     org.apache.turbine.session.SessionListener
 *   &lt;/listener-class&gt;
 * &lt;/listener&gt;
 * </pre></code></blockquote>
 *
 * @author <a href="mailto:quintonm@bellsouth.net">Quinton McCombs</a>
 * @author <a href="mailto:dlr@collab.net">Daniel Rall</a>
 * @since 2.3
 * @version $Id$
 * @see org.apache.turbine.services.session.TurbineSession
 * @see org.apache.turbine.services.session.SessionListener
 */
public class TurbineSessionService
        extends TurbineBaseService
        implements SessionService
{
    /** Map of active sessions */
    private Map activeSessions;

    /**
     * Gets a list of the active sessions.
     *
     * @return A copy of the list of <code>HttpSession</code> objects.
     */
    public Collection getActiveSessions()
    {
        // Sync externally to allow ArrayList's ctor to iterate
        // activeSessions' values in a thread-safe fashion.
        synchronized (activeSessions)
        {
            return new ArrayList(activeSessions.values());
        }
    }

    /**
     * Adds a session to the current list.  This method should only be
     * called by the listener.
     *
     * @param session Session to add
     */
    public void addSession(HttpSession session)
    {
        activeSessions.put(session.getId(), session);
    }

    /**
     * Removes a session from the current list.  This method should only be
     * called by the listener.
     *
     * @param session Session to remove
     */
    public void removeSession(HttpSession session)
    {
        activeSessions.remove(session.getId());
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
        return getActiveUsers().contains(user);
    }

    /**
     * Gets a collection of all user objects representing the users currently
     * logged in.  This will exclude any instances of anonymous user that
     * Turbine will use before the user actually logs on.
     *
     * @return A set of {@link org.apache.turbine.om.security.User} objects.
     */
    public Collection getActiveUsers()
    {
        Collection users;
        synchronized (activeSessions)
        {
            // Pre-allocate a list which won't need expansion more
            // than once.
            users = new ArrayList((int) (activeSessions.size() * 0.7));
            for (Iterator i = activeSessions.values().iterator(); i.hasNext();)
            {
                User u = getUserFromSession((HttpSession) i.next());
                if (u != null && u.hasLoggedIn())
                {
                    users.add(u);
                }
            }
        }

        return users;
    }

    /**
     * Gets the User object of the the specified HttpSession.
     *
     * @param session The session from which to extract a user.
     * @return The Turbine User object.
     */
    public User getUserFromSession(HttpSession session)
    {
        return (User) session.getAttribute(User.SESSION_KEY);
    }

    /**
     * Gets the HttpSession by the session identifier
     *
     * @param sessionId The unique session identifier.
     * @return The session keyed by the specified identifier.
     */
    public HttpSession getSession(String sessionId)
    {
        return (HttpSession) this.activeSessions.get(sessionId);
    }

    /**
     * Get a collection of all session on which the given user
     * is logged in.
     *
     * @param user the user
     * @return Collection of HtttSession objects
     */
    public Collection getSessionsForUser(User user)
    {
        Collection sessions = new ArrayList();
        synchronized (activeSessions)
        {
            for (Iterator i = activeSessions.values().iterator(); i.hasNext();)
            {
                HttpSession session = (HttpSession) i.next();
                User u = this.getUserFromSession(session);
                if (user.equals(u))
                {
                    sessions.add(session);
                }
            }
        }

        return sessions;
    }


    // ---- Service initilization ------------------------------------------

    /**
     * Initializes the service
     */
    public void init()
    {
        this.activeSessions = new Hashtable();

        setInit(true);
    }

    /**
     * Returns to uninitialized state.
     */
    public void shutdown()
    {
        this.activeSessions = null;

        setInit(false);
    }

}
