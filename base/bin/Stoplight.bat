@echo OFF

REM calls setlibpath.bat which sets the path to the required jar files.
CALL %COUGAAR_INSTALL_PATH%\bin\setlibpath.bat

REM produces the inventory chart display
set MYCLASSES=ui.planviewer.supply.SupplyController

java.exe -classpath %LIBPATHS% %MYCLASSES%

