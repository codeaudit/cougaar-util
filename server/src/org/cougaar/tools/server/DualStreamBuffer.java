/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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
 
package org.cougaar.tools.server;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * This utility-class is a buffer for capturing system-out and 
 * system-error output to a byte[], plus reading the captured 
 * output back with the interleaved write-ordering preserved.
 * <p>
 * Example usage:<pre>
 *    // create streams
 *    DualStreamBuffer dsb = new DualStreamBuffer();
 *    PrintStream pout = new PrintStream(dsb.getOutputStream(true));
 *    PrintStream perr = new PrintStream(dsb.getOutputStream(false));
 *    // use streams as usual
 *    pout.print("example std-out");
 *    perr.print("some error");
 *    pout.print("more std-out");
 *    // replay to standard-out and standard-err
 *    dsb.writeTo(System.out, System.err);
 * </pre>
 * <p>
 * The internal representation has been optimized to make
 * both appending and the <tt>writeTo(*)</tt> methods efficient
 * (memory + time).
 * <p>
 * <b>NOTE:</b> this class is not internally thread-safe!  If
 * multiple threads need access to the same DualStreamBuffer
 * instance then they must externally synchronized their 
 * actions.  This class <i>does</i> internally support multiple 
 * simultaneous readers if there are zero simultaneous writers.
 */
