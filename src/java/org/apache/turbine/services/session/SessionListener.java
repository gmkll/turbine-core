package org.apache.turbine.services.session;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import java.io.Serializable;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * This class is a listener for both session creation and destruction,
 * and for session activation and passivation.  It must be configured
 * via your web application's <code>web.xml</code> deployment
 * descriptor as follows for the container to call it:
 *
 * <blockquote><code><pre>
 * &lt;listener&gt;
 *   &lt;listener-class&gt;
 *     org.apache.turbine.session.SessionListener
 *   &lt;/listener-class&gt;
 * &lt;/listener&gt;
 * </pre></code></blockquote>
 *
 * <code>&lt;listener&gt;</code> elemements can occur between
 * <code>&lt;context-param&gt;</code> and <code>&lt;servlet&gt;</code>
 * elements in your deployment descriptor.
 *
 * The {@link #sessionCreated(HttpSessionEvent)} callback will
 * automatically add an instance of this listener to any newly created
 * <code>HttpSession</code> for detection of session passivation and
 * re-activation.
 *
 * @since 2.3
 * @author <a href="mailto:quintonm@bellsouth.net">Quinton McCombs</a>
 * @author <a href="mailto:dlr@apache.org">Daniel Rall</a>
 * @version $Id$
 * @see javax.servlet.http.HttpSessionListener
 */
public class SessionListener
        implements HttpSessionListener, HttpSessionActivationListener, Serializable
{
    /** Serial Version UID */
    private static final long serialVersionUID = -8083730704842809870L;

    // ---- HttpSessionListener implementation -----------------------------

    /**
     * Called by the servlet container when a new session is created
     *
     * @param event Session creation event.
     */
    public void sessionCreated(HttpSessionEvent event)
    {
        TurbineSession.addSession(event.getSession());
        event.getSession().setAttribute(getClass().getName(), this);
    }

    /**
     * Called by the servlet container when a session is destroyed
     *
     * @param event Session destruction event.
     */
    public void sessionDestroyed(HttpSessionEvent event)
    {
        TurbineSession.removeSession(event.getSession());
    }


    // ---- HttpSessionActivationListener implementation -------------------

    /**
     * Called by the servlet container when an existing session is
     * (re-)activated.
     *
     * @param event Session activation event.
     */
    public void sessionDidActivate(HttpSessionEvent event)
    {
        TurbineSession.addSession(event.getSession());
    }

    /**
     * Called by the servlet container when a an existing session is
     * passivated.
     *
     * @param event Session passivation event.
     */
    public void sessionWillPassivate(HttpSessionEvent event)
    {
        TurbineSession.removeSession(event.getSession());
    }
}
