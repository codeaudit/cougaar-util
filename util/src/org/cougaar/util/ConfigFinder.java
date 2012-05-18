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
package org.cougaar.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.cougaar.bootstrap.SystemProperties;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * ConfigFinder provides utilitites to search for a named file in
 * several specified locations, returning the first location where a
 * file by that name is found.
 * <p>
 * Files are found and opened by the open() method. open() tries to
 * find the file using each of the elements of org.cougaar.config.path. The
 * elements of org.cougaar.config.path are separated by semicolons and
 * interpreted as URLs. The URLs in org.cougaar.config.path are interpreted
 * relative to the directory specified by org.cougaar.install.path. Several
 * special tokens may appear in these URLs:
 * <pre>
 *   $RUNTIME signifies &lt;org.cougaar.runtime.path&gt;
 *   $SOCIETY signifies &lt;org.cougaar.society.path&gt;
 *   $INSTALL signifies &lt;org.cougaar.install.path&gt;
 *   $CONFIG signifies &lt;org.cougaar.config&gt;
 *   $CWD signifies &lt;user.dir&gt;
 *   $HOME signifies &lt;user.home&gt;
 *   $MOD signifies the name of a Cougaar module - a sub-directory of $INSTALL
 * </pre>
 * The default value for org.cougaar.config.path is defined in the static
 * variable DEFAULT_CONFIG_PATH:
 *   $CWD;\
 *   $RUNTIME/configs/$CONFIG;\
 *   $RUNTIME/configs/common;\
 *   $SOCIETY/configs/$CONFIG;\
 *   $SOCIETY/configs/common;\
 *   $INSTALL/configs/$CONFIG;\
 *   $INSTALL/configs/common
 * <p>
 * If a value is specified for org.cougaar.config.path that ends with a
 * semicolon, the above default is appended to the specified
 * value. The URLs in org.cougaar.config.path are interpreted relative to
 * $INSTALL. URLs may be absolute in which case some or all of the
 * base URL may be ignored.
 * <p>
 * By default, $MOD is not set. However, when an object requests
 * a ConfigFinder, it may specify a String value for $MOD. If specified,
 * the search path used is augmented, adding 9 directories to the start
 * of the search path:
 * <ul>
 *   <li>$RUNTIME/$MOD/configs/$CONFIG</li>
 *   <li>$RUNTIME/$MOD/configs</li>
 *   <li>$RUNTIME/$MOD/data/$CONFIG</li>
 *   <li>$RUNTIME/$MOD/data</li>
 *   <li>$SOCIETY/$MOD/configs/$CONFIG</li>
 *   <li>$SOCIETY/$MOD/configs</li>
 *   <li>$SOCIETY/$MOD/data/$CONFIG</li>
 *   <li>$SOCIETY/$MOD/data</li>
 *   <li>$INSTALL/$MOD/configs/$CONFIG</li>
 *   <li>$INSTALL/$MOD/configs</li>
 *   <li>$INSTALL/$MOD/data/$CONFIG</li>
 *   <li>$INSTALL/$MOD/data</li>
 * </ul>
 * <br>
 *
 * Enable INFO level logging on org.cougaar.core.util.ConfigFinder to turn on
 * additional information on usage of ConfigFinder.
 * @property org.cougaar.install.path Used as the base path for config file finding.
 * @property org.cougaar.config.path The search path for config files.  See the class
 * documentation for details.
 * @property org.cougaar.core.util.ConfigFinder.verbose When set to <em>true</em>, report
 * progress while finding each config file.
 * @property org.cougaar.config The configuration being run, for example minitestconfig or small-135.
 * Setting this property means that CIP/configs/<value of this property> will be searched before configs/common 
 * @property org.cougaar.util.ConfigFinder.ClassName The class to use instead of ConfigFinder, presumably an
 * extension.
 **/
public class ConfigFinder {
  private List configPath;
  private final Map properties; // initialized by all constructors

  /** Cache of the String to URL mappings found.
   * @note that access should be synchronized on the cache itself.
   **/
  protected final HashMap urlCache = new HashMap(89);

  // logger support
  private Logger logger = null; // use getLogger to access
  protected final synchronized Logger getLogger() {
    if (logger == null) {
      logger = Logging.getLogger(ConfigFinder.class);
    }
    return logger;
  }

  /** 
   * Alias for ConfigFinder(null, null, null)
   **/
  public ConfigFinder() {
    this(null, null, null);
  }

  /** 
   * Alias for ConfigFinder(null, path, null)
   **/
  public ConfigFinder(String configpath) {
    this(null, configpath, null);
  }

  /**
   * Alias for ConfigFinder(module, configpath, null)
   **/
  public ConfigFinder(String module, String configpath) {
    this(module, configpath, null);
  }

