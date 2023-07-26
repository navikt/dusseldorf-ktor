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

caffeineVersion() {
    line=$(grep '<caffeine\.version>' pom.xml)
    version=$(echo $line | sed 's/<caffeine.version>\(.*\)<\/caffeine.version>/\1/g')
    echo $version
}

slf4jVersion() {
    line=$(grep '<slf4j\.version>' pom.xml)
    version=$(echo $line | sed 's/<slf4j.version>\(.*\)<\/slf4j.version>/\1/g')
    echo $version
}

kotlinxCoroutinesVersion() {
    line=$(grep '<kotlinx\.coroutines\.version>' pom.xml)
    version=$(echo $line | sed 's/<kotlinx.coroutines.version>\(.*\)<\/kotlinx.coroutines.version>/\1/g')
    echo $version
}

micrometerVersion() {
    line=$(grep '<micrometer\.version>' pom.xml)
    version=$(echo $line | sed 's/<micrometer.version>\(.*\)<\/micrometer.version>/\1/g')
    echo $version
}

kafkaVersion() {
    line=$(grep '<kafka\.version>' pom.xml)
    version=$(echo $line | sed 's/<kafka.version>\(.*\)<\/kafka.version>/\1/g')
    echo $version
}

kafkaEmbeddedEnvVersion() {
    line=$(grep '<kafka\.embedded\.env\.version>' pom.xml)
    version=$(echo $line | sed 's/<kafka.embedded.env.version>\(.*\)<\/kafka.embedded.env.version>/\1/g')
    echo $version
}

orgjsonVersion() {
    line=$(grep '<orgjson\.version>' pom.xml)
    version=$(echo $line | sed 's/<orgjson.version>\(.*\)<\/orgjson.version>/\1/g')
    echo $version
}

filename="gradle/dusseldorf-ktor.gradle.kts"

rm -rf ${filename}

mkdir -p gradle && touch $filename

echo "val ktorVersion by extra(\""$(ktorVersion)\"")" >> "${filename}"
echo "val kotlinVersion by extra(\""$(kotlinVersion)\"")" >> "${filename}"
echo "val logbackVersion by extra(\""$(logbackVersion)\"")" >> "${filename}"
echo "val logstashLogbackVersion by extra(\""$(logstashLogbackVersion)\"")" >> "${filename}"
echo "val prometheusVersion by extra(\""$(prometheusVersion)\"")" >> "${filename}"
echo "val jacksonVersion by extra(\""$(jacksonVersion)\"")" >> "${filename}"
echo "val caffeineVersion by extra(\""$(caffeineVersion)\"")" >> "${filename}"
echo "val slf4jVersion by extra(\""$(slf4jVersion)\"")" >> "${filename}"
echo "val kotlinxCoroutinesVersion by extra(\""$(kotlinxCoroutinesVersion)\"")" >> "${filename}"
echo "val micrometerVersion by extra(\""$(micrometerVersion)\"")" >> "${filename}"
echo "val kafkaVersion by extra(\""$(kafkaVersion)\"")" >> "${filename}"
echo "val kafkaEmbeddedEnvVersion by extra(\""$(kafkaEmbeddedEnvVersion)\"")" >> "${filename}"
echo "val orgjsonVersion by extra(\""$(orgjsonVersion)\"")" >> "${filename}"

cat ${filename}