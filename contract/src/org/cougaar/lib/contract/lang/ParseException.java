/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.contract.lang;

/**
 * An <code>org.cougaar.core.util.Operator</code> parse failure.
 */
public class ParseException extends Exception {

  private StringBuffer trace;
  private String cacheTrace;

  private ParseException() { }

  public ParseException(String s) {
    if (s != null) {
      trace = new StringBuffer(s.trim());
      trace.append("\nTrace:\n");
    } else {
      trace = new StringBuffer("Unknown Parse Exception\nTrace:\n");
    }
  }

  public ParseException(Exception e) {
    this(e.toString());
    e.printStackTrace();
  }

  public void addTrace(String line) {
    trace.append("       \"at ");
    if (line != null) {
      trace.append(line).append("\"\n");
    } else {
      trace.append("<Unknown line>\"\n");
    }
  }

  /**
   * Override <code>java.lang.Exception</code>'s <tt>getMessage</tt> with
   * our custom trace message.
   */
  public String getMessage() {
    if (cacheTrace == null) {
      cacheTrace = trace.toString();
    }
    return cacheTrace;
  }
}
