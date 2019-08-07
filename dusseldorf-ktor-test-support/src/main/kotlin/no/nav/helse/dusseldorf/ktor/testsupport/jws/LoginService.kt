package no.nav.helse.dusseldorf.ktor.testsupport.jws
import java.time.LocalDateTime
import java.util.*

object LoginService {
    object V1_0 : Issuer {
        private val privateKeyJwk = """
        {
            "kid": "R9iKtD8v3h3U_Rkx7b4mtqQ_JZg=",
            "kty": "RSA",
            "n": "s-q-Mceu6SGG-M7f_bPgXHvGBBfPH2FHGuuurUych5Ytr5C-4j8rDiGsHDAthveBt7oC6KbfHFJH23TlyXJ08PcjtrIyNWYrSfUZ0VTy6qd4LtemElFXaDXMgJTX2gLEPEIg3A-XNmyqj-FwIfwCFbCE2ZmMZd3XX76p5yFAur7updRn8LT_Px_jzr3CSJ3e-E5V3mVEcFL_qlpggq06rWby81WGLPtAXE4t9N2KLRW7-GY-2w3I2se8zHzOxwNkKvWuCPe7EcC-MpaDEWvTiB4NcYW2t1Oj8s5QSNvmpbuvZjpMshsrSBsw6nEijv36MPcUwDFk_6l70xlOa6kbmQ",
            "e": "AQAB",
            "d": "nLHX0BcveQ-gM9ZJMvaUczfBcBMkaxoYCZQ2X-yXb2cWhw-zj-ygHQCqmypCnIe3zEIPAn1DV7E5sCKRdu9edfdofVeZbvR6w0DaqXwozMHqZDGhC9cYH0SrxVjc3Z0HN8wIPmC9u8LMkd8wAAkx7537NmEK7DVXvKWb6BcH_xuRcjE5Q140UU6x1qXjEo_iqftti1XeLKL6FDYtbOfGLPbl_W10Zs-ir_6VfYuSR7dgGc7R5MU-iH32VWx8X9rXW4xua0XoghhLitvhcBjRXLdXgEILxlVt8_JQlZNoGB4Z15-Dt4ZtNQQETJFNkE6aEvYzTP971ehGT_cp9T43AQ",
            "p": "2doRP23VZINdXMvFdPRKZ7j8V1LeKtksAZfTIEraH0yOy9ykQ2TjnGwYzh-pSUgxwAPOhyR-ljAZoonH5aPuzkLNTeywVFkGxuU7CwcwPM1y4R8lV1gr8TWE3YbLwTLvTm7fYHU0GdJRJuHHQ_ShR3MgB76adbLpyCoWwdUOYEk",
            "q": "02weh-ykq_ej6DKO10VVI0KYEAGtN2n9wyahHQ8fHYu3jP9WKRSykQ7IOjz7_kjkRaYuHzsZAD8bghfaOkC6-hxrP48az7xIs4AYYSzSFX0oLjv2Remhnp9A6AH-XCH-fIPQKoEVlZNQRaWkvO5AIGCH7sDdwCe_EF2uRASIgNE",
            "dp": "xEZqn8EvL34DkeWto2-t0cGi6HIq5GQB9_V4LHIED4sEZLwH6whs4bOqrZttBC9dU2ianLvOqTXxArYsOcP3FjKtknWbcIM9Tdmd1pPFbCKWTCmplQZFoO7dJFGcJhbj8MCH1PoD1PfeUa-9voYbAKrCwveBZmWpp8Pz-PMiHrE",
            "dq": "D0WaFvtzEbD7uAQe6VHqync_ALj-SChx2izqKm0zNDEpexfeo20JUQ0eTYamNUISqy1L25mrPqdQV19dtiOC3ho7bQXrS-Iv5jd0i2BvdHnr8xsFuRsdP5YFXDz4oLMb7OagnNFiO8G-ZKmIctVK8IvIheiMnBrC5G2_7s3dBmE",
            "qi": "HR0V3SasA2xHdLZHauJquOAvRbBk-7qwZMyruqkGa_XWrA1L_9xhehQEvPyvYtYL2qUTOV9yIzqljkvXfIjSPSFUpZUxuTG90WKVGkjR9nJkfO5qtL7yefndLdpDSHyJFL6CG1vQkuxarGpwPPFCVN0NVrrKfnYmFzw3qZd5riw"
        }
        """.trimIndent()
        private val jwsFunctions = JwsFunctions(privateKeyJwk)

        private const val version = "1.0"
        private const val audience = "http://localhost/login-service/v1.0/audience"
        private const val actualIssuer = "http://localhost/login-service/v1.0/issuer"

        override fun getIssuer() = actualIssuer
        fun getAudience() = audience
        override fun getPublicJwk() = jwsFunctions.getPublicJwk()

        fun generateJwt(
                fnr: String,
                level: Int = 4,
                issuer: String = actualIssuer,
                overridingClaims: Map<String, Any> = emptyMap()
        ) = jwsFunctions.generateJwt(
                claims = overridingClaims.toMutableMap().apply {
                    if (!containsKey("acr")) put("acr","Level$level")
                    if (!containsKey("sub")) put("sub", fnr)
                    if (!containsKey("aud")) put("aud", audience)
                    if (!containsKey("iss")) put("iss", issuer)
                    if (!containsKey("auth_time")) put("auth_time", LocalDateTime.now().toDate())
                    if (!containsKey("ver")) put("ver", version)
                    if (!containsKey("nonce")) put("nonce", UUID.randomUUID().toString())
                }.toMap()
        )
    }
}