#!/bin/bash

DIR=/home/ubuntu/aic-group-2-topic-3
BRANCH=master
TAG=aws-tested

su ubuntu
cd "$DIR"
git stash
git pull
git checkout "$BRANCH"
git checkout "tags/$TAG"
git pull
./gradlew installApp
exec ./node/chain/build/install/chain/bin/chain