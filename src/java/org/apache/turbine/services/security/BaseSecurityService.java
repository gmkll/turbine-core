package org.apache.turbine.services.security;

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

import java.util.Map;
import javax.servlet.ServletConfig;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.util.Criteria;
import org.apache.turbine.om.security.Group;
import org.apache.turbine.om.security.Permission;
import org.apache.turbine.om.security.Role;
import org.apache.turbine.om.security.User;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.services.crypto.TurbineCrypto;
import org.apache.turbine.services.crypto.CryptoService;
import org.apache.turbine.services.crypto.CryptoAlgorithm;
import org.apache.turbine.services.factory.FactoryService;
import org.apache.turbine.services.InitializationException;
import org.apache.turbine.services.TurbineBaseService;
import org.apache.turbine.util.security.AccessControlList;
import org.apache.turbine.util.security.DataBackendException;
import org.apache.turbine.util.security.EntityExistsException;
import org.apache.turbine.util.security.GroupSet;
import org.apache.turbine.util.security.PasswordMismatchException;
import org.apache.turbine.util.security.PermissionSet;
import org.apache.turbine.util.security.RoleSet;
import org.apache.turbine.util.security.TurbineAccessControlList;
import org.apache.turbine.util.security.UnknownEntityException;

/**
 * This is a common subset of SecurityService implementation.
 *
 * Provided functionality includes:
 * <ul>
 * <li> methods for retrieving User objects, that delegates functionality
 *      to the pluggable implementations of the User interface.
 * <li> synchronization mechanism for methods reading/modifying the security
 *      information, that guarantees that multiple threads may read the
 *      information concurrently, but threads that modify the information
 *      acquires exclusive access.
 * <li> implementation of convenience methods for retrieving security entities
 *      that maintain in-memory caching of objects for fast access.
 * </ul>
 *
 * @author <a href="mailto:Rafal.Krzewski@e-point.pl">Rafal Krzewski</a>
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @author <a href="mailto:marco@intermeta.de">Marco Kn&uuml;ttel</a>
 * @version $Id$
 */
public abstract class BaseSecurityService
    extends TurbineBaseService
    implements SecurityService
{
    // management of Groups/Role/Permissions

    /** Holds a list of all groups in the systems, for speeding up the access */
    private GroupSet allGroups = null;

    /** Holds a list of all roles in the systems, for speeding up the access */
    private RoleSet allRoles = null;

    /** Holds a list of all permissions in the systems, for speeding up the access */
    private PermissionSet allPermissions = null;

    /** The number of threads concurrently reading security information */
    private int readerCount = 0;

    /** The instance of UserManager the SecurityService uses */
    private UserManager userManager = null;

    /** The class of User the SecurityService uses */
    private Class userClass = null;

    /** The class of ACL the SecurityService uses */
    private Class aclClass = null;

    /** A factory to construct ACL Objects */
    private FactoryService aclFactoryService = null;

    /**
     * The Group object that represents the <a href="#global">global group</a>.
     */
    private static Group globalGroup = null;

    /** Logging */
    private static Log log = LogFactory.getLog(BaseSecurityService.class);

    /**
     * This method provides client-side encryption of passwords.
     *
     * If <code>secure.passwords</code> are enabled in TurbineResources,
     * the password will be encrypted, if not, it will be returned unchanged.
     * The <code>secure.passwords.algorithm</code> property can be used
     * to chose which digest algorithm should be used for performing the
     * encryption. <code>SHA</code> is used by default.
     *
     * @param password the password to process
     * @return processed password
     */
    public String encryptPassword(String password)
    {
        return encryptPassword(password, null);
    }

    /**
     * This method provides client-side encryption of passwords.
     *
     * If <code>secure.passwords</code> are enabled in TurbineResources,
     * the password will be encrypted, if not, it will be returned unchanged.
     * The <code>secure.passwords.algorithm</code> property can be used
     * to chose which digest algorithm should be used for performing the
     * encryption. <code>SHA</code> is used by default.
     *
     * The used algorithms must be prepared to accept null as a
     * valid parameter for salt. All algorithms in the Fulcrum Cryptoservice
     * accept this.
     *
     * @param password the password to process
     * @param salt     algorithms that needs a salt can provide one here
     * @return processed password
     */

    public String encryptPassword(String password, String salt)
    {
        if (password == null)
        {
            return null;
        }
        String secure = getConfiguration().getString(
            SecurityService.SECURE_PASSWORDS_KEY,
            SecurityService.SECURE_PASSWORDS_DEFAULT).toLowerCase();

        String algorithm = getConfiguration().getString(
            SecurityService.SECURE_PASSWORDS_ALGORITHM_KEY,
            SecurityService.SECURE_PASSWORDS_ALGORITHM_DEFAULT);

        CryptoService cs = TurbineCrypto.getService();

        if (cs != null && (secure.equals("true") || secure.equals("yes")))
        {
            try
            {
                CryptoAlgorithm ca = cs.getCryptoAlgorithm(algorithm);

                ca.setSeed(salt);

                String result = ca.encrypt(password);

                return result;
            }
            catch (Exception e)
            {
                log.error("Unable to encrypt password: ", e);

                return null;
            }
        }
        else
        {
            return password;
        }
    }

    /**
     * Checks if a supplied password matches the encrypted password
     *
     * @param checkpw      The clear text password supplied by the user
     * @param encpw        The current, encrypted password
     *
     * @return true if the password matches, else false
     *
     */

    public boolean checkPassword(String checkpw, String encpw)
    {
        String result = encryptPassword(checkpw, encpw);

        return (result == null) ? false : result.equals(encpw);
    }

    /**
     * Initializes the SecurityService, locating the apropriate UserManager
     * This is a zero parameter variant which queries the Turbine Servlet
     * for its config.
     *
     * @throws InitializationException Something went wrong in the init stage
     */
    public void init()
        throws InitializationException
    {
        Configuration conf = getConfiguration();

        String userManagerClassName = conf.getString(
            SecurityService.USER_MANAGER_KEY,
            SecurityService.USER_MANAGER_DEFAULT);

        String userClassName = conf.getString(
            SecurityService.USER_CLASS_KEY,
            SecurityService.USER_CLASS_DEFAULT);

        String aclClassName = conf.getString(
            SecurityService.ACL_CLASS_KEY,
            SecurityService.ACL_CLASS_DEFAULT);

        try
        {
            userClass       = Class.forName(userClassName);
            aclClass        = Class.forName(aclClassName);
        }
        catch (Exception e)
        {
            if (userClass == null)
            {
                throw new InitializationException(
                    "Failed to create a Class object for User implementation", e);
            }
            if (aclClass == null)
            {
                throw new InitializationException(
                    "Failed to create a Class object for ACL implementation", e);
            }
        }

        try
        {
            userManager =
                (UserManager) Class.forName(userManagerClassName).newInstance();
        }
        catch (Exception e)
        {
            throw new InitializationException("Failed to instantiate UserManager", e);
        }

        try
        {
            aclFactoryService = (FactoryService) TurbineServices.getInstance().
                getService(FactoryService.SERVICE_NAME);
        }
        catch (Exception e)
        {
            throw new InitializationException(
                "BaseSecurityService.init: Failed to get the Factory Service object", e);
        }

        setInit(true);
    }

    /**
     * Initializes the SecurityService, locating the apropriate UserManager
     *
     * @param config a ServletConfig, to enforce early initialization
     * @throws InitializationException Something went wrong in the init stage
     * @deprecated use init() instead.
     */
    public void init(ServletConfig config) throws InitializationException
    {
        init();
    }

    /**
     * Return a Class object representing the system's chosen implementation of
     * of User interface.
     *
     * @return systems's chosen implementation of User interface.
     * @throws UnknownEntityException if the implementation of User interface
     *         could not be determined, or does not exist.
     */
    public Class getUserClass()
        throws UnknownEntityException
    {
        if (userClass == null)
        {
            throw new UnknownEntityException(
                "Failed to create a Class object for User implementation");
        }
        return userClass;
    }

    /**
     * Construct a blank User object.
     *
     * This method calls getUserClass, and then creates a new object using
     * the default constructor.
     *
     * @return an object implementing User interface.
     * @throws UnknownEntityException if the object could not be instantiated.
     */
    public User getUserInstance()
        throws UnknownEntityException
    {
        User user;
        try
        {
            user = (User) getUserClass().newInstance();
        }
        catch (Exception e)
        {
            throw new UnknownEntityException(
                "Failed instantiate an User implementation object", e);
        }
        return user;
    }

    /**
     * Return a Class object representing the system's chosen implementation of
     * of ACL interface.
     *
     * @return systems's chosen implementation of ACL interface.
     * @throws UnknownEntityException if the implementation of ACL interface
     *         could not be determined, or does not exist.
     */
    public Class getAclClass()
        throws UnknownEntityException
    {
        if (aclClass == null)
        {
            throw new UnknownEntityException(
                "Failed to create a Class object for ACL implementation");
        }
        return aclClass;
    }

    /**
     * Construct a new ACL object.
     *
     * This constructs a new ACL object from the configured class and
     * initializes it with the supplied roles and permissions.
     *
     * @param roles The roles that this ACL should contain
     * @param permissions The permissions for this ACL
     *
     * @return an object implementing ACL interface.
     * @throws UnknownEntityException if the object could not be instantiated.
     */
    public AccessControlList getAclInstance(Map roles, Map permissions)
        throws UnknownEntityException
    {
        Object[] objects    = { roles, permissions };
        String[] signatures = {Map.class.getName(), Map.class.getName()};
        AccessControlList accessControlList;

        try
        {
            accessControlList =
                (AccessControlList) aclFactoryService.getInstance(aclClass.getName(),
                                                                  objects,
                                                                  signatures);
        }
        catch (Exception e)
        {
            throw new UnknownEntityException(
                "Failed to instantiate an ACL implementation object", e);
        }

        return accessControlList;
    }

    /**
     * Check whether a specified user's account exists.
     *
     * The login name is used for looking up the account.
     *
     * @param user The user to be checked.
     * @return true if the specified account exists
     * @throws DataBackendException if there was an error accessing the data
     *         backend.
     */
    public boolean accountExists(User user)
        throws DataBackendException
    {
        return userManager.accountExists(user);
    }

    /**
     * Check whether a specified user's account exists.
     *
     * The login name is used for looking up the account.
     *
     * @param userName The name of the user to be checked.
     * @return true if the specified account exists
     * @throws DataBackendException if there was an error accessing the data
     *         backend.
     */
    public boolean accountExists(String userName)
        throws DataBackendException
    {
        return userManager.accountExists(userName);
    }

    /**
     * Authenticates an user, and constructs an User object to represent
     * him/her.
     *
     * @param username The user name.
     * @param password The user password.
     * @return An authenticated Turbine User.
     * @throws PasswordMismatchException if the supplied password was incorrect.
     * @throws UnknownEntityException if the user's account does not
     *            exist in the database.
     * @throws DataBackendException if there is a problem accessing the storage.
     */
    public User getAuthenticatedUser(String username, String password)
        throws DataBackendException, UnknownEntityException,
               PasswordMismatchException
    {
        return userManager.retrieve(username, password);
    }

    /**
     * Constructs an User object to represent a registered user of the
     * application.
     *
     * @param username The user name.
     * @return A Turbine User.
     * @throws UnknownEntityException if the user's account does not exist
     * @throws DataBackendException if there is a problem accessing the storage.
     */
    public User getUser(String username)
        throws DataBackendException, UnknownEntityException
    {
        return userManager.retrieve(username);
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
     * @throws DataBackendException if there is a problem accessing the storage.
     */
    public User[] getUsers(Criteria criteria)
        throws DataBackendException
    {
        return userManager.retrieve(criteria);
    }

    /**
     * Constructs an User object to represent an anonymous user of the
     * application.
     *
     * @return An anonymous Turbine User.
     * @throws UnknownEntityException if the implementation of User interface
     *         could not be determined, or does not exist.
     */
    public User getAnonymousUser()
        throws UnknownEntityException
    {
        User user = getUserInstance();
        user.setUserName("");
        return user;
    }

    /**
     * Saves User's data in the permanent storage. The user account is required
     * to exist in the storage.
     *
     * @param user the User object to save
     * @throws UnknownEntityException if the user's account does not
     *         exist in the database.
     * @throws DataBackendException if there is a problem accessing the storage.
     */
    public void saveUser(User user)
        throws UnknownEntityException, DataBackendException
    {
        userManager.store(user);
    }

    /**
     * Creates new user account with specified attributes.
     *
     * @param user the object describing account to be created.
     * @param password The password to use for the account.
     *
     * @throws DataBackendException if there was an error accessing the
     *         data backend.
     * @throws EntityExistsException if the user account already exists.
     */
    public void addUser(User user, String password)
        throws DataBackendException, EntityExistsException
    {
        userManager.createAccount(user, password);
    }

    /**
     * Removes an user account from the system.
     *
     * @param user the object describing the account to be removed.
     * @throws DataBackendException if there was an error accessing the data
     *         backend.
     * @throws UnknownEntityException if the user account is not present.
     */
    public void removeUser(User user)
        throws DataBackendException, UnknownEntityException
    {
        // revoke all roles form the user
        revokeAll(user);

        userManager.removeAccount(user);
    }

    /**
     * Change the password for an User.
     *
     * @param user an User to change password for.
     * @param oldPassword the current password supplied by the user.
     * @param newPassword the current password requested by the user.
     * @throws PasswordMismatchException if the supplied password was incorrect.
     * @throws UnknownEntityException if the user's record does not
     *            exist in the database.
     * @throws DataBackendException if there is a problem accessing the storage.
     */
    public void changePassword(User user, String oldPassword,
                               String newPassword)
        throws PasswordMismatchException, UnknownEntityException,
               DataBackendException
    {
        userManager.changePassword(user, oldPassword, newPassword);
    }

    /**
     * Forcibly sets new password for an User.
     *
     * This is supposed by the administrator to change the forgotten or
     * compromised passwords. Certain implementatations of this feature
     * would require administrative level access to the authenticating
     * server / program.
     *
     * @param user an User to change password for.
     * @param password the new password.
     * @throws UnknownEntityException if the user's record does not
     *            exist in the database.
     * @throws DataBackendException if there is a problem accessing the storage.
     */
    public void forcePassword(User user, String password)
        throws UnknownEntityException, DataBackendException
    {
        userManager.forcePassword(user, password);
    }

    /**
     * Acquire a shared lock on the security information repository.
     *
     * Methods that read security information need to invoke this
     * method at the beginning of their body.
     */
    protected synchronized void lockShared()
    {
        readerCount++;
    }

    /**
     * Release a shared lock on the security information repository.
     *
     * Methods that read security information need to invoke this
     * method at the end of their body.
     */
    protected synchronized void unlockShared()
    {
        readerCount--;
        this.notify();
    }

    /**
     * Acquire an exclusive lock on the security information repository.
     *
     * Methods that modify security information need to invoke this
     * method at the beginning of their body. Note! Those methods must
     * be <code>synchronized</code> themselves!
     */
    protected void lockExclusive()
    {
        while (readerCount > 0)
        {
            try
            {
                this.wait();
            }
            catch (InterruptedException e)
            {
            }
        }
    }

    /**
     * Release an exclusive lock on the security information repository.
     *
     * This method is provided only for completeness. It does not really
     * do anything. Note! Methods that modify security information
     * must be <code>synchronized</code>!
     */
    protected void unlockExclusive()
    {
        // do nothing
    }

    /**
     * Provides a reference to the Group object that represents the
     * <a href="#global">global group</a>.
     *
     * @return a Group object that represents the global group.
     */
    public Group getGlobalGroup()
    {
        if (globalGroup == null)
        {
            synchronized (BaseSecurityService.class)
            {
                if (globalGroup == null)
                {
                    try
                    {
                        globalGroup = getAllGroups()
                            .getGroup(Group.GLOBAL_GROUP_NAME);
                    }
                    catch (DataBackendException e)
                    {
                        log.error("Failed to retrieve global group object: ", e);
                    }
                }
            }
        }
        return globalGroup;
    }

    /**
     * Retrieve a Group object with specified name.
     *
     * @param name the name of the Group.
     * @return an object representing the Group with specified name.
     * @throws DataBackendException if there was an error accessing the
     *         data backend.
     * @throws UnknownEntityException if the group does not exist.
     */
    public Group getGroup(String name)
        throws DataBackendException, UnknownEntityException
    {
        GroupSet groups = getAllGroups();
        Group group = groups.getGroup(name);
        if (group != null)
        {
            return group;
        }
        else
        {
            throw new UnknownEntityException(
                "The specified group does not exist");
        }
    }

    /**
     * Retrieve a Role object with specified name.
     *
     * @param name the name of the Role.
     * @return an object representing the Role with specified name.
     * @throws DataBackendException if there was an error accessing the
     *         data backend.
     * @throws UnknownEntityException if the role does not exist.
     */
    public Role getRole(String name)
        throws DataBackendException, UnknownEntityException
    {
        RoleSet roles = getAllRoles();
        Role role = roles.getRole(name);
        if (role != null)
        {
            role.setPermissions(getPermissions(role));
            return role;
        }
        else
        {
            throw new UnknownEntityException(
                "The specified role does not exist");
        }
    }

    /**
     * Retrieve a Permission object with specified name.
     *
     * @param name the name of the Permission.
     * @return an object representing the Permission with specified name.
     * @throws DataBackendException if there was an error accessing the
     *         data backend.
     * @throws UnknownEntityException if the permission does not exist.
     */
    public Permission getPermission(String name)
        throws DataBackendException, UnknownEntityException
    {
        PermissionSet permissions = getAllPermissions();
        Permission permission = permissions.getPermission(name);
        if (permission != null)
        {
            return permission;
        }
        else
        {
            throw new UnknownEntityException(
                "The specified permission does not exist");
        }
    }

    /**
     * Retrieves all groups defined in the system.
     *
     * @return the names of all groups defined in the system.
     * @throws DataBackendException if there was an error accessing the
     *         data backend.
     */
    public GroupSet getAllGroups()
        throws DataBackendException
    {
        if (allGroups == null)
        {
            synchronized (this)
            {
                if (allGroups == null)
                {
                    allGroups = getGroups(new Criteria());
                }
            }
        }
        return allGroups;
    }

    /**
     * Retrieves all roles defined in the system.
     *
     * @return the names of all roles defined in the system.
     * @throws DataBackendException if there was an error accessing the
     *         data backend.
     */
    public RoleSet getAllRoles()
        throws DataBackendException
    {
        if (allRoles == null)
        {
            synchronized (this)
            {
                if (allRoles == null)
                {
                    allRoles = getRoles(new Criteria());
                }
            }
        }
        return allRoles;
    }

    /**
     * Retrieves all permissions defined in the system.
     *
     * @return the names of all roles defined in the system.
     * @throws DataBackendException if there was an error accessing the
     *         data backend.
     */
    public PermissionSet getAllPermissions()
        throws DataBackendException
    {
        if (allPermissions == null)
        {
            synchronized (this)
            {
                if (allPermissions == null)
                {
                    allPermissions = getPermissions(new Criteria());
                }
            }
        }
        return allPermissions;
    }
}
