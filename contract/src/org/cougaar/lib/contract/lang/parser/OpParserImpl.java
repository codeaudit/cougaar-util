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

package org.cougaar.lib.contract.lang.parser;

import java.io.*;
import java.util.*;

import org.cougaar.lib.contract.lang.*;
import org.cougaar.lib.contract.lang.op.OpBuilder;
import org.cougaar.lib.contract.lang.type.TypeImpl;
import org.cougaar.lib.contract.lang.type.TypeListImpl;

/**
 * Implementation of <code>OpParser</code>, which parses an
 * <code>Op</code>.
 */
public class OpParserImpl implements OpParser {

  public static final boolean DEBUG = false;

  protected TypeList currentTypeList;

  protected VisitTokenizer visTokenizer;

  public void parse(
      VisitTokenizer visTokenizer) throws Exception {
    this.visTokenizer = visTokenizer;
    if (DEBUG) {
      System.out.println("parsed: ");
      System.out.println(ParenParser.toString(visTokenizer));
    }
    // set default instance assumption to "is:Object"
    setTypeList(Object.class);
  }

  public Op nextOp() throws ParseException {
    int tok = visTokenizer.nextToken();
    switch (tok) {
      case VisitTokenizer.TT_END:
        if (DEBUG) {
          System.out.println("@@ end");
        }
        return null;
      case VisitTokenizer.TT_END_OF_TREE:
        return null;
      case VisitTokenizer.TT_WORD:
        {
          String sval = visTokenizer.getWord();
          if (DEBUG) {
            System.out.println("@@ word "+sval);
          }
          Op newOp = OpBuilder.create(sval);
          if (DEBUG) {
            System.out.println("@@ created "+sval+" = "+newOp);
          }
          try {
            // attempt parse
            newOp = newOp.parse(this);
          } catch (ParseException pe) {
            // add trace and re-throw
            pe.addTrace(sval);
            throw pe;
          } catch (Exception e) {
            // create trace
            ParseException npe = new ParseException(e);
            npe.addTrace(sval);
            throw npe;
          }
          if (DEBUG) {
            System.out.println("@@ parsed "+sval+" = "+newOp);
          }
          return newOp;
        }
      case VisitTokenizer.TT_CONSTANT:
        {
          String consttype = visTokenizer.getConstantType();
          String constvalue = visTokenizer.getConstantValue();
          if (DEBUG) {
            System.out.println(
              "@@ const "+consttype+" "+constvalue);
          }
          Op newOp = OpBuilder.createConstantOp(consttype, constvalue);
          if (DEBUG) {
            System.out.println(
              "@@ const "+consttype+" "+constvalue+" = "+newOp);
          }
          return newOp;
        }
      default:
        throw new InternalError("No such token: "+tok);
    }
  }

  /**
   * @see org.cougaar.lib.contract.lang.OpParser
   */
  public void setTypeList(final TypeList tl) {
    if (tl == null) {
      throw new IllegalArgumentException(
        "Parser given null TypeList");
    }
    currentTypeList = tl;
  }

  public void setTypeList(final Class cl) {
    currentTypeList = new TypeListImpl(TypeImpl.getInstance(false, cl));
  }

  public int addType(final Type type) {
    return currentTypeList.add(type);
  }

  public TypeList getTypeList() {
    return currentTypeList;
  }

  public TypeList cloneTypeList() {
    return (TypeList)currentTypeList.clone();
  }
}
