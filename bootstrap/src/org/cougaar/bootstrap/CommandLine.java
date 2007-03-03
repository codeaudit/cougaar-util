/*
 * <copyright>
 *
 *  Copyright 2004 BBNT Solutions, LLC
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
package org.cougaar.bootstrap;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class is used by
 * <tt>$COUGAAR_INSTALL_PATH/bin/cougaar[.bat]</tt>
 * to read the configuration XML files and print the <b>java</b>
 * command line, which is then used to launch the Cougaar node
 * process.
 * <p>
 * For usage, see the {@link #USAGE} string, or run:<pre>
 *   $COUGAAR_INSTALL_PATH/bin/cougaar --help
 * </pre>
 * The typical usage is:<pre>
 *   # <i>start node X specified in "mySociety.xml":</i>
 *   cd <i>your_config_directory</i>
 *   $COUGAAR_INSTALL_PATH/bin/cougaar mySociety.xml X
 * </pre>
 * <p>
 * This class is intended to replace the old Cougaar scripts:<br>
 * &nbsp;&nbsp;Node, Node.bat, XMLNode.bat, XSLNode, XSLNode.bat<br>
 * The behavior is backwards-compatible with XSLNode.
 * <p>
 * For example, the input files and command line:<br>
 * <i>mySociety.xml:</i><pre>
 *   &lt;?xml version='1.0'?&gt;
 *   &lt;society name='MinPing' ..&gt;
 *     &lt;node name='NodeA'&gt;
 *       &lt;vm_parameter&gt;-Dx=y&lt;/vm_parameter&gt;
 *       &lt;vm_parameter&gt;-Dfoo=$bar&lt;/vm_parameter&gt;
 *     &lt;/node&gt;
 *   &lt;/society&gt;
 * </pre>
 * <i>myRuntime.xml:</i><pre>
 *   &lt;?xml version='1.0'?&gt;
 *   &lt;vm_parameters&gt;
 *     &lt;vm_parameter&gt;-Dx=z&lt;/vm_parameter&gt;
 *     ..
 *   &lt;/vm_parameters&gt;
 * </pre>
 * <i>command line:</i><pre>
 *   $COUGAAR_INSTALL_PATH/bin/cougaar \
 *     --society mySociety.xml\
 *     --runtime myRuntime.xml\
 *     NodeA
 * </pre>
 * would print a command line output similar to:<pre>
 *   java -Dx=z -Dfoo=$bar ...
 * </pre>
 * and on Windows (based on the 'os.name' property) this would be:<pre>
 *   java -Dx=z -Dfoo=%bar% ...
 * </pre>
 * <p>
 * This parser also adds several standard Cougaar defaults, which
 * add the minimal -Ds and options required to run a Cougaar node:
 * <ul>
 *   <li>If the &lt;command&gt; is not specified in either the
 *       society_xml or optional runtime_xml, "java" is used.</li>
 *   <li>If the &lt;class&gt; is not specified in either xml,
 *      "org.cougaar.bootstrap.Bootstrapper" is used.  The
 *      following "-Ds" rules are only executed if the "Bootstrapper"
 *      class is specified:</li>
 *   <li>If zero &lt;prog_parameter&gt;s are specified,
 *       and "-Dorg.cougaar.bootstrap.application=<i>classname</i>"
 *       is not specified in either xmls or command-line arguments,
 *       then
 *       "-Dorg.cougaar.bootstrap.application=org.cougaar.core.node.Node"
 *       is used.  The following "-D" rules are only executed if
 *       the "Node" bootstrapped class is specified (plus the above
 *       "Bootstrapper" class requirement):</li>
 *   <li>If "-Dorg.cougaar.runtime.file=<i>filename</i>" is not
 *       specified, and a runtime_xml was specified on the command line, then
 *       "-Dorg.cougaar.runtime.file=<i>runtime_xml</i>
 *       is used.</li>
 *   <li>If "-Dorg.cougaar.society.file=<i>filename</i>" is not
 *       specified,
 *       "-Dorg.cougaar.society.file=<i>society_xml</i>
 *       is used.</li>
 *   <li>If "-Dorg.cougaar.node.name=<i>node_name</i> is not
 *       specified, then the &lt;node name='..'&gt; of the
 *       <b><u>first node</u></b> listed in the aplication_xml is
 *       used, otherwise the first node name listed in the runtime_xml
 *       is used.</li>
 *   <li>If the "-Dorg.cougaar.runtime.path=<i>directory</i> is not
 *       specified, then
 *       "-Dorg.cougaar.runtime.path=$COUGAAR_RUNTIME_PATH"
 *       is used.</li>
 *   <li>If the "-Dorg.cougaar.society.path=<i>directory</i> is not
 *       specified, then
 *       "-Dorg.cougaar.society.path=$COUGAAR_SOCIETY_PATH"
 *       is used.</li>
 *   <li>If the "-Dorg.cougaar.install.path=<i>directory</i> is not
 *       specified, then
 *       "-Dorg.cougaar.install.path=$COUGAAR_INSTALL_PATH"
 *       is used.</li>
 *   <li>If a "-Xbootclasspath.." is not specified, then
 *       "-Xbootclasspath/p:$COUGAAR_INSTALL_PATH/lib/javaiopatch.jar"
 *       is used.</li>
 *   <li>If
 *       "-Dorg.cougaar.core.node.InitializationComponent=<i>type</i>"
 *       is not specified,
 *       "-Dorg.cougaar.core.node.InitializationComponent=XML"
 *       is used.</li>
 *   <li>If "-Djava.class.path=<i>jars</i>" is not specified,
 *       "-Djava.class.path=$COUGAAR_INSTALL_PATH/lib/bootstrap.jar"
 *       is used.</li>
 * </ul>
 */
