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
