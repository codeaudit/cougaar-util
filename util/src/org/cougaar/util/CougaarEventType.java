/*
 * Created by IntelliJ IDEA.
 * User: Richard Kilmer (rich@infoether.com)
 * Date: Jul 12, 2002
 * Time: 8:56:40 AM
 *
 * <copyright>
 *  Copyright 2002-2003 BBNT Solutions, LLC
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
package org.cougaar.util;

import java.io.Serializable;

/**
 * The CougaarEventType class encapsulates a string that indicates the type
 * of event.  Several predefined event types are located on this class
 * as statics.
 */
public class CougaarEventType implements Serializable {
    private String type;

    /**
     * The start of an event
     */
    public static final CougaarEventType START = new CougaarEventType("START");
    /**
     * The end of some event
     */
    public static final CougaarEventType END = new CougaarEventType("END");
    /**
     * A stressor is enabled
     */
    public static final CougaarEventType STRESSOR_ENABLE = new CougaarEventType("STRESSOR_ENABLE");
    /**
     * A stressor is disabled
     */
    public static final CougaarEventType STRESSOR_DISABLE = new CougaarEventType("STRESSOR_DISABLE");
    /**
     * A status event (message)
     */
    public static final CougaarEventType STATUS = new CougaarEventType("STATUS");
    /**
     * A general event
     */
    public static final CougaarEventType GENERIC = new CougaarEventType("GENERIC");

    /**
     * Constructs an Event Type from the supplied type string
     * @param type The type name
     */
    public CougaarEventType(String type) {
        this.type = type;
    }

    /**
     * Gets the type
     * @return The type string
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type
     * @param type The type string
     */
  /*
    public void setType(String type) {
        this.type = type;
    }
  */
}
