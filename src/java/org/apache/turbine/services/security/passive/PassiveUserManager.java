package org.apache.turbine.services.security.passive;


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


import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.fulcrum.security.acl.AccessControlList;
import org.apache.fulcrum.security.util.DataBackendException;
import org.apache.fulcrum.security.util.EntityExistsException;
import org.apache.fulcrum.security.util.PasswordMismatchException;
import org.apache.fulcrum.security.util.UnknownEntityException;
import org.apache.turbine.om.security.User;
import org.apache.turbine.services.security.UserManager;

/**
 * Void user manager can be used where no data storage is needed
 * by the application.
 * It's methods don't provide any useful functionality  except throwing
 * DataBackendExceptions. Security service will be still able to create
 * anonymous User objects when this UserManager is used.
 *
 * @author <a href="mailto:Rafal.Krzewski@e-point.pl">Rafal Krzewski</a>
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @version $Id$
 */
public class PassiveUserManager implements UserManager
{
    /**
     * Initializes the UserManager
     *
     * @param conf A Configuration object to init this Manager
     */
    @Override
    public void init(Configuration conf)
    {
        // GNDN
    }

    /**
     * Check whether a specified user's account exists.
     *
     * The login name is used for looking up the account.
     *
     * @param user The user to be checked.
     * @return true if the specified account exists
     * @throws DataBackendException if there was an error accessing the data backend.
     */
    @Override
    public boolean accountExists(User user)
            throws DataBackendException
    {
        throw new DataBackendException("PassiveUserManager knows no users");
    }

    /**
     * Check whether a specified user's account exists.
     *
     * The login name is used for looking up the account.
     *
     * @param userName The name of the user to be checked.
     * @return true if the specified account exists
     * @throws DataBackendException if there was an error accessing the data backend.
     */
    @Override
    public boolean accountExists(String userName)
            throws DataBackendException
    {
        throw new DataBackendException("PassiveUserManager knows no users");
    }

    /**
     * Retrieve a user from persistent storage using username as the
     * key.
     *
     * @param username the name of the user.
     * @return an User object.
     * @throws UnknownEntityException if the user's record does not
     *            exist in the database.
     * @throws DataBackendException if there is a problem accessing the
     *            storage.
     */
    @Override
    public <U extends User> U retrieve(String username)
            throws UnknownEntityException, DataBackendException
    {
        throw new DataBackendException("PassiveUserManager knows no users");
    }

    /**
     * Retrieve a set of users that meet the specified criteria.
     *
     * As the keys for the criteria, you should use the constants that
     * are defined in {@link User} interface, plus the names
     * of the custom attributes you added to your user representation
     * in the data storage. Use verbatim names of the attributes -
     * without table name prefix in case of DB implementation.
     *
     * @param criteria The criteria of selection.
     * @return a List of users meeting the criteria.
     * @throws DataBackendException if there is a problem accessing the
     *         storage.
     */
    @Override
    public List<? extends User> retrieveList(Object criteria)
            throws DataBackendException
    {
        throw new DataBackendException("PassiveUserManager knows no users");
    }

    /**
     * Retrieve a user from persistent storage using username as the
     * key, and authenticate the user. The implementation may chose
     * to authenticate to the server as the user whose data is being
     * retrieved.
     *
     * @param username the name of the user.
     * @param password the user supplied password.
     * @return an User object.
     * @throws PasswordMismatchException if the supplied password was
     *            incorrect.
     * @throws UnknownEntityException if the user's record does not
     *            exist in the database.
     * @throws DataBackendException if there is a problem accessing the
     *            storage.
     */
    @Override
    public <U extends User> U retrieve(String username, String password)
            throws PasswordMismatchException, UnknownEntityException,
            DataBackendException
    {
        throw new DataBackendException("PassiveUserManager knows no users");
    }

    /**
     * Save an User object to persistent storage. User's record is
     * required to exist in the storage.
     *
     * @param user an User object to store.
     * @throws UnknownEntityException if the user's record does not
     *            exist in the database.
     * @throws DataBackendException if there is a problem accessing the
     *            storage.
     */
    @Override
    public void store(User user)
            throws UnknownEntityException, DataBackendException
    {
        throw new DataBackendException("PassiveUserManager does not support saving user data");
    }

    /**
     * Saves User data when the session is unbound. The user account is required
     * to exist in the storage.
     *
     * LastLogin, AccessCounter, persistent pull tools, and any data stored
     * in the permData hashmap that is not mapped to a column will be saved.
     *
     * @throws UnknownEntityException if the user's account does not
     *            exist in the database.
     * @throws DataBackendException if there is a problem accessing the
     *            storage.
     */
    @Override
    public void saveOnSessionUnbind(User user)
            throws UnknownEntityException, DataBackendException
    {
        throw new DataBackendException("PassiveUserManager does not support saving user data");
    }