public final class DualStreamBuffer 
implements Serializable, Cloneable {

  //
  // The data structure is a shared "byte[] mergedBuffer" for all 
  // the output, plus an "int[] offsetTable" for starting-point 
  // offsets within the mergedBuffer.  All indices into the 
  // offsetTable follow this pattern:
  //    EVEN indices are standard-out
  //    ODD indices are standard-err
  //

  private transient byte[] mergedBuffer;
  private int mergedBufferLength;
  private transient int[] offsetTable;
  private int offsetTableLength;

  public DualStreamBuffer() {
    this(89);
  }

  public DualStreamBuffer(int size) {
    if (size <= 0) {
      throw new IllegalArgumentException(
          "Invalid size: "+size);
    }
    mergedBuffer = new byte[size];
    mergedBufferLength = 0;
    offsetTable = new int[size];
    offsetTableLength = 1;
  }

  //
  // For now there are only two "setter" methods, used
  // for appending bytes from standard-out or standard-err:
  //

  /**
   * <pre>
   * Get a stream for appending data either
   *   standard-out   <tt>(isOut == true)</tt> or
   *   standard-error <tt>(isOut == false)</tt>.
   * </pre>
   */
  public OutputStream getOutputStream(boolean isOut) {
    return new InnerOutputStream(isOut);
  }

  //
  // These "getter" methods seem sufficient for now:
  //

  /**
   * Get an InputStream for both the 
   * standard-out <b>and</b> standard-error (interleaved in 
   * correct ordering).
   *
   * @see #writeTo(OutputStream)
   */
  public InputStream getInputStream() {
    return 
      new ByteArrayInputStream(
          mergedBuffer, 0, mergedBufferLength);
  }

  /**
   * Get an InputStream for just standard-out
   * <b>or</b> standard-error.
   *
   * @see #writeTo(OutputStream,boolean)
   * @see #writeTo(OutputStream,OutputStream)
   */
  public InputStream getInputStream(boolean isOut) {
    // not hard to code...  let's wait until there's a need.
    throw new UnsupportedOperationException();
  }

  /**
   * Write both the standard-out <b>and</b> standard-err
   * (interleaved in correct ordering) to the given stream.
   * <p>
   * This is equivalent to fully reading a 
   * <tt>getInputStream()</tt>, but is more efficient.
   */
  public void writeTo(OutputStream os) throws IOException {
    _write(os, 0, mergedBufferLength);
  }

  /**
   * Write either the standard-out <b>or</b> standard-error
   * to the given stream.
   * <p>
   * Note that the ordering information relative to the
   * other (out/err) is ignored, which is fine if you only
   * care about the given (out/err) stream.  See 
   * <tt>writeTo(Stream,Stream)</tt> for an alternative.
   * <p>
   * This is equivalent to fully reading a 
   * <tt>getInputStream(boolean)</tt>, but is more efficient.
   */
  public void writeTo(
      OutputStream os, boolean isOut) throws IOException {
    // based upon "writeTo(OutputStream,OutputStream)"
    // can optimize by loop unrolling, then "i+=2" math...
    // for now this is fine:
    boolean currIsOut = true;
    int currOff = 0;
    for (int i = 0; i < offsetTableLength; i++) {
      int nextOff = offsetTable[i];
      int len = (nextOff - currOff);
      if (currIsOut == isOut) {
        _write(os, currOff, len);
      }
      currIsOut = !currIsOut;
      currOff = nextOff;
    }
  }

  /**
   * Write the standard-out <b>and (separately)</b> the 
   * standard-error to the given streams.
   * <p>
   * Each alternation of (out/err) is allowed to complete
   * its write, which preserves the interleaving.  This is 
   * handy when you need to display both streams in the 
   * correct order.
   */
  public void writeTo(
      OutputStream out,
      OutputStream err) throws IOException {
    boolean currIsOut = true;
    int currOff = 0;
    for (int i = 0; i < offsetTableLength; i++) {
      int nextOff = offsetTable[i];
      int len = (nextOff - currOff);
      if (currIsOut) {
        _write(out, currOff, len);
      } else {
        _write(err, currOff, len);
      }
      currIsOut = !currIsOut;
      currOff = nextOff;
    }
  }

  //
  // Index-based "getters" could be added here, 
  //   e.g. "byte[] getBytes(int index) {}"
  // but (for now) let's keep the API simple.
  // One can always pass a ByteArrayOutputStream...
  //

  /**
   * Write from the "mergedBuffer" to the given stream.
   * <p>
   * May want to alter this code to guarantee that the 
   * stream can't modify the array (e.g. write byte-by-byte
   * instead of passing the array itself).
   */
  private final void _write(
      OutputStream os, int off, int len) throws IOException {
    os.write(mergedBuffer, off, len);
  }

  //
  // serializers:
  //

  /**
   * Save the state of the <tt>DualStreamBuffer</tt> instance to a 
   * stream (that is, serialize it).
   *
   * @serialData the byte[] length, its byte elements, 
   *             the int[] length, and its int elements.
   */
  private void writeObject(
      ObjectOutputStream s) throws IOException {
    // write array lengths
    s.defaultWriteObject();
    // write mergedBuffer array
    s.writeInt(mergedBuffer.length);
    s.write(mergedBuffer, 0, mergedBufferLength);
    // write offsetTable array
    s.writeInt(offsetTable.length);
    for (int i = 0; i < offsetTableLength; i++) {
      s.writeInt(offsetTable[i]);
    }
  }

  /**
   * Reconstitute the <tt>DualStreamBuffer</tt> instance from a 
   * stream (that is, deserialize it).
   */
  private void readObject(
      ObjectInputStream s) throws IOException, 
  ClassNotFoundException {
    // read array lengths
    s.defaultReadObject();
    // read mergedBuffer array
    int mbLen = s.readInt();
    mergedBuffer = new byte[mbLen];
    int len = mergedBufferLength;
    for (int n = 0; n < len; ) {
      int count = s.read(mergedBuffer, n, len - n);
      if (count < 0) {
        throw new EOFException();
      }
      n += count;
    }
    // read offsetTable array
    int otLen = s.readInt();
    offsetTable = new int[otLen];
    for (int i = 0; i < offsetTableLength; i++) {
      offsetTable[i] = s.readInt();
    }
    // validate offsetTable contents?
  }

  /**
   * @return  a clone of this <tt>DualStreamBuffer</tt> instance.
   */
  public Object clone() {
    // since the buffer is add-only we may be able to cheat.
    // However, we want the clone to "trim()" and save memory.
    // For now let's do a real clone:
    try { 
      DualStreamBuffer dsb = (DualStreamBuffer) super.clone();
      dsb.mergedBuffer = new byte[mergedBufferLength];
      System.arraycopy(
          mergedBuffer, 0, dsb.mergedBuffer, 0, mergedBufferLength);
      dsb.offsetTable = new int[offsetTableLength];
      System.arraycopy(
          offsetTable, 0, dsb.offsetTable, 0, offsetTableLength);
      return dsb;
    } catch (CloneNotSupportedException e) { 
      throw new InternalError("never!");
    }
  }

  //
  // Inner classes for stream implementations:
  //

  /**
   * "setter" stream for appending.
   */
  private class InnerOutputStream extends OutputStream {
    private final boolean isOut;

    public InnerOutputStream(boolean isOut) {
      this.isOut = isOut;
    }

    public void write(int b) {
      // append
      int mbIdx = mergedBufferLength;
      growCapacity(1);
      mergedBuffer[mbIdx] = (byte) b;
    }

    public void write(byte b[]) {
      write(b, 0, b.length);
    }

    public void write(byte b[], int off, int len) {
      if (b == null) {
        throw new NullPointerException();
      } else if ((off < 0) || (off > b.length) || (len < 0)) {
        throw new IndexOutOfBoundsException();
      }
      int tail = (off + len);
      if ((tail > b.length) || (tail < 0)) {
        throw new IndexOutOfBoundsException();
      } else if (len == 0) {
        return;
      }
      // append
      int mbIdx = mergedBufferLength;
      growCapacity(len);
      int i = off;
      do {
        mergedBuffer[mbIdx++] = b[i++];
      } while (i < tail);
    }

    // alters lengths and offset-table!
    private void growCapacity(int len) {
      int otIdx = offsetTableLength;
      if (isOut != ((otIdx & 1) == 0)) {
        otIdx--;
      } else {
        offsetTableLength++;
        if (offsetTableLength >= offsetTable.length) {
          // grow offsetTable
          int[] oldOffsetTable = offsetTable;
          offsetTable = new int[((offsetTableLength * 3)/2 + 1)];
          System.arraycopy(
              oldOffsetTable, 0, 
              offsetTable, 0, 
              otIdx);
        }
      }
      int mbIdx = mergedBufferLength;
      mergedBufferLength += len;
      if (mergedBufferLength >= mergedBuffer.length) {
        // grow mergedBuffer
        byte[] oldMergedBuffer = mergedBuffer;
        mergedBuffer = new byte[((mergedBufferLength * 3)/2 + 1)];
        System.arraycopy(oldMergedBuffer, 0, mergedBuffer, 0, mbIdx);
      }
      offsetTable[otIdx] = mergedBufferLength;
    }
  }

  private static final long serialVersionUID = 678782739879123215L;
}
