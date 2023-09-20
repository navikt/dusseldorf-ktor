package no.nav.helse.dusseldorf.testsupport.jws

import org.apache.hc.client5.http.utils.Hex
import java.security.MessageDigest
import java.security.cert.CertificateFactory

object ClientCredentials {
    private fun asHexThumbprint(certificatePem : String) : String {
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val certificate = certificatePem.byteInputStream().use {
            certificateFactory.generateCertificate(it)
        }
        val messageDigest = MessageDigest.getInstance("SHA-1")
        messageDigest.update(certificate.encoded)
        return Hex.encodeHexString(messageDigest.digest())
    }

    object ClientA {
        val privateKeyJwk = """
        {
            "kid": "MGBHHGXl7ITAVj-fHgBkPBQ-T5A=",
            "x5t": "MGBHHGXl7ITAVj-fHgBkPBQ-T5A=",
            "kty": "RSA",
            "n": "vTs0mS-huLVOv7_EaaIHoqkM3Rz1TOImAEVdQK6PZqqQLnbRC5yszxuqOOFPvw8QFY3HT2iUrkxlVPkW3Z9LAXS3dmZKw0MJboLHvusdmLFn0FhIgbldRyAxJ4UcepLJdcR4xofW_MgIH34xkjEDY-dSeDB4fiKi1_8lPTJYuVP5vAywfV3Z_R7msK6rlvl0g28SsOZrxJ9OC6nH3cVsT75vZcmd2eip7LLGCkO8-V9qGgAYUjocn7x6-0XlPVilCF8ic6PNClwe4bmjDR2a_SbDSc3akE8vxaMtINt49CcPfUhkPPm_0mfWsayCXzuwBfUeTaXF_ABCxkipYYpu6w",
            "e": "AQAB",
            "d": "p-V0Eca1UtFrga6AcskUxToA897RttmgpfTlfJJlIc6MBu3dJNRqb4g4TCd9PiP7PWSCRu6fnNajwfUQWKsRPcV1UlQIWZ-NKsRWvgqWQ_iEB9OM4ay6GnVxp4LvdcHvhdJA5sV39uj0bBznlrJuM6H3BjTbc-7_VW5IeDfHiQZ2pWW4DSbiwBhIUYu13IspZcsyk-fLfU-asS_lpz-Mc7XdP56xmstc9D22rOhBCz2NWpamM2UaqRS1zdn1V0wULjfO-tRMGghef_LaAuEEeOhhd-rw_wS89MfPdAoHvJWEyBVgQAS9LqxwRfrcjIu5pEf51Q0VrlV7dij8K1-KQQ",
            "p": "3auUKk-tkDtyQFr8bW_aA74hkVIBuRbnWe5F-r3O66Vy40tWHhJnGCyncCWb5f_k146ZwxwNlooheJS6T3bi4hsI5bbj3ElE_jSR_7SBVEab4bHbGkYpER4AUCHxfZwD8PJEoLZ4f14U87cBYL1GNEyZnLUitSjCDDmfJfI5LBM",
            "q": "2omKIMQetGibKTS60lMAxcVn4kQiCX3_spnrBIxLuEgXGYrOAqbsRVdSc07wKAljE-ig2SN1EaLKHyWVBxdxd4KMZqxbmh6HNuYru412ilPwZM5csLasU9TAD1yYCtju1Bj2GMU6awnc3hKoTXeWZWBrK4eubX4nb2WZxvfwXMk",
            "dp": "Eh6NTOwYZtrFGweU7Kkg6_9lpQhMBcIehRZZ-AX93Ps4KeYlku20KaC0yxD37lP9c7U_Ulh_r9d4pu-ZTxeLsim9j3FkrMP8dL79VCaAD9B5u3gbTcmAX9rQ8bvkjnzrQY28GFrx_I9HLSi_XxX5oBrGz61qud4sBm3LWYG0NKs",
            "dq": "CxnLc2ii6qUZpJkyGDbxJhql8T9mvzawQ2FAJ-X8fqriyYBcgJP8EnWiEYtj9ZSsfLlnWkBL1Q6A194v2MFfGSP_f8Onj4eXdLlyZT-FUvd6kZRN7wgIbuWyr9UTQBHO5-UwswdptUA2AO3PsMevUwz3xKlKufMbi7QMgKfdhMk",
            "qi": "vTdHKVXjTPkhe5IIzv-YxhANMIsBZVT4OFF2a0eZr06anwM-tEJCkCJTjlkQmjBqKtjbYaubTXAnX6_uTRpfNVmhhpws_hRsN6fmGBdQXwe7wSlzudrctpv-02ABRnT0EGBi9r9LSRATzh22uGwoan2xfChuUKZhy4uZqxcicbI"
        }
        """.trimIndent()

