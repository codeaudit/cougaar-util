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

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

/** Class for testing Bean introspection in general and 
 * Asset/PG introspection in particular.  Essentially
 * loads a class as specified by the arglist, introspects 
 * on the class, and prints the results.
 **/

public class IntrospectionTest {
  public static void main(String args[]) {
    for (int ci = 0; ci < args.length; ci++) {
      String cname = args[ci];
      System.out.println(cname+": ");
      try {
        Class c = Class.forName(cname);
        BeanInfo bi = Introspector.getBeanInfo(c);
        PropertyDescriptor pds[] = bi.getPropertyDescriptors();
        for (int pi = 0; pi < pds.length; pi++) {
          PropertyDescriptor pd = pds[pi];
          System.out.println("\t"+pd.getReadMethod());
        }
      } catch (Exception e) {
        System.out.println("Caught "+e);
        e.printStackTrace();
      } 
    }
  }
}
