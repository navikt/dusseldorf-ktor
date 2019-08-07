package no.nav.helse.dusseldorf.ktor.testsupport.jws

object NaisSts : Issuer {
    private val privateKeyJwk = """
    {
        "kid": "tH1GrEoJXDEbtqg1C2xllTflR-Y=",
        "kty": "RSA",
        "n": "t-JvfVrdGYmtJfGaJiRC6gcbAaN4p_YTZDPus0XRap3yXs0ibqJm1g3u2LNAzmdbPMNte0Kd6XREaLJaqifsoRxnUwi8tOPtPCInyJh3AOt_1NjsX7lfqfYvFiZwODXi6pOdJ-58fGZ6lOHgeqVFavOlYYlk-X8tYRIAk_UMB72Yiu5V1e0WUi6vTfNA_gy3DJSG_GxueP6FgQHI-g6ejydFKo2cXSHhtDiud81ja7w1z8NJB0TEWKhBDo0scXUp25PJDerb-PVbvMSlpdSFQg45NP3yQi7L0pJuvu0KMLbRTTCKvVNdEpL9lY48AQa2o9yRdzYi82kh-6xZVyTiUw",
        "e": "AQAB",
        "d": "VIFlRzCIx4Gz5uofM0Qa1D4rtRcRDe9Sfgw3uH9vfBXWGqcfGcTciaahi0jlkXWCQmx1Vd4JgA4EmjDM5cGvR2MsQMaVInRUw_vWWKNonS2c8441ItCoHA2dGgrUK6DoBXGNGZrJ7smRr_YWGYb2Xe5WpHplwxslGaUSSuykeLFrF2901GNHsTdGnDC3_Slgo6E4ONxgLfwt8Bg-d1iv92pg1tu6a-7StijNbJ-kH1LdS0D6nc9h8KjuqY7ExAd5kiI4InqU_nV6bBeFaAbYaKGU_tDLUMCNVETk1cZxE6HIwA8C76XTZB0l4vKiltNDrYwFBdJg_1uwjc92z5GmQQ",
        "p": "6BiZF77UuxA8q55KpSCIyWf1LPb57DWgq85gKLd2Yt_N8jN2Wo6GQlFmnJNc_vYYJHC8rzQG165_io0MbP3oHRlKlkvJi8yIl1ZBwCuh1L5QzOv2kglbXeWp6No_aJq5d4TeGrgRP8_xv_iKnqUr62T7p951M3hsREaEQJZ0icM",
        "q": "ytKzP8qhDRr044tI5gCgFOdu0Yd5RBMm8RH29dLYXLtQWJTlKi4i3hcFyW1UUcS_5Crvop_VogBOfgLpBZpO-E--OP3GviLrAA5vkmc1A8i5pvcXcIdVBKfaa7mSqYqKnaWk-uuZXY3VFw6V2dTpV2tI3IZzMDwiNyF_B0SjLDE",
        "dp": "mPvNvWcd1YeoVf7qeNlvEEKjytWoetgGZfiMTYpQUG_w0dq2Ao67VG9PUZmYzL3tv-dHl7yVXgk5LFw4eocKPlPzZAnkeW-CAS7r-_1GmHIx1EEcr7nXLZc6Xa-t6AdEhNFhjP8S75rjYIQ06sk4n9paPpZVbm3L_1zzw5bojf0",
        "dq": "Ry1QGGfswuOd4yijKYw4oX1iZ4eqZ9hsxxbt2vDmWwylMqD2FypftB7SOhDzEUsBqHnk5OZAm3yODyzIR94SLpQVhnyQvXMVk0x9nRw4_532gQCfqi7kCCE7xqg2Ludeo9pYnnIFvU4j5NaKggp05OfShwlr7RZ9T2mi4ZMVzFE",
        "qi": "ND0Sney-ImOS-edhfzJufNjzq-IkEpNY4LEYQUWd-uDixlxSL5nI1SMP6b3HxiCm2JSDFwZU6s1M5iJ8o-KC6OFP3xAfCGDHBmrOuTLDoPVT542IyXWs482t686Po6lT1b43vfCkMC82zDnAuj2MbxCmQDx0ndhUDfKMB9tllXs"
    }
    """.trimIndent()
    private val jwsFunctions = JwsFunctions(privateKeyJwk)

    private const val audience = "http://localhost/nais-sts/audience"
    private const val actualIssuer = "http://localhost/nais-sts/issuer"

    override fun getPublicJwk() = jwsFunctions.getPublicJwk()
    override fun getIssuer() = actualIssuer
    fun getAudience() = audience

    fun generateJwt(
            application: String,
            issuer: String = actualIssuer,
            overridingClaims: Map<String, Any> = emptyMap()
    ) = jwsFunctions.generateJwt(
            claims = overridingClaims.toMutableMap().apply {
                if (!containsKey("sub")) put("sub", application)
                if (!containsKey("aud")) put("aud", audience)
                if (!containsKey("iss")) put("iss", issuer)
            }.toMap()
    )
}