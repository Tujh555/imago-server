package io.tujh.files

import io.ktor.util.cio.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import net.coobird.thumbnailator.Thumbnails
import java.io.File
import java.util.UUID
import javax.imageio.ImageIO

class WriteImage(folderName: String) {
    val folder by folder(folderName)

    suspend fun resized(source: ByteReadChannel, width: Int = 300, height: Int = 300): String? {
        val target = folder.random()

        return try {
            withContext(Dispatchers.IO) {
                val image = ImageIO.read(source.toInputStream(currentCoroutineContext().job))
                Thumbnails.of(image).size(width, height).toFile(target)
                url(target.path)
            }
        } catch (e: Exception) {
            target.delete()
            null
        }
    }

    suspend fun original(source: ByteReadChannel): String? {
        val target = folder.random()

        return try {
            withContext(Dispatchers.IO) {
                source.copyAndClose(target.writeChannel())
                url(target.path)
            }
        } catch (e: Exception){
            target.delete()
            null
        }
    }

    private fun File.random() = resolve("${UUID.randomUUID()}.jpg").apply(File::createNewFile)

    private fun folder(name: String) = lazy {
        File(name).apply {
            if (exists().not()) {
                mkdirs()
            }
        }
    }

    private fun url(path: String) = "http://10.0.2.2:8080/$path"
}