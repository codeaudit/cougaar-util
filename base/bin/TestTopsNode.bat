@echo OFF

REM pass in "NodeName" to run a specific named Node
REM pass in "admin" to run SANode separately

rem Domains are now usually defined by the config file LDMDomains.ini
rem But you may still use properties if you wish.
rem set MYDOMAINS=-Dorg.cougaar.domain.alp=org.cougaar.domain.glm.GLMDomain
set MYDOMAINS=
set MYCLASSES=org.cougaar.core.society.Node

set TOPS_DEMO_PATH=%COUGAAR_INSTALL_PATH%\tops\configs\demo-config
set TOPS_TEST_PATH=%COUGAAR_INSTALL_PATH%\tops\configs\test-config

set MYPROPERTIES=-Dorg.cougaar.system.path=%COUGAAR3RDPARTY% -Dorg.cougaar.install.path=%COUGAAR_INSTALL_PATH% -Dorg.cougaar.config.path=%TOPS_TEST_PATH%\data;%TOPS_TEST_PATH%\data\PROTOTYPES;%TOPS_TEST_PATH%\data\CLUSTERINPUT; -Duser.timezone=GMT -Dorg.cougaar.core.cluster.startTime=08/10/2005 -Dorg.cougaar.domain.planning.ldm.lps.ComplainingLP.level=0 -Dorg.cougaar.core.cluster.SharedPlugInManager.watching=false

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

