@echo OFF

rem Domains are now usually defined by the config file LDMDomains.ini
rem But you may still use properties if you wish.
rem set MYDOMAINS=-Dorg.cougaar.domain.alp=org.cougaar.domain.glm.GLMDomain
set MYDOMAINS=
set MYCLASSES=org.cougaar.core.society.Node

set MYPROPERTIES=-Dorg.cougaar.system.path=%COUGAAR3RDPARTY% -Dorg.cougaar.install.path=%COUGAAR_INSTALL_PATH% -Duser.timezone=GMT -Dorg.cougaar.core.cluster.startTime=08/10/2005 -Dorg.cougaar.domain.planning.ldm.lps.ComplainingLP.level=0 -Dorg.cougaar.core.cluster.SharedPlugInManager.watching=false

set MYMEMORY=-Xms100m -Xmx300m

