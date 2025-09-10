package aftsundayServer.aftsunday_minis3.util

import org.apache.commons.codec.binary.Hex  // implementation("commons-codec:commons-codec:1.16.0") build.gradle.kts 에 필요
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object CryptoUtil {
    fun hmacSha256Hex(secret : String, data: String) : String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
        val bytes = mac.doFinal(data.toByteArray())
        return Hex.encodeHexString(bytes)
    }
}