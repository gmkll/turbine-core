package org.apache.turbine.services.assemblerbroker.util;


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


import org.apache.turbine.modules.Assembler;
import org.apache.turbine.modules.Loader;

/**
 * Interface for AssemblerFactory's
 *
 * @author <a href="mailto:leon@opticode.co.za">Leon Messerschmidt</a>
 * @param <T> the specialized assembler type
 */
public interface AssemblerFactory<T extends Assembler> extends Loader<T>
{
    /**
     * Get the loader for this type of assembler
     *
     * @return a Loader
     */
    Loader<T> getLoader();

    /**
     * Get the class of this assembler
     *
     * @return a class
     */
    Class<T> getManagedClass();
}
