/*
 * <copyright>
 *  
 *  Copyright 1997-2004 Cougaar Software, Inc
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

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.cougaar.bootstrap.SystemProperties;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.Configuration;

/**
 * JarConfigFinder provides utilities to search for a named file in several specified
 * locations, returning the first location where a file by that name is found.
 * Files are found and opened by the open() method.
 * JarConfigFinder searches an ordered list of configuration paths to locate files.
 * Each element in the list is a URL pointing to a file or a directory, and may
 * reference one of the following elements:
 * <ul>
 * <li>A simple file</li>
 * <li>A signed or unsigned jar file in a file system or available through an HTTP connection</li>
 * <li>A local directory</li>
 * </ul>
 * The configuration path is a semi-colon ordered list of these URL elements.
 * The search is performed by looking at the URL elements from left to right.
 * <ul>
 * <li>If the element is a simple file and the name matches the requested file,
 * the file is returned.</li>
 * <li>If the element is a jar file, the JarConfigFinder attempts to locate
 * a file in the jar file that matches the name of the requested file.
 * The jar file can be opened in a file system or through an HTTP connection.</li>
 * <li>If the element is a directory in a file system, the JarConfigFinder
 * attempts to locate a matching (unsigned) file in that directory.
 * The JarConfigFinder also looks up all jar files contained in that
 * directory and tries to find a matching file in one of those jar files.</li>
 * </ul>
 * Properties:
 * <ul>
 * <li>If the <b>org.cougaar.util.jar.jarFilesOnly</b> is set to true,
 * JarConfigFinder returns files contained in JAR files only</li>
 * <li>If the <b>org.cougaar.config.signedOnly</b> property is set to true,
 * the SecureConfigFinder returns only files that have been signed.</b>
 * </ul>
 */
