@echo OFF

REM calls setlibpath.bat which sets the path to the required jar files.
CALL %COUGAAR_INSTALL_PATH%\bin\setlibpath.bat

REM runs the Asset Availability Perturbation GUI
set MYCLASSES=org.cougaar.domain.mlm.ui.perturbation.asset.AssetPerturbationDisplay

@ECHO ON

java.exe -classpath %LIBPATHS% %MYCLASSES% 

