#!/bin/sh
set -e
cd /source
cabal update
cabal install --global
