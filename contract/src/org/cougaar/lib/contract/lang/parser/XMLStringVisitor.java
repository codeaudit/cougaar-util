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
 * Creates a <code>TreeVisitor</code> that has XML as a <tt>toString</tt>.
 */
public class XMLStringVisitor implements StringVisitor {

  /**
   * The <code>String</code> result buffer.
   */
  private StringBuffer sb;

  /**
   * A stack of words, with <tt>inWord</tt> as the implied top.
   */
  private Stack stk;

  /**
   * The most current word, or null.
   * <p>
   * This is used to trim "&gt;foo&lt;&gt;/foo&lt;" down to "&gt;foo/&lt;"
   * by recording the "top" of the <tt>stk</tt>.
   */
  private String inWord;

  /**
   * Verbose flag.
   */
  private boolean verbose;

  /**
   * Pretty-print flag;
   */
  private boolean pretty;

  /**
   * Indent depth if <tt>pretty</tt> is <tt>true</tt>.
   */
  private int indent;

  public final void initialize() { 
    sb = new StringBuffer();
    stk = new Stack();
    inWord = null;
    verbose = DEFAULT_VERBOSE;
    pretty = true;//false;
    indent = 0;
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
    if (inWord != null) {
      sb.append("/>");
      inWord = null;
      if (pretty) {
        --indent;
      }
    } else {
      String w = (String)stk.pop();
      if (pretty) {
        sb.append(IndentFactory.createIndent(--indent));
      }
      sb.append("</").append(w).append(">");
    }
  }

  public final void visitWord(String word) {
    if (inWord != null) {
      stk.push(inWord);
      sb.append(">");
    }
    inWord = word;
    // word
    if (pretty) {
      sb.append(IndentFactory.createIndent(indent++));
    }
    sb.append("<").append(word);
  }

  public final void visitConstant(String type, String value) {
    if (inWord != null) {
      stk.push(inWord);
      sb.append(">");
      inWord = null;
    }
    // constant "(const [\"type\"] \"value\")"
    if (pretty) {
      sb.append(IndentFactory.createIndent(indent));
    }
    sb.append("<const");
    if (type != null) {
      sb.append(" type=\"").append(type).append("\"");
    }
    sb.append(" value=\"").append(value).append("\"/>");
  }

  public final void visitConstant(String value) {
    visitConstant(null, value);
  }

  public final void visitEndOfTree() {
  }

  public String toString() {
    return sb.toString();
  }
}