  /** 
   * Alias for ConfigFinder(null, configpath, props)
   **/
  public ConfigFinder(String configpath, Map props) {
    this(null, configpath, props);
  }

  /**
   * Construct a ConfigFinder that will first search within
   * the specified module, and then in the directories on the 
   * given search path, using the given Property substitutions.<br>
   *
   * When searching the given module, we search the following 8
   * directories (if defined) before any other directories:
   * <ul>
   *   <li>$RUNTIME/$module/configs/$CONFIG</li>
   *   <li>$RUNTIME/$module/configs</li>
   *   <li>$RUNTIME/$module/data/$CONFIG</li>
   *   <li>$RUNTIME/$module/data</li>
   *   <li>$SOCIETY/$module/configs/$CONFIG</li>
   *   <li>$SOCIETY/$module/configs</li>
   *   <li>$SOCIETY/$module/data/$CONFIG</li>
   *   <li>$SOCIETY/$module/data</li>
   *   <li>$INSTALL/$module/configs/$CONFIG</li>
   *   <li>$INSTALL/$module/configs</li>
   *   <li>$INSTALL/$module/data/$CONFIG</li>
   *   <li>$INSTALL/$module/data</li>
   * </ul>
   *
   * @param module name of the module to use for module-specific configs.  If null, 
   * no module-specific paths are added.
   * @param configpath configuration path string.  If null, defaults to Configuration.getConfigPath();
   * @param props properties to use for configpath variable substitutions.
   **/
  public ConfigFinder(String module, String configpath, Map props) {
    String s = configpath;
    if (s == null) s = Configuration.getConfigPath();
    if (props == null) props = Configuration.getDefaultProperties();

    if (getLogger().isDebugEnabled()) {
      getLogger().debug("ConfigFinder class: " + this.getClass().getName());
    }

    properties = new HashMap(89);
    if (props != null) properties.putAll(props);

    if (s == null) {
      s = Configuration.getConfigPath();
    } else {
      s = s.replace('\\', '/'); // Make sure its a URL and not a file path
      // append the default if we end with a ';'
      if (s.endsWith(";")) s += Configuration.getConfigPath();
    }

    List v = new ArrayList();

    // add module paths
    if (module != null) {
      properties.put("MOD", module);
      properties.put("MODULE", module);
      // tack on to the front of the search path
      for (int i = 0; i < 3; i++) {
        String base = (i == 0 ? "RUNTIME" : i == 1 ? "SOCIETY" : "INSTALL");
        if (!properties.containsKey(base)) continue;
        boolean has_config = properties.containsKey("CONFIG");
        if (has_config) {
          v.add("$"+base+"/$MOD/configs/$CONFIG");
        }
        v.add("$"+base+"/$MOD/configs");
        if (has_config) {
          v.add("$"+base+"/$MOD/data/$CONFIG");
        }
        v.add("$"+base+"/$MOD/data");
      }
    }

    // split the specified path up
    if (s != null) {
      String[] els = s.trim().split("\\s*;\\s*");
      for (int i = 0; i<els.length; i++) {
        v.add(els[i]);
      }
    }

    // resolve and remove duplicates
    List expanded = new ArrayList(v.size());
    List resolved = new ArrayList(v.size());
    for (int i = 0; i < v.size(); i++) {
      String vi = (String) v.get(i);
      String ei = Configuration.substituteProperties(vi, properties);
      if (expanded.contains(ei)) {
        continue;
      }
      URL ui;
      try {
        ui = Configuration.urlify(ei);
      } catch (MalformedURLException mue) {
        getLogger().error("Bad ConfigPath element \""+vi+"\" -> \""+ei+"\"", mue);
        continue;
      }
      resolved.add(ui);
    }

    // save
    configPath = Collections.unmodifiableList(resolved);

    if (getLogger().isInfoEnabled()) {
      String tmp = configPath.toString();
      getLogger().info("ConfigPath = "+tmp.substring(1, tmp.length()-1));
    }
  }

  /** get the config path as an unmodifiable List of URL instances
   * which describes, in order, the set of base locations searched by
   * this instance of the ConfigFinder.
   * Contrast with Configuration.getConfigPath() which returns the
   * vm's default path.
   **/
  public List getConfigPath() { return configPath; }


  /** Do variable expansion/substitution on the argument.
   * Essentially calls Configuration.substituteProperties(s, myproperties);
   **/
  protected final String substituteProperties(String s) {
    return Configuration.substituteProperties(s, properties);
  }

