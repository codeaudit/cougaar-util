/*
 * <copyright>
 *  
 *  Copyright 2004 BBNT Solutions, LLC
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

import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;
import org.cougaar.util.PropertyParser;

/**
 * A utility for requesting a GC in various ways.
 */
public class GC {
  // uninstantiable
  private GC() {}

  /** Static logger for GC **/
  private static Logger log = Logging.getLogger(GC.class);

  /** lock for GC timer **/
  private static final Object gcLock = new Object();

  /** when did we last do a GC, guarded by gcLock **/
  private static long gcTime = 0L;

  public static final long minGCInterval_DEFAULT = 5*60*1000L;
  public static final String minGCInterval_DEFPROP = "org.cougaar.core.persistence.lazyInterval";
  public static final String minGCInterval_PROP = "org.cougaar.util.GC.minGCInterval";
  private static final long minGCInterval = 
    PropertyParser.getLong(minGCInterval_PROP, PropertyParser.getLong(minGCInterval_DEFPROP, minGCInterval_DEFAULT));

  /** Invoke System.gc(), subject to policy constraints:
   * e.g. if it has been at least minGCInterval since
   * the last gc call completed.
   *
   * At INFO logging level, will log when gcs are deferred and allowed.
   * At DEBUG logging lovel, will log how long allowed GCs run for.
   *
   * @property org.cougaar.util.GC.minGCInterval
   * Minimum number of milliseconds between IdentityTable-directed gcs.
   * Defaults to 300000 (5 minutes).  Garbage Collections not invoked
   * through this interface are invisible.  The default value 
   * is actually supplied by the property org.cougaar.core.persistence.lazyInterval,
   * if available.
   **/
  public static final void gc() {
    long now = System.currentTimeMillis();
    synchronized (gcLock) {
      if ((gcTime != 0L) &&
          ((now-gcTime)<minGCInterval)) {
        if (log.isInfoEnabled()) {
          log.info("deferred explicit GC");
        }
        return;
      } 

      System.gc();

      if (log.isInfoEnabled()) { log.info("explicit GC"); }
      long tend = System.currentTimeMillis();
      gcTime = tend;
      if (log.isDebugEnabled()) {
        log.debug("GC.gc() ran for "+(tend-now)+" millis");
      }
    }
  }
}
