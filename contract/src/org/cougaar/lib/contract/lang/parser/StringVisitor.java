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

import org.cougaar.lib.contract.lang.*;

/**
 * A <code>TreeVisitor</code> with "pretty-printer" capability.
 */
public interface StringVisitor extends TreeVisitor {

  public boolean isPrettyPrint();

  public void setPrettyPrint(boolean pretty);

}