  /**
   * Locate an actual file in the config path. This will skip over
   * elements of org.cougaar.config.path that are not file: urls.
   **/
  public File locateFile(String aFilename) {
    synchronized (urlCache) {
      URL u = (URL) urlCache.get(aFilename);
      if (u != null) {
        return new File(u.getFile());
      }
    }

    for (int i = 0 ; i < configPath.size() ; i++) {
      URL base = (URL) configPath.get(i);
      try {
        URL url = newURL(base, aFilename);
        if (url == null) continue;
        File result = new File(url.getFile());
        if (!result.exists()) continue;
        traceLog(aFilename, base);
        synchronized (urlCache) {
          urlCache.put(aFilename, url);
        }
        return result;
      } catch (MalformedURLException mue) {
        continue;
      }
    }
    traceLog(aFilename, null);
    return null;
  }

  /**
   * Resolve a logical reference to a URL, e.g.
   * will convert "$INSTALL/configs/common/foo.txt" to
   * "file:/opt/cougaar/20030331/configs/common/foo.txt"
   * or somesuch.
   * @return null if unresolvable.
   **/
  public URL resolveName(String logicalName) throws MalformedURLException {
    return Configuration.urlify(Configuration.substituteProperties(logicalName,properties));
  }

  /**
   * Opens an InputStream to access the named file. The file is sought
   * in all the places specified in configPath.
   * @throws IOException if the resource cannot be found.
   **/
  public InputStream open(String aURL) throws IOException {
    synchronized (urlCache) {
      URL u = (URL) urlCache.get(aURL);
      if (u != null) {
        return u.openStream();
      }
    }

    for (int i = 0, l=configPath.size(); i < l; i++) {
      URL base = (URL) configPath.get(i);
      try {
        URL url = newURL(base, aURL);
        if (url == null) continue;
        InputStream is = url.openStream();
        if (is == null) continue; // Don't return null
        traceLog(aURL, base);
        synchronized (urlCache) {
          urlCache.put(aURL, url);
        }
        return is;
      } 
      catch (MalformedURLException mue) {
        if (getLogger().isDebugEnabled()) {
          getLogger().debug("Exception while looking for "+aURL+" at "+base, mue);
        }
        continue;
      }
      catch (IOException ioe) {
        if (getLogger().isDebugEnabled()) {
          getLogger().debug("Exception while looking for "+aURL+" at "+base, ioe);
        }
        continue;
      }
      catch (RuntimeException rte) {
        if (getLogger().isDebugEnabled()) {
          getLogger().debug("Exception while looking for "+aURL+" at "+base, rte);
        }
        continue;
      }
    }

    traceLog(aURL, null);

    throw new FileNotFoundException(aURL);
  }

  /**
   * Attempt to find the URL which would be opened by the open method.
   * Note that this must actually attempt to open the various URLs
   * under consideration, so this is <em>not</em> an inexpensive operation.
   **/
  public URL find(String aURL) {
    synchronized (urlCache) {
      URL u = (URL) urlCache.get(aURL);
      if (u != null) {
        return u;
      }
    }
    for (int i = 0 ; i < configPath.size() ; i++) {
      URL base = (URL) configPath.get(i);
      try {
        URL url = newURL(base, aURL);
        if (url == null) continue;
        InputStream is = url.openStream();
        if (is == null) continue; // Don't return null
        is.close();
        traceLog(aURL, base);
        synchronized (urlCache) {
          urlCache.put(aURL, url);
        }
        return url;
      }
      catch (MalformedURLException mue) {
        continue;
      }
      catch (IOException ioe) {
        continue;
      }
    }
    //if (getLogger().isInfoEnabled()) 
    {
      // it isn't really an error, but I think we'd like to see these.
      getLogger().warn("Failed to find "+aURL);
    }
    traceLog(aURL, null);

    return null;
  }

  private URL newURL(URL base, String s) throws MalformedURLException {
    if (base == null || s == null) {
      return null;
    }
    if (!"file".equals(base.getProtocol()) ||
        !base.getPath().startsWith("/IN_COUGAAR_JARS/")) {
      return new URL(base, s);
    }
    String path =
      (s.startsWith("/") ? s :
       (base.getPath().substring("/IN_COUGAAR_JARS/".length()-1) + s));
    return getClass().getResource(path);
  }

  /** Read and parse an XML file somewhere in the configpath **/
  public Document parseXMLConfigFile(String xmlfile) throws IOException {
    InputStream istream = null;
      istream = open(xmlfile);
      if (istream == null) {
         throw new RuntimeException("Got null InputStream opening file " + xmlfile);
      }
    return parseXMLConfigFile(istream, xmlfile);
  }

  private final ConfigResolver _configResolver = new ConfigResolver();
  protected ConfigResolver getConfigResolver() { return _configResolver; }

