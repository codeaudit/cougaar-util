/*
 * <copyright>
 *  
 *  Copyright 2003-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */
package org.cougaar.tools.server.system.linux;

import org.cougaar.tools.server.system.JavaThreadDumper;
import org.cougaar.tools.server.system.ProcessKiller;
import org.cougaar.tools.server.system.ProcessLauncher;
import org.cougaar.tools.server.system.ProcessStatusReader;
import org.cougaar.tools.server.system.SystemAccessFactory;

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
