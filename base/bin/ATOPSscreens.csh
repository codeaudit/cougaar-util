#/bin/csh
#
# First column
xterm -sl 50000 -title Tops2000TheaterFortNode -geometry 80x6+0+120 -exec ssh alp-21 &
xterm -sl 50000 -title Tops2000TheaterFortNode.txt -geometry 80x6+0+230 -exec ssh alp-21 &
xterm -sl 50000 -title Tops2000AirNode -geometry 80x6+0+340 -exec ssh alp-27 &
xterm -sl 50000 -title Tops2000AirNode.txt -geometry 80x6+0+450 -exec ssh1 alp-27 &
#
# Second column
xterm -sl 50000 -title Tops2000SeaNode -geometry 80x6+520+120 -exec ssh alp-25 &
xterm -sl 50000 -title Tops2000SeaNode.txt -geometry 80x6+520+230 -exec ssh alp-25 &
xterm -sl 50000 -title Tops2000FortNode -geometry 80x6+520+340 -exec ssh alp-25 &
xterm -sl 50000 -title Tops2000FortNode.txt -geometry 80x6+520+450 -exec ssh alp-25 &
xterm -sl 50000 -title VISNU/SCRIPTS/runScedulerAlp69 -geometry 80x6+520+550 -exec ssh alp-155 &


 

