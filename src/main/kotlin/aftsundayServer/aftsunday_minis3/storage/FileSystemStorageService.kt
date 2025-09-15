package aftsundayServer.aftsunday_minis3.storage

import aftsundayServer.aftsunday_minis3.config.AppProperties
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Service
class FileSystemStorageService(
    private val props: AppProperties
) : StorageService {

    private fun bucketPath(bucket: String): Path =
        Paths.get(props.storageRoot).resolve(bucket)

    private fun objectPath(bucket: String, key: String): Path =
        bucketPath(bucket).resolve(key)

    override fun listBuckets(): List<String> {
        val root = Paths.get(props.storageRoot)
        if (!Files.exists(root)) return emptyList()
        return Files.list(root)
            .filter { Files.isDirectory(it) }
            .map { it.fileName.toString() }
            .sorted()
            .toList()
    }

    override fun createBucket(bucket: String) {
        val p = bucketPath(bucket)
        Files.createDirectories(p)
    }

    override fun deleteBucket(bucket: String) {
        val p = bucketPath(bucket)
        if (!Files.exists(p)) return
        // 비어있을 때만 삭제
        Files.newDirectoryStream(p).use { ds ->
            if (ds.iterator().hasNext()) {
                throw IllegalStateException("Bucket not empty")
            }
        }
        Files.delete(p)
    }

    override fun listObjects(bucket: String, prefix: String?): List<ObjectInfo> {
        val base = bucketPath(bucket)
        if (!Files.exists(base)) return emptyList()

        val results = mutableListOf<ObjectInfo>()
        Files.walk(base).use { stream ->
            stream.filter { Files.isRegularFile(it) }.forEach { path ->
                val key = base.relativize(path).toString().replace(File.separatorChar, '/')
                if (prefix == null || key.startsWith(prefix)) {
                    results += ObjectInfo(bucket, key, Files.size(path))
                }
            }
        }
        return results.sortedBy { it.key }
    }

    override fun putObject(bucket: String, key: String, file: MultipartFile) {
        val target = objectPath(bucket, key)
        Files.createDirectories(target.parent)
        BufferedInputStream(file.inputStream).use { input ->
            BufferedOutputStream(FileOutputStream(target.toFile())).use { out ->
                input.copyTo(out)
            }
        }
    }

    override fun getObjectStream(bucket: String, key: String) =
        BufferedInputStream(FileInputStream(objectPath(bucket, key).toFile()))

    override fun getObjectSize(bucket: String, key: String) =
        Files.size(objectPath(bucket, key))

    override fun deleteObject(bucket: String, key: String) {
        val p = objectPath(bucket, key)
        if (Files.exists(p)) Files.delete(p)
    }

    override fun existsBucket(bucket: String) = Files.exists(bucketPath(bucket))

    override fun existsObject(bucket: String, key: String) = Files.exists(objectPath(bucket, key))
}