public final class CommandLine {

  public static final String USAGE =
    "Usage: cougaar [default...] [option...] FILE [override...] [NODE]\n"+
    "Start a Cougaar Node and Agents specified in an XML file.\n"+
    "\n"+
    "  -s, --society STRING       society XML FILE name\n"+
    "  -r, --runtime STRING       runtime XML file name, which defaults to the\n"+
    "                             society FILE\n"+
    "  -n, --nameserver STRING    equivalent to -Dorg.cougaar.name.server=STRING\n"+
    "  -d, --defaults .. \\;       default \"-D\" system properties until the \\;\n"+
    "  -o, --overrides .. \\;      override \"-D\" system properties until the \\;\n"+
    "  --node STRING              NODE name\n"+
    "  -w, --windows              Convert variables to Windows format, for example,\n"+
    "                             \"-Da=$x\" becomes \"-Da=%x%\"\n"+
    "  -h, -?, --help             print this help message\n"+
    "  -v, --verbose              also print the command line to stderr\n"+
    "  -vv, --debug               print debug output to stderr\n"+
    "  -*                         if none of the above, if in a \"--defaults\" or\n"+
    "                             the society FILE is not set, add a default\n"+
    "                             \"-D\", otherwise add an override \"-D\"\n"+
    "  *                          if none of the above, set the society FILE if\n"+
    "                              it has not been set, otherwise set the NODE if it\n"+
    "                             has not been set\n"+
    "\n"+
    "Java system properties are preferred in the following order:\n"+
    "  1) The \"--overrides\" from the command line, else\n"+
    "  2) The \"--runtime\" XML file's \"<vm_parameter>\"s, else\n"+
    "  3) The \"--society\" XML file's \"<vm_parameter>\"s, else\n"+
    "  4) The \"--defaults\" from the command line, else\n"+
    "  5) Standard Cougaar default \"-Ds\", such as:\n"+
    "       \"-Dorg.cougaar.install.path=$COUGAAR_INSTALL_PATH\"\n"+
    "       as documented in the "+CommandLine.class.getName()+" javadocs.\n"+
    "\n"+
    "Example usage:\n"+
    "\n"+
    "  # start node X specified in \"mySociety.xml\":\n"+
    "  cougaar mySociety.xml X\n"+
    "\n"+
    "  # start the first node listed in the XML:\n"+
    "  cougaar mySociety.xml\n"+
    "\n"+
    "  # read -D's from soc.xml, override with -D's from rt.xml, force -Da=b:\n"+
    "  cougaar soc.xml rt.xml -Da=b\n"+
    "\n"+
    "Report bugs to <bugs@cougaar.org>.";