    /**
     * Authenticate an User with the specified password. If authentication
     * is successful the method returns nothing. If there are any problems,
     * exception was thrown.
     *
     * @param user an User object to authenticate.
     * @param password the user supplied password.
     * @throws PasswordMismatchException if the supplied password was
     *            incorrect.
     * @throws UnknownEntityException if the user's record does not
     *            exist in the database.
     * @throws DataBackendException if there is a problem accessing the
     *            storage.
     */
    @Override
    public void authenticate(User user, String password)
            throws PasswordMismatchException, UnknownEntityException,
            DataBackendException
    {
        throw new DataBackendException("PassiveUserManager knows no users");
    }

    /**
     * Creates new user account with specified attributes.
     *
     * @param user the object describing account to be created.
     * @param initialPassword The password to use for the object creation
     *
     * @throws DataBackendException if there was an error accessing the data backend.
     * @throws EntityExistsException if the user account already exists.
     */
    @Override
    public void createAccount(User user, String initialPassword)
            throws EntityExistsException, DataBackendException
    {
        throw new DataBackendException("PassiveUserManager does not support"
                + " creating accounts");
    }

    /**
     * Removes an user account from the system.
     *
     * @param user the object describing the account to be removed.
     * @throws DataBackendException if there was an error accessing the data backend.
     * @throws UnknownEntityException if the user account is not present.
     */
    @Override
    public void removeAccount(User user)
            throws UnknownEntityException, DataBackendException
    {
        throw new DataBackendException("PassiveUserManager does not support removing accounts");
    }

    /**
     * Change the password for an User.
     *
     * @param user an User to change password for.
     * @param oldPassword the current password supplied by the user.
     * @param newPassword the current password requested by the user.
     * @throws PasswordMismatchException if the supplied password was
     *            incorrect.
     * @throws UnknownEntityException if the user's record does not
     *            exist in the database.
     * @throws DataBackendException if there is a problem accessing the
     *            storage.
     */
    @Override
    public void changePassword(User user, String oldPassword,
                               String newPassword)
            throws PasswordMismatchException, UnknownEntityException,
            DataBackendException
    {
        throw new DataBackendException("PassiveUserManager does not support setting passwords");
    }

    /**
     * Forcibly sets new password for an User.
     *
     * This is supposed by the administrator to change the forgotten or
     * compromised passwords. Certain implementations of this feature
     * would require administrative level access to the authenticating
     * server / program.
     *
     * @param user an User to change password for.
     * @param password the new password.
     * @throws UnknownEntityException if the user's record does not
     *            exist in the database.
     * @throws DataBackendException if there is a problem accessing the
     *            storage.
     */
    @Override
    public void forcePassword(User user, String password)
            throws UnknownEntityException, DataBackendException
    {
        throw new DataBackendException("PassiveUserManager does not support setting passwords");
    }

    /**
     * Constructs an User object to represent an anonymous user of the
     * application.
     *
     * @return An anonymous Turbine User.
     * @throws UnknownEntityException
     *             if the anonymous User object couldn't be constructed.
     */
    @Override
    public <T extends User> T getAnonymousUser() throws UnknownEntityException
    {
        throw new UnknownEntityException("PassiveUserManager knows no users");
    }

    /**
     * Checks whether a passed user object matches the anonymous user pattern
     * according to the configured user manager
     *
     * @param u a user object
     *
     * @return true if this is an anonymous user
     *
     */
    @Override
    public boolean isAnonymousUser(User u)
    {
        return true;
    }

    /**
     * Construct a blank User object.
     *
     * This method calls getUserClass, and then creates a new object using the
     * default constructor.
     *
     * @return an object implementing User interface.
     * @throws DataBackendException
     *             if the object could not be instantiated.
     */
    @Override
    public <T extends User> T getUserInstance() throws DataBackendException
    {
        throw new DataBackendException("PassiveUserManager knows no users");
    }

    /**
     * Construct a blank User object.
     *
     * This method calls getUserClass, and then creates a new object using the
     * default constructor.
     *
     * @param userName
     *            The name of the user.
     *
     * @return an object implementing User interface.
     * @throws DataBackendException
     *             if the object could not be instantiated.
     */
    @Override
    public <T extends User> T getUserInstance(String userName) throws DataBackendException
    {
        throw new DataBackendException("PassiveUserManager knows no users");
    }

    /**
     * Return a Class object representing the system's chosen implementation of
     * of ACL interface.
     *
     * @return systems's chosen implementation of ACL interface.
     * @throws UnknownEntityException
     *             if the implementation of ACL interface could not be
     *             determined, or does not exist.
     */
    @Override
    public <T extends AccessControlList> T getACL(User user) throws UnknownEntityException
    {
        throw new UnknownEntityException("PassiveUserManager knows no users");
    }
}
