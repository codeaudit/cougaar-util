/*
 * <copyright>
 *  Copyright 1999-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
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
