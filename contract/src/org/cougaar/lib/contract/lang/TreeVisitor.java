/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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
 * An generic tree visitor, customized for the contract language.
 */
 //
 // Note: 
 //  Might want to change the <tt>visitEnd()</tt> to something like
 //  <tt>visitEndWord(String)</tt>, since this would make String XML 
 //  generation easier.   The only downside is the extra memory in 
 //  saving the duplicate <code>String</code>s when tokenizing.
 //
public interface TreeVisitor {

  public static final boolean DEFAULT_VERBOSE = false;

  public void initialize();

  public boolean isVerbose();

  public void setVerbose(boolean verbose);

  public void visitEnd();

  public void visitWord(String word);

  /**
   * @param type if null, then "java.lang.String" is assumed
   */
  public void visitConstant(String type, String value);

  /** 
   * Short for <tt>visitConstant(null, value)</tt>. 
   */
  public void visitConstant(String value);

  public void visitEndOfTree();

}
