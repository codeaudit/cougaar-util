@echo OFF

REM calls setlibpath.bat which sets the path to the required jar files.
CALL %COUGAAR_INSTALL_PATH%\bin\setlibpath.bat
CALL %COUGAAR_INSTALL_PATH%\bin\setarguments.bat

REM produces the inventory chart display
set MYCLASSES=org.cougaar.domain.mlm.ui.planviewer.inventory.InventoryChartUI
set BS=org.cougaar.core.society.Bootstrapper
@ECHO ON

java.exe %MYPROPERTIES% %MYMEMORY% -classpath %LIBPATHS% %BS% %MYCLASSES% %1

