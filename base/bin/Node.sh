#!/bin/sh

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


# Environment variables
# COUGAAR_INSTALL_PATH = the head of the alp install.
# COUGAAR3RDPARTY = a directory containing 3rd party jar files
#
# COUGAAR bootstrapping classpath will be:
#  $COUGAAR_DEV_PATH	if defined
#  $COUGAAR_INSTALL_PATH/lib/core.jar
#
# once running, jar files will be searched for in (in order):
#  -Dorg.cougaar.class.path 	like classpath
#  $CLASSPATH		(alp bootstrapping path from above)
#  $COUGAAR_INSTALL_PATH/lib/*
#  $COUGAAR_INSTALL_PATH/plugins/*
#  -Dorg.cougaar.system.path=$COUGAAR3RDPARTY
#  $COUGAAR_INSTALL_PATH/sys/*
#

source $COUGAAR_INSTALL_PATH/bin/setlibpath.sh
source $COUGAAR_INSTALL_PATH/bin/setarguments.sh

node="$1"
shift
rest="$*"
if [ -z "$node" ]; then
  node="Clusters"
fi

if [ "$node" = "admin" ]; then
    args="-c -r -n Administrator -p 8000 $rest"
    MYMEMORY="-Djava.compiler=NONE"
else 
    args="-n $node -c $rest"
    # arguments to adjust (defaults are given)
    # -Xmx64m	      # max java heap
    # -Xms3m	      # min (initial) java heap
    # -Xmaxf0.6       # max heap free percent
    # -Xminf0.35      # min heap free percent
    # -Xmaxe4m        # max heap expansion increment
    # -Xmine1m	      # min heap expansion increment
    # -Xoss400k       # per-thread *java* stack size
    MYMEMORY="-Xmx768m -Xms64m -Xmaxf0.9 -Xminf0.1 -Xoss128k"
    #set MYMEMORY="-Xmx300m -Xms100m"
fi

if [ "$OS" = "Linux" ]; then
    # set some system runtime limits
    limit stacksize 16m    #up from 8m
    limit coredumpsize 0   #down from 1g
    #turn this on to enable inprise JIT
    #setenv JAVA_COMPILER javacomp
fi

#set javaargs="$osargs $MYPROPERTIES $MYMEMORY -classpath $LIBPATHS -Dorg.cougaar.core.message.isLogging=true -Djava.rmi.server.logCalls=true -Dsun.rmi.server.exceptionTrace=true -Dsun.rmi.transport.tcp.readTimeout=150000 "
javaargs="$MYPROPERTIES $MYMEMORY -classpath $LIBPATHS"

if [ "$COUGAAR_DEV_PATH" != "" ]; then
    echo java $javaargs org.cougaar.core.society.Node $args
fi

# exec instead of eval
exec java $javaargs org.cougaar.core.society.Node $args
