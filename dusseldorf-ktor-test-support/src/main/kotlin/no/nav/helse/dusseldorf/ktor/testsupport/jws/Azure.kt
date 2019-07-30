package no.nav.helse.dusseldorf.ktor.testsupport.jws

object Azure {
    enum class ClientAuthenticationMode(val claimValue: String) {
        PUBLIC("0"),
        CLIENT_SECRET("1"),
        CERTIFICATE("2")
    }

    object V1_0 : Issuer {
        private const val version = "1.0"
        private const val actualIssuer = "http://localhost/azure/v1.0"

        private val privateKeyJwk = """
        {
            "kid": "_iQwR_9GRT41GekNOP3P3evNBjI=",
            "kty": "RSA",
            "n": "ufVCnAjIHFbLLZbW4EtreAo7iQzj7XdblniGRhVv1ZsvXI4LckY_s0ttp6JYIbBq16K-hn7Kokqcrxu5npQwQNSfz8QnlZ4qrcmgvMLln0TgSjwxdQw1uFNcoTKcx6BsTOth2MoaR76M7WlqOm6ddluksSSNoktTaakpIQ5dt6tvUEGb_wHoq0myx5nZSo3xCFUs1P9pStzRWbUgGu_h5gye_M4DEMlZ166gaQ0pR_omeHJ4a7WhRHAPjMsK0P9zF9TpgRiD5xy8sPsExlCI3vvl965A5M59KCJfIxQgvdHbzg7h6jq-KA-eWGuJ23azKbt0wruyGKRw-SOyObDYUw",
            "e": "AQAB",
            "d": "UCKjC761DZFRiVnhS1L1AwvV0B-f8WtoKpXRt03xnal8ZXRI9nvTY55zqLPLKultgXN-i1MEW5-vmb5m5TGTAb0fCVfMfytj1PR4sh-C-Hmj9hmubnROihpSZpz69wPo59Tfm56saJJzBLRStM-KR8ECXdwUtQWk8nmVYcthE46vFawEgs3h7qhzqyi2mjGttJiu5zAplnSYx-Mj61mVHt3fKx0bSN-2xvHl15jBdsJsca9br2QKvVRqhVUXaVxxn007IGPREy05Xf2E07zAyVc8XcDd_rtyl9bfIByfXCtjLMkfZPZsn1xSwAfyqQR3MJ6QAYhs-0dLnz_fJ1LHAQ",
            "p": "31IgyXmeecUOQ3boXGaNi2AZSIGAxkHvoLkSKl0Gum5a4WSqxjSkOnhvQLKoTJ11L3ggBn2QTVkwR1GYoam_Brs91YAl3-cf9dwu3Bj8_T1_8rbijCkkUrdNCN_-6CHraqrQm7ebsTiEXossTJi0EgeNTvGUwnjyVMSXb88k0xM",
            "q": "1St6H9DWfdCL3s_v_7g3p8DLlkzLn7GK_e8wuw3BaTxQD6MoWLTQaKTEKXBR46-5M6j1xnDlE81hUnxInLCnIilwv97NY-xHSDUEsif9vGpH9XS0wcV-5d13oyyyIfVqHne9bfjYgVN2awAAgbBpnTCPD1FpOq5ANyh_JgPrTcE",
            "dp": "O8Z57DPXjXSHettrGsCAhpv0aInxdB_WCkk_aYN495HuVYJii6cvhk8SSFkJ32gOI-Sdfd9CLtH-sDhRCIStExovVOR2PtZYobCMAnHV1dsOUmNNhILGrDVQM13xlyTtJKIQMH4W-HrOQ5gxBVeia2kGE-lqDhvdkX8FofHqQ20",
            "dq": "eQdDQjcW4L37-9hBYgXci4D51e7qGVP7HyujlkA8FJyqtAYljR-zyAXsXb6or8YAN6VxTWahmTmdTPDFZTki21F982HzaQ0od4WqkeAEvun0yxak3fNclfPEzj89L-yfgIbpZTvI6LxTWnLY1skrXZTJVKAbR7bRPL_gWNwWx8E",
            "qi": "JAzispFSyRp044NuSwA3odZJtctHfH9D93bxtkU9voTTrWh9uzh-eFtL3LzmQs0qSz8Hun7KOMPoiZ-TzddSjrSAP-5mTVTrwtQulMR8WvGYTeIA6YVl5UWdxrmAkUHJs5rTfeKLIMYVoBJzg-_lNWGziJpIQa8sLwtlk0HoDs4"
        }
        """.trimIndent()
        private val jwsFunctions = JwsFunctions(privateKeyJwk)

        override fun getIssuer() = actualIssuer
        override fun getPublicJwk() = jwsFunctions.getPublicJwk()

        fun generateJwt(
                clientId: String,
                audience: String,
                clientAuthenticationMode: ClientAuthenticationMode = Azure.ClientAuthenticationMode.CERTIFICATE,
                groups: Set<String> = emptySet(),
                roles: Set<String> = emptySet(),
                scopes: Set<String> = emptySet(),
                issuer: String = actualIssuer
        ) = jwsFunctions.generateJwt(
                claims = mapOf(
                        "ver" to version,
                        "aud" to audience,
                        "iss" to issuer,
                        "appid" to clientId,
                        "appidacr" to clientAuthenticationMode.claimValue,
                        "groups" to groups,
                        "roles" to roles,
                        "scp" to scopes.joinToString(" ")
                )
        )
    }

    object V2_0 : Issuer {
        private const val version = "2.0"
        private const val actualIssuer = "http://localhost/azure/v2.0"

        private val privateKeyJwk = """
        {
            "kid": "pxGPIjJP0b9r6ywnukU9ZYig5bo=",
            "kty": "RSA",
            "n": "ymdcz36cE2sSVRejEOhFf-OBAzCBlDNhOl2xEYXnVEt2pdfY8VtKvrAxSc4WUe6eiRmxx-M8bIAhStcjuNWal2fTszMuxg0BNGtiG3LHfp6rP0y3epUOTh-g1fcMd_VxjquMhWntAfh9pehYl6_BewdolvWomAEW3UNKwclNNpbGGvEVsn8tjKrkw6a9O-cniqYrLRL8wmlYFONulwcdXBgW02IYipRQ5zZkMEakfv3ZuZIoyU-_CnINH2wwqvPnMXdVzCCtAINAadUSDAhzAS-XjCATh-55bv163wACw0D1wW5OALEXeUjK3JbpWLO3uViSUfEYEUX-wmTsGrjjZw",
            "e": "AQAB",
            "d": "VDhqUBS40QOunyW0vqZHtQ1vc2pNoOM4Q9cUhNwZA-RavGtyZAu-sFYUTEeq1fDamuXMKgaN7__o2oFm5dRL_VBAsMJNZi-nHq7IJe--vxs62p4LgsBfMXbLr_yafZft5pXPZPxMmJNThSOHKacCdTUB-j7CJm3dm7gSdUxUCrA3kYLtRR8MxN5kSEjG7sVLCYGky1TmO__DWqHzFNB2l_9WUYOZoOii8-JpSEunXXw3EdDo1IeR4aBeKr1pdshuXcENvfB0tmzxQxi_gZLL1RTw12pJLmjRb3KRZIPiHUYVeAazpDToeTeNVheORAnrCyxY9i67ehMbl9qz-PoJsQ",
            "p": "_2Izrld2rhNewhLoZlxv6XvTO70pLZLzCm-lnQMMxqJqAPKAqa4vhK0Jm3ok5qCakXtn5qv0Kn5bgza9rPUky-P9Jl7CSGhy1V7uFHQWt-lP1RVnv6f60kxEV5j9vWPXnPHxiCj8euEoOvpexGuoPTM099lsczw7uDLw9z7jiss",
            "q": "yuRs1PvNZQUzCoBn-xD-IBXNTMdCK2aa3V2yGKKSDEvKE4rVpnfjNmo6B0OJtAURlgc6n8oazUsfm-w3I8kq0BGSL0lWVQ8CIhTtnNK1NIOBTWDRSwZw4o6C-3Dqwi3v3UTBeXTeHgeFMSW1M9smfGj31k1pws8B_7Zj1IfjqlU",
            "dp": "xh7O7R-ZSG5qgrDcbhykDUQsmRmkimCH_76hgm9NSAPTrKx0uC4TWyBKZb2aRvitMNPFxSP9JcIlCGQ9PaJoS2yxhUTaAAZXn8QneuKchUyQzEPw2rRcVy0nj7V2k6iTKoRf1jvFsyZdaXO2dTb3q5LAMs8P3U4LHlkWqxi0uYU",
            "dq": "i3SScZpZxRPbz14PGnzFj1ws7pcPHjG6RGmAXnpvlFALp38H9xH0dJRzKkb7wWayfcKeQvJxlaYLJesow0okSTuqlSH9Wx2jw7VK1T3nGx9AouTnNoBI2xBFa3pjgfB9LEN0EH2Jpm5Y2D0o3WnGfEDSCNTQ2vvaTd8Zox18GLk",
            "qi": "WQQAejSpzO-ox8h8MmoRTORAfXCYSliG1a5RYcLS-_4ZSdj1ijL31hJm1veibwIoOkxBDShZP7uZ88WJFDJS80V0Xa-nbqc0v51EVracwvfo7Nw7LC1uXDpa_h6EjZTJYv6le2_aM5_JE0FVG__91LLVmd-WJzaPOM5q2jHxmSM"
        }
        """.trimIndent()
        private val jwsFunctions = JwsFunctions(privateKeyJwk)

        override fun getIssuer() = actualIssuer
        override fun getPublicJwk() = jwsFunctions.getPublicJwk()

        fun generateJwt(
                clientId: String,
                audience: String,
                clientAuthenticationMode: ClientAuthenticationMode = Azure.ClientAuthenticationMode.CERTIFICATE,
                groups: Set<String> = emptySet(),
                roles: Set<String> = emptySet(),
                scopes: Set<String> = emptySet(),
                issuer: String = actualIssuer
        ) = jwsFunctions.generateJwt(
                claims = mapOf(
                        "ver" to version,
                        "aud" to audience,
                        "iss" to issuer,
                        "azp" to clientId,
                        "azpacr" to clientAuthenticationMode.claimValue,
                        "groups" to groups,
                        "roles" to roles,
                        "scp" to scopes.joinToString(" ")
                )
        )
    }
}