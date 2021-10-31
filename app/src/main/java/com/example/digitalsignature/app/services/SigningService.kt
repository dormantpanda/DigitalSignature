package com.example.digitalsignature.app.services

import android.net.Uri
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder

import java.math.BigInteger
import java.security.*
import java.security.cert.X509Certificate
import java.security.spec.ECGenParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*
import java.util.concurrent.TimeUnit


class SigningService {
    companion object {
        private val bcProvider = BouncyCastleProvider()

        /*fun signTest(
            data: ByteArray,
            certificate: X509Certificate?,
            encodedPrivateKey: ByteArray
        ): ByteArray {

            val certificates: Array<X509Certificate?> = arrayOfNulls(1)
            certificates[0] = certificate

            //большой вопрос, а надо ли
            val privateKeySpec = PKCS8EncodedKeySpec(encodedPrivateKey)
            val keyFactory = KeyFactory.getInstance("ECGOST3410", bcProvider)
            val privateKey: PrivateKey = keyFactory.generatePrivate(privateKeySpec)

            val msg: CMSTypedData = CMSProcessableByteArray(data)
            val certStore: Store<*> = JcaCertStore(certificates.toList())
            val generator = CMSSignedDataGenerator()
            val signer = JcaContentSignerBuilder("GOST3411withECGOST3410")
                .setProvider(bcProvider)
                .build(privateKey)
            generator.addSignerInfoGenerator(
                JcaSignerInfoGeneratorBuilder(
                    JcaDigestCalculatorProviderBuilder().setProvider(bcProvider).build()
                ).build(signer, certificates[0])
            )
            generator.addCertificates(certStore)
            val sigData = generator.generate(msg, true)

            return sigData.encoded
        }*/

        fun generateKeys(): Pair<KeyPair, X509Certificate> {
            val keyPairGenerator = KeyPairGenerator.getInstance("ECGOST3410", bcProvider)
            keyPairGenerator.initialize(ECGenParameterSpec("GostR3410-2001-CryptoPro-A"))
            val keyPair = keyPairGenerator.generateKeyPair()

            val subject = X500Name("CN=Me")

            val serial: BigInteger = BigInteger.ONE // its fine

            val notBefore = Date()
            val notAfter = Date(notBefore.time + TimeUnit.DAYS.toMillis(365))

            val certificateBuilder = JcaX509v3CertificateBuilder(
                subject, serial,
                notBefore, notAfter,
                subject, keyPair.public
            )
            val certificateHolder = certificateBuilder.build(
                JcaContentSignerBuilder("GOST3411withECGOST3410")
                    .setProvider(bcProvider)
                    .build(keyPair.private)
            )
            val certificateConverter = JcaX509CertificateConverter()
            val certificate = certificateConverter.getCertificate(certificateHolder)

            return keyPair to certificate

            /*val keyStore: KeyStore = KeyStore.getInstance("JKS")
        keyStore.load(null, null) // initialize new keystore

        keyStore.setEntry(
            "alias",
            KeyStore.PrivateKeyEntry(
                keyPair.private, arrayOf(certificate)
            ),
            KeyStore.PasswordProtection("entryPassword".toCharArray())
        )
        keyStore.store(FileOutputStream("test.jks"), "keystorePassword".toCharArray())*/
        }

        /*fun checkTest(publicKey: String, hash: String, sign: String): Boolean {
            val x = BigInteger(publicKey.substring(0, 64), 16)
            val y = BigInteger(publicKey.substring(64), 16)
            val r = BigInteger(sign.substring(0, 64), 16)
            val s = BigInteger(sign.substring(64), 16)

            val parameters =
                ECGOST3410NamedCurves.getByOID(CryptoProObjectIdentifiers.gostR3410_2001_CryptoPro_A)
            val curve = parameters.curve

            val spec = ECParameterSpec(curve, parameters.g, parameters.n)
            val pubKey = ECPublicKeySpec(curve.createPoint(x, y), spec)
            val n = parameters.n
            val aprE = BigInteger(hash, 16).mod(n)
            val e = if (aprE.compareTo(ECConstants.ZERO) == -1) aprE.add(n) else aprE

            if (r.compareTo(ECConstants.ONE) < 0 || r.compareTo(n) >= 0) {
                return false
            }

            if (s.compareTo(ECConstants.ONE) < 0 || s.compareTo(n) >= 0) {
                return false
            }

            val v = e.modInverse(n)
            val z1 = s.multiply(v).mod(n)
            val z2 = n.subtract(r).multiply(v).mod(n)
            val G: ECPoint = parameters.g
            val Q: ECPoint = pubKey.q
            val point: ECPoint = ECAlgorithms.sumOfTwoMultiplies(G, z1, Q, z2)

            if (point.isInfinity) {
                return false
            }
            val R: BigInteger = point.xCoord.toBigInteger().mod(n)

            return (r == R)
        }*/

        /*fun checkTest2(data: ByteArray, signature: ByteArray): Boolean {
            val signedContent: CMSProcessable = CMSProcessableByteArray(data)
            val signedData: CMSSignedData
            try {
                signedData = CMSSignedData(signedContent, signature)
            } catch (e: CMSException) {
                return false
            }

            val signer: SignerInformation
            try {
                val certStoreInSing = signedData.certificates
                signer = signedData.signerInfos.signers.iterator().next()
                val certCollection: Collection<*> = certStoreInSing.getMatches(signer.sid as Selector<X509CertificateHolder>)
                val certIt = certCollection.iterator()
                val certHolder = certIt.next() as X509CertificateHolder
                val certificate = JcaX509CertificateConverter().getCertificate(certHolder)
                return signer.verify(JcaSimpleSignerInfoVerifierBuilder().build(certificate))
            } catch (ex: Exception) {
                return false
            }
        }*/
    }
}