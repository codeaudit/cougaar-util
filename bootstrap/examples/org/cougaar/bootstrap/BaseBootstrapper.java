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

package org.cougaar.bootstrap;

public class BaseBootstrapper 
  extends Bootstrapper 
{

  private String nodename;
  protected String getNodename() { return nodename; }

  protected ClassLoader prepareVM(String classname, String[] args) {
    nodename = parseNodename(args);

    setPolicy();

    setSecurityManager();

    createJarVerificationLog();

    ClassLoader cl = super.prepareVM(classname, args);

    loadCryptoProviders();

    return cl;
  }
 
  protected void launchMain(ClassLoader cl, String classname, String[] args) {
    super.launchMain(cl, classname, args);
  }

  
  protected String parseNodename(String[] args) {
    return "unknown";
  }

  protected void setPolicy() {}
  protected void setSecurityManager() {
    String key = getNodename();
  }
  protected void createJarVerificationLog() {
    String key = getNodename();
  }
  protected void loadCryptoProviders() {
  }


}
  
  
  
