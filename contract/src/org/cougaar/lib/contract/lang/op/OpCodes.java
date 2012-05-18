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

package org.cougaar.lib.contract.lang.op;


public interface OpCodes {

  /** All <code>Op</code>s have unique integer ID **/
  // constant
  public static final int CONSTANT_ID        =  0;
  public static final int GET_ID             =  1;
  // list
  public static final int ALL_ID             =  2;
  public static final int EMPTY_ID           =  3;
  public static final int EXISTS_ID          =  4;
  // logical
  public static final int AND_ID             =  5;
  public static final int FALSE_ID           =  6;
  public static final int NOT_ID             =  7;
  public static final int OR_ID              =  8;
  public static final int TRUE_ID            =  9;
  // reflect
  public static final int APPLY_ID           = 10;
  public static final int FIELD_ID           = 11;
  public static final int INSTANCEOF_ID      = 12;
  public static final int METHOD_ID          = 13;
  public static final int REFLECT_ID         = 14;
  public static final int THIS_ID            = 15;

  /** All <code>Op</code>s have unique names **/
  // constant
  public static final String CONSTANT_NAME        = "const";
  public static final String GET_NAME             = "get";
  // list
  public static final String ALL_NAME             = "all";
  public static final String EMPTY_NAME           = "empty";
  public static final String EXISTS_NAME          = "exists";
  // logical
  public static final String AND_NAME             = "and";
  public static final String FALSE_NAME           = "false";
  public static final String NOT_NAME             = "not";
  public static final String OR_NAME              = "or";
  public static final String TRUE_NAME            = "true";
  // reflect
  public static final String APPLY_NAME           = "apply";
  public static final String FIELD_NAME           = "field";
  public static final String INSTANCEOF_NAME      = "instanceof";
  public static final String METHOD_NAME          = "method";
  public static final String REFLECT_NAME         = "reflect";
  public static final String THIS_NAME            = "this";
}
