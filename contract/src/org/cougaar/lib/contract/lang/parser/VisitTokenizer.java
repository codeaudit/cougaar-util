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

import java.util.*;

import org.cougaar.lib.contract.lang.*;

/**
 * A generic tree tokenizer, used as a buffer from a <code>TreeVisitor</code>.
 */
public interface VisitTokenizer {

  /** 
   * Tokens.
   */
  public static final int TT_END = 0;
  public static final int TT_END_OF_TREE = 1;
  public static final int TT_WORD = 2;
  public static final int TT_CONSTANT = 3;

  /**
   * Move to the beginning.
   */
  public void rewind();

  /** 
   * Set the marker at the current point. 
   * @see reset
   */
  public void mark();

  /** 
   * Move to the marker. 
   * @see mark
   */
  public void reset();
  
  /**
   * @return a TT_* constant.
   */
  public int nextToken();

  /**
   * @return a TT_* constant.
   */
  public int previousToken();

  /**
   * Should only be called if the current token is <tt>TT_WORD</tt>.
   */
  public String getWord();

  /**
   * Should only be called if the current token is <tt>TT_CONSTANT</tt>.
   */
  public String getConstantType();

  /**
   * Should only be called if the current token is <tt>TT_CONSTANT</tt>.
   */
  public String getConstantValue();
}
