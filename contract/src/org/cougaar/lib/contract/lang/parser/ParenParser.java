/*
 * <copyright>
 *  Copyright 1999-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.contract.lang.parser;

import java.io.*;
import java.util.*;

import org.cougaar.lib.contract.lang.*;

/**
 * A "paren" semi-lisp styled <code>Op</code> parser which can be
 * used to control a <code>TreeVisitor</code>.
 * <p>
 * This is a fairly simple conversion which breaks the parenthesis
 * apart and converts shorthand <code>(a b)</code> to <code>(a (b))</code>.
 * <pre>
 * For example,<code>
 *   (and a (b) (c d (e) "f" g) h)</code>
 * becomes essentially<code>
 *   (and (a) (b) (c (d) (e) ("f") (g)) (h))</code></pre>.
 * This is further simplified to the <code>TreeVisitor</code> syntax:
 *   and a) b) c d) e) "f" g) h)
 * <p>
 * @see XMLParser for XML implementation
 */
public class ParenParser {

  private ParenParser() { }

  public static void parse(
      TreeVisitor visitor, Object o) throws ParseException {
    if (o instanceof String) {
      // convert string to reader
      o = new StringReader((String)o);
    } else if (o instanceof InputStream) {
      o = new InputStreamReader((InputStream)o);
    } else if (!(o instanceof Reader)) {
      throw new ParseException(
        "Parser unable to read from "+
        ((o != null) ? o.getClass().getName() : "null"));
    }
    // build tokenizer
    StreamTokenizer st = new StreamTokenizer((Reader)o);
    // allow XML name characters in words
    st.wordChars('.', '.');
    st.wordChars('-', '-');
    st.wordChars('_', '_');
    st.wordChars(':', ':');
    // allow Java-style comments
    st.slashStarComments(true);
    st.slashSlashComments(true);

    visitor.initialize();

    int depth = 0;

readTokens:
    while (true) {
      // read the next token
      int token;
      try {
        token = st.nextToken();
      } catch (IOException ioe) {
        throw new ParseException(
          "Parser received IO Exception \""+ioe+"\"");
      }
      // parse the token
      switch (token) {
        case StreamTokenizer.TT_EOF:
          break readTokens;
        case (int)')':
          // END
          if ((--depth) < 0) {
            // treat extra ")"s as "EndOfTree"
            break readTokens;
          }
          visitor.visitEnd();
          break;
        case StreamTokenizer.TT_NUMBER: 
          throw new ParseException(
            "Parser expects numbers to use \"const\", e.g.: "+
            "(const \"double\" \""+st.nval+"\")");
        case StreamTokenizer.TT_WORD:
          // shorthand "word" for "(word)"
          visitor.visitWord(st.sval);
          visitor.visitEnd();
          break;
        default:
          // ordinary character.
          if (st.ttype == '(') {
            // expecting WORD or STRING
            int tok0;
            try {
              tok0 = st.nextToken();
            } catch (IOException ioe) {
              throw new ParseException(
                "Parser received IO Exception \""+ioe+"\"");
            }
            if (tok0 == StreamTokenizer.TT_WORD) {
              // word
              ++depth;
              visitor.visitWord(st.sval);
            } else if (st.ttype == '"') {
              // quoted string -- should be followed by ")"!
              visitor.visitConstant(st.sval);
              int tok1;
              try {
                tok1 = st.nextToken();
              } catch (IOException ioe) {
                throw new ParseException(
                  "Parser received IO Exception \""+ioe+"\"");
              }
              if (tok1 == (int)')') {
                // typical string
              } else if (tok0 == StreamTokenizer.TT_EOF) {
                break readTokens;
              } else {
                throw new ParseException(
                  "Parser expecting String to be followed by \")\"");
              }
            } else {
              throw new ParseException(
                "Parser expecting Word or String, not "+st.toString());
            }
            break;
          } else if (st.ttype == '"') {
            // quoted string
            visitor.visitConstant(st.sval);
            break;
          } else {
            // single-letter non-ascii word?
            throw new ParseException(
              "Parser given invalid character: "+st.ttype);
          }
      }
    }

    for (; depth > 0; depth--) {
      // add missing ")"s
      visitor.visitEnd();
    }
    visitor.visitEndOfTree();
  }

  public static StringVisitor getStringVisitor() {
    return new ParenStringVisitor();
  }

  public static String toString(VisitTokenizer visTokenizer) {
    TreeVisitor visitor = getStringVisitor();
    VisitReplayer.replay(visitor, visTokenizer);
    return visitor.toString();
  }

  public static void main(String[] args) {
    String input = "(and (a) b (c d (e";
    System.out.print("Given: "+input+"\nParsed: ");
    try {
      TreeVisitor strVis = ParenParser.getStringVisitor();
      ParenParser.parse(strVis, input);
      System.out.println(strVis.toString());
    } catch (Exception e) {
      System.out.println("\n######\n"+e);
      e.printStackTrace();
    }
  }
}
