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
