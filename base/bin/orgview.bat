@echo OFF
CALL %COUGAAR_INSTALL_PATH%\bin\setlibpath.bat
cd %COUGAAR_INSTALL_PATH%\alpine\data\orgview
java -cp %LIBPATHS% org.cougaar.domain.mlm.ui.orgviewServer.NetMapServer
