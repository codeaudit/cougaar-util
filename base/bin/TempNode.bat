@echo OFF

REM calls setlibpath.bat which sets the path to the required jar files.
REM calls setarguments.bat which sets input parameters for system behavior
CALL %COUGAAR_INSTALL_PATH%\bin\setlibpath.bat
CALL %COUGAAR_INSTALL_PATH%\bin\setarguments.bat

REM pass in "NodeName" to run a specific named Node
REM pass in "admin" to run SANode separately
set MYARGUMENTS= -c -n "%1"
REM if "%1"=="admin" set MYARGUMENTS= -n Administrator -c -r -p 8000
if "%1"=="admin" set MYARGUMENTS= -n Administrator -c -r TRNS -n %COMPUTERNAME% -p %2

@ECHO ON

java.exe %MYPROPERTIES% -classpath %LIBPATHS% %MYCLASSES% %MYARGUMENTS%