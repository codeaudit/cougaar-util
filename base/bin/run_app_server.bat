set Path=%COUGAAR_INSTALL_PATH%\bin;%Path%
CALL %COUGAAR_INSTALL_PATH%\bin\setlibpath.bat
java -classpath %LIBPATHS% -Dorg.cougaar.install.path=%COUGAAR_INSTALL_PATH% org.cougaar.appserver.ApplicationServer 8001