#!/bin/sh

source $COUGAAR_INSTALL_PATH/bin/setlibpath.sh
source $COUGAAR_INSTALL_PATH/bin/setarguments.sh

MYCLASSES="org.cougaar.domain.mlm.ui.planviewer.XMLClient"

exec java $MYPROPERTIES -classpath $LIBPATHS $BOOTSTRAPPER $MYCLASSES $*
