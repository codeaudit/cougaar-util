#!/usr/bin/perl
# -*- Perl -*-
# PlugInToPlugin changes all instances of PlugIn to Plugin in file names 
# and inside files.
# usage:
# PlugInToPlugin [-cmvp] dir pkgdef+
#  -m  actually move files which match package prefixes.
#  -v  be verbose.  Print every command executed an report on
#      any files changed.
#  -p  dont actually do anything - just pretend.
#  -c  call CVS to remove files with PlugIn in the name from the repository 
# and add files with new names to the repository
#

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


# stuff for internal find
use Cwd;
use File::Copy;
use File::Path;
use File::Find;

require "getopts.pl";
&Getopts("cmvphd:");
&usage() if $opt_h;
$cvsP = $opt_c;
$moveP = $opt_m;
$verboseP = $opt_v;
$pretendP = $opt_p;
$realP = ! $pretendP;
$prefix = $opt_d;

$logP = $realP;

&usage() if ($#ARGV < 0);

# init globals
@abbr_old;
@abbr_new;
@pkg_old;
@pkg_new;
@dir_old;
@dir_new;

$stat_files_modified = 0;
$stat_files_moved = 0;

$tmp = "/tmp/core-repackage.$$";

$PlugIn = "PlugIn";
$Plugin = "Plugin";

&main(@ARGV);

sub lprint {
    local($rpt) = @_;
    printf STDERR "$rpt\n" if $verboseP;
    printf LOG "$rpt\n" if $logP;
}


# package convert a whole file, using a temp file
sub process_file {
    local($file) = @_;
    $changed=0;
    if (isBinary($file)) {
	lprint("# not changing binary file $file");
    } else {
	open(IN, "<$file");
	open(OUT, ">$tmp") if ($realP);

	while (<IN>) {
	    # cleanup crlfs
#	  $changed++ if (s/\r\n/\n/g);
#	  $changed++ if (s/\r/\n/g);
	  $changed++ if (s/$PlugIn/$Plugin/g);
	    print OUT "$_" if ($realP);
	}
	close IN;
	close OUT if ($realP);
    }

    lprint("# modified $file ($changed changes)") if ($changed);

    local ($newname);
    if ($moveP) {
      $newname = $file;
      $newname =~ s/$PlugIn/$Plugin/;
    } else {
      $newname = $file;
    }
    
    $newname = "$prefix/$newname" if ($prefix);
    local ($renamed) = !($file eq $newname);

    lprint("# Warning: NOT moving $file") if (!$renamed && $moveP);

    if ($changed || $renamed) {
	$stat_files_modified++ if $changed;
	if ($renamed) {
	    $stat_files_moved++;
	    lprint("mv $file $newname");
	    lprint("cvs remove $file");
	    lprint("cvs add $newname");
	    check_target_dir($newname);
	    copyfile($tmp, $newname) if ($realP);
	    system("cvs remove $file") if ($realP & $cvsP);
	    system("cvs add $newname") if ($realP & $cvsP);
	    unlink($file) if ($realP);  
	} else {
	    copyfile($tmp, $file) if ($realP);
	}
	unlink($tmp) if ($realP);
    }
    return $changed;
}

%checked_dirs;
sub check_target_dir {
    local($path) = @_;
    local(@v)=split('/',$path);
    local($l)=$#v;
    local($i);
    # don't check the final component, since that is the filename
    for ($i=0;$i<$l;$i++) {
	local($d) = join '/', @v[0..$i];
	if (! $checked_dirs{$d}) {
	    $checked_dirs{$d}=1;
	    careful_mkdir($d) if (! -d $d);
	}		
    }
}

# return true when argument names a binary file
sub isBinary {
    local($_) = @_;
    /\.(gif|jpg|png|zip|jar)$/;
}

# converts the packages of a string
sub convert_pkg {
    local ($_) = @_;
    study $_;
    local($i);
    local($l)=$#pkg_old;
    for ($i=0;$i<=$l;$i++) {
	local($old)=$pkg_old[$i];
	local($new)=$pkg_new[$i];
	if ( s/\b$old/$new/g ) {
	    # global used to detect if there were any changes made
	    $changed++;
	}
    }
    return $_;
}

sub guess_rename {
    local ($_) = @_;
    local($bak)=$_;
    local ($base)="";
    if (/([^\/]*\/)(.*)/) {
      $base=$1;
      $_=$2;
    }
    # study $_; # don't bother
    local($i);
    local($l)=$#pkg_old;
    for ($i=0;$i<=$l;$i++) {
	local($old)=$file_old[$i];
	local($new)=$file_new[$i];
	if ( s/^$old/$new/g ) {
	  return "$_";		# bail at the first match
	}
    }
    # no match, so return $_
    #return $bak;
    return $_;
}    

sub copyfile {
    local($old, $new) = @_;
    unlink($new);
    open(IN, "<$old");
    open(OUT, ">$new");
    while(<IN>)  {
      print OUT "$_";
    }
    close(OUT);
    close(IN);
}

sub report_stats {
    lprint("# Files modified: $stat_files_modified\n");
    lprint("# Files moved: $stat_files_moved\n");
}

sub usage {
   print STDERR "Usage: PlugInToPlugin [-mvph] [-d dest] dir
Replaces \"PlugIn\" with \"Plugin\" inside files and in file names
\t-m  actually move files which match package prefixes.
\t-v  be verbose.  Print every command executed an report on
\t     any files changed.
\t-p  don't actually do anything - just pretend.
\t-c  cvs removes files with PlugIn in name and cvs adds replacement
\t-h  print this message and exit.
\t-d dest  copy files to new directory dest.
";
    exit 1;
}

sub process_dir {
    local($dir)=@_;
    lprint("# Processing directory $dir");
    
    local(@files)=findfiles($dir);
    local($_);
    foreach $_ (@files) {
	local($dev,$ino,$mode,$nlink,$uid,$gid) = lstat($_);
	if (-d $_) {
	    # consider creating new target directories up front
	    # let the files decide for now.
	    #consider_directory($_);
	} else {
	    process_file($_);
	}
    }
}

%madedir;
sub careful_mkdir {
    local($_) = @_;
    return if ($_ eq "");
    (($dev,$ino,$mode,$nlink,$uid,$gid) = lstat($_));
    if (-d $_) {
	# directory already there
	return;
    } else {
	if ($pretendP) {
	    # if we're pretending, make sure to record
	    # the mkdir so that we don't do it over and over...
	    return if $madedir{$_};
	    $madedir{$_}=1;
	}
	lprint("mkdir $_");
	mkdir($_, 0777) if $realP;
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
    (($dev,$ino,$mode,$nlink,$uid,$gid) = lstat($_));
    push @stuff, $File::Find::name;
  };
  local(%ref);
  $ref{"wanted"}=$code;
  &find(\%ref, $dir);
  @stuff;
}


sub main {
    local(@args)=@_;
    local($dir)= $args[0];
    @args = @args[1,];
    

    open(LOG, ">/tmp/repackage.log") if $logP;

    # grok the packagedefs

    process_dir($dir);

    report_stats();

    lprint("# done");

    close(LOG) if $logP;

    exit 0;
}




