package org.cougaar.bootstrap;

import java.io.CharArrayWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * This class is used by
 * <tt>$COUGAAR_INSTALL_PATH/bin/Cougaar[.bat]</tt>
 * to read the configuration XML files and print the <b>java</b>
 * command line, which is then used to launch the Cougaar node
 * process.
 * <p>
 * For usage, see the {@link $usage} string, or run:<pre>
 *   $COUGAAR_INSTALL_PATH/bin/Cougaar --help
 * </pre>
 * The typical usage is:<pre>
 *   # <i>start node X specified in "mySociety.xml":</i> 
 *   cd <i>your_config_directory</i> 
 *   $COUGAAR_INSTALL_PATH/bin/Cougaar mySociety.xml X
 * </pre>
 * <p>
 * This class is intended to replace the old Cougaar scripts:<br>
 * &nbsp;&nbsp;Node, Node.bat, XMLNode.bat, XSLNode, XSLNode.bat<br>
 * The behavior is backwards-compatible with XSLNode.
 * <p>
 * For example, the input files and command line:<br>
 * <i>my_system.xml:</i><pre>
 *   &lt;?xml version='1.0'?&gt;
 *   &lt;vm_parameters&gt;
 *     &lt;vm_parameter&gt;-Dx=y&lt;/vm_parameter&gt;
 *     &lt;vm_parameter&gt;-Dfoo=$bar&lt;/vm_parameter&gt;
 *     ..
 *   &lt;/vm_parameters&gt;
 * </pre>
 * <i>my_application.xml:</i><pre>
 *   &lt;?xml version='1.0'?&gt;
 *   &lt;society name='MinPing' ..&gt;
 *     &lt;node name='NodeA'&gt;
 *       &lt;vm_parameter&gt;-Dx=z&lt;/vm_parameter&gt;
 *     &lt;/node&gt;
 *   &lt;/society&gt;
 * </pre> 
 * <i>command line:</i><pre>
 *   $COUGAAR_INSTALL_PATH/bin/Cougaar \
 *     --system my_system.xml\
 *     --application my_application.xml\
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
 *       application_xml or optional system_xml, "java" is used.</li>
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
 *   <li>If "-Dorg.cougaar.society.file=<i>filename</i>" is not
 *       specified,
 *       "-Dorg.cougaar.society.file=<i>application_xml</i>
 *       is used.</li>
 *   <li>If "-Dorg.cougaar.node.name=<i>node_name</i> is not
 *       specified, then the &lt;node name='..'&gt; of the
 *       <b><u>first node</u></b> listed in the aplication_xml is
 *       used, otherwise the first node name listed in the system_xml
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
    "Usage: Cougaar [default...] [option...] FILE [override...] [NODE]\n"+
    "Read XML files to print a Java command line, suitable for starting Cougaar.\n"+
    "\n"+
    "  -a, --application STRING   application XML FILE name\n"+
    "  -s, --system STRING        system XML file name, which defaults to the\n"+
    "                             application FILE\n"+
    "  -n, --nameserver STRING    equivalent to -Dorg.cougaar.name.server=STRING\n"+
    "  -d, --defaults .. \\;       default \"-D\" system properties until the \\;\n"+
    "  -o, --overrides .. \\;      override \"-D\" system properties until the \\;\n"+
    "  --node STRING              NODE name\n"+
    "  -w, --windows              Convert variables to Windows format, for example,\n"+
    "                             \"-Da=$x\" becomes \"-Da=%x%\"\n"+
    "  -h, --help                 print this help message\n"+
    "  -v, --verbose              also print the command line to stderr\n"+
    "  -vv, --debug               print debug output to stderr\n"+
    "  -*                         if none of the above, if in a \"--defaults\" or\n"+
    "                             the application FILE is not set, add a default\n"+
    "                             \"-D\", otherwise add an override \"-D\"\n"+
    "  *                          if none of the above, set the application FILE if\n"+
    "                              it has not been set, otherwise set the NODE if it\n"+
    "                             has not been set\n"+
    "\n"+
    "Java system properties are preferred in the following order:\n"+
    "  1) The \"--overrides\" from the command line, else\n"+
    "  2) The \"--application\" XML file's \"<vm_parameter>\"s, else\n"+
    "  3) The \"--defaults\" from the command line, else\n"+
    "  4) The \"--system\" XML file's \"<vm_parameter>\"s, else\n"+
    "  5) Standard Cougaar default \"-Ds\", such as:\n"+
    "       \"-Dorg.cougaar.install.path=$COUGAAR_INSTALL_PATH\"\n"+
    "       as documented in the "+CommandLine.class.getName()+" javadocs.\n"+
    "\n"+
    "Example usage:\n"+
    "\n"+
    "  # start node X specified in \"mySociety.xml\":\n"+
    "  Cougaar mySociety.xml X\n"+
    "\n"+
    "  # start the first node listed in the XML:\n"+
    "  Cougaar mySociety.xml\n"+
    "\n"+
    "  # read \"-D\"s from sys.xml, override -D's from mySoc.xml, force -Da=b:\n"+
    "  Cougaar -s sys.xml -a mySoc.xml -Da=b\n"+
    "\n"+
    "Report bugs to <bugs@cougaar.org>.";

  // args
  private final String[] args;

  // from args
  private boolean verbose;
  private boolean debug;
  private boolean windows;
  private String system_xml;
  private String application_xml;
  private String node_name;

  // from system xml file (e.g. "system.xml")
  private CommandData system_data;

  // from command-line args "--defaults"
  private CommandData default_data;

  // from application xml file (e.g. "mySociety.xml")
  private CommandData application_data;
  
  // from command-line args "--overrides"
  private CommandData override_data;

  public static void main(String[] args) throws Exception {
    (new CommandLine(args)).run();
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
          if (application_xml == null) {
            application_xml = s;
          } else if (node_name == null) {
            node_name = s;
          } else {
            // force usage
            application_xml = null;
            break;
          }
        }
      } else if (s.equals("-h") || s.equals("--help")) {
        // force usage
        application_xml = null;
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
      } else if (s.equals("-a") || s.equals("--application")) {
        inDefaults = false;
        inOverrides = false;
        application_xml = args[++i];
      } else if (s.equals("-s") || s.equals("--system")) {
        inDefaults = false;
        inOverrides = false;
        system_xml = args[++i];
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
            (!inOverrides && application_xml == null)) {
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
    if (application_xml == null) {
      System.err.println(USAGE);
      return false;
    }
    if (default_vm_parameters != null) {
      default_data = 
        new CommandData(default_vm_parameters, node_name);
    }
    if (override_vm_parameters != null) {
      override_data = 
        new CommandData(override_vm_parameters, node_name);
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
      XMLReader xml_reader = XMLReaderFactory.createXMLReader(
          "org.apache.crimson.parser.XMLReaderImpl");
      xml_reader.setEntityResolver(new MyResolver());

      application_data = 
        parse(xml_reader, node_name, application_xml);
      if (application_data == null ||
          !application_data.processedNode) {
        System.err.println(
            "Unable to find "+
            (node_name == null ? "any nodes" : "node "+node_name)+
            " in file "+application_xml);
        System.exit(-1);
      }

      if (system_xml != null && !system_xml.equals(application_xml)) {
        String sys_node_name = 
          (node_name != null ? node_name :
           application_data != null ? application_data.node :
           null);
        system_data = parse(xml_reader, sys_node_name, system_xml);
      }

      if (debug) {
        // okay to print to stderr
        System.err.println("system: "+system_data);
        System.err.println("default: "+default_data);
        System.err.println("application: "+application_data);
        System.err.println("override: "+override_data);
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
   *   <li>application.xml</li>
   *   <li>--defaults "-Ds"</li>
   *   <li>system.xml</li>
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
        (x == 0 ? system_data :
         x == 1 ? default_data :
         x == 2 ? application_data :
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
            application_xml != null) {
          m.put("-Dorg.cougaar.society.file", application_xml);
        }
        if (!m.containsKey("-Dorg.cougaar.node.name") &&
            node != null) {
          m.put("-Dorg.cougaar.node.name", node);
        }
        String cip = (String) m.get("-Dorg.cougaar.install.path");
        if (cip == null) {
          cip = "$COUGAAR_INSTALL_PATH";
          if (windows) {
            cip = toWindows(cip);
          }
          m.put("-Dorg.cougaar.install.path", cip);
        }
        if (!hasBootpath) {
          m.put("-Xbootclasspath/p:"+cip+"/lib/javaiopatch.jar", null);
        }
        if (!m.containsKey(
              "-Dorg.cougaar.core.node.InitializationComponent")) {
          m.put(
              "-Dorg.cougaar.core.node.InitializationComponent",
              "XML");
        }
        if (!m.containsKey("-Djava.class.path")) {
          m.put("-Djava.class.path", cip+"/lib/bootstrap.jar");
        }
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
      buf.append(s.substring(i, j));
      buf.append('%');
      i = j+1;
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

  /**
   * $INSTALL signifies file:<org.cougaar.install.path>
   * $CONFIG signifies <org.cougaar.config>
   * $CWD signifies <user.dir>
   * $HOME signifies <user.home>
   * $MOD signifies the name of a Cougaar module - a sub-directory of $INSTALL
  {
    String s = System.getProperty("org.cougaar.command.path");
    if (s == null) {
      s = "$CWD;$INSTALL/bin;$INSTALL/configs/common";
    }
    String[] els = s.trim().split("\\s*;\\s*");
    for (int i = 0; i<els.length; i++) {
      if (els.equals("$CWD")) {
      } else if (els.equals("$INSTALL")) {
      }
    }
  }
   */

  /** Parse an XML file, return the contained command data */
  private CommandData parse(
      XMLReader xml_reader,
      String node,
      String filename) throws Exception {
    XMLConfigHandler handler = new XMLConfigHandler(node);
    xml_reader.setContentHandler(handler);
    xml_reader.parse(filename);
    return handler.getCommandData();
  }

  /** Java command data, including the -Ds */
  private static class CommandData {

    public final String command;
    public final List vm_parameters;
    public final String clazz;
    public final List prog_parameters;
    public final String node;
    public final boolean processedNode;

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
        (vm_parameters == null ?
         Collections.EMPTY_LIST :
         vm_parameters);
      this.clazz = clazz;
      this.prog_parameters = 
        (prog_parameters == null ?
         Collections.EMPTY_LIST :
         prog_parameters);
      this.node = node;
      this.processedNode = processedNode;
    }

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
   * SAX XML parser for the "mySociety.xml" and "system.xml".
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
