/*
 * <copyright>
 *  Copyright 1999-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.tools.server;

import java.util.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import org.cougaar.tools.server.*;
import java.rmi.*;
import java.rmi.registry.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.border.*;
import javax.swing.event.*;

public class Console {

  Properties properties = null;

  final static DateFormat fileDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

  // ------------------------------------------------------
  // class which sets up the main gui NodePanel
  // ------------------------------------------------------
 
  class NodePanel extends JPanel {
    // private RunnerBase runner;
    private String nodeName;
    private String hostName;
    
    private JTextArea stdoutArea = new JTextArea();
    private JScrollPane stdoutPane = new JScrollPane(stdoutArea);
    private Box buttons = Box.createVerticalBox();
    private JPanel buttons1 = new JPanel();
   
    private JPanel buttons2 = new JPanel();
    // panel for typelist & configlist
    private JPanel typePanel = new JPanel(new BorderLayout());               
    // get configs
    private JList configList = new JList(getHosts(ConfigFileName));
    // get hosts
    private JList hostList = new JList(getHosts(HostFileName));              
    // add scrolling for config
    JScrollPane configscroll = new JScrollPane(configList);
  
    
    private JButton runButton = new JButton("Run");
    private JButton stopButton = new JButton("Stop");
    private JLabel agentPassword = new JLabel("");
    // node & host from gui
    private String host_name, node_name;          
    
    // the first host to be launched.  Used to determine where the
    // nameserver runs.
    private String firstHost = null;

    NodePanel(String nodeName) throws IOException {     
      
      configList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      
      // handle config from gui
      configList.addListSelectionListener(new ListSelectionListener() {
          public void valueChanged(ListSelectionEvent e) {
            JList source = (JList)e.getSource();
            node_name = (String)source.getSelectedValue();
	  }
        });

      // handle host from gui
      hostList.addListSelectionListener(new ListSelectionListener() {
          public void valueChanged(ListSelectionEvent e) {
            JList source = (JList)e.getSource();
            host_name = (String)source.getSelectedValue();
          }
        });
      
      // handle run from gui by calling
      runButton.addActionListener(new ActionListener() {    
          // PassConfig() which spawns remote nodes
	  public void actionPerformed(ActionEvent e) {
            int port = 8484;
            int number = 1;
            if (host_name == null) {
              hostList.setSelectedIndex(0);
              host_name = (String) hostList.getSelectedValue();
            }
            PassConfig(node_name, ConfigFileName, host_name, additionalArgs, port, 1);
          }
        });

      stopButton.addActionListener(new ActionListener() {    
          // PassConfig() which spawns remote nodes
	  public void actionPerformed(ActionEvent e) {
            stopNode(node_name);
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
      typePanel.setLayout( new GridLayout( 1, 3) );                       
      typePanel.setBorder(blackline); 
      
      TitledBorder title1 = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "hosts");
      TitledBorder title2 = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "nodes");
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
    }  


    // file i/o to check host names 
    
    public String[] getHosts(String afilename) {
            
      RandomAccessFile host_input;
      String[] diff_hosts = new String[30];
      String ihost = new String();
      int hostIndex = 0;           
            
      try { 
	host_input = new RandomAccessFile(afilename, "r");	
      	try {
	  while (true) {
	    ihost = host_input.readLine();			// get their name       
	    
	    if (ihost == null) {
		host_input.close();
		return diff_hosts;
	      }
	    
	    diff_hosts[hostIndex] = ihost;
	    hostIndex +=1;
	  }
	} catch (EOFException eof) {
          host_input.close();
        }
      } catch (IOException e) {
    	System.err.println( "Error during read/open from file\n" + e.toString() );
    	System.exit(1); 
      } 

      return diff_hosts;      
    }
    
    
    //------------------------------------------------------------------
    // code that runs the spawns the nodes
    //------------------------------------------------------------------

    
    private void PassConfig(String name, String configname, String hostname, String args[], int port, int numnodes)
    {
	
      RemoteProcess p;      // reference to remote process on app server
      RemoteNodeServer ra;   // reference to remote implementation
	
      if (firstHost == null) firstHost=hostname;

      //add new properties specifying which configuration
      Properties c_props = new Properties();
      c_props.putAll(properties);
      
      c_props.put("org.cougaar.node.name", name);
      c_props.put("org.cougaar.config", configname);
	
      String nsps = properties.getProperty("org.cougaar.tools.server.nameserver.ports", "8888:5555");
      c_props.put("org.cougaar.name.server", firstHost+":"+nsps);

      //obtain remote reference
      try {
        // add a tab and panel to the main window
        //JTextArea stdoutArea = new JTextArea();
        DefaultStyledDocument doc = new DefaultStyledDocument();
        final JTextPane pane = new JTextPane(doc);
        JScrollPane stdoutPane = new JScrollPane(pane);
        nodePane.add(name, stdoutPane);
	
        SimpleAttributeSet outAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(outAttr, Color.black);
        SimpleAttributeSet errAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(errAttr, Color.red);


        Writer ow = new DocumentWriter(doc,outAttr);
        Writer ew = new DocumentWriter(doc,errAttr);

        if (true) {
          Writer fileWriter = new BufferedWriter(new FileWriter(getLogFileName(name)));
          ow = new CompoundWriter(fileWriter, ow);
          ew = new CompoundWriter(fileWriter, ew);
        }
        RemoteOutputStream out = new RemoteWriterImpl(ow);
        RemoteOutputStream err = new RemoteWriterImpl(ew);
	  
        //locate registry at <hostname, port>
        //System.out.println("looking up registry");
        Registry reg = LocateRegistry.getRegistry(hostname, port);
        //System.out.println("got registry");
        String regname = properties.getProperty("org.cougaar.tools.server.name", 
                                                NodeServer.DEFAULT_NAME);
        ra = (RemoteNodeServer)reg.lookup(regname);

        p = (RemoteProcess)ra.createNode("ALPNode", c_props, args, out, err);
        addNode(name,p);
      }
      catch (Exception e){}
    }

    private void stopNode(String name) {
      RemoteProcess node = getNode(name);
      if (node != null) {
        // kill the node
        try {
          node.destroy();
        } catch (Exception ex) {
          ex.printStackTrace();
        }

        // remove it from the list
        removeNode(name);

        // remove the pane from the tabs
        {
          int i = nodePane.indexOfTab(name);
          if (i != -1) {
            nodePane.removeTabAt(i);
          }
        }
      }
    }

    private HashMap nodes = new HashMap();
    private void addNode(String name, RemoteProcess node) {
      synchronized (nodes) {
        nodes.put(name, node);
      }
    }
    private void removeNode(String name) {
      synchronized (nodes) {
        nodes.remove(name);
      }
    }

    private RemoteProcess getNode(String name) {
      synchronized (nodes) {
        return (RemoteProcess) nodes.get(name);
      }
    }

    private String getLogFileName(String prefix) {
      return prefix + fileDateFormat.format(new Date()) + ".log";
    }
    
    public Dimension getPreferredSize() {
      return new Dimension(850, 350);
    }
  
    
    public void start() {
    }
  
    public void stop() {
    }
    
    
  }
   
  private NodePanel[] nodePanels;         // for one main panel
  private JPanel content;                 // for main pane
  private JTabbedPane nodePane;
  private String HostFileName, ConfigFileName;
  private String[] additionalArgs = null;

  // -------------------------------------
  // init() which makes a new pane
  // -------------------------------------
    
  private Console() {
  }

  public Component init(String args[]) throws IOException {
    properties = new Properties();

    int l = args.length;
    if (l<2) {
      System.err.println("Usage: Console hostfile configfile [propertiesfile]");
      System.exit(1);
    }

    HostFileName = args[0];
    ConfigFileName = args[1];
    if (l>2) {
      String pfile = args[2];
      if (pfile != null && (!(pfile.equals("-")))) {
        try {
          InputStream is = new FileInputStream(pfile);
          properties.load(is);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    }

    content = new JPanel();
    //content.setPreferredSize(new Dimension(800, 400));
    return content;
    
  }

  // ------------------------------------------------------
  // setFirstPanel() sets original pane with one main panel
  // ------------------------------------------------------
  
  public void setFirstPanel(String nodeName) throws IOException
  {
    nodePanels = new NodePanel[1];
    nodePanels[0] = new NodePanel(nodeName);       // first arg on the command line
     
    content.add(nodeName, nodePanels[0]); 
  }
  
  public void start() {
    for (int i = 0; i < nodePanels.length; i++) {
      nodePanels[i].start();
    }
  }
  
  public void stop() {
    for (int i = 0; i < nodePanels.length; i++) {
      nodePanels[i].stop();
    }
  }
  
  // --------------------------------------------
  // main() creates a Console instance and fires up the gui
  // --------------------------------------------

  public static void main(String[] args) {
    try {
      final Console console = new Console();
      Component component = console.init(args);
      console.setFirstPanel(args[0]);
      JFrame frame = new JFrame("Console");
      frame.getContentPane().add(component);
      frame.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent e) {
            console.stop();
            System.exit(0);
          }
        });
            
      frame.pack();
      frame.show();
      console.start();
    }
    catch (Exception e) {
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
    
    public synchronized void write(char[] buf, int off, int len) throws IOException {
      ensureOpen();
      final String insertion = new String(buf, off, len);
      try {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              try {
                text.insertString(text.getLength(), insertion, att);
              } catch (Exception e) {}
            }
          });
      }
      catch (RuntimeException e) {
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
