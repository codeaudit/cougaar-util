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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A PropertyTree is an <code>ArrayMap</code> that limits it's keys to
 * <code>String</code>s and it's values to:<ol>
 *   <li>null</li>
 *   <li>String</li>
 *   <li>wrapped Java primitive (Integer, Boolean, etc)</li>
 *   <li>another PropertyTree</li>
 *   <li>a Serializable Collection of elements, each element matching 
 *       one of the above criterion</li>
 * </ol>.
 *
 * An ArrayMap is a <code>java.util.Map</code>, and can be used exactly
 * like a typical Map.  The most significant differences are that<ol>
 *   <li>The elements are kept in the order that they are added.</li>
 *   <li>One can restrict the (key, value) types.</li>
 *   <li>There are index-based "getters", such as <tt>getKey(int)</tt>.
 * </ol>
 *
 * @see ArrayMap
 */
public class PropertyTree 
  extends ArrayMap {

  //
  // constructors
  //

  public PropertyTree(int initialCapacity) {
    super(initialCapacity);
  }

  public PropertyTree() {
    super(10);
  }

  public PropertyTree(Map t) {
    super(t);
  }

  //
  // maybe add some new "NS"-like directory methods
  //

  //
  // keep all the Map methods
  //

  /**
   * Override entries to be <code>PropertyTree.PropertyTreeEntry</code>.
   *
   * @see PropertyTree.PropertyTreeEntry
   */
  protected Map.Entry createEntry(
      final Object key,
      final Object value) {
    return new PropertyTreeEntry(key, value);
  }

  /**
   * Force keys to be non-null <code>String</code>s 
   * and values to be one of:<ol>
   *   <li>null</li>
   *   <li>String</li>
   *   <li>wrapped Java primitive (Integer, Boolean, etc)</li>
   *   <li>another PropertyTree</li>
   *   <li>a Serializable Collection of elements, each element matching 
   *       one of the above criterion</li>
   * </ol>.
   */
  protected static class PropertyTreeEntry 
    extends ArrayMap.ArrayEntry {

      public PropertyTreeEntry(
          final Object key, 
          final Object value) {
        super(key, value);
        assertIsValidKey(key);
        assertIsValidValue(value);
      }

      public Object setValue(final Object newValue) {
        assertIsValidValue(newValue);
        return super.setValue(newValue);
      }

      /**
       * Valid PropertyTree "keys" must be non-null Strings.
       *
       * @throws IllegalArgumentException
       */
      private static final void assertIsValidKey(final Object key) {
        if (key instanceof String) {
          // valid
        } else {
          throw new IllegalArgumentException(
              "PropertyTree \"key\" must be a non-null String, not "+
              ((key != null) ? key.getClass().getName() : "null"));
        }
      }

      /**
       * Valid PropertyTree "values" are:<ol>
       *   <li>null</li>
       *   <li>String</li>
       *   <li>wrapped Java primitive (Integer, Boolean, etc)</li>
       *   <li>another PropertyTree</li>
       *   <li>a Serializable Collection of elements, each element matching 
       *       one of the above criterion</li>
       * </ol>.
       *
       * @throws IllegalArgumentException if not <tt>isValidValue(value)</tt>.
       */
      private static final void assertIsValidValue(final Object value) {
        if ((value == null) ||
            (value instanceof String) ||
            (value instanceof PropertyTree) ||
            ((value instanceof Collection) &&
             (value instanceof Serializable)) ||
            (ArrayMap.isWrappedPrimitive(value))) {
          // valid
        } else {
          throw new IllegalArgumentException(
              "PropertyTree \"value\" of illegal type: "+
              value.getClass().getName());
        }
      }

    }

  private static final long serialVersionUID = 182883718294610011L;

  /** simple test utility */
  public static void main(String[] args) {
    System.out.println("create a sample PropertyTree...");
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
    System.out.println(pt);
    System.out.println("should look like: \n"+
"{foo=bar, testList=[elemA, elemB], subPT={sub1=val1, sub2=val2, sub3=val3}}");
    System.out.println("get(\"foo\"): "+pt.get("foo"));
    System.out.println("get(\"testList\"): "+pt.get("testList"));
    System.out.println("get(\"subPT\"): "+pt.get("subPT"));
    System.out.println("((PropertyTree)get(\"subPT\")).get(\"sub1\"): "+
        ((PropertyTree)pt.get("subPT")).get("sub1"));
    System.out.println("get(\"xxx\"): "+pt.get("xxx"));
    System.out.println("get(null): "+pt.get(null));
    try {
      pt.put(null, "val");
      System.out.println("bad: allows illegal key");
    } catch (Exception e) {
      System.out.println("good: denies illegal key (null)");
    }
    try {
      pt.put("obj", new Object());
      System.out.println("bad: allows illegal value");
    } catch (Exception e) {
      System.out.println("good: denies illegal value (new Object())");
    }
    System.out.println("attempt clone...");
    PropertyTree dupPt = (PropertyTree)pt.clone();
    System.out.println(dupPt);
    try {
      System.out.println("attempt serialize...");
      java.io.ByteArrayOutputStream baos =
        new java.io.ByteArrayOutputStream(100);
      java.io.ObjectOutputStream oos = 
        new java.io.ObjectOutputStream(baos);
      oos.writeObject(pt);
      byte buf[] = baos.toByteArray();
      oos.close();
      System.out.println("serialized to byte["+buf.length+"]");
      System.out.println("attempt deserialize...");
      java.io.ByteArrayInputStream bais =
        new java.io.ByteArrayInputStream(buf);
      java.io.ObjectInputStream ois = 
        new java.io.ObjectInputStream(bais);
      PropertyTree newPT = (PropertyTree)ois.readObject();
      ois.close();
      System.out.println("view deserialized result:");
      System.out.println(newPT);
    } catch (Exception e) {
      System.out.println("serialization failure:");
      e.printStackTrace();
    }
    System.out.println("end test");
  }

}

