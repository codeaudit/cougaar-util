@echo OFF
CALL %COUGAAR_INSTALL_PATH%\bin\setlibpath.bat
@echo ON

java -classpath %LIBPATHS%   org.cougaar.domain.mlm.ui.views.PolicyApplication
