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

import java.lang.ref.SoftReference;

/** A hack for computing a complex object's toString as needed,
 * but without keeping it around for ever.
 **/
public abstract class ToStringMemo {
  private SoftReference memo = null;
  public final synchronized void discard() {
    memo = null;
  }
  public final synchronized String toString() {
    if (memo != null) {         // we've got a memo
      String s = (String) memo.get();
      if (s != null) {          // and the memo isn't empty
        return s;               // return it
      }
    }
    // otherwise, recompute the memo
    String s = generate();
    memo = new SoftReference(s);
    return s;
  }
  
  /** Implement this to be the actual toString implementation **/
  protected abstract String generate();
      
  /** Construct a ToStringMemo which uses the parameter object's toString
   * to build the memoized toString value.
   **/
  public static ToStringMemo getInstance(final Object gen) {
    return new ToStringMemo() {
        protected String generate() { return gen.toString(); }
      };
  }
}
