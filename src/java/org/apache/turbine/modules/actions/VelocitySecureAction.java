package org.apache.turbine.modules.actions;

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

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;

/**
 * VelocitySecure action.
 *
 * Always performs a Security Check that you've defined before
 * executing the doBuildtemplate().  You should extend this class and
 * add the specific security check needed.  If you have a number of
 * screens that need to perform the same check, you could make a base
 * screen by extending this class and implementing the isAuthorized().
 * Then each action that needs to perform the same check could extend
 * your base action.
 *
 * @author <a href="mailto:mbryson@mont.mindspring.com">Dave Bryson</a>
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @version $Id$
 */
public abstract class VelocitySecureAction extends VelocityAction
{
    /**
     * Implement this to add information to the context.
     *
     * @param data Turbine information.
     * @param context Context for web pages.
     * @throws Exception a generic exception.
     */
    public abstract void doPerform(RunData data, Context context)
            throws Exception;

    /**
     * This method overrides the method in WebMacroSiteAction to
     * perform a security check first.
     *
     * @param data Turbine information.
     * @throws Exception a generic exception.
     */
    protected void perform(RunData data) throws Exception
    {
        if (isAuthorized(data))
        {
            super.perform(data);
        }
    }

    /**
     * Implement this method to perform the security check needed.
     * You should set the template in this method that you want the
     * user to be sent to if they're unauthorized.
     *
     * @param data Turbine information.
     * @return True if the user is authorized to access the screen.
     * @throws Exception a generic exception.
     */
    protected abstract boolean isAuthorized(RunData data)
            throws Exception;
}
