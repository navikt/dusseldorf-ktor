#!/bin/bash
CLIENT_NAME=$1

if [ -z "$CLIENT_NAME" ]
then
      echo "Client Name må settes"
      exit 1
else
    echo "$CLIENT_NAME"
    openssl genrsa 2048 | pem-jwk > private_key_$CLIENT_NAME.jwk
    pem-jwk private_key_$CLIENT_NAME.jwk > private_key_$CLIENT_NAME.pem
    openssl req -x509 -key private_key_$CLIENT_NAME.pem -new -out certificate_$CLIENT_NAME.pem -days 730 -subj '/C=NO/O=NAV/CN='"$CLIENT_NAME"'.nav.no'
fi