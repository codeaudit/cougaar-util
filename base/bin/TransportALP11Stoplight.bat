@echo OFF

REM calls setlibpath.bat which sets the path to the required jar files.
CALL %COUGAAR_INSTALL_PATH%\bin\setlibpath.bat

REM produces the transport stoplight chart for a society on alp-11
set MYCLASSES=ui.planviewer.stoplight.SupplyController
set MYPROPERTIES=-Dorg.cougaar.install.path=%COUGAAR_INSTALL_PATH%

@ECHO ON
java.exe %MYPROPERTIES% -classpath %LIBPATHS% %MYCLASSES% Transport alp-11


