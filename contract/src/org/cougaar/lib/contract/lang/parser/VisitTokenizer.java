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
