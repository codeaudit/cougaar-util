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
