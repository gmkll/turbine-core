package org.apache.turbine.services.intake;


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


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fulcrum.intake.IntakeException;
import org.apache.fulcrum.intake.IntakeService;
import org.apache.fulcrum.intake.Retrievable;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.parser.ValueParser;
import org.apache.fulcrum.pool.Recyclable;
import org.apache.turbine.annotation.TurbineService;
import org.apache.turbine.services.pull.ApplicationTool;
import org.apache.turbine.util.RunData;


/**
 * The main class through which Intake is accessed.  Provides easy access
 * to the Fulcrum Intake component.
 *
 * @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @author <a href="mailto:quintonm@bellsouth.net">Quinton McCombs</a>
 * @author <a href="mailto:epugh@upstate.com">Eric Pugh</a>
 * @version $Id$
 */
public class IntakeTool
        implements ApplicationTool, Recyclable
{
    /** Used for logging */
    protected static final Log log = LogFactory.getLog(IntakeTool.class);

    /** Constant for default key */
    public static final String DEFAULT_KEY = "_0";

    /** Constant for the hidden fieldname */
    public static final String INTAKE_GRP = "intake-grp";

    /** Groups from intake.xml */
    protected HashMap<String, Group> groups = null;

    /** ValueParser instance */
    protected ValueParser pp;

    private final HashMap<String, Group> declaredGroups = new HashMap<String, Group>();
    private final StringBuilder allGroupsSB = new StringBuilder(256);
    private final StringBuilder groupSB = new StringBuilder(128);

    /** The cache of PullHelpers. **/
    private Map<String, IntakeTool.PullHelper> pullMap = null;

    /**
     * The Intake service.
     */
    @TurbineService
    protected IntakeService intakeService;

    /**
     * Constructor
     */
    public IntakeTool()
    {
    }

    /**
     * Prepares intake for a single request
     */
    @Override
    public void init(Object runData)
    {
        if (groups == null) // Initialize only once
        {
            String[] groupNames = intakeService.getGroupNames();
            int groupCount = 0;
            if (groupNames != null)
            {
                groupCount = groupNames.length;
            }
            groups = new HashMap<String, Group>((int) (1.25 * groupCount + 1));
            pullMap = new HashMap<String, IntakeTool.PullHelper>((int) (1.25 * groupCount + 1));

            for (int i = groupCount - 1; i >= 0; i--)
            {
                pullMap.put(groupNames[i], new PullHelper(groupNames[i]));
            }
        }

        this.pp = ((RunData) runData).getParameters();

        String[] groupKeys = pp.getStrings(INTAKE_GRP);
        String[] groupNames = null;
        if (groupKeys == null || groupKeys.length == 0)
        {
            groupNames = intakeService.getGroupNames();
        }
        else
        {
            groupNames = new String[groupKeys.length];
            for (int i = groupKeys.length - 1; i >= 0; i--)
            {
                groupNames[i] = intakeService.getGroupName(groupKeys[i]);
            }

        }

        for (int i = groupNames.length - 1; i >= 0; i--)
        {
            try
            {
                List<Group> foundGroups = intakeService.getGroup(groupNames[i])
                    .getObjects(pp);

                if (foundGroups != null)
                {
                    for (Group group : foundGroups)
                    {
                        groups.put(group.getObjectKey(), group);
                    }
                }
            }
            catch (IntakeException e)
            {
                log.error(e);
            }
        }
    }

    /**
     * Add all registered group ids to the value parser
     *
     * @param vp the value parser
     */
    public void addGroupsToParameters(ValueParser vp)
    {
        for (Group group : groups.values())
        {
            if (!declaredGroups.containsKey(group.getIntakeGroupName()))
            {
                declaredGroups.put(group.getIntakeGroupName(), null);
                vp.add("intake-grp", group.getGID());
            }
            vp.add(group.getGID(), group.getOID());
        }
        declaredGroups.clear();
    }

    /**
     * A convenience method to write out the hidden form fields
     * that notify intake of the relevant groups.  It should be used
     * only in templates with 1 form.  In multiform templates, the groups
     * that are relevant for each form need to be declared using
     * $intake.newForm() and $intake.declareGroup($group) for the relevant
     * groups in the form.
     *
     * @return the HTML that declares all groups to Intake in hidden input fields
     *
     */
    public String declareGroups()
    {
        allGroupsSB.setLength(0);
        for (Group group : groups.values())
        {
            declareGroup(group, allGroupsSB);
        }
        return allGroupsSB.toString();
    }

    /**
     * A convenience method to write out the hidden form fields
     * that notify intake of the group.
     *
     * @param group the group to declare
     * @return the HTML that declares the group to Intake in a hidden input field
     */
    public String declareGroup(Group group)
    {
        groupSB.setLength(0);
        declareGroup(group, groupSB);
        return groupSB.toString();
    }

    /**
     * xhtml valid hidden input field(s) that notifies intake of the
     * group's presence.
     * @param group the group to declare
     * @param sb a String Builder where the hidden field HTML will be appended
     */
    public void declareGroup(Group group, StringBuilder sb)
    {
        if (!declaredGroups.containsKey(group.getIntakeGroupName()))
        {
            declaredGroups.put(group.getIntakeGroupName(), null);
            sb.append("<input type=\"hidden\" name=\"")
                    .append(INTAKE_GRP)
                    .append("\" value=\"")
                    .append(group.getGID())
                    .append("\"/>\n");
        }
        group.appendHtmlFormInput(sb);
    }

    /**
     * Declare that a new form starts
     */
    public void newForm()
    {
        declaredGroups.clear();
        for (Group group : groups.values())
        {
            group.resetDeclared();
        }
    }

    /**
     * Implementation of ApplicationTool interface is not needed for this
     * tool as it is request scoped
     */
    @Override
    public void refresh()
    {
        // empty
    }

    /**
     * Inner class to present a nice interface to the template designer
     */
    public class PullHelper
    {
        /** Name of the group used by the pull helper */
        String groupName;

        /**
         * Protected constructor to force use of factory method.
         *
         * @param groupName the group name
         */
        protected PullHelper(String groupName)
        {
            this.groupName = groupName;
        }

        /**
         * Populates the object with the default values from the XML File
         *
         * @return a Group object with the default values
         * @throws IntakeException if getting the group fails
         */
        public Group getDefault()
                throws IntakeException
        {
            return setKey(DEFAULT_KEY);
        }

        /**
         * Calls setKey(key,true)
         *
         * @param key the group key
         * @return an Intake Group
         * @throws IntakeException if getting the group fails
         */
        public Group setKey(String key)
                throws IntakeException
        {
            return setKey(key, true);
        }

        /**
         * Return the group identified by its key
         *
         * @param key the group key
         * @param create true if a non-existing group should be created
         * @return an Intake Group
         * @throws IntakeException if getting the group fails
         */
        public Group setKey(String key, boolean create)
                throws IntakeException
        {
            Group g = null;

            String inputKey = intakeService.getGroupKey(groupName) + key;
            if (groups.containsKey(inputKey))
            {
                g = groups.get(inputKey);
            }
            else if (create)
            {
                g = intakeService.getGroup(groupName);
                groups.put(inputKey, g);
                g.init(key, pp);
            }

            return g;
        }

        /**
         * maps an Intake Group to the values from a Retrievable object.
         *
         * @param obj A retrievable object
         * @return an Intake Group
         */
        public Group mapTo(Retrievable obj)
        {
            Group g = null;

            try
            {
                String inputKey = intakeService.getGroupKey(groupName)
                        + obj.getQueryKey();
                if (groups.containsKey(inputKey))
                {
                    g = groups.get(inputKey);
                }
                else
                {
                    g = intakeService.getGroup(groupName);
                    groups.put(inputKey, g);
                }

                return g.init(obj);
            }
            catch (IntakeException e)
            {
                log.error(e);
            }

            return null;
        }
    }

    /**
     * get a specific group
     * @param groupName the name of the group
     * @return a {@link PullHelper} wrapper around the group
     */
    public PullHelper get(String groupName)
    {
        return pullMap.get(groupName);
    }

    /**
     * Get a specific group
     *
     * @param groupName the name of the group
     * @param throwExceptions if false, exceptions will be suppressed.
     * @return a {@link PullHelper} wrapper around the group
     * @throws IntakeException could not retrieve group
     */
    public PullHelper get(String groupName, boolean throwExceptions)
            throws IntakeException
    {
        return pullMap.get(groupName);
    }

    /**
     * Loops through all of the Groups and checks to see if
     * the data within the Group is valid.
     * @return true if all groups are valid
     */
    public boolean isAllValid()
    {
        boolean allValid = true;
        for (Group group : groups.values())
        {
            allValid &= group.isAllValid();
        }
        return allValid;
    }

    /**
     * Get a specific group by name and key.
     * @param groupName the name of the group
     * @param key the key for the group
     * @return the {@link Group}
     * @throws IntakeException if the group could not be retrieved
     */
    public Group get(String groupName, String key)
            throws IntakeException
    {
        return get(groupName, key, true);
    }

    /**
     * Get a specific group by name and key. Also specify
     * whether or not you want to create a new group.
     * @param groupName the name of the group
     * @param key the key for the group
     * @param create true if a new group should be created
     * @return the {@link Group}
     * @throws IntakeException if the group could not be retrieved
     */
    public Group get(String groupName, String key, boolean create)
            throws IntakeException
    {
        if (groupName == null)
        {
            throw new IntakeException("intakeService.get: groupName == null");
        }
        if (key == null)
        {
            throw new IntakeException("intakeService.get: key == null");
        }

        PullHelper ph = get(groupName);
        return (ph == null) ? null : ph.setKey(key, create);
    }

    /**
     * Removes group.  Primary use is to remove a group that has
     * been processed by an action and is no longer appropriate
     * in the view (screen).
     * @param group the group instance to remove
     */
    public void remove(Group group)
    {
        if (group != null)
        {
            groups.remove(group.getObjectKey());
            group.removeFromRequest();

            String[] groupKeys = pp.getStrings(INTAKE_GRP);

            pp.remove(INTAKE_GRP);

			if (groupKeys != null)
			{
		        for (int i = 0; i < groupKeys.length; i++)
		        {
		            if (!groupKeys[i].equals(group.getGID()))
		            {
		                 pp.add(INTAKE_GRP, groupKeys[i]);
		            }
                }
		    }

            try
            {
                intakeService.releaseGroup(group);
            }
            catch (IntakeException ie)
            {
                log.error("Tried to release unknown group "
                        + group.getIntakeGroupName());
            }
        }
    }

    /**
     * Removes all groups.  Primary use is to remove groups that have
     * been processed by an action and are no longer appropriate
     * in the view (screen).
     */
    public void removeAll()
    {
        Object[] allGroups = groups.values().toArray();
        for (int i = allGroups.length - 1; i >= 0; i--)
        {
            Group group = (Group) allGroups[i];
            remove(group);
        }
    }

    /**
     * Get a Map containing all the groups.
     *
     * @return the Group Map
     */
    public Map<String, Group> getGroups()
    {
        return groups;
    }

    // ****************** Recyclable implementation ************************

    private boolean disposed;

    /**
     * Recycles the object for a new client. Recycle methods with
     * parameters must be added to implementing object and they will be
     * automatically called by pool implementations when the object is
     * taken from the pool for a new client. The parameters must
     * correspond to the parameters of the constructors of the object.
     * For new objects, constructors can call their corresponding recycle
     * methods whenever applicable.
     * The recycle methods must call their super.
     */
    @Override
    public void recycle()
    {
        disposed = false;
    }

    /**
     * Disposes the object after use. The method is called
     * when the object is returned to its pool.
     * The dispose method must call its super.
     */
    @Override
    public void dispose()
    {
        for (Group group : groups.values())
        {
            try
            {
                intakeService.releaseGroup(group);
            }
            catch (IntakeException ie)
            {
                log.error("Tried to release unknown group "
                        + group.getIntakeGroupName());
            }
        }

        groups.clear();
        declaredGroups.clear();
        pp = null;

        disposed = true;
    }

    /**
     * Checks whether the recyclable has been disposed.
     *
     * @return true, if the recyclable is disposed.
     */
    @Override
    public boolean isDisposed()
    {
        return disposed;
    }
}
