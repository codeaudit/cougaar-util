@echo OFF

REM calls setlibpath.bat which sets the path to the required jar files.
CALL %COUGAAR_INSTALL_PATH%\bin\setlibpath.bat

set MYCLASSES=org.cougaar.domain.mlm.ui.tpfdd.aggregation.Server
set MYMEMORY= Xmx300m
set MYARGUMENTS= -clusterList FortBenningITO,FortStewartITO,SavannahPort,GlobalSea,GlobalAir,VirtualAir,VirtualAirlines,VirtualC141Wing,TheaterFort,C141WingMcGuire,C17WingCharleston,C5WingDove 

@ECHO ON

java.exe -classpath %LIBPATHS% %MYCLASSES% %MYARGUMENTS%

