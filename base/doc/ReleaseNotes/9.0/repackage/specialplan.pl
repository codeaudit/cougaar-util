#!/usr/bin/perl
# -*- Perl -*-

# <copyright>
#  Copyright 2001 BBNT Solutions, LLC
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

# deals with very special cases in the org.cougaar.planning.ldm.plan package

# adds global imports
use Cwd;
use File::Copy;
use File::Path;
use File::Find;

$tmp = "addimports";
#local($dir) = $ARGV[0];
&main(@ARGV);

sub main {
    local(@args)=@_;
    local($dir)= $args[0];
    $found = 0;
    process_dir($dir);
    print "\n Found:", $found, " files to change.\n";
    print "done\n";
}

sub process_dir {
    local($dir)=@_;
    print("# Processing directory $dir");
    
    local(@files)=findfiles($dir);
    local($_);
    foreach $_ (@files) {
	$file = $_;
	#local($dev,$ino,$mode,$nlink,$uid,$gid) = lstat($_);
#	print "\nfile name is: ", $file;
	if (substr($file, -29) eq "ClusterObjectFactoryImpl.java") {
	    $found++;
	    process_domain($_);
	} elsif (substr($file, -9) eq "Task.java") {
	    $found++;
	    process_blackboardforapi($_);
	} elsif (substr($file, -16)eq "PlanElement.java") {
	    $found++;
	    process_blackboardforapi($_);
	} elsif (substr($file, -13)eq "TaskImpl.java") {
	    $found++;
	    process_blackboardforimpl($_);
	} elsif (substr($file, -20)eq "PlanElementImpl.java") {
	    $found++;
	    process_blackboardforimpl($_);
	} elsif (substr($file, -29)eq "RelationshipScheduleImpl.java") {
	    $found++;
	    process_uid($_);
	}
    }
}

sub findfiles {
  local($dir) = @_;
  local(@stuff);
  local($code)= sub { 
      # skip cvs dirs
    if (/^CVS$/) {
      $File::Find::prune = 1;
      return;
    }
    #(($dev,$ino,$mode,$nlink,$uid,$gid) = lstat($_));
    push @stuff, $File::Find::name;
  };
  local(%ref);
  $ref{"wanted"}=$code;
  &find(\%ref, $dir);
  @stuff;
}

sub process_domain {
    local($file) = @_;
    #print "\nprocessing file:", $file;
    open(IN, $file);
    open(OUT, ">".$tmp);
    while (<IN>) {
	$input_line = $_;
	printf (OUT $input_line);
	# find the package line and put the import lines after it.
	if (substr($input_line,0,7) eq "package") {
	    $found++;
	    printf OUT ("\nimport org.cougaar.core.domain.LDMServesPlugIn\;\n");
	}

    }
    close(OUT);
    close(IN);
    rename($tmp, $file);
}

sub process_blackboardforapi {
    local($file) = @_;
    #print "\nprocessing file:", $file;
    open(IN, $file);
    open(OUT, ">".$tmp);
    while (<IN>) {
	$input_line = $_;
	printf (OUT $input_line);
	# find the package line and put the import lines after it.
	if (substr($input_line,0,7) eq "package") {
	    $found++;
	    printf OUT ("\nimport org.cougaar.core.blackboard.Publishable\;\n");
	    printf OUT ("\nimport org.cougaar.core.blackboard.ChangeReport\;\n");
	}

    }
    close(OUT);
    close(IN);
    rename($tmp, $file);
}

sub process_blackboardforimpl {
    local($file) = @_;
    #print "\nprocessing file:", $file;
    open(IN, $file);
    open(OUT, ">".$tmp);
    while (<IN>) {
	$input_line = $_;
	printf (OUT $input_line);
	# find the package line and put the import lines after it.
	if (substr($input_line,0,7) eq "package") {
	    $found++;
	    printf OUT ("\nimport org.cougaar.core.blackboard.ActiveSubscriptionObject\;\n");
	    printf OUT ("\nimport org.cougaar.core.blackboard.Subscriber\;\n");
	    printf OUT ("\nimport org.cougaar.core.blackboard.PublishableAdapter\;\n");
	}

    }
    close(OUT);
    close(IN);
    rename($tmp, $file);
}

sub process_uid {
    local($file) = @_;
    #print "\nprocessing file:", $file;
    open(IN, $file);
    open(OUT, ">".$tmp);
    while (<IN>) {
	$input_line = $_;
	printf (OUT $input_line);
	# find the package line and put the import lines after it.
	if (substr($input_line,0,7) eq "package") {
	    $found++;
	    printf OUT ("\nimport org.cougaar.core.util.UID\;\n");
	}

    }
    close(OUT);
    close(IN);
    rename($tmp, $file);
}







