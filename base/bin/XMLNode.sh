#!/bin/sh

# <copyright>
#  Copyright 2001-2003 BBNT Solutions, LLC
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
# Node runs a cougaar society node from an xml config file
# usage:  XMLNode [-v] [-preoptions ...] society.xml Nodename[.ini] [-postoptions ...]
#   -v  outputs debugging information to stdout during the script's execution
#   -preoptions are passed as VM arguments, generally -D args, but must start with "-"
#   society.xml  The name of the society xml spec file.
#   Nodename[.ini] The name of the Node to run.  the ".ini" suffix is tolerated but discouraged.
#   -postoptions are passed as Node arguments (e.g. after the node classname) and may be of
#      any form understood by the Node class
# 
# The environment variable $COUGAAR_INSTALL_PATH should be set to the installed location
# of Cougaar if not set, the script will check $CIP and then will attempt to 
# guess based on where the Node script is located.
#

# figure out COUGAAR_INSTALL_PATH
scriptname=`basename $0`
if [ -z "$COUGAAR_INSTALL_PATH" ]; then
    if [ "$CIP" ]; then
	export COUGAAR_INSTALL_PATH="$CIP";
    else
	s=`echo "$0" | sed -e "s,/bin/${scriptname},,"`
	if [ x$s != x ] ; then
	    echo "Warning: Defaulting COUGAAR_INSTALL_PATH to '$s'!";
	    export COUGAAR_INSTALL_PATH=$s
	else
	    echo "Error: Could not find COUGAAR_INSTALL_PATH!";
	    exit;
	fi
    fi
fi

# -v for verbose mode
verbose=

# deal with command-line arguments
# commandline arguments before the nodename
preargs=
# commandline arguments after the nodename
postargs=
# the nodename is defined to be the first name without a "-" prefix
nodename=
socname=

while [ x"$1" != x ]; do
  case $1 in
    -v) verbose=1
	shift
	continue;;
    -*) preargs="$preargs $1"
	shift
	continue;;
    *)  socname="$1"
        shift
        nodename=`echo $1 | sed 's/.ini\$//'`
	shift
	break;;
  esac
done
# anything leftover is postargs
postargs="$*"

if [ x"$socname" == x ] ; then
    echo "Warning: default society configuration name to \"society.xml\"";
    socname="society.xml"
fi
if [ x"$nodename" == x ]; then
    echo "Warning: defaulting Nodename to \"Node\"";
    nodename="Node"
fi

# figure out workspace
if [ -z "$COUGAAR_WORKSPACE" ]; then
    export COUGAAR_WORKSPACE="$COUGAAR_INSTALL_PATH/workspace";
    if [ $verbose ]; then echo "Defaulting COUGAAR_WORKSPACE to $COUGAAR_WORKSPACE"; fi
fi


# This is a minimal classpath for booting - usually only bootstrap.jar is required
jars="$COUGAAR_INSTALL_PATH/lib/bootstrap.jar"

# domains are now usually defined by the config file LDMDomains.ini.
# But you may still use properties if you wish.
# eg:
# MYDOMAINS="-Dorg.cougaar.domain.alp=org.cougaar.glm.GLMDomain"


# Cougaar Arguments: set up the cougaar application
cargs="-Dorg.cougaar.install.path=$COUGAAR_INSTALL_PATH \
-Dorg.cougaar.workspace=$COUGAAR_WORKSPACE";
if [ -n "$MYDOMAINS" ]; then
    cargs="$cargs $MYDOMAINS";
fi

# add your cougaar options here (or on the command line):
# To quiet the complainingLP:
# -Dorg.cougaar.planning.ldm.lps.ComplainingLP.level=0
coptions="-Dorg.cougaar.core.agent.startTime=07/11/2005"

# vm arguments to adjust (defaults are given).  You can tune the VM performance
# by fooling with these values... or you can render your society unrunnable.
# -Xmx64m	      # max java heap
# -Xms3m	      # min (initial) java heap
# -Xmaxf0.6       # max heap free percent
# -Xminf0.35      # min heap free percent
# -Xmaxe4m        # max heap expansion increment
# -Xmine1m	      # min heap expansion increment
# -Xoss400k       # per-thread *java* stack size
vmargs="-Xmx768m -Xms64m -Xoss256k"

# environment arguments - e.g. set timezone to GMT to avoid confusion
eargs="-Duser.timezone=GMT";

# Specify the name of the node to run.
nodeargs="-Dorg.cougaar.node.name=$nodename"

# extend the bootclasspath with cougaar's persistence support
bootargs="-Xbootclasspath/p:${COUGAAR_INSTALL_PATH}/lib/javaiopatch.jar"

# name of the bootstrapper class
bootclass="org.cougaar.bootstrap.Bootstrapper"

# name of the node class
nodeclass="org.cougaar.core.node.Node"

# XML init arguments
#uncomment this to turn on validation
#xmlvalidate="-Dorg.cougaar.core.node.validate=true"
xmlvalidate=""
xmlargs="-Dorg.cougaar.core.node.InitializationComponent=XML -Dorg.cougaar.society.file=$socname $xmlvalidate"

allargs="${bootargs} \
${vmargs} \
${eargs} \
${cargs} \
${preargs} \
${nodeargs} \
${coptions} \
-classpath $jars \
$xmlargs \
${bootclass} ${nodeclass} \
${postargs}"
    
if [ $verbose ]; then echo "java $allargs"; fi
exec java $allargs
