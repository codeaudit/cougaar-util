#!/bin/sh
# Should be sourced by a shell (or shell script) to set defaults for
# LIBPATHS and COUGAAR3RDPARTY environment variables.
# COUGAAR_INSTALL_PATH must already be set

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
     COUGAAR3RDPARTY=/opt/alp-jars
     export COUGAAR3RDPARTY
fi
