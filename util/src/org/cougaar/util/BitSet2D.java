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

import java.util.BitSet;

/**
 * A two dimensional set of bits. Like java.io.util.Bitset but the
 * bits are addressed in two dimensions. The set is grown as needed to
 * accomodate indices in either dimension. Indices must be positive.
 **/
public class BitSet2D {
  private BitSet theSet = new BitSet();
  int height = 100;

  public BitSet2D(int height) {
    this.height = height;
  }

  public BitSet2D() {
    this.height = 8;
  }

  private int index(int x, int y) {
    if (y >= height) {
      BitSet newSet = new BitSet();
      int newheight = y + 1;
      for (int i = 0, n = theSet.size(); i < n; i++) {
        int xx = i / height;
        int yy = i % height;
        int ii = yy + newheight * xx;
        if (theSet.get(i)) {
          newSet.set(ii);
        } else {
          newSet.clear(ii);
        }
      }
      theSet = newSet;
      height = newheight;
    }
    return y + height * x;
  }

  public boolean get(int x, int y) {
    return theSet.get(index(x, y));
  }

  public void set(int x, int y) {
    theSet.set(index(x, y));
  }

  public void clear(int x, int y) {
    theSet.clear(index(x, y));
  }
}