  // optional -D specified *in the xml* to expand the bootstrapper
  // jar path.
  private static final String EXPAND_JAR_PATH_PROP =
    "org.cougaar.bootstrap.commandLine.expandJarPath";

  // args
  private final String[] args;

  // from args
  private boolean verbose;
  private boolean debug;
  private boolean windows;
  private String society_xml;
  private String runtime_xml;
  private String node_name;

  // from command-line args "--overrides"
  private CommandData override_data;

  // from runtime xml file (e.g. "myRuntime.xml")
  private CommandData runtime_data;

  // from society xml file (e.g. "mySociety.xml")
  private CommandData society_data;

  // from command-line args "--defaults"
  private CommandData default_data;

  /**
   * Parse an XML file and print the Java command line.
   */
  public static void main(String[] args) throws Exception {
    (new CommandLine(args)).run();
  }

  /**
   * @param args see {@link #USAGE}.
   * @return the parsed command line data structure.
   */
  public static CommandData parse(String[] args) {
    CommandLine cl = new CommandLine(args);
    if (!cl.parse_arguments()) {
      return null;
    }
    return cl.generate_command();
  }

  public CommandLine(String[] args) {
    this.args = args;
  }

  public void run() throws Exception {
    if (!parse_arguments()) {
      System.exit(-1);
    }
    CommandData command = generate_command();
    if (command == null) {
      System.exit(-1);
    }
    if (verbose) {
      System.err.println(command);
    }
    System.out.println(command);
  }

  /** @see #usage */
  private boolean parse_arguments() {
    List default_vm_parameters = null;
    List override_vm_parameters = null;
    boolean inDefaults = false;
    boolean inOverrides = false;
    for (int i = 0; i < args.length; i++) {
      String s = args[i];
      if (!s.startsWith("-")) {
        inDefaults = false;
        inOverrides = false;
        if (!s.equals(";") && !s.equals("\\;")) {
          if (society_xml == null) {
            society_xml = s;
          } else if (node_name == null) {
            node_name = s;
          } else if (runtime_xml == null) {
            runtime_xml = node_name;
            node_name = s;
          } else {
            // force usage
            society_xml = null;
            break;
          }
        }
      } else if (
          s.equals("-h") || s.equals("-?") || s.equals("--help")) {
        // force usage
        society_xml = null;
        break;
      } else if (s.equals("-v") || s.equals("--verbose")) {
        inDefaults = false;
        inOverrides = false;
        verbose = true;
      } else if (s.equals("-vv") || s.equals("--debug")) {
        inDefaults = false;
        inOverrides = false;
        debug = true;
      } else if (s.equals("-w") || s.equals("--windows")) {
        inDefaults = false;
        inOverrides = false;
        windows = true;
      } else if (s.equals("-s") || s.equals("--society")) {
        inDefaults = false;
        inOverrides = false;
        society_xml = args[++i];
      } else if (s.equals("-r") || s.equals("--runtime")) {
        inDefaults = false;
        inOverrides = false;
        runtime_xml = args[++i];
      } else if (s.equals("--node")) {
        inDefaults = false;
        inOverrides = false;
        node_name = args[++i];
      } else if (s.equals("-d") || s.equals("--defaults")) {
        inDefaults = true;
        inOverrides = false;
      } else if (s.equals("-o") || s.equals("--overrides")) {
        inDefaults = false;
        inOverrides = true;
      } else {
        if (s.equals("-n") || s.equals("--nameserver")) {
          String x = args[++i];
          s =
            "-Dorg.cougaar.name.server"+
            (x.indexOf('=') < 0 ? "=" : ".")+
            x;
        }
        if (inDefaults ||
            (!inOverrides && society_xml == null)) {
          if (default_vm_parameters == null) {
            default_vm_parameters = new ArrayList();
          }
          default_vm_parameters.add(s);
        } else {
          if (override_vm_parameters == null) {
            override_vm_parameters = new ArrayList();
          }
          override_vm_parameters.add(s);
        }
      }
    }
    if (society_xml == null) {
      System.err.println(USAGE);
      return false;
    }
    if (default_vm_parameters != null) {
      default_data = new CommandData(default_vm_parameters, null);
    }
    if (override_vm_parameters != null) {
      override_data = new CommandData(override_vm_parameters, null);
    }
    return true;
  }

