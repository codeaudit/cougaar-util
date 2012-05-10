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

/** 
 * A Thunk is small piece of code to be executed repeatedly, often
 * gathering state for later perusal.
 */

public interface Thunk 
{
  /** Called to "run" the thunk on an object **/
  void apply(Object o);

  /** A counter thunk which counts the number of times it is called.
   * May be reused via reset() method, but no attempt is made to
   * make instances thread-safe.
   **/
  class Counter implements Thunk {
    private int counter = 0;
    public Counter() {}
    public void apply(Object o) {
      counter++;
    }
    public int getCount() { return counter; }
    public void reset() { counter=0;}
  }

  /** a Thunk which collects all the arguments into a Collection **/
  class Collector implements Thunk {
    private final Collection c;
    public Collector() { c = new ArrayList(); }
    public Collector(Collection c) { this.c=c; }
    public void apply(Object o) { c.add(o); }
    public Collection getCollection() { return c; }
  }

}

