@echo OFF

REM "<copyright>"
REM " Copyright 2001-2003 BBNT Solutions, LLC"
REM " under sponsorship of the Defense Advanced Research Projects Agency (DARPA)."
REM ""
REM " This program is free software; you can redistribute it and/or modify"
REM " it under the terms of the Cougaar Open Source License as published by"
REM " DARPA on the Cougaar Open Source Website (www.cougaar.org)."
REM ""
REM " THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS"
REM " PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR"
REM " IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF"
REM " MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT"
REM " ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT"
REM " HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL"
REM " DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,"
REM " TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR"
REM " PERFORMANCE OF THE COUGAAR SOFTWARE."
REM "</copyright>"


REM Make sure that COUGAAR_INSTALL_PATH is specified
IF NOT "%COUGAAR_INSTALL_PATH%" == "" GOTO L_2

REM Unable to find cougaar-install-path
ECHO COUGAAR_INSTALL_PATH not set!
GOTO L_END
:L_2

REM Make sure that COUGAAR3RDPARTY is specified
IF NOT "%COUGAAR3RDPARTY%" == "" GOTO L_3

REM Unable to find "sys" path for 3rd-party jars
REM This is usually COUGAAR_INSTALL_PATH/sys
ECHO COUGAAR3RDPARTY not set! Defaulting to CIP\sys
SET COUGAAR3RDPARTY=%COUGAAR_INSTALL_PATH%\sys
:L_3

REM Make sure that COUGAAR_WORKSPACE is set
IF NOT "%COUGAAR_WORKSPACE%" == "" GOTO L_4

REM Path for runtime output not set.
REM Default is CIP/workspace
ECHO COUGAAR_WORKSPACE not set. Defaulting to CIP/workspace
SET COUGAAR_WORKSPACE=%COUGAAR_INSTALL_PATH%\workspace
:L_4

REM calls setlibpath.bat which sets the path to the required jar files.
REM calls setarguments.bat which sets input parameters for system behavior
CALL %COUGAAR_INSTALL_PATH%\bin\setlibpath.bat
CALL %COUGAAR_INSTALL_PATH%\bin\setarguments.bat

REM pass in "NodeName" to run a specific named Node
REM pass in "admin" to run SANode separately
set MYARGUMENTS= -c -n "%1"
if "%1"=="admin" set MYARGUMENTS= -n Administrator -c -r -p 8000
if "%1"=="admin" set MYMEMORY= -Djava.compiler=NONE -Xms16m 
if "%1"=="EmptyNode" set MYMEMORY= -Xms16m 

@ECHO ON

java.exe -Dorg.cougaar.core.persistence.enable=true -Xbootclasspath/p:%COUGAAR_INSTALL_PATH%\lib\javaiopatch.jar %MYPROPERTIES% %MYMEMORY% -classpath %LIBPATHS% %MYCLASSES% %MYARGUMENTS% %2 %3

:L_END
