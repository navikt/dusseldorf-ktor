#!/bin/bash

ktorVersion() {
    line=$(grep '<ktor\.version>' pom.xml)
    version=$(echo $line | sed 's/<ktor.version>\(.*\)<\/ktor.version>/\1/g')
    echo $version
}

kotlinVersion() {
    line=$(grep '<kotlin\.version>' pom.xml)
    version=$(echo $line | sed 's/<kotlin.version>\(.*\)<\/kotlin.version>/\1/g')
    echo $version
}

logbackVersion() {
    line=$(grep '<logback\.version>' pom.xml)
    version=$(echo $line | sed 's/<logback.version>\(.*\)<\/logback.version>/\1/g')
    echo $version
}

logstashLogbackVersion() {
    line=$(grep '<logstash\.logback\.version>' pom.xml)
    version=$(echo $line | sed 's/<logstash.logback.version>\(.*\)<\/logstash.logback.version>/\1/g')
    echo $version
}


prometheusVersion() {
    line=$(grep '<prometheus\.version>' pom.xml)
    version=$(echo $line | sed 's/<prometheus.version>\(.*\)<\/prometheus.version>/\1/g')
    echo $version
}

jacksonVersion() {
    line=$(grep '<jackson\.version>' pom.xml)
    version=$(echo $line | sed 's/<jackson.version>\(.*\)<\/jackson.version>/\1/g')
    echo $version
}


filename="gradle/dusseldorf-ktor.gradle.kts"

rm -rf ${filename}

mkdir -p gradle && touch $filename

echo "val dusseldorfKtorVersion by extra(\""$(./scripts/get-version.sh)\"")" >> "${filename}"
echo "val ktorVersion by extra(\""$(ktorVersion)\"")" >> "${filename}"
echo "val kotlinVersion by extra(\""$(kotlinVersion)\"")" >> "${filename}"
echo "val logbackVersion by extra(\""$(logbackVersion)\"")" >> "${filename}"
echo "val logstashLogbackVersion by extra(\""$(logstashLogbackVersion)\"")" >> "${filename}"
echo "val prometheusVersion by extra(\""$(prometheusVersion)\"")" >> "${filename}"
echo "val jacksonVersion by extra(\""$(jacksonVersion)\"")" >> "${filename}"


cat ${filename}