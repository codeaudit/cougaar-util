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
    private JButton listClustersButton;
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

      // handle list-clusters button
      listClustersButton.addActionListener(
          new ActionListener() {    
            public void actionPerformed(ActionEvent e) {
              listClusters(selectedNodeName);
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
      setLayout(new BorderLayout( 5, 10 ));
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
      buttons2.add(listClustersButton);
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
      Writer toOut;
      Writer toErr;
      Writer toListen;
      try {
        // create an output pane
        DefaultStyledDocument doc = new DefaultStyledDocument();
        JTextPane pane = new JTextPane(doc);
        JScrollPane stdoutPane = new JScrollPane(pane);
        nodePane.add(name, stdoutPane);
        SimpleAttributeSet outAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(outAttr, Color.black);
        SimpleAttributeSet errAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(errAttr, Color.red);
        SimpleAttributeSet listenAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(listenAttr, Color.blue);

        // create output streams to the GUI
        toOut = new DocumentWriter(doc, outAttr);
        toErr = new DocumentWriter(doc, errAttr);
        toListen = new DocumentWriter(doc, listenAttr);

        // wrap and capture to a log
        Writer fileWriter = 
          new BufferedWriter(
              new FileWriter(
                getLogFileName(name)));
        toOut = new CompoundWriter(fileWriter, toOut);
        toErr = new CompoundWriter(fileWriter, toErr);
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
              new MyListener(toListen),
              toOut,
              toErr);
      } catch (Exception e) {
        System.err.println(
            "Unable to create node \""+name+"\" on host \""+hostname+"\"");
        e.printStackTrace();
        return;
      }

      myNodes.put(name, newNode);
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

    private void stopNode(String name) {
      NodeServesClient nsc = (NodeServesClient)myNodes.get(name);
      if (nsc == null) {
        System.err.println(
            "Unknown node name: "+name);
        return;
      }

      try {
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
    protected class MyListener implements NodeActionListener {

      private Writer w;

      private MyListener(Writer w) {
        this.w = w;
      }

      /**
       * Simple "marker" for future UI work -- can easily replace with 
       * code that updates the GUI.
       */
      private void debug(String s) {
        try {
          w.write(s);
        } catch (Exception e) {
        }
      }

      public void handleNodeCreated(
          NodeServesClient nsc) {
        // could add this to a list of active nodes
        String nodeName;
        try {
          nodeName = nsc.getName();
        } catch (Exception e) {
          nodeName = "???";
        }
        debug("Created node: "+nodeName+"\n");
      }

      public void handleNodeDestroyed(
          NodeServesClient nsc) {
        // could remove this node from the list of active nodes
        String nodeName;
        try {
          nodeName = nsc.getName();
        } catch (Exception e) {
          nodeName = "???";
        }
        // frame probably already removed, but attempt to write anyways
        debug("Destroyed node: "+nodeName+"\n");
      }

      public void handleClusterAdd(
          NodeServesClient nsc,
          ClusterIdentifier clusterId) {
        // could remove this node from the list of active nodes
        String nodeName;
        try {
          nodeName = nsc.getName();
        } catch (Exception e) {
          nodeName = "???";
        }
        debug("Cluster added (node: "+nodeName+") clusterId: "+clusterId+"\n");
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

  //
  // Supporting classes
  //

  static class DocumentWriter extends Writer {
    private StyledDocument text;
    private SimpleAttributeSet att;

    DocumentWriter(StyledDocument text, SimpleAttributeSet att) {
      this.text = text;
      this.att = att;
    }


    private void ensureOpen() throws IOException {
      if (text == null) {
        throw new IOException("Writer closed");
      }
    }

    public synchronized void write(
        char[] buf, int off, int len) throws IOException {
      ensureOpen();
      final String insertion = new String(buf, off, len);
      try {
        // must use swing "invokeLater" to be thread-safe
        SwingUtilities.invokeLater(
            new Runnable() {
              public void run() {
                try {
                  text.insertString(text.getLength(), insertion, att);
                } catch (Exception e) {
                }
              }
            });
      } catch (RuntimeException e) {
        throw new IOException(e.getMessage());
      }
    }

    public synchronized void flush() throws IOException {
      ensureOpen();
    }

    public synchronized void close() throws IOException {
      ensureOpen();
      text = null;
    }
  }


  static class CompoundWriter extends Writer {
    private Writer w1,w2;

    public CompoundWriter(Writer w1, Writer w2) {
      this.w1 = w1;
      this.w2 = w2;
    }

    public void write(char[] buf, int off, int len) throws IOException {
      w1.write(buf,off,len);
      w1.flush();
      w2.write(buf,off,len);
      w2.flush();
    }

    public void flush() throws IOException {
      w1.flush();
      w2.flush();
    }

    public synchronized void close() throws IOException {
      w1.close();
      w2.close();
    }
  }
}
