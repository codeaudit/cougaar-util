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
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

/** 
 * Translations holds a set of static methods for translating between
 * various representations of objects, primarily collection-like
 * things.  For the most part, this functionality should either not be
 * needed (e.g. enumeration versus iteration) or belongs in core java.
 **/

public final class Translations {
  private Translations() {}

  /** copy the contents of an enumeration into a collection for later
   * searching.  The Enumeration will be empty when the method
   * returns.
   **/
  public static Collection toCollection(Enumeration e) {
    Collection tmp = new ArrayList();
    while (e.hasMoreElements()) {
      tmp.add(e.nextElement());
    }
    return tmp;
  }

  public static Collection toCollection(Iterator i) {
    Collection tmp = new ArrayList();
    while (i.hasNext()) {
      tmp.add(i.next());
    }
    return tmp;
  }

  public static Enumeration toEnumeration(Collection c) {
    return new Enumerator(c);
  }

  public static Enumeration toEnumeration(Iterator i) {
    return new Enumerator(i);
  }
}
