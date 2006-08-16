/*
 * <copyright>
 *  
 *  Copyright 2001-2004 BBNT Solutions, LLC
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

package org.cougaar.util.log.log4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.cougaar.bootstrap.SystemProperties;
import org.cougaar.util.Configuration;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.LoggerController;
import org.cougaar.util.log.LoggerFactory;

/** 
 * Log4j implementation of LoggerFactory, which is used to
 * create Logger and LoggerController instances.
 * <p>
 * Typically the "requestor" classname is used to identify 
 * loggers.  A special "name" is "root", which is used
 * to specify the root (no-parent) logger.
 * <p>
 * To configure Log4J, you may specify a file of Log4J
 * configuration properties. This may be a Log4J XML format file 
 * (ends with ".xml"), or a standard Java Properties (name=value) format file.
 * If no file is given, the default logging settings are used
 * (log to the CONSOLE at WARN level). 
 * You may also set any number of explicit System Properties to
 * over-ride any settings from a file. Log4J configuration properties
 * are documented in 
 * <a href="http://jakarta.apache.org/log4j/docs/manual.html">the log4j manual</a>.
 * <p>
 * To specify a file of Log4J configuration settings, use the
 * System Property <code>org.cougaar.util.log.config</code> 
 * (there are several aliases for this property - see below). 
 * This property should be a valid URL, absolute path to a file,
 * or a filename in $INSTALL/configs/common. 
 * (and if not found, the Default settings are used as if no 
 * no file was given).
 * <p>
 * To specify a particular Log4J configuration setting,
 * prefix the standard Log4J setting with <code>log4j.logger</code> OR
 * <code>org.cougaar.util.log</code> OR 
 * <code>org.cougaar.core.logging</code>.
 * For example, to turn on logging to the INFO level for this package,
 * use: <code>org.cougaar.util.log.log4j.category.org.cougaar.util.log.log4j=INFO</code>.
 *
 * @property org.cougaar.util.log.config Specifies a URL where a LoggerFactory configuration
 *    file may be found.  The Log4jLoggerFactory inteprets this as a file of log4j properties.
 * @property org.cougaar.util.log.config.filename Alias for <code>org.cougaar.util.log.config</code>
 * @property org.cougaar.core.logging.config.filaname Alias for <code>org.cougaar.util.log.config</code>
 * @property log4j.configuration Alias for <code>org.cougaar.util.log.config</code>
 * 
 * @property org.cougaar.core.logging.* Over-ride or set a particular Log4J configuration setting (the *)
 * @property org.cougaar.util.log.* Over-ride or set a particular Log4J configuration setting (the *)
 * @property log4j.logger.* Over-ride or set a particular Log4J configuration setting (the *)
 *    Properties not pointing to a Config File are stripped of their 
 *    prefix (one of the above 3) and passed to the
 *    logger configuration.  These properties override any 
 *    properties defined in the (optional) 
 *    logging config file.  
 */
