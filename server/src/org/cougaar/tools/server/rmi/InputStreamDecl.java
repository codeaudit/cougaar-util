/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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

package org.cougaar.tools.server.rmi;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote RMI-wrapper for an InputStream.
 *
 * @see java.io.InputStream
 */
interface InputStreamDecl 
extends Remote {

  /**
   * Special "read(byte[])" equivalent for RMI.
   * <p>
   * This replaces:<ul>
   *   <li>public int read(byte[] b) throws IOException</li>
   *   <li>public int read(byte[] b, int off, int len) throws IOException</li>
   * </ul>
   * The typical "read(byte[] ..)" methods will not work for RMI, since
   * the result must be serialized back as a separate Object.
   *
   * @param len number of bytes to read
   *
   * @return a byte[] of data, or null if the end of the stream has been
   *   reached
   */
  byte[] read(int len) throws IOException, RemoteException;

  //
  // all the other methods of InputStream:
  //

  int read() throws IOException, RemoteException;
  long skip(long n) throws IOException, RemoteException;
  int available() throws IOException, RemoteException;
  void close() throws IOException, RemoteException;
  void mark(int readlimit) throws RemoteException;
  void reset() throws IOException, RemoteException;
  boolean markSupported() throws RemoteException;
}
