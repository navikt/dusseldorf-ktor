# Düsseldorf ktor
Boilerplate for ktor-applikasjoner

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
Helsesjekker og tilgjengeliggjøre helsjesjekk end point

## Fødselsnummer
I Testklassen `StringExtTest` finnes funksjonen `genererFodselsnummer` for å generere matematisk korrekte fødselsnummer for bruk i enhetstester hvor det er nødvendig.
For å minimere sjansen for å bruke aktive fødselsnummer genereres det fødselsnummer med individsifre som er reservert for perioden 1854–1899 (500-749)*, samt fødselsdato innenfor samme periode.
Om det mot formodning blir generert et aktivt fødselsnummer er ikke fødselsnummer i seg selv en senstiv personopplysning**.

[* skatteetaten om fødselsnummer](https://www.skatteetaten.no/person/folkeregister/fodsel-og-navnevalg/barn-fodt-i-norge/fodselsnummer/)
[** datatilsynet om fødselsnummer](https://www.datatilsynet.no/rettigheter-og-plikter/personopplysninger/fodselsnummer/)

## Henvendelser

Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub.

## For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #område-helse.