#!/bin/csh -f
##
## Filename: CSSDemand.csh
##
## Summary:  Display demand graphs for Maintenance and
##           Class I Subsistence (Water Production,
##           Water Transportation, and Quartermaster Handling).
##

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


setenv COUGAAR_DEV_PATH $COUGAAR_INSTALL_PATH/lib/sra.jar

## All listed are requisite
setenv CLASSPATH ${COUGAAR_INSTALL_PATH}/lib/bootstrap.jar:${COUGAAR_INSTALL_PATH}/lib/util.jar:${COUGAAR_INSTALL_PATH}/lib/core.jar:${COUGAAR_INSTALL_PATH}/lib/glm.jar:${COUGAAR_INSTALL_PATH}/lib/planserver.jar:${COUGAAR_INSTALL_PATH}/lib/jcchart400K.jar:${COUGAAR_INSTALL_PATH}/lib/xerces.jar:${COUGAAR_DEV_PATH}

set MYCLASSES="org.cougaar.css.ui.ClusterDisplay"


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
