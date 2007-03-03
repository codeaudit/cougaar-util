/*
 * <copyright>
 *
 *  Copyright 2007 BBNT Solutions, LLC
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

import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Create an XMLReader that matches the JDK version's supported parser.
 */
public final class XMLReaderUtils {

  // preferred XML readers
  private static final String[] XML_READERS = new String[] {
    "-Dorg.xml.sax.driver", // optional system property
      "com.sun.org.apache.xerces.internal.parsers.SAXParser", // JDK 1.5
      "org.apache.crimson.parser.XMLReaderImpl", // JDK 1.4
      null, // use ParserFactory, which uses -Dorg.xml.sax.parser
  };

  private XMLReaderUtils() { }

  public static XMLReader createXMLReader() {
    for (int i = 0; i < XML_READERS.length; i++) {
      String classname = XML_READERS[i];
      if (classname != null && classname.startsWith("-D")) {
        classname = SystemProperties.getProperty(classname.substring(2));
        if (classname == null) {
          continue;
        }
      }
      XMLReader ret;
      try {
        ret =
          (classname == null ?
           XMLReaderFactory.createXMLReader() :
           XMLReaderFactory.createXMLReader(classname));
      } catch (Exception e) {
        ret = null;
      }
      if (ret != null) {
        // success!
        return ret;
      }
    }
    // failed
    StringBuffer buf = new StringBuffer();
    buf.append("Unable to create XMLReader, attempted:");
    for (int i = 0; i < XML_READERS.length; i++) {
      String s = XML_READERS[i];
      if (s == null) {
        s = "(default XMLFactory.createXMLReader())";
      }
      buf.append("\n  ");
      if (s.startsWith("-D")) {
        buf.append(SystemProperties.getProperty(s)).append(
            " (from \"").append(s).append("\")");
      } else {
        buf.append(s);
      }
    }
    throw new RuntimeException(buf.toString());
  }
}
