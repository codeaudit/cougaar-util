/*
 * <copyright>
 *  Copyright 1999-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.contract.lang;

import java.io.*;
import java.util.*;

import org.cougaar.util.Operator;
import org.cougaar.util.OperatorFactory;

import org.cougaar.lib.contract.lang.cache.ClassCache;
import org.cougaar.lib.contract.lang.parser.*;

/**
 * Implementation of <code>org.cougaar.util.OperatorFactory</code> -- used to hide
 * package dependency and some internals.
 * <p>
 * @see org.cougaar.util.OperatorFactory
 */
public class OperatorFactoryImpl extends OperatorFactory {

  public OperatorFactoryImpl() {
    // specify standard ALP packages.
    //
    // This should probably be moved from a static configuration to a 
    // field of this factory!
    String[] packages = new String[7];
    packages[0] = "java.lang.";
    packages[1] = "java.util.";
    packages[2] = "org.cougaar.domain.planning.ldm.plan.";
    packages[3] = "org.cougaar.domain.planning.ldm.asset.";
    packages[4] = "org.cougaar.domain.planning.ldm.measure.";
    packages[5] = "org.cougaar.domain.glm.";
    packages[6] = "org.cougaar.domain.glm.ldm.asset.";
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
