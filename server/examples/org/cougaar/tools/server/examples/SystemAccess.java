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
package org.cougaar.tools.server.examples;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.cougaar.tools.server.system.InvokeUtility;
import org.cougaar.tools.server.system.JavaThreadDumper;
import org.cougaar.tools.server.system.ProcessLauncher;
import org.cougaar.tools.server.system.ProcessStatus;
import org.cougaar.tools.server.system.ProcessStatusReader;
import org.cougaar.tools.server.system.SystemAccessFactory;

/**
 * Example usage of the "system" utilities.
 * <p>
 * The output is OS- and runtime- specific, so this is not
 * appropriate for automated regression testing.
 *
 * @see SystemAccessTest
 */
public class SystemAccess {

  private static final String DEFAULT_COMMAND = 
    "java"+
    " -classpath "+
    System.getProperty("java.class.path")+
    " org.cougaar.tools.server.examples.SystemAccessTest";

  public static void main(String[] args) throws Exception {

    String cmd;
    if (args.length > 0) {
      cmd = "";
      for (int i = 0; i < args.length; i++) {
        String ai = args[i];
        //
        // FIXME: what about whitespaces?
        //
        cmd += ai;
        if (i < (args.length - 1)) {
          cmd += " ";
        }
      }
    } else {
      cmd = DEFAULT_COMMAND;
    }
    System.out.println("Child command: "+cmd);

    System.out.println("Start child process");
    System.out.println("***********************************");
    long pid = startChild(cmd);
    System.out.println("***********************************");
    System.out.println("Child PID: "+pid);

    System.out.println("List child processes");
    System.out.println("***********************************");
    ProcessStatus[] psa = listProcesses(false);
    // mark our child's "pid"
    markProcesses(psa, pid);
    System.out.println("***********************************");
    printProcesses(psa);

    System.out.println("List all processes");
    System.out.println("***********************************");
    ProcessStatus[] psa2 = listProcesses(true);
    // mark our child's "pid"
    markProcesses(psa2, pid);
    System.out.println("***********************************");
    printProcesses(psa2);

    System.out.println("Dump the stack for "+pid);
    System.out.println("***********************************");
    dumpStack(pid);
    System.out.println("***********************************");

    System.out.println("Tests completed");
    System.exit(-1);
  }

  private static final long startChild(String cmd) throws Exception {
    // get OS-specific factory
    SystemAccessFactory saf = SystemAccessFactory.getInstance();

    // create process-launcher
    ProcessLauncher pl = saf.createProcessLauncher();

    // get the modified command line
    String[] newCmd = pl.getCommandLine(cmd);

    // start the process
    Process proc = 
      Runtime.getRuntime().exec(newCmd);

    // read the process id
    InputStream procIn = proc.getInputStream();
    long pid = pl.parseProcessIdentifier(procIn);

    // let some other threads watch the output
    OutputWatcher ow = 
      new OutputWatcher(procIn, "StdOut: ");
    OutputWatcher ew = 
      new OutputWatcher(proc.getErrorStream(), "StdErr: ");
    (new Thread(ow)).start();
    (new Thread(ew)).start();

    // should do "proc.waitFor()", but we're just testing...

    return pid;
  }

  private static final boolean dumpStack(long pid) throws Exception {

    // get OS-specific factory
    SystemAccessFactory saf = SystemAccessFactory.getInstance();

    // create Thread-Dumper
    JavaThreadDumper jtd = saf.createJavaThreadDumper();
    if (jtd == null) {
      throw new UnsupportedOperationException(
          "Java Thread-Dump not available");
    }

    // get the command line
    String[] cmd = jtd.getCommandLine(pid);

    // invoke the command
    InputStream in = InvokeUtility.invokeCommand(cmd);

    // parse the response
    boolean b = jtd.parseResponse(in);

    return b;
  }

  private static final ProcessStatus[] listProcesses(
      boolean showAll) throws Exception {

    // get OS-specific factory
    SystemAccessFactory saf = SystemAccessFactory.getInstance();

    // create process-statys reader
    ProcessStatusReader psr = saf.createProcessStatusReader();
    if (psr == null) {
      throw new UnsupportedOperationException(
          "Process status listing not available");
    }

    // get the command line
    String[] cmd = psr.getCommandLine(showAll);

    // invoke the command
    InputStream in = InvokeUtility.invokeCommand(cmd);

    // parse the response
    ProcessStatus[] ret = psr.parseResponse(in);

    return ret;
  }

  private static final void printProcesses(
      ProcessStatus[] psa) {
    System.out.println("ProcessStatus["+psa.length+"]:");
    for (int i = 0; i < psa.length; i++) {
      System.out.println(psa[i]);
    }
  }

  private static void markProcesses(
      ProcessStatus[] psa,
      long pid) {
    for (int i = 0; i < psa.length; i++) {
      ProcessStatus pi = psa[i];
      if (pi.getProcessIdentifier() == pid) {
        pi.mark();
        break;
      }
    }
  }

  private static class OutputWatcher implements Runnable {
    private final Reader in;
    private final String pre;

    public OutputWatcher(InputStream in, String pre) {
      this.in = new InputStreamReader(in);
      this.pre = ((pre != null) ? pre : "");
    }

    public void run() {
      try {
        char[] cbuf = new char[1024];
        while (true) {
          int len = in.read(cbuf);
          if (len <= 0) {
            return;  // End of file or error
          }
          System.out.println(pre+(new String(cbuf, 0, len)));
        }
      } catch (Exception e) {
      }
    }
  }
}
