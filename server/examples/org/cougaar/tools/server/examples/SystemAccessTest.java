/*
 * <copyright>
 *  Copyright 2003 BBNT Solutions, LLC
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
package org.cougaar.tools.server.examples;

import java.io.*;
import java.util.*;

/**
 * Simple test-process for <code>SystemAccess</code>'s use.
 * <p>
 * Runs forever, printing a "Testing[NUMBER]" every five
 * seconds.  SystemAccess can use this to test it's:<ul>
 *   <li>process-launching and PID reading</li>
 *   <li>process-listing</li>
 *   <li>JVM stack-dump signalling</li>
 * </ul>
 *
 * @see SystemAccess
 */
public class SystemAccessTest {

  public static void main(String[] args) throws Exception {
    for (int i = 0; ; i++) {
      System.out.println("Testing["+i+"]");
      Thread.sleep(5000);
    }
  }

}
