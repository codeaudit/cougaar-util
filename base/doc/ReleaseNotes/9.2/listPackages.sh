#! /bin/sh

# <copyright>
#  Copyright 2002 BBNT Solutions, LLC
#  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
# 
#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the Cougaar Open Source License as published by
#  DARPA on the Cougaar Open Source Website (www.cougaar.org).
# 
#  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
#  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
#  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
#  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
#  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
#  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
#  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
#  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
#  PERFORMANCE OF THE COUGAAR SOFTWARE.
# </copyright>

# This script scans the "src" directories (modules) and 
# generates an HTML file.
#
# The result is a file that lists all packages.

if test `hostname` != eiger; then 
  echo This script only runs on host \"eiger\";
  exit 1;
fi

srcpath=/build/cougaar/builds/nightly-HEAD/latest/src
tempfile=$HOME/tmp.txt
outputfile=$HOME/packages.html

echo creating $outputfile

echo > $tempfile
echo "<html><body><pre>" > $outputfile
cd $srcpath
for x in *; do 
  if test -d $x/src && test $x != unified; then 
    cd $x/src;
    find .  -type d >> $tempfile;
    cd ../..; 
  fi; 
done
cat $tempfile | sort | uniq >> $outputfile
rm $tempfile
echo "</pre></body></html>" >> $outputfile

echo done
