#!/usr/bin/env sh

##############################################################################
##
##  Clear ComponentForAndroidX
##
##############################################################################

find . -name "*.iml" | xargs rm -rf
rm -rf ./.idea
rm -rf ./build
rm -rf ./libs/security/.cxx