        val certificatePem = """
        -----BEGIN CERTIFICATE-----
        MIIDnjCCAoYCCQCh9N36BTiQozANBgkqhkiG9w0BAQsFADCBkDELMAkGA1UEBhMC
        Tk8xDTALBgNVBAgMBE9zbG8xDTALBgNVBAcMBE9zbG8xLzAtBgNVBAoMJk5BViAo
        QXJiZWlkcy0gb2cgdmVsZmVyZHNkaXJla3RvcmF0ZXQpMQ8wDQYDVQQLDAZOQVYg
        SVQxITAfBgNVBAMMGENsaWVudEEudW5pdC10ZXN0Lm5hdi5ubzAeFw0xOTA4MDEx
        MDIxMTZaFw0yMTA3MzExMDIxMTZaMIGQMQswCQYDVQQGEwJOTzENMAsGA1UECAwE
        T3NsbzENMAsGA1UEBwwET3NsbzEvMC0GA1UECgwmTkFWIChBcmJlaWRzLSBvZyB2
        ZWxmZXJkc2RpcmVrdG9yYXRldCkxDzANBgNVBAsMBk5BViBJVDEhMB8GA1UEAwwY
        Q2xpZW50QS51bml0LXRlc3QubmF2Lm5vMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A
        MIIBCgKCAQEAvTs0mS+huLVOv7/EaaIHoqkM3Rz1TOImAEVdQK6PZqqQLnbRC5ys
        zxuqOOFPvw8QFY3HT2iUrkxlVPkW3Z9LAXS3dmZKw0MJboLHvusdmLFn0FhIgbld
        RyAxJ4UcepLJdcR4xofW/MgIH34xkjEDY+dSeDB4fiKi1/8lPTJYuVP5vAywfV3Z
        /R7msK6rlvl0g28SsOZrxJ9OC6nH3cVsT75vZcmd2eip7LLGCkO8+V9qGgAYUjoc
        n7x6+0XlPVilCF8ic6PNClwe4bmjDR2a/SbDSc3akE8vxaMtINt49CcPfUhkPPm/
        0mfWsayCXzuwBfUeTaXF/ABCxkipYYpu6wIDAQABMA0GCSqGSIb3DQEBCwUAA4IB
        AQCWbTHF1nGfBb+9+8ZGFYSa00AwwvdzArZuFxq1IizZUpXr/ldWdHMI5Gpn+NyL
        DbkDFWTreoCRcc1UZN46yFKxNcIU8LoWe93JG6gE5Vv1sENQ1ikFLC1vtW4msli2
        Ucci3Db1xTtfdEZSfjjxtf7lIqTXxrNns2c//md0P/JFKACL5JF9iQBaXjwG/0bd
        aXCne+Bxcezh5bjcMegqDqqjAwb5/gyl8wnKowezMMcfl4HbHLLHK6bZaPN78ofl
        7mZsAoo2OlzqFUvk3XmKCmbjhxq/ZFMFz8b4upgHiCC10iv4f42bvHDsUxKux4RT
        vvEC76nH2z8IIKEr8p//znbw
        -----END CERTIFICATE-----
        """.trimIndent()

