/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.tools.server;

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

import org.cougaar.core.cluster.ClusterIdentifier;

/**
 * Sample GUI console for community control.
 * <p>
 * This is fairly rough, but 
 * <p>
 * @see CommunityServesClient for a definition of "community"
 */
public class Console {

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
    private JButton listClustersButton;
    private JButton getFileButton;
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

      buttons2 = new JPanel();
      // panel for typelist & configlist
      typePanel = new JPanel(new BorderLayout());               
      // get configs
      configList = 
        new JList(
            getHosts(
              Console.this.ConfigFileName));
      // get hosts
      hostList = 
        new JList(
            getHosts(
              Console.this.HostFileName));              
      // add scrolling for config
      configscroll = new JScrollPane(configList);

      runButton = new JButton("Run");
      listClustersButton = new JButton("List-Clusters");
      getFileButton = new JButton("Get-File");
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
            String[] additionalArgs = new String[4];
            additionalArgs[0] = "-f";
            additionalArgs[1] = selectedNodeName+".ini";
            additionalArgs[2] = "-controlPort";
            additionalArgs[3] = Integer.toString(port);
            createNode(
                selectedHostName, 
                port,
                selectedNodeName, 
                ConfigFileName, 
                additionalArgs);
            }
          });

      // handle the "Flush-Output" button
      flushNodeEventsButton.addActionListener(
          new ActionListener() {    
            public void actionPerformed(ActionEvent e) {
              flushNodeEvents(selectedNodeName);
            }
          });

      // handle "List-Clusters" button
      listClustersButton.addActionListener(
          new ActionListener() {    
            public void actionPerformed(ActionEvent e) {
              listClusters(selectedNodeName);
            }
          });

      // handle "Get-File" button
      getFileButton.addActionListener(
          new ActionListener() {    
            public void actionPerformed(ActionEvent e) {
              getFile();
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
      buttons2.add(flushNodeEventsButton);
      buttons2.add(listClustersButton);
      buttons2.add(getFileButton);
      buttons2.add(listFilesButton);
      buttons2.add(stopButton);
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
        String hostname, 
        int port,
        String name, 
        String configname, 
        String[] args) {

      // remember the first host that is spawned
      if (this.firstHost == null) {
        this.firstHost = hostname;
      }

      // add a tab and panel to the main window
      DefaultStyledDocument doc;
      try {
        // create an output pane
        doc = new DefaultStyledDocument();
        JTextPane pane = new JTextPane(doc);
        JScrollPane stdoutPane = new JScrollPane(pane);
        nodePane.add(name, stdoutPane);
      } catch (Exception e) {
        return;
      }

      // add new properties specifying the configuration
      Properties c_props = new Properties();
      c_props.putAll(
          Console.this.properties);
      c_props.put("org.cougaar.node.name", name);
      c_props.put("org.cougaar.config", configname);
      String nsps = 
        Console.this.properties.getProperty(
            "org.cougaar.tools.server.nameserver.ports", 
            "8888:5555");
      c_props.put("org.cougaar.name.server", this.firstHost+":"+nsps);

      String regName = 
        Console.this.properties.getProperty(
            "org.cougaar.tools.server.name", 
            DEFAULT_SERVER_NAME);

      NodeEventListener nel;
      try {
        nel = 
          new MyListener(
              getLogFileName(name), 
              doc);
      } catch (Exception e) {
        System.err.println(
            "Unable to create output for \""+name+"\"");
        e.printStackTrace();
        // remove panel!
        return;
      }

      NodeEventFilter nef = 
        new NodeEventFilter(20);

      NodeServesClient newNode;
      try {
        newNode = 
          communitySupport.createNode(
              hostname, 
              port, 
              regName,
              name,
              c_props,
              args,
              nel,
              nef,
              null);
      } catch (Exception e) {
        System.err.println(
            "Unable to create node \""+name+"\" on host \""+hostname+"\"");
        e.printStackTrace();
        // remove panel!
        return;
      }

      myNodes.put(name, newNode);
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

    private void listClusters(String name) {
      NodeServesClient nsc = (NodeServesClient)myNodes.get(name);
      if (nsc == null) {
        System.err.println(
            "Unknown node name: "+name);
        return;
      }

      boolean isRegistered;
      try {
        isRegistered = nsc.isRegistered();
      } catch (Exception e) {
        isRegistered = false;
      }
      if (!(isRegistered)) {
        // replace with pretty GUI code...
        System.out.println(
            "Can only list clusters once the Node has registered");
        return;
      }

      java.util.List l;
      try {
        l = nsc.getClusterIdentifiers();
      } catch (Exception e) {
        System.err.println(
            "Unable to query \""+name+"\" for cluster identifiers");
        e.printStackTrace();
        l = null;
      }

      int n = ((l != null) ? l.size() : 0);

      // replace with pretty GUI code...
      System.out.println("clusters["+n+"]: "+l);
    }

    private void getFile() {
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
        Console.this.fileDateFormat.format(new Date()) + 
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

  private Console() {
  }

  public Component init(
      CommunityServesClient communitySupport,
      String args[]) throws IOException {
    int l = args.length;
    if (l < 2) {
      System.err.println(
          "Usage: Console hostfile configfile [propertiesfile]");
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
  // main() creates a Console instance and fires up the gui
  // --------------------------------------------

  public static void main(String[] args) {
    try {
      // create the support hook
      CommunityServesClient communitySupport = createCommunitySupport();

      // create the console
      final Console console = new Console();
      Component component = console.init(communitySupport, args);

      // wrap in a GUI frame
      JFrame frame = new JFrame("Console");
      frame.getContentPane().add(component);
      frame.addWindowListener(
          new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
              console.stop();
              System.exit(0);
            }
          });
      frame.pack();
      frame.show();

      // start the console
      console.start();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }

}
