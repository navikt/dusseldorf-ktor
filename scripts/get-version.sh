#!/bin/bash
majorVersion="2"
ktorVersionLine=$(grep '<ktor\.version>' pom.xml)
ktorVersion=$(echo $ktorVersionLine | sed 's/<ktor.version>\(.*\)<\/ktor.version>/\1/g')
gitShortHash=$(git rev-parse --short HEAD)
gitBranch=$(git branch --show-current)
newProjectVersion="${majorVersion}.${ktorVersion}-${gitShortHash}"

if [ ${gitBranch} == "master" ]; then
  echo "${newProjectVersion}"
else
  echo "${newProjectVersion}-RC"
fi