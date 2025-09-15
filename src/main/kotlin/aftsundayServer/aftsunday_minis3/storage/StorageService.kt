package aftsundayServer.aftsunday_minis3.storage

import org.springframework.web.multipart.MultipartFile
import java.io.InputStream

data class ObjectInfo(
    val bucket: String,
    val key: String,
    val size: Long
)

interface StorageService {
    fun listBuckets(): List<String>
    fun createBucket(bucket: String)
    fun deleteBucket(bucket: String) // 비어있을 때만

    fun listObjects(bucket: String, prefix: String? = null): List<ObjectInfo>
    fun putObject(bucket: String, key: String, file: MultipartFile)
    fun getObjectStream(bucket: String, key: String): InputStream
    fun getObjectSize(bucket: String, key: String): Long
    fun deleteObject(bucket: String, key: String)

    fun existsBucket(bucket: String): Boolean
    fun existsObject(bucket: String, key: String): Boolean
}