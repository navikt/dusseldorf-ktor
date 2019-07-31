# Düsseldorf-oauth2-client

## Generere Client sertifikat
* Bruker openssl
* Bruker npm-pakke pem-jwk (https://www.npmjs.com/package/pem-jwk). Må installeres globalt.

```bash
cd scripts
./genereate-client-certificate ${client_name} ${environment}
```

Opprettes to filer som brukes til å registrere og bruke clienten.

Om det ikke settes environment (prod) blir `${common_name}` satt til `${client_name}.nav.no`


Om det settes et environment blir `${common_name}` satt til `${client_name}.${environment}.nav.no`

### certificate_${common_name}.pem
Lastes opp i Azure Portal

### private_key_${common_name}.jwk
Legges i vault og brukes i applikasjonen i `PrivateKeyProvider:FromJwk`

## Hente Key ID
* Bruk thumbprint som vises i Azure når sertifikat er lastet opp i `KeyIdProvider:FromCertificateHexThumbprint`
* Bruk sertifikat pem-fil i `KeyIdProvider:FromCertificatePem`