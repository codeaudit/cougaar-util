@ECHO OFF

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
set PLAYBACK=true

REM set this true to record all messages into metrics.dat
set RECORD=false

set MYARGUMENTS=-port %PORT% -threshold %THRESHOLD% -filtersize %FILTERSIZE% -delay %DELAY% -debug %DEBUG% -test %TEST% -playback %PLAYBACK% -record %RECORD%

java.exe -classpath %LIBPATHS% metrics.MetricsProxy %MYARGUMENTS%



