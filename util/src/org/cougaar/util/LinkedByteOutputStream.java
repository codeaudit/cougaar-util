/*
 * <copyright>
 *  Copyright 2002-2003 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Replacement for ByteArrayOutputStream which grows a linked list 
 * of multiple byte[]s instead of a single byte[].
 * <p>
 * The advantage of this implementation is that it grows in size
 * more efficiently than a single byte[].  This class also allows
 * greater control over the growth memory usage by overriding
 * the "allocLink(int)" method.
 *
 * @see java.io.ByteArrayOutputStream
 */
public class LinkedByteOutputStream extends OutputStream {

  protected static final int DEFAULT_INITIAL_SIZE = 1024;

  protected final int initialSize;

  protected int count;
  protected Link head;
  protected Link tail;

  /**
   * Creates a new linked byte output stream with a minimal buffer
   * growth increment of the default size (1024 bytes).
   */
  public LinkedByteOutputStream() {
    this(DEFAULT_INITIAL_SIZE);
  }

  /**
   * Creates a new linked byte output stream with the specified
   * initial size.
   *
   * @param size the initial size.
   */
  public LinkedByteOutputStream(int size) {
    this.initialSize = size;
    if (size < 0) {
      throw new IllegalArgumentException(
          "Negative size: "+size);
    }
  }

  /**
   * Writes the specified byte to this linked byte output stream.
   *
   * @param b the byte to be written.
   */
  public synchronized void write(int b) {
    byte[] buf;
    if (tail == null) {
      Link x = allocLink(1);
      buf = x.buf;
      head = x;
      tail = x;
    } else {
      buf = tail.buf;
      if (tail.offset + 1 > buf.length) {
        Link x = allocLink(1);
        buf = x.buf;
        tail.next = x;
        tail = x;
      }
    }
    buf[tail.offset++] = (byte) b;
    ++count;
  }

  /**
   * Writes <code>len</code> bytes from the specified byte array
   * starting at offset <code>off</code> to this linked byte output
   * stream.
   *
   * @param b the data.
   * @param off the start offset in the data.
   * @param len the number of bytes to write.
   */
  public void write(byte[] b, int off, int len) {
    if ((off < 0) || (off > b.length) || (len < 0) ||
        ((off + len) > b.length) || ((off + len) < 0)) {
      throw new IndexOutOfBoundsException();
    } else if (len == 0) {
      return;
    }
    synchronized (this) {
      if (tail == null) {
        Link x = allocLink(len);
        byte[] buf = x.buf;
        head = x;
        tail = x;
        System.arraycopy(b, off, buf, 0, len);
        tail.offset = len;
        count = len;
      } else {
        byte[] buf = tail.buf;
        int offset = tail.offset;
        int length = buf.length;
        int avail = length - offset;
        int spill = len - avail;
        if (spill > 0) {
          if (avail > 0) {
            System.arraycopy(b, off, buf, offset, avail);
            tail.offset = length;
          }
          Link x = allocLink(spill);
          buf = x.buf;
          tail.next = x;
          tail = x;
          System.arraycopy(b, off+avail, buf, 0, spill);
          tail.offset = spill;
        } else {
          System.arraycopy(b, off, buf, offset, len);
          tail.offset += len;
        }
        count += len;
      }
    }
  }

  /**
   * Writes the complete contents of this linked byte output stream to
   * the specified output stream argument, as if by calling the output
   * stream's write method using <code>out.write(buf, 0, count)</code>.
   *
   * @param      out   the output stream to which to write the data.
   * @exception  IOException  if an I/O error occurs.
   */
  public synchronized void writeTo(OutputStream out) throws IOException {
    for (Link curr = head; curr != null; curr = curr.next) {
      out.write(curr.buf, 0, curr.offset);
    }
  }

  /**
   * Resets the <code>count</code> field of this linked byte output
   * stream to zero, so that all currently accumulated output in the
   * ouput stream is discarded.
   * <p>
   * Note that this implementation does not reuse the reclaimed
   * links.  This would require checking the tail for a non-null
   * next link.
   */
  public synchronized void reset() {
    count = 0;
    Link x = head;
    head = null;
    tail = null;
    freeLinks(x);
  }

  /**
   * Creates a newly allocated byte array. Its size is the current
   * size of this output stream and the valid contents of the buffer
   * have been copied into it.
   *
   * @return the current contents of this output stream, as a byte array.
   * @see #size()
   */
  public synchronized byte[] toByteArray() {
    byte[] newbuf = new byte[count];
    int i = 0;
    for (Link curr = head; curr != null; curr = curr.next) {
      int j = curr.offset;
      System.arraycopy(curr.buf, 0, newbuf, i, j);
      i += j;
    }
    return newbuf;
  }

  /**
   * @return  the number of valid bytes in this output stream.
   */
  public int size() {
    return count;
  }

  /**
   * Converts the buffer's contents into a string, translating bytes into
   * characters according to the platform's default character encoding.
   *
   * @return String translated from the buffer's contents.
   */
  public String toString() {
    return new String(toByteArray());
  }

  /**
   * Converts the buffer's contents into a string, translating bytes into
   * characters according to the specified character encoding.
   *
   * @param   enc  a character-encoding name.
   * @return String translated from the buffer's contents.
   * @throws UnsupportedEncodingException
   *         If the named encoding is not supported.
   */
  public String toString(String enc) throws UnsupportedEncodingException {
    return new String(toByteArray(), enc);
  }

  /**
   * Closing a <tt>LinkedByteOutputStream</tt> has no effect. The methods in
   * this class can be called after the stream has been closed without
   * generating an <tt>IOException</tt>.
   */
  public void close() throws IOException {
  }

  /**
   * Construct a new link with at least the specified
   * capacity.
   * <p>
   * The default implementation works like array list,
   * growing from the initial size by (3/2).
   * <p>
   * This is here to support subclassing:<ol>
   *  <li>Support more complex growth algorithms.</li>
   *  <li>Support future link pooling<li>
   * </ol>
   */
  protected Link allocLink(int minIncrement) {
    int oldCapacity = count;
    int newIncrement;
    if (oldCapacity < initialSize) {
      newIncrement = initialSize;
    } else {
      int newCapacity = (oldCapacity * 3)/2 + 1;
      newIncrement = newCapacity - oldCapacity;
    }
    if (newIncrement < minIncrement)
      newIncrement = minIncrement;
    return new Link(newIncrement);
  }

  /**
   * Free all links in the linked list, starting with the
   * specified link.
   * <p>
   * This is here to support future link pooling.  If you
   * implement this method, consider implementing "finalize()".
   */
  protected void freeLinks(Link first) {
  }

  /**
   * Inner class for each link in the linked list.
   */
  protected static class Link {
    // non-null byte buffer:
    public final byte[] buf;
    // index between 0 and buf.length:
    public int offset;
    // next link, null if (offset < buf.length):
    public Link next;
    // constructor:
    public Link(int len) { this.buf = new byte[len]; }
    // developer debug:
    public String toString() {
      return 
        "{"+offset+"/"+buf.length+"}["+new String(buf)+"]"+
        (next != null ? next.toString() : "");
    }
  }

}
