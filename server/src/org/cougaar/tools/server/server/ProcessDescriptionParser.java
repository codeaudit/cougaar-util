/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

package org.cougaar.tools.server.server;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cougaar.tools.server.ProcessDescription;

/** 
 */
class ProcessDescriptionParser {

  private final ProcessDescription pd;
  private final Map defaultJavaProps;

  private String[] cmdLine;
  private String[] envVars;

  public ProcessDescriptionParser(
      ProcessDescription pd,
      Map defaultJavaProps) {
    this.pd = pd;
    this.defaultJavaProps = defaultJavaProps;
    if (pd == null) {
      throw new NullPointerException();
    }
  }

  public void parse() {
    String[][] tmp = 
      parse0(pd, defaultJavaProps);
    this.cmdLine = tmp[0];
    this.envVars = tmp[1];
  }

  public String[] getCommandLine() {
    return cmdLine;
  }

  public String[] getEnvironmentVariables() {
    return envVars;
  }

  /**
   * Parse and expand the given ProcessDescription to create
   * a full command-line and environment-variables.
   * <p>
   * This will probably be refactored and split into an
   * interface/implementation pair, plus security features
   * will be added.  For now this is separate just for clarity.
   * 
   * @return a String[2], where String[0] is a String[] of 
   *    command-line arguments and String[1] is a String[] of
   *    environment variables.
   */
  private static final String[][] parse0(
      ProcessDescription pd,
      Map defaultJavaProps) {
    String[] cmdLine;
    String[] envVars;

    ArrayList cmdList = new ArrayList();
    ArrayList envList = new ArrayList();

    Map descProps = pd.getJavaProperties();
    if ((defaultJavaProps != null) ||
        ((descProps != null) && 
         (descProps.size() > 0))) {
      // merge the default and passed-in props
      Map allProps = new HashMap();
      if (defaultJavaProps != null) {
        allProps.putAll(defaultJavaProps);
      }
      if (descProps != null) {
        allProps.putAll(descProps);
      }

      // split all properties into three groups:
      //   "env.*"  environment properties (e.g. "env.DISPLAY=..")
      //   "java.*" java options (e.g. "java.class.path=..")
      //   "*"      all other "-D" system properties (e.g. "foo=bar")

      Map envProps = new HashMap();
      Map javaProps = new HashMap();
      Map sysProps = new HashMap();

      for (Iterator iter = allProps.entrySet().iterator();
          iter.hasNext();
          ) {
        // take the next property
        Map.Entry me = (Map.Entry) iter.next();
        Object ovalue = me.getValue();
        if (ovalue == null) {
          continue;
        }
        Object oname = me.getKey();

        // cast and trim
        String name = ((String) oname).trim();
        String value = ((String) ovalue).trim();

        // SECURITY -- scan for illegal characters
        //
        // for now just test the value as an example
        for (int n = value.length() - 1; n >= 0; n--) {
          char ch = value.charAt(n);
          // add tests here!
          if ((ch == ' ') ||
              (ch == '\t') ||
              (ch == '\n')) {
            throw new IllegalArgumentException(
                "Property name \""+name+
                "\" contains illegal whitespace ("+((int) ch)+
                ") in value: \""+value+"\"");
          }
        }

        if (name.startsWith("env.")) {
          name = name.substring(4);
          if (!(name.startsWith("env."))) {
            envProps.put(name, value);
            continue;
          }
        } else if (name.startsWith("java.")) {
          name = name.substring(5);
          if (!(name.startsWith("java."))) {
            javaProps.put(name, value);
            continue;
          }
        }
        sysProps.put(name, value);
      }

      if ((!(javaProps.isEmpty())) ||
          (!(sysProps.isEmpty()))) {
        // select "java" executable
        String jvmProgram = 
          (String) javaProps.remove("jvm.program");
        if (jvmProgram != null) {
          // SECURITY -- guard against "jvm.program=rm"!!!
          // use name as-is
        } else {
          String javaHome = System.getProperty("java.home");
          if (javaHome != null) {
            // defaults to "{java.home}/bin/java"
            jvmProgram = 
              javaHome+
              File.separator+
              "bin"+
              File.separator+
              "java";
          } else {
            jvmProgram = "java";
          }
        }

        cmdList.add(jvmProgram);

        // check for an executable jar
        String jar = 
          (String) javaProps.remove("jar");
        if (jar != null) {
          if (jar.length() == 0) {
            throw new IllegalArgumentException(
                "Classpath must be non-empty");
          }
          // SECURITY -- examine jar
          //
          // specify the jar later
          cmdList.add("-jar");
        }

        // check for JVM mode ("classic", "hotspot", "client", 
        //   or "server")
        String jvmMode = 
          (String) javaProps.remove("jvm.mode");
        if (jvmMode != null) {
          if ((jvmMode.equals("classic")) ||
              (jvmMode.equals("hotspot")) ||
              (jvmMode.equals("client")) ||
              (jvmMode.equals("server"))) {
            cmdList.add("-"+jvmMode);
          } else {
            throw new IllegalArgumentException(
                "Illegal \"jvm.mode="+jvmMode+"\"");
          }
        }

        // check for green threads
        String jvmGreen = 
          (String) javaProps.remove("jvm.green");
        if (jvmGreen != null) {
          if (jvmGreen.equals("true")) {
            cmdList.add("-green");
          }
        }

        // check for a classpath IFF no executable jar
        String classpath = 
          (String) javaProps.remove("class.path");
        if (classpath != null) {
          if (jar != null) {
            // warning: ignoring classpath!
          } else {
            if (classpath.length() == 0) {
              throw new IllegalArgumentException(
                  "Classpath must be non-empty");
            }
            // SECURITY -- examine path
            cmdList.add("-classpath");
            cmdList.add(classpath);
          }
        }

        // check for "Xbootclasspath"s
        for (Iterator iter = javaProps.entrySet().iterator();
            iter.hasNext();
            ) {
          Map.Entry me = (Map.Entry) iter.next();
          String name = (String) me.getKey();
          if (name.startsWith("Xbootclasspath")) {
            iter.remove();
            // expecting name to be one of:
            //   Xbootclasspath
            //   Xbootclasspath/a
            //   Xbootclasspath/p
            String nameTail = 
              name.substring("Xbootclasspath".length());
            if (nameTail.equals("") ||
                nameTail.equals("/a") ||
                nameTail.equals("/p")) {
              // valid
            } else {
              throw new IllegalArgumentException(
                  "Expecting \"Xbootclasspath[|/a|/p]\", not \""+
                  name+"\"");
            }
            String value = (String) me.getValue();
            if (value.length() == 0) {
              throw new IllegalArgumentException(
                  "\""+name+"\" must be non-empty");
            }
            // SECURITY -- examine path
            cmdList.add("-"+name+":"+value);
          }
        }

        // check for heapMin
        String heapMin = 
          (String) javaProps.remove("heap.min");
        if (heapMin != null) {
          if (heapMin.length() == 0) {
            throw new IllegalArgumentException(
                "\"heap.min\" must be non-empty");
          }
          // SECURITY -- examine heap size
          cmdList.add("-Xms"+heapMin);
        }

        // check for heapMax
        String heapMax = 
          (String) javaProps.remove("heap.max");
        if (heapMax != null) {
          if (heapMax.length() == 0) {
            throw new IllegalArgumentException(
                "\"heap.max\" must be non-empty");
          }
          // SECURITY -- examine heap size
          cmdList.add("-Xmx"+heapMax);
        }

        // check for stackSize
        String stackSize = 
          (String) javaProps.remove("stack.size");
        if (stackSize != null) {
          if (stackSize.length() == 0) {
            throw new IllegalArgumentException(
                "\"stack.size\" must be non-empty");
          }
          // SECURITY -- examine stack size
          cmdList.add("-Xss"+stackSize);
        }

        // check for assertions
        for (Iterator iter = javaProps.entrySet().iterator();
            iter.hasNext();
            ) {
          Map.Entry me = (Map.Entry) iter.next();
          String name = (String) me.getKey();
          boolean isEnable = name.startsWith("enable.");
          if (isEnable || name.startsWith("disable.")) {
            String value = (String) me.getValue();
            iter.remove();
            // expecting name to be one of:
            //   enable.assertions
            //   enable.system.assertions
            //   disable.assertions
            //   disable.system.assertions
            String nameTail = 
              name.substring(
                  isEnable ? 
                  "enable.".length() :
                  "disable.".length());
            String newCmd = null;
            if (nameTail.startsWith("assertions")) {
              if ((value == null) ||
                  (value.equalsIgnoreCase("true"))) {
                // SECURITY -- should be okay
                newCmd = (isEnable ? "-ea" : "-da");
              } else if (value.equalsIgnoreCase("false")) {
                // SECURITY -- should be okay
                newCmd = (isEnable ? "-da" : "-ea");
              } else {
                // SECURITY -- examine value
                newCmd = 
                    ((isEnable ? "-ea" : "-da") +
                    ":"+value);
              }
            } else if (nameTail.equals("system.assertions")) {
              if ((value == null) ||
                  (value.equalsIgnoreCase("true"))) {
                // SECURITY -- should be okay
                newCmd = (isEnable ? "-esa" : "-dsa");
              } else if (value.equalsIgnoreCase("false")) {
                // SECURITY -- should be okay
                newCmd = (isEnable ? "-dsa" : "-esa");
              } else {
                // invalid value
              }
            }
            if (newCmd != null) {
              cmdList.add(newCmd);
            } else {
              throw new IllegalArgumentException(
                  "Expecting \"java."+
                  (isEnable ? "enable" : "disable")+
                  ".assertions[=(true|false|value)]\" or \"java."+
                  (isEnable ? "enable" : "disable")+
                  ".system.assertions[=(true|false)]\", not "+
                  name+
                  ((value != null) ? ("="+value) : ""));
            }
          }
        }

        // take classname IFF no executable jar
        String classname = 
          (String) javaProps.remove("class.name");
        if (classname != null) {
          if (jar == null) {
            // warning: ignoring classname!
          } else {
            if (classname.length() == 0) {
              throw new IllegalArgumentException(
                  "Classname must be non-empty");
            }
            // SECURITY -- examine class name
            //
            // specify the classname later
          }
        } else {
          throw new IllegalArgumentException(
              "Properties must specify a \"java.class.name\"");
        }

        // add all remaining java properties
        for (Iterator iter = javaProps.entrySet().iterator();
            iter.hasNext();
            ) {
          Map.Entry me = (Map.Entry) iter.next();
          String name = (String) me.getKey();
          String value = (String) me.getValue();

          // SECURITY -- maybe block some pairs
          if (value.length() > 0) {
            cmdList.add("-"+name+"="+value);
          } else {
            cmdList.add("-"+name);
          }
        }

        // add all the system properties
        for (Iterator iter = sysProps.entrySet().iterator();
            iter.hasNext();
            ) {
          Map.Entry me = (Map.Entry) iter.next();
          String name = (String) me.getKey();
          String value = (String) me.getValue();

          // SECURITY -- these are probably okay...
          if (value.length() > 0) {
            cmdList.add("-D"+name+"="+value);
          } else {
            cmdList.add("-D"+name);
          }
        }

        if (jar != null) {
          // add the jar name
          cmdList.add(jar);
        } else {
          // add the class name
          cmdList.add(classname);
        }
      }

      if (!(envProps.isEmpty())) {
        // flatten the envProps to a String[]
        int nEnvProps = envProps.size();
        if (nEnvProps > 0) {
          Iterator iter = envProps.entrySet().iterator();
          for (int i = 0; i < nEnvProps; i++) {
            Map.Entry me = (Map.Entry) iter.next();
            String name = (String) me.getKey();
            String value = (String) me.getValue();

            // SECURITY -- should scan these closely, possibly 
            //   OS specific
            if (value.length() > 0) {
              envList.add(name+"="+value);
            } else {
              envList.add(name);
            }
          }
        }
      }
    }

    // add any additional command-line arguments
    List args = pd.getCommandLineArguments();
    int nargs = ((args != null) ? args.size() : 0);
    for (int i = 0, n = nargs; i < n; i++)  {
      String argi = (String) args.get(i);
      // SECURITY -- these are probably okay...
      cmdList.add(argi);
    }

    // flatten the command line to a String[]
    // 
    // we may need to be careful about whitespaces and quoting
    //   here -- see JDK bug 4064116.
    cmdLine = (String[]) cmdList.toArray(new String[cmdList.size()]);

    // flatten the envs to a String[]
    envVars = (String[]) envList.toArray(new String[envList.size()]);

    return 
      new String[][] {
        cmdLine,
        envVars};
  }
}
