#!/bin/bash
set -eou pipefail

TEST_ID=$(date +%s)
./gradlew gitConsumer -Ptestid=$TEST_ID
rm -rf ./build
./gradlew gitConsumer -Ptestid=$TEST_ID
