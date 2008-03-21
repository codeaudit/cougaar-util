#!/bin/sh 
#
# This script is designed to download the latest Cougaar 
# jars and install them in a standard place to be used
# as your COUGAAR_INSTALL_PATH. 
#
# The required argument specifies the root path in your 
# local file system where the downloaded data should be
# unpacked.  This is referred to below as the "cougaar-install-base".
#
# An optional second argument specifies which release
# to download.  The default is HEAD.
#
# The cougaar-install-base is assumed to have the following
# structure:
#
# <cougaar-install-base>/
#    <version-1>/
#        <date-1>/
#        ....
#        <date-n>/
#        latest/
#    ...
#    <version-m>/
#        <date-1>/
#        ....
#        <date-n>/
#        latest/
#
# Each <version> corresponds to Cougaar version tag.
# The <date> directories correspond to the date you
# downloaded (not the date of the build) and have the
# form yy_mm_dd_HH-MM.  The most recent download is
# always called 'latest', and a symlink with the 
# corresponding date timestamp points there.  The
# symlinks are configured this way for compatibility
# with Eclipse: Eclipse users can easily create a 
# User Library that references 'latest' and that
# Library will always in fact reference the latest
# download.  If 'latest' were  symlink to one of the
# timestamped directories, the actual reference stored
# by Eclipse would the resolved path, not the link.
#
# In order to use this script you must have the following
# utilities installed and accessible via $PATH: curl, unzip,
# wc, find and sed.  The find command must support the
# '-maxdepth' option.
# 


if [ "$#" = 0 ] ; then
    echo "getcip.sh <cougaar-install-base> [<version>]"
    exit -1
fi

time=`date +%y_%m_%d_%H-%M`
# convert install directory to absolute directory name
cougaar_install_base=`cd $1; pwd`

if [ "$#" = 2 ] ; then
    version=$2
else
    version=HEAD
fi

cip=$cougaar_install_base/$version/$time
cougaar_url_base="http://build.cougaar.org/auto/$version/latest_success"

mkdir -p $cip
cd $cip

# Get cougaar zips

# core cougaar
curl -o cougaar.zip $cougaar_url_base/cougaar.zip 

# third-party jars
curl -o cougaar-support.zip $cougaar_url_base/cougaar-support.zip

# optional: cougaar javadoc
#curl -o cougaar-api.zip $cougaar_url_base/cougaar-api.zip

# optional cougaar sources
#curl -o cougaar-src.zip $cougaar_url_base/cougaar-src.zip

# optional: standalone cougaar demos
#curl -o demo-tools.zip $cougaar_url_base/demo-hello.zip
#curl -o demo-ping.zip $cougaar_url_base/demo-ping.zip
#curl -o demo-tools.zip $cougaar_url_base/demo-tools.zip


# Inflate into time-stamped directory
unzip -o cougaar.zip
unzip -o cougaar-support.zip
#unzip -o cougaar-api.zip
#unzip -o cougaar-src.zip
#unzip -o demo-hello.zip
#unzip -o demo-ping.zip
#unzip -o demo-tools.zip
rm *.zip

cd $cougaar_install_base/$version

# Rename the previous 'latest' back to its timestamp
previous=`find . -maxdepth 1 -type l -print`
symlinks=`echo $previous | wc -w | sed 's/^[ ]*//'`
echo "found $symlinks symbolic links"
if [ "$symlinks" = 1 ] ; then
    rm -f $previous
    mv latest $previous
elif [ "$symlinks" != 0 ] ; then
   echo "!!! too many symbolic links in directory: not setting latest"
   exit -1
fi

# Rename the download we just did to 'latest', and
# then create symlink to it using the timestamp.
# We do this for Eclipse compatibility.
mv $time latest
ln -s latest $time

