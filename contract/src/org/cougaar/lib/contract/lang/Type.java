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
 * Holds [Not]Class Type information, for example "is:Not:List".
 */
public interface Type {

  public boolean isNot();

  /** Get the Class for the type -- use "Clazz" to avoid "Object.getClass" **/
  public Class getClazz();

  public boolean implies(final Type xtype);
  public boolean implies(final boolean xnot, final Class xcl);
  public boolean impliedBy(final Type xtype);
  public boolean impliedBy(final boolean xnot, final Class xcl);

  public String toString(boolean verbose);
}
