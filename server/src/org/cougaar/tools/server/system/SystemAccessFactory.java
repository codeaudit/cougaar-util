/*
 * <copyright>
 *  Copyright 2003 BBNT Solutions, LLC
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

package org.cougaar.tools.server.system;

import org.cougaar.tools.server.system.linux.LinuxSystemAccessFactory;
import org.cougaar.tools.server.system.other.NullSystemAccessFactory;

/**
 * Operating-System aware factory for creating the various system 
 * access utilities (<code>JavaThreadDumper</code>, etc).
 */
public abstract class SystemAccessFactory {

  private static final int OS_OTHER = 0;
  private static final int OS_LINUX = 1;

  private static final int OS_TYPE;
  static {
    // figure out the OS type
    int type;
    String osName = System.getProperty("os.name");
    if ("Linux".equals(osName)) {
      type = OS_LINUX;
    } else {
      type = OS_OTHER;
    }
    OS_TYPE = type;
  }

  /**
   * Get an instance of the appropriate Operating-System specific
   * <code>SystemAccessFactory</code>.
   * <p>
   * Subclasses of <code>SystemAccessFactory</code> "inherit"
   * this static method, so they must define a similar
   * (but differently named) "static get*Instance()" method.
   */
  public static SystemAccessFactory getInstance() {
    if (OS_TYPE == OS_LINUX) {
      return LinuxSystemAccessFactory.getLinuxInstance();
    } else {
      return NullSystemAccessFactory.getNullInstance();
    }
  }

  /**
   * Create a <code>ProcessLauncher</code> for this
   * Operating System.
   */
  public abstract ProcessLauncher createProcessLauncher();

  /**
   * Create a <code>JavaThreadDumper</code> for this
   * Operating System, or <tt>null</tt> if the
   * service is not supported.
   */
  public abstract JavaThreadDumper createJavaThreadDumper();

  /**
   * Create a <code>ProcessStatusReader</code> for this
   * Operating System, or <tt>null</tt> if the
   * service is not supported.
   */
  public abstract ProcessStatusReader createProcessStatusReader();

  /**
   * Create a <code>ProcessKiller</code> for this
   * Operating System, or <tt>null</tt> if the
   * service is not supported.
   */
  public abstract ProcessKiller createProcessKiller();

}
