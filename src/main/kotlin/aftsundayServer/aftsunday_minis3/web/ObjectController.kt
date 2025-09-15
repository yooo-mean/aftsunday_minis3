package aftsundayServer.aftsunday_minis3.web

import aftsundayServer.aftsunday_minis3.config.AppProperties
import aftsundayServer.aftsunday_minis3.storage.StorageService
import aftsundayServer.aftsunday_minis3.util.CryptoUtil
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant

@RestController
@RequestMapping("/b")
class ObjectController(
    private val storage: StorageService,
    private val props: AppProperties
) {

    @GetMapping("/{bucket}")
    fun listObjects(
        @PathVariable bucket: String,
        @RequestParam(required = false) prefix: String?
    ) = storage.listObjects(bucket, prefix)

    @PostMapping("/{bucket}/{**key}")
    fun putObject(
        @PathVariable bucket: String,
        @PathVariable key: String,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<Unit> {
        if (!storage.existsBucket(bucket)) throw NoSuchElementException("bucket not found")
        storage.putObject(bucket, key, file)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/{bucket}/{**key}")
    fun getObject(
        @PathVariable bucket: String,
        @PathVariable key: String,
        @RequestParam(required = false) expires: Long?,
        @RequestParam(required = false) signature: String?,
        res: HttpServletResponse
    ) {
        // 프리사인 검증 (있다면)
        if (expires != null || signature != null) {
            if (expires == null || signature == null) throw NoSuchElementException("missing presign params")
            val now = Instant.now().epochSecond
            if (now > expires) throw IllegalStateException("url expired")

            val data = "GET\n/$bucket/$key\n$expires"
            val sig = CryptoUtil.hmacSha256Hex(props.presignSecret, data)
            if (!sig.equals(signature, ignoreCase = true)) {
                throw IllegalStateException("invalid signature")
            }
        }

        if (!storage.existsObject(bucket, key)) throw NoSuchElementException("object not found")

        val size = storage.getObjectSize(bucket, key)
        val stream = storage.getObjectStream(bucket, key)

        res.contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE
        res.setHeader(HttpHeaders.CONTENT_LENGTH, size.toString())

        // 다운로드 파일명 헤더(선택)
        val filename = URLEncoder.encode(key.substringAfterLast('/'), StandardCharsets.UTF_8)
        res.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''$filename")

        stream.use { input -> input.copyTo(res.outputStream) }
    }

    @DeleteMapping("/{bucket}/{**key}")
    fun deleteObject(@PathVariable bucket: String, @PathVariable key: String): ResponseEntity<Unit> {
        if (!storage.existsObject(bucket, key)) throw NoSuchElementException("object not found")
        storage.deleteObject(bucket, key)
        return ResponseEntity.noContent().build()
    }
}