#!/usr/bin/perl
# -*- Perl -*-

# <copyright>
#  
#  Copyright 2003-2004 BBNT Solutions, LLC
#  under sponsorship of the Defense Advanced Research Projects
#  Agency (DARPA).
# 
#  You can redistribute this software and/or modify it under the
#  terms of the Cougaar Open Source License as published on the
#  Cougaar Open Source Website (www.cougaar.org).
# 
#  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
#  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
#  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
#  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
#  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
#  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
#  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
#  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
#  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
#  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
#  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#  
# </copyright>


# fixCID fixes cougaar 9.* code which references CougaarIdentifier and NodeIdentifier
# This script does *not* take care of the following cases:
# - if your code does a wildcard import like "import org.cougaar.core.agent.*;", you
# may need to add "import org.cougaar.core.mts.MessageAddress" as well. You can
# try setting $fixingWildcards = 1 below, but you will probably have lots of redundant
# bulk imports left over needing manual fixing.
# - Multicast references - instead of MessageAddress.COMMUNITY for instance, the key
# is MessageAddress.MULTICAST_COMMUNITY.  We don't do this here because it will make
# too many mistakes that a human wouldn't.
# - Unusual MessageAddress constructors, including those with MessageAttributes.  There
# are too many cases (and very few uses) to cover safely without the script needing to
# know the argument types.  Most of the cases will be covered automatically by the
# conversion of constructor->factory, but the rest should result in straightforward
# to fix compile errors.

# usage example:
# find . -name "*.java" | perl fixCID.pl
#

$fixingWildcards=0;		# set to true to try catching bulk imports

$total=0;
$fixed=0;
$repls = 0;

print "Fixing MessageAddress family for Cougaar 10.0\n";

while (<>) {
  chop;
  $file = $_;

  &process($file);
}

print "Total files = $total\n";
print "Total files modified = $fixed\n";
print "Total lines modified = $repls\n";

exit(0);

sub process {
  local($file) = @_;
  local($found)=0;		# if != 0, copy the file back
  local($tmp) = "/tmp/deleteme";
  open(IN, "<$file");
  unlink($tmp);
  open(OUT, ">$tmp");
  while (<IN>) {
    $found++ if (s/org\.cougaar\.core\.agent\.ClusterIdentifier/org\.cougaar\.core\.mts\.MessageAddress/g);
    $found++ if (s/new ClusterIdentifier/MessageAddress.getMessageAddress/g);
    $found++ if (s/ClusterIdentifier/MessageAddress/g);
    $found++ if (s/cleanToString/toString/g);

    $found++ if (s/org\.cougaar\.core\.node\.NodeIdentifier/org\.cougaar\.core\.mts\.MessageAddress/g);
    $found++ if (s/new NodeIdentifier/MessageAddress.getMessageAddress/g);
    $found++ if (s/NodeIdentifier/MessageAddress/g);

    $found++ if (s/new AttributeBasedAddress/AttributeBasedAddress.getAttributeBasedAddress/g);

    if ($fixingWildcards) {
      if (/import org\.cougaar\.core\.agent\.\*/) {
	$found++;
	print OUT "import org.cougaar.core.mts.*;\n";
      }
      if (/import org\.cougaar\.core\.node\.\*/) {
	$found++;
	print OUT "import org.cougaar.core.mts.*;\n";
      }
    }

    $found++ if (s/new MessageAddress/MessageAddress\.getMessageAddress/g);

    print OUT "$_";
  }
  close(OUT);
  close(IN);

  # copy it back
  if ($found) {
    $fixed++;
    $repls = $repls+$found;

    unlink($file);
    open(IN, "<$tmp");
    open(OUT, ">$file");
    while(<IN>)  {
      print OUT "$_";
    }
    close(OUT);
    close(IN);
  }

  #unlink($tmp);
  $total++;
}
