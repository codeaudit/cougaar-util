/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.tools.server.rmi;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote RMI-wrapper for an <code>java.io.InputStream</code>.
 *
 * @see java.io.InputStream
 */
public interface ServerInputStream 
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
  public byte[] read(int len) throws IOException, RemoteException;

  //
  // all the other methods of InputStream:
  //

  public int read() throws IOException, RemoteException;
  public long skip(long n) throws IOException, RemoteException;
  public int available() throws IOException, RemoteException;
  public void close() throws IOException, RemoteException;
  public void mark(int readlimit) throws RemoteException;
  public void reset() throws IOException, RemoteException;
  public boolean markSupported() throws RemoteException;

}
