@ECHO OFF

REM "<copyright>"
REM " Copyright 2001 BBNT Solutions, LLC"
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


CALL %COUGAAR_INSTALL_PATH%\bin\setlibpath.bat

REM Set this to port you want to use for metrics voyager client
set PORT=7999

REM The threshold for # of changes to make a cluster the hot color
set THRESHOLD=16

REM The number of seconds over which the deltas are summed to compute color
set FILTERSIZE=12

REM The number of seconds to run behind real-time
set DELAY=10

REM Set this true to print all records to the shell
set DEBUG=false

REM Set this true for canned display of 50+ clusters without alp nodes
REM (Still have to run admin node)
set TEST=false

REM set this true to use metrics.dat as playback file, ignoring node messages
set PLAYBACK=false

REM set this true to record all messages into metrics.dat
set RECORD=false

REM Color used for maximum activity level
set HOTCOLOR=0,255,0

REM Color used for no recent activity
set STATICCOLOR=255,255,255

REM Need to define COUGAAR_INSTALL_PATH, since it's used to look up IP addresses
set MYPROPERTIES=-Dorg.cougaar.install.path=%COUGAAR_INSTALL_PATH%

set MYARGUMENTS=-port %PORT% -threshold %THRESHOLD% -filtersize %FILTERSIZE% -delay %DELAY% -debug %DEBUG% -test %TEST% -playback %PLAYBACK% -record %RECORD% -hotcolor %HOTCOLOR% -staticcolor %STATICCOLOR%

java.exe %MYPROPERTIES% -classpath %LIBPATHS% metrics.MetricsProxy %MYARGUMENTS%



