#!/bin/bash

ktorVersionLine=$(grep '<ktor\.version>' pom.xml)
ktorVersion=$(echo $ktorVersionLine | sed 's/<ktor.version>\(.*\)<\/ktor.version>/\1/g')
gitShortHash=$(git rev-parse --short HEAD)
echo "Current Ktor version is '${ktorVersion}'"
echo "Current Git short hash is '${gitShortHash}'"
newProjectVersion="${ktorVersion}.${gitShortHash}"
echo "New project version is '${newProjectVersion}'"
./mvnw versions:set -DnewVersion="${newProjectVersion}"