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

import org.cougaar.core.util.Operator;

/**
 * An <code>org.cougaar.core.util.Operator</code> with additional methods for parsing.
 * <p>
 * @see org.cougaar.core.util.Operator
 */
public interface Op extends Operator {

  /** 
   * The instruction ID -- unique by Class, not instance 
   */
  public int getID();

  /**
   * Various methods needed for parsing.
   */
  public Op parse(final OpParser p) throws ParseException;
  public boolean isReturnBoolean();
  public Class getReturnClass();

  /**
   * Accept tree visitors.
   */
  public void accept(TreeVisitor visitor);
}
