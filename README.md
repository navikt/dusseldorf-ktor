# Düsseldorf ktor
Boilerplate for ktor-applikasjoner

## Releasing new versions
https://docs.github.com/en/repositories/releasing-projects-on-github/automatically-generated-release-notes

## Moduler

### dusseldorf-ktor-core
Minste samling av dependencyer for en ktor app for Düsseldorf.

### dusseldorf-ktor-client
For applikasjoner som trenger å gjøre HTTP requester.

### dusseldorf-ktor-metrics
Metrics for alle requeter til applikasjonen og tilgjengeliggjør Prometheus metrics end point

### dusseldorf-ktor-jackson
Status pages og dependencies for jackson

### dusseldorf-ktor-health
Helsesjekker og tilgjengeliggjøre helsjesjekk end point.

Gauge "health_check_status" kan brukes i Grafana på følgende måte
```
sum_over_time(health_check_status{app="<APP>"}[2m])
```
Videre mappe alt over 0 som feil, 0 som OK

### dusselforf-ktor-auth
Verifisere innhold av claims i token

### dusseldorf-test-support
Modul for testing av applikasjonene. Inneholder funksjonalitet for å generere Access Tokens for tester og WireMock for tilgjengeliggjøring av OIDC/Oauth2 end points.

Ikke KTOR-spesififkk.

### dusseldorf-oauth2-client
Client for å hente Oauth2 Access Tokens.

Ikke KTOR-spesifikk.

Se egen README.md i denne modulen.

## Fødselsnummer
I Testklassen `StringExtTest` finnes funksjonen `genererFodselsnummer` for å generere matematisk korrekte fødselsnummer for bruk i enhetstester hvor det er nødvendig.

For å minimere sjansen for å bruke aktive fødselsnummer genereres det fødselsnummer med individsifre som er reservert for perioden 1854–1899 (500-749)*, samt fødselsdato innenfor samme periode.

Om det mot formodning blir generert et aktivt fødselsnummer er ikke fødselsnummer i seg selv en senstiv personopplysning**.

[* skatteetaten om fødselsnummer](https://www.skatteetaten.no/person/folkeregister/fodsel-og-navnevalg/barn-fodt-i-norge/fodselsnummer/)

[** datatilsynet om fødselsnummer](https://www.datatilsynet.no/rettigheter-og-plikter/personopplysninger/fodselsnummer/)

## Henvendelser
Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub.

Interne henvendelser kan sendes via Slack i kanalen #sif-brukerdialog
