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

import java.util.*;

/**
 * A "memoization" utility - as long as the specified condition object
 * doesn't change, then the result doesn't change.
 **/
public class Memo
{
  private static Object UNDEFINED = new Object();

  private final Memo.Function function;
  private Object param = UNDEFINED;
  private Object result = UNDEFINED;

  private Memo(Memo.Function f) { this.function = f; }

  /** Factory method for a Memo **/
  public static Memo get(Memo.Function f) {
    return new Memo(f);
  }

  /** Compute the result of applying the Memo's function to the parameter.
   * The function might not actually be run if the parameter's value is ==
   * the previous value (if any).
   **/
  public synchronized Object eval(Object p) {
    if (param != p) {           // catches UNDEFINED, too
      result = function.eval(p);
      param = p;
    }
    return result;
  }

  /** Similar to #eval(Object), except will return the result of applying
   * the function to the parameter IFF the function was actually called.
   * This allows the caller to easily abort a sequence of events if
   * the previous computation is still in force.
   * @note Use of this method implies that the function should never return null
   * as a valid result or it will be impossible to distinguish between that case
   * and a "not new" case.
   **/
  public synchronized Object evalIfNew(Object p) {
    if (param != p) {           // catches UNDEFINED, too
      result = function.eval(p);
      param = p;
      return result;
    } else {
      return null;
    }
  }    

  public static interface Function {
    Object eval(Object x);
  }
}

      

    
