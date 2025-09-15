package aftsundayServer.aftsunday_minis3.web

import aftsundayServer.aftsunday_minis3.storage.StorageService
import jakarta.validation.constraints.Pattern
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("/buckets")
class BucketController(
    private val storage: StorageService
) {
    data class CreateBucketReq(
        @field:Pattern(regexp = "^[a-z0-9.-]{3,63}$",
            message = "bucket name must be 3-63 chars, lowercase, digits, dot, hyphen")
        val name: String
    )

    @GetMapping
    fun listBuckets() = storage.listBuckets()

    @PostMapping
    fun createBucket(@RequestBody req: CreateBucketReq): ResponseEntity<Unit> {
        if (storage.existsBucket(req.name)) {
            return ResponseEntity.status(409).build()
        }
        storage.createBucket(req.name)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/{bucket}")
    fun deleteBucket(@PathVariable bucket: String): ResponseEntity<Unit> {
        if (!storage.existsBucket(bucket)) throw NoSuchElementException("bucket not found")
        storage.deleteBucket(bucket)
        return ResponseEntity.noContent().build()
    }
}