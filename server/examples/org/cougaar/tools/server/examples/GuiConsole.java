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
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.border.*;
import javax.swing.event.*;

import org.cougaar.tools.server.*;
import org.cougaar.tools.server.system.ProcessStatus;

/**
 * Sample GUI console for community control.
 * <p>
 * See "MinConsole" for a trimmed-down (no-UI) example.
 */
public class GuiConsole {

  /**
   * Name of the <code>CommunityServesClient</code> class.
   * <p>
   * Note that this keeps the console/community API clean!
   */
  public static final String DEFAULT_SERVER_CLASS =
    "org.cougaar.tools.server.rmi.ClientCommunityController";

  /** 
   * Name of the remote registry that contains the runtime information.
   */
  public static final String DEFAULT_SERVER_NAME = 
    "ServerHook";



  private NodePanel nodePanel;         // for one main panel
  private JPanel content;              // for main pane

  private Properties properties = null;
  private String HostFileName;
  private String ConfigFileName;

  private static DateFormat fileDateFormat = 
    new SimpleDateFormat("yyyyMMddHHmmss");

  // ------------------------------------------------------
  // class which sets up the main gui NodePanel
  // ------------------------------------------------------

  protected class NodePanel extends JPanel {

    private JTabbedPane nodePane;

    private JTextArea stdoutArea;
    private JScrollPane stdoutPane;
    private Box buttons;
    private JPanel buttons1;

    private JPanel buttons2;
    // panel for typelist & configlist
    private JPanel typePanel;
    private JList configList;
    private JList hostList;
    // add scrolling for config
    JScrollPane configscroll;

    private JButton runButton;
    private JButton flushNodeEventsButton;
    private JButton dumpThreadsButton;
    private JButton listNodeProcessesButton;
    private JButton listAllProcessesButton;
    private JButton readFileButton;
    private JButton writeFileButton;
    private JButton listFilesButton;
    private JButton stopButton;

    // node & host from gui
    private String selectedHostName;
    private String selectedNodeName;          

    // the first host to be launched.  Used to determine where the
    //   nameserver runs.
    private String firstHost;

    CommunityServesClient communitySupport;
    Map myNodes;

    public NodePanel(
        CommunityServesClient communitySupport) throws IOException {

      stdoutArea = new JTextArea();
      stdoutPane = new JScrollPane(stdoutArea);
      buttons = Box.createVerticalBox();
      buttons1 = new JPanel();
      buttons2 = new JPanel(new GridLayout(3,3));

      // panel for typelist & configlist
      typePanel = new JPanel(new BorderLayout());               
      // get configs
      configList = 
        new JList(
            getHosts(
              GuiConsole.this.ConfigFileName));
      // get hosts
      hostList = 
        new JList(
            getHosts(
              GuiConsole.this.HostFileName));              
      // add scrolling for config
      configscroll = new JScrollPane(configList);

      runButton = new JButton("Run");
      dumpThreadsButton = new JButton("Trigger-Stack-Trace");
      listNodeProcessesButton = new JButton("List-Node-Procs");
      listAllProcessesButton = new JButton("List-All-Procs");
      readFileButton = new JButton("Read-\"./dir/test.txt\"");
      writeFileButton = new JButton("Write-\"./dir/test.txt\"");
      listFilesButton = new JButton("List-Files");
      flushNodeEventsButton = new JButton("Flush-Output");
      stopButton = new JButton("Stop");


      // the first host to be launched.  Used to determine where the
      //   nameserver runs.
      firstHost = null;

      myNodes = new HashMap();


      // save the community controller
      this.communitySupport = communitySupport;

      // configure list select 
      configList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

      // handle config from gui
      configList.addListSelectionListener(
          new ListSelectionListener() {
          public void valueChanged(ListSelectionEvent e) {
          JList source = (JList)e.getSource();
          selectedNodeName = (String)source.getSelectedValue();
          }
          });

      // handle host from gui
      hostList.addListSelectionListener(
          new ListSelectionListener() {
          public void valueChanged(ListSelectionEvent e) {
          JList source = (JList)e.getSource();
          selectedHostName = (String)source.getSelectedValue();
          }
          });

      // handle run from gui by calling
      runButton.addActionListener(
          new ActionListener() {
            // createNode() which spawns remote nodes
            public void actionPerformed(ActionEvent e) {
            int port = 8484;
            if (selectedHostName == null) {
              hostList.setSelectedIndex(0);
              selectedHostName = (String) hostList.getSelectedValue();
            }
            createNode(
                selectedHostName, 
                port,
                selectedNodeName);
            }
          });

      // handle the "Flush-Output" button
      flushNodeEventsButton.addActionListener(
          new ActionListener() {    
            public void actionPerformed(ActionEvent e) {
              flushNodeEvents(selectedNodeName);
            }
          });

      // handle "Trigger-Stack-Trace" button
      dumpThreadsButton.addActionListener(
          new ActionListener() {    
            public void actionPerformed(ActionEvent e) {
              dumpThreads(selectedNodeName);
            }
          });

      // handle "List-Node-Procs" button
      listNodeProcessesButton.addActionListener(
          new ActionListener() {    
            public void actionPerformed(ActionEvent e) {
              listProcesses(selectedNodeName, false);
            }
          });

      // handle "List-All-Procs" button
      listAllProcessesButton.addActionListener(
          new ActionListener() {    
            public void actionPerformed(ActionEvent e) {
              listProcesses(selectedNodeName, true);
            }
          });


      // handle "Read-File" button
      readFileButton.addActionListener(
          new ActionListener() {    
            public void actionPerformed(ActionEvent e) {
              readFile();
            }
          });

      // handle "Write-File" button
      writeFileButton.addActionListener(
          new ActionListener() {    
            public void actionPerformed(ActionEvent e) {
              writeFile();
            }
          });

      // handle "List-File" button
      listFilesButton.addActionListener(
          new ActionListener() {    
            public void actionPerformed(ActionEvent e) {
              listFiles();
            }
          });

      // handle stop button
      stopButton.addActionListener(
          new ActionListener() {    
            // createNode() which spawns remote nodes
            public void actionPerformed(ActionEvent e) {
              stopNode(selectedNodeName);
            }
          });

      // label for NodePanel name
      JLabel titleLabel = new JLabel(ConfigFileName, JLabel.CENTER);
      titleLabel.setFont(new Font("sans", Font.BOLD, 16));
      titleLabel.setForeground(Color.black);

      // gui aesthetics & setup
      setLayout(new BorderLayout( 5, 10));
      Border blackline = BorderFactory.createLineBorder(Color.black);

      // typePanel corresponds to configList and hostList in gui
      typePanel.setLayout(new GridLayout(1, 3));                       
      typePanel.setBorder(blackline); 

      TitledBorder title1 = 
        BorderFactory.createTitledBorder(
            BorderFactory.createEmptyBorder(), "hosts");
      TitledBorder title2 = 
        BorderFactory.createTitledBorder(
            BorderFactory.createEmptyBorder(), "nodes");
      title1.setTitlePosition(TitledBorder.ABOVE_TOP);
      title2.setTitlePosition(TitledBorder.ABOVE_TOP);

      hostList.setBorder(title1);
      configList.setBorder(title2);
      configList.setVisibleRowCount(10);

      // add gui components
      typePanel.add(configscroll);
      typePanel.add(hostList);
      add(typePanel, BorderLayout.WEST);                                    
      add(titleLabel, BorderLayout.NORTH);

      // adds stdout tabs dynamically
      stdoutArea.setLineWrap(true);
      nodePane = new JTabbedPane();
      add(nodePane, BorderLayout.CENTER);

      add(buttons, BorderLayout.SOUTH);
      buttons.add(buttons1, BorderLayout.NORTH);
      buttons.add(buttons2, BorderLayout.SOUTH);    

      buttons2.add(runButton);
      buttons2.add(stopButton);
      buttons2.add(flushNodeEventsButton);

      buttons2.add(dumpThreadsButton);
      buttons2.add(listNodeProcessesButton);
      buttons2.add(listAllProcessesButton);

      buttons2.add(readFileButton);
      buttons2.add(writeFileButton);
      buttons2.add(listFilesButton);
      // empty slot
    }  

    /**
     * Get all the hostname specified in the given file.
     */
    public String[] getHosts(String afilename) {
      java.util.List hosts = new ArrayList();

      // read hosts, one per line
      try { 
        RandomAccessFile host_input =
          new RandomAccessFile(afilename, "r");      
        while (true) {
          String ihost = host_input.readLine(); // get their name       
          if (ihost == null) {
            break;
          }
          hosts.add(ihost);
        }
        host_input.close();
      } catch (IOException e) {
        System.err.println( 
            "Error during read/open from file\n" + e.toString());
        System.exit(1); 
      } 

      // convert List to a String[]
      return (String[])hosts.toArray(new String[hosts.size()]);
    }

    /**
     * Create a Node on the given host.
     */
    private void createNode(
        String hostName, 
        int controlPort,
        String nodeName) {

      // remember the first host that is spawned
      if (this.firstHost == null) {
        this.firstHost = hostName;
      }

      // add a tab and panel to the main window
      DefaultStyledDocument doc;
      try {
        // create an output pane
        doc = new DefaultStyledDocument();
        JTextPane pane = new JTextPane(doc);
        JScrollPane stdoutPane = new JScrollPane(pane);
        nodePane.add(nodeName, stdoutPane);
      } catch (Exception e) {
        return;
      }

      // add new properties specifying the configuration
      Properties c_props = new Properties();
      c_props.putAll(
          GuiConsole.this.properties);
      c_props.put("org.cougaar.node.name", nodeName);
      c_props.put(
          "org.cougaar.control.port", 
          Integer.toString(controlPort));

      String nsps = 
        GuiConsole.this.properties.getProperty(
            "org.cougaar.tools.server.nameserver.ports", 
            "8888:5555");
      c_props.put("org.cougaar.name.server", this.firstHost+":"+nsps);

      String regName = 
        GuiConsole.this.properties.getProperty(
            "org.cougaar.tools.server.name", 
            DEFAULT_SERVER_NAME);

      NodeEventListener nel;
      try {
        nel = 
          new MyListener(
              getLogFileName(nodeName), 
              doc);
      } catch (Exception e) {
        System.err.println(
            "Unable to create output for \""+nodeName+"\"");
        e.printStackTrace();
        // remove panel!
        return;
      }

      NodeEventFilter nef = 
        new NodeEventFilter(20);

      NodeServesClient newNode;
      try {
        HostServesClient hostSupport =
          communitySupport.getHost(
              hostName,
              controlPort);
        ProcessDescription desc =
          new ProcessDescription(
              nodeName, null, c_props, null);
        newNode = 
          hostSupport.createNode(
              desc,
              nel,
              nef,
              null);
      } catch (Exception e) {
        System.err.println(
            "Unable to create node \""+nodeName+"\" on host \""+hostName+"\"");
        e.printStackTrace();
        // remove panel!
        return;
      }

      myNodes.put(nodeName, newNode);
    }

    private void flushNodeEvents(String name) {
      NodeServesClient nsc = (NodeServesClient)myNodes.get(name);
      if (nsc == null) {
        System.err.println(
            "Unknown node name: "+name);
        return;
      }

      try {
        nsc.flushNodeEvents();
      } catch (Exception e) {
        System.err.println(
            "Unable to flush events");
        e.printStackTrace();
      }
    }

    private void dumpThreads(String name) {
      NodeServesClient nsc = (NodeServesClient)myNodes.get(name);
      if (nsc == null) {
        System.err.println(
            "Unknown node name: "+name);
        return;
      }

      try {
        nsc.dumpThreads();
      } catch (Exception e) {
        System.err.println(
            "Unable to trigger a stack dump for node: "+name);
        e.printStackTrace();
        return;
      }

      // replace with pretty GUI code...
      System.out.println("Triggered a stack dump for node: "+name);
    }

    private void listProcesses(String name, boolean showAll) {
      NodeServesClient nsc = (NodeServesClient)myNodes.get(name);
      if (nsc == null) {
        System.err.println(
            "Unknown node name: "+name);
        return;
      }

      ProcessStatus[] psa;
      try {
        psa = nsc.listProcesses(showAll);
      } catch (Exception e) {
        System.err.println(
            "Unable to list processes for node: "+name);
        e.printStackTrace();
        return;
      }

      // replace with pretty GUI code...
      int n = ((psa != null) ? psa.length : 0);
      System.out.println(
          ((showAll) ? "All" : "Node's")+
          " ProcessStatus["+n+"] for node "+name+":");
      for (int i = 0; i < n; i++) {
        ProcessStatus pi = psa[i];
        // show terse details
        System.out.println("  "+pi.toString(false));
      }
    }

    private void readFile() {
      // for now this test is hard-coded
      System.out.println(
          "Test -- read file \"./dir/test.txt\" from \"localhost:8484\"");
      try {
        HostServesClient hsc =
          communitySupport.getHost(
              "localhost",
              8484);
        InputStream in = hsc.open("./dir/test.txt");
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        for (int i = 0; ; ) {
          String s = r.readLine();
          if (s == null) {
            break;
          }
          System.out.println(i+"\t"+s);
        }
      } catch (Exception e) {
        System.err.println("Failed: "+e);
        e.printStackTrace();
      }
    }

