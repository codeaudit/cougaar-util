package org.cougaar.tools.server.system.linux;

import java.io.*;

import java.util.ArrayList;
import java.util.List;

import org.cougaar.tools.server.system.ProcessLauncher;

/**
 * Linux-specific implementation of a 
 * <code>ProcessLauncher</code>.
 * 
 * @see ProcessLauncher
 * @see org.cougaar.tools.server.system.SystemAccessFactory
 */
public class LinuxProcessLauncher 
implements ProcessLauncher {

  private static final byte START_BYTE = '{';
  private static final byte END_BYTE = '}';

  private static final String[] LINUX_EXEC =
    new String[] {
      "sh",
      "-c",
      "echo -n "+
        ((char)START_BYTE)+
        "$$"+
        ((char)END_BYTE)+
        "; exec ",  // +cmd
    };

  public LinuxProcessLauncher() {
    // check "os.name"?
  }

  public String[] getCommandLine(String[] cmd) {
    // need to flatten the array... this *should* be okay.
    //
    // FIXME: what about whitespace!!!
    StringBuffer sb = new StringBuffer();
    int n = cmd.length;
    for (int i = 0; i < n; i++) {
      String si = cmd[i];
      //
      // might need to wrap si in quotes here!
      //
      sb.append(si);
      if (i < (n - 1)) {
        sb.append(" ");
      }
    }
    String fullCmd = sb.toString();

    return getCommandLine(fullCmd);
  }

  public String[] getCommandLine(String fullCmd) {
    // tack on the full-cmd after the "exec"
    int n = LINUX_EXEC.length;
    String[] cmd = new String[n];
    for (int i = 0; i < n; i++) {
      cmd[i] = LINUX_EXEC[i];
    }
    cmd[n-1] = cmd[n-1]+fullCmd;
    return cmd;
  }

  public long parseProcessIdentifier(
      InputStream in) throws Exception {
    return parseProcessIdentifier(in, START_BYTE, END_BYTE);
  }

  /**
   * Read a "startByte + NUMBER + endByte" from a stream,
   * such as "{123}".
   */
  private static long parseProcessIdentifier(
      InputStream in,
      byte startByte,
      byte endByte) throws IOException {

    // read start byte
    int t0 = in.read();
    if (t0 < 0) {
      throw new IOException(
          "Unexpected empty Stream, expecting the PID header \""+
          ((char)startByte)+
          "<NUMBER>"+
          ((char)endByte)+
          "\"");
    }
    byte b0 = (byte)t0;
    if (!(b0 == startByte)) {
      throw new IOException(
          "Stream doesn't start with the expected PID header \""+
          ((char)startByte)+
          "<NUMBER>"+
          ((char)endByte)+
          "\": \""+
          ((char)b0)+
          "..\"");
    } 

    // read a number, up to 32 digits max (sanity check)
    byte[] buf = new byte[32];
    int i = 0;
    while (true) {
      int ti = in.read();
      if (ti < 0) {
        throw new IOException(
            "Unexpected end of Stream, expecting futher PID digits"+
            " or \""+
            ((char)endByte)+
            "\"");
      }
      byte bi = (byte)ti;
      if ((bi >= '0') &&
          (bi <= '9')) {
        // another digit
        try {
          buf[i++] = bi;
        } catch (ArrayIndexOutOfBoundsException ae) {
          throw new IOException(
              "Error -- PID is too long to be valid: \""+
              (new String(buf))+
              "\"");
        }
      } else if (bi == endByte) {
        // end of digits
        break;
      } else {
        // ERROR!
        throw new IOException(
            "Stream contains an unexpected byte \""+
            ((char)bi)+
            "\", expecting a digit or \""+
            ((char)endByte)+
            "\"");
      }
    }

    // parse the number
    String snum = new String(buf, 0, i);
    long lnum = Long.parseLong(snum);

    return lnum;
  }
}
