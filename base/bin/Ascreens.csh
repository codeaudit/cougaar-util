#/bin/csh
#
# First column
xterm -sl 50000 -title BJ-1BDE-NODE -geometry 80x6+0+120 -exec ssh alp-151 &
xterm -sl 50000 -title BJ-2BDE-NODE -geometry 80x6+0+230 -exec ssh alp-17 &
xterm -sl 50000 -title BJ-TCBN-MED-NODE -geometry 80x6+0+340 -exec ssh alp-141 &
xterm -sl 50000 -title BJ-CSB-NODE -geometry 80x6+0+450 -exec ssh alp-19 &
xterm -sl 50000 -title "/opt/alp/configs/bj-mar-config/BJ-MAR-NODE" -geometry 80x6+0+570 -exec ssh alp-143 &
xterm -sl 50000 -title "BJ-126-MEDTM-FSURG-NODE" -geometry 80x6+0+570 -exec ssh alp-161 &
#
# Second column
xterm -sl 50000 -title BJ-COMMAND-3ID-NODE -geometry 80x6+520+120 -exec ssh alp-155 &
xterm -sl 50000 -title BJ-SUPPLY-NODE -geometry 80x6+520+230 -exec ssh alp-11 &
xterm -sl 50000 -title DELTA-PROXY-NODE -geometry 80x6+520+340 -exec ssh alp-13 &
xterm -sl 50000 -title BJ-IBCT-NODE -geometry 80x6+520+450 -exec ssh alp-153 &
xterm -sl 50000 -title "BJ-21-MED-CSHOSP" -geometry 80x6+520+570 -exec ssh alp-149 &




