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

/** LockFlag class adapted from sample in   "Java Threads"
 * -- Scott Oaks and Henry Wong.  O'Reilly, Jan 1997
 **/

public class LockFlag {
  private Thread busyflag = null;
  private int busycount = 0;

  private static LockFlag defaultLock = new LockFlag();

  /** Returns a single VM-scoped LockFlag instance which can
   * be shared amongst many threads
   **/
  public static LockFlag getDefaultLock()
  {
    return defaultLock;
  }

  public synchronized void getBusyFlag()
  {
    while (tryGetBusyFlag() == false) {
      try {
        wait();
      } catch (Exception e) {}
    }
  }

  public synchronized boolean tryGetBusyFlag()
  {
    if ( busyflag == null ) {
      busyflag = Thread.currentThread();
      busycount = 1;
      return true;
    }
    if ( busyflag == Thread.currentThread() ) {
      busycount++;
      return true;
    }
    return false;
  }

  /** @return true on success.
   **/
  public synchronized boolean freeBusyFlag()
  {
    if ( getBusyFlagOwner() == Thread.currentThread()) {
      busycount--;
      if( busycount == 0 ) {
        busyflag = null;
        notify();
      }
      return true;
    } else {
      return false;
    }
  }

  public synchronized Thread getBusyFlagOwner()
  {
    return busyflag;
  }

  public int getBusyCount()
  {
    return busycount;
  }

}
