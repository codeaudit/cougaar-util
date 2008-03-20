#!/bin/sh 
if [ "$#" = 0 ] ; then
    echo "getcip.sh <cougaar_install_base_directory> [<version>]"
        exit
fi

time=`date +%y_%m_%d_%H-%M`
# convert install directory to absolute directory name
cougaar_install_base=`cd $1; pwd`
cip=$cougaar_install_base/$time

if [ "$#" = 2 ] ; then
    version=$2
else
    version=HEAD
fi

cougaar_url_base="http://build.cougaar.org/auto/$version/latest_success"

mkdir -p $cip
cd $cip

# Get cougaar zips
curl -o cougaar.zip $cougaar_url_base/cougaar.zip 
curl -o cougaar-support.zip $cougaar_url_base/cougaar-support.zip
#curl -o cougaar-api.zip $cougaar_url_base/cougaar-api.zip
#curl -o cougaar-src.zip $cougaar_url_base/cougaar-src.zip
#curl -o demo-tools.zip $cougaar_url_base/demo-hello.zip
#curl -o demo-ping.zip $cougaar_url_base/demo-ping.zip
#curl -o demo-tools.zip $cougaar_url_base/demo-tools.zip


#Inflate into time-stamped directory
unzip -o cougaar.zip
unzip -o cougaar-support.zip
#unzip -o cougaar-api.zip
#unzip -o cougaar-src.zip
#unzip -o demo-hello.zip
#unzip -o demo-ping.zip
#unzip -o demo-tools.zip
rm *.zip

#link latest to new directory
# "latest" directory name is not symbolic, because eclipse/java
# wants canonical pathames except within the workspace
#
cd $cougaar_install_base
previous=`find . -type l -maxdepth 1`
symlinks=`echo $previous | wc -w | sed 's/^[ ]*//'`
echo "found $symlinks symbolic links"
if [ "$symlinks" = 1 ] ; then
    rm -f $previous
    mv latest $previous
elif [ "$symlinks" != 0 ] ; then
   echo "!!! too many symbolic links in directory: not setting latest"
  exit 0
fi
mv $time latest
ln -s latest $time