        val certificateHexThumbprint = asHexThumbprint(certificatePem)
    }
    object ClientB {
        val privateKeyJwk = """
        {
            "kid": "IIx3o-0TZaznLLk0wOCkcB_be78=",
            "x5t": "IIx3o-0TZaznLLk0wOCkcB_be78=",
            "kty": "RSA",
            "n": "0CUjU__2-sJHzVfdO1JDbTsCJnvh2ITTF6hp6LHAUsldVxkOjiBmINr_PFJuSS3J4nRq3YEjkGFXBhpE6yrRGIlfNNBlcOi7DB6L7FBdG9C1_IlDPoMwH_ddTd1Ki8IkeMbGH9xjMy9eoylIRPS7fbFguKIZ-FbGSPiyGGCwVbgN-x2-TrkCuuYexuVRe_sVqP0XtmVpMaHsgVvW1dJZocY0dbEvj2yQWpry0gHEgMZFNWYgpALPiIO8zZg7-yb0AmeD6tNWjR8fMvsxkXX85UqyHAa-XbzyxVjLvumaerFj27BTgDJ_k6LMRYPoCjQDtKnNA_0P-EIe-gWVqA3vYQ",
            "e": "AQAB",
            "d": "O0LHmyveQqi7zq_8cd5LCjdptlFBDOdMPokZlkG9cxSXuauNpFN_IE2Zq7AAbF9YAlyI7IXO_VVpdHhYNOhc3fKGo7_NkecBEkNELUT4LWov1jWyaoLpWcCQ1RFzHvVocuaNFBcoOzN6a1PemptfIjs1QXwNjtIN5ErCUgR9T_nytFlEl1GoxA7DIkPNKf3TCWz9NQHU5NGY4Orn6ZWsEu1uXxebUiOPr4LBm8AUTlU4ZaNvqQF4NGIgGDSOiLpCA6m1G5qqVr4GKmoozc_zsP0LKp7cJCT7yJndTJ4vb80t8OwuKRzDi8nL2YQuGySft9j-Hm4Sq3jS6Sa-90P4zQ",
            "p": "6fFccl4VHI_BmNeM5BcMuDxj0P8BmcAAN8G5KyufGBoMzs58_wvBJY1KeILqvh3OPDWudA_W8v6hWAB6vBLlun9DzePaGxzUAQQiosVoppcrqkPALwcJr5SHPB8YHr_Exk4TLxBUsjB8KK2nTezrPzHoqudrbIAc1d-ZbAKqEy8",
            "q": "48UZ2IigRp-alJnmDVuPgUjfKCX_1anudZ28iPJb5ik25x7wGwhNO3sjw05B3xnOms1sK1hEtJMSebu5INaIEGwUgzRSLeC1oH0gmR1mnpUgHMW2U15sP8e3EblDGORdG81v7x4KtlgPf52DkJ2jR6OopMY6EBwiEeGA1kUawm8",
            "dp": "axMZqQ7-wq_ZgEdIKS2TmM-rMAFHjkOe1eZJBkVU8AccTZudAm8y3CkrKLVjE9k4h75aNqz5SQbaehjeFC7iDtYpBpd_QGGVd1GZOL76AyW1ypxv3hZujHqC2zWwKILFYa5igWwMF_KxIT06EiSaF7aUM4TwWhzZFdrdsd4WUek",
            "dq": "m76x7qN13PTT0ZrueOHL3d6DYUOijWp2OQyT15zJhDdYaG96RTHHBc4s5SaE2lYeNTIULgtcZ5FNh9n6O98WCQyudgz3yJEoAMau3KQDAJr9606yXo1pPa-FmePxYnr8w78VNA95yberWVrOlLuF472f2pQFLfDexxVIcLuuEZc",
            "qi": "kyPhFHrzIhXkiJGKaiqxJNzC8mbUO6XXbLAhib18J-fTmcnJOVF8u6b2mg1ZdfhghbELiqzE-kMSDAxWjE0wOni-u72ACPz-w22o9FgNsxfXpcmWeG0B2VHmUffgixfLjciSINhD3u-de8d4reAvXJs2xM0b7QBUtnaWcl0xibc"
        }
        """.trimIndent()

