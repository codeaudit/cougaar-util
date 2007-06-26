#!/usr/bin/perl
# -*- Perl -*-

# <copyright>
#  Copyright 2001-2006 BBNT Solutions, LLC
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

#
# Parse the output of bootstrapper class loading logs.
#
# For usage, run with "--help".  The defaults are pretty good.
# 
# Required System properties to create the input log(s):
#   -Dorg.cougaar.bootstrap.classloader.class=org.cougaar.bootstrap.LoggingClassLoader
#   -Dorg.cougaar.bootstrap.classloader.log=class_usage.log
#
# For more info on the input file, see:
#   bootstrap/src/org/cougaar/bootstrap/LoggingClassLoader.java
#
# Specify the log(s) on the command line, e.g.:
#   ./classUsage.plx class_usage1.log class_usage2.log > class_stats.txt
#
# The output can be grepped for useful info:
#
#   For each jar, show how many classes were used:
#     grep "^count" class_stats.txt | sort -n -k6
#   E.g. for "core" the stats might be:
#     classes loaded:      735
#     classes not loaded:  525
#     total class count:  1260
#     percent loaded:       58
#
#   For each jar, show how many bytes were used:
#     grep "^size" class_stats.txt | sort -n -k6
#   E.g. for "core" the stats might be:
#     class bytes loaded:      1909007
#     class bytes not loaded:  1252804
#     total classfile bytes:   3161811
#     percent bytes loaded:         60
#
#   List used classes (should match "class_usage.log"):
#     grep "^USED" class_stats.txt
#
#   List unused classes (i.e. jar listing minus USED listing):
#     grep "^dead" class_stats.txt
#

my %files;
my %jars;
my %names;

my $s=" ";
my $opt_inner=0;
my $opt_merge=0;
my $opt_used=1;
my $opt_dead=1;
my $opt_sizes=0;

my $allUsedCount=0;
my $allDeadCount=0;
my $allUsedSize=0;
my $allDeadSize=0;

parse_args();
if ($opt_merge) {
  merge_logs();
} else {
  read_jar_names();
  measure_usage();
  print_overall();
}
exit(0);

sub usage {
  print STDERR <<EOF;
Usage: $0 [OPTION] [FILE]...
Parse Cougaar bootstrap classloader logging output from log FILE(s).

 -merge            merge classloader logs without counting 
                     usage (default=false)
 -inner            count uses of any outer/inner class as used 
                     if any other matching class is used.  For
                     example, if "org.Foo" is used, count 
                     "org.Foo\$1" as used even if it was not used.
                     (default=false)
 -inner=STRING     same as "-inner", but prints the specified
                     STRING instead of USED.
 -used=BOOLEAN     print used classes (default=true)
 -dead=BOOLEAN     print unused classes (default=true)
 -sep=STRING       separator string (default=" ")
 -sizes=BOOLEAN    print per-class byte sizes (default=false)
 -verbose          set -used=true, -dead=true, -sizes=true

Report bugs to http://www.cougaar.org/bugs
EOF
  exit(1);
}

sub parse_args {
  foreach (@args = @ARGV) {
    chomp;
    if (/^-+(.*)/) {
      my $arg = $1;
      if ($arg eq "merge") {
        $opt_merge=1;
      } elsif ($arg eq "inner") {
        $opt_inner="USED";
      } elsif ($arg =~ /^inner=(.+)/) {
        if ($1 eq "false" || $1 eq "dead") {
          $opt_inner=0;
        } else {
          $opt_inner=$1;
        }
      } elsif ($arg =~ /^sep=(.+)/) {
        $s=$1;
      } elsif ($arg =~ /^used=(.*)/) {
        $opt_used=("true" eq $1);
      } elsif ($arg =~ /^dead=(.*)/) {
        $opt_dead=("true" eq $1);
      } elsif ($arg =~ /^sizes=(.*)/) {
        $opt_sizes=("true" eq $1);
      } elsif ($arg eq "verbose") {
        $opt_used=1;
        $opt_dead=1;
        $opt_sizes=1;
      } else {
        usage();
      }
    } else {
      $files{$_}="1";
    }
  }
}

