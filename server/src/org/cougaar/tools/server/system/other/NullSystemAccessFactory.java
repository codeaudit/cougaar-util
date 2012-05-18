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

  @Override
public ProcessLauncher createProcessLauncher() {
    return npl;
  }

  @Override
public JavaThreadDumper createJavaThreadDumper() {
    return null;
  }

  @Override
public ProcessStatusReader createProcessStatusReader() {
    return null;
  }

  @Override
public ProcessKiller createProcessKiller() {
    return null;
  }

}
