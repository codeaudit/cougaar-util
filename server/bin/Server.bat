@ECHO OFF

REM "<copyright>"
REM " "
REM " Copyright 2001-2004 BBNT Solutions, LLC"
REM " under sponsorship of the Defense Advanced Research Projects"
REM " Agency (DARPA)."
REM ""
REM " You can redistribute this software and/or modify it under the"
REM " terms of the Cougaar Open Source License as published on the"
REM " Cougaar Open Source Website (www.cougaar.org)."
REM ""
REM " THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS"
REM " "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT"
REM " LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR"
REM " A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT"
REM " OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,"
REM " SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT"
REM " LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,"
REM " DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY"
REM " THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT"
REM " (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE"
REM " OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE."
REM " "
REM "</copyright>"


REM Sample script for running the Node Server.
REM
REM Be sure to edit the COUGAAR_INSTALL_PATH and NODE_PROPS_FILE 
REM properties below.
REM
REM Note that CSMART may writes configuration files to the current 
REM working directory.


REM Set the COUGAAR_INSTALL_PATH, which is the base directory for the
REM COUGAAR installation
REM  #SET COUGAAR_INSTALL_PATH=C:\opt\cougaar


REM Specify the host-specific properties file, which defines *all*
REM the Node installation-specific properties.
REM
REM The ".props" file must be modified to match your installation.  An
REM example is provided with this release:
REM
REM    server\data\win-server-sample.props
REM
REM The above example includes documentation details.
REM
REM Some additional properties are set by CSMART at run time, such 
REM as the Node's name, but otherwise this ".props" file specifies 
REM the full configuration.
REM
REM Note that Server properties are *not* passed to the Node -- 
REM only the properties in the ".props" file are passed.  For
REM example, the classpath for the Node is read from the file.
REM
REM Also see the Server documentation ("server\doc\README") for 
REM further details.

IF "%1" == "" GOTO USE_DEFAULT_PROPS
SET NODE_PROPS_FILE=%1
ECHO Using properties file : %1
GOTO CHECK_PROPS_EXISTS
:USE_DEFAULT_PROPS
SET NODE_PROPS_FILE=server.props
ECHO Using default properties file: server.props
GOTO CHECK_PROPS_EXISTS
:CHECK_PROPS_EXISTS
IF EXIST %NODE_PROPS_FILE% GOTO PROPS_EXISTS
ECHO ERROR : properties file [%NODE_PROPS_FILE%] does not exist
GOTO END
:PROPS_EXISTS

IF EXIST "%COUGAAR_INSTALL_PATH%\workspace" GOTO SETWORKSPACE
mkdir "%COUGAAR_INSTALL_PATH%\workspace"
:SETWORKSPACE
SET COUGAAR_WORKSPACE=%COUGAAR_INSTALL_PATH%\workspace

REM
REM The remaining settings should not require modifications
REM unless you are debugging or have a custom COUGAAR installation.
REM


REM Specify the optional properties for the server itself.
REM
REM All server properties start with "-Dorg.cougaar.tools.server."
REM and only modify the Server's behavior.  See the Server 
REM documentation ("server\doc\README") for details.
REM
REM #SET SERVERCONFIG = "-Dorg.cougaar.tools.server.verbose=true"

SET SERVERCONFIG="-Dorg.cougaar.tools.server.temp.path=%COUGAAR_WORKSPACE%"

REM Only the "server.jar" should be in the classpath:
SET LIBPATHS="%COUGAAR_INSTALL_PATH%\lib\server.jar"

SET JAVA_ARGS=^
  -classpath %LIBPATHS%

REM start the server

@ECHO ON

java ^
  %JAVA_ARGS% ^
  %SERVERCONFIG% ^
  org.cougaar.tools.server.AppServer ^
  %NODE_PROPS_FILE%

:END
