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
 * Additional math functions.
 **/
public class MoreMath {

  /**
   * Returns the value ln[gamma(xx)] for xx > 0. Full accuracy is
   * obtained for xx > 1. For 0 < xx < 1, the reflection formula (6.1.4)
   * can be used first. Internal arithmetic will be done in double
   * precision, a nicety that you can omit if five figure accuracy is
   * good enough.
   * Algorithm from "Recipes for Scientific Computing"
   **/
  public static double gammaLn(double xx) {
    double x = xx - 1.0;
    double tmp = x + 5.5;
    tmp = (x + 0.5) * Math.log(tmp) - tmp;
    double ser = 1.0;
    ser += 76.18009173e0 / (x += 1.0);
    ser -= 86.50532033e0 / (x += 1.0);
    ser += 24.01409822e0 / (x += 1.0);
    ser -= 1.231739516e0 / (x += 1.0);
    ser += .120858003e-2 / (x += 1.0);
    ser -= 0.536382e-5   / (x += 1.0);
    return tmp + Math.log(2.50662827465e0 * ser);
  }

  /**
   * Compares two doubles for near equality. Equality comparisons of
   * doubles is instrinsically difficult because of rounding. This
   * methods checks the relative difference between two doubles and
   * their values.
   * @param a one of the values to compare
   * @param b the other value to compare
   * @return true if the difference is less than 1 part per billion
   **/
  public static boolean nearlyEquals(double a, double b) {
    if (a == b) return true;
    return Math.abs(a - b) / (Math.abs(a) + Math.abs(b)) < .5e-9;
  }

  /**
   * Compares two doubles for near equality. Equality comparisons of
   * doubles is instrinsically difficult because of rounding. This
   * methods checks the relative difference between two doubles and
   * their values.
   * @param a one of the values to compare
   * @param b the other value to compare
   * @param tolerance the allowable difference
   * @return true if the difference divided by the sum of the absolute
   * values of the parameters is less than the tolerance
   **/
  public static boolean nearlyEquals(double a, double b, double tolerance) {
    if (a == b) return true;
    return Math.abs(a - b) / (Math.abs(a) + Math.abs(b)) < tolerance;
  }
}
