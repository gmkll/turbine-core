package org.apache.turbine.pipeline;

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

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.TurbineException;

/**
 * Implements the Redirect Requested portion of the "Turbine classic"
 * processing pipeline (from the Turbine 2.x series).
 *
 * @author <a href="mailto:epugh@opensourceConnections.com">Eric Pugh</a>
 * @version $Id$
 */
public class DetermineRedirectRequestedValve
    extends AbstractValve
{
    Log log = LogFactory.getLog(DetermineRedirectRequestedValve.class);
    /**
     * Creates a new instance.
     */
    public DetermineRedirectRequestedValve()
    {
    }

    /**
     * @see org.apache.turbine.Valve#invoke(RunData, ValveContext)
     */
    public void invoke(RunData data, ValveContext context)
        throws IOException, TurbineException
    {
        try
        {
            redirectRequested(data);
        }
        catch (Exception e)
        {
            throw new TurbineException(e);
        }

        // Pass control to the next Valve in the Pipeline
        context.invokeNext(data);
    }

    /**
     * Perform clean up after processing the request.
     *
     * @param data The run-time data.
     */
    protected void redirectRequested(RunData data)
        throws Exception
    {      

// handle a redirect request
        boolean requestRedirected = ((data.getRedirectURI() != null)
        && (data.getRedirectURI().length() > 0));
        if (requestRedirected)
        {
            if (data.getResponse().isCommitted())
            {
                requestRedirected = false;
                log.warn("redirect requested, response already committed: " +
                        data.getRedirectURI());
            }
            else
            {
                data.getResponse().sendRedirect(data.getRedirectURI());
            }
        }

        if (!requestRedirected)
        {
            try
            {
                if (data.isPageSet() == false && data.isOutSet() == false)
                {
                    throw new Exception("Nothing to output");
                }

                // We are all done! if isPageSet() output that way
                // otherwise, data.getOut() has already been written
                // to the data.getOut().close() happens below in the
                // finally.
                if (data.isPageSet() && data.isOutSet() == false)
                {
                    // Modules can override these.
                    data.getResponse().setLocale(data.getLocale());
                    data.getResponse().setContentType(
                            data.getContentType());

                    // Set the status code.
                    data.getResponse().setStatus(data.getStatusCode());
                    // Output the Page.
                    data.getPage().output(data.getOut());
                }
            }
            catch (Exception e)
            {
                // The output stream was probably closed by the client
                // end of things ie: the client clicked the Stop
                // button on the browser, so ignore any errors that
                // result.
                log.debug("Output stream closed? ", e);
            }
        }
    }
}