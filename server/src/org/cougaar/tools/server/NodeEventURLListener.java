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
 
package org.cougaar.tools.server;

import java.net.URL;

/**
 * Static class with methods for creating and parsing headers for a URL based
 * listener.
 */
public class NodeEventURLListener {
  private static final String PREFIX = "GET ";
  private static final int PREFIX_LENGTH = PREFIX.length();
  private static final String SUFFIX = " HTTP/1.0 302\r\n\r\n";
  private static final int SUFFIX_LENGTH = SUFFIX.length();

  public static String getFileName(String header) {
    if (header.startsWith(PREFIX) && 
        header.endsWith(SUFFIX)) {
      return header.substring(PREFIX_LENGTH - 1, 
                              header.length() - SUFFIX_LENGTH -1).trim(); 
      
    } else {
      System.err.println("NodeEventURLListener.getFileName - can't parse " + 
                         header);
      return "";
    }
  }

  public static String createHeader(URL listenerURL) {
    return new String(PREFIX + listenerURL.getFile() + SUFFIX);
  }

}


