package com.nevoit.cresto.data.backup

import android.content.Context
import java.io.File
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Packs the app's private data directory into a zip archive.
 *
 * This is a raw safety net, distinct from the JSON backup: it copies the MMKV files and the
 * Room database itself, so it survives even if the backup schema ever changes in a way that
 * makes old JSON unreadable.
 *
 * Only preferences and database files are included; caches are skipped.
 */
object AppDataArchive {

    private val INCLUDED_DIRS = listOf("files/mmkv", "databases", "shared_prefs")

    /**
     * Writes the archive to [output].
     *
     * @return number of files written.
     */
    fun write(context: Context, output: OutputStream): Int {
        val root = context.dataDir
        var count = 0
        ZipOutputStream(output.buffered()).use { zip ->
            for (relative in INCLUDED_DIRS) {
                val dir = File(root, relative)
                if (dir.exists()) {
                    count += addDirectory(zip, dir, "$relative/")
                }
            }
        }
        return count
    }

    private fun addDirectory(zip: ZipOutputStream, dir: File, prefix: String): Int {
        val entries = dir.listFiles() ?: return 0
        var count = 0
        for (file in entries) {
            if (file.isDirectory) {
                count += addDirectory(zip, file, "$prefix${file.name}/")
            } else if (file.isFile) {
                zip.putNextEntry(ZipEntry("$prefix${file.name}"))
                file.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
                count++
            }
        }
        return count
    }
}
