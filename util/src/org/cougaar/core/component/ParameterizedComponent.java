/*
 * <copyright>
 *  
 *  Copyright 2000-2004 BBNT Solutions, LLC
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
package org.cougaar.core.component;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.cougaar.util.GenericStateModelAdapter;

/**
 * Simple Component base class that provides named paramater support.
 * Parameters should take the form <var>=<value>.
 */
abstract public class ParameterizedComponent
    extends GenericStateModelAdapter
    implements Component
{
    private Properties parameters;

    public String getParameter(String key) {
	return getParameter(key, null);
    }

    public String getParameter(String key, String defaultValue) {
	if (parameters != null)
	    return parameters.getProperty(key, defaultValue);
	else
	    return defaultValue;
    }

    public long getParameter(String key, long defaultValue) {
	String spec = getParameter(key);
	if (spec != null) {
	    try { return Long.parseLong(spec); }
	    catch (NumberFormatException ex) { return defaultValue; }
	} else {
	    return defaultValue;
	}
    }

    public double getParameter(String key, double defaultValue) {
	String spec = getParameter(key);
	if (spec != null) {
	    try { return Double.parseDouble(spec); }
	    catch (NumberFormatException ex) { return defaultValue; }
	} else {
	    return defaultValue;
	}
    }

    public void setParameter(Object param) {
	parameters = new Properties();
	if (param instanceof List) {
	    Iterator itr = ((List) param).iterator();
	    while(itr.hasNext()) {
		String property = (String) itr.next();
		int sepr = property.indexOf('=');
		if (sepr < 0) continue;
		String key = property.substring(0, sepr);
		String value = property.substring(++sepr);
		parameters.setProperty(key, value);
	    }
	}
    }

}
