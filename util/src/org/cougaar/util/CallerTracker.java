/*
 * <copyright>
 *  
 *  Copyright 2004 BBNT Solutions, LLC
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

import java.util.HashSet;

/** 
 * A set of utilities for tracking method invocations by stack.
 * Typically used to reduce repetitive warnings by only emitting
 * one complaint per caller.
 *
 * @note These are typically expensive operations, so do not use lightly.
 */

public abstract class CallerTracker {
  protected CallerTracker() {}

  protected final HashSet set = new HashSet(11);

  public boolean isNew() {
    return (checkFrame(new Throwable()) != null);
  }

  public Object isNewFrame() {
    return checkFrame(new Throwable());
  }
   
  protected Object checkFrame(Throwable t) {
    Object key = getKey(t);
    if (key == null) return null;
    synchronized (set) {
      if (set.contains(key)) {
        return null;
      } else {
        set.add(key);
        return key;
      }
    }
  }

  /** compute a set object from the throwable's stack.
   * May return null if it cannot compute a useful key.
   **/
  protected abstract Object getKey(Throwable t);
    

  /** Alias for getShallowTracker(1) */
  public static CallerTracker getShallowTracker() { return new ShallowTracker(1); }
  /** Construct a CallerTracker which discriminates between contexts based only
   * on the stack frame the specified number of frames above the frame which invokes
   * #isNew().<p>
   * A typical use would be to track callers of a particular method foo().  In the
   * class foo, define a static member which has a ShallowTracker(1) and then
   * modify the foo() method to emit a log message IFF tracker.isNew() is true.
   **/
  public static CallerTracker getShallowTracker(int i) { return new ShallowTracker(i); }


  protected static class ShallowTracker extends CallerTracker {
    private int depth;
    protected ShallowTracker(int n) {
      depth = n+1;
    }
    public Object getKey(Throwable t) {
      StackTraceElement[] stack = t.getStackTrace();
      return (stack.length > depth)?stack[depth]:null;
    }
  }

  /*
  private static CallerTracker tracker = getShallowTracker();

  public static void main(String[] args) {
    foo();
    foo();
    a();
    b();
    c();
    foo();
  }
  private static void a() {
    foo();
    foo();
  }
  private static void b() {
    foo();
    foo();
  }
  private static void c() {
    a();
    b();
    foo();
  }

  private static void foo() {
    Object f = tracker.isNewFrame();
    if (f != null) {
      System.out.println("New call to foo() from "+f);
    }
  }
  */
}
