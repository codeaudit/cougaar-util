/* 
 * <copyright> 
 *  Copyright 1999-2005 Cougaar Software, Inc.
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

import java.util.Properties;

import junit.framework.TestCase;


/**
 * @author srosset
 */
public class SystemPropertiesTest extends TestCase {

  /*
   * A set of properties Before Expansion.
   */
  private static final String BE_A       = "c:\\cougaar";
  private static final String BE_A_SUBA  = "bob";
  private static final String BE_B       = "${a} ${a.${c}}/bar";
  private static final String BE_C       = "subA";
  private static final String BE_D       = "\\$\\{a\\} ${a.${c}}/bar";
  private static final String BE_E       = "\\\\$\\{a\\} ${a.${c}}/bar";
  private static final String BE_V       = "${b}";
  private static final String BE_W       = "${a.${abc}}";
  private static final String BE_X       = "${y}";
  private static final String BE_Y       = "${z}";
  private static final String BE_Z       = "${x}";
  
  /*
   * A set of properties After Expansion.
   */
  private static final String AE_B       = BE_A + " " + BE_A_SUBA + "/bar";
  private static final String AE_D       = "${a} " + BE_A_SUBA + "/bar";
  private static final String AE_E       = "\\${a} " + BE_A_SUBA + "/bar";
  
  public void testPropertyExpansion() {
    System.setProperties(new Properties());

    System.setProperty("a",      BE_A);
    System.setProperty("a.subA", BE_A_SUBA);
    System.setProperty("b",      BE_B);
    System.setProperty("c",      BE_C);
    System.setProperty("d",      BE_D);
    System.setProperty("e",      BE_E);
    // Test forward references
    System.setProperty("V",      BE_V);
    
    // Expand properties
    try {
      SystemProperties.expandProperties();
    }
    catch (Exception e) {
      fail ("Unexpected property after expansion: " + e);
    }
    
    checkProperty("a", BE_A);
    checkProperty("b", AE_B);
    checkProperty("c", BE_C);
    checkProperty("d", AE_D);
    checkProperty("e", AE_E);
    checkProperty("V", AE_B);
  }
  

  /**
   * Test unresolved references
   */
  public void testUnresolvedReferences() {
    System.setProperties(new Properties());
    
    System.setProperty("x",      BE_X);
    System.setProperty("y",      BE_Y);
    
    // Expand properties
    try {
      // This should throw an exception
      SystemProperties.expandProperties();
      fail ("Did not detect unresolved references");
    }
    catch (IllegalArgumentException e) {
      // Nothing to do. This is what we expect in the test.
    }
    catch (Exception e) {
      fail("Unexpected exception: " + e);
    }
    
    System.setProperty("W",      BE_W);
    // Expand properties
    try {
      // This should throw an exception
      SystemProperties.expandProperties();
      fail ("Did not detect unresolved references");
    }
    catch (IllegalArgumentException e) {
      // Nothing to do. This is what we expect in the test.
    }
    catch (Exception e) {
      fail("Unexpected exception: " + e);
    }
  }
  
  /**
   * Test circular references
   */
  public void testCircularExpansion() {
    System.setProperties(new Properties());
    System.setProperty("x",      BE_X);
    System.setProperty("y",      BE_Y);
    System.setProperty("z",      BE_Z);

    // Expand properties
    try {
      // This should throw an exception
      SystemProperties.expandProperties();
      fail ("Did not detect circular reference");
    }
    catch (IllegalArgumentException e) {
      // Nothing to do. This is what we expect in the test.
    }
    catch (Exception e) {
      fail("Unexpected exception: " + e);
    }
  }

  /**
   * Check the value of a System property after expansion.
   * @param propertyName The name of a System property.
   * @param expectedValue The expected value of the named property.
   */
  private void checkProperty(String propertyName, String expectedValue) {
    String value = System.getProperty(propertyName);
    if (expectedValue == null) {
      assertNull(value);
    }
    else {
      assertEquals("Property " + propertyName +
          ": Expected " + expectedValue + " but was " + value,
          expectedValue, value);
    }
  }
}