  /**
   * Parse the XML file(s), mix in command-line -Ds, and return
   * the merged command line.
   */
  private CommandData generate_command() {
    CommandData ret = null;
    try {
      XMLReader xml_reader = XMLReaderUtils.createXMLReader();
      xml_reader.setEntityResolver(new MyResolver());

      // if only node_name is set then this is ambiguous, since it might be:
      //    bin/cougaar mySociety.xml myRuntime.xml
      // so we check to see if the node_name is a file, and prefer that file
      // over possible "mySociety.xml" entry for "<node name='myRuntime.xml'/>"
      if (node_name != null && runtime_xml == null &&
          (new File(node_name)).isFile()) {
        if (debug) {
         System.err.println("changing node_name to runtime_xml: "+node_name);
        }
        runtime_xml = node_name;
        node_name = null;
      }

      society_data =
        parse(xml_reader, node_name, society_xml);
      if (society_data == null ||
          !society_data.processedNode) {
        System.err.println(
            "Unable to find "+
            (node_name == null ? "any nodes" : "node "+node_name)+
            " in file "+society_xml);
        return null;
      }

      if (runtime_xml != null && !runtime_xml.equals(society_xml)) {
        String sys_node_name =
          (node_name != null ? node_name :
           society_data != null ? society_data.node :
           null);
        runtime_data = parse(xml_reader, sys_node_name, runtime_xml);
      }

      if (debug) {
        // okay to print to stderr
        System.err.println("override: "+override_data);
        System.err.println("runtime: "+runtime_data);
        System.err.println("society: "+society_data);
        System.err.println("default: "+default_data);
      }

      ret = merge_data();

      if (debug) {
        System.err.println("result: "+ret);
      }
    } catch (FileNotFoundException fnfe) {
      String s = fnfe.getMessage();
      System.err.println("File not found: "+s);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ret;
  }

  /**
   * Merge the command data from the XML files and command line
   * "--defaults" and "--overrides", returning the merged command.
   * <p>
   * The preference order is:
   * <ol>
   *   <li>--overrides "-Ds"</li>
   *   <li>society_xml</li>
   *   <li>runtime_xml</li>
   *   <li>--defaults "-Ds"</li>
   *   <li>cougaar default (if "&lt;class&gt;" == Bootstrapper)</li>
   * </ol>
   */
  private CommandData merge_data() {

    // iterate over xml/-Ds
    String command = null;
    String clazz = null;
    List prog_parameters = Collections.EMPTY_LIST;
    Map m = new LinkedHashMap();
    boolean hasBootpath = false;
    String node = node_name;
    for (int x = 0; x < 4; x++) {
      CommandData cd =
        (x == 0 ? default_data :
         x == 1 ? society_data :
         x == 2 ? runtime_data :
         x == 3 ? override_data :
         null);
      if (cd == null) {
        continue;
      }
      if (cd.command != null) {
        command = cd.command;
      }
      if (cd.clazz != null) {
        clazz = cd.clazz;
      }
      if (!cd.prog_parameters.isEmpty()) {
        prog_parameters = cd.prog_parameters;
      }
      if (cd.node != null) {
        node = cd.node;
      }
      List l = cd.vm_parameters;
      for (int i = 0, n = l.size(); i < n; i++) {
        String s = (String) l.get(i);
        if (!s.startsWith("-")) {
          throw new RuntimeException(
              "vm_parameter must start with \"-\", not "+s);
        }
        if (windows) {
          s = toWindows(s);
        }
        if (s.startsWith("-D")) {
          int j = s.indexOf('=');
          if (j < 0) {
            m.put(s, null);
          } else {
            String s1 = s.substring(0, j);
            String s2 = s.substring(j+1);
            m.put(s1, s2);
          }
        } else {
          if (s.startsWith("-Xbootclasspath")) {
            hasBootpath = true;
          }
          m.put(s, null);
        }
      }
    }

    // add cougaar defaults
    if (command == null) {
      command = "java";
    }
    if (clazz == null) {
      clazz = "org.cougaar.bootstrap.Bootstrapper";
    }
    if (clazz.equals("org.cougaar.bootstrap.Bootstrapper")) {
      String p0 =
        (m.containsKey("-Dorg.cougaar.bootstrap.application") ?
         (String) m.get("-Dorg.cougaar.bootstrap.application") :
         (prog_parameters.isEmpty() ?
          (null) :
          (String) prog_parameters.get(0)));
      if (p0 == null) {
        p0 = "org.cougaar.core.node.Node";
        m.put("-Dorg.cougaar.bootstrap.application", p0);
      }
      if ("org.cougaar.core.node.Node".equals(p0)) {
        if (!m.containsKey("-Dorg.cougaar.society.file") &&
            society_xml != null) {
          m.put("-Dorg.cougaar.society.file", society_xml);
        }
        if (!m.containsKey("-Dorg.cougaar.runtime.file") &&
            runtime_xml != null) {
          m.put("-Dorg.cougaar.runtime.file", runtime_xml);
        }
        if (!m.containsKey("-Dorg.cougaar.node.name") &&
            node != null) {
          m.put("-Dorg.cougaar.node.name", node);
        }
        String runtime_path = (String) m.get("-Dorg.cougaar.runtime.path");
        if (runtime_path == null) {
          runtime_path = "$COUGAAR_RUNTIME_PATH";
          if (windows) {
            runtime_path = toWindows(runtime_path);
          }
          m.put("-Dorg.cougaar.runtime.path", runtime_path);
        }
        String society_path = (String) m.get("-Dorg.cougaar.society.path");
        if (society_path == null) {
          society_path = "$COUGAAR_SOCIETY_PATH";
          if (windows) {
            society_path = toWindows(society_path);
          }
          m.put("-Dorg.cougaar.society.path", society_path);
        }
        String install_path = (String) m.get("-Dorg.cougaar.install.path");
        if (install_path == null) {
          install_path = "$COUGAAR_INSTALL_PATH";
          if (windows) {
            install_path = toWindows(install_path);
          }
          m.put("-Dorg.cougaar.install.path", install_path);
        }
        if (!hasBootpath) {
          m.put("-Xbootclasspath/p:"+install_path+"/lib/javaiopatch.jar", null);
        }
        if (!m.containsKey(
              "-Dorg.cougaar.core.node.InitializationComponent")) {
          m.put(
              "-Dorg.cougaar.core.node.InitializationComponent",
              "XML");
        }
        if (!m.containsKey("-Djava.class.path")) {
          m.put("-Djava.class.path", install_path+"/lib/bootstrap.jar");
        }
      }
    }

    // optionally remove bootstrapper & jar path
    if (clazz.equals("org.cougaar.bootstrap.Bootstrapper") &&
        "true".equals(m.get("-D"+EXPAND_JAR_PATH_PROP))) {
      try {
        expandJarPath(m);
        // success
        m.remove("-D"+EXPAND_JAR_PATH_PROP);
        clazz = (String) m.remove("-Dorg.cougaar.bootstrap.application");
      } catch (Exception e) {
        System.err.println(
            "Warning: Unable to remove bootstrapper, ignoring -D"+
            EXPAND_JAR_PATH_PROP);
        e.printStackTrace();
      }
    }

    // flatten map to list
    List vm_parameters = new ArrayList(m.size());
    for (Iterator iter = m.entrySet().iterator();
        iter.hasNext();
        ) {
      Map.Entry me = (Map.Entry) iter.next();
      String key = (String) me.getKey();
      String value = (String) me.getValue();
      if (value == null) {
        vm_parameters.add(key);
      } else {
        vm_parameters.add(key+"="+value);
      }
    }

    return new CommandData(
        command,
        vm_parameters,
        clazz,
        prog_parameters,
        node_name,
        true);
  }

  /**
   * Rewrite the -Ds to remove the bootstrapper, expand the jar path,
   * and resolve all system properties that contain "\${<i>name</i>}"
   * expansions (see {@link SystemProperties.expandProperties()}.
   */
  private void expandJarPath(Map m) {
    // Build a properties table containing all our map properties,
    // resolving environment variables as necessary.
    //
    // For example, suppose we have:
    //   -Dorg.cougaar.install.path=$COUGAAR_INSTALL_PATH
    // The jar finder will use this to find jars, e.g.
    //   <cip>/lib:<cip>/sys
    // Since we're bypassing the shell, we must resolve the
    // $COUGAAR_INSTALL_PATH here, otherwise the jar finder will
    // see:
    //   $COUGAAR_INSTALL_PATH/lib:$COUGAAR_INSTALL_PATH/sys
    // instead of real filesystem paths.
    //
    // Note that we only save the resolved -Ds that are set back
    // in the properties table.  The above example's -D won't be
    // saved in our "m" map, since the Bootstrapper won't call
    // "props.setProperty(..)" on it.  Currently only "\${name}"
    // properties will be copied back, due to the SystemProperties
    // "expandProperties(props)" method.
    if (debug) {
      System.err.println("rewriting command to remove bootstrapper");
    }
    Properties p = new Properties(SystemProperties.getProperties());
    for (Iterator iter = m.entrySet().iterator(); iter.hasNext(); ) {
      Map.Entry me = (Map.Entry) iter.next();
      String key = (String) me.getKey();
      if (!key.startsWith("-D")) {
        continue;
      }
      key = key.substring(2);
      String originalValue = (String) me.getValue();
      String value = 
        SystemProperties.resolveEnv(originalValue, windows);
      if (debug &&
          originalValue != null && !originalValue.equals(value)) {
        System.err.println("resolved -D"+key+"="+value);
      }
      p.setProperty(key, value);
    }
    // record any "setProperty(..)" changes
    final Map m2 = m;
    final Properties props = new Properties(p) {
      public Object put(Object key, Object value) {
        Object ret = super.put(key, value);
        if (debug) {
          System.err.println("overriding -D"+key+"="+value);
        }
        m2.put("-D"+key, value);
        return ret;
      }
    };
    // wrap the bootstrapper to use our props
    Bootstrapper b = new Bootstrapper() {
      protected String getProperty(String key) {
        if ("org.cougaar.bootstrap.excludeJars".equals(key)) {
          return "javaiopatch.jar";
        }
        return props.getProperty(key);
      }
      protected String getProperty(String key, String def) {
        return props.getProperty(key, def);
      }
      protected Properties getProperties() {
        return props;
      }
    };
    // use the bootstrapper to compute the jar url list
    b.readPropertiesFromURL(
        props.getProperty("org.cougaar.properties.url"));
    SystemProperties.expandProperties(props);
    List l = b.computeURLs();
    if (debug) {
      System.err.println("found jars["+l.size()+"]="+l);
    }
    // replace the java -classpath with the jars
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < l.size(); i++) {
      URL url = (URL) l.get(i);
      if (i > 0) {
        buf.append(File.pathSeparator);
      }
      buf.append(url);
    }
    m.put("-Djava.class.path", buf.toString());
    // remove other bootstrapper -Ds
    m.put("-Dorg.cougaar.useBootstrapper", "false");
    m.remove("-Dorg.cougaar.class.path");
    m.remove("-Dorg.cougaar.jar.path");
    // the caller should use the bootstrap.application
  }

