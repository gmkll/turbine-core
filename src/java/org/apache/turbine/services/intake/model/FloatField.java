package org.apache.turbine.services.intake.model;

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

import org.apache.turbine.services.intake.xmlmodel.Rule;
import org.apache.turbine.services.intake.xmlmodel.XmlField;
import org.apache.turbine.util.ParameterParser;
import org.apache.turbine.util.Log;

/**
 * Creates Field objects.
 *
 * @author <a href="mailto:r.wekker@rubicon-bv.com>Ronald Wekker</a>
 * @version $Id$
 */
 
public class FloatField extends Field
{

    public FloatField(XmlField field, Group group)
        throws Exception
    {
        super(field, group);
    }

    /**
     * Sets the default value for an Float
     */
    
    protected void setDefaultValue(String prop)
    {
        defaultValue = null;

        if(prop == null)
        {
            return;
        }

        try
        {
            defaultValue = new Float(prop);
        } 
        catch(Exception e) 
        {
            Log.error("Could not convert "+prop+" into an Float. ("+name+")");
        }
    }

    /**
     * A suitable validator.
     *
     * @return "FloatValidator"
     */
    protected String getDefaultValidator()
    {
        return "org.apache.turbine.services.intake.validator.NumberValidator";
    }

    /**
     * converts the parameter to the correct Object.
     */
    protected void doSetValue(ParameterParser pp)
    {
        if ( isMultiValued  )
        {
            String[] ss = pp.getStrings(getKey());
            float[] ival = new float[ss.length];
            for (int i=0; i<ss.length; i++)
            {
                ival[i] = Float.parseFloat(ss[i]);
            }
            setTestValue(ival);
        }
        else
        {
            setTestValue(new Float(pp.getString(getKey())));
        }
    }
}
