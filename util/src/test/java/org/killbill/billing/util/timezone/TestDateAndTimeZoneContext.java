/*
 * Copyright 2010-2013 Ning, Inc.
 * Copyright 2014-2016 Groupon, Inc
 * Copyright 2014-2016 The Billing Project, LLC
 *
 * The Billing Project licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.killbill.billing.util.timezone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.killbill.billing.util.AccountDateAndTimeZoneContext;
import org.killbill.billing.util.UtilTestSuiteNoDB;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

//
// There are two categories of tests, one that test the offset calculation and one that calculates
// how to get a DateTime from a LocalDate (in account time zone)
//
// Tests {1, 2, 3} use an account timezone with a negative offset (-8) and tests {A, B, C} use an account timezone with a positive offset (+8)
//
public class TestDateAndTimeZoneContext extends UtilTestSuiteNoDB {

    private final DateTimeFormatter DATE_TIME_FORMATTER = ISODateTimeFormat.dateTimeParser();
    private final String effectiveDateTime1 = "2012-01-20T07:30:42.000Z";
    private final String effectiveDateTime2 = "2012-01-20T08:00:00.000Z";
    private final String effectiveDateTime3 = "2012-01-20T08:45:33.000Z";
    private final String effectiveDateTimeA = "2012-01-20T16:30:42.000Z";
    private final String effectiveDateTimeB = "2012-01-20T16:00:00.000Z";
    private final String effectiveDateTimeC = "2012-01-20T15:30:42.000Z";

    @Test(groups = "fast")
    public void testComputeUTCDateTimeFromLocalDate1() {
        final DateTime effectiveDateTime = DATE_TIME_FORMATTER.parseDateTime(effectiveDateTime1);

        final DateTimeZone timeZone = DateTimeZone.forOffsetHours(-8);
        internalCallContext.setReferenceDateTimeZone(timeZone);
        final AccountDateAndTimeZoneContext dateContext = new DefaultAccountDateAndTimeZoneContext(effectiveDateTime, internalCallContext);

        final LocalDate endDate = new LocalDate(2013, 01, 19);
        final DateTime endDateTimeInUTC = dateContext.computeUTCDateTimeFromLocalDate(endDate);
        assertTrue(endDateTimeInUTC.compareTo(effectiveDateTime.plusYears(1)) == 0);
    }

    @Test(groups = "fast")
    public void testComputeUTCDateTimeFromLocalDate2() {
        final DateTime effectiveDateTime = DATE_TIME_FORMATTER.parseDateTime(effectiveDateTime2);

        final DateTimeZone timeZone = DateTimeZone.forOffsetHours(-8);
        internalCallContext.setReferenceDateTimeZone(timeZone);
        final AccountDateAndTimeZoneContext dateContext = new DefaultAccountDateAndTimeZoneContext(effectiveDateTime, internalCallContext);

        final LocalDate endDate = new LocalDate(2013, 01, 20);
        final DateTime endDateTimeInUTC = dateContext.computeUTCDateTimeFromLocalDate(endDate);
        assertTrue(endDateTimeInUTC.compareTo(effectiveDateTime.plusYears(1)) == 0);
    }

    @Test(groups = "fast")
    public void testComputeUTCDateTimeFromLocalDate3() {
        final DateTime effectiveDateTime = DATE_TIME_FORMATTER.parseDateTime(effectiveDateTime3);

        final DateTimeZone timeZone = DateTimeZone.forOffsetHours(-8);
        internalCallContext.setReferenceDateTimeZone(timeZone);
        final AccountDateAndTimeZoneContext dateContext = new DefaultAccountDateAndTimeZoneContext(effectiveDateTime, internalCallContext);

        final LocalDate endDate = new LocalDate(2013, 01, 20);
        final DateTime endDateTimeInUTC = dateContext.computeUTCDateTimeFromLocalDate(endDate);
        assertTrue(endDateTimeInUTC.compareTo(effectiveDateTime.plusYears(1)) == 0);
    }

    @Test(groups = "fast")
    public void testComputeUTCDateTimeFromLocalDateA() {
        final DateTime effectiveDateTime = DATE_TIME_FORMATTER.parseDateTime(effectiveDateTimeA);

        final DateTimeZone timeZone = DateTimeZone.forOffsetHours(8);
        internalCallContext.setReferenceDateTimeZone(timeZone);
        final AccountDateAndTimeZoneContext dateContext = new DefaultAccountDateAndTimeZoneContext(effectiveDateTime, internalCallContext);

        final LocalDate endDate = new LocalDate(2013, 01, 21);
        final DateTime endDateTimeInUTC = dateContext.computeUTCDateTimeFromLocalDate(endDate);
        assertTrue(endDateTimeInUTC.compareTo(effectiveDateTime.plusYears(1)) == 0);
    }

    @Test(groups = "fast")
    public void testComputeUTCDateTimeFromLocalDateB() {
        final DateTime effectiveDateTime = DATE_TIME_FORMATTER.parseDateTime(effectiveDateTimeB);

        final DateTimeZone timeZone = DateTimeZone.forOffsetHours(8);
        internalCallContext.setReferenceDateTimeZone(timeZone);
        final AccountDateAndTimeZoneContext dateContext = new DefaultAccountDateAndTimeZoneContext(effectiveDateTime, internalCallContext);

        final LocalDate endDate = new LocalDate(2013, 01, 21);
        final DateTime endDateTimeInUTC = dateContext.computeUTCDateTimeFromLocalDate(endDate);
        assertTrue(endDateTimeInUTC.compareTo(effectiveDateTime.plusYears(1)) == 0);
    }

    @Test(groups = "fast")
    public void testComputeUTCDateTimeFromLocalDateC() {
        final DateTime effectiveDateTime = DATE_TIME_FORMATTER.parseDateTime(effectiveDateTimeC);

        final DateTimeZone timeZone = DateTimeZone.forOffsetHours(8);
        internalCallContext.setReferenceDateTimeZone(timeZone);
        final AccountDateAndTimeZoneContext dateContext = new DefaultAccountDateAndTimeZoneContext(effectiveDateTime, internalCallContext);

        final LocalDate endDate = new LocalDate(2013, 01, 20);
        final DateTime endDateTimeInUTC = dateContext.computeUTCDateTimeFromLocalDate(endDate);
        assertTrue(endDateTimeInUTC.compareTo(effectiveDateTime.plusYears(1)) == 0);
    }

    @Test(groups = "fast")
    public void testComputeTargetDateWithDayLightSaving() {
        final DateTime dateTime1 = new DateTime("2015-01-01T08:01:01.000Z");
        final DateTime dateTime2 = new DateTime("2015-09-01T08:01:01.000Z");
        final DateTime dateTime3 = new DateTime("2015-12-01T08:01:01.000Z");

        // Alaska Standard Time
        final DateTimeZone tz = DateTimeZone.forID("America/Juneau");
        internalCallContext.setReferenceDateTimeZone(tz);

        // Time zone is AKDT (UTC-8h) between March and November
        final DateTime referenceDateTimeWithDST = new DateTime("2015-09-01T08:01:01.000Z");
        final AccountDateAndTimeZoneContext tzContextWithDST = new DefaultAccountDateAndTimeZoneContext(referenceDateTimeWithDST, internalCallContext);
        assertEquals(tzContextWithDST.computeLocalDateFromFixedAccountOffset(dateTime1), new LocalDate("2015-01-01"));
        assertEquals(tzContextWithDST.computeLocalDateFromFixedAccountOffset(dateTime2), new LocalDate("2015-09-01"));
        assertEquals(tzContextWithDST.computeLocalDateFromFixedAccountOffset(dateTime3), new LocalDate("2015-12-01"));

        // Time zone is AKST (UTC-9h) otherwise
        final DateTime referenceDateTimeWithoutDST = new DateTime("2015-02-01T08:01:01.000Z");
        final AccountDateAndTimeZoneContext tzContextWithoutDST = new DefaultAccountDateAndTimeZoneContext(referenceDateTimeWithoutDST, internalCallContext);
        assertEquals(tzContextWithoutDST.computeLocalDateFromFixedAccountOffset(dateTime1), new LocalDate("2014-12-31"));
        assertEquals(tzContextWithoutDST.computeLocalDateFromFixedAccountOffset(dateTime2), new LocalDate("2015-08-31"));
        assertEquals(tzContextWithoutDST.computeLocalDateFromFixedAccountOffset(dateTime3), new LocalDate("2015-11-30"));
    }
}
