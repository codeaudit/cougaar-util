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
