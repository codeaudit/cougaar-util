/*
 * <copyright>
 *  
 *  Copyright 1997-2007 BBNT Solutions, LLC
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
/*
  File: EDU/oswego/cs/dl/util/concurrent/FutureResult.java

  Originally written by Doug Lea and released into the public domain.
  This may be used for any purposes whatsoever without acknowledgment.
  Thanks for the assistance and support of Sun Microsystems Labs,
  and everyone contributing, testing, and using this code.

  History:
  Date       Who                What
  30Jun1998  dl               Create public version
*/
package org.cougaar.util;

// CHANGES:
//   - Changed package to "org.cougaar.util"
//   - Changed code style (e.g. use braces)
//   - Changed javadoc
//   - Made TimeoutException a static inner class

import java.lang.reflect.InvocationTargetException;

/**
 * A container that allows one thread to wait for the processing 
 * result of a parallel thread.
 * <p>
 * Typically one thread calls "get()" to block for the result, and another
 * thread, possibly queued, does the work and eventually calls "set(...)"
 * to unblock the waiter.
 * <p>
 * This is a copy of Doug Lea's public domain "FutureResult" class:<br>
 * &nbsp;&nbsp; EDU/oswego/cs/dl/util/concurrent/FutureResult.java<br>
 * For some reason it was not included in JDK 1.5's "java.util.concurrent"
 * package.
 */
public class FutureResult {

  protected Object value_ = null;
  protected boolean ready_ = false;
  protected InvocationTargetException exception_ = null;

  public FutureResult() { }

  /** Wait until the result is ready and get it */
  public synchronized Object get() 
    throws InterruptedException, InvocationTargetException {
      while (!ready_) { wait(); }
      return doGet();
    }

  /**
   * Wait at most "msecs" milliseconds for the result, otherwise
   * throw a TimeoutException.
   */
  public synchronized Object timedGet(long msecs) 
    throws TimeoutException, InterruptedException, InvocationTargetException {
      if (ready_) return doGet();
      if (msecs <= 0) throw new TimeoutException(msecs);
      long startTime = System.currentTimeMillis();
      long waitTime = msecs;
      while (true) {
        wait(waitTime);
        if (ready_) return doGet();
        waitTime = msecs - (System.currentTimeMillis() - startTime);
        if (waitTime <= 0) throw new TimeoutException(msecs);
      }
    }

  /** Set a non-exception result and notify the waiters */
  public synchronized void set(Object newValue) {
    value_ = newValue;
    ready_ = true;
    notifyAll();
  }

  /** Set an exception result and notify the waiters. */
  public synchronized void setException(Throwable ex) {
    exception_ = new InvocationTargetException(ex);
    ready_ = true;
    notifyAll();
  }

  public synchronized boolean isReady() { return ready_; }
  public synchronized Object peek() { return value_; }
  public synchronized InvocationTargetException getException() {
    return exception_;
  }

  public synchronized void clear() {
    value_ = null;
    exception_ = null;
    ready_ = false;
  }

  protected Object doGet() throws InvocationTargetException {
    if (exception_ != null) throw exception_;
    return value_; 
  }

  public static class TimeoutException extends InterruptedException {
    private final long duration;
    public TimeoutException(long time) { duration = time; }
    public TimeoutException(long time, String message) {
      super(message);
      duration = time;
    }
    public long getDuration() { return duration; }
  }
}
