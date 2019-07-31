#!/bin/bash
CLIENT_NAME=$1
ENVIRONMENT=$2

if [ -z "$CLIENT_NAME" ]
then
      echo "Client Name må settes"
      exit 1
fi

if [ -z "$ENVIRONMENT" ]
then
    COMMON_NAME="$CLIENT_NAME.nav.no"
else
    COMMON_NAME="$CLIENT_NAME.$ENVIRONMENT.nav.no"
fi

echo "$COMMON_NAME"
openssl genrsa 2048 | pem-jwk > private_key_$COMMON_NAME.jwk
pem-jwk private_key_$COMMON_NAME.jwk > private_key_$COMMON_NAME.pem
openssl req -x509 -key private_key_$COMMON_NAME.pem -new -out certificate_$COMMON_NAME.pem -days 730 -subj '/C=NO/ST=Oslo/L=Oslo/O=NAV (Arbeids- og velferdsdirektoratet)/OU=NAV IT/CN='"$COMMON_NAME"''