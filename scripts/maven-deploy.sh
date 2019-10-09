#!/bin/bash

repositoryId=github
url=https://maven.pkg.github.com/navikt/dusseldorf-ktor
groupId=no.nav.helse
version=$(./scripts/get-version.sh)

deploy() {
    if [ "$1" == "pom" ]; then
        pomFile=pom.xml
        file=pom.xml
    else
        pomFile=$2/pom.xml
        file=$2/target/$2-$version.$1
    fi

    ./mvnw --settings .github/settings.xml deploy:deploy-file \
      -DgroupId=$groupId \
      -DartifactId=$2 \
      -Dversion=$version \
      -Dpackaging=$1 \
      -Dfile=$file \
      -DrepositoryId=$repositoryId \
      -Durl=$url \
      -DpomFile=$pomFile
}

deploy pom dusseldorf-ktor
deploy jar dusseldorf-ktor-core
deploy jar dusseldorf-ktor-metrics
deploy jar dusseldorf-ktor-client
deploy jar dusseldorf-ktor-jackson
deploy jar dusseldorf-ktor-health
deploy jar dusseldorf-oauth2-client
deploy jar dusseldorf-ktor-auth
deploy jar dusseldorf-ktor-test-support
