/*
 * Created by IntelliJ IDEA.
 * User: Richard Kilmer (rich@infoether.com)
 * Date: Jul 12, 2002
 * Time: 8:56:29 AM
 *
 * <copyright>
 *  Copyright 2002 BBNT Solutions, LLC
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

import org.cougaar.util.log.Logging;

import java.io.Serializable;

/**
 * The CougaarEvent class is a helper class to construct an event string
 * and output that event to a pre-defined destination that is currently the
 * Logging service of Cougaar.  The class is composed of static methods that
 * can be called to publish the event:<br>
 * <br>
 * Example:<br>
 * <code>CougaarEvent.postEvent(CougaarEventType.STATUS, "Status update from FooBar");</code><br>
 * <br>
 * The current Logging service transport for events logs at the "SHOUT" level.
 */
public class CougaarEvent implements Serializable {

    private CougaarEventType type;
    private String clusterIdentifier;
    private String component;
    private String eventText;
    private boolean encoded;

    /**
     * Constructs a CougaarEvent object.  This constructor should be used to build an instance of
     * an event that should be published to the blackboard instead of outputting to the default
     * output method (Logger).
     *
     * @param type The type of event
     * @param clusterIdentifier The name of the Agent (Cluster)
     * @param component The name of the component
     * @param eventText The text to event (this text will be XML encoded)
     * @param encoded True if the eventText is XML encoded (or valid)
     */
    public CougaarEvent(CougaarEventType type, String clusterIdentifier, String component, String eventText, boolean encoded) {
        this.type = type;
        this.clusterIdentifier = clusterIdentifier;
        this.component = component;
        this.eventText = eventText;
        this.encoded = encoded;
    }

    public CougaarEventType getType() {
        return type;
    }

    public String getMessageAddress() {
        return clusterIdentifier;
    }

    public String getComponent() {
        return component;
    }

    public String getEventText() {
        return eventText;
    }

    public boolean isEncoded() {
        return encoded;
    }

    /**
     * Posts a CougaarEvent instance to the event output method.  This is normally used
     * when an instance of a CougaarEvent is published to the blackboard and needs to be
     * output to the event output method (Logger, etc).
     *
     * @param event The event to post.
     */
    public static void postComponentEvent(CougaarEvent event) {
        Logging.defaultLogger().shout(
                CougaarEventGenerator.generateEventString(event.getType(),
                        event.getMessageAddress(), event.getComponent(),
                        event.getEventText(), event.isEncoded()));
    }

    /**
     * Posts an event from a Cougaar Agent component.  This method posts directly Event output
     * method and bypasses adding anything to the blackboard.  If this event should pass through
     * the blackboard, consider using postBlackboardEvent instead. 
     *
     * @param type The type of event
     * @param clusterIdentifier The name of the Agent (Cluster)
     * @param component The name of the component
     * @param eventText The text to event (this text will be XML encoded)
     */
    public static void postComponentEvent(CougaarEventType type, String clusterIdentifier, String component, String eventText) {
        Logging.defaultLogger().shout(CougaarEventGenerator.generateEventString(type, clusterIdentifier, component, eventText, false));
    }

    /**
     * Posts an event from an object that is not a component (such as infrastructure or helper classes)
     *
     * @param type The type of event
     * @param eventText The text to event (this text will be XML encoded)
     */
    public static void postEvent(CougaarEventType type, String eventText) {
        Logging.defaultLogger().shout(CougaarEventGenerator.generateEventString(type, null, null, eventText, false));
    }

    /**
     * Posts an event from a Cougaar Agent component.  This method posts directly Event output
     * method and bypasses adding anything to the blackboard.  If this event should pass through
     * the blackboard, consider using postBlackboardEvent instead.  This event is assumed to be
     * valid XML text (with escaped < as &amp;lt; and & as &amp;amp;).
     *
     * @param type The type of event
     * @param clusterIdentifier The name of the Agent (Cluster)
     * @param component The name of the component
     * @param eventText The text to event (XML valid...possibly XML itself)
     */
    public static void postComponentEventEncoded(CougaarEventType type, String clusterIdentifier, String component, String eventText) {
        Logging.defaultLogger().shout(CougaarEventGenerator.generateEventString(type, clusterIdentifier, component, eventText, true));
    }

    /**
     * Posts an event from an object that is not a component (such as infrastructure or helper classes).
     * This event is assumed to be valid XML text (with escaped < as &amp;lt; and & as &amp;amp;).
     *
     * @param type The type of event
     * @param eventText The text to event (XML valid...possibly XML itself)
     */
    public static void postEventEncoded(CougaarEventType type, String eventText) {
        Logging.defaultLogger().shout(CougaarEventGenerator.generateEventString(type, null, null, eventText, true));
    }

    /**
     * Handles transforming event string to XML.  This class is only used by the CougaarEvent class
     * and should not be used directly. 
     */
    public static class CougaarEventGenerator {

        /**
         * Returns an XML string that encodes the supplied parameters. 
         * @param type The Cougaar event type
         * @param clusterIdentifier The cluster identifier (can be null)
         * @param component The component name (can be null)
         * @param eventText The text of the event
         * @param encoded If true, the eventText is assumed to be pre-encoded)
         * @return String representation of the Event in XML
         */
        public static String generateEventString(CougaarEventType type, String clusterIdentifier, String component, String eventText, boolean encoded) {
            StringBuffer event = new StringBuffer();
            event.append("<CougaarEvent type=\"").append(type.getType());
            if (clusterIdentifier != null)
                event.append("\" clusterIdentifier=\"").append(clusterIdentifier);
            if (component != null)
                event.append("\" component=\"").append(component);
            event.append("\">");
            if (encoded) {
                event.append(eventText);
            } else {
                event.append(encodeXML(eventText));
            }
            event.append("</CougaarEvent>");
            return event.toString();
        }

        /**
         * Replaces all & with &amp;amp; and < with &lt;lt;
         * @param text to encode
         * @return XML encoding of given string
         */
        public static String encodeXML(String text) {
            text = text.replaceAll("&", "&amp;");
            return text.replaceAll("<", "&lt;");
        }

    }

}