  /** Convert "$x" to "%x%". */
  private static final String toWindows(String s) {
    int j = s.indexOf('$');
    if (j < 0) {
      return s;
    }
    int n = s.length();
    StringBuffer buf = new StringBuffer(n+4);
    int i = 0;
    while (true) {
      boolean escape = false;
      for (int k = j-1; k >= i && s.charAt(k) == '\\'; k--) {
        escape = !escape;
      }
      buf.append(s.substring(i, j));
      i = j+1;
      if (escape) {
        buf.append('$');
      } else {
        buf.append('%');
        boolean paren = (s.charAt(i) == '{');
        if (paren) {
          i++;
        }
        j = i;
        for (j = i; j < n; j++) {
          char ch = s.charAt(j);
          if (!((ch >= 'a' && ch <= 'z') ||
                (ch >= 'A' && ch <= 'Z') ||
                (ch >= '0' && ch <= '9') ||
                (ch == '_'))) {
            break;
          }
        }
        buf.append(s.substring(i, j));
        buf.append('%');
        i = j;
        if (paren && s.charAt(i) == '}') {
          i++;
        }
      }
      j = s.indexOf('$', i);
      if (j < 0) {
        if (i < n) {
          buf.append(s.substring(i));
        }
        break;
      }
    }
    return buf.toString();
  }

