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

package org.cougaar.lib.contract.lang;

import java.io.*;
import java.util.*;

import org.cougaar.core.util.Operator;
import org.cougaar.core.util.OperatorFactory;

import org.cougaar.lib.contract.lang.cache.ClassCache;
import org.cougaar.lib.contract.lang.parser.*;

/**
 * Implementation of <code>org.cougaar.core.util.OperatorFactory</code> -- used to hide
 * package dependency and some internals.
 * <p>
 * @see org.cougaar.core.util.OperatorFactory
 */
public class OperatorFactoryImpl extends OperatorFactory {

  public OperatorFactoryImpl() {
    // specify standard COUGAAR packages.
    //
    // This should probably be moved from a static configuration to a 
    // field of this factory!
    String[] packages = new String[7];
    packages[0] = "java.lang.";
    packages[1] = "java.util.";
    packages[2] = "org.cougaar.planning.ldm.plan.";
    packages[3] = "org.cougaar.planning.ldm.asset.";
    packages[4] = "org.cougaar.planning.ldm.measure.";
    packages[5] = "org.cougaar.glm.";
    packages[6] = "org.cougaar.glm.ldm.asset.";
    ClassCache.setPackages(packages);
  }

  public Operator create(
      final int style, 
      final Object inObj) throws Exception {
    try {
      // build tree tokenizer
      BufferedVisitor bufVis = new BufferedVisitor();
      if ((style & Op.PAREN_FLAG) != 0) {
        ParenParser.parse(bufVis, inObj);
      } else if ((style & Op.XML_FLAG) != 0) {
        XMLParser.parse(bufVis, inObj);
      } else {
        throw new IllegalArgumentException(
          "Unknown OpParser.parse style: "+style);
      }
      OpParserImpl opi = new OpParserImpl();
      opi.parse(bufVis.getVisitTokenizer());
      return opi.nextOp();
    } catch (ParseException pe) {
      // clear Java-stack and re-throw
      throw (ParseException)pe.fillInStackTrace();
    } catch (Exception e) {
      // create trace
      ParseException npe = new ParseException(e.getMessage());
      npe.addTrace("OperatorFactory.create()");
      throw npe;
    }
  }
}
