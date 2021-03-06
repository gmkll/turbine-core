package org.apache.turbine.services.crypto;

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

import org.apache.fulcrum.crypto.CryptoAlgorithm;
import org.apache.fulcrum.crypto.CryptoService;
import org.apache.turbine.services.ServiceManager;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.test.BaseTestCase;
import org.apache.turbine.util.TurbineConfig;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Verifies the Fulcrum Crypto Service works properly in Turbine.
 *
 * @author <a href="mailto:epugh@upstate.com">Eric Pugh</a>
 * @author <a href="mailto:Rafal.Krzewski@e-point.pl">Rafal Krzewski</a>
 * @author <a href="mailto:sgoeschl@apache.org">Siegfried Goeschl</a>
 * @version $Id: CryptoRunningInECMTest.java 222043 2004-12-06 17:47:33Z painter $
 */
public class FulcrumCryptoServiceTest extends BaseTestCase
{
    private static final String preDefinedInput = "Oeltanks";
    private static TurbineConfig tc = null;
    private CryptoService cryptoService;



    @Test public void testMd5()
    {
        String preDefinedResult = "XSop0mncK19Ii2r2CUe29w==";

        try
        {
            CryptoAlgorithm ca =cryptoService.getCryptoAlgorithm("default");
            ca.setCipher("MD5");
            String output = ca.encrypt(preDefinedInput);
            assertEquals("MD5 Encryption failed ", preDefinedResult, output);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
    }

    @Test public void testSha1()
    {
        String preDefinedResult = "uVDiJHaavRYX8oWt5ctkaa7j1cw=";

        try
        {
            CryptoAlgorithm ca = cryptoService.getCryptoAlgorithm("default");
            ca.setCipher("SHA1");
            String output = ca.encrypt(preDefinedInput);
            assertEquals("SHA1 Encryption failed ", preDefinedResult, output);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
    }

    @BeforeClass
    public static void init() throws Exception
    {
        tc = new TurbineConfig(".", "/conf/test/TestFulcrumComponents.properties");
        tc.initialize();

    }

    @Before
    public void setUpBefore() throws Exception
    {
        ServiceManager serviceManager = TurbineServices.getInstance();
        cryptoService = (CryptoService) serviceManager.getService(CryptoService.ROLE);
    }

    @AfterClass
    public static void tearDown() throws Exception
    {
        if (tc != null)
        {
            tc.dispose();
        }
    }
}