  /** Parse an XML file, return the contained command data */
  private CommandData parse(
      XMLReader xml_reader,
      String node,
      String filename) throws Exception {
    XMLConfigHandler handler = new XMLConfigHandler(node);
    xml_reader.setContentHandler(handler);
    try {
      xml_reader.parse(filename);
    } catch (Exception e) {
      // do backwards compatible ".ini" check
      if (e instanceof FileNotFoundException ||
          (e instanceof SAXParseException &&
           "Document root element is missing.".equals(
             e.getMessage()))) {
        CommandData iniData = parseINI(filename);
        if (iniData != null) {
          return iniData;
        }
      }
      throw e;
    }
    return handler.getCommandData();
  }

  /** backwards compatibility for INI files (bug 3881) */
  private CommandData parseINI(String filename) {
    try {
      File f = new File(filename);
      if (!f.exists()) {
        f = new File(filename+".ini");
        if (!f.exists()) {
          return null;
        }
      }
    } catch (Exception e) {
      return null;
    }
    String node = filename;
    if (node.regionMatches(
          true, (node.length() - 4), ".ini", 0, 4)) {
      node = node.substring(0, node.length() - 4);
    }
    int sep = node.lastIndexOf('/');
    if (sep >= 0) {
      node = node.substring(sep+1);
    }
    sep = node.lastIndexOf('\\');
    if (sep >= 0) {
      node = node.substring(sep+1);
    }
    List vm_parameters =
      Collections.singletonList(
          "-Dorg.cougaar.core.node.InitializationComponent=File");
    return new CommandData(
        null,
        vm_parameters,
        null,
        null,
        node,
        true);
  }

