package no.nav.dusseldorf.ktor.oauth2.client

class TestData {
    companion object {
        val CERTIFICATE_PEM = """
        -----BEGIN CERTIFICATE-----
        MIICnjCCAYYCCQCNjhI4E/LY7DANBgkqhkiG9w0BAQsFADARMQ8wDQYDVQQDDAZu
        YXYubm8wHhcNMTkwNDEyMDk1MTI1WhcNMjEwNDExMDk1MTI1WjARMQ8wDQYDVQQD
        DAZuYXYubm8wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDBQvl1aY3C
        6RoWFd+FUwu8/mqXjoKUDJgH9TDMr6DlicYXpXY1+W/37jR/QymKxkdE/26noteb
        Y8SjCi5UiovUXNNFSpdQJi3m4etCxKBV/8Ri6fWt65weqWk47i3tjVrTaGA3Kg8W
        sBR4ezYvJ0leqy61ScN09eZDaUgkcOaWQ+/8No93198+FrYKzo7GG9mwgW7V8f3P
        imAVQ08+ua2JIvVflYzX1gg86horKRTUo6kI6oig5h1q5CCBCQ/fUIHanF85sCjM
        aZoQuqEG9kETzk33idsAnbJ+4cL7sqmLlo7sKqVBHy4kfV0NR1QYZynm2iZXP+HG
        KEtZ/xC4dEo3AgMBAAEwDQYJKoZIhvcNAQELBQADggEBAJcuC1mGr+7Fk/tvIQ3c
        +PkK6UJZZVziOLyQu1iibtXbhKbkyNLOKsVn9tBCr//1Rj3mzixDR20epEwbiYEP
        rEgioLVDXrn8NXDRJo8CYkOpJg+B7u52nnGlaieLjdH/SeOFA6QzZqfgVhvN5SOJ
        XcKlsI0qF6JSurD1rRrZ0Bq8m54XJib89Dh8btfFSFPD16tJ3Bwei6yKQ8CjTMKJ
        lGPIqG03nxXLF8Jq1Tn6RF8UDXq/cql+Tri1OyABQ1hJ6MwZUzNyP2IJvja3IMc6
        vfjHm1KBqirmWt7VAAh6mABYAP0o/LuR4Jw9LLlUFH6w68viYeh2LS6KYkBmv747
        IBI=
        -----END CERTIFICATE-----
        """.trimIndent()

        val PRIVATE_KEY_JWK = """
        {
            "kty": "RSA",
            "n": "wUL5dWmNwukaFhXfhVMLvP5ql46ClAyYB_UwzK-g5YnGF6V2Nflv9-40f0MpisZHRP9up6LXm2PEowouVIqL1FzTRUqXUCYt5uHrQsSgVf_EYun1reucHqlpOO4t7Y1a02hgNyoPFrAUeHs2LydJXqsutUnDdPXmQ2lIJHDmlkPv_DaPd9ffPha2Cs6OxhvZsIFu1fH9z4pgFUNPPrmtiSL1X5WM19YIPOoaKykU1KOpCOqIoOYdauQggQkP31CB2pxfObAozGmaELqhBvZBE85N94nbAJ2yfuHC-7Kpi5aO7CqlQR8uJH1dDUdUGGcp5tomVz_hxihLWf8QuHRKNw",
            "e": "AQAB",
            "d": "rvd4q3DttAmf4WDaJXVjCi73x9BwraKdPY8hKB7VNxq9qgjV9dWnJjqVcqYWhRovyerLfp9yPa9chhMgkM-B-rgzWedHCHwYUJA2kxM66cEORlL0ZwoJJMVRiwYvtIKmOQGlIixEztCJJ-68xoH17exi9CQc_dXmKx-0_bDtQhoYdmwq-yQ9QNFN29h0nXzC9fxdqZu_m0iQz7mnOozWildWMfPTA9maw3oUPfXpXKL2Ytnn7tuKWdS77lmuqsjMVgOeOafRjm7I0kDEi-T24Ws8do6WYB7Lvwl8x9S6QjkW7RIpjl11rsFo5XzEDSAEkn28eT54-dsR82nRclJPeQ",
            "p": "-LG-HTc7QzMQ0fs1GVHDJhb6KaohCQyKLx5ZVXpppipBL0r7TwFC8VQ2ewN1nIKE5eFnG2k-QubmaVmg-hPELZgvMLOO5pEvocOu5jhorExPA6yb_XxbZvaVHQ7DVUQPIsxa6bLRxOMCCLraDb9Mk1Em_pb5oC6JghfK3bifEW0",
            "q": "xvBca8iOYwZkGRDGpw9emzo6z-iBl4OHCB3cMeY5rtROHbsgJ6dcd8oTuzefEscj41RQtGsgjw0UWMIQ8l_RhbkkzYIfFmkvNmNqJcxXfVcuvI7WQGnetI9UzJ0LALxEueURP2MbydMuoBiRsTjRMpmBBhNRgOZBE9kNk0Mxp7M",
            "dp": "oGUilH02v5SD2KoICRhuoHZZSd3sCIYJ6XHNdA_La9v8xp-5ja7mmfcSXCxTAmo7hHfnpAowmb2KBZBE_oUZFb79UI--Ln6dFdu7RchD2jwtCdWdldNKsBGBAoiu-qM2j971E5y9JhqzCSZZ1Fv461p_p9t_jAv3q-vkbzPPg9E",
            "dq": "O298bUdSIwu5xDNa5naVEVNoVs1kSlwlb6tcKhxah30uiXtqs-4wliltk7WedQHCGx0Jr52B8Ls7pPj2DzPJaWZTNIL3vr5WOK2i_P_785qkf_k80anPu6pG74rLysB02AU0DfgSSU4q0_IDNSuAdNmfzqze4N_p-YATqjedEjk",
            "qi": "YXSa0y9QitZhYzeFPmLw5wX6S3t7A4V_HJs2ule2-8w7e7lckrOPxRndmKQ8Pql3pNsQjhqtgZKRo30qpsBng_fklZy4-JfdJIVRtVrHo3aP6sTFvGynib0GK6tk-cCQ37gUnDYnZEQfVdeB3EKbsiwFXxQf9zpE0u68h4pbn30"
        }
        """.trimIndent()

        val PRIVATE_KEY_PEM = """
        -----BEGIN RSA PRIVATE KEY-----
        MIIEpAIBAAKCAQEAwUL5dWmNwukaFhXfhVMLvP5ql46ClAyYB/UwzK+g5YnGF6V2
        Nflv9+40f0MpisZHRP9up6LXm2PEowouVIqL1FzTRUqXUCYt5uHrQsSgVf/EYun1
        reucHqlpOO4t7Y1a02hgNyoPFrAUeHs2LydJXqsutUnDdPXmQ2lIJHDmlkPv/DaP
        d9ffPha2Cs6OxhvZsIFu1fH9z4pgFUNPPrmtiSL1X5WM19YIPOoaKykU1KOpCOqI
        oOYdauQggQkP31CB2pxfObAozGmaELqhBvZBE85N94nbAJ2yfuHC+7Kpi5aO7Cql
        QR8uJH1dDUdUGGcp5tomVz/hxihLWf8QuHRKNwIDAQABAoIBAQCu93ircO20CZ/h
        YNoldWMKLvfH0HCtop09jyEoHtU3Gr2qCNX11acmOpVyphaFGi/J6st+n3I9r1yG
        EyCQz4H6uDNZ50cIfBhQkDaTEzrpwQ5GUvRnCgkkxVGLBi+0gqY5AaUiLETO0Ikn
        7rzGgfXt7GL0JBz91eYrH7T9sO1CGhh2bCr7JD1A0U3b2HSdfML1/F2pm7+bSJDP
        uac6jNaKV1Yx89MD2ZrDehQ99elcovZi2efu24pZ1LvuWa6qyMxWA545p9GObsjS
        QMSL5Pbhazx2jpZgHsu/CXzH1LpCORbtEimOXXWuwWjlfMQNIASSfbx5Pnj52xHz
        adFyUk95AoGBAPixvh03O0MzENH7NRlRwyYW+imqIQkMii8eWVV6aaYqQS9K+08B
        QvFUNnsDdZyChOXhZxtpPkLm5mlZoPoTxC2YLzCzjuaRL6HDruY4aKxMTwOsm/18
        W2b2lR0Ow1VEDyLMWumy0cTjAgi62g2/TJNRJv6W+aAuiYIXyt24nxFtAoGBAMbw
        XGvIjmMGZBkQxqcPXps6Os/ogZeDhwgd3DHmOa7UTh27ICenXHfKE7s3nxLHI+NU
        ULRrII8NFFjCEPJf0YW5JM2CHxZpLzZjaiXMV31XLryO1kBp3rSPVMydCwC8RLnl
        ET9jG8nTLqAYkbE40TKZgQYTUYDmQRPZDZNDMaezAoGBAKBlIpR9Nr+Ug9iqCAkY
        bqB2WUnd7AiGCelxzXQPy2vb/MafuY2u5pn3ElwsUwJqO4R356QKMJm9igWQRP6F
        GRW+/VCPvi5+nRXbu0XIQ9o8LQnVnZXTSrARgQKIrvqjNo/e9ROcvSYaswkmWdRb
        +Otaf6fbf4wL96vr5G8zz4PRAoGAO298bUdSIwu5xDNa5naVEVNoVs1kSlwlb6tc
        Khxah30uiXtqs+4wliltk7WedQHCGx0Jr52B8Ls7pPj2DzPJaWZTNIL3vr5WOK2i
        /P/785qkf/k80anPu6pG74rLysB02AU0DfgSSU4q0/IDNSuAdNmfzqze4N/p+YAT
        qjedEjkCgYBhdJrTL1CK1mFjN4U+YvDnBfpLe3sDhX8cmza6V7b7zDt7uVySs4/F
        Gd2YpDw+qXek2xCOGq2BkpGjfSqmwGeD9+SVnLj4l90khVG1Wsejdo/qxMW8bKeJ
        vQYrq2T5wJDfuBScNidkRB9V14HcQpuyLAVfFB/3OkTS7ryHiluffQ==
        -----END RSA PRIVATE KEY-----
        """.trimIndent()
    }
}