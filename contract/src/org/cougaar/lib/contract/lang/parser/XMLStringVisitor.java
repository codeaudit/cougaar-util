/*
 * <copyright>
 *  Copyright 1999-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
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
