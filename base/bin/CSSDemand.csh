#!/bin/csh -f
##
## Filename: CSSDemand.csh
##
## Summary:  Display demand graphs for Maintenance and
##           Class I Subsistence (Water Production,
##           Water Transportation, and Quartermaster Handling).
##

setenv COUGAAR_DEV_PATH $COUGAAR_INSTALL_PATH/lib/sra.jar

## All listed are requisite
setenv CLASSPATH ${COUGAAR_INSTALL_PATH}/lib/core.jar:${COUGAAR_INSTALL_PATH}/lib/glm.jar:${COUGAAR_INSTALL_PATH}/lib/planserver.jar:${COUGAAR_INSTALL_PATH}/lib/jcchart400K.jar:${COUGAAR_INSTALL_PATH}/lib/xerces.jar:${COUGAAR_DEV_PATH}

set MYCLASSES="org.cougaar.domain.css.ui.ClusterDisplay"


set osargs=""
set os=`uname`
if ("$os" == "Linux") then
  set osargs="-green"
endif

if ($?COUGAAR_DEV_PATH) then
    echo java $osargs -classpath $CLASSPATH $MYCLASSES
endif

set host=$argv[1-]
if ("$host" == "") then
   java  $osargs -classpath $CLASSPATH $MYCLASSES
else
   java  $osargs -classpath $CLASSPATH $MYCLASSES $host 5555
endif
