package org.apache.turbine.modules.screens;

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

import org.apache.ecs.ConcreteElement;
import org.apache.turbine.services.jsp.JspService;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.services.template.TurbineTemplate;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.apache.turbine.services.jsp.TurbineJsp;

/**
 * Jsp Screen with support for conext. The buildTemplate() assumes the
 * template parameter has been set in the RunData object.  This provides
 * the ability to execute several templates from one Screen.
 *
 * <p>
 *
 * If you need more specific behavior in your application, extend this
 * class and override the doBuildTemplate() method.
 *
 * @author <a href="mailto:mbryson@mont.mindspring.com">Dave Bryson</a>
 * @author <a href="mailto:gabrielm@itcsoluciones.com">Gabriel A. Moreno</a>
 * @version $Id$
 */
public class JspScreen extends BaseJspScreen {
    
    /**
     * Jsp Screens extending this class should overide this
     * method to perform any particular business logic and add
     * information to the context.
     *
     * @param data Turbine information.
     * @param context Context for web pages.
     * @exception Exception, a generic exception.
     */
    protected void doBuildTemplate( RunData data,
                                    Context context )
        throws Exception
    {
    }

    /**
     * Needs to be implemented to make TemplateScreen like us.  The
     * actual method that you should override is the one with the
     * context in the parameter list.
     *
     * @param data Turbine information.
     * @exception Exception, a generic exception.
     */
    protected void doBuildTemplate( RunData data )
        throws Exception
    {
        doBuildTemplate(data, TurbineJsp.getContext(data));
    }

    /**
     * Return the Context needed which can then be used in the Jsp template
     *
     * @param data Turbine information.
     * @return A Context.
     */
    public static Context getContext(RunData data)
    {
        return TurbineJsp.getContext(data);
    }
   
}