@echo OFF

REM "<copyright>"
REM " Copyright 2001 BBNT Solutions, LLC"
REM " under sponsorship of the Defense Advanced Research Projects Agency (DARPA)."
REM ""
REM " This program is free software; you can redistribute it and/or modify"
REM " it under the terms of the Cougaar Open Source License as published by"
REM " DARPA on the Cougaar Open Source Website (www.cougaar.org)."
REM ""
REM " THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS"
REM " PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR"
REM " IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF"
REM " MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT"
REM " ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT"
REM " HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL"
REM " DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,"
REM " TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR"
REM " PERFORMANCE OF THE COUGAAR SOFTWARE."
REM "</copyright>"


REM pass in "NodeName" to run a specific named Node
REM pass in "admin" to run SANode separately

rem Domains are now usually defined by the config file LDMDomains.ini
rem But you may still use properties if you wish.
rem set MYDOMAINS=-Dorg.cougaar.domain.alp=org.cougaar.glm.GLMDomain
set MYDOMAINS=
set MYCLASSES=org.cougaar.core.node.Node

set TOPS_DEMO_PATH=%COUGAAR_INSTALL_PATH%\tops\configs\demo-config
set TOPS_TEST_PATH=%COUGAAR_INSTALL_PATH%\tops\configs\test-config

set MYPROPERTIES=-Dorg.cougaar.system.path=%COUGAAR3RDPARTY% -Dorg.cougaar.install.path=%COUGAAR_INSTALL_PATH% -Dorg.cougaar.config.path=%TOPS_DEMO_PATH%\data;%TOPS_DEMO_PATH%\data\PROTOTYPES;%TOPS_DEMO_PATH%\data\CLUSTERINPUT; -Duser.timezone=GMT -Dorg.cougaar.core.agent.startTime=08/10/2005 -Dorg.cougaar.planning.ldm.lps.ComplainingLP.level=0 -Dorg.cougaar.core.agent.SharedPluginManager.watching=false

set MYMEMORY=-Xms100m -Xmx300m

set MYARGUMENTS= -c -n "%1"
if "%1"=="admin" set MYARGUMENTS= -n Administrator -c -r -p 8000
if "%1"=="admin" set MYMEMORY= -Djava.compiler=NONE -Xms16m 
if "%1"=="EmptyNode" set MYMEMORY= -Xms16m 

set LIBPATHS=%COUGAAR_INSTALL_PATH%\lib\core.jar
set LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\lib\glm.jar
set LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\lib\planserver.jar
set LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\lib\toolkit.jar
set LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\lib\blackjack.jar

set LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\lib\j2ee.jar
set LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\lib\vgj.jar
set LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\lib\jcchart451K.jar
set LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\lib\omcore.jar

set LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\lib\openmap.jar
set LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\lib\scalability.jar
set LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\lib\tops.jar
set LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\lib\vishnu.jar

set LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\lib\xalan.jar
set LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\lib\xerces.jar
set LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\lib\xygraf.jar
set LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\lib\aggagent.jar

set LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\lib\classes12.zip

set LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\tops\lib\topsga.jar
set LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%\tops\lib\chart.jar

set COUGAAR3RDPARTY=%COUGAAR_INSTALL_PATH%\lib

@ECHO ON

java.exe %MYPROPERTIES% %MYMEMORY% -classpath %LIBPATHS% %MYCLASSES% %MYARGUMENTS% %2 %3

