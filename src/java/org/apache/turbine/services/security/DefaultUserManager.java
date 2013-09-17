package org.apache.turbine.services.security;


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


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.fulcrum.security.acl.AccessControlList;
import org.apache.fulcrum.security.model.turbine.TurbineUserManager;
import org.apache.fulcrum.security.model.turbine.entity.TurbineUser;
import org.apache.fulcrum.security.util.DataBackendException;
import org.apache.fulcrum.security.util.EntityExistsException;
import org.apache.fulcrum.security.util.PasswordMismatchException;
import org.apache.fulcrum.security.util.UnknownEntityException;
import org.apache.fulcrum.security.util.UserSet;
import org.apache.turbine.om.security.DefaultUserImpl;
import org.apache.turbine.om.security.User;
import org.apache.turbine.services.ServiceManager;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.util.ObjectUtils;

/**
 * Default user manager.
 *
 * The user manager wraps Fulcrum security user objects into
 * Turbine-specific ones.
 *
 * @author <a href="mailto:tv@apache.org">Thomas Vandahl</a>
 * @version $Id: PassiveUserManager.java 1096130 2011-04-23 10:37:19Z ludwig $
 */
public class DefaultUserManager implements UserManager
{
    /** Fulcrum user manager instance to delegate to */
    private TurbineUserManager umDelegate = null;

    /**
     * Wrap a Fulcrum user object into a Turbine user object
     *
     * @param user the user object to delegate to
     *
     * @return the wrapped object
     */
    protected <U extends User> U wrap(TurbineUser user)
    {
        @SuppressWarnings("unchecked")
        U u = (U)new DefaultUserImpl(user);
        return u;
    }

