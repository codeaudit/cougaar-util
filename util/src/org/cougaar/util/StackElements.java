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

/**
 * Wrapper around a Throwable for stack-based equality.
 */
public final class StackElements {

  private final Throwable throwable;
  private StackTraceElement[] elements;
  private int _hc;

  public StackElements(Throwable throwable) {
    this.throwable = throwable;
    if (throwable == null) {
      throw new IllegalArgumentException("null throwable");
    }
  }

  public Throwable getThrowable() {
    return throwable;
  }

  private StackTraceElement[] getStackTrace() {
    // cache the array, otherwise each access is a clone
    if (elements == null) {
      elements = throwable.getStackTrace();
    }
    return elements;
  }

  public int hashCode() {
    if (_hc == 0) {
      _hc = 1;
      StackTraceElement[] st = getStackTrace();
      for (int i = 0, n = st.length; i < n; i++) {
        _hc = 31 * _hc + st[i].hashCode();
      }
    }
    return _hc;
  }

  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof StackElements)) {
      return false;
    }
    StackTraceElement[] a_st = getStackTrace();
    StackTraceElement[] b_st = ((StackElements) o).getStackTrace();
    if (a_st.length != b_st.length) {
      return false;
    }
    for (int i = 0, n = a_st.length; i < n; i++) {
      if (!a_st[i].equals(b_st[i])) {
        return false;
      }
    }
    return true;
  }

  public String toString() {
    return throwable.toString();
  }
}