  /** Java command data, including the -Ds */
  public static final class CommandData implements Serializable {

    private final String command;
    private final List vm_parameters;
    private final String clazz;
    private final List prog_parameters;
    private final String node;
    private final boolean processedNode;

    public CommandData(
        List vm_parameters,
        String node) {
      this(null, vm_parameters, null, null, node, false);
    }

    public CommandData(
        String command,
        List vm_parameters,
        String clazz,
        List prog_parameters,
        String node,
        boolean processedNode) {
      this.command = command;
      this.vm_parameters =
        (vm_parameters == null || vm_parameters.isEmpty() ?
         Collections.EMPTY_LIST :
         Collections.unmodifiableList(vm_parameters));
      this.clazz = clazz;
      this.prog_parameters =
        (prog_parameters == null || prog_parameters.isEmpty() ?
         Collections.EMPTY_LIST :
         Collections.unmodifiableList(prog_parameters));
      this.node = node;
      this.processedNode = processedNode;
    }

    /** The command, which is usually "java" */
    public String getCommand() { return command; }

    /** The -Ds and -Xs in the order specified by the XML file */
    public List getProperties() { return vm_parameters; }

    /** The classname, which is null if "-jar" is used */
    public String getClassname() { return clazz; }

    /** The arguments after the classname */
    public List getArguments() { return prog_parameters; }

    public String toString() {
      StringBuffer buf = new StringBuffer();
      if (command != null) {
        buf.append(command);
      }
      toBuf(buf, vm_parameters);
      if (clazz != null) {
        buf.append(" ").append(clazz);
      }
      toBuf(buf, prog_parameters);
      return buf.toString();
    }
    private void toBuf(StringBuffer buf, List l) {
      int n = (l == null ? 0 : l.size());
      for (int i = 0; i < n; i++) {
        buf.append(" ").append(l.get(i));
      }
    }
  }

  /** An XML resolver API */
  private interface Resolver extends EntityResolver, URIResolver {
    InputSource resolveEntity(
        String publicId, String systemId)
      throws SAXException, IOException;
    Source resolve(String href, String base)
      throws TransformerException;
  }

  /** XML resolver that looks in the current directory */
  private static class MyResolver implements Resolver {
    public InputSource resolveEntity(
        String publicId,
        String systemId) throws SAXException, IOException {
      return new InputSource(open(systemId));
    }
    public Source resolve(
        String href,
        String base) throws TransformerException {
      try {
        return new StreamSource(open(href));
      } catch (Exception e) {
        throw new TransformerException("resolve("+href+")", e);
      }
    }
    private InputStream open(String s) throws IOException {
      return new FileInputStream(s);
    }
  }

