/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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
 
package org.cougaar.tools.server.examples;

import java.util.*;
import java.io.*;

import org.cougaar.tools.server.*;
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

  public static final String DEFAULT_SERVER_CLASS =
    "org.cougaar.tools.server.rmi.ClientCommunityController";

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

    // create a community-support instance
    CommunityServesClient communitySupport =
      contactCommunity();

    // contact the host
    HostServesClient hostSupport = 
      communitySupport.getHost(hostName, controlPort);

    // just to test, let's try a "ping()"
    ping(hostSupport);

    // list the running processes on the server
    listProcs(hostSupport);

    // define the process
    ProcessDescription desc = 
      createProcessDescription(procId, nodeName, namingAddr);

    // launch process
    NodeServesClient proc = createProcess(hostSupport, desc);

    // wait a couple seconds
    Thread.sleep(2*1000);

    // list the processes on the server
    listProcs(hostSupport);

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
      hostSupport.killNode(desc.getName());

      // list the processes on the server
      listProcs(hostSupport);

      Thread.sleep(2*1000);

      // re-launch process
      System.out.println(
          "****************************************************\n"+
          "Re-launch process \""+procId+"\"");
      NodeServesClient proc2 = createProcess(hostSupport, desc);

      System.out.println(
          "****************************************************\n"+
          "Run for 5 seconds");
      Thread.sleep(5*1000);

      System.out.println("exit, force server-side cleanup.");

      // force exit -- RMI-threads would keep this running
      System.exit(0);
    }
  }

  private static CommunityServesClient contactCommunity(
      ) throws Exception {
    String classname = DEFAULT_SERVER_CLASS;
    Class cl = Class.forName(classname);
    if (!(CommunityServesClient.class.isAssignableFrom(cl))) {
      throw new IllegalArgumentException(
          "Class \""+classname+"\" is not a \"CommunityServesClient\": "+
          ((cl != null) ? cl.getName() : "null"));
    }
    return (CommunityServesClient) cl.newInstance();
  }

  private static void ping(
      HostServesClient hostSupport) throws Exception {
    long t1 = System.currentTimeMillis();
    long t2 = hostSupport.ping();
    long t3 = System.currentTimeMillis();
    System.out.println(
        "Ping (millis): "+
        "\n  sent: "+t1+
        "\n  ret:  "+t2+
        "\n  recv: "+t3+
        "\n  (recv - sent): "+(t3 - t1));
  }

  private static void listProcs(
      HostServesClient hostSupport) throws Exception {
    // list the running processes on the server
    List runningProcs = 
      hostSupport.listProcessDescriptions();
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

  private static NodeServesClient createProcess(
      HostServesClient hostSupport,
      final ProcessDescription desc) throws Exception {
    // create an output listener
    NodeEventListener nel = 
      new NodeEventListener() {
        public void handle(
            NodeServesClient nsc,
            NodeEvent ne) {
          System.out.println(ne);
        }
        public void handleAll(
            final NodeServesClient nsc,
            final java.util.List l) {
          int n = ((l != null) ? l.size() : 0);
          for (int i = 0; i < n; i++) {
            NodeEvent nei = (NodeEvent) l.get(i);
            System.out.println(nei);
          }
        }
      };

    // create the output filter/buffer config
    NodeEventFilter nef = 
      new NodeEventFilter(20);

    // create the node
    return hostSupport.createNode(desc, nel, nef, null);
  }

  private static void listProcessStatus(
      NodeServesClient proc,
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