        val certificatePem = """
        -----BEGIN CERTIFICATE-----
        MIIDnjCCAoYCCQD8/AISRp6l2jANBgkqhkiG9w0BAQsFADCBkDELMAkGA1UEBhMC
        Tk8xDTALBgNVBAgMBE9zbG8xDTALBgNVBAcMBE9zbG8xLzAtBgNVBAoMJk5BViAo
        QXJiZWlkcy0gb2cgdmVsZmVyZHNkaXJla3RvcmF0ZXQpMQ8wDQYDVQQLDAZOQVYg
        SVQxITAfBgNVBAMMGENsaWVudEIudW5pdC10ZXN0Lm5hdi5ubzAeFw0xOTA4MDEx
        MDIxMjBaFw0yMTA3MzExMDIxMjBaMIGQMQswCQYDVQQGEwJOTzENMAsGA1UECAwE
        T3NsbzENMAsGA1UEBwwET3NsbzEvMC0GA1UECgwmTkFWIChBcmJlaWRzLSBvZyB2
        ZWxmZXJkc2RpcmVrdG9yYXRldCkxDzANBgNVBAsMBk5BViBJVDEhMB8GA1UEAwwY
        Q2xpZW50Qi51bml0LXRlc3QubmF2Lm5vMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A
        MIIBCgKCAQEA0CUjU//2+sJHzVfdO1JDbTsCJnvh2ITTF6hp6LHAUsldVxkOjiBm
        INr/PFJuSS3J4nRq3YEjkGFXBhpE6yrRGIlfNNBlcOi7DB6L7FBdG9C1/IlDPoMw
        H/ddTd1Ki8IkeMbGH9xjMy9eoylIRPS7fbFguKIZ+FbGSPiyGGCwVbgN+x2+TrkC
        uuYexuVRe/sVqP0XtmVpMaHsgVvW1dJZocY0dbEvj2yQWpry0gHEgMZFNWYgpALP
        iIO8zZg7+yb0AmeD6tNWjR8fMvsxkXX85UqyHAa+XbzyxVjLvumaerFj27BTgDJ/
        k6LMRYPoCjQDtKnNA/0P+EIe+gWVqA3vYQIDAQABMA0GCSqGSIb3DQEBCwUAA4IB
        AQAZgLPw9f3YWsST4OlstckRvAxmcQelfryQ82TaykUENf3CYat0sQ0vRap4g/+I
        JDYmY0VeNfFmUd/Nzi5tHzZwz3wpMX/Pse6MFRgsUa9jQIq2+YCAuevmiiBPStJl
        ZZ2CR8Ks6s3vG0x3HOYPSyPYnb0wmodprq4ODMJxeQM+QqsDIuKkovRVPDjVQ0eu
        8njlmZAB/bG1ngTODSuuwlKlub+jRyaIyqLhwEgcqr2HjfrjVjvxo3eWEYDoEEB2
        hdBEJHh19mOmTCUf4JLwvcNVVlpqOzIb+A4fG+hLH6Oflx50Rsb+jdVAicdn7ZYX
        r7A7Q6AGosZP2kAEtulbvuj1
        -----END CERTIFICATE-----
        """.trimIndent()

        val certificateHexThumbprint = asHexThumbprint(certificatePem)

    }
    object ClientC {
        val privateKeyJwk = """
        {
            "kid": "bAa3TOJkF6sInF_jX50kbv6DwYQ=",
            "x5t": "bAa3TOJkF6sInF_jX50kbv6DwYQ=",
            "kty": "RSA",
            "n": "yYBQnAbJNZnY6374Y-FmRmyM8JGsrSGIJV7dTCs90E5oQqjlBZdOBUCb0BOi7vcgRy6gjkrR6PA1jp6G__Obk5bBxrJdmHZVzNMRaj7znCA8Qo1uYhk3b_L5MhnWFnDGvcyWQCksvnp_FtI0e7KpQ5-6bfahDtf5Ov0ZconmGvUR9eH-2yvT4OAUy3fpEDXyU1-0ait8NMa8QtzuKsH4p79eVx5lB1_feCoWvCU4FplzrduYdrF61cniAxwvtCd0HrT-yeCIZAd8TPKLZBNUb_n3wCN-UpiD8aJA7V7DlzdtdawyisUjjBztjsoE7HXnHPtaOdYq9hXD9tIRslVPfQ",
            "e": "AQAB",
            "d": "SFuplr3hFzxt5vMrY0KXpYZpfiHxriIyb6kHMsj8Y4ckbXuU8lMJE13GFgxXIBTiJG2b_jzj8BACS3ql49eo8HDcmOuILK2h1fGdeVetx0zUFtoO329Ktwq7bcBVXq2U0Kdc9SFUDfqTFLDbY-geXEhJfB9qapgnyYUF1FmspdTkQKcAds14LbMHYZTt8AVe1PsJrI54Zd7H9yAmxl3um-tFyhn61lLMG1vaYUzpjYhfprnaCShX1_BS9q6X20wfVktObsISEAeRQl1NC1vetsC-3XquswfkMqCOGaUDzRGkD6KsRbHVn706oaD1yF1__HYE7XwvyEJdA-XumjvlQQ",
            "p": "5rGSibH5JRLZhXwmEGo7b6ITYCrptt5xR6nF2vs75AdSwFmmSVVTMGnqMWn5w-rfXZ1J5LOsaFob4iSpFqyHeDW_zC71krzbu4NDmNaYLemVB45wNuliNepLT4Z1lWKTRjMYkohpJ_dkfwDR7bCJ_YVUzN9pH0puDTaUyS02a4U",
            "q": "35rzLem3JJl46waFhXzNbF4KUKPanc2mvJxPCCZbXQgMWeEwbzv8KqBtTYpOqsXeA4nieRjoTNnLE74lqcWj0_Cska1W5xjp_j-jE0Qh6FcaaD1ufDyxhBubmKLmr0KHCCg-hHvzebm3u0yvPLRvOfVlZnsoADNM-n7aYEwX6Zk",
            "dp": "TkvoY0l_OOD76L0pIhscDxhgIRyPdBxtMBhj6Z2pRwxhPp1uvaby9BSOgrToK2F07A3tPWzp0AT9P7SXmvV1iQwdC4zIlkP5A9xaPe5fef20MftbBJgTWB8r7lLBZ7EsMQA4j8syfXkOp7g8h8KKIvx0uo_82rzlOyDSQLFSrg0",
            "dq": "cjoOtkqpY0n_aliXhvydWBmmPVGnEWwIHFvhR7GIhLrizFtsSbaj-AaiL7VSwIx6AObEScZ-jRvJURmW1eZ_7XoKPhUFyuKAToClxubMR4uPkuam9VJwAKO11cCKBcf_gRxF65oCZBGKmWTwNBucokgI99Q8N_X9Ag4hcoYMxSk",
            "qi": "4_cYyrT-YLu5iUJGAmrr1OdAatXwqTk2BNmGOOYA2_p9z3GSy0uEhbQTYvSaMvzqoObbYZN_9evTzl0DCyOCWxNZU1JuLjwBz0xdxnBkcNTVgXqe8JEE1mNAuOg013Tzd59bqZqHRnRAdKAOPixMsn6LkfBBCG-akRAuohsxmqk"
        }
        """.trimIndent()

        val certificatePem = """
        -----BEGIN CERTIFICATE-----
        MIIDnjCCAoYCCQCk9EASozl/9jANBgkqhkiG9w0BAQsFADCBkDELMAkGA1UEBhMC
        Tk8xDTALBgNVBAgMBE9zbG8xDTALBgNVBAcMBE9zbG8xLzAtBgNVBAoMJk5BViAo
        QXJiZWlkcy0gb2cgdmVsZmVyZHNkaXJla3RvcmF0ZXQpMQ8wDQYDVQQLDAZOQVYg
        SVQxITAfBgNVBAMMGENsaWVudEMudW5pdC10ZXN0Lm5hdi5ubzAeFw0xOTA4MDEx
        MDIxMjNaFw0yMTA3MzExMDIxMjNaMIGQMQswCQYDVQQGEwJOTzENMAsGA1UECAwE
        T3NsbzENMAsGA1UEBwwET3NsbzEvMC0GA1UECgwmTkFWIChBcmJlaWRzLSBvZyB2
        ZWxmZXJkc2RpcmVrdG9yYXRldCkxDzANBgNVBAsMBk5BViBJVDEhMB8GA1UEAwwY
        Q2xpZW50Qy51bml0LXRlc3QubmF2Lm5vMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A
        MIIBCgKCAQEAyYBQnAbJNZnY6374Y+FmRmyM8JGsrSGIJV7dTCs90E5oQqjlBZdO
        BUCb0BOi7vcgRy6gjkrR6PA1jp6G//Obk5bBxrJdmHZVzNMRaj7znCA8Qo1uYhk3
        b/L5MhnWFnDGvcyWQCksvnp/FtI0e7KpQ5+6bfahDtf5Ov0ZconmGvUR9eH+2yvT
        4OAUy3fpEDXyU1+0ait8NMa8QtzuKsH4p79eVx5lB1/feCoWvCU4FplzrduYdrF6
        1cniAxwvtCd0HrT+yeCIZAd8TPKLZBNUb/n3wCN+UpiD8aJA7V7DlzdtdawyisUj
        jBztjsoE7HXnHPtaOdYq9hXD9tIRslVPfQIDAQABMA0GCSqGSIb3DQEBCwUAA4IB
        AQBAPrW9LPLi/y4HeQAIRJpfCW0DbMbvXGsUxgzSoORF3XDF/5TzDk4vrein+meD
        9p9tf7ADhKchZDKoXYj3Ekrf0iWo6G0sxZ19jH/KgL9e+LBD8TdwVMqR+DGMWJxU
        6wxbwZyW0Rqx9sGeYSgbWyojurjGrPEbCZga5MiGdUcISwOkekOtw3cemEjDr3ef
        fky0olPktxex8vZVm2l0sc2uoi3V+LN16QIifgaD57WZNrqvRrX2vJKjrnSpzRHv
        o4g7oBK25cf1qzM9qgq/e13DZ9BSt/Rc9CN2h4jbBZBscz/Lb5pBfFlwL0AbBLlC
        AZ6jmtmexwQdLlu4KvTlRfPe
        -----END CERTIFICATE-----
        """.trimIndent()

        val certificateHexThumbprint = asHexThumbprint(certificatePem)
    }
}