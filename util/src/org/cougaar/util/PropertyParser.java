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

import java.util.Properties;
import org.cougaar.bootstrap.SystemProperties;

/**
 * Utility for parsing Properties values of various types with defaults
 * @see SystemProperties
 */
public abstract class PropertyParser {
  public static final boolean getBoolean(Properties props, String prop, boolean def) {
    return (Boolean.valueOf(props.getProperty(prop, String.valueOf(def)))).booleanValue();
  }

  public static final boolean getBoolean(String prop, boolean def) {
    return SystemProperties.getBoolean(prop, def);
  }

  public static final int getInt(Properties props, String prop, int def) {
    try {
      return Integer.parseInt(props.getProperty(prop, String.valueOf(def)));
    } catch (NumberFormatException e) {
      return def;
    }
  }
  public static final int getInt(String prop, int def) {
    return SystemProperties.getInt(prop, def, true);
  }

  public static final long getLong(Properties props, String prop, long def) {
    try {
      return Long.parseLong(props.getProperty(prop, String.valueOf(def)));
    } catch (NumberFormatException e) {
      return def;
    }

  }
  public static final long getLong(String prop, long def) {
    return SystemProperties.getLong(prop, def, true);
  }

  public static final float getFloat(Properties props, String prop, float def) {
    try {
      return Float.parseFloat(props.getProperty(prop, String.valueOf(def)));
    } catch (NumberFormatException e) {
      return def;
    }
  }
  public static final float getFloat(String prop, float def) {
    return SystemProperties.getFloat(prop, def, true);
  }

  public static final double getDouble(Properties props, String prop, double def) {
    try {
      return Double.parseDouble(props.getProperty(prop, String.valueOf(def)));
    } catch (NumberFormatException e) {
      return def;
    }
  }
  public static final double getDouble(String prop, double def) {
    return SystemProperties.getDouble(prop, def, true);
  }
}
