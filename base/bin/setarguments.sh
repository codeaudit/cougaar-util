#!/bin/sh

# This should be sourced by a shell or shell script to define the
# standard contents of an COUGAAR command line.

# Domains are now usually defined by the config file LDMDomains.ini
# But you may still use properties if you wish.
# set MYDOMAINS=-Dorg.cougaar.domain.alp=org.cougaar.domain.glm.GLMDomain

MYDOMAINS=""
BOOTSTRAPPER=org.cougaar.core.society.Bootstrapper
MYCLASSES=org.cougaar.core.society.Node
OS=`uname`
if [ "$OS" == "Linux" ]; then
  MYPROPERTIES="-green"
fi
MYPROPERTIES="$MYPROPERTIES $MYDOMAINS  -Dorg.cougaar.system.path=$COUGAAR3RDPARTY -Dorg.cougaar.install.path=$COUGAAR_INSTALL_PATH"
MYPROPERTIES="$MYPROPERTIES -Duser.timezone=GMT -Dorg.cougaar.core.useBootstrapper=true"

MYMEMORY="-Xms100m -Xmx300m"
