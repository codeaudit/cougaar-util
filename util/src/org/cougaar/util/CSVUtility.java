/*
 * <copyright>
 *  
 *  Copyright 2003-2004 BBNT Solutions, LLC
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
 * See original perl version of the parser (from japhy) at the end.
 */

package org.cougaar.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CSVUtility {
  private CSVUtility() {}

  private static final String patternPrefix = "\\G\\s*(?!$)(\\\"[^\\\"]*(?:\\\"\\\"[^\\\"]*)*\\\"|[^";
  private static final String patternMiddle = "\\\"]*)";
  private static final String patternSuffix = "?";
  private static final Pattern commaP = buildPattern(',');

  private static Pattern buildPattern(char comma) {
    return Pattern.compile(patternPrefix + comma + patternMiddle + comma + patternSuffix);
  }

  private static final String[] emptyStrings = {};

  /** Parse a single string in mocrosift-like CSV format **/
  public static String[] parse(String str) {
    return parse(str, commaP);
  }
  
  public static String[] parse(String str, char sep) {
    return parse(str, buildPattern(sep));
  }

  private static String[] parse(String str, Pattern p) {
    List l = parseToList(str, p);
    return (String[]) l.toArray(emptyStrings);
  }

  public static List parseToList(String str) {
    return parseToList(str,commaP);
  }

  public static List parseToList(String str, char sep) {
    return parseToList(str, buildPattern(sep));
  }

  public static List parseToList(String str, Pattern p) {
    List l = new ArrayList();
    Matcher m = p.matcher(str);
    while (m.find()) {
      String v = m.group(1);
      v.trim();
      if (v.length() > 1 && v.startsWith("\"") && v.endsWith("\"")) {
        v = v.substring(1,v.length()-1);
        v = v.replaceAll("\"\"", "\"");
      }
      l.add(v);
    }
    return l;
  }

  /** @deprecated Use parseToList instead **/
  public static Collection parseToCollection(String str) {
    return parseToList(str, commaP);
  }
}  

/*
From the perl at http://japhy.perlmonk.org/

@fields = parseCSV(
  'this,that,"those,these, and mine","""I want those,"" he said"'
);

print map "($_)", @fields;


sub parseCSV {
  my $str = @_ ? shift : $_;
  my @ret;
  while ($str =~ /\G\s*(?!$)("[^"]*(?:""[^"]*)*"|[^,"]*),?/g) {
    push @ret, $1;
    $ret[-1] =~ s/\s+$//;
    if ($ret[-1] =~ s/^"//) { chop $ret[-1]; $ret[-1] =~ s/""/"/g }
  }
  return @ret;
}


__END__

$str =~ m{
  \G         # where the last match left off (defaults to ^)
  \s*        # any whitespace (we don't save this)
  (?!$)      # NOT followed by end-of-string
  (          # save this to $1 (this is ONE CSV)
    "        # a "
    [^"]*    # followed by 0 or more non-"
    (?:      # then...
      ""     # a "" (which is a CSV 'escape' for a ")
      [^"]*  # followed by 0 or more non-"
    )*       # 0 or more times
    "        # the ending "
    |        # *OR*
    [^,"]*   # 0 or more non-, and non-" characters
  )
  ,?         # with an optional ending comma
}gx;
*/
