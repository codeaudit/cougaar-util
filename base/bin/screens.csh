#/bin/csh
#
# First column
#xterm -sl 50000 -title ADMIN -geometry  80x6+0+0 &

# <copyright>
#  Copyright 2001 BBNT Solutions, LLC
#  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
# 
#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the Cougaar Open Source License as published by
#  DARPA on the Cougaar Open Source Website (www.cougaar.org).
# 
#  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
#  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
#  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
#  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
#  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
#  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
#  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
#  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
#  PERFORMANCE OF THE COUGAAR SOFTWARE.
# </copyright>

xterm -sl 50000 -title 1-BDE-NODE -geometry 80x6+0+120 -exec ssh alp-11 &
xterm -sl 50000 -title 2-BDE-NODE -geometry 80x6+0+230 -exec ssh alp-13 &
xterm -sl 50000 -title 3-BDE-NODE -geometry 80x6+0+340 -exec ssh alp-15 &
xterm -sl 50000 -title AVNBDE -geometry 80x6+0+450 -exec ssh1 alp-17 &
xterm -sl 50000 -title 3ID-DIV-NODE -geometry 80x6+0+560 -exec ssh alp-19 &
#
# Second column
xterm -sl 50000 -title SUPPLY-NODE -geometry 80x6+520+120 -exec ssh alp-21 &
xterm -sl 50000 -title SUPPORT-NODE -geometry 80x6+520+230 -exec ssh alp-25 &
xterm -sl 50000 -title TRANSCOM-NODE -geometry 80x6+520+340 -exec ssh alp-27 &
xterm -sl 50000 -title COMMAND-NODE -geometry 80x6+520+450 -exec ssh alp-29 &
xterm -sl 50000 -title IBCT-NODE -geometry 80x6+520+560 -exec ssh alp-33 &
xterm -sl 50000 -title 24-SPTGP-NODE -geometry 80x6+520+670 -exec ssh alp-35 &




