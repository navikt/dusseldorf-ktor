package no.nav.dusseldorf.ktor.oauth2.client

import java.net.URL
import kotlin.test.Ignore
import kotlin.test.Test

class SignedJwtAccessTokenClientTest {

    @Test
    @Ignore
    fun `Hent access token`() {
        val client = SignedJwtAccessTokenClient(
                clientId = "4bd971d8-2469-434f-9322-8cfe7a7a3379",
                keyIdProvider = FromCertificateHexThumbprint("C49D374041458D12109E1ED9A4879C23D28EC7FD"),
                tokenUrl = URL("https://login.microsoftonline.com/966ac572-f5b7-4bbe-aa88-c76419c0f851/oauth2/v2.0/token"),
                privateKeyProvider = FromJwk("""{"kty":"RSA","n":"wUL5dWmNwukaFhXfhVMLvP5ql46ClAyYB_UwzK-g5YnGF6V2Nflv9-40f0MpisZHRP9up6LXm2PEowouVIqL1FzTRUqXUCYt5uHrQsSgVf_EYun1reucHqlpOO4t7Y1a02hgNyoPFrAUeHs2LydJXqsutUnDdPXmQ2lIJHDmlkPv_DaPd9ffPha2Cs6OxhvZsIFu1fH9z4pgFUNPPrmtiSL1X5WM19YIPOoaKykU1KOpCOqIoOYdauQggQkP31CB2pxfObAozGmaELqhBvZBE85N94nbAJ2yfuHC-7Kpi5aO7CqlQR8uJH1dDUdUGGcp5tomVz_hxihLWf8QuHRKNw","e":"AQAB","d":"rvd4q3DttAmf4WDaJXVjCi73x9BwraKdPY8hKB7VNxq9qgjV9dWnJjqVcqYWhRovyerLfp9yPa9chhMgkM-B-rgzWedHCHwYUJA2kxM66cEORlL0ZwoJJMVRiwYvtIKmOQGlIixEztCJJ-68xoH17exi9CQc_dXmKx-0_bDtQhoYdmwq-yQ9QNFN29h0nXzC9fxdqZu_m0iQz7mnOozWildWMfPTA9maw3oUPfXpXKL2Ytnn7tuKWdS77lmuqsjMVgOeOafRjm7I0kDEi-T24Ws8do6WYB7Lvwl8x9S6QjkW7RIpjl11rsFo5XzEDSAEkn28eT54-dsR82nRclJPeQ","p":"-LG-HTc7QzMQ0fs1GVHDJhb6KaohCQyKLx5ZVXpppipBL0r7TwFC8VQ2ewN1nIKE5eFnG2k-QubmaVmg-hPELZgvMLOO5pEvocOu5jhorExPA6yb_XxbZvaVHQ7DVUQPIsxa6bLRxOMCCLraDb9Mk1Em_pb5oC6JghfK3bifEW0","q":"xvBca8iOYwZkGRDGpw9emzo6z-iBl4OHCB3cMeY5rtROHbsgJ6dcd8oTuzefEscj41RQtGsgjw0UWMIQ8l_RhbkkzYIfFmkvNmNqJcxXfVcuvI7WQGnetI9UzJ0LALxEueURP2MbydMuoBiRsTjRMpmBBhNRgOZBE9kNk0Mxp7M","dp":"oGUilH02v5SD2KoICRhuoHZZSd3sCIYJ6XHNdA_La9v8xp-5ja7mmfcSXCxTAmo7hHfnpAowmb2KBZBE_oUZFb79UI--Ln6dFdu7RchD2jwtCdWdldNKsBGBAoiu-qM2j971E5y9JhqzCSZZ1Fv461p_p9t_jAv3q-vkbzPPg9E","dq":"O298bUdSIwu5xDNa5naVEVNoVs1kSlwlb6tcKhxah30uiXtqs-4wliltk7WedQHCGx0Jr52B8Ls7pPj2DzPJaWZTNIL3vr5WOK2i_P_785qkf_k80anPu6pG74rLysB02AU0DfgSSU4q0_IDNSuAdNmfzqze4N_p-YATqjedEjk","qi":"YXSa0y9QitZhYzeFPmLw5wX6S3t7A4V_HJs2ule2-8w7e7lckrOPxRndmKQ8Pql3pNsQjhqtgZKRo30qpsBng_fklZy4-JfdJIVRtVrHo3aP6sTFvGynib0GK6tk-cCQ37gUnDYnZEQfVdeB3EKbsiwFXxQf9zpE0u68h4pbn30"}""".trimIndent())
        )

        val resp = client.getAccessToken(
                scopes = setOf("5a5878bf-7654-490d-bbdd-6eb66caac4a3/.default")
        )

        println(resp.getAuthorizationHeader())
    }

}