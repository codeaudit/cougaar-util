/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.lib.contract.lang;

/**
 * An <code>org.cougaar.lib.contract.Operator</code> parse failure.
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
    //e.printStackTrace();
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
