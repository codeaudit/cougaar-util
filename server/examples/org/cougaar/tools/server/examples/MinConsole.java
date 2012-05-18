/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
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

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.cougaar.tools.server.DualStreamBuffer;
import org.cougaar.tools.server.OutputBundle;
import org.cougaar.tools.server.OutputListener;
import org.cougaar.tools.server.OutputPolicy;
import org.cougaar.tools.server.ProcessDescription;
import org.cougaar.tools.server.RemoteHost;
import org.cougaar.tools.server.RemoteHostRegistry;
import org.cougaar.tools.server.RemoteListenableConfig;
import org.cougaar.tools.server.RemoteProcess;
import org.cougaar.tools.server.system.ProcessStatus;

/**
 * Minimal "console" for using to test the server.
 * <p>
 * Can optionally pass these command-line arguments to
 * <tt>main(String[])</tt>:<ul>
 *   <li>killMillis=LONG   (default=20000)</li>
 *   <li>hostName=STRING   (default=localhost)</li>
 *   <li>controlPort=INT   (default=8484)</li>
 *   <li>nodeName=STRING   (default=MiniNode)</li>
 *   <li>namingAddr=STRING (default=localhost:8888)</li>
 * </ul>
 */
public class MinConsole {

  public static void main(String[] args) throws Exception {

    long killMillis = 20*1000;

    String hostName = "localhost";
    int controlPort = 8484;
    String nodeName = "MiniNode";
    String namingAddr = hostName+":8888";

    for (int i = 0; i < args.length; i++) {
      String ai = args[i];
      if (ai.startsWith("killMillis=")) {
        String tmp = ai.substring("killMillis=".length());
        killMillis = Long.parseLong(tmp);
      } else if (ai.startsWith("hostName=")) {
        hostName = ai.substring("hostName=".length());
      } else if (ai.startsWith("controlPort=")) {
        String tmp = ai.substring("controlPort=".length());
        controlPort = Integer.parseInt(tmp);
      } else if (ai.startsWith("nodeName=")) {
        nodeName = ai.substring("nodeName=".length());
      } else if (ai.startsWith("namingAddr=")) {
        namingAddr = ai.substring("namingAddr=".length());
      } else {
        throw new IllegalArgumentException(
            "Illegal arg["+i+"]: "+ai);
      }
    }

    String procId = nodeName;
    
    System.out.println(
        MinConsole.class.getName()+" {"+
        "\n  killMillis: "+killMillis+
        "\n  hostName: "+hostName+
        "\n  controlPort: "+controlPort+
        "\n  nodeName: "+nodeName+
        "\n  namingAddr: "+namingAddr+
        "\n  procId: "+procId+
        "\n}");

    // create a remote-host-registry instance
    RemoteHostRegistry hostReg = 
      RemoteHostRegistry.getInstance();

    // contact the host
    RemoteHost rhost = 
      hostReg.lookupRemoteHost(hostName, controlPort, true);

    // just to test, let's try a "ping()"
    ping(rhost);

    // list the running processes on the server
    listProcs(rhost);

    // define the process
    ProcessDescription desc = 
      createProcessDescription(procId, nodeName, namingAddr);

    // launch process
    RemoteProcess proc = createProcess(rhost, desc);

    // wait a couple seconds
    Thread.sleep(2*1000);

    // list the processes on the server
    listProcs(rhost);

    // let it run for a while
    System.out.println(
        "Created process \""+procId+"\", run "+
        ((killMillis <= 0) ? 
         "forever" : 
         "for "+killMillis+"milliseconds")+
        "\n"+
        "***************************************************");
    // list running processes ("ps")
    listProcessStatus(proc, false);

    if (killMillis > 0) {
      Thread.sleep(killMillis);

      // kill the process
      System.out.println(
          "****************************************************\n"+
          "Kill process \""+procId+"\"");
      rhost.killRemoteProcess(desc.getName());

      // list the processes on the server
      listProcs(rhost);

      Thread.sleep(2*1000);

      // re-launch process
      System.out.println(
          "****************************************************\n"+
          "Re-launch process \""+procId+"\"");
         createProcess(rhost, desc);

      System.out.println(
          "****************************************************\n"+
          "Run for 5 seconds");
      Thread.sleep(5*1000);

      System.out.println("exit, force server-side cleanup.");

      // force exit -- RMI-threads would keep this running
      System.exit(0);
    }
  }

  private static void ping(
      RemoteHost rhost) throws Exception {
    long t1 = System.currentTimeMillis();
    long t2 = rhost.ping();
    long t3 = System.currentTimeMillis();
    System.out.println(
        "Ping (millis): "+
        "\n  sent: "+t1+
        "\n  ret:  "+t2+
        "\n  recv: "+t3+
        "\n  (recv - sent): "+(t3 - t1));
  }

  private static void listProcs(
      RemoteHost rhost) throws Exception {
    // list the running processes on the server
    List runningProcs = 
      rhost.listProcessDescriptions();
    int nRunningProcs = 
      ((runningProcs != null) ? runningProcs.size() : 0);
    System.out.println(
        "Running processes["+nRunningProcs+"]: ");
    for (int i = 0; i < nRunningProcs; i++) {
      ProcessDescription pdi = 
        (ProcessDescription) runningProcs.get(i);
      System.out.println(
          "  ["+i+" / "+nRunningProcs+"]: "+pdi);
    }
  }

  private static ProcessDescription createProcessDescription(
      String procId,
      String nodeName,
      String namingAddr) throws Exception {
    // build a configuration
    Properties javaProps = new Properties();
    javaProps.put("org.cougaar.node.name", nodeName);
    javaProps.put("org.cougaar.name.server", namingAddr);
    return
      new ProcessDescription(
          procId,
          "group-for-"+procId,
          javaProps,
          null);
  }

  private static RemoteProcess createProcess(
      RemoteHost rhost,
      final ProcessDescription desc) throws Exception {
    // create an output listener
    OutputListener ol = 
      new OutputListener() {
        public void handleOutputBundle(
            OutputBundle ob) {
          // just write std-out and std-err
          DualStreamBuffer dsb = ob.getDualStreamBuffer();
          try {
            dsb.writeTo(System.out, System.err);
          } catch (IOException ioe) {
            ioe.printStackTrace();
          }
        }
      };

    // create the output filter/buffer config
    OutputPolicy op = new OutputPolicy(20);

    // create a listener config
    RemoteListenableConfig rlc = 
      new RemoteListenableConfig(ol, op);

    // create the node
    try {
      return rhost.createRemoteProcess(desc, rlc);
    } catch (Exception e) {
      System.err.println("Unable to create process:");
      e.printStackTrace();
      throw e;
    }
  }

  private static void listProcessStatus(
      RemoteProcess proc,
      boolean showAll) throws Exception {
    // query
    ProcessStatus[] psa = proc.listProcesses(showAll);
    // display
    int n = ((psa != null) ? psa.length : 0);
    System.out.println(
        ((showAll) ? "All" : "Process's")+
        " ProcessStatus["+n+"] for Process \""+
        proc.getProcessDescription().getName()+"\":");
    for (int i = 0; i < n; i++) {
      ProcessStatus pi = psa[i];
      // show terse details
      System.out.println("  "+pi.toString(false));
    }
  }
}
