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


# Sample script for running the Node Server.
#
# Be sure to edit the COUGAAR_INSTALL_PATH and NODE_PROPS_FILE 
# properties below.
#
# Note that CSMART may writes configuration files to the current 
# working directory.


# Set the COUGAAR_INSTALL_PATH, which is the base directory for the
# COUGAAR installation
#COUGAAR_INSTALL_PATH=/opt/cougaar

# Specify the host-specific properties file, which defines *all*
# the Node installation-specific properties.
#
# The ".props" file must be modified to match your installation.  An
# example is provided with this release:
#
#    server/data/unix-server-sample.props
#
# The above example includes documentation details.
#
# Some additional properties are set by CSMART at run time, such 
# as the Node's name, but otherwise this ".props" file specifies 
# the full configuration.
#
# Note that Server properties are *not* passed to the Node -- 
# only the properties in the ".props" file are passed.  For
# example, the classpath for the Node is read from the file.
#
# Also see the Server documentation ("server/doc/README") for 
# further details.
#NODE_PROPS_FILE="server.props"
if [ $# = 0 ]; then
    NODE_PROPS_FILE="server.props"
    echo Using default properties file: server.props
else 
    NODE_PROPS_FILE=$1
    echo "Using properties file : $1"
fi


if [ ! -f $NODE_PROPS_FILE ]; then
    echo "ERROR : properties file [${NODE_PROPS_FILE}] does not exist"
    exit 1
fi

if [! -f $COUGAAR_WORKSPACE]; then
    echo "Defaulting COUGAAR_WORKSPACE to CIP/workspace"
    COUGAAR_WORKSPACE=$COUGAAR_INSTALL_PATH/workspace
    if [ ! -f $COUGAAR_WORKSPACE ]; then
      mkdir $COUGAAR_WORKSPACE
    endif
endif

#
# The remaining settings should not require modifications
# unless you are debugging or have a custom COUGAAR installation.
#


# Specify the optional properties for the server itself.
#
# All server properties start with "-Dorg.cougaar.tools.server."
# and only modify the Server's behavior.  See the Server 
# documentation ("server/doc/README") for details.
#
#SERVERCONFIG="-Dorg.cougaar.tools.server.verbose=true"
SERVERCONFIG="-Dorg.cougaar.tools.server.temp.path=$COUGAAR_WORKSPACE"

# Only the "server.jar" should be in the classpath:
LIBPATHS="${COUGAAR_INSTALL_PATH}/lib/server.jar"

if [ "$OS" = "Linux" ]; then
    # set some system runtime limits
    limit stacksize 16m    #up from 8m
    limit coredumpsize 0   #down from 1g
fi

JAVA_ARGS="-classpath $LIBPATHS" 

if [ "$COUGAAR_DEV_PATH" != "" ]; then
    echo \
    java \
    $JAVA_ARGS \
    $SERVERCONFIG \
    org.cougaar.tools.server.AppServer \
    $NODE_PROPS_FILE
fi

# start the server
exec \
  java \
  $JAVA_ARGS \
  $SERVERCONFIG \
  org.cougaar.tools.server.AppServer \
  $NODE_PROPS_FILE

