package org.cougaar.tools.server.examples;

import java.io.*;
import java.util.*;

import org.cougaar.tools.server.system.*;

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

  private static void unmarkProcesses(
      ProcessStatus[] psa) {
    for (int i = 0; i < psa.length; i++) {
      ProcessStatus pi = psa[i];
      pi.unmark();
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