sub merge_logs {
  my $num_files = keys %files;
  if ($num_files <= 0) {
    return;
  } elsif ($num_files == 1) {
    my $single_file = (keys %files)[0];
    exec "cat $single_file";
  }
# build a big hash maps of all unique
#   entries ("jar class")
# and
#   urls ("# URL jar url")
  my %entries;
  my %urls;
  my $firstTime=1;
  foreach (keys %files) {
    print "#reading $_\n";
    open(FD, "<$_")
      or die "Unable to open $_: $!\n";
    while (<FD>) {
      my $line = $_;
      chomp;
      if (/^[^#](.*)/) {
        my $entry = $1;
        if ($firstTime || !defined($entries{$entry})) {
          $entries{$entry} = "1";
          print $line;
        }
      } elsif (/^# URL (\S+) (.*)/) {
        my $name = $1;
        my $url = $2;
        if ($firstTime) {
          $urls{$name} = $url;
          print $line;
        } else {
          my $prior = $urls{$name};
          if (!defined($prior)) {
            $urls{$name} = $url;
            print $line;
          } elsif ($prior ne $url) {
            print "#conflict $name already recorded as $prior, ignoring $url";
          }
        }
      }
    }
    close FD;
    $firstTime=0;
  };
}

sub read_jar_names {
# read jars names from files
#
# should refactor into above "merge_logs()"
  foreach (keys %files) {
    print "#reading $_\n";
    open(FD, "<$_")
      or die "Unable to open $_: $!\n";
    while (<FD>) {
      chomp;
      if (/^([^#].*) /) {
        my $name = $1;
        $names{$name} = "1";
      } elsif (/^# URL (\S+) (.*)/) {
        my $name = $1;
        my $url = $2;
        if ($url =~ /^file:(\/.+)/) {
          my $file = $1;
          my $prior = $jars{$name};
          if (!defined($prior)) {
            $jars{$name} = $file;
          } elsif ($prior ne $file) {
            print "#conflict $name already recorded as $prior, ignoring $file";
          }
        } else {
          print "#skipping non-\"file:/\" url: $url\n";
        }
      }
    }
    close FD;
  };
}


sub measure_usage {
  while (($name, $file) = each %jars) {
    my $isUsed = (defined($names{$name}));
    my %used;
    if ($isUsed) {
      print "#re-scanning for uses of jar $name\n";
# extract uses
      foreach (keys %files) {
        chomp;
        print "#reading log \"$_\"\n";
        open(FD, "<$_")
          or die "Unable to open $_: $!\n";
        while (<FD>) {
          chomp;
#print "# Line: $_\n";
          if (/^$name (.*)/) {
            my $class = $1;
            if (defined($used{$class})) {
# already recorded use
            } else {
              $used{$class} = "1";
            }
          }
        }
        close FD;
      }
    }
# extract all jar contents
    my $usedCount=0;
    my $deadCount=0;
    my $usedSize=0;
    my $deadSize=0;
    print "#listing jar $name contents from file $file\n";
    open(DUMP, '-|', "unzip -v $file") 
      or die "Unable to list $file contents: $!\n";
    while (<DUMP>) {
      chomp;
      my $line = $_;
#print "# read $line\n";
#
# expecting something like:
#  "  9876 blah  5432 blah blah blah org/Foo.class"
# where:
#  9876 is the uncompressed size
#  5432 is the compressed size
#  org/Foo is the class name
#
      next unless $line =~ /^\s*(\d+)(\s+\S+\s+)(\d+)(.+) (\S+)\.class$/;
      my $size = $1;
# unused, but here for future enhancement
      my $compr = $3;
      my $class = $5;
      $class =~ s/\//\./g;
      my $classUsed=0;
      if ($isUsed) {
        if (defined($used{$class})) {
# typical "USED" for usage
          $classUsed="USED";
        } elsif ($opt_inner && $class =~ /^([^\$]+)\$.*/) {
          my $outer = $1;
          if (defined($used{$outer})) {
# use of outer class forces used=true, prints "USED" by default
            $classUsed=$opt_inner;
          }
        }
      }
      if ($classUsed) {
        $usedCount++;
        $usedSize += $size;
        if ($opt_used) {
          print "$classUsed$s$name$s$class";
          print "$s$size" if $opt_sizes;
          print "\n";
        }
      } else {
        $deadCount++;
        $deadSize += $size;
        if ($isUsed && $opt_dead) {
          print "dead$s$name$s$class";
          print "$s$size\n" if $opt_sizes;
          print "\n";
        }
      }
    }

    print_stats(
        $name,
        $usedCount, $deadCount,
        $usedSize, $deadSize);

# increment overall counters
    $allUsedCount += $usedCount;
    $allDeadCount += $deadCount;
    $allUsedSize += $usedSize;
    $allDeadSize += $deadSize;
  }
}

sub print_overall() {
  print_stats(
      "OVERALL",
      $allUsedCount, $allDeadCount,
      $allUsedSize, $allDeadSize);
}

sub print_stats {
  my ($name, $usedCount, $deadCount, $usedSize, $deadSize) = @_;
  my $totalCount = $usedCount + $deadCount;
  my $ratioCount = ratio($usedCount, $deadCount);
  print "#class_count <name> <used> <dead> <total> <percentUsed>\n";
  print "count$s$name$s$usedCount$s$deadCount$s$totalCount$s$ratioCount\n";
  my $totalSize = $usedSize + $deadSize;
  my $ratioSize = ratio($usedSize, $deadSize);
  print "#byte_size <name> <used> <dead> <total> <percentUsed>\n";
  print "size$s$name$s$usedSize$s$deadSize$s$totalSize$s$ratioSize\n";
}

sub ratio {
  my ($u, $d) = @_;
  return 0 if $u <= 0;
  my $r = int (100 * $u/($u + $d));
  $r = 1 if $r <= 0;
  return $r;
}

