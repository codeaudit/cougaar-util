#/bin/csh
#
# First column
xterm -sl 50000 -title BJ-1BDE-NODE -geometry 80x6+0+120 -exec ssh alp-123 &
xterm -sl 50000 -title BJ-2BDE-NODE -geometry 80x6+0+230 -exec ssh alp-15 &
xterm -sl 50000 -title BJ-TCBN-MED-NODE -geometry 80x6+0+340 -exec ssh alp-119 &
xterm -sl 50000 -title BJ-CSB-NODE -geometry 80x6+0+450 -exec ssh alp-105 &
#
# Second column
xterm -sl 50000 -title BJ-COMMAND-3ID-NODE -geometry 80x6+520+120 -exec ssh alp-131 &
xterm -sl 50000 -title BJ-SUPPLY-NODE -geometry 80x6+520+230 -exec ssh alp-93 &
xterm -sl 50000 -title DELTA-PROXY-NODE -geometry 80x6+520+340 -exec ssh alp-95 &
xterm -sl 50000 -title TRANSCOM -geometry 80x6+520+450 -exec ssh alp-101 &
xterm -sl 50000 -title BJ-IBCT-NODE -geometry 80x6+520+550 -exec ssh alp-129 &




