#!/usr/bin/perl
# -*- Perl -*-

# updatecr reads a list of files from the standard input, updating
# any copyright notices it finds which conform to a standard format:

$fixingWildcards=0;		# set to true to try catching bulk imports

$total=0;
$fixed=0;
$repls = 0;

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
