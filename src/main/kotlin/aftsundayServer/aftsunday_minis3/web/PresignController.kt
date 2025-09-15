package aftsundayServer.aftsunday_minis3.web

import aftsundayServer.aftsunday_minis3.config.AppProperties
import aftsundayServer.aftsunday_minis3.util.CryptoUtil
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/presign")
class PresignController(
    private val props: AppProperties
) {
    data class PresignReq(
        val bucket: String,
        val key: String,
        val ttlSeconds: Long? = null
    )
    data class PresignRes(
        val url: String,
        val expires: Long,
        val signature: String
    )

    @PostMapping("/get")
    fun presignGet(@RequestBody req: PresignReq): PresignRes {
        val exp = Instant.now().epochSecond + (req.ttlSeconds ?: props.presignDefaultTtlSeconds)
        val data = "GET\n/${req.bucket}/${req.key}\n$exp"
        val sig = CryptoUtil.hmacSha256Hex(props.presignSecret, data)
        val url = "/b/${req.bucket}/${req.key}?expires=$exp&signature=$sig"
        return PresignRes(url, exp, sig)
    }
}