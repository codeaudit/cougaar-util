#/bin/csh
#
# First column
#xterm -sl 50000 -title ADMIN -geometry  80x6+0+0 &
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




