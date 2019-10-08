#!/bin/bash

ktorVersionLine=$(grep '<ktor\.version>' pom.xml)
ktorVersion=$(echo $ktorVersionLine | sed 's/<ktor.version>\(.*\)<\/ktor.version>/\1/g')
gitShortHash=$(git rev-parse --short HEAD)
newProjectVersion="${ktorVersion}.${gitShortHash}"
echo "${gitShortHash}"