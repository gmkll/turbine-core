package org.apache.turbine.services.security.torque;

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

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import java.sql.Connection;

import java.util.Date;
import java.util.Hashtable;

import javax.servlet.http.HttpSessionBindingEvent;

import org.apache.turbine.services.security.TurbineSecurity;

import org.apache.turbine.om.security.User;

import org.apache.turbine.util.security.TurbineSecurityException;

import org.apache.torque.om.Persistent;

/**
 * This is the User class used by the TorqueSecurity Service. It decouples
 * all the database peer access from the actual Peer object
 *
 * @author <a href="mailto:josh@stonecottage.com">Josh Lucas</a>
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
 * @author <a href="mailto:frank.kim@clearink.com">Frank Y. Kim</a>
 * @author <a href="mailto:cberry@gluecode.com">Craig D. Berry</a>
 * @author <a href="mailto:mpoeschl@marmot.at">Martin Poeschl</a>
 * @author <a href="mailto:dlr@collab.net">Daniel Rall</a>
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @version $Id$
 */

public class TorqueUser
    extends TorqueObject
    implements User,
               Persistent
{
    /** The date on which the user last accessed the application. */
    private Date lastAccessDate = null;

    /** This is data that will survive a servlet engine restart. */
    private Hashtable permStorage = null;

    /** This is data that will not survive a servlet engine restart. */
    private Hashtable tempStorage = null;

    /**
     * Constructor.
     * Create a new User and set the createDate.
     */
    public TorqueUser()
    {
        super();
        setCreateDate(new Date());
        tempStorage = new Hashtable(10);
        setHasLoggedIn(Boolean.FALSE);
    }

    /**
     * This Constructor is used when the UserPeerManager
     * has retrieved a list of Database Objects from the peer and
     * must 'wrap' them into TorqueRole Objects. You should not use it directly!
     *
     * @param obj An Object from the peer
     */
    public TorqueUser(Persistent obj)
    {
        super(obj);

        // Do not set creation date. This is only called on retrieval from
        // storage! 

        tempStorage = new Hashtable(10);
        setHasLoggedIn(Boolean.FALSE);
    }

    /**
     * Returns the underlying Object for the Peer
     *
     * Used in the UserPeerManager when building a new Criteria.
     *
     * @return The underlying persistent object
     *
     */

    public Persistent getPersistentObj()
    {
        if (obj == null)
        {
            obj = UserPeerManager.newPersistentInstance();
        }
        return obj;
    }

    /**
     * Stores the object in the database.  If the object is new,
     * it inserts it; otherwise an update is performed.
     *
     * @param dbName The name under which the object should be stored.
     *
     * @exception Exception This method might throw an exceptions
     */
    public void save(String dbName) 
        throws Exception
    {
        setObjectdata(ObjectUtils.serializeHashtable(getPermStorage()));
        super.save(dbName);
    }

    /**
     * Stores the object in the database.  If the object is new,
     * it inserts it; otherwise an update is performed.  This method
     * is meant to be used as part of a transaction, otherwise use
     * the save() method and the connection details will be handled
     * internally
     *
     * @param con A Connection object to save the object
     *
     * @exception Exception This method might throw an exceptions
     */
    public void save(Connection con) 
        throws Exception
    {
        setObjectdata(ObjectUtils.serializeHashtable(getPermStorage()));
        super.save(con);
    }

    /**
     * Makes changes made to the User attributes permanent.
     *
     * @throws TurbineSecurityException if there is a problem while
     *  saving data.
     */
    public void save()
        throws TurbineSecurityException
    {
        //
        // This is inconsistent with all the other security classes
        // because it calls save() on the underlying object directly.
        // But the UserManager calls ((Persistent)user).save()
        // so if we do TurbineSecurity.saveUser(this) here, we get
        // a nice endless loop. :-(
        //
        // Users seem to be a special kind of security objects...
        //

        try
        {
            setObjectdata(ObjectUtils.serializeHashtable(getPermStorage()));
            getPersistentObj().save();
        }
        catch (Exception e)
        {
            throw new TurbineSecurityException("User object said", e);
        }
    }

    /**
     * Returns the name of this object.
     *
     * @return The name of the object.
     */
    public String getName()
    {
        return getUserName();
    }

    /**
     * Sets the name of this object
     *
     * @param name The name of the object
     */
    public void setName(String name)
    {
        setUserName(name);
    }

    /**
     * Gets the Id of this object
     *
     * @return The Id of the object
     */
    public int getId()
    {
        return UserPeerManager.getIdAsObj(getPersistentObj()).intValue();
    }

    /**
     * Gets the Id of this object
     *
     * @return The Id of the object
     */
    public Integer getIdAsObj()
    {
        return UserPeerManager.getIdAsObj(getPersistentObj());
    }

    /**
     * Sets the Id of this object
     *
     * @param id The new Id 
     */
    public void setId(int id)
    {
        UserPeerManager.setId(getPersistentObj(), id);
    }

    /**
     * Returns the name of this user.
     *
     * @return The name of the user.
     */
    public String getUserName()
    {
        return UserPeerManager.getUserName(getPersistentObj());
    }

    /**
     * Sets the name of this user.
     *
     * @param name The name of the user.
     */
    public void setUserName(String name)
    {
        UserPeerManager.setUserName(getPersistentObj(), name);
    }

    /**
     * Returns the password of the User
     *
     * @return The password of the User
     */
    public String getPassword()
    {
        return UserPeerManager.getUserPassword(getPersistentObj());
    }

    /**
     * Sets the password of the User
     *
     * @param password The new password of the User
     */
    public void setPassword(String password)
    {
        UserPeerManager.setUserPassword(getPersistentObj(), password);
    }

    /**
     * Returns the first name of the User
     *
     * @return The first name of the User
     */
    public String getFirstName()
    {
        return UserPeerManager.getUserFirstName(getPersistentObj());
    }

    /**
     * Sets the first name of the User
     *
     * @param firstName The new first name of the User
     */
    public void setFirstName(String firstName)
    {
        UserPeerManager.setUserFirstName(getPersistentObj(), firstName);
    }

    /**
     * Returns the last name of the User
     *
     * @return The last name of the User
     */
    public String getLastName()
    {
        return UserPeerManager.getUserLastName(getPersistentObj());
    }

    /**
     * Sets the last name of User
     *
     * @param lastName The new last name of the User
     */
    public void setLastName(String lastName)
    {
        UserPeerManager.setUserLastName(getPersistentObj(), lastName);
    }

    /**
     * Returns the email address of the user
     *
     * @return The email address of the user
     */
    public String getEmail()
    {
        return UserPeerManager.getUserEmail(getPersistentObj());
    }

    /**
     * Sets the new email address of the user
     *
     * @param email The new email address of the user
     */
    public void setEmail(String email)
    {
        UserPeerManager.setUserEmail(getPersistentObj(), email);
    }

    /**
     * Returns the confirm value of the user
     *
     * @return The confirm value of the user
     */
    public String getConfirmed()
    {
        return UserPeerManager.getUserConfirmed(getPersistentObj());
    }

    /**
     * Sets the new confirm value of the user
     *
     * @param confirm The new confirm value of the user
     */
    public void setConfirmed(String confirm)
    {
        UserPeerManager.setUserConfirmed(getPersistentObj(), confirm);
    }

    /**
     * Returns the creation date of the user
     *
     * @return The creation date of the user
     */
    public java.util.Date getCreateDate()
    {
        return UserPeerManager.getUserCreateDate(getPersistentObj());
    }

    /**
     * Sets the new creation date of the user
     *
     * @param createDate The new creation date of the user
     */
    public void setCreateDate(java.util.Date createDate)
    {
        UserPeerManager.setUserCreateDate(getPersistentObj(), createDate);
    }

    /**
     * Returns the date of the last login of the user
     *             
     * @return The date of the last login of the user
     */
    public java.util.Date getLastLogin()
    {
        return UserPeerManager.getUserLastLogin(getPersistentObj());
    }

    /**
     * Sets the new date of the last login of the user
     *
     * @param lastLogin The new the date of the last login of the user
     */
    public void setLastLogin(java.util.Date lastLogin)
    {
        UserPeerManager.setUserLastLogin(getPersistentObj(), lastLogin);
    }

    /**
     * Returns the value of the objectdata for this user.
     * Objectdata is a VARBINARY column in the table used
     * to store the permanent storage table from the User
     * object.
     *
     * @return The bytes in the objectdata for this user
     */
    public byte [] getObjectdata()
    {
        return UserPeerManager.getUserObjectdata(getPersistentObj());
    }

    /**
     * Sets the value of the objectdata for the user
     *
     * @param objectdata The new the date of the last login of the user
     */
    public void setObjectdata(byte [] objectdata)
    {
        UserPeerManager.setUserObjectdata(getPersistentObj(), objectdata);
    }


    /**
     * Gets the access counter for a user from perm storage.
     *
     * @return The access counter for the user.
     */
    public int getAccessCounter()
    {
        try
        {
            return ((Integer) getPerm(User.ACCESS_COUNTER)).intValue();
        }
        catch (Exception e)
        {
            return 0;
        }
    }

    /**
     * Gets the access counter for a user during a session.
     *
     * @return The access counter for the user for the session.
     */
    public int getAccessCounterForSession()
    {
        try
        {
            return ((Integer) getTemp(User.SESSION_ACCESS_COUNTER)).intValue();
        }
        catch (Exception e)
        {
            return 0;
        }
    }

    /**
     * Increments the permanent hit counter for the user.
     */
    public void incrementAccessCounter()
    {
        // Ugh. Race city, here I come... 
        setAccessCounter(getAccessCounter() + 1);
    }

    /**
     * Increments the session hit counter for the user.
     */
    public void incrementAccessCounterForSession()
    {
        setAccessCounterForSession(getAccessCounterForSession() + 1);
    }

    /**
     * Sets the access counter for a user, saved in perm storage.
     *
     * @param cnt The new count.
     */
    public void setAccessCounter(int cnt)
    {
        setPerm(User.ACCESS_COUNTER, new Integer(cnt));
    }

    /**
     * Sets the session access counter for a user, saved in temp
     * storage.
     *
     * @param cnt The new count.
     */
    public void setAccessCounterForSession(int cnt)
    {
        setTemp(User.SESSION_ACCESS_COUNTER, new Integer(cnt));
    }

    /**
     * This method reports whether or not the user has been confirmed
     * in the system by checking the User.CONFIRM_VALUE
     * column in the users record to see if it is equal to
     * User.CONFIRM_DATA.
     *
     * @return True if the user has been confirmed.
     */
    public boolean isConfirmed()
    {
        String value = getConfirmed();
        return (value != null && value.equals(User.CONFIRM_DATA));
    }

    /**
     * The user is considered logged in if they have not timed out.
     *
     * @return Whether the user has logged in.
     */
    public boolean hasLoggedIn()
    {
        Boolean loggedIn = getHasLoggedIn();
        return (loggedIn != null && loggedIn.booleanValue());
    }

    /**
     * This sets whether or not someone has logged in.  hasLoggedIn()
     * returns this value.
     *
     * @param value Whether someone has logged in or not.
     */
    public void setHasLoggedIn(Boolean value)
    {
        setTemp(User.HAS_LOGGED_IN, value);
    }

    /**
     * Gets the last access date for this User.  This is the last time
     * that the user object was referenced.
     *
     * @return A Java Date with the last access date for the user.
     */
    public java.util.Date getLastAccessDate()
    {
        if (lastAccessDate == null)
        {
            setLastAccessDate();
        }
        return lastAccessDate;
    }

    /**
     * Sets the last access date for this User. This is the last time
     * that the user object was referenced.
     */
    public void setLastAccessDate()
    {
        lastAccessDate = new java.util.Date();
    }

    /**
     * Returns the permanent storage. This is implemented
     * as a Hashtable and backed by an VARBINARY column in
     * the database.
     *
     * @return A Hashtable.
     */
    public Hashtable getPermStorage()
    {
        if (permStorage == null)
        {
            byte [] objectdata = getObjectdata();

            if (objectdata != null)
            {
                permStorage = (Hashtable) ObjectUtils.deserialize(objectdata);
            }

            if (permStorage == null)
            {
                permStorage = new Hashtable();
            }
        }

        return permStorage;
    }

    /**
     * This should only be used in the case where we want to save the
     * data to the database.
     *
     * @param storage A Hashtable.
     */
    public void setPermStorage(Hashtable storage)
    {
        this.permStorage = storage;
    }

    /**
     * Returns the temporary storage. This is implemented
     * as a Hashtable
     *
     * @return A Hashtable.
     */
    public Hashtable getTempStorage()
    {
        if (tempStorage == null)
        {
            tempStorage = new Hashtable();
        }
        return tempStorage;
    }

    /**
     * This should only be used in the case where we want to save the
     * data to the database.
     *
     * @param storage A Hashtable.
     */
    public void setTempStorage(Hashtable storage)
    {
        this.tempStorage = storage;
    }

    /**
     * Get an object from permanent storage.
     *
     * @param name The object's name.
     * @return An Object with the given name.
     */
    public Object getPerm(String name)
    {
        return getPermStorage().get(name);
    }

    /**
     * Get an object from permanent storage; return default if value
     * is null.
     *
     * @param name The object's name.
     * @param def A default value to return.
     * @return An Object with the given name.
     */
    public Object getPerm(String name, Object def)
    {
        try
        {
            Object val = getPermStorage().get(name);
            return (val == null ? def : val);
        }
        catch (Exception e)
        {
            return def;
        }
    }

    /**
     * Put an object into permanent storage. If the value is null,
     * it will convert that to a "" because the underlying storage
     * mechanism within TorqueUser is currently a Hashtable and
     * null is not a valid value.
     *
     * @param name The object's name.
     * @param value The object.
     */
    public void setPerm(String name, Object value)
    {
        ObjectUtils.safeAddToHashtable(getPermStorage(), name, value);
    }

    /**
     * Get an object from temporary storage.
     *
     * @param name The object's name.
     * @return An Object with the given name.
     */
    public Object getTemp(String name)
    {
        return getTempStorage().get(name);
    }

    /**
     * Get an object from temporary storage; return default if value
     * is null.
     *
     * @param name The object's name.
     * @param def A default value to return.
     * @return An Object with the given name.
     */
    public Object getTemp(String name, Object def)
    {
        Object val;
        try
        {
            val = getTempStorage().get(name);
            if (val == null)
            {
                val = def;
            }
        }
        catch (Exception e)
        {
            val = def;
        }
        return val;
    }

    /**
     * Put an object into temporary storage. If the value is null,
     * it will convert that to a "" because the underlying storage
     * mechanism within TorqueUser is currently a Hashtable and
     * null is not a valid value.
     *
     * @param name The object's name.
     * @param value The object.
     */
    public void setTemp(String name, Object value)
    {
        ObjectUtils.safeAddToHashtable(getTempStorage(), name, value);
    }

    /**
     * Remove an object from temporary storage and return the object.
     *
     * @param name The name of the object to remove.
     * @return An Object.
     */
    public Object removeTemp(String name)
    {
        return getTempStorage().remove(name);
    }

    /**
     * Updates the last login date in the database.
     *
     * @exception Exception A generic exception.
     */
    public void updateLastLogin()
        throws Exception
    {
        setLastLogin(new java.util.Date());
    }

    /**
     * Implement this method if you wish to be notified when the User
     * has been Bound to the session.
     *
     * @param event Indication of value/session binding.
     */
    public void valueBound(HttpSessionBindingEvent hsbe)
    {
        // Currently we have no need for this method.
    }

    /**
     * Implement this method if you wish to be notified when the User
     * has been Unbound from the session.
     *
     * @param event Indication of value/session unbinding.
     */
    public void valueUnbound(HttpSessionBindingEvent hsbe)
    {
        try
        {
            if (hasLoggedIn())
            {
                TurbineSecurity.saveOnSessionUnbind(this);
            }
        }
        catch (Exception e)
        {
            //Log.error("TorqueUser.valueUnbound(): " + e.getMessage(), e);

            // To prevent messages being lost in case the logging system
            // goes away before sessions get unbound on servlet container
            // shutdown, print the stcktrace to the container's console.
            ByteArrayOutputStream ostr = new ByteArrayOutputStream();
            e.printStackTrace(new PrintWriter(ostr, true));
            String stackTrace = ostr.toString();
            System.out.println(stackTrace);
        }
    }

    /**
     * This gets whether or not someone has logged in.  hasLoggedIn()
     * returns this value as a boolean.  This is private because you
     * should use hasLoggedIn() instead.
     *
     * @return True if someone has logged in.
     */
    private Boolean getHasLoggedIn()
    {
        return (Boolean) getTemp(User.HAS_LOGGED_IN);
    }

}