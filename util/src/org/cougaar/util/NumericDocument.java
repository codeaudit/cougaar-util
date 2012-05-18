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

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class NumericDocument extends PlainDocument {
  /**
    * 
    */
   private static final long serialVersionUID = 1L;

/**
   * Default constructor creates a NumericDocument holding a zero value.
   **/
  public NumericDocument() {
    try {
      insertString(0, "0", null);
    }
    catch (BadLocationException e) {
    }
  }
  /**
   * Insert a string into the document. The string is checked to
   * insure that all characters are digits.
   * @param offset the location in the document where insertion is to occur.
   * @param s the string to insert -- all characters must be decimal digits.
   * @param attrs the set of attributes fo the inserted characters.
   **/
  @Override
public void insertString(int offset, String s, AttributeSet attrs) throws BadLocationException {
    for (int i = 0, n = s.length(); i < n; i++) {
      char c = s.charAt(i);
      if (c < '0' || c > '9') {
        throw new IllegalArgumentException("not a digit");
      }
    }
    super.insertString(offset, s, attrs);
  }

  /**
   * Replace the current value in the document with a new value.
   * @param value the new value to insert.
   **/
  public void setValue(int value) {
    try {
      remove(0, getLength());
      insertString(0, Integer.toString(value), null);
    }
    catch (BadLocationException e) {
    }
  }

  /**
   * Get the current value in the document. Converts the string in
   * the document to a number.
   * @return the value in the buffer as an int.
   **/
  public int getValue() {
    try {
      String text = getText(0, getLength());
      return Integer.parseInt(text);
    }
    catch (BadLocationException e) {
      return 0;
    }
  }
}
