/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

package org.cougaar.lib.contract.lang.parser;

import java.io.*;
import java.util.*;

import org.cougaar.lib.contract.lang.*;

/**
 * A simple <code>String</code> indent factory.
 */
public class IndentFactory {

  private static String[] presetIndents;
  static {
    growPresetIndents();
  }

  private IndentFactory() { }

  /**
   * Create a <code>String</code> representing a newline and an indent of 
   * <tt>(2*indent)</tt> spaces (' ').
   * <p>
   * For example, <tt>createIndent(3)</tt> is equal to <tt>"\n      "</tt>.
   */
  public static final String createIndent(final int indent) {
    while (true) {
      try {
        return presetIndents[indent];
      } catch (ArrayIndexOutOfBoundsException e) {
	// rare!
	if (indent < 0)
	  throw new RuntimeException("NEGATIVE INDENT???");
	growPresetIndents();
      }
    }
  }

  /**
   * Grow the <tt>presetIndents</tt> array, due to initialization or
   * the request of a <tt>createIndent</tt> beyond the length of
   * <tt>presetIndents</tt>.
   */
  private static void growPresetIndents() {
    // synchronize?
    int newLen = ((presetIndents != null) ? 
	          (2*presetIndents.length) :
		  10);
    presetIndents = new String[newLen];
    StringBuffer parensb = new StringBuffer(2*newLen);
    parensb.append("\n");
    for (int i = 0; i < newLen; i++) {
      presetIndents[i] = parensb.toString();
      parensb.append("  ");
    }
  }
}
