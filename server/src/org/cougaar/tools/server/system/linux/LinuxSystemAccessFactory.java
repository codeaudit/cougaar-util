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
package org.cougaar.tools.server.system.linux;

import org.cougaar.tools.server.system.SystemAccessFactory;
import org.cougaar.tools.server.system.ProcessLauncher;
import org.cougaar.tools.server.system.JavaThreadDumper;
import org.cougaar.tools.server.system.ProcessStatusReader;
import org.cougaar.tools.server.system.ProcessKiller;

/**
 * Linux-specific factory for system access.
 */
public class LinuxSystemAccessFactory 
extends SystemAccessFactory {

  private static final LinuxSystemAccessFactory SINGLETON =
    new LinuxSystemAccessFactory();

  private final LinuxProcessLauncher lpl;
  private final LinuxJavaThreadDumper ljtd;
  private final LinuxProcessStatusReader lpsr;
  private final LinuxProcessKiller lpk;

  /**
   * Obtain an instance of the Linux-specific factory.
   */
  public static LinuxSystemAccessFactory getLinuxInstance() {
    return SINGLETON;
  }

  /**
   * @see #getLinuxInstance()
   */
  private LinuxSystemAccessFactory() {
    // assert "os.name" == Linux?
    //
    // may need to check system configuration here!
    //   (e.g. "where is 'ps' installed?", etc)

    // for now these are singleton utilities -- in the
    //   future we may need to create an instance per
    //   use.
    lpl = new LinuxProcessLauncher();
    ljtd = new LinuxJavaThreadDumper();
    lpsr = new LinuxProcessStatusReader();
    lpk = new LinuxProcessKiller();
  }

  public ProcessLauncher createProcessLauncher() {
    return lpl;
  }

  public JavaThreadDumper createJavaThreadDumper() {
    return ljtd;
  }

  public ProcessStatusReader createProcessStatusReader() {
    return lpsr;
  }

  public ProcessKiller createProcessKiller() {
    return lpk;
  }

}
