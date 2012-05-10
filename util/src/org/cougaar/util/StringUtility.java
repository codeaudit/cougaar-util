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

import java.util.Vector;

/** Utility knife for efficiently parsing stuff out of strings.
 **/

public class StringUtility {

  /** an empty String instance **/
  public static final String emptyString = "";

  /** @return an array of strings which represents the comma-separated-
   * values list from the range of characters in buf. 
   * @deprecated Use {@link CSVUtility#parse(String)} 
   **/
  public static Vector parseCSV(String buf, int start, int end, char comma) {
    Vector tmp = new Vector();

    StringBuffer sb = new StringBuffer(); // temp storage

    int i = start;
    int lc = -1;                 // last comma index
    boolean white = true;
    for (; i < end; i++) {
      char c = buf.charAt(i);
      if (c == comma) {
        tmp.addElement(trimStringBufferRight(sb));
        sb.setLength(0);
        lc = i;
        white = true;
      } else if (c == '\\') {
        i++;
        c = buf.charAt(i);
        sb.append(c);
        white=false;
      } else if (! white) {
        sb.append(c);
      } else if (! Character.isWhitespace(c)) {
        sb.append(c);
        white=false;
      } else {
        // white and still reading initial white space
      }
    }
    if (sb.length() > 0) {
      // still chars in the buffer?
      tmp.addElement(trimStringBufferRight(sb));
    } else if (lc > -1 && i>lc) {
      tmp.addElement(emptyString);
    }
      
    return tmp;
  }

  public static String trimStringBufferRight(StringBuffer sb) {
    int l = sb.length();

    // if empty, return an empty string
    if (l == 0) return emptyString;

    // skip trailing whitespace
    int j;
    for (j = (l-1); j>=0; j--) {
      if (! Character.isWhitespace(sb.charAt(j))) {
        break;
      }
    }

    // if only whitespace return empty
    if (j < 0) return emptyString;

    sb.setLength(j+1);

    return sb.toString();
  }

  static Vector toVector(String[] ss) {
    Vector r = new Vector(ss.length);
    for (int i = 0; i< ss.length; i++) {
      r.add(ss[i]);
    }
    return r;
  }

  /** @deprecated Use {@link CSVUtility#parse(String)} **/
  public static Vector parseCSV(String buf) {
    return toVector(CSVUtility.parse(buf));
  }
  /** @deprecated Use {@link CSVUtility#parse(String)} **/
  public static Vector parseCSV(String buf, int start) {
    return toVector(CSVUtility.parse(buf.substring(start)));
  }
  /** @deprecated Use {@link CSVUtility#parse(String)} **/
  public static Vector parseCSV(String buf, int start, int end) {
    return toVector(CSVUtility.parse(buf.substring(start,end)));
  }

  /** @deprecated Use {@link CSVUtility#parse(String)} or {@link String#split(String)} **/
  public static Vector parseCSV(String buf, char comma) {
    if (comma == ',') {
      return parseCSV(buf);
    } else {
      return toVector(buf.split("\\s*"+comma+"\\s*"));
    }
  }

  /** @deprecated Use {@link String#split(String)} **/
  public static Vector explode(String s) {
    Vector v = new Vector();
    int k = 0;                  // char after last white
    int l = s.length();
    int i = 0;
    while (i < l) {
      if (Character.isWhitespace(s.charAt(i))) {
        // is white - what do we do?
        if (i == k) {           // skipping contiguous white
          k++;
        } else {                // last char wasn't white - word boundary!
          v.addElement(s.substring(k,i));
          k=i+1;
        }
      } else {                  // nonwhite
        // let it advance
      }
      i++;
    }
    if (k != i) {               // leftover non-white chars
      v.addElement(s.substring(k,i));
    }
    return v;
  }

  /** 
   * Prefix all lines of "s" with "prefix", e.g.<br>
   *   <code>prefixLines("X: ", "a\nb")</code><br>
   * returns<br>
   *   <code>"X: a\nA: b"</code><br>
   */
  public static String prefixLines(String prefix, String s) {
    StringBuffer sb = new StringBuffer();
    int j = s.indexOf('\n');
    if (j < 0) {
      sb.append(prefix).append(s);
    } else {
      int i = 0;
      do {
        sb.append(prefix).append(s.substring(i, ++j));
        i = j;
        j = s.indexOf('\n', i);
      } while (j >= 0);
      sb.append(prefix).append(s.substring(i));
    }
    return sb.toString();
  }

  public static String arrayToString(Object[] array) {
    return appendArray(new StringBuffer(), array).substring(0);
  }

  public static StringBuffer appendArray(StringBuffer buf, Object[] array) {
    buf.append('[');
    for (int i = 0; i < array.length; i++) {
      if (i > 0) buf.append(',');
      buf.append(array[i]);
    }
    buf.append(']');
    return buf;
  }
}
