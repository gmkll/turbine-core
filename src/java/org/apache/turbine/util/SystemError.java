package org.apache.turbine.util;


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



/**
 * Used for wrapping system errors (exceptions) that may occur in the
 * application.
 *
 * @author <a href="mailto:neeme@one.lv">Neeme Praks</a>
 * @author <a href="mailto:quintonm@bellsouth.net">Quinton McCombs</a>
 * @version $Id$
 */
public class SystemError extends Error
{
    /** Serial version */
	private static final long serialVersionUID = -8965344546132616128L;

	/**
     * Constructor.
     *
     * @param cause A Throwable object
     */
    public SystemError(Throwable cause)
    {
        super(cause);
    }

    /**
     * Constructor.
     *
     * @param message Error message
     */
    public SystemError(String message)
    {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param cause A Throwable object
     * @param message A String.
     */
    public SystemError(String message, Throwable cause)
    {
        super(message, cause);
    }
}