public class Log4jLoggerFactory 
  extends LoggerFactory 
{
  public static final String PREFIX = "org.cougaar.core.logging.";
  public static final String LOG4JPREFIX = "log4j.logger.";
  public static final String FILE_NAME_PROPERTY = PREFIX + "config.filename";
  public static final String LOG4JCONF = "log4j.configuration";

  /**
   * Default configuration only prints WARN or higher
   * statements.
   * <p>
   * A client with name "x.y.z" that calls:<pre>
   *   log.warn("test message");
   * </pre>
   * will generate a standard-output message similar to:<pre>
   *   2002-03-08 22:26:08,980 WARN [z] - test message
   * </pre>.
   * <p>
   * See the log4j docs for further details.
   */
  private static final String[][] DEFAULT_PROPS = {
    {"log4j.rootCategory",                            "WARN,A1" },
    {"log4j.appender.A1",                             "org.apache.log4j.ConsoleAppender" },
    {"log4j.appender.A1.layout",                      "org.apache.log4j.PatternLayout" },
    {"log4j.appender.A1.layout.ConversionPattern",    "%d{ISO8601} %-5p [%c{1}] - %m%n" },
  };

  private static final Properties DEFAULT_PROPERTIES;

  static {
    Properties p = new Properties();
    for (int i = 0, n = DEFAULT_PROPS.length; i < n; i++) {
      p.put(DEFAULT_PROPS[i][0], DEFAULT_PROPS[i][1]);
    }
    DEFAULT_PROPERTIES = p;
  }

  // Name of file to read configuration from
  private String configFileName = null;

  public Log4jLoggerFactory() {
    // Save any errors for logging once Log4J configured
    Throwable err = null;

    // Get the logging properties config file from a System Property, if any
    configFileName = Log4jLoggerFactory.getConfigFileName();
    // System.out.println("Got Log4J config filename: " + configFileName);

    if (configFileName == null) {
      // No file of properties
      // Gather Default Props
      // Overlay with System Props
      // Configure
      //System.out.println("Using default properties");
      overlaySystemPropsAndConfigure(DEFAULT_PROPERTIES);
    } else {
      // Canonicalize / pseudo-ConfigFinder to get file
      URL cu = findURLFromName(configFileName);
      // if can't find it use above no-file approach
      if (cu == null) {
	System.err.println("Couldn't find Log4J config file " + configFileName + ". Using defaults.");
	overlaySystemPropsAndConfigure(DEFAULT_PROPERTIES);
      } else {
	//System.out.println("Configuring from URL " + cu);
	// Got a config file to parse and configure from
	if (configFileName.endsWith(".xml")) {
	  //System.out.println("Using DOMConfigurator");
	  //      Configure with DOMConfigurator
	  DOMConfigurator.configure(cu);
	  // Try overlaying with System Props
	  overlaySystemPropsAndConfigure(new Properties());
	  org.apache.log4j.Logger.getLogger(Log4jLoggerFactory.class).info("Configured Log4J logging using DOMConfigurator");
	} else {
	  //System.out.println("Reading in from props file");
	  Properties props = new Properties();
	  try {
	    // Read in Props from file
	    InputStream is = cu.openStream();
	    if (is != null) {
	      props.load(is);
	      is.close();
	    }
	  } catch (IOException ioe) {
	    // Save the error for later logging once Log4J configured
	    err = ioe;
	  }
	  // Overlay with System Props
	  // Configure
	  overlaySystemPropsAndConfigure(props);
	} // end of block to configure from props file
	// Log URL where config file found here.
	org.apache.log4j.Logger.getLogger(Log4jLoggerFactory.class).info("Configured logging from file found at URL: " + cu);

	// Log any error from above
	if (err != null)
	  org.apache.log4j.Logger.getLogger(Log4jLoggerFactory.class).error("Failed to configure logging from file. Defaults used.", err);

      } // end of block to handl xml or props files
    } // end of block where got a file name   
  }
  
  /**
   * Search the various possible System Properties for
   * the log4j configuration file name
   * Search takes first set property, searching:
   * ocu.log.config (LF_CONFIG_PROP)
   * ocu.log.config.filename (LF_PREFIX + "config.filename")
   * occ.logging.config.filename (FILE_NAME_PROPERTY)
   * log4j.configuration (LOG4JCONF)
   * @return String pointing to a file to configure Log4J from (possibly null)
   **/
  public static final String getConfigFileName() {
    String configFileName = SystemProperties.getProperty(LF_CONFIG_PROP);
    if (configFileName == null) configFileName = SystemProperties.getProperty(LF_PREFIX + ".config.filename");
    if (configFileName == null) configFileName = SystemProperties.getProperty(FILE_NAME_PROPERTY);
    if (configFileName == null) configFileName = SystemProperties.getProperty(LOG4JCONF);
    return configFileName;
  }

  // Do a pseudo-ConfigFinder search for a file
  // HOWEVER: In this case, search only 2 ways.
  // 1: Treat the filename as a URL or Absolute Path.
  // If we can't open a URL connection to that (it doesn't exist)
  // then,
  // 2: Look for a file of that name in $INSTALL/configs/common
  // return URL to the file or NULL if not found
  // See org.cougaar.util.Configuration
  private URL findURLFromName(String fileName) {
    URL url = null;
    
    // First try the fileName as a URL or absolute path
    try {
      url = Configuration.urlify(fileName);
      if (url != null) {
	//System.out.println("File arg was URLable: " + url);
	InputStream is = url.openStream();
	if (is == null) {
	  // Couldn't open it. Set url to null to indicate failure
	  url = null;
	} else {
	  is.close();
	  //System.out.println("Found " + url);
	}
      }
    } catch (MalformedURLException mue) {
      url = null;
    } catch (IOException ioe) {
      url = null;
    }

    // If the url is null then that didn't work
    // Try looking for it in configs/common
    if (url == null) {
      //System.out.println("Filename arg as URL wouldn't open.");
      try {
	// This call interprets the COUGAAR_INSTALL_PATH in and creates a URL from this
	URL base = Configuration.urlify(Configuration.resolveValue("$INSTALL/configs/common"));
	if (base != null) {
	  //System.out.println("URLified configs/common: " + base);
	  url = new URL(base, fileName);
	  InputStream is = url.openStream();
	  if (is == null) {
	    url = null;
	  } else {
	    is.close();
	    //System.out.println("Found in configs/common: " + url);
	  }
	}
      } catch (MalformedURLException mue) {
	//System.out.println("Got malformed URL: " + mue);
	url = null;
      } catch (IOException ioe) {
	url = null;
      }	
    }
    //System.out.println("Result of looking for " + fileName + " is the url: " + url);

    // Return whatever URL we came up with - possible null
    return url;
  }

  // Input a properties hashmap (possibly empty)
  // Overlay properties from System Properties
  // Such overlaid properties may start with
  // org.cougaar.core.logging (which will be trimmed off)
  // org.cougaar.util.log (which will be trimmed off)
  // or log4j.logger (which will NOT)
  // Then call PropertyConfigurator.configure with this map
  // Note that if Log4J has been previously configured using the
  // DOMConfigurator, these overlays may have little or no effect.
  private void overlaySystemPropsAndConfigure(Properties props) {
    if (props == null)
      props = new Properties();

    // Track input props for later info logging
    int inputProps = props.size();

    // Get properties from SystemProperties
    Properties spProps = new Properties();
    spProps.putAll(SystemProperties.getSystemPropertiesWithPrefix(PREFIX));
    spProps.putAll(SystemProperties.getSystemPropertiesWithPrefix(LOG4JPREFIX));
    // Allow SystemProps to specify as ocu.log.*?
    spProps.putAll(SystemProperties.getSystemPropertiesWithPrefix(LF_PREFIX));

    for (Iterator it = spProps.keySet().iterator(); it.hasNext(); ) {
      String name = (String) it.next();
      //System.out.println("Handling property " + name);

      // Exclude the properties that point to a config file
      if (name.equals(FILE_NAME_PROPERTY)) {
        continue;
      }

      if (name.equals(LF_CONFIG_PROP) || name.equals(LF_PREFIX + ".config.filename")) {
	continue;
      }

      String value = spProps.getProperty(name);
      // Strip the prefixes off
      if (name.indexOf(PREFIX) != -1)
	name = name.substring(PREFIX.length());

      // Note that you must add one for the "."
      if (name.indexOf(LF_PREFIX) != -1)
 	name = name.substring(LF_PREFIX.length() + 1);

      // Look for props that end with .File to check that dirs exist
      // Ugly, but the Log4J configurator refuses to try to create the directory
      if (name.endsWith(".File")) {
	if (value == null)
	  continue;

	File log = new File(value);
	//System.out.println("Using log file " + log);
	File parentdir = log.getParentFile();
	//System.out.println("Using parent dir " + parentdir);
	if (parentdir != null && ((! parentdir.exists() && ! parentdir.mkdirs()) || ! parentdir.canWrite())) {
	  System.err.println("Log4JLoggerFactory: Couldn't create / write to directory for File appender " + name + " for file " + value);
	  System.err.println(" -- Will skip that logging property.");
	  // can't write to the dir denoted. Skip it
	  continue;
	} else {
	  //System.out.println("Dir is OK");
	}
      }

//      System.out.println("Adding to list of system props: " + name + "=" + value);

      // Add this new prop to that from the info - over-riding
      // any previous value for that property
      props.put(name, value);
    } // end of loop over SystemProperties props to overlay

    // For debug: Print out logging properties
//     for (Iterator it = props.keySet().iterator(); it.hasNext(); ) {
//       String name = (String) it.next();
//       String value = props.getProperty(name);
//       System.out.println(name + "=" + value);
//     }

    // Have Log4J configure itself from the collected properties.
    PropertyConfigurator.configure(props);

    org.apache.log4j.Logger.getLogger(Log4jLoggerFactory.class).info("Configured logging from "+spProps.size()+" System Properties" + (inputProps > 0 ? (" overlaid on " + inputProps + " Properties from " + configFileName) : ""));
  }

  // ugh. bashing of static structure... sigh.
  public void configure(Properties props) {
    PropertyConfigurator.configure(props);
  }
  // ugh. bashing of static structure... sigh.
  public void configure(Map m) {
    Properties p = new Properties();
    p.putAll(m);
    PropertyConfigurator.configure(p);
  }

  /*
    // rely on the base class now
  public Logger createLogger(Object requestor) {
    return new LoggerImpl(requestor);
  }
  */

  /** called by the default implementation of createLogger to do the dirty work.
   * Should always create a new instance.
   **/
  protected Logger instantiateLogger(Object requestor) {
    return new LoggerImpl(requestor);
  }

  public LoggerController createLoggerController(String requestor) {
    (new Throwable()).printStackTrace();
    return new LoggerControllerImpl(requestor);
  }
}
