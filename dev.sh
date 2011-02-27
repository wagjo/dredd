#!/bin/sh

cd src/dredd

emacs &

cd ../..

lein swank
