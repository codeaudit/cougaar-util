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
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class TestPropertyTree extends TestCase {
  private String str(Object o) {
    return (o==null)?"null":o.toString();
  }

  public void test_PropertyTree() {
    PropertyTree pt = new PropertyTree();
    pt.put("foo", "bar");
    List x = new ArrayList();
    x.add("elemA");
    x.add("elemB");
    pt.put("testList", x);

    PropertyTree subPT = new PropertyTree(1);
    subPT.put("sub1", "val1");
    subPT.put("sub2", "val2");
    subPT.put("sub3", "val3");
    pt.put("subPT", subPT);

    assertEquals("initialize", 
                 "{foo=bar, testList=[elemA, elemB], subPT={sub1=val1, sub2=val2, sub3=val3}}",
                 str(pt.toString()));
    assertEquals("get(foo)", "bar", pt.get("foo"));
    assertEquals("get(testList)", "[elemA, elemB]", str(pt.get("testList")));
    assertEquals("get(subPT)", "{sub1=val1, sub2=val2, sub3=val3}", str(pt.get("subPT")));

    assertEquals("get(subPT).get(sub1)", "val1", str(((PropertyTree)pt.get("subPT")).get("sub1")));
    assertEquals("get(xxx)", "null", str(pt.get("xxx")));
    assertEquals("get(null)", "null", str(pt.get(null)));
    try {
      pt.put(null, "val");
      fail("allows illegal key");
    } catch (Exception e) {
      assertTrue("denies illegal key (null)", true);
    }

    try {
      pt.put("obj", new Object());
      fail("allows illegal value");
    } catch (Exception e) {
      assertTrue("denies illegal value (new Object())", true);
    }

    PropertyTree dupPt = (PropertyTree)pt.clone();
    assertEquals("clone", 
                 "{foo=bar, testList=[elemA, elemB], subPT={sub1=val1, sub2=val2, sub3=val3}}",
                 str(dupPt.toString()));
    // .equals is probably not worth implementing properly
    //assertEquals("original.equals(clone)", pt, dupPt);
    try {
      java.io.ByteArrayOutputStream baos =
        new java.io.ByteArrayOutputStream(100);
      java.io.ObjectOutputStream oos = 
        new java.io.ObjectOutputStream(baos);
      oos.writeObject(pt);
      byte buf[] = baos.toByteArray();
      oos.close();
      //System.out.println("serialized to byte["+buf.length+"]");
      //System.out.println("attempt deserialize...");
      java.io.ByteArrayInputStream bais =
        new java.io.ByteArrayInputStream(buf);
      java.io.ObjectInputStream ois = 
        new java.io.ObjectInputStream(bais);
      PropertyTree newPT = (PropertyTree)ois.readObject();
      ois.close();
      //System.out.println("view deserialized result:");
      assertEquals("piped", 
                   "{foo=bar, testList=[elemA, elemB], subPT={sub1=val1, sub2=val2, sub3=val3}}",
                   str(newPT.toString()));
    } catch (Exception e) {
      fail("serialization failure:"+e);
    }
  }
}
