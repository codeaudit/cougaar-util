#!/bin/csh -f

echo "In runNode"

if ( ! $?DEFAULT_NODE) then
    echo "DEFAULT_NODE not set : Run Node <nodename>"
else
    echo "Starting Node $DEFAULT_NODE"
    Node $DEFAULT_NODE
endif

