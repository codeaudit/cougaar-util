/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
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
