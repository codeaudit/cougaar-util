/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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
 
package org.cougaar.tools.server;

import java.util.Properties;

public final class ProcessDescription 
implements java.io.Serializable, Cloneable 
{

  private final String name;
  private final String group;
  private final Properties javaProps;
  private final String[] commandLineArgs;

  public ProcessDescription(
      String name,
      String group,
      Properties javaProps,
      String[] commandLineArgs) {
    this.name = name;
    this.group = group;
    this.javaProps = javaProps;
    this.commandLineArgs = commandLineArgs;
    // null-check
    if (name == null) {
      throw new NullPointerException("Missing name");
    }
  }

  /**
   * Get the "name" identifier for this process.
   */
  public String getName() {
    return name;
  }

  /**
   * Get the optional "group" identifier for this process.
   * <p>
   * This can be used to tag multiple processes into logical
   * groups.
   */
  public String getGroup() {
    return group;
  }

  /**
   * Get the java properties.
   * <p>
   * All properties fall into three groups: <ul>
   *   <li>"<b>env.*</b>" process environment properties 
   *       (e.g. "env.DISPLAY=..")</li>
   *   <li>"<b>java.*</b>" java options 
   *       (e.g. "java.class.path=..")</li>
   *   <li>"<b>*</b>" for all other "-D" system properties 
   *       (e.g. "foo=bar")</li>
   * </ul><br>
   * Double-prefixes ("env.env.*" and "java.java.*") are 
   * stripped and converted to "-D" properties.  For example,
   * "java.java.duck=quack" is converted to "-Djava.duck=quack".
   * <p>
   * Some properties of note:<ul>
   *   <li>"java.class.name=" to set the classname, which
   *       is required</li>
   *   <li>"java.jvm.program=" to set the java executable
   *       (defaults to "${java.home}/bin/java")</li>
   *   <li>"java.jvm.mode=" to set the JVM mode 
   *       ("classic", "hotspot", "client", or "server");
   *       defaults to "hotspot"</li>
   *   <li>"java.jvm.green=" to add "-green" for green threads,
   *       defaults to false.
   *   <li>"java.class.path=" (note that this *doesn't* adopt
   *       the server's classpath!); defaults to the system-default
   *       classpath</li>
   *   <li>"java.Xbootclasspath[/p|/a]=" to set the bootclasspath.
   *       "java.Xbootclasspath/p=" is used to prefix the default
   *       bootclasspath, or "java.Xbootclasspath/a=" to append.</li>
   *   <li>"java.X*" for other "-X*" properties, such as 
   *       "java.Xmx300m=" for "-Xmx300m"</li>
   * </ul>
   */
  public Properties getJavaProperties() {
    // clone?
    return javaProps;
  }

  /**
   * Get the command-line arguments (in addition to the optional 
   * java-properties), or <tt>null</tt> if there are no additional
   * command-line arguments.
   */
  public String[] getCommandLineArguments() {
    // clone?
    return commandLineArgs;
  }

  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      // never
      throw new InternalError();
    }
  }

  public String toString() { 
    return 
      "Process \""+name+
      "\" of group "+
      ((group != null) ? ("\""+group+"\"") : "(none)");
  }

  public int hashCode() {
    return name.hashCode();
  }
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof ProcessDescription)) {
      return false;
    }
    ProcessDescription pd = (ProcessDescription) o;
    if (!(name.equals(pd.name))) {
      return false;
    }
    if ((group != null) ?
        (!(group.equals(pd.group))) :
        (pd.group != null)) {
      return false;
    }
    if ((javaProps != null) ?
        (!(javaProps.equals(pd.javaProps))) :
        (pd.javaProps != null)) {
      return false;
    }
    if (commandLineArgs != null) {
      if (pd.commandLineArgs == null) {
        return false;
      }
      int len = commandLineArgs.length;
      if (pd.commandLineArgs.length != len) {
        return false;
      }
      for (int i = 0; i < len; i++) {
        String ai = commandLineArgs[i];
        String bi = pd.commandLineArgs[i];
        if ((ai != null) ?
            (!(ai.equals(bi))) :
            (bi != null)) {
          return false;
        }
      }
    } else if (pd.commandLineArgs != null) {
      return false;
    }
    return true;
  }

  private static final long serialVersionUID = 1209381608235681283L;
}
