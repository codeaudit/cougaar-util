/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Short form date formatter, using "month/day/year" (e.g. 10/20/1999).
 * <p>
 * Easier to use and does better error checking than 
 * <code>java.text.SimpleDateFormat</code>.
 * <p>
 * See method <code>main()</code> at end for sample usage.
 */

public class ShortDateFormat {

  protected SimpleDateFormat myDateFormat;

  public ShortDateFormat() {
    myDateFormat = new SimpleDateFormat("MM/dd/yyyy");
  }

  /**
   * @param date date to convert to string
   * @return date string
   */
  public String toString(Date date) {
    return 
      myDateFormat.format(
        ((date != null) ? date : new Date()),
        new StringBuffer(""), 
        new java.text.FieldPosition(DateFormat.YEAR_FIELD)).toString();
  }

  /**
   * Helper for adjusting a given date.
   * <p>
   * @param date date to adjust (current date if null)
   * @param months adjust by number of months
   * @param days adjust by number of days
   * @return adjusted date
   */
  public static Date adjustDate(Date date, int months, int days) {
    if (date == null)
      date = new Date();
    if ((months != 0) || (days != 0)) {
      Calendar cal = Calendar.getInstance();
      cal.setTime(date);
      if (months != 0)
        cal.add(Calendar.MONTH, months);
      if (days != 0)
        cal.add(Calendar.DATE, days);
      date = cal.getTime();
    }
    return date;
  }

  /**
   * Helper for filling hour:minute:second of a given date to
   * current hour:minute:second, since default is 0:00:00
   * <p>
   * @param date date needing hour:minute:second
   * @return filled date
   */
  public static Date fixTime(Date date) {
    if (date == null)
      date = new Date();
    else {
      Calendar cal = Calendar.getInstance();
      cal.setTime(date);
      Calendar nowCal = Calendar.getInstance();
      cal.set(Calendar.HOUR_OF_DAY, nowCal.get(Calendar.HOUR_OF_DAY));
      cal.set(Calendar.MINUTE, nowCal.get(Calendar.MINUTE));
      cal.set(Calendar.SECOND, nowCal.get(Calendar.SECOND));
      date = cal.getTime();
    }
    return date;
  }

  /**
   * @param sDate Short date string (e.g. "10/20/1999")
   * @return Corresponding Date
   */
  public Date toDate(String sDate) {
    return toDate(sDate, true);
  }

  /**
   * @param sDate Short date string (e.g. "10/20/1999")
   * @param doFixTime if true then adjust hour:minute to current hour:minute
   * @return Corresponding Date
   */
  public Date toDate(String sDate, boolean doFixTime) {
    Date theDate = null;
    if (isValid(sDate)) {
      try {
        // parse
        theDate = myDateFormat.parse(sDate, 
          new java.text.ParsePosition(0));
        if (doFixTime)
          theDate = fixTime(theDate);
      } catch (Exception badDateE) {
      }
    }
    return theDate;
  }

  /**
   * <code>DateFormat</code> seems to do a lousy job at parsing the 
   * date -- it allows all sorts of mistakes (e.g. "12/12/1x944", etc).
   * What we want in BNF is:<br>
   *   digit := ('0'..'9')<br>
   *   slash := '/'<br>
   *   days := (digit | (digit digit))<br>
   *   months := (digit | (digit digit))<br>
   *   years := (digit digit digit digit)<br>
   *   date := months slash days slash years<br>
   * We'll let the myDateFormatter check the digit values.
   * <p>
   * Note: Doesn't check values! "99/99/9999" is "valid"!
   * <p>
   * @param sDate Short date string (e.g. "10/20/1999")
   * @return true if sDate format is valid
   */
  protected static boolean isValid(String sDate) {
    if (sDate == null) 
      return false;
    // BEGIN
    int iPos = 0;
    // 1 or 2 digits
    iPos = readDigits(sDate, iPos, 1, 2);
    if (iPos < 0)
      return false;
    // slash
    if ((iPos >= sDate.length()) ||
        (sDate.charAt(iPos) != '/'))
      return false;
    // 1 or 2 digits
    iPos = readDigits(sDate, iPos+1, 1, 2);
    if (iPos < 0)
      return false;
    // slash
    if ((iPos >= sDate.length()) ||
        (sDate.charAt(iPos) != '/'))
      return false;
    // 4 digits
    iPos = readDigits(sDate, iPos+1, 4, 4);
    if (iPos < 0)
      return false;
    // END
    if (iPos != sDate.length())
      return false;
    // VALID
    return true;
  }

  /**
   * read digits from given string and starting position.  check that
   * the given range of digits were read.
   */
  protected static int readDigits(String s, int iStart, 
      int minDigits, int maxDigits) {
    int iCurrent;
    for (iCurrent = iStart; 
         ((iCurrent < s.length()) &&
          (Character.isDigit(s.charAt(iCurrent))));
         iCurrent++);
    int iDiff = iCurrent - iStart;
    if ((iDiff < minDigits) || (iDiff > maxDigits))
      return -1;
    return iCurrent;
  }
}

