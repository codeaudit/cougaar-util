/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.contract.lang;

/**
 * Interface which allows <code>Op</code>s to be parsed, provides their 
 * arguments, and allows type checking.
 */
public interface OpParser {

  /**
   * Take the next <tt>Op</tt> argument, returning <tt>null</tt> if the end 
   * of the arguments has been reached -- essentially a tokenizer for 
   * <tt>Op</tt>s.
   * <p>
   * For example, consider "(a (b) (c (d (e)) f)".  Remove all the "("s 
   * and replace the ")"s with <tt>null</tt> to yield:<br>
   *   "a b <tt>null</tt> c d e <tt>null</tt> <tt>null</tt> f <tt>null</tt>"<br>
   * This would be the result of many calls to <tt>nextOp</tt>, with any
   * further calls returning <tt>null</tt>.
   * <p>
   * Future: Maybe create real AST?  Currently can get away with simple
   * <tt>nextOp</tt> pattern.
   */
  public Op nextOp() throws ParseException;

  /**
   * @see org.cougaar.lib.contract.lang.type.TypeList
   */
  public void setTypeList(final TypeList tl);
  public void setTypeList(final Class cl);
  public int addType(final Type type);
  public TypeList getTypeList();
  public TypeList cloneTypeList();
}
