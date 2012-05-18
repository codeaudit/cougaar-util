/* 
 * <copyright>
 * 
 *  Copyright 2004 InfoEther
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

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;
import org.cougaar.bootstrap.SystemProperties;

/**
 * A log4j appender that sends "EVENT.*" logs (representing Cougaar
 * EventService operations) to a remote socket, typically where
 * ACME is listening.
 * <p>
 * To use, modify your "$CIP/configs/common/loggingConfig.conf" to
 * add:<pre> 
 *   log4j.rootCategory=WARN, .. ,EVENT
 *   log4j.category.EVENT=DEBUG
 *   log4j.appender.EVENT=org.cougaar.util.log.log4j.SocketAppender
 *   log4j.appender.EVENT.layout.ConversionPattern=%d{ABSOLUTE} %-5p - %c{1} - %m%n
 * </pre>
 * and in your Java "-D"s, specify the listener address:<pre>
 *   -Dorg.cougaar.event.host=<i>hostname</i>
 *   -Dorg.cougaar.event.port=<i>port</i>
 * </pre>
 *
 * @property org.cougaar.event.stdio
 * If set to </em>true</em> (the default) then the {@link
 * StreamCapture} will redirect STDOUT/STDERR to the logger.
 *
 * @property org.cougaar.event.host
 * Host name of the socket to which we'll send events.
 *
 * @property org.cougaar.event.port
 * Port of the socket to which we'll send events.
 */
public class SocketAppender extends AppenderSkeleton {

  private static final int SHOUT_INT = Priority.ERROR_INT+1;

  private boolean hasOptHost = false;
  private boolean hasOptPort = false;
  private String optHost="127.0.0.1";
  private int optPort=2000;

  private Socket connection;
  private PrintWriter pw;
  private boolean checkedOnce = false;

  private StreamCapture stdOutCapture;
  private StreamCapture stdErrCapture;

  @Override
public boolean requiresLayout() {
    return false;
  }

  private boolean checkConnection() {
    if (connection != null || checkedOnce) {
      // already connected
      return 
        (connection != null &&
         connection.isConnected());
    }

    // only perform this connection check once
    checkedOnce = true;

    String host = null;
    int port = 0;
    String sysHost = SystemProperties.getProperty("org.cougaar.event.host");
    String sysPort = SystemProperties.getProperty("org.cougaar.event.port");
    if (sysHost != null && sysPort != null) {
      host = sysHost;
      try {
        port = Integer.valueOf(sysPort).intValue();
      } catch (NumberFormatException ex) {
        ex.printStackTrace(System.err);
      }
    } else if (hasOptHost && hasOptPort) {
      host = optHost;
      port = optPort; 
    }

    boolean connect = (host != null && port > 0);
    if (connect) {
      try {
        connection = new Socket(host, port);
        initPrintWriter();
      } catch (IOException ex) {
        ex.printStackTrace(System.err);
      } catch (NumberFormatException ex) {
        ex.printStackTrace(System.err);
      }
    }

    //capture the StdOut and StdErr streams to Log4J
    boolean eatStdio = SystemProperties.getBoolean("org.cougaar.event.stdio", true);
    SecurityException se = null;
    if (eatStdio) {
      try {
        stdOutCapture = StreamCapture.captureStdOut();
        stdErrCapture = StreamCapture.captureStdErr();
      } catch (SecurityException x) {
        se = x;
      }
    } else {
      System.out.println("stdout and stderr will not be redirected");
    }

    if (connect) {
      try {
        String msg =
          "Std0ut and Stderr "+
          (eatStdio ?
           (se == null ?
            "redirected" :
            "redirect failed: "+se) :
           "will not be redirected");
        pw.println(
            "<CougaarEvent type=\"STATUS\">"+msg+"</CougaarEvent>");
        pw.flush();
      } catch (Exception ex) {
        ex.printStackTrace(System.err);
      }
    }

    //System.err.print("Log4J SocketAppender connected");

    return
      (connection != null &&
       connection.isConnected());
  }

  private void initPrintWriter() {
    try {
      pw = new PrintWriter(connection.getOutputStream());

      String nodeName = SystemProperties.getProperty("org.cougaar.node.name");

      String experimentName = 
        SystemProperties.getProperty("org.cougaar.event.experiment");
      if (experimentName == null) {
        experimentName = "";
      }

      pw.println(
          "<CougaarEvents Node=\"" + nodeName +
          "\" experiment=\""+experimentName+"\">");
      pw.flush();
    }
    catch (Exception e){e.printStackTrace();}
  }

  /**
   * @param clusterIdentifier
   * @param component
   * @param eventText
   * @param encoded
   * @return
   */
  private String generateEventString(
      String clusterIdentifier, String component, String eventText, boolean encoded) {
    StringBuffer event = new StringBuffer();
    event.append("<CougaarEvent type=\"").append("STATUS");
    if (clusterIdentifier != null)
      event.append("\" clusterIdentifier=\"").append(clusterIdentifier);
    if (component != null)
      event.append("\" component=\"").append(component);
    event.append("\">");
    if (encoded) {
      event.append(eventText);
    } else {
      event.append(encodeXML(eventText));
    }
    event.append("</CougaarEvent>");
    return event.toString();
  }

  public static String encodeXML(String text) {
    text = text.replaceAll("&", "&amp;");
    return text.replaceAll("<", "&lt;");
  }

  @Override
protected void append(LoggingEvent event) {
    if (!checkConnection()) {
      return;
    }

    String loggerName = event.getLoggerName();
    if (!loggerName.startsWith("EVENT")) {
      return;
    }

    String componentId = loggerName.substring(loggerName.lastIndexOf(".")+1);
    String clusterId = event.getRenderedMessage();
    clusterId = clusterId.substring(0, clusterId.indexOf(":"));
    String msg = generateEventString(
        clusterId, componentId, event.getRenderedMessage(), false);
    try {
      pw.println(msg);
      pw.flush();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  @Override
public void close() {
    if (stdOutCapture != null) {
      stdOutCapture.closeStream();
    }
    if (stdErrCapture != null) {
      stdErrCapture.closeStream();
    }

    if (connection != null && connection.isConnected()) {
      try {
        pw.println("</CougaarEvents>");
        connection.close();
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  @Override
public void activateOptions() {
    // create connection early if options were set by the log4j
    // PropertyConfigurator
    checkConnection();
  }

  public void setHostName(String hostName) {
    optHost = hostName;
    hasOptHost = true;
  }
  public void setPort(int port) {
    optPort = port;
    hasOptPort = true;
  }

}
