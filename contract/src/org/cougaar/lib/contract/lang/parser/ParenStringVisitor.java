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

package org.cougaar.lib.contract.lang.parser;

import java.io.*;
import java.util.*;

import org.cougaar.lib.contract.lang.*;

/**
 * Implementation of <code>TreeVisitor</code> which will create a
 * "paren" semi-lisp style <tt>toString</tt>
 */
public class ParenStringVisitor implements StringVisitor {

  private StringBuffer sb;
  private boolean pretty;
  private int indent;
  private boolean verbose;

  public void initialize() {
    sb = new StringBuffer();
    this.pretty = false;
    indent = 0;
    verbose = DEFAULT_VERBOSE;
  }

  public final boolean isVerbose() {
    return verbose;
  }

  public final void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  public final boolean isPrettyPrint() {
    return pretty;
  }

  public final void setPrettyPrint(boolean pretty) {
    this.pretty = pretty;
    this.indent = 0;
  }

  public final void visitEnd() {
    if (pretty) {
      --indent;
    }
    sb.append(")");
  }

  public final void visitWord(String word) {
    if (pretty) {
      sb.append(IndentFactory.createIndent(indent++));
    }
    sb.append("(").append(word);
  }

  public final void visitConstant(String type, String value) {
    if (pretty) {
      sb.append(IndentFactory.createIndent(indent));
    }
    if (type == null) {
      sb.append("\"").append(value).append("\"");
    } else {
      sb.append("(const \"").append(type);
      sb.append("\" \"").append(value).append("\")");
    }
  }

  /** Short for <tt>visitConstant("java.lang.String", value)</tt>. */
  public final void visitConstant(String value) {
    visitConstant(null, value);
  }

  public final void visitEndOfTree() {
  }

  public String toString() {
    return sb.toString();
  }
}
