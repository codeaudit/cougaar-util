#!/usr/bin/perl
# -*- Perl -*-

# <copyright>
#  Copyright 2003 BBNT Solutions, LLC
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


# Script to correct many uses of PluginBindingSite.

# As of 10.0, ComponentPlugin extends BlackboardClientComponent
# As a result, getBindingSite() no longer returns a PluginBindingSite -
# an explicit cast is necessary if you really want it and not a generic
# BindingSite.
# However, most uses were to get the AgentIdentifier 
# (aka ClusterIdentifier), which you can get directly from the Plugin.
# Other uses were to get a ConfigFinder, which you can
# also do directly via the Plugin.
# This script covers some of the common uses, but not all.
# WARNING: Run this script _after_ running fixCID.pl
# Also, Windows/Cygwin users should watch out for extra ^Ms being added to each line.

# Finally, Agent sub-components (Node.AgentManager.Agent.*), which are bound in
# an AgentChildBindingSite can still legitimately do getBindingSite().getAgentIdentifier(), and
# getBindingSite().getConfigFinder(). This script attempts to avoid changing such uses, but
# a warning is printed so users can double-check these changes.

# Uses of PluginBindingSite that are not recognized will be reported.

# Usage example:
# find . -name "*.java" | perl $COUGAAR_INSTALL_PATH/doc/ReleaseNotes/10.0/fixBindingSite.pl
#

$total=0;
$fixed=0;
$repls = 0;

print "Fixing PluginBindingSite references for Cougaar 10.0\n";

while (<>) {
  chop;
  $file = $_;

  &process($file);
}

print "-----------------------\n";
print "\n";
print "Total files = $total\n";
print "Total files modified = $fixed\n";
print "Total lines modified = $repls\n";

exit(0);

sub process {
  local($file) = @_;
  local($found)=0;		# if != 0, copy the file back
  local($tmp) = "/tmp/deleteme";
  local ($lineno)=0; # Current line in file
  local ($agentchild)=0; # Does this file appear to be an agent child?

  open(IN, "<$file");
  unlink($tmp);
  open(OUT, ">$tmp");
  while (<IN>) {
      $lineno++;

      if (/AgentChildBind/) {
	  # This is probably a Node.AgentManager.Agent.* component...
	  $agentchild = 1;
      }

      # first replace ((PluginBindingSite)getBindingSite()).getAgentIdentifier() with getAgentIdentifier()
      $found++ if (s/\(\s*\(\s*PluginBindingSite\s*\)\s*getBindingSite\s*\(\s*\)\s*\)\s*\.\s*getAgentIdentifier\s*\(\s*\)/getAgentIdentifier\(\)/g);

      # WARNING: AgentChildBindingSite also supports getAgentIdentifier(). So another component at Node.AgentManager.Agent.*
      # with that kind of binding site could legitimately do this!
      if (/getBindingSite\s*\(\s*\)\s*\.\s*getAgentIdentifier\s*\(\s*\)/) {
	  # Only do the swap if it doesn't appear to be an agent child
	  if (! $agentchild) {
	      # Then replace getBindingSite().getAgentIdentifier() with getAgentIdentifier()
	      s/getBindingSite\s*\(\s*\)\s*\.\s*getAgentIdentifier\s*\(\s*\)/getAgentIdentifier\(\)/g;
	      $found++;
	      print "Warning: Changed getBindingSite().getAgentIdentifier to getAgentIdentifier at $file:$lineno.\nIf this class is a Node.AgentManager.Agent.* component, this change is likely wrong!\n";
	  } else {
	      print "Component appears to be an Agent child (Node.AgentManager.Agent.*), so not changing getBindingSite().getAgentIdentifier().\nDouble check $file:$lineno\n";
	  }
      }

      # Calls to ((PluginBindingSite)getBindingSite()).getConfigFinder should be just getConfigFinder
      $found++ if (s/\(\s*\(\s*PluginBindingSite\s*\)\s*getBindingSite\s*\(\s*\)\s*\)\s*\.\s*getConfigFinder/getConfigFinder/g);

      # WARNING: AgentChildBindingSite also supports getConfigFinder(). So another component at Node.AgentManager.Agent.*
      # with that kind of binding site could legitimately do this!
      if (/getBindingSite\s*\(\s*\)\s*\.\s*getConfigFinder/) {
	  # Only do the swap if it doesn't appear to be an agent child
	  if (! $agentchild) {
	      # Calls to getBindingSite().getConfigFinder should be just getConfigFinder
	      s/getBindingSite\s*\(\s*\)\s*\.\s*getConfigFinder/getConfigFinder/g;
	      $found++;
	      print "Warning: Changed getBindingSite().getConfigFinder to getConfigFinder at $file:$lineno.\nIf this class is a Node.AgentManager.Agent.* component, this change is likely wrong!\n";
	  } else {
	      print "Component appears to be an Agent child (Node.AgentManager.Agent.*), so not changing getBindingSite().getConfigFinder().\nDouble check $file:$lineno\n";
	  }
      }


      # Getting the service broker is legitimate.
      # Note though that if I add getServiceBroker to PluginAdapter,
      # then, for Plugins, a simple getServiceBroker is enough
#      if (/getBindingSite\s*\(\s*\)\s*\.\s*getServiceBroker/) {
#	  print "getBindingSite().getServiceBroker found at $file:$lineno\n$_\n";
#      }

      # If you cast the BindingSite to something other than
      # a PluginBindingSite, this script doesn't care
#      elsif (/\(\s*\S+\s*\)\s*getBindingSite/ && 
#	  ! ( /\(\s*PluginBindingSite\s*\)\s*getBindingSite/ ) ) {
#	  print "Found cast of binding site to non PluginBindingSite at $file:$lineno\n$_\n";
#      }

#      elsif (/getBindingSite/) {
#	  print "Found other use of getBindingSite at $file:$lineno.\n$_\n";
#      }

    # Then flag other uses.
      if (/PluginBindingSite/) {
	  print "Warning: Found other reference to PluginBindingSite at $file:$lineno.\n$_\n";
      }
    print OUT "$_";
  }
  close(OUT);
  close(IN);

  # copy it back
  if ($found) {
      print "Changes made in $file.\n";
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