    /**
     * Initializes the UserManager
     *
     * @param conf A Configuration object to init this Manager
     */
    public void init(Configuration conf)
    {
        ServiceManager manager = TurbineServices.getInstance();
        this.umDelegate = (TurbineUserManager)manager.getService(TurbineUserManager.ROLE);
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
    public boolean accountExists(User user)
            throws DataBackendException
    {
        return umDelegate.checkExists(user);
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
    public boolean accountExists(String userName)
            throws DataBackendException
    {
        return umDelegate.checkExists(userName);
    }

    /**
     * Retrieve a user from persistent storage using username as the
     * key.
     *
     * @param username the name of the user.
     * @return an User object.
     * @exception UnknownEntityException if the user's record does not
     *            exist in the database.
     * @exception DataBackendException if there is a problem accessing the
     *            storage.
     */
    public <U extends User> U retrieve(String username)
            throws UnknownEntityException, DataBackendException
    {
        TurbineUser u = umDelegate.getUser(username);
        return wrap(u);
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
    public List<? extends User> retrieveList(Object criteria)
            throws DataBackendException
    {
        UserSet uset = umDelegate.getAllUsers();
        List<User> userList = new ArrayList<User>();

        for (org.apache.fulcrum.security.entity.User u : uset)
        {
            TurbineUser tu = (TurbineUser)u;
            userList.add(wrap(tu));
        }

        return userList;
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
     * @exception PasswordMismatchException if the supplied password was
     *            incorrect.
     * @exception UnknownEntityException if the user's record does not
     *            exist in the database.
     * @exception DataBackendException if there is a problem accessing the
     *            storage.
     */
    public <U extends User> U retrieve(String username, String password)
            throws PasswordMismatchException, UnknownEntityException,
            DataBackendException
    {
        TurbineUser u = umDelegate.getUser(username, password);
        return wrap(u);
    }

    /**
     * Save an User object to persistent storage. User's record is
     * required to exist in the storage.
     *
     * @param user an User object to store.
     * @exception UnknownEntityException if the user's record does not
     *            exist in the database.
     * @exception DataBackendException if there is a problem accessing the
     *            storage.
     */
    public void store(User user)
            throws UnknownEntityException, DataBackendException
    {
        try
        {
            user.setObjectdata(ObjectUtils.serializeMap(user.getPermStorage()));
        }
        catch (Exception e)
        {
            throw new DataBackendException("Could not serialize permanent storage", e);
        }

        umDelegate.saveUser(((DefaultUserImpl)user).getUserDelegate());
    }

    /**
     * Saves User data when the session is unbound. The user account is required
     * to exist in the storage.
     *
     * LastLogin, AccessCounter, persistent pull tools, and any data stored
     * in the permData hashtable that is not mapped to a column will be saved.
     *
     * @exception UnknownEntityException if the user's account does not
     *            exist in the database.
     * @exception DataBackendException if there is a problem accessing the
     *            storage.
     */
    public void saveOnSessionUnbind(User user)
            throws UnknownEntityException, DataBackendException
    {
        store(user);
    }

    /**
     * Authenticate an User with the specified password. If authentication
     * is successful the method returns nothing. If there are any problems,
     * exception was thrown.
     *
     * @param user an User object to authenticate.
     * @param password the user supplied password.
     * @exception PasswordMismatchException if the supplied password was
     *            incorrect.
     * @exception UnknownEntityException if the user's record does not
     *            exist in the database.
     * @exception DataBackendException if there is a problem accessing the
     *            storage.
     */
    public void authenticate(User user, String password)
            throws PasswordMismatchException, UnknownEntityException,
            DataBackendException
    {
        umDelegate.authenticate(user, password);
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
    public void createAccount(User user, String initialPassword)
            throws EntityExistsException, DataBackendException
    {
        umDelegate.addUser(user, initialPassword);
    }

    /**
     * Removes an user account from the system.
     *
     * @param user the object describing the account to be removed.
     * @throws DataBackendException if there was an error accessing the data backend.
     * @throws UnknownEntityException if the user account is not present.
     */
    public void removeAccount(User user)
            throws UnknownEntityException, DataBackendException
    {
        umDelegate.removeUser(user);
    }

    /**
     * Change the password for an User.
     *
     * @param user an User to change password for.
     * @param oldPassword the current password supplied by the user.
     * @param newPassword the current password requested by the user.
     * @exception PasswordMismatchException if the supplied password was
     *            incorrect.
     * @exception UnknownEntityException if the user's record does not
     *            exist in the database.
     * @exception DataBackendException if there is a problem accessing the
     *            storage.
     */
    public void changePassword(User user, String oldPassword,
                               String newPassword)
            throws PasswordMismatchException, UnknownEntityException,
            DataBackendException
    {
        umDelegate.changePassword(
                ((DefaultUserImpl)user).getUserDelegate(),
                oldPassword, newPassword);
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
     * @exception UnknownEntityException if the user's record does not
     *            exist in the database.
     * @exception DataBackendException if there is a problem accessing the
     *            storage.
     */
    public void forcePassword(User user, String password)
            throws UnknownEntityException, DataBackendException
    {
        umDelegate.forcePassword(user, password);
    }

    /**
     * Constructs an User object to represent an anonymous user of the
     * application.
     *
     * @return An anonymous Turbine User.
     * @throws UnknownEntityException
     *             if the anonymous User object couldn't be constructed.
     */
    public <T extends User> T getAnonymousUser() throws UnknownEntityException
    {
        TurbineUser u = umDelegate.getAnonymousUser();
        return wrap(u);
    }

    /**
     * Checks whether a passed user object matches the anonymous user pattern
     * according to the configured user manager
     *
     * @param An
     *            user object
     *
     * @return True if this is an anonymous user
     *
     */
    public boolean isAnonymousUser(User u)
    {
        return umDelegate.isAnonymousUser(u);
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
    public <T extends User> T getUserInstance() throws DataBackendException
    {
        TurbineUser u = umDelegate.getUserInstance();
        return wrap(u);
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
    public <T extends User> T getUserInstance(String userName) throws DataBackendException
    {
        TurbineUser u = umDelegate.getUserInstance(userName);
        return wrap(u);
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
    public <T extends AccessControlList> T getACL(User user) throws UnknownEntityException
    {
        return umDelegate.getACL(user);
    }
}
