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

package org.cougaar.tools.server.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.cougaar.tools.server.RemoteFileSystem;

/** 
 * Server implementation of the "remote-file-system" API.
 */
class RemoteFileSystemImpl implements RemoteFileSystem {

  private static String dotPath = "."+File.separatorChar;

  private final boolean verbose;
  private final String tempPath;

  public RemoteFileSystemImpl(
      boolean verbose,
      String tempPath) {
    this.verbose = verbose;
    this.tempPath = calculateTempPath(tempPath);
  }

  private static String calculateTempPath(String tempPath) {
    // tempPath uses the system-style path separator
    //
    // fix a relative ".[/\\]" path to an absolute path
    String tPath = 
      ((tempPath != null) ? 
       tempPath.trim() : 
       dotPath);
    if (tPath.startsWith(dotPath)) {
      // fix to be the full system-style path
      try {
        File dotF = new File(".");
        String absPath = dotF.getAbsolutePath();
        if (absPath.endsWith(".")) {
          absPath = absPath.substring(0, (absPath.length() - 1));
        }
        tPath = absPath + tPath.substring(2);
      } catch (Exception e) {
        // ignore?
      }
    }
    // path must end in "[/\\]"
    if (tPath.charAt(tPath.length()-1) != File.separatorChar) {
      tPath = tPath + File.separatorChar;
    }
    return tPath;
  }

  /**
   * List files on a host.
   */
  public String[] list(
      String path) {
    // check the path
    if (!(path.startsWith("./"))) {
      throw new IllegalArgumentException(
          "Path must start with \"./\", not \""+path+"\"");
    } else if (path.indexOf("..") > 0) {
      throw new IllegalArgumentException(
          "Path can not contain \"..\": \""+path+"\"");
    } else if (path.indexOf("\\") > 0) {
      throw new IllegalArgumentException(
          "Path must use the \"/\" path separator, not \"\\\": \""+path+"\"");
    } else if (!(path.endsWith("/"))) {
      throw new IllegalArgumentException(
          "Path must end in \"/\", not \""+path+"\"");
    }
    // other checks?  Security manager?

    // fix the path to use the system path-separator and be relative to 
    //   the "tempPath"
    String sysPath = path;
    if (File.separatorChar != '/') {
      sysPath = sysPath.replace('/', File.separatorChar);
    }
    sysPath = tempPath + sysPath.substring(2);

    // open a File for the sysPath
    File d = new File(sysPath);

    // make sure it's a readable directory, etc
    if (!(d.exists())) {
      throw new IllegalArgumentException(
          "Path does not exist: \""+path+"\"");
    } else if (!(d.isDirectory())) {
      throw new IllegalArgumentException(
          "Path is not a directory: \""+path+"\"");
    } else if (!(d.canRead())) {
      throw new IllegalArgumentException(
          "Unable to read from path: \""+path+"\"");
    }

    // get a directory listing of the files
    File[] files = d.listFiles();
    if (files == null) {
      throw new IllegalArgumentException(
          "Unable to get a directory listing for path: \""+path+"\"");
    }
    int nfiles = files.length;

    // make an array of file names
    String[] ret = new String[nfiles];

    // get the file names and fix them to 
    //  - only list readable, non-hidden files
    //  - all start with the "path" (i.e. start with "./")
    //  - have directory names end in "/"
    int nret = 0;
    for (int i = 0; i < nfiles; i++) {
      File fi = files[i];
      if (fi.canRead() &&
          (!(fi.isHidden()))) {
        String si = path + fi.getName();
        if (fi.isDirectory()) {
          si += "/";
        }
        ret[nret++] = si;
      }
    }

    // trim the array if necessary
    if (nret < nfiles) {
      String[] trimmedRet = new String[nret];
      System.arraycopy(ret, 0, trimmedRet, 0, nret);
      ret = trimmedRet;
    }

    // return the list of file names
    return ret;
  }

  /**
   * Open a file for reading.
   */
  public InputStream read(
      String filename) {
    File f = getSysFile(filename);
    
    // make sure that the file is not a directory, etc
    if (!(f.exists())) {
      throw new IllegalArgumentException(
          "File does not exist: \""+filename+"\"");
    } else if (!(f.isFile())) {
      throw new IllegalArgumentException(
          "File is "+
          (f.isDirectory() ?  "a directory" : "not a regular file")+
          ": \""+filename+"\"");
    } else if (!(f.canRead())) {
      throw new IllegalArgumentException(
          "Unable to read file: \""+filename+"\"");
    }

    // get an input stream for the file
    FileInputStream fin;
    try {
      fin = new FileInputStream(f);
    } catch (FileNotFoundException fnfe) {
      // shouldn't happen -- I already checked "f.exists()"
      throw new IllegalArgumentException(
          "File does not exist: \""+filename+"\" "+fnfe);
    }

    return fin;
  }

  /**
   * Open a file for writing.
   */
  public OutputStream write(
      String filename) {
    return _write(filename, false);
  }

  /**
   * Open a file for appending.
   */
  public OutputStream append(
      String filename) {
    return _write(filename, true);
  }

  private OutputStream _write(
      String filename,
      boolean append) {
    File f = getSysFile(filename);
    
    // make sure that the file is not a directory, etc
    if (f.exists()) {
      if (!(f.isFile())) {
        throw new IllegalArgumentException(
            "File is "+
            (f.isDirectory() ?  "a directory" : "not a regular file")+
            ": \""+filename+"\"");
      } else if (!(f.canWrite())) {
        throw new IllegalArgumentException(
            "Unable to write file: \""+filename+"\"");
      }
    } else {
      if (append) {
        throw new IllegalArgumentException(
            "File does not exist: \""+filename+"\"");
      }
    }

    // get an input stream for the file
    FileOutputStream fout;
    try {
      fout = new FileOutputStream(f);
    } catch (FileNotFoundException fnfe) {
      // shouldn't happen -- I already checked "f.exists()"
      throw new IllegalArgumentException(
          "File does not exist: \""+filename+"\" "+fnfe);
    }

    return fout;
  }

  // Error check the filename, then get a file handle for the system
  private File getSysFile(String filename) {
    // check the path  (should merge this code with the "list(..)" code)
    if (!(filename.startsWith("./"))) {
      throw new IllegalArgumentException(
          "Filename must start with \"./\", not \""+filename+"\"");
    } else if (filename.indexOf("..") > 0) {
      throw new IllegalArgumentException(
          "Filename can not contain \"..\": \""+filename+"\"");
    } else if (filename.indexOf("\\") > 0) {
      throw new IllegalArgumentException(
          "Filename must use the \"/\" path separator, not \"\\\": \""+
          filename+"\"");
    } else if (filename.endsWith("/")) {
      throw new IllegalArgumentException(
          "Filename can not end in \"/\": \""+filename+"\"");
    }
    // other checks?  Security manager?

    // fix the filename path to use the system path-separator and be 
    //   relative to the "tempPath"
    String sysFilename = filename;
    if (File.separatorChar != '/') {
      sysFilename = sysFilename.replace('/', File.separatorChar);
    }
    sysFilename = tempPath + sysFilename.substring(2);
    // open the file
    return new File(sysFilename);
  }

  // add delete/write/etc support here
}