    private void writeFile() {
      // for now this test is hard-coded
      System.out.println(
          "Test -- write file \"./dir/test.txt\" from \"localhost:8484\"");
      try {
        HostServesClient hsc =
          communitySupport.getHost(
              "localhost",
              8484);
        OutputStream os = hsc.write("./dir/test.txt", false);
        os.write("test foo\ncontents".getBytes());
        os.close();
      } catch (Exception e) {
        System.err.println("Failed: "+e);
        e.printStackTrace();
      }
    }

    private void listFiles() {
      // for now this test is hard-coded
      System.out.println(
          "Test -- list files in \"./dir/\" from \"localhost:8484\"");
      try {
        HostServesClient hsc =
          communitySupport.getHost(
              "localhost",
              8484);
        String[] ret = hsc.list("./dir/");
        int nret = ((ret != null) ? ret.length : 0);
        System.out.println("listing["+nret+"]:");
        for (int i = 0; i < nret; i++) {
          System.out.println(i+"\t"+ret[i]);
        }
      } catch (Exception e) {
        System.err.println("Failed: "+e);
        e.printStackTrace();
      }
    }

    private void stopNode(String name) {
      NodeServesClient nsc = (NodeServesClient)myNodes.get(name);
      if (nsc == null) {
        System.err.println(
            "Unknown node name: "+name);
        return;
      }

      try {
        // should flush first!
        nsc.flushNodeEvents();

        // now destroy
        nsc.destroy();
      } catch (Exception e) {
        System.err.println(
            "Unable to destroy node \""+name+"\"");
      }

      myNodes.remove(name);

      // remove the pane from the tabs
      int i = nodePane.indexOfTab(name);
      if (i != -1) {
        nodePane.removeTabAt(i);
      }
    }

    private String getLogFileName(String prefix) {
      return 
        prefix + 
        GuiConsole.this.fileDateFormat.format(new Date()) + 
        ".log";
    }

    public Dimension getPreferredSize() {
      return new Dimension(850, 350);
    }

    public void start() {
    }

    public void stop() {
    }


    /**
     * Listener for "pushed" Node activities.
     */
    protected class MyListener implements NodeEventListener {

      private Writer toFile;

      private StyledDocument toText;
      private SimpleAttributeSet[] atts;

      private MyListener(
          String toFileName,
          StyledDocument toText) throws IOException {

        // wrap and capture to a log
        this.toFile = 
          new BufferedWriter(
              new FileWriter(
                toFileName));

        // save the GUI output
        this.toText = toText;

        // create our attributes
        atts = new SimpleAttributeSet[4];
        atts[0] = new SimpleAttributeSet();
        StyleConstants.setForeground(atts[0], Color.black);
        atts[1] = new SimpleAttributeSet();
        StyleConstants.setForeground(atts[1], Color.red);
        atts[2] = new SimpleAttributeSet();
        StyleConstants.setForeground(atts[2], Color.green);
        atts[3] = new SimpleAttributeSet();
        StyleConstants.setForeground(atts[3], Color.blue);
      }

      public void handle(
          NodeServesClient nsc,
          NodeEvent ne) {

        // get node's name
        /*
        String nodeName;
        try {
          nodeName = nsc.getName();
        } catch (Exception e) {
          nodeName = "???";
        }
        */

        final String toUI;
        int attIndex;
        switch (ne.getType()) {
          case NodeEvent.STANDARD_OUT:
            attIndex = 0;
            toUI = ne.getMessage();
            break;
          case NodeEvent.STANDARD_ERR:
            attIndex = 1;
            toUI = ne.getMessage();
            break;
          default:
            attIndex = 2;
            toUI = ne.toString();
            break;
        }
        final SimpleAttributeSet att = atts[attIndex];
        
        try {
          toFile.write(toUI);
        } catch (Exception e) {
        }

        // 
        // bug here if "toText" is destroyed
        //

        try {
          // must use swing "invokeLater" to be thread-safe
          SwingUtilities.invokeLater(
              new Runnable() {
                public void run() {
                  try {
                    toText.insertString(
                      toText.getLength(), 
                      toUI, 
                      att);
                  } catch (Exception e) {
                  }
                }
              });
        } catch (RuntimeException e) {
        }
      }

