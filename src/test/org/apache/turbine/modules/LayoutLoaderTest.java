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
package org.apache.turbine.modules;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.apache.turbine.modules.layouts.TestVelocityOnlyLayout;
import org.apache.turbine.om.security.User;
import org.apache.turbine.pipeline.DefaultPipelineData;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.services.template.TemplateService;
import org.apache.turbine.test.BaseTestCase;
import org.apache.turbine.test.EnhancedMockHttpServletRequest;
import org.apache.turbine.test.EnhancedMockHttpSession;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.TurbineConfig;

import com.mockobjects.servlet.MockHttpServletResponse;
import com.mockobjects.servlet.MockServletConfig;


/**
 * @author <a href="mailto:peter@courcoux.biz">Peter Courcoux</a>
 */
public class LayoutLoaderTest extends BaseTestCase {
	private static TurbineConfig tc = null;
	private static TemplateService ts = null;
	private MockServletConfig config = null;
	private EnhancedMockHttpServletRequest request = null;
	private EnhancedMockHttpSession session = null;
	private HttpServletResponse response = null;
	private static ServletConfig sc = null;
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		config = new MockServletConfig();
		config.setupNoParameters();
		request = new EnhancedMockHttpServletRequest();
		request.setupServerName("bob");
		request.setupGetProtocol("http");
		request.setupScheme("scheme");
		request.setupPathInfo("damn");
		request.setupGetServletPath("damn2");
		request.setupGetContextPath("wow");
		request.setupGetContentType("html/text");
		request.setupAddHeader("Content-type", "html/text");
		request.setupAddHeader("Accept-Language", "en-US");
		Vector v = new Vector();
		request.setupGetParameterNames(v.elements());
		session = new EnhancedMockHttpSession();
		response = new MockHttpServletResponse();
		session.setupGetAttribute(User.SESSION_KEY, null);
		request.setSession(session);
		sc = config;
		tc =
			new TurbineConfig(
				".",
				"/conf/test/CompleteTurbineResources.properties");
		tc.initialize();
	}
	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		if (tc != null) {
			tc.dispose();
		}
	}
	/**
	 * Constructor for LayoutLoaderTest.
	 * @param arg0
	 */
	public LayoutLoaderTest(String arg0) throws Exception {
		super(arg0);
	}

	public void testPipelineDataContainsRunData()
	{
	    try
	    {
		    RunData data = getRunData(request,response,config);
			PipelineData pipelineData = new DefaultPipelineData();
			Map runDataMap = new HashMap();
			runDataMap.put(RunData.class, data);
			pipelineData.put(RunData.class, runDataMap);
			data.setLayout("TestVelocityOnlyLayout");
			int numberOfCalls = TestVelocityOnlyLayout.numberOfCalls;
			try {
				LayoutLoader.getInstance().exec(pipelineData, data.getLayout());
			} catch (Exception e) {
			    e.printStackTrace();
			    Assert.fail("Should not have thrown an exception.");
			}
			assertEquals(numberOfCalls+1,TestVelocityOnlyLayout.numberOfCalls);
	    }
	    catch (Exception e)
	    {
	        e.printStackTrace();
	        Assert.fail("Should not have thrown an exception.");
	    }
	}

	public void testDoBuildWithRunData()
	{
	    try
	    {
		    RunData data = getRunData(request,response,config);
			data.setLayout("TestVelocityOnlyLayout");
			int numberOfCalls = TestVelocityOnlyLayout.numberOfCalls;
			try {
				LayoutLoader.getInstance().exec(data, data.getLayout());
			} catch (Exception e) {
			    e.printStackTrace();
			    Assert.fail("Should not have thrown an exception.");
			}
			assertEquals(numberOfCalls+1,TestVelocityOnlyLayout.numberOfCalls);
	    }
	    catch (Exception e)
	    {
	        e.printStackTrace();
	        Assert.fail("Should not have thrown an exception.");
	    }
	}

	
}