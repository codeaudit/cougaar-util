/*
 * <copyright>
 *  Copyright 2003 BBNT Solutions, LLC
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
package org.cougaar.tools.server.system.linux;

import java.io.*;
import java.util.*;

import org.cougaar.tools.server.system.ProcessStatus;
import org.cougaar.tools.server.system.ProcessStatusReader;

/**
 * Linux-specific implementation of a 
 * <code>ProcessStatusReader</code>.
 * 
 * @see ProcessStatusReader
 * @see org.cougaar.tools.server.system.SystemAccessFactory
 */
public class LinuxProcessStatusReader 
implements ProcessStatusReader {

  /**
   * This works for "procps version 2.0.7" and <i>maybe</i>
   * other "ps" implementations -- hopefully it's POSIX
   * compliant.
   */
  private static final String[] LINUX_PS_SHORT = {
    "/bin/ps",
    "-o",
    "pid,ppid,user,lstart,cmd",
    "-H",
    "--width",
    "1000",
    "--sort=pid",
    "--no-headers",
  };

  private static final String[] LINUX_PS_ALL;
  static {
    // tack on a "-A"
    String[] sa = new String[LINUX_PS_SHORT.length+1];
    for (int i = 0; i < LINUX_PS_SHORT.length; i++) {
      sa[i] = LINUX_PS_SHORT[i];
    }
    sa[LINUX_PS_SHORT.length] = "-A";
    LINUX_PS_ALL = sa;
  }

  // format for "lstart"
  private static final String LONG_TIME_FORMAT =
    "EEE MMM d hh:mm:ss yyyy";

  private static final int LONG_TIME_SPACES;
  static {
    int n = 0;
    for (int i = 0; i < LONG_TIME_FORMAT.length(); i++) {
      if (LONG_TIME_FORMAT.charAt(i) == ' ') {
        n++;
      }
    }
    LONG_TIME_SPACES = n;
  }

  // formatter
  private static final java.text.SimpleDateFormat TIME_FORMATTER =
    new java.text.SimpleDateFormat(LONG_TIME_FORMAT);

  public LinuxProcessStatusReader() {
    // check "os.name"?
  }

  public String[] getCommandLine(
      boolean findAll) {
    if (findAll) {
      return LINUX_PS_ALL;
    } else {
      return LINUX_PS_SHORT;
    }
  }

  public final ProcessStatus[] parseResponse(
      InputStream in) throws IOException {
    return
      parseResponse(
          new BufferedReader(
            new InputStreamReader(
              in)));
  }

  public final ProcessStatus[] parseResponse(
      BufferedReader br) throws IOException {
    return parse0(br);
  }

  private static final ProcessStatus[] parse0(
      BufferedReader br) throws IOException {

    ArrayList l = new ArrayList();
    ArrayList lKids = new ArrayList();

    Position pos = new Position();
    while (true) {
      // read line
      String line = br.readLine();
      if (line == null) {
        break;
      }

      // parse line
      pos.i = 0;
      long pid = parsePID(line, pos);
      long ppid = parsePPID(line, pos);
      String user = parseUser(line, pos);
      long lstart = parseLStart(line, pos);
      String cmd = parseCmd(line, pos);

      // find parent
      ProcessStatus parent = null;
      List parentChildren = null;
      for (int n = l.size() - 1; n >= 0; n--) {
        ProcessStatus pn = (ProcessStatus)l.get(n);
        if (pn.getProcessIdentifier() == ppid) {
          parent = pn;
          parentChildren = (List)lKids.get(n);
          break;
        }
      }

      List selfChildren = new ArrayList(13);

      // create entry
      ProcessStatus self = 
        new ProcessStatus(
            parent,
            selfChildren,
            pid,
            ppid,
            lstart,
            user,
            cmd);

      if (parentChildren != null) {
        parentChildren.add(self);
      }

      l.add(self);
      lKids.add(selfChildren);
    }

    // convert to array
    return (ProcessStatus[]) l.toArray(new ProcessStatus[l.size()]);
  }

  private static class Position {
    // no pass-by-reference...
    public int i;
  }

  private static long parsePID(String s, Position pos) {
    // expecting (' '* PID ' ')
    long pid;
    try {
      int i = pos.i;
      while (s.charAt(i) == ' ') {
        ++i;
      }
      int j = s.indexOf(' ', i);
      String spid = s.substring(i, j);
      pid = Long.parseLong(spid);
      pos.i = j+1;
    } catch (RuntimeException re) {
      throw new IllegalArgumentException(
          "Illegal pid: \""+s+"\"");
    }
    return pid;
  }

  private static long parsePPID(String s, Position pos) {
    long ppid;
    try {
      int i = pos.i;
      while (s.charAt(i) == ' ') {
        ++i;
      }
      int j = s.indexOf(' ', i);
      String sppid = s.substring(i, j);
      ppid = Long.parseLong(sppid);
      pos.i = j+1;
    } catch (RuntimeException re) {
      throw new IllegalArgumentException(
          "Illegal ppid: \""+s+"\"");
    }
    return ppid;
  }

  private static String parseUser(String s, Position pos) {
    String suser;
    try {
      int i = pos.i;
      while (s.charAt(i) == ' ') {
        ++i;
      }
      int j = s.indexOf(' ', i);
      suser = s.substring(i, j);
      pos.i = j+1;
    } catch (RuntimeException re) {
      throw new IllegalArgumentException(
          "Illegal user: \""+s+"\"");
    }
    return suser;
  }

  private static long parseLStart(String s, Position pos) {
    // expecting something like:
    //
    //   "Tue Oct 16 17:57:12 2001"
    //
    // FIXME: this is very fragile!
    long time;
    try {
      int i = pos.i;
      while (s.charAt(i) == ' ') {
        ++i;
      }
      // find end of date
      int j = i;
      for (int x = 0; x <= LONG_TIME_SPACES; x++) {
        j = s.indexOf(' ', j+1);
        while (s.charAt(j) == ' ') {
          ++j;
        }
      }
      String sstart = s.substring(i, j);
      time = parseLongDate(sstart);
      pos.i = j;
    } catch (RuntimeException re) {
      throw new IllegalArgumentException(
          "Illegal lstart: \""+s+"\"");
    }
    return time;
  }

  private static String parseCmd(String s, Position pos) {
    String scmd;
    try {
      int i = pos.i;
      while (s.charAt(i) == ' ') {
        ++i;
      }
      scmd = s.substring(i);
      pos.i = s.length();
    } catch (RuntimeException re) {
      throw new IllegalArgumentException(
          "Illegal cmd: \""+s+"\"");
    }
    return scmd;
  }

  protected static long parseLongDate(String lstart) {
    java.text.ParsePosition p = 
      new java.text.ParsePosition(0);
    java.util.Date d;
    synchronized (TIME_FORMATTER) {
      d = TIME_FORMATTER.parse(lstart, p);
    }
    return d.getTime();
  }

}
