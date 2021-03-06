/*
 * <copyright>
 *  
 *  Copyright 2002-2004 BBNT Solutions, LLC
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

import java.util.Iterator;
import java.util.NoSuchElementException;

/** An Iterator implementation which successively iterates over
 * a list of iterators.
 **/
public class ChainingIterator 
  implements Iterator 
{
  private Iterator[] its;
  private int i = 0;
  private int l;
  private Object nextOne = null;
  private boolean hasNext = true; // gets set to false by advance if empty

  public ChainingIterator(Iterator[] its) {
    this.its = its;
    l = its.length;
    advance();
  }
    
  private void advance() {
    while (i<l) {               // as long as we have iterators
      // check the current iterator
      Iterator c = its[i];

      // null iterator? advance iterators
      if (c == null) {         
        i++;
        continue;
      } 
      
      // anything left on the current iterator?
      if (c.hasNext()) {
        nextOne = c.next();
        return;
      }

      // nope - advance
      i++;
    }
    hasNext = false;
  }
        
  public final boolean hasNext() { return hasNext; }
  public final Object next() { 
    if (hasNext) {
      Object o = nextOne;
      advance();
      return o;
    } else {
      throw new NoSuchElementException(); 
    }
  }

  public final void remove() {
    throw new UnsupportedOperationException();
  }
}
      
