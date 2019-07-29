package no.nav.helse.dusseldorf.ktor.testsupport.wiremock

import com.nimbusds.jwt.SignedJWT
import java.util.*

internal fun String.getExpiresIn() = (SignedJWT.parse(this).jwtClaimsSet.expirationTime.time - Date().time) / 1000