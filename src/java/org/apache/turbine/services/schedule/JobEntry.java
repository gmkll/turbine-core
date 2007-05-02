package org.apache.turbine.services.schedule;

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

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.turbine.util.TurbineException;

/**
 * This is a wrapper for a scheduled job.  It is modeled after the
 * Unix scheduler cron.  Note: BaseJobEntry is generated by Torque
 * at compile time, and is therefore NOT in CVS.
 *
 * @author <a href="mailto:mbryson@mont.mindspring.com">Dave Bryson</a>
 * @author <a href="mailto:quintonm@bellsouth.net">Quinton McCombs</a>
 * @version $Id$
 */
public class JobEntry
        extends BaseJobEntry
        implements Comparable
{
    /** Logging */
    private static Log log = LogFactory.getLog(ScheduleService.LOGGER_NAME);

    /** indicates if job is currently running */
    private boolean jobIsActive = false;

    /** Next runtime. **/
    private long runtime = 0;

    /** schedule types **/
    private static final int SECOND = 0;
    private static final int MINUTE = 1;
    private static final int WEEK_DAY = 2;
    private static final int DAY_OF_MONTH = 3;
    private static final int DAILY = 4;

    /**
     * default constructor
     */
    public JobEntry()
    {
    }

    /**
     * Constuctor.
     *
     * Schedule a job to run on a certain point of time.<br>
     *
     * Example 1: Run the DefaultScheduledJob at 8:00am every 15th of
     * the month - <br>
     *
     * JobEntry je = new JobEntry(0,0,8,15,"DefaultScheduledJob");<br>
     *
     * Example 2: Run the DefaultScheduledJob at 8:00am every day -
     * <br>
     *
     * JobEntry je = new JobEntry(0,0,8,-1,"DefaultScheduledJob");<br>
     *
     * Example 3: Run the DefaultScheduledJob every 2 hours. - <br>
     *
     * JobEntry je = new JobEntry(0,120,-1,-1,"DefaultScheduledJob");<br>
     *
     * Example 4: Run the DefaultScheduledJob every 30 seconds. - <br>
     *
     * JobEntry je = new JobEntry(30,-1,-1,-1,"DefaultScheduledJob");<br>
     *
     * @param sec Value for entry "seconds".
     * @param min Value for entry "minutes".
     * @param hour Value for entry "hours".
     * @param wd Value for entry "week days".
     * @param day_mo Value for entry "month days".
     * @param task Task to execute.
     * @exception TurbineException a generic exception.
     */
    public JobEntry(int sec,
                    int min,
                    int hour,
                    int wd,
                    int day_mo,
                    String task)
            throws TurbineException
    {
        if (StringUtils.isEmpty(task))
        {
            throw new TurbineException("Error in JobEntry. " +
                    "Bad Job parameter. Task not set.");
        }

        setSecond(sec);
        setMinute(min);
        setHour(hour);
        setWeekDay(wd);
        setDayOfMonth(day_mo);
        setTask(task);

        calcRunTime();
    }

    /**
     * Used for ordering Jobentries
     * Note: this comparator imposes orderings that are inconsistent with
     * equals.
     *
     * @param je The first <code>JobEntry</code> object.
     * @return An <code>int</code> indicating the result of the comparison.
     */
    public int compareTo(Object je)
    {
        int result = -1;
        if (je instanceof JobEntry)
        {
            result = getJobId() - ((JobEntry) je).getJobId();
        }
        return result;
    }

    /**
     * Sets whether the job is running.
     *
     * @param isActive Whether the job is running.
     */
    public void setActive(boolean isActive)
    {
        jobIsActive = isActive;
    }

    /**
     * Check to see if job is currently active/running
     *
     * @return true if job is currently geing run by the
     *  workerthread, otherwise false
     */
    public boolean isActive()
    {
        return jobIsActive;
    }

    /**
     * Get the next runtime for this job as a long.
     *
     * @return The next run time as a long.
     */
    public long getNextRuntime()
    {
        return runtime;
    }

    /**
     * Gets the next runtime as a date
     *
     * @return Next run date
     */
    public Date getNextRunDate()
    {
        return new Date(runtime);
    }

    /**
     * Get the next runtime for this job as a String.
     *
     * @return The next run time as a String.
     */
    public String getNextRunAsString()
    {
        return getNextRunDate().toString();
    }

    /**
     * Calculate how long before the next runtime.<br>
     *
     * The runtime determines it's position in the job queue.
     * Here's the logic:<br>
     *
     * 1. Create a date the represents when this job is to run.<br>
     *
     * 2. If this date has expired, them "roll" appropriate date
     * fields forward to the next date.<br>
     *
     * 3. Calculate the diff in time between the current time and the
     * next run time.<br>
     *
     * @exception TurbineException a generic exception.
     */
    public void calcRunTime()
            throws TurbineException
    {
        Calendar schedrun = Calendar.getInstance();
        Calendar now = Calendar.getInstance();

        switch (evaluateJobType())
        {
            case SECOND:
                // SECOND (every so many seconds...)
                schedrun.add(Calendar.SECOND, getSecond());
                runtime = schedrun.getTime().getTime();
                break;

            case MINUTE:
                // MINUTE (every so many minutes...)
                schedrun.add(Calendar.SECOND, getSecond());
                schedrun.add(Calendar.MINUTE, getMinute());
                runtime = schedrun.getTime().getTime();
                break;

            case WEEK_DAY:
                // WEEKDAY (day of the week)
                schedrun.set(Calendar.SECOND, getSecond());
                schedrun.set(Calendar.MINUTE, getMinute());
                schedrun.set(Calendar.HOUR_OF_DAY, getHour());
                schedrun.set(Calendar.DAY_OF_WEEK, getWeekDay());

                if (now.before(schedrun))
                {
                    // Scheduled time has NOT expired.
                    runtime = schedrun.getTime().getTime();
                }
                else
                {
                    // Scheduled time has expired; roll to the next week.
                    schedrun.add(Calendar.DAY_OF_WEEK, 7);
                    runtime = schedrun.getTime().getTime();
                }
                break;

            case DAY_OF_MONTH:
                // DAY_OF_MONTH (date of the month)
                schedrun.set(Calendar.SECOND, getSecond());
                schedrun.set(Calendar.MINUTE, getMinute());
                schedrun.set(Calendar.HOUR_OF_DAY, getHour());
                schedrun.set(Calendar.DAY_OF_MONTH, getDayOfMonth());

                if (now.before(schedrun))
                {
                    // Scheduled time has NOT expired.
                    runtime = schedrun.getTime().getTime();
                }
                else
                {
                    // Scheduled time has expired; roll to the next month.
                    schedrun.add(Calendar.MONTH, 1);
                    runtime = schedrun.getTime().getTime();
                }
                break;

            case DAILY:
                // DAILY (certain hour:minutes of the day)
                schedrun.set(Calendar.SECOND, getSecond());
                schedrun.set(Calendar.MINUTE, getMinute());
                schedrun.set(Calendar.HOUR_OF_DAY, getHour());

                // Scheduled time has NOT expired.
                if (now.before(schedrun))
                {
                    runtime = schedrun.getTime().getTime();
                }
                else
                {
                    // Scheduled time has expired; roll forward 24 hours.
                    schedrun.add(Calendar.HOUR_OF_DAY, 24);
                    runtime = schedrun.getTime().getTime();
                }
                break;

            default:
                // Do nothing.
        }

        log.info("Next runtime for task " + this.getTask() + " is " + this.getNextRunDate());
    }

    /**
     * What schedule am I on?
     *
     * I know this is kinda ugly!  If you can think of a cleaner way
     * to do this, please jump in!
     *
     * @return A number specifying the type of schedule. See
     * calcRunTime().
     * @exception TurbineException a generic exception.
     */
    private int evaluateJobType()
            throws TurbineException
    {

        // First start by checking if it's a day of the month job.
        if (getDayOfMonth() < 0)
        {
            // Not a day of the month job... check weekday.
            if (getWeekDay() < 0)
            {
                // Not a weekday job...check if by the hour.
                if (getHour() < 0)
                {
                    // Not an hourly job...check if it is by the minute
                    if (getMinute() < 0)
                    {
                        // Not a by the minute job so must be by the second
                        if (getSecond() < 0)
                            throw new TurbineException("Error in JobEntry. Bad Job parameter.");

                        return SECOND;
                    }
                    else
                    {
                        // Must be a job run by the minute so we need minutes and
                        // seconds.
                        if (getMinute() < 0 || getSecond() < 0)
                            throw new TurbineException("Error in JobEntry. Bad Job parameter.");

                        return MINUTE;
                    }
                }
                else
                {
                    // Must be a daily job by hours minutes, and seconds.  In
                    // this case, we need the minute, second, and hour params.
                    if (getMinute() < 0 || getHour() < 0 || getSecond() < 0)
                        throw new TurbineException("Error in JobEntry. Bad Job parameter.");

                    return DAILY;
                }
            }
            else
            {
                // Must be a weekday job.  In this case, we need
                // minute, second, and hour params
                if (getMinute() < 0 || getHour() < 0 || getSecond() < 0)
                    throw new TurbineException("Error in JobEntry. Bad Job parameter.");

                return WEEK_DAY;
            }
        }
        else
        {
            // Must be a day of the month job.  In this case, we need
            // minute, second, and hour params
            if (getMinute() < 0 || getHour() < 0)
                throw new TurbineException("Error in JobEntry. Bad Job parameter.");

            return DAY_OF_MONTH;
        }
    }

}
