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
package org.cougaar.tools.server.system.other;

import org.cougaar.tools.server.system.JavaThreadDumper;
import org.cougaar.tools.server.system.ProcessKiller;
import org.cougaar.tools.server.system.ProcessLauncher;
import org.cougaar.tools.server.system.ProcessStatusReader;
import org.cougaar.tools.server.system.SystemAccessFactory;

/**
 * Stub <code>SystemAccessFactory<code> for Operating Systems
 * that are either:<pre>
 *   - unknown
 * or
 *   - don't support process-aware commands (Windows, etc)
 * </pre>
 */
public class NullSystemAccessFactory 
extends SystemAccessFactory {

  private static final NullSystemAccessFactory SINGLETON =
    new NullSystemAccessFactory();

  private final NullProcessLauncher npl;

  private NullSystemAccessFactory() {
    // see "getNullInstance()"
    npl = new NullProcessLauncher();
  }

  public static NullSystemAccessFactory getNullInstance() {
    return SINGLETON;
  }

  public ProcessLauncher createProcessLauncher() {
    return npl;
  }

  public JavaThreadDumper createJavaThreadDumper() {
    return null;
  }

  public ProcessStatusReader createProcessStatusReader() {
    return null;
  }

  public ProcessKiller createProcessKiller() {
    return null;
  }

}
