/*
 * <copyright>
 *  
 *  Copyright 2002-2004 BBNT Solutions, LLC
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

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An LRUExpireMap is a simple least-recently-used cache that also
 * expires entries based on the system time.
 * <p>
 * Entries are put in the map with an explicit expiration time.  The
 * cache will not return expired entries and will remove them
 * automatically.  The cache also has a maximum size to limit the
 * number of non-expired entries.
 * <p>
 * The caller must synchronized access on this map.
 * <p>
 * If you just want a simple LRU cache then see the javadocs for
 * <tt>LinkedHashMap</tt>, where you can override the
 * "removeEldestEntry(..)" method.
 */
public class LRUExpireMap extends LinkedHashMap {

  /**
    * 
    */
   private static final long serialVersionUID = 1L;

/** configuration controller */
  public interface Config {
    /** the initial size for the map */
    int initialSize();
    /** the maximum cache size */
    int maxSize();
    /** the minimal entry lifetime if the get-bypass is used */
    long minBypassTime();
  }

  /** optional observer for logging/statistics */
  public interface Watcher {
    /** called upon removal of an expired entry */
    void noteExpire(
        Object key, Object value, long putTime, long expireTime);
    /** called when the non-expired LRU is evicted due to overfill */
    void noteEvict(
        Object key, Object value, long putTime, long expireTime);
    /** called at the end of "trim()" */
    void noteTrim(int nfreed, int origSize);
  }

  protected final Config config;
  protected final Watcher watcher;

  public LRUExpireMap(Config config, Watcher watcher) {
    super(config.initialSize(), 0.75f, true);
    this.config = config;
    this.watcher = watcher;
  }

  @Override
public Object get(Object key) {
    return get(key, false);
  }

  /**
   * Get a value with an optional "bypass the cache" flag".
   * <p>
   * The config's "minBypassTime()" is consulted if the
   * entry has not expired.  This allows the config to
   * limit the bypass to a fixed time from the "put(..)"
   * of the entry (e.g. "you can only bypass if the
   * entry is older than 5 seconds").
   *
   * @return the value
   */
  public Object get(Object key, boolean bypass) {
    Expirable eo = getExpirable(key, bypass);
    return (eo == null ? null : eo.value);
  }

  /**
   * Get the expiration time for an entry.
   *
   * @return -1 if the entry is not in the cache or has expired.
   */
  public long getExpirationTime(Object key) {
    Expirable eo = getExpirable(key, false);
    return (eo == null ? -1 : eo.expireTime);
  }

  protected Expirable getExpirable(Object key) {
    return getExpirable(key, false);
  }

  protected Expirable getExpirable(Object key, boolean bypass) {
    Expirable ret = null;
    Expirable eo = (Expirable) super.get(key);
    if (eo != null) {
      long now = System.currentTimeMillis();
      if (eo.expireTime < now) {
        // expired
        if (watcher != null) {
          watcher.noteExpire(
              key,
              eo.value,
              eo.putTime,
              eo.expireTime);
        }
        remove(key);
      } else if (
          bypass && 
          (eo.putTime + config.minBypassTime() < now)) {
        // valid, but the user wants to bypass the cache
        // and this entry has been around for a little while
      } else {
        // valid but maybe null
        ret = eo;
      }
    }
    return ret;
  }

  /**
   * @throws UnsupportedOperationException 
   *   Must specify an expiration time
   */
  @Override
public Object put(Object key, Object value) {
    throw new UnsupportedOperationException(
        "Must specify an expiration time");
  }

  /**
   * Put an entry in the cache with an expiration time.
   * <p>
   * If it has already expired, this is method removes the old
   * value (even if it hasn't expired) and ignores the new value.
   *
   * @return the old value if one was replaced
   */
  public Object put(Object key, Object value, long expireTime) {
    Expirable oldEO = null;
    long now = System.currentTimeMillis();
    if (expireTime <= now) {
      oldEO = (Expirable) super.remove(key);
    } else {
      Expirable eo = new Expirable(value, now, expireTime);
      oldEO = (Expirable) super.put(key, eo);
    }
    return (oldEO == null ? null : oldEO.value);
  }

  /**
   * Called by the LinkedHashMap when an entry is added,
   * this allows the cache to remove the LRU "eldest" entry.
   */
  @Override
protected boolean removeEldestEntry(Map.Entry eldest) {
    // check size
    int origSize = size();
    if (origSize < config.maxSize()) {
      // plenty of room left...
      return false;
    }
    long now = System.currentTimeMillis();
    // quick-check for an expired eldest
    Expirable eo = 
      (eldest == null ?
       (null) :
       (Expirable) eldest.getValue());
    if (eo != null && eo.expireTime < now) {
      // the eldest has expired, so just remove it
      if (watcher != null) {
        watcher.noteExpire(
            eldest.getKey(),
            eo.value,
            eo.putTime,
            eo.expireTime);
      }
      return true;
    }
    if (trim()) {
      // removed something, so leave the eldest alone
      return false;
    }
    // remove the eldest entry
    if (eo != null && watcher != null) {
      watcher.noteEvict(
          eldest.getKey(),
          eo.value,
          eo.putTime,
          eo.expireTime);
    }
    return true;
  }

  /**
   * Remove all expired entries from the cache.
   * <p>
   * This can optionally be called on a periodic timer, but
   * it's not necessary -- the cache will limit its size based
   * upon the config's "maxSize()".
   *
   * @return true if anything was removed
   */
  public boolean trim() {
    int origSize = size();
    if (origSize <= 0) {
      return false;
    }
    int nfreed = 0;
    long now = System.currentTimeMillis();
    for (Iterator iter = entrySet().iterator();
        iter.hasNext();
        ) {
      Map.Entry me = (Map.Entry) iter.next();
      Expirable eo = (Expirable) me.getValue();
      if (eo.expireTime < now) {
        if (watcher != null) {
          watcher.noteExpire(
              me.getKey(),
              eo.value,
              eo.putTime,
              eo.expireTime);
        }
        iter.remove();
        ++nfreed;
      }
    }
    if (watcher != null) {
      watcher.noteTrim(nfreed, origSize);
    }
    return (nfreed > 0);
  }

  /**
   * All entries are of this type.
   * <p>
   * Typically this isn't visible, but an iterator
   * will see it...
   */
  public static class Expirable 
    implements Serializable {
      /**
    * 
    */
   private static final long serialVersionUID = 1L;
      public final Object value;
      public final long putTime;
      public final long expireTime;
      public Expirable(
          Object value,
          long now,
          long expireTime) {
        this.value = value;
        this.putTime = now;
        this.expireTime = expireTime;
        if (expireTime <= now) {
          throw new IllegalArgumentException(
              "Expire time ("+expireTime+
              ") must be >= to the current time ("+
              now+")");
        }
      }
      @Override
      public String toString() {
        return 
          "(updated="+putTime+
          " expires="+expireTime+
          " value="+value+
          ")";
      }
    }

}
