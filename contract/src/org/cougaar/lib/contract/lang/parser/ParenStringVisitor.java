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
