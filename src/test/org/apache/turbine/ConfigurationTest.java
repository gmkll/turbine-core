package org.apache.turbine;

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

import junit.framework.Test;
import junit.framework.TestSuite;
import java.util.*;

import org.apache.commons.configuration.Configuration;

import org.apache.turbine.test.BaseTestCase;
import org.apache.turbine.util.TurbineConfig;
import org.apache.turbine.util.TurbineXmlConfig;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.configuration.BaseConfiguration;

/**
 * Tests that the ConfigurationFactory and regular old properties methods both work.
 * Verify the overriding of properties.
 * 
 * @author <a href="mailto:epugh@upstate.com">Eric Pugh</a>
 * @version $Id$
 */
public class ConfigurationTest extends BaseTestCase
{
    public static final String SERVICE_PREFIX = "services.";

    /**
     * A <code>Service</code> property determining its implementing
     * class name .
     */
    public static final String CLASSNAME_SUFFIX = ".classname";

    private static TurbineConfig tc = null;
    private static TurbineXmlConfig txc = null;

    public ConfigurationTest(String name) throws Exception
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(ConfigurationTest.class);
    }

    public void testCreateTurbineWithConfigurationXML() throws Exception
    {
        txc = new TurbineXmlConfig(".", "/conf/test/TurbineConfiguration.xml");

        try
        {
            txc.initialize();

            Configuration configuration = Turbine.getConfiguration();
            assertNotNull("No Configuration Object found!", configuration);
            assertFalse("Make sure we have values", configuration.isEmpty());

            // overridden value
            String key = "module.cache";
            assertEquals("Read a config value " + key + ", received:" + configuration.getString(key), "true", configuration.getString(key));

            // non overridden value
            key = "scheduledjob.cache.size";
            assertEquals("Read a config value " + key + ", received:" + configuration.getString(key), "10", configuration.getString(key));
        }
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            txc.dispose();
        }
    }

    public void testCreateTurbineWithConfiguration() throws Exception
    {
        tc = new TurbineConfig(".", "/conf/test/TemplateService.properties");

        try
        {
            tc.initialize();

            Configuration configuration = Turbine.getConfiguration();
            assertNotNull("No Configuration Object found!", configuration);
            assertFalse("Make sure we have values", configuration.isEmpty());

            String key = "scheduledjob.cache.size";
            assertEquals("Read a config value " + key + ", received:" + configuration.getString(key), "10", configuration.getString(key));
        }
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            tc.dispose();
        }
    }

    public void testConfigurationValuesInSameOrder() throws Exception
    {
        Configuration simpleConfiguration = null;
        Configuration compositeConfiguration = null;

        tc = new TurbineConfig(".", "/conf/test/TemplateService.properties");

        try
        {
            tc.initialize();
            simpleConfiguration = Turbine.getConfiguration();
        }
        finally
        {
            tc.dispose();
        }

        txc = new TurbineXmlConfig(".", "/conf/test/TurbineConfiguration.xml");
        try
        {
            txc.initialize();
            compositeConfiguration = Turbine.getConfiguration();
        }
        finally
        {
            txc.dispose();
        }
        Configuration a = simpleConfiguration.subset("services");
        Configuration b = compositeConfiguration.subset("services");

        List keysSimpleConfiguration = IteratorUtils.toList(a.getKeys());
        List keysCompositeConfiguration = IteratorUtils.toList(b.getKeys());

        assertTrue("Size:" + keysSimpleConfiguration.size(), keysSimpleConfiguration.size() > 0);
        assertEquals(keysSimpleConfiguration.size(), keysCompositeConfiguration.size());

        for (int i = 0; i < keysSimpleConfiguration.size(); i++)
        {
            assertEquals(keysSimpleConfiguration.get(i), keysCompositeConfiguration.get(i));
        }

    }

    public void testMappingInSameOrder() throws Exception
    {
        Configuration simpleConfiguration = null;
        Configuration compositeConfiguration = null;
        Configuration mapping = new BaseConfiguration();
        Configuration mapping2 = new BaseConfiguration();

        tc = new TurbineConfig(".", "/conf/test/TemplateService.properties");

        try
        {
            tc.initialize();
            simpleConfiguration = Turbine.getConfiguration();
        }
        finally
        {
            tc.dispose();
        }

        txc = new TurbineXmlConfig(".", "/conf/test/TurbineConfiguration.xml");
        try
        {
            txc.initialize();
            compositeConfiguration = Turbine.getConfiguration();
        }
        finally
        {
            txc.dispose();
        }

        for (Iterator keys = simpleConfiguration.getKeys(); keys.hasNext();)
        {
            String key = (String) keys.next();
            String[] keyParts = StringUtils.split(key, ".");

            if ((keyParts.length == 3) && (keyParts[0] + ".").equals(SERVICE_PREFIX) && ("." + keyParts[2]).equals(CLASSNAME_SUFFIX))
            {
                String serviceKey = keyParts[1];

                if (!mapping.containsKey(serviceKey))
                {
                    mapping.setProperty(serviceKey, simpleConfiguration.getString(key));
                }
            }
        }

        for (Iterator keys = compositeConfiguration.getKeys(); keys.hasNext();)
        {
            String key = (String) keys.next();
            String[] keyParts = StringUtils.split(key, ".");

            if ((keyParts.length == 3) && (keyParts[0] + ".").equals(SERVICE_PREFIX) && ("." + keyParts[2]).equals(CLASSNAME_SUFFIX))
            {
                String serviceKey = keyParts[1];

                if (!mapping2.containsKey(serviceKey))
                {
                    mapping2.setProperty(serviceKey, compositeConfiguration.getString(key));
                }
            }
        }
                
    }

}