package no.nav.helse.dusseldorf.oauth2.client

import java.net.URL

class TestData {
    companion object {
        val AZURE_PREPROD_TOKEN_URL = URL("https://login.microsoftonline.com/966ac572-f5b7-4bbe-aa88-c76419c0f851/oauth2/v2.0/token")

        const val CERTIFICATE_THUMBPRINT_SHA1_HEX = "D254A26835EA39102965CBFD58FC03897DFF127F"
        const val CERTIFICATE_THUMBPRINT_SHA1_HEX_BASE64 = "0lSiaDXqORApZcv9WPwDiX3_En8="

        val CERTIFICATE_PEM = """
        -----BEGIN CERTIFICATE-----
        MIIC9jCCAd4CCQDGYtVVzzqpxTANBgkqhkiG9w0BAQsFADA9MQswCQYDVQQGEwJO
        TzEMMAoGA1UECgwDTkFWMSAwHgYDVQQDDBdkdXNzZWxkb3JmLXRlc3RzLm5hdi5u
        bzAeFw0xOTA0MTMxMDQ3MDdaFw0yMTA0MTIxMDQ3MDdaMD0xCzAJBgNVBAYTAk5P
        MQwwCgYDVQQKDANOQVYxIDAeBgNVBAMMF2R1c3NlbGRvcmYtdGVzdHMubmF2Lm5v
        MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApBjxqwjKaM4YG3Kf9dRu
        gVjWGFLT6w4tdHRaQoipaTzl/891DMx6ccuUMMbjTxdxevsDbYOB0fjcKHHXQ9JW
        5yVaBxl2hk7FIre3uDeLOqNbbpr7mekwGzqz4YGAkTpNjDoljxS+5v3Dxo5Zr85F
        FXpdoed4Vs37p3U7FAlc91sZ0TJ0BV1q5k+kkG6UmsEsdp1qZxNsQ/5K1nWxREDx
        aBUOiIfDfPiHmmRXHEEEKY/AQ00+i97SC4vMu4cW9tCKxiBpKh743qF+GkctCePo
        l5PCjFpy56PFC4PnZjRFgnn80kdSbFOwH8l07unzOiUzKlWkv0b5WXw8h3ydZFFe
        xwIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQBiRL7sc/aecxMQuOSQnZbxzQ3spw62
        uFGdoEFwAPGNkmQblLz7y8Nvyl2yvUyhOj7fTQl6AFU+BRodNFWMyDMhmiu5jY2P
        XQuzIsqJFGRlub7N/8nkrGwKTK9SpVx8BQbqbf9Vc2AldZKR7N02olqApRJOKU0H
        IlycMTG7DcF9vrAjfZMBnFmgT01NeSPPyRQHFmBacRADMQzJ6g+GOO5ccyzJIKDo
        qwzP7QlRZpbLusAgaejUETpcmxNXKQvXn8xpmPUajYh3TqQs+Koxx+Nuousn57Mn
        kz4xaKf+7RmLroqgTHbw+qTovJE39T12V8XfHBmM/kXIrVDy664hODvn
        -----END CERTIFICATE-----
        """.trimIndent()

        val PRIVATE_KEY_JWK = """
        {
            "kty": "RSA",
            "n": "pBjxqwjKaM4YG3Kf9dRugVjWGFLT6w4tdHRaQoipaTzl_891DMx6ccuUMMbjTxdxevsDbYOB0fjcKHHXQ9JW5yVaBxl2hk7FIre3uDeLOqNbbpr7mekwGzqz4YGAkTpNjDoljxS-5v3Dxo5Zr85FFXpdoed4Vs37p3U7FAlc91sZ0TJ0BV1q5k-kkG6UmsEsdp1qZxNsQ_5K1nWxREDxaBUOiIfDfPiHmmRXHEEEKY_AQ00-i97SC4vMu4cW9tCKxiBpKh743qF-GkctCePol5PCjFpy56PFC4PnZjRFgnn80kdSbFOwH8l07unzOiUzKlWkv0b5WXw8h3ydZFFexw",
            "e": "AQAB",
            "d": "gRm-x7iaxemevblob5c5eTnS9j_zybHVwRDpEf9CiTEIIkGs7OzSSETJybYvj0H6Xa6t-7LCp9cKHieyHAGXrTKNqZg2z2OZZL71I1FPkEqE3HfCCkyTNFjyvC-OXrNn3zK_6dmAd2qeY9AKb23wm_0xPPdGjcRwgEaSvCjBozgd8dKgrn8bnALb1V1mGPZt5X648723uW6zBqO94ue73gqp7WrE2AMTG4SaiX-CzO4dSzLI6AUZGnBfF6umyxrZBFR6g2m1zATBa5i0YXrIHXM3RREnFNmcOrcNO3borzNtZCiMW7ZrXIqO8AVnDjNmVzbg5v3f9Ol1U6t1TT0XAQ",
            "p": "2f9NC_bpRpCvZwsZpALy2HQTYkX0b2P6N5zgQkm17PKiukc73AwHbk9YJYzPjTJY0IpRl2pivSTMOTuTuRU3sDxC_yMvuyJ3gI8rEtux_SDf2G2_OfGRtDgpUNuqzMveaoMpnCfxkO4JO5RmuvX99OW7_8wpZqQh6OQLttWJU5E",
            "q": "wLQ4zrqas0YaTYb0SDmbDexP98CRDGj7E3n6JIx9HwsAm98BDxu9w59gSsCvzg0YnlQNYzvy10v9NxHKnvMvahfg_qmcD1o9YwcvzcljUw3dAIVIQ9rHbNZG4wfgAJt1QgkZQrz2KmGBaIdqR57IpUpfnJ5v4S7FpBZPNm7BMNc",
            "dp": "yUTpgc5p-njDOUQKXF9Mj4Q8EVO9JssLziTM-ObNTQOIMqxqG_QPOE2ReLnVNuvxDDlos3_JwhAjbgQPk6Z_T_uTb7Sw8PoVk2CbyEGGx8p-YXiSQZFDkTz5CGqH-6WOqJCI7mACrGjZpWSSpLNR0bX6KWX6I4YOuNMz7Y6hx8E",
            "dq": "I9ulUnqQvNlHnbOGE0Z83sthWgXAN-H1Dnu9Gz31LmiatWZ6yPftiNBIV8ChNiNjuFqFnziRiJSAStYJsSgpY4GMAXdILecp0xqMP6vAyryiqi0i9FVqlIsO58IYYaSL3jzZMXz-BYbdULkaAre-OFutjPRCd1F_v3fTR5q2YkM",
            "qi": "JcR435ZvHWYJTj9oJedcuJjj4LRhL7bZdXU0PxwEwmMYMoF5Sc64dRs5chats1tVM35fjmEhAthxeQv19x_SV5NB0hX8sE_jqIfAKx5nsZbv-3-FQaI68GKlpxdsGqaK0CqAJYAkGtEUeeWi6HAV8Xl68GSzBYMYDtOM8PPJWO4"
        }
        """.trimIndent()

        val PRIVATE_KEY_PEM = """
        -----BEGIN RSA PRIVATE KEY-----
        MIIEpAIBAAKCAQEApBjxqwjKaM4YG3Kf9dRugVjWGFLT6w4tdHRaQoipaTzl/891
        DMx6ccuUMMbjTxdxevsDbYOB0fjcKHHXQ9JW5yVaBxl2hk7FIre3uDeLOqNbbpr7
        mekwGzqz4YGAkTpNjDoljxS+5v3Dxo5Zr85FFXpdoed4Vs37p3U7FAlc91sZ0TJ0
        BV1q5k+kkG6UmsEsdp1qZxNsQ/5K1nWxREDxaBUOiIfDfPiHmmRXHEEEKY/AQ00+
        i97SC4vMu4cW9tCKxiBpKh743qF+GkctCePol5PCjFpy56PFC4PnZjRFgnn80kdS
        bFOwH8l07unzOiUzKlWkv0b5WXw8h3ydZFFexwIDAQABAoIBAQCBGb7HuJrF6Z69
        uWhvlzl5OdL2P/PJsdXBEOkR/0KJMQgiQazs7NJIRMnJti+PQfpdrq37ssKn1woe
        J7IcAZetMo2pmDbPY5lkvvUjUU+QSoTcd8IKTJM0WPK8L45es2ffMr/p2YB3ap5j
        0ApvbfCb/TE890aNxHCARpK8KMGjOB3x0qCufxucAtvVXWYY9m3lfrjzvbe5brMG
        o73i57veCqntasTYAxMbhJqJf4LM7h1LMsjoBRkacF8Xq6bLGtkEVHqDabXMBMFr
        mLRhesgdczdFEScU2Zw6tw07duivM21kKIxbtmtcio7wBWcOM2ZXNuDm/d/06XVT
        q3VNPRcBAoGBANn/TQv26UaQr2cLGaQC8th0E2JF9G9j+jec4EJJtezyorpHO9wM
        B25PWCWMz40yWNCKUZdqYr0kzDk7k7kVN7A8Qv8jL7sid4CPKxLbsf0g39htvznx
        kbQ4KVDbqszL3mqDKZwn8ZDuCTuUZrr1/fTlu//MKWakIejkC7bViVORAoGBAMC0
        OM66mrNGGk2G9Eg5mw3sT/fAkQxo+xN5+iSMfR8LAJvfAQ8bvcOfYErAr84NGJ5U
        DWM78tdL/TcRyp7zL2oX4P6pnA9aPWMHL83JY1MN3QCFSEPax2zWRuMH4ACbdUIJ
        GUK89iphgWiHakeeyKVKX5yeb+EuxaQWTzZuwTDXAoGBAMlE6YHOafp4wzlEClxf
        TI+EPBFTvSbLC84kzPjmzU0DiDKsahv0DzhNkXi51Tbr8Qw5aLN/ycIQI24ED5Om
        f0/7k2+0sPD6FZNgm8hBhsfKfmF4kkGRQ5E8+Qhqh/uljqiQiO5gAqxo2aVkkqSz
        UdG1+ill+iOGDrjTM+2OocfBAoGAI9ulUnqQvNlHnbOGE0Z83sthWgXAN+H1Dnu9
        Gz31LmiatWZ6yPftiNBIV8ChNiNjuFqFnziRiJSAStYJsSgpY4GMAXdILecp0xqM
        P6vAyryiqi0i9FVqlIsO58IYYaSL3jzZMXz+BYbdULkaAre+OFutjPRCd1F/v3fT
        R5q2YkMCgYAlxHjflm8dZglOP2gl51y4mOPgtGEvttl1dTQ/HATCYxgygXlJzrh1
        GzlyFq2zW1Uzfl+OYSEC2HF5C/X3H9JXk0HSFfywT+Ooh8ArHmexlu/7f4VBojrw
        YqWnF2waporQKoAlgCQa0RR55aLocBXxeXrwZLMFgxgO04zw88lY7g==
        -----END RSA PRIVATE KEY-----
        """.trimIndent()
    }
}