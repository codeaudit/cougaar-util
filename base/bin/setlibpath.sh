#!/bin/sh
# Should be sourced by a shell (or shell script) to set defaults for
# LIBPATHS and COUGAAR3RDPARTY environment variables.
# COUGAAR_INSTALL_PATH must already be set

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


if [ -z "$COUGAAR_INSTALL_PATH" ]; then
    echo "COUGAAR_INSTALL_PATH is not set. Using /alp"
    COUGAAR_INSTALL_PATH=/alp
    export COUGAAR_INSTALL_PATH
fi

LIBPATHS=$COUGAAR_INSTALL_PATH/lib/core.jar
if [ "$COUGAAR_DEV_PATH" != "" ]; then
    os=`uname`
    SEP=";"
    if [ $os = "Linux" -o $os = "SunOS" ]; then SEP=":"; fi
    LIBPATHS="${COUGAAR_DEV_PATH}${SEP}${LIBPATHS}"
fi
BOOTPATH=$COUGAAR_INSTALL_PATH/lib/javaiopatch.jar
if [ "$COUGAAR3RDPARTY" = "" ]; then
     COUGAAR3RDPARTY=/opt/cougaar-jars
     export COUGAAR3RDPARTY
fi
