@echo off
call setlibpath.bat
call setarguments.bat
MYCLASSES=org.cougaar.domain.glm.execution.eg.EventGenerator

java %MYPROPERTIES% -classpath %LIBPATHS% %BOOTSTRAPPER% %MYCLASSES% %1 %2 %3 %4 %5 %6 %7 %8 %9
