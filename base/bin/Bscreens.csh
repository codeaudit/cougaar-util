#/bin/csh
#
# First column
xterm -sl 50000 -title BJ-1BDE-NODE -geometry 80x6+0+120 -exec ssh alp-135 &
xterm -sl 50000 -title BJ-2BDE-NODE -geometry 80x6+0+230 -exec ssh alp-133 &
xterm -sl 50000 -title BJ-TCBN-MED-NODE -geometry 80x6+0+340 -exec ssh alp-37 &
xterm -sl 50000 -title BJ-CSB-NODE -geometry 80x6+0+450 -exec ssh alp-39 &
xterm -sl 50000 -title "/opt/alp/configs/bj-mar-config/BJ-MAR-NODE" -geometry 80x6+0+570 -exec ssh alp-147 &
#
# Second column
xterm -sl 50000 -title BJ-COMMAND-3ID-NODE -geometry 80x6+520+120 -exec ssh alp-139 &
xterm -sl 50000 -title BJ-SUPPLY-NODE -geometry 80x6+520+230 -exec ssh alp-33 &
xterm -sl 50000 -title DELTA-PROXY-NODE -geometry 80x6+520+340 -exec ssh alp-35 &
xterm -sl 50000 -title BJ-IBCT-NODE -geometry 80x6+520+450 -exec ssh alp-137 &




