#!/bin/bash

newProjectVersion=$(./scripts/get-version.sh)
./mvnw versions:set -DnewVersion="${newProjectVersion}"