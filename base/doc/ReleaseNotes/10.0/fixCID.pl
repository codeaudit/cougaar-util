#!/usr/bin/perl
# -*- Perl -*-

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
