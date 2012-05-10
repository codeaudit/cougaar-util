/*
 * <copyright>
 *  
 *  Copyright 2001-2004 BBNT Solutions, LLC
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

package org.cougaar.util.log.log4j;

import org.apache.log4j.Level;
import org.apache.log4j.Priority;

/**
 * Logging level that is higher than error but less than 
 * fatal, which is used for information messages that should 
 * rarely be filtered.
 * <p>
 * <b>NOTE:</b> a reference to the "DETAIL" level from a
 * log4j properties file must specify the full 
 * "DETAIL#org.cougaar.util.log.log4j.DetailPriority"
 * line, otherwise log4j doesn't know this class and will
 * default the level to "DEBUG".
 * <p>
 * For example, so set "com.foo.*" to DETAIL or higher:<pre>
 *   ...
 *   log4j.category.com.foo=DETAIL#org.cougaar.util.log.log4j.DetailPriority
 *   ...
 * </pre>
 * Other levels do not need this "#.." suffix, e.g.:<pre>
 *   ...
 *   log4j.category.com.foo=INFO
 *   ...
 * </pre>
 */
public class DetailPriority extends Level {

  static final int DETAIL_INT = Level.DEBUG_INT - 1;

  static String DETAIL_STR = "DETAIL";

  static final DetailPriority DETAIL =
    new DetailPriority(DETAIL_INT, DETAIL_STR, 0);

  protected DetailPriority(int level, String strLevel, int syslogEquiv) {
    super(level, strLevel, syslogEquiv);
  }

  public static Priority toPriority(String sArg, Priority defaultValue) {
    return 
      ((DETAIL_STR.equalsIgnoreCase(sArg)) ?
       (DETAIL) :
       (Priority.toPriority(sArg, defaultValue)));
  }

  public static Priority toPriority(int i) throws  IllegalArgumentException {
    return 
      ((i == DETAIL_INT)?
       (DETAIL) :
       (Priority.toPriority(i)));
  }

  public static Level toLevel(String sArg) {
    return ((DETAIL_STR.equalsIgnoreCase(sArg)) ? (DETAIL) : (Level.toLevel(sArg)));
  }

  public static Level toLevel(int val) {
    return ((val == DETAIL_INT) ? (DETAIL) : (Level.toLevel(val)));
  }

  public static Level toLevel(int val, Level defaultLevel) {
    return ((val == DETAIL_INT) ? (DETAIL) : Level.toLevel(val, defaultLevel));
  }

  public static Level toLevel(String sArg, Level defaultLevel) {
    return ((DETAIL_STR.equalsIgnoreCase(sArg)) ? (DETAIL) : Level.toLevel(sArg, defaultLevel));
  }
}
