/*
 * <copyright>
 *  Copyright 2000-2003 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */
package org.cougaar.core.component;

import org.cougaar.util.GenericStateModelAdapter;

import java.util.Properties;
import java.util.Iterator;
import java.util.List;

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
