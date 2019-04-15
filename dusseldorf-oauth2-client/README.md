# Düsseldorf-oauth2-client

## Generere Client sertifikat
* Bruker openssl
* Bruker npm-pakke pem-jwk (https://www.npmjs.com/package/pem-jwk)

```bash
cd scripts
./genereate_client_certificate ${client_name}
```

Opprettes to filer som brukes til å registrere og bruke clienten.

### certificate_${client_name}.pem
Lastes opp i Azure Portal

### private_key_${client_name}.jwk
Legges i vault og brukes i applikasjonen i `PrivateKeyProvider:FromJwk`

## Hente Key ID
* Bruk thumbprint som vises i Azure når sertifikat er lastet opp i `KeyIdProvider:FromCertificateHexThumbprint`
* Bruk sertifikat pem-fil i `KeyIdProvider:FromCertificatePem`
