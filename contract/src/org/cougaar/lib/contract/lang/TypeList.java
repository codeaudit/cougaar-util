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

import java.util.*;

/**
 * Holds a list of <code>Type</code> assertions about the current Object, such
 * as {"is:List", "is:Not:ArrayList", etc}.
 */
public interface TypeList extends Cloneable {

  public static final boolean ALWAYS_ADD_TO_FRONT = false;

  public Object clone();

  public Type getWanted();
  public List getKnownTypes();

  public static final int ADD_IGNORED =  0;
  public static final int ADD_USED =     1;
  public static final int ADD_CONFLICT = 2;
  public static final int ADD_ERROR    = 3;

  /** @return an "ADD_" int **/
  public int add(Type type);
}