  /** parse an XML stream in the context of the current configuration environment.
   * This means that embedded references to relative XML objects must be resolved
   * via the configfinder rather than the stream itself.
   **/
  protected Document parseXMLConfigFile(InputStream isstream, String xmlfile) {
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder parser = dbf.newDocumentBuilder();
      parser.setEntityResolver(getConfigResolver());
      InputSource is = new InputSource(isstream);
      return parser.parse(is);
    } catch (Exception e) {
      throw new RuntimeException("Unable to parse XML file \""+xmlfile+"\"", e);
    }
  }


  private static Class getConfigFinderClass() {
    String configFinderClassName = 
      SystemProperties.getProperty("org.cougaar.util.ConfigFinder.ClassName");
    Class theClass = ConfigFinder.class;
    if (configFinderClassName != null) {
      try {
	theClass = Class.forName(configFinderClassName);
      }
      catch (Exception e) {
	throw new RuntimeException("Not a valid ConfigFinder class: "
				   + configFinderClassName, e);
      }
      if (!ConfigFinder.class.isAssignableFrom(theClass)) {
	throw new RuntimeException(ConfigFinder.class.getName() + " should be a superclass of "
				   + configFinderClassName);
      }
    }
    return theClass;
  }

  private static ConfigFinder getConfigFinderInstance(String module, String path) {
    Class cls = getConfigFinderClass();
    Class paramCls[] = new Class[] {String.class, String.class};
    Object paramVal[] = new Object[] { module, path };

    try {
      return (ConfigFinder) cls.getConstructor(paramCls).newInstance(paramVal);
    }
    catch (Exception e) {
      throw new RuntimeException("Unable to instantiate ConfigFinder");
    }
  }

  // Singleton pattern
  private static ConfigFinder defaultConfigFinder = null;
  private synchronized static ConfigFinder getDefaultConfigFinder() {
    if (defaultConfigFinder == null) {
      try {
	defaultConfigFinder =
	  (ConfigFinder) getConfigFinderClass().newInstance();    
      }
      catch (Exception e) {
	throw new Error("Unable to instantiate ConfigFinder", e);
      }
    }
    return defaultConfigFinder;
  }

  /**
   * Return the default static instance of the ConfigFinder,
   * configured using the system properties.
   **/
  public static ConfigFinder getInstance() {
    return getDefaultConfigFinder();
  }

  // hash of the default module config finders
  private static Map moduleConfigFinders = new HashMap(11);

  /**
   * Return a new ConfigFinder that uses the system properties
   * for most configuration details, adding the four module-specific
   * directories to the front of the search path.
   **/
  public static ConfigFinder getInstance(String module) {
    if (module == null || module.equals("")) {
      return getDefaultConfigFinder();
    }
    
    String config_path = SystemProperties.getProperty("org.cougaar.config.path");
    if (config_path != null && 
	config_path.charAt(0) == '"' &&
	config_path.charAt(config_path.length()-1) == '"')	
      config_path = config_path.substring(1, config_path.length()-1);
    
    ConfigFinder mcf = (ConfigFinder) moduleConfigFinders.get(module);
    if (mcf == null) {
      mcf = getConfigFinderInstance(module, config_path);
      moduleConfigFinders.put(module, mcf);
    }
    return mcf;
  }

  /** Support class for parsing of XML files
   **/
  protected class ConfigResolver implements EntityResolver {
    public InputSource resolveEntity (String publicId, String systemId) {
      URL url = null;

      try {
	url = new URL(systemId);
      } catch(Exception e) {}

      String filename = url.getFile();

      // Convert any '\'s to '/'s.
      filename = filename.replace('\\', '/');

      filename = filename.substring(filename.lastIndexOf("/") + 1, filename.length());

      InputSource is = null;
      try {
	InputStream istream = open(filename);
        if (istream == null) {
	  throw new RuntimeException("Got null input stream opening file " + filename);
        }
        is = new InputSource(istream);
      } catch(IOException e) {
        getLogger().error("Error getting input source for file \""+filename+"\"", e);
      }
      
      if(is == null) {
        getLogger().error("Null InputSource for file \""+filename+"\"");
      }

      return is;
    } 
  }

  /** Hack for logging nice messages when tracing is enabled.
   * Looks back up the stack for method and origin points.
   **/
  // this could be static except that the logger isn't static right now.
  private void traceLog(Object name, Object value) {
    if (getLogger().isInfoEnabled()) {
      Throwable t = new Throwable();
      StackTraceElement[] st = t.getStackTrace();
      String frame = "unknown";
      String method = "unknown";
      for (int i = 0; i<st.length; i++) {
        StackTraceElement se = st[i];
        if (i == 1) {
          method = se.getMethodName();
        }
        String cn = se.getClassName();
        if ((!cn.startsWith("org.cougaar.util.")) &&
            (!cn.startsWith("org.apache."))) {
          frame = se.toString();
          break;
        }
      }
      getLogger().info(method+"("+name.toString()+")="+value+" from "+frame);
    }
  }

}
