package org.apache.turbine.services.pull.util;

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
import java.util.GregorianCalendar;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test class for DateFormatter.
 *
 * @author <a href="mailto:seade@backstagetech.com.au">Scott Eade</a>
 * @version $Id$
 */
public class DateFormatterTest
{

//    /*
//     * Class under test for String format(Date)
//     */
//    public void testFormatDate()
//    {
//        // Need configuration to test.
//    }

    /*
     * Class under test for String format(Date, String)
     */
    @Test public void testFormatDateString()
    {
        Calendar cal = new GregorianCalendar();
        DateFormatter df = new DateFormatter();
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH) + 1; // zero based
        int year = cal.get(Calendar.YEAR);
        String dayString = (day < 10 ? "0" : "") + day;
        String monthString = (month < 10 ? "0" : "") + month;
        String ddmmyyyy = dayString + "/" + monthString + "/" + year;
        assertEquals(ddmmyyyy, df.format(cal.getTime(), "dd/MM/yyyy"));

        String mmddyyyy = "" + monthString + "/" + dayString + "/" + year;
        assertEquals(mmddyyyy, df.format(cal.getTime(), "MM/dd/yyyy"));
    }

    /*
     * Class under test for String format(null, String)
     */
    @Test public void testFormatDateStringNullString()
    {
        DateFormatter df = new DateFormatter();
        assertEquals("null argument should produce an empty String",
                "", df.format(null, "MM/dd/yyyy"));
    }

    /*
     * Class under test for String format(Date, "")
     */
    @Test public void testFormatDateStringEmptyString()
    {
        Date today = new Date();
        DateFormatter df = new DateFormatter();
        assertEquals("Empty pattern should produce empty String",
                "", df.format(today, ""));
    }

    /*
     * Class under test for String format(Date, "")
     */
    @Test public void testFormatDateStringNullFormat()
    {
        Date today = new Date();
        DateFormatter df = new DateFormatter();
        assertEquals("null pattern should produce empty String",
                "", df.format(today, null));
    }

}
