/*
 * <copyright>
 *  
 *  Copyright 1997-2004 Cougaar Software Inc
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
 *
 * CHANGE RECORD
 * - 
 */

package org.cougaar.util.jar;

import java.net.JarURLConnection;
import java.net.URL;
import java.util.jar.JarFile;

public class JarFileInfo {
  /** The URL of the Jar file
   */
  private URL _theJarFileUrl;

  /** A JarFile reference to the JAR file
   */
  private JarFile _theJarFile;

  /** True if all the file names in the jar files have been processed and stored
   *  in the cache.
   */
  private boolean _isProcessed;

  private long    _creationTime;

  public JarFileInfo(URL aUrl)
    throws java.io.IOException {
    _theJarFileUrl = aUrl;
    JarURLConnection juc =
      (JarURLConnection)_theJarFileUrl.openConnection();
    _theJarFile = juc.getJarFile();
    _isProcessed = false;
    _creationTime = System.currentTimeMillis();
  }

  public long getCreationTime() {
    return _creationTime;
  }

  public URL getJarFileURL() {
    return _theJarFileUrl;
  }
  public JarFile getJarFile() {
    return _theJarFile;
  }
  public boolean isJarFileProcessed() {
    return _isProcessed;
  }
  public void setJarFileProcessed(boolean value) {
    _isProcessed = value;
  }
}
