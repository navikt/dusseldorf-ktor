package no.nav.helse.dusseldorf.ktor.testsupport.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import no.nav.helse.dusseldorf.ktor.testsupport.wiremock.Paths.AZURE_V1_JWKS_PATH
import no.nav.helse.dusseldorf.ktor.testsupport.wiremock.Paths.AZURE_V1_TOKEN_PATH
import no.nav.helse.dusseldorf.ktor.testsupport.wiremock.Paths.AZURE_V1_WELL_KNOWN_PATH
import no.nav.helse.dusseldorf.ktor.testsupport.wiremock.Paths.AZURE_V2_JWKS_PATH
import no.nav.helse.dusseldorf.ktor.testsupport.wiremock.Paths.AZURE_V2_TOKEN_PATH
import no.nav.helse.dusseldorf.ktor.testsupport.wiremock.Paths.AZURE_V2_WELL_KNOWN_PATH
import no.nav.helse.dusseldorf.ktor.testsupport.wiremock.Paths.LOGIN_SERVICE_V1_JWKS_PATH
import no.nav.helse.dusseldorf.ktor.testsupport.wiremock.Paths.LOGIN_SERVICE_V1_WELL_KNOWN_PATH
import no.nav.helse.dusseldorf.ktor.testsupport.wiremock.Paths.NAIS_STS_JWKS_PATH
import no.nav.helse.dusseldorf.ktor.testsupport.wiremock.Paths.NAIS_STS_TOKEN_PATH
import no.nav.helse.dusseldorf.ktor.testsupport.wiremock.Paths.NAIS_STS_WELL_KNOWN_PATH

internal object Paths {
    private const val AZURE_V1_PATH = "/azure/v1.0"
    internal const val AZURE_V1_TOKEN_PATH = "$AZURE_V1_PATH/token"
    internal const val AZURE_V1_WELL_KNOWN_PATH = "$AZURE_V1_PATH/.well-known"
    internal const val AZURE_V1_JWKS_PATH = "$AZURE_V1_PATH/jwks"

    private const val AZURE_V2_PATH = "/azure/v2.0"
    internal const val AZURE_V2_TOKEN_PATH = "$AZURE_V2_PATH/token"
    internal const val AZURE_V2_WELL_KNOWN_PATH = "$AZURE_V2_PATH/.well-known"
    internal const val AZURE_V2_JWKS_PATH = "$AZURE_V2_PATH/jwks"

    private const val LOGIN_SERVICE_V1_PATH = "/login-service/v1.0"
    internal const val LOGIN_SERVICE_V1_LOGIN_PATH = "$LOGIN_SERVICE_V1_PATH/login"
    internal const val LOGIN_SERVICE_V1_WELL_KNOWN_PATH = "$LOGIN_SERVICE_V1_PATH/.well-known"
    internal const val LOGIN_SERVICE_V1_JWKS_PATH = "$LOGIN_SERVICE_V1_PATH/jwks"

    private const val NAIS_STS_PATH = "/nais-sts"
    internal const val NAIS_STS_TOKEN_PATH = "$NAIS_STS_PATH/token"
    internal const val NAIS_STS_WELL_KNOWN_PATH = "$NAIS_STS_PATH/.well-known"
    internal const val NAIS_STS_JWKS_PATH = "$NAIS_STS_PATH/jwks"
}

fun WireMockServer.getAzureV1WellKnownUrl() = baseUrl() + AZURE_V1_WELL_KNOWN_PATH
fun WireMockServer.getAzureV1TokenUrl() = baseUrl() + AZURE_V1_TOKEN_PATH
fun WireMockServer.getAzureV1JwksUrl() = baseUrl() + AZURE_V1_JWKS_PATH

fun WireMockServer.getAzureV2WellKnownUrl() = baseUrl() + AZURE_V2_WELL_KNOWN_PATH
fun WireMockServer.getAzureV2TokenUrl() = baseUrl() + AZURE_V2_TOKEN_PATH
fun WireMockServer.getAzureV2JwksUrl() = baseUrl() + AZURE_V2_JWKS_PATH

fun WireMockServer.getNaisStsWellKnownUrl() = baseUrl() + NAIS_STS_WELL_KNOWN_PATH
fun WireMockServer.getNaisStsTokenUrl() = baseUrl() + NAIS_STS_TOKEN_PATH
fun WireMockServer.getNaisStsJwksUrl() = baseUrl() + NAIS_STS_JWKS_PATH

fun WireMockServer.getLoginServiceV1WellKnownUrl() = baseUrl() + LOGIN_SERVICE_V1_WELL_KNOWN_PATH
fun WireMockServer.getLoginServiceV1JwksUrl() = baseUrl() + LOGIN_SERVICE_V1_JWKS_PATH
