@echo OFF

REM calls setlibpath.bat which sets the path to the required jar files.
CALL %COUGAAR_INSTALL_PATH%\bin\setlibpath.bat

REM runs the XMLUIDataClient which communicates with the UIDATA PSP
REM which is used by the UI developers
set MYCLASSES=ui.planviewer.XMLUIDataClient

@ECHO ON

java.exe -classpath %LIBPATHS% %MYCLASSES% 