public class JarConfigFinder
extends ConfigFinder
{
  private static final String ORG_COUGAAR_WORKSPACE = "org.cougaar.workspace";
  
  private static final String JAR_FILES_ONLY = "org.cougaar.util.jar.jarFilesOnly";
  
  private static long DELAY_TO_RELEASE_JAR_FILES = 5 * 60 * 1000;
  
  /** A list of URLs pointing to JAR files containing resource files.
   * Files requested by a client are searched in those jar files.
   */
  private List _jarFileCache = new ArrayList();
  
  /** A list of path to look for JAR files.
   *  This list is initially populated by using the configuration
   *  from the constructor. It may be augmented by adding Jar files
   *  found in the config path
   */
  private List _configPathList;
  
  /**
   * A list of URLs pointing to Jar files that have been released.
   * This allows JarFiles to be reclaimed by the garbage collector.
   */
  private List  _releasedJarFileUrls = new ArrayList();
  
  /** A directory to store files extracted from JAR files, so that we
   *  can return a File reference to the client.
   */
  protected File _jarFileCacheDirectory;
  
  /** A directory to extract files from JAR files, before they are
   *  moved to the _jarFileCacheDirectory.
   */
  protected File _tmpDirectory;
  
  /** Parent directory to _tmpDirectory and _jarFileCacheDirectory,
   * specified by getTmpBaseDirectoryName().  If null, we will
   * use the system-defined tmp directory (see File.createTempFile).
   **/
  protected File _tmpBaseDirectory;
  
  private boolean _jarFilesOnly;
  
  /**
   * Alias for JarConfigFinder({@link Configuration#getConfigPath()}, {@link Configuration#getDefaultProperties()})
   */
  public JarConfigFinder() {
    this(Configuration.getConfigPath(), Configuration.getDefaultProperties());
  }
  
  /**
   * Alias for JarConfigFinder(path, {@link Configuration#getDefaultProperties()})
   */
  public JarConfigFinder(String path) {
    this(path, Configuration.getDefaultProperties());
  }
  
  /**
   * Alias for JarConfigFinder(null, path, props)
   */
  public JarConfigFinder(String path, Map props) {
    this(null, path, props);
  }
  
  /**
   * Alias for JarConfigFinder(module, path, Configuration.getDefaultProperties())
   */
  public JarConfigFinder(String module, String path) {
    this(module, path, Configuration.getDefaultProperties());
  }
  
  /**
   * Constructs a JarConfigFinder that will first search within the specified module,
   * and then in the directories on the given search path, using the given
   * Property substitutions.
   * When searching the given module, we search the following 4 directories (if defined) before any other directories:
   * <ul>
   * <li>$INSTALL/$module/configs/$CONFIG</li>
   * <li>$INSTALL/$module/configs</li>
   * <li>$INSTALL/$module/data/$CONFIG</li>
   * <li>$INSTALL/$module/data</li>
   * </ul>
   * 
   * @param module Name of the module to use for module-specific configs. If null, no module-specific paths are added.
   * @param path Configuration path string. If null, defaults to Configuration.getConfigPath()
   * @param props Properties to use for configpath variable substitutions
   */
  public JarConfigFinder(String module, String path, Map props) {
    super(module, path, props);
    createJarFileCacheDirectory();
    
    _configPathList = new ArrayList();
    _configPathList.addAll(getConfigPath());
    
    // Allow locating files in all files by default
    _jarFilesOnly = SystemProperties.getBoolean(JAR_FILES_ONLY);
    
    if (getLogger().isDebugEnabled()) {
      getLogger().debug("jar Files only: " + jarFilesOnly());
      Iterator it = _configPathList.iterator();
      String s = "";
      while (it.hasNext()) {
        s = s + " - " + ((URL) it.next()).toString();
      }
      getLogger().debug("Config path:" + s);
    }
    launchJarInfoCleanupThread();
  }
  
  ////////////////////////////////////////////////////////////////////
  // BEGIN ConfigFinder overloaded methods
  
  /**
   * Locate an actual file in the config path.
   * @param aFilename The name of a file being searched
   */
  @Override
public File locateFile(String aFilename) {
    if (getLogger().isDebugEnabled()) {
      getLogger().debug("locateFile:" + aFilename);
    }
    URL aUrl = resolveUrl(aFilename);
    if (aUrl == null) {
      return null;
    }
    if (getLogger().isDebugEnabled()) {
      getLogger().debug("Found:" + aUrl.toString());
    }
    File f = null;
    if (aUrl.getProtocol().startsWith("jar")) {
      // The file is in a Jar file. We cannot return a File handle
      // directly, so we copy the file in a temporary directory
      // copyFileToTempDirectory checks for invalid signatures.
      try {
        f = copyFileToTempDirectory(aUrl, aFilename);
      }
      catch (Exception e) {
        getLogger().warn("Unable to copy to temp directory: " + aFilename
            + ". Reason: " + e);
      }
    }
    else {
      try {
        f = new File(new URI(aUrl.toString()));
      }
      catch (Exception e) {
        getLogger().warn("Unable to get file handle for " + aFilename);
      }
    }
    return f;
  }
  
  /**
   * Opens an InputStream to access the named file. The file is sought
   * in all the places specified in configPath.
   * The file being searched should be specified as one of:
   * <ul>
   * <li>The name of the file being searched, e.g. just the last name in the pathname's name sequence.
   * For example, a component might lookup a file by specifying "config.txt"</li>
   * <li>The last few elements in the pathname of the file being searched.
   * For example, a component might lookup a file by specifying "org/cougaar/config.txt"</li>
   * <li>The full URL of the file in the JAR file
   * For example, a component might lookup a file by specifying
   * "jar:file://usr/local/cougaar/configs.jar!org/cougaar/config.txt"</li>
   * </ul>
   * @param aURL The name of a file being searched
   * @throws FileNotFoundException if the resource cannot be found.
   **/
  @Override
public InputStream open(String aURL)
  throws IOException {
    if (getLogger().isDebugEnabled()) {
      getLogger().debug("open:" + aURL);
    }
    URL aUrl = resolveUrl(aURL);
    if (getLogger().isDebugEnabled()) {
      getLogger().debug("Found:" + (aUrl != null ? aUrl.toString() : null));
    }
    if (aUrl == null) {
      throw new FileNotFoundException("Resource cannot be found: " + aURL);
    }
    else {
      // The resolveUrl() method checks that the
      // signature was valid, so we do not check again here
      return aUrl.openStream();
    }
  }
  
  /**
   * Attempt to find the URL which would be opened by the open method.
   * Note that this must actually attempt to open the various URLs
   * under consideration, so this is <em>not</em> an inexpensive operation.
   * The file being searched should be specified as one of:
   * <ul>
   * <li>The name of the file being searched, e.g. just the last name in the pathname's name sequence.
   * For example, a component might lookup a file by specifying "config.txt"</li>
   * <li>The last few elements in the pathname of the file being searched.
   * For example, a component might lookup a file by specifying "org/cougaar/config.txt"</li>
   * <li>The full URL of the file in the JAR file
   * For example, a component might lookup a file by specifying
   * "jar:file://usr/local/cougaar/configs.jar!org/cougaar/config.txt"</li>
   * </ul>
   * @param aURL The name of a file being searched
   **/
  @Override
public URL find(String aURL) {
    // The resolveUrl() method checks that the
    // signature was valid, so we do not check again
    URL theURL = resolveUrl(aURL);
    return theURL;
  }
  
  // END ConfigFinder overloaded methods
  ///////////////////////////////////////////////////////////////////
  
  /**
   * Resolve a logical reference to a URL
   * @param aFileName the name of a file to be resolved
   * @return null if no file can be found at that location
   */
  protected URL resolveUrl(String aFileName) {
    URL theURL = null;
    
    synchronized (urlCache) {
      if (getLogger().isDebugEnabled()) {
        getLogger().debug("Looking up " + aFileName + " in local cache ("
            + urlCache.size() + " elements)");
      }
      // First, search in the cache
      theURL = (URL) urlCache.get(aFileName);
    }
    
    if (theURL != null) {
      if (getLogger().isDebugEnabled()) {
        getLogger().debug("Found " + aFileName + " in local cache");
      }
    } else {
      // Second, search the file in the list of JAR files
      if (getLogger().isDebugEnabled()) {
        getLogger().debug("Looking up " + aFileName + " in Jar file list ("
            + _jarFileCache.size() + " jar files)");
      }
      synchronized (_jarFileCache) {
        refreshJarFileCache();
        ListIterator it = _jarFileCache.listIterator();
        while (it.hasNext()) {
          JarFileInfo entry = (JarFileInfo) it.next();
          theURL = locateFileInJarFile(aFileName, entry);
          if (theURL != null) {
            if (getLogger().isDebugEnabled()) {
              getLogger().debug("Found " + aFileName + " in Jar file list");
            }
          }
        }
      }
    }
    
    if (theURL == null) {
      // Third, the list of JAR files may have not been updated yet.
      // Update the list of jar files by looking in the configPath and
      // finding jar files. Remove jar files from configPath as they
      // are added to _jarFileCache.
      List configPath = null;
      List pathsToRemove = null;
      synchronized (_configPathList) {
        configPath = new ArrayList(_configPathList);
      }
      if (getLogger().isDebugEnabled()) {
	getLogger().debug(
			  "Looking up " + aFileName + " in config path (" +
			  configPath.size() + " elements)");
      }
      for (ListIterator it = configPath.listIterator();
           (it.hasNext() && (theURL == null));
	   ) {
	URL base = (URL) it.next();
	theURL = locateFileInPathElement(base, aFileName);
	// plan to remove the path, so that we don't look it
	// up again the next time we search.
        if (getLogger().isDebugEnabled()) { 
          getLogger().debug("Removing " + base + " from path");
        }
        if (pathsToRemove == null) {
          pathsToRemove = new ArrayList();
        }
        pathsToRemove.add(base);
	/*
	  for (int i = 0 ; i < configPath.size() ; i++) {
	  getLogger().debug(configPath.get(i).toString());
	  }
	*/
      }
      if (pathsToRemove != null) {
        synchronized (_configPathList) {
          _configPathList.removeAll(pathsToRemove);
        }
      }
    }
    
    if (theURL == null) {
      if (getLogger().isDebugEnabled()) {
        File f = new File(aFileName);
        getLogger().debug("Looking up " + aFileName + " - Accept absolute path:"
            + acceptAbsoluteFileNames() + " - isAbsolutePath:" +
            f.isAbsolute() + " - Jar Files only:" + jarFilesOnly());
      }
      // The URL may be an absolute file name
      if (acceptAbsoluteFileNames()) {
        File f = new File(aFileName);
        if (f.isAbsolute() && !jarFilesOnly()) {
          try {
            if (isValidUrl(f.toURI().toURL())) {
              addFileEntryToCache(f.getName(), f.toURI().toURL());
              // Is there a match?
              if (f.getPath().equals(aFileName)) {
                theURL = f.toURI().toURL();
              }
            }
          } catch (Exception e) {
            getLogger().warn("Unable to get URL for " + f.getPath());
          }
        }
      }
    }
    
    // Verify the integrity of the input stream.
    try {
      verifyInputStream(theURL);
    }
    catch (Exception e) {
      // Do not return the URL if the integrity of the data could not
      // be verified.
      theURL = null;
    }
    return theURL;
  }
  
  /** Return a JarFile if the file is really a Jar file.
   *  Return null otherwise.
   */
  protected JarFile getJarFile(File aFile) {
    try {
      JarFile aJarFile = new JarFile(aFile);
      return aJarFile;
    }
    catch (Exception e) {
      // This is not a Jar file, or the file could not be read
      return null;
    }
  }
  
  protected void appendJarFiles(URL[] jarFiles) {
    for (int i = 0 ; i < jarFiles.length ; i++) {
      appendJarFile(jarFiles[i]);
    }
  }
  
  /** Return a pathname to use as the directory argument for File.createTempFile.
   * to construct _tmpBaseDirectory.
   * If null, then we'll use the system-defined tmp directory location
   * The default implementation returns the value of the system property "org.cougaar.workspace"
   * if defined, otherwise null.
   */
  protected String getTmpBaseDirectoryName() {
    /*
     // for instance, the SecureConfigFinder might define this as:
      return SystemProperties.getProperty("org.cougaar.workspace") + File.separator +
      "security" + File.separator + 
      "jarconfig" + File.separator +
      SystemProperties.getProperty("org.cougaar.node.name");
      */
    // by default, use /tmp or the equivalent
    return SystemProperties.getProperty(ORG_COUGAAR_WORKSPACE) + File.separator
    + "jarfiles";
  }
  
  /** create a set of uniquely-named temporary directories for
   * our use.
   */
  protected void createJarFileCacheDirectory() {
    // Create temporary directory to store files
    try {
      String base = getTmpBaseDirectoryName();
      File baseF;
      if (base == null) {
        baseF = null;
      } else {
        baseF = new File(base);
        if (!baseF.exists()) {
          baseF.mkdirs();
        }
      }
      
      // creates a regular ".lck" file.  We'll use the base name
      // of the created file to create the directory (without the .lck suffix)
      File fL = File.createTempFile("jarconfig", ".lck", baseF);
      fL.deleteOnExit();      
      
      // Now we create a base directory using the created temp file
      // as a lock and a suggestion for a name
      String tmpPath;
      {
        String tmp = fL.getCanonicalPath();
        tmpPath = tmp.substring(0, tmp.length()-4); // trim off the ".lck"
      }
      
      _tmpBaseDirectory = new File(tmpPath);
      if (_tmpBaseDirectory.exists()) {
        getLogger().warn("tmpBaseDirectory "+tmpPath+" already exists!");
      }
      _tmpBaseDirectory.mkdirs();
      _tmpBaseDirectory.deleteOnExit();
      
      _jarFileCacheDirectory = new File(tmpPath + File.separator + "jarFileCache");
      _jarFileCacheDirectory.mkdirs();
      _jarFileCacheDirectory.deleteOnExit();
      
      _tmpDirectory = new File(tmpPath + File.separator + "tmp");
      _tmpDirectory.mkdirs();
      _tmpDirectory.deleteOnExit();
      
    }
    catch (Exception e) {
      getLogger().warn("Unable to create temporary directory", e);
    }
  }
  
  /**
   * Copy a file contained in a Jar file so that it can be opened using
   * a <class>File</class> handle.
   */
  protected File copyFileToTempDirectory(URL aUrl, String aFilename)
  throws IOException {
    if (getLogger().isDebugEnabled()) {
      getLogger().debug("Copying " + aUrl + " to temp directory");
    }
    
    File tempFile = 
      File.createTempFile("tmpFile", ".tmp", _tmpDirectory);
    FileOutputStream fos = new FileOutputStream(tempFile);
    JarURLConnection juc = (JarURLConnection)aUrl.openConnection();
    juc.setUseCaches(false);
    InputStream is = juc.getInputStream();
    int v = 0;
    while ((v = is.read()) != -1) {
      fos.write(v);
    }
    fos.close();
    is.close();
    
    File newFile = new File(_jarFileCacheDirectory, aFilename);
    boolean isRenamed = tempFile.renameTo(newFile);
    if (isRenamed) {
      return newFile;
    }
    else {
      // Remove the temp file
      tempFile.delete();
      return null;
    }
  }
  
  protected JarFileInfo appendJarFile(URL jarFile) {
    try {
      if (!jarFile.getProtocol().equals("jar")) {
        // The multi-parameter URL constructor does not like
        // the "jar:file" protocol, but it works with a one-parameter
        // constructor
        String s = "jar:" + jarFile.getProtocol() + ":" +
        jarFile.getHost();
        if (jarFile.getPort() != -1) {
          s = s + ":" + jarFile.getPort();
        }
        s = s + jarFile.getPath() + "!/";
        jarFile = new URL(s);
        if (getLogger().isDebugEnabled()) {
          getLogger().debug("Append Jar File: " + jarFile.toString());
        }
      }
      
      JarFileInfo entry = new JarFileInfo(jarFile);
      
      verifyJarFile(entry.getJarFile());
      synchronized (_jarFileCache) {
        _jarFileCache.add(entry);
      }
      return entry;
    }
    catch (Exception e) {
      getLogger().warn("Unable to add entry: " + jarFile.toString(), e);
      return null;
    }
  }
  
  /**
   * Attempts to locate a file in a JAR file.
   * @param aFileName the name of a file being searched.
   * @param entry a cache entry representing a JAR file
   * @return the URL of the file in the JAR file, if an entry was found.
   * null if no entry was found in the JAR file.
   */
  protected URL locateFileInJarFile(String aFileName, JarFileInfo entry) {
    URL theURL = null;
    if (entry == null) {
      return null;
    }
    if (getLogger().isDebugEnabled()) {
      getLogger().debug("Locate file " +
          aFileName + " in " + entry.getJarFileURL()
          + " Processed: " + entry.isJarFileProcessed());
    }
    if (entry.isJarFileProcessed()) {
      // The JAR file has already been processed. Therefore,
      // we should have already looked in the cache for that Jar file.
      return null;
    }
    JarFile jarFile = entry.getJarFile();
    Enumeration e = jarFile.entries();
    while (e.hasMoreElements()) {
      JarEntry jarEntry = (JarEntry) e.nextElement();
      if (jarEntry.isDirectory()) {
	continue;
      }
      String entryFullName = jarEntry.getName();
      File aFile = new File(entryFullName);
      String entryName = aFile.getName();

      String s = entry.getJarFileURL().toString() + entryFullName;
      try {
	URL aURL = new URL(s);
	addFileEntryToCache(entryName, aURL);
	if (entryName.equals(aFileName)) {
	  theURL = aURL;
	}
      }
      catch (Exception ex) {
	getLogger().warn("Unexpected exception: ", ex);
      }
    }
    entry.setJarFileProcessed(true);
    return theURL;
  }
  
  /**
   * Check the integrity of a jar file. Do nothing in the base
   * implementation.
   */
  protected void verifyJarFile(JarFile aJarFile) {
  }
  
  /**
   * Verify the integrity of the data contained at a URL.
   * Some integrity issues might be discovered late when reading
   * an input stream. For example, digest errors are discovered
   * when the entire stream has been read. This gives the opportunity
   * for a secure file finder to verify the data before the stream
   * is returned to the caller.
   *
   * Does nothing in the default implementation, but should typically be
   * defined in a derived class.
   * @param aURL the URL to check.
   * @exception IOException if an IO Exception occurs while opening the stream
   * @exception GeneralSecurityException if there was a problem while checking
   *                the integrity of the input stream.
   */
  protected void verifyInputStream(URL aURL) {
  }
  
  private void addFileEntryToCache(String aFileName, URL aURL) {
    synchronized (urlCache) {
      if (!urlCache.containsKey(aFileName)) {
        // getLogger().debug("Adding mapping: " + aFileName + "<=>"
        // + aURL.toString());
        urlCache.put(aFileName, aURL);
      }
    }
  }
  
  /**
   * @param base - The path element where to search files.
   * @param aFileName - The name of a file to search.
   *
   * If aFileName is null, then no search is performed. The path
   * element is still added to the cache.
   */
  protected URL locateFileInPathElement(URL base, String aFileName) {
    URL theURL = null;
    if (getLogger().isDebugEnabled()) {
      getLogger().debug("locateFileInPathElement:" + aFileName
          + " in " + base.toString());
    }
    if (base.getProtocol().equals("file") ||
        base.getProtocol().equals("jar:file")) {
      theURL = locateFileInFileElement(base, aFileName);
    }
    else if (base.getProtocol().equals("http")) {
      theURL = locateFileInHttpElement(base, aFileName);
    }
    return theURL;
  }
  
  protected URL locateFileInFileElement(URL base, String aFileName) {
    URL theURL = null;
    File aFile = new File(base.getFile());
    if (aFile.exists()) {
      if (aFile.isFile()) {
        // This is a file. Return it if there is a match
        // and it is ok to return unsigned jar files.
        JarFile aJarFile = getJarFile(aFile);
        if (aJarFile == null) {
          // This is not a Jar file, or the file could not be read
          if (!jarFilesOnly() && aFileName != null) {
            try {
              if (isValidUrl(aFile.toURI().toURL())) {
                addFileEntryToCache(aFile.getName(), aFile.toURI().toURL());
                // Is there a match?
                if (aFile.getPath().equals(aFileName)) {
                  theURL = aFile.toURI().toURL();
                }
              }
            } catch (Exception e) {
              getLogger().warn("Unable to get URL for " + aFile.getPath());
            }
          }
        }
        else {
          // This is a Jar file.
          // Add the new jar file to the list of jar files.
          try {
            JarFileInfo entry = appendJarFile(aFile.toURI().toURL());
            if (aFileName != null) {
              theURL = locateFileInJarFile(aFileName, entry);
            }
          }
          catch (Exception e) {
            getLogger().warn("Unable to get URL for " + aFile.getPath());
          }
        }
      }
      else if (aFile.isDirectory()) {
        // This is a directory. Attempt to find jar files
        // in that directory.
        
        File jarFiles[] = aFile.listFiles(new FileFilter() {
          public boolean accept(File pathname) {
            return pathname.getName().endsWith(".jar");
          }
        });
        for (int i = 0 ; i < jarFiles.length ; i++) {
          JarFile aJar = getJarFile(jarFiles[i]);
          if (aJar != null) {
            // Add the new jar file to the list of jar files.
            try {
              JarFileInfo entry = appendJarFile(jarFiles[i].toURI().toURL());
              if (aFileName != null) {
                URL aURL = locateFileInJarFile(aFileName, entry);
                if (aURL != null && theURL == null) {
                  theURL = aURL;
                }
              }
            }
            catch (Exception e) {
              getLogger().warn("Unable to get URL for " + jarFiles[i]);
            }
          }
        }
        // Also try to find simple files in that directory
        // (if allowed by the configuration)
        if (!jarFilesOnly()) {
          File files[] = aFile.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
              return true;
            }
          });
          for (int i = 0 ; i < files.length ; i++) {
            try {
              File confFile = files[i];
              if (isValidUrl(confFile.toURI().toURL())) {
                addFileEntryToCache(confFile.getName(), confFile.toURI().toURL());
                // Is there a match?
                if (confFile.getName().equals(aFileName)) {
                  theURL = confFile.toURI().toURL();
                }
              }
            }
            catch (Exception e) {
              getLogger().warn("Unable to get URL for " + aFile.getPath());
            }
          }
        }
      }
    }
    if (getLogger().isDebugEnabled()) {
      getLogger().debug("Searched " + aFileName
          + " under " + base.toString() + ". Found: " + theURL);
    }
    return theURL;
  }
  
  protected URL locateFileInHttpElement(URL base, String aFileName) {
    URL theURL = null;
    // First, try to open the file as a Jar URL connection
    try {
      JarURLConnection juc = (JarURLConnection)base.openConnection();
      juc.setUseCaches(false);
      // called for side-effect
      juc.getJarFile();
      // This is a Jar file.
      // Add the new jar file to the list of jar files.
      JarFileInfo entry = appendJarFile(juc.getURL());
      if (aFileName != null) {
        theURL = locateFileInJarFile(aFileName, entry);
      }
    }
    catch (Exception e) {
      // That wasn't a Jar file
    }
    if (theURL == null) {
      // Process the given URL as a standard file
      if (base.getFile().equals(aFileName)) {
        theURL = base;
      }
    }
    return theURL;
  }
  
  /**
   * Determines if a simple configuration file can be loaded.
   * When signed jar files are used, files that are not in signed jar files
   * are not loaded. However, there might be exceptions to the rule and
   * specific files may be authorized even if they are not signed.
   * By default, the base JarConfigFinder always allows unsigned jar files.
   *
   * @param aUrl The URL of a configuration file
   */
  protected boolean isValidUrl(URL aUrl) {
    return true;
  }
  
  /**
   * Determines if configuration files must be stored in signed jar files.
   *
   * @return true if configuration files must be in signed jar files only
   */
  protected boolean jarFilesOnly() {
    return _jarFilesOnly;
  }
  
  /**
   * Determines if ConfigFinder client may specify absolute file names.
   */
  protected boolean acceptAbsoluteFileNames() {
    return true;
  }
  
  protected void launchJarInfoCleanupThread() {
    Runnable r = new Runnable() {
      public void run() {
        while (true) {
          try {
            Thread.sleep(DELAY_TO_RELEASE_JAR_FILES);
          }
          catch (Exception e) {}
          synchronized (_jarFileCache) {
            Iterator it = _jarFileCache.iterator();
            while (it.hasNext()) {
              JarFileInfo entry = (JarFileInfo) it.next();
              long age = System.currentTimeMillis() - entry.getCreationTime();
              if (age > DELAY_TO_RELEASE_JAR_FILES) {
                synchronized(_releasedJarFileUrls) {
                  //System.out.println("Releasing " + entry.getJarFileURL());
                  _releasedJarFileUrls.add(entry.getJarFileURL());
                }
                it.remove();
              }
            }
          }
          /*
           System.out.println("Size of Jar cache: " + _jarFileCache.size()
           + " - Size of released files: "
           + _releasedJarFileUrls.size());
           */
        }
      }
    };
    Thread t = new Thread(r);
    t.start();
  }
  
  /**
   * Add Jar files back to the cache of Jar files.
   */
  protected void refreshJarFileCache() {
    synchronized(_releasedJarFileUrls) {
      Iterator it = _releasedJarFileUrls.iterator();
      while (it.hasNext()) {
        URL entry = (URL) it.next();
        appendJarFile(entry);
        it.remove();
      }
    }
  }
}
