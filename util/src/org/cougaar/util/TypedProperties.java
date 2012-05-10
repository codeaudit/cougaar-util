/*
 * <copyright>
 *  
 *  Copyright 2001-2004 BBNT Solutions, LLC
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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

public class TypedProperties {
	
    private final Properties props;
    private final Logger log;
    
    public TypedProperties(Properties p) {
        props = p;
        log = Logging.getLogger(getClass().getName());
    }

    public Properties getProperties() {
        return props;
    }

    public String getProperty(String name) {
        return getString(name, null);
    }

    public String getString(String name, String def) {
        if (props != null)
            return props.getProperty(name, def);
        return def;
    }

    public int getInt(String name, int def) {
        if (props != null) {
            try {
                String val = props.getProperty(name);
                if (val == null)
                    return def;
                return Integer.parseInt(val);
            } catch (NumberFormatException nfe) {
                log.warn("Expected integer in property: " + name);
            }
        }
        return def;
    }

    public long getLong(String name, long def) {
        if (props != null) {
            try {
                String val = props.getProperty(name);
                if (val == null)
                    return def;
                return Long.parseLong(val);
            } catch (NumberFormatException nfe) {
                log.warn("Expected long in property: " + name);
            }
        }
        return def;
    }

    public float getFloat(String name, float def) {
        if (props != null) {
            try {
                String val = props.getProperty(name);
                if (val == null)
                    return def;
                return Float.parseFloat(val);
            } catch (NumberFormatException nfe) {
                log.warn("Expected float in property: " + name);
            }
        }
        return def;
    }

    public double getDouble(String name, double def) {
        if (props != null) {
            try {
                String val = props.getProperty(name);
                if (val == null)
                    return def;
                return Double.parseDouble(val);
            } catch (NumberFormatException nfe) {
                log.warn("Expected double in property: " + name);
            }
        }
        return def;
    }

    public boolean getBoolean(String name, boolean def) {
        if (props != null) {
            String val = props.getProperty(name);
            if (val == null)
                return def;
            val = val.toLowerCase();
            if (val.equals("1") || val.equals("y") || val.equals("yes")
                    || val.equals("t") || val.equals("true"))
                return true;
            else if (val.equals("0") || val.equals("n") || val.equals("no")
                    || val.equals("f") || val.equals("false"))
                return false;
            else
                log.warn("Expected boolean in property: " + name);
        }
        return def;
    }

    public InetAddress getInetAddress(String name, InetAddress def) {
        if (props != null) {
            String val = props.getProperty(name);
            if (val != null) {
                try {
                    return InetAddress.getByName(val);
                } catch (UnknownHostException e) {
                    log.warn("Could not interpret: " + val
                            + " as a valid hostname.");
                    return def;
                }
            }
        }
        return def;
    }

}