      public void handleAll(
          final NodeServesClient nsc,
          final java.util.List l) {

        final int n = l.size();
        if (n <= 0) {
          return;
        }

        // get node's name
        /*
        String nodeName;
        try {
          nodeName = nsc.getName();
        } catch (Exception e) {
          nodeName = "???";
        }
        */

        try {
          int i = 0;
          do {
            NodeEvent ne = (NodeEvent)l.get(i);
            toFile.write(getString(ne));
          } while (++i < n);
        } catch (Exception e) {
          // file dead?
        }

        // 
        // bug here if "toText" is destroyed
        //

        try {
          // must use swing "invokeLater" to be thread-safe
          SwingUtilities.invokeLater(
              new Runnable() {
                public void run() {
                  NodeEvent n0 = (NodeEvent)l.get(0);
                  int prevType = n0.getType();
                  String prevSi = getString(n0);
                  for (int i = 1; i < n; i++) {
                    NodeEvent ni = (NodeEvent)l.get(i);
                    int type = ni.getType();
                    String si = getString(ni);
                    if (type == prevType) {
                      prevSi += si;
                    } else {
                      try {
                        toText.insertString(
                            toText.getLength(), 
                            prevSi, 
                            getStyle(prevType));
                      } catch (Exception e) {
                        break;
                      }
                      prevSi = si;
                      prevType = type;
                    }
                  }
                  try {
                    toText.insertString(
                        toText.getLength(), 
                        prevSi, 
                        getStyle(prevType));
                  } catch (Exception e) {
                  }
                }
              });
        } catch (RuntimeException e) {
        }
      }

      private final String getString(final NodeEvent ne) {
        switch (ne.getType()) {
          case NodeEvent.STANDARD_OUT:
          case NodeEvent.STANDARD_ERR:
            return ne.getMessage();
          case NodeEvent.HEARTBEAT:
            return "@";
          default:
            return ne.toString();
        }
      }

      private final SimpleAttributeSet getStyle(final int type) {
        switch (type) {
          case NodeEvent.STANDARD_OUT:
            return atts[0];
          case NodeEvent.STANDARD_ERR:
            return atts[1];
          case NodeEvent.HEARTBEAT:
            return atts[2];
          default:
            return atts[3];
        }
      }
    }

  }

  private GuiConsole() {
  }

  public Component init(
      CommunityServesClient communitySupport,
      String args[]) throws IOException {
    int l = args.length;
    if (l < 2) {
      System.err.println(
          "Usage: GuiConsole hostfile configfile [propertiesfile]");
      System.exit(1);
    }

    HostFileName = args[0];
    ConfigFileName = args[1];

    properties = new Properties();
    if (l > 2) {
      String pfile = args[2];
      if (!("-".equals(pfile))) {
        try {
          InputStream is = new FileInputStream(pfile);
          properties.load(is);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    }

    content = new JPanel(new BorderLayout());
    //content.setPreferredSize(new Dimension(800, 400));

    nodePanel = new NodePanel(communitySupport);
    content.add(nodePanel); 

    return content;
  }

  public void start() {
    nodePanel.start();
  }

  public void stop() {
    nodePanel.stop();
  }

  public static CommunityServesClient createCommunitySupport() 
  throws Exception {
    // get the classname
    String classname = DEFAULT_SERVER_CLASS;

    // load the class
    Class cl = Class.forName(classname);
    if (!(CommunityServesClient.class.isAssignableFrom(cl))) {
      throw new IllegalArgumentException(
          "Class \""+classname+"\" is not a \"CommunityServesClient\": "+
          ((cl != null) ? cl.getName() : "null"));
    }

    // create an instance
    return (CommunityServesClient)cl.newInstance();
  }

  // --------------------------------------------
  // main() creates a GuiConsole instance and fires up the gui
  // --------------------------------------------

  public static void main(String[] args) {
    try {
      // create the support hook
      CommunityServesClient communitySupport = createCommunitySupport();

      // create the console
      final GuiConsole guiconsole = new GuiConsole();
      Component component = guiconsole.init(communitySupport, args);

      // wrap in a GUI frame
      JFrame frame = new JFrame("GuiConsole");
      frame.getContentPane().add(component);
      frame.addWindowListener(
          new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
              guiconsole.stop();
              System.exit(0);
            }
          });
      frame.pack();
      frame.show();

      // start the guiconsole
      guiconsole.start();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }

}
