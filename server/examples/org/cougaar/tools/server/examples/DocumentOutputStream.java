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
 
package org.cougaar.tools.server.examples;

import java.io.OutputStream;
import java.awt.Color;
import javax.swing.text.*;

/**
 * Adapter for a swing Document to act like an OutputStream.
 * <p>
 * All "write" calls simply append to the document.
 * <p>
 * Additionally there is simple color and attribute support,
 * such as "append in red".
 * <p>
 * All uses of this adapter <b>must</b> be done within the
 * swing thread (or use swing's "invokeLater(..)").
 * <p>
 * I'm surprised that something like this isn't included in 
 * the JDK.  Feel free to copy this class into other 
 * non-example packages...
 */
public class DocumentOutputStream extends OutputStream {

  private final Document doc;
  private final AttributeSet atts;

  public DocumentOutputStream(
      Document doc) {
    this(doc, (Color) null);
  }

  public DocumentOutputStream(
      Document doc,
      Color color) {
    this(doc, (new SimpleAttributeSet()));
    if (color != null) {
      StyleConstants.setForeground(
          (SimpleAttributeSet) atts,
          color);
    }
  }

  public DocumentOutputStream(
      Document doc,
      AttributeSet atts) {
    this.doc = doc;
    this.atts = atts;
  }

  public void write(int b) {
    append(Integer.toString(b));
  }

  public void write(byte[] b) {
    append(new String(b));
  }

  public void write(byte[] b, int off, int len) {
    append(new String(b, off, len));
  }

  public void flush() {
  }

  public void close() {
  }

  private void append(String s) {
    try {
      doc.insertString(
          doc.getLength(), 
          s, 
          atts);
    } catch (BadLocationException ble) {
      // shouldn't happen -- maybe concurrent mod?
      throw new RuntimeException(ble.getMessage());
    }
  }
}
