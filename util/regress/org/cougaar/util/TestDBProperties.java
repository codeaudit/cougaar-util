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
import java.util.*;
import java.io.*;

import junit.framework.TestCase;
import junit.framework.*;

public class TestDBProperties extends TestCase {

  private DBProperties fill(String name, String s) {
    try {
      ReaderInputStream in = new ReaderInputStream(new StringReader(s));
      DBProperties dbp = new DBProperties.Immutable(name,in);
      in.close();
      return dbp;
    } catch (IOException e) {
      throw new RuntimeException("failed to create DBProperties", e);
    }
  }

  public void test_dbp() {
    DBProperties a = fill("a", "b = 2");
    String b = "b";

    assertEquals("initial value correct", "2", a.getProperty(b));
    try {
      a.setProperty(b, "99");
      fail("Immutable allowed set b=99");
    } catch (IllegalArgumentException iae) {
      assertTrue("disallow setting immutable value", true);         // keep the test count the same
    }
    assertEquals("Post-disallow left correct value alone", "2", a.getProperty(b));

    a = a.unlock();
    assertEquals("unlocked initial value correct", "2", a.getProperty(b));
    try {
      a.setProperty(b, "99");
      assertTrue("allow unlocked setting immutable value", true);         // keep the test count the same
    } catch (IllegalArgumentException iae) {
      fail("unlocked Immutable set disallowed set b=99");
    }
    assertEquals("unlocked immutable set properly", "99", a.getProperty(b));
    
    a = a.lock();
    assertEquals("relocked initial value correct", "99", a.getProperty(b));
    try {
      a.setProperty(b, "42");
      fail("relocked Immutable allowed set b=42");
    } catch (IllegalArgumentException iae) {
      assertTrue("relocked disallow setting immutable value", true); // keep the test count the same
    }
    assertEquals("relocked Post-disallow left correct value alone", "99", a.getProperty(b));
  }
}
