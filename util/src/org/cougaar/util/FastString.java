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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/** A string wrapper which serializes the enclosed string more
 * efficiently... maybe.
 **/

public class FastString implements Externalizable {
  private transient String string;
  public FastString() {}
  public FastString(String s) { string = s; }

  @Override
public String toString() {
    return string;
  }

  private static final int BUFSIZE = Short.MAX_VALUE;
  private static final byte[] srcbuf = new byte[BUFSIZE];

  public final void readExternal(ObjectInput in) throws IOException {
    int l = in.readShort();
    synchronized (srcbuf) {
      in.readFully(srcbuf,0,l);

      string=new String(srcbuf,0,l);
    }

    // intern short strings
    if (l < 18) string=string.intern();
  }

  private static final char[] dstbuf = new char[BUFSIZE];

  public final void writeExternal(ObjectOutput out) throws IOException {
    int l = string.length();
    if (l > BUFSIZE)
      throw new IOException("Attempt to serialize a FastString of length "+l+
                            " (limit is "+BUFSIZE+").\n"+
                            "  First 60 characters are \""+string.substring(0,60)+
                            "...\"");
    out.writeShort(l);
    char[] dst = dstbuf;        // localize the buffer reference
    synchronized (dst) {
      string.getChars(0, l, dst, 0);
      for (int i=0; i<l; i++) {
        out.write(dst[i]);      // upper bits ignored
      }
    }
  }

}