  /**
   * SAX XML parser for the "mySociety.xml" and "myRuntime.xml".
   * <p>
   * The two XML files use the same content format.  See the
   * top-level class javadocs for example XML files.
   */
  private static class XMLConfigHandler extends DefaultHandler {

    private final String node_name;

    private final CharArrayWriter argValueBuffer = new CharArrayWriter();

    private boolean inNode;

    private boolean thisNode;
    private boolean processedNode;

    private boolean inString;

    private String command;
    private String node;
    private List vm_parameters;
    private String clazz;
    private List prog_parameters;

    public XMLConfigHandler(String node_name) {
      this.node_name = node_name;
      this.node = node_name;
    }

    public CommandData getCommandData() {
      return new CommandData(
          command,
          vm_parameters,
          clazz,
          prog_parameters,
          node,
          processedNode);
    }

    // begin element
    public void startElement(
        String namespaceURI,
        String localName,
        String qName,
        Attributes atts)
      throws SAXException {

        if (localName.equals("node") ||
            localName.equals("vm_parameters")) {
          startNode(atts);
        }

        if (inNode && !thisNode) {
          return;
        }

        if (localName.equals("command")) {
          startCommand(atts);
        } else if (localName.equals("vm_parameter")) {
          startVmParameter(atts);
        } else if (localName.equals("class")) {
          startClass(atts);
        } else if (localName.equals("prog_parameter")) {
          startProgParameter(atts);
        }
      }

    // misc characters within an element, e.g. vm_parameter data
    public void characters(char[] ch, int start, int length)
      throws SAXException {
        if (inString) {
          // inside string tag (e.g. vm_parameter), so save characters
          argValueBuffer.write(ch, start, length);
        }
      }

    // end element
    public void endElement(
        String namespaceURI, String localName, String qName
        ) throws SAXException {
      if (localName.equals("node") ||
         localName.equals("vm_parameters")) {
        endNode();
        return;
      }

      if (inNode && !thisNode) {
        return;
      }

      if (localName.equals("command")) {
        endCommand();
      } else if (localName.equals("vm_parameter")) {
        endVmParameter();
      } else if (localName.equals("prog_parameter")) {
        endProgParameter();
      } else if (localName.equals("class")) {
        endClass();
      } else {
        // ignore
      }
    }

    // our element handlers:

    private void startNode(Attributes atts) {
      inNode = true;
      if (processedNode) {
        return;
      }
      String name = atts.getValue("name");
      boolean anyName = (name == null || name.equals("*"));
      boolean anyNode = (node_name == null || node_name.equals("*"));
      thisNode =
        (anyName ||
         anyNode ||
         node_name.equals(name));
      if (thisNode) {
        node =
          (anyName ?
           (anyNode ? null : node_name) :
           name);
        processedNode = true;
      }
    }

    private void endNode() {
      inNode = false;
      thisNode = false;
    }

    private void startString() {
      if (inString) {
        throw new RuntimeException(
            "Already have a string value buffer? "+argValueBuffer);
      }
      inString = true;
    }
    private String endString() {
      if (!inString) {
        throw new RuntimeException("Not in a string tag?");
      }
      inString = false;
      String s = argValueBuffer.toString().trim();
      argValueBuffer.reset();
      return s;
    }

    private void startCommand(Attributes atts) {
      startString();
    }
    private void endCommand() {
      command = endString();
    }

    private void startVmParameter(Attributes atts) {
      startString();
    }
    private void endVmParameter() {
      String s = endString();
      if (vm_parameters == null) {
        vm_parameters = new ArrayList();
      }
      vm_parameters.add(s);
    }

    private void startClass(Attributes atts) {
      startString();
    }
    private void endClass() {
      clazz = endString();
    }

    private void startProgParameter(Attributes atts) {
      startString();
    }
    private void endProgParameter() {
      if (prog_parameters == null) {
        prog_parameters = new ArrayList();
      }
      prog_parameters.add(endString());
    }

  }
}
