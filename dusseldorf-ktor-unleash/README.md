# dusseldorf-ktor-unleash
Modul for konfigurasjon av Unleash klient for feature toggeling.

## Konfigurasjon
Unleash klienten er konfigurert med følgende egenskaper:

- **path** - Base path til konfigurasjon i konfigurasjonsfil. Default satt til `nav.unleash`.
- **isSynchronousFetchOnInitialisation** - Henter oppdatert status på feature flag ved instansering. Default: ```true```
- **fetchTogglesInterval** - Intervall for henting av oppdatert status på feature flag . Default: ```1 ms```
- **sendMetricsInterval** - Intervall for publisering av metrics. Default: ```1 ms```
- **subscriber** - Listener for feature flag endringer. Default:
```kotlin
object : UnleashSubscriber {
        override fun onReady(ready: UnleashReady) {
            logger.info("Unleash is ready")
        }

        override fun togglesFetched(response: FeatureToggleResponse) {
            logger.info("Fetch toggles with status: " + response.status)
        }

        override fun togglesBackedUp(toggleCollection: ToggleCollection) {
            logger.info("Backup stored.")
        }
    }
```

## Properties
Unleash klienten er konfigurert gjennom ApplicationConfig, som en extention function.

For å kunne bruke den, må følgende properties være satt i application.conf.

- **{path}.app_name** - Navn på klienten. Default til miljøvariabel: `NAIS_APP_NAME`
- **{path}.instance_id** - Navn på instansen av tjenesten. Default satt til miljøvariabel: `HOSTNAME`
- **{path}.api_url_** - API url til unleash server. Default satt til https://unleash.nais.io/api/
- **{path}.cluster** - Returnerer en FakeUnleash dersom cluster ikke er en gyldig nais cluster. Default satt til miljøvariabel: `NAIS_CLUSTER_NAME`
