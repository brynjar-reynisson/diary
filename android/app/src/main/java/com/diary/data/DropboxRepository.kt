package com.diary.data

import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.Calendar

class DropboxRepository(accessToken: String) : DiaryRepository {

    private val client: DbxClientV2 = DbxClientV2(
        DbxRequestConfig.newBuilder("diary-android/1.0").build(),
        accessToken,
    )

    private val DIARY_ROOT = "/diary"

    override suspend fun listEntries(): Map<String, Map<String, List<DiaryEntry>>> =
        withContext(Dispatchers.IO) {
            val result = mutableMapOf<String, MutableMap<String, MutableList<DiaryEntry>>>()

            val yearEntries = try {
                client.files().listFolder(DIARY_ROOT).entries
            } catch (e: Exception) {
                return@withContext result
            }

            for (yearMeta in yearEntries) {
                if (yearMeta !is com.dropbox.core.v2.files.FolderMetadata) continue
                val year = yearMeta.name
                result[year] = mutableMapOf()

                val monthEntries = client.files()
                    .listFolder("$DIARY_ROOT/$year").entries

                for (monthMeta in monthEntries) {
                    if (monthMeta !is com.dropbox.core.v2.files.FolderMetadata) continue
                    val month = monthMeta.name
                    result[year]!![month] = mutableListOf()

                    val fileEntries = client.files()
                        .listFolder("$DIARY_ROOT/$year/$month").entries

                    for (fileMeta in fileEntries) {
                        if (fileMeta !is com.dropbox.core.v2.files.FileMetadata) continue
                        if (!fileMeta.name.endsWith(".txt")) continue
                        val day = fileMeta.name.removePrefix("entry-").substringBefore("-")
                        result[year]!![month]!!.add(
                            DiaryEntry(year, month, day, fileMeta.name)
                        )
                    }
                    result[year]!![month]!!.sortBy { it.filename }
                }
            }
            result
        }

    override suspend fun getEntryContent(entry: DiaryEntry): String =
        withContext(Dispatchers.IO) {
            val out = ByteArrayOutputStream()
            client.files().download("$DIARY_ROOT/${entry.year}/${entry.month}/${entry.filename}").download(out)
            out.toString("UTF-8")
        }

    override suspend fun createEntry(content: String): DiaryEntry =
        withContext(Dispatchers.IO) {
            val now = Calendar.getInstance()
            val year = now.get(Calendar.YEAR).toString()
            val month = String.format("%02d", now.get(Calendar.MONTH) + 1)
            val day = String.format("%02d", now.get(Calendar.DAY_OF_MONTH))
            val hh = String.format("%02d", now.get(Calendar.HOUR_OF_DAY))
            val mm = String.format("%02d", now.get(Calendar.MINUTE))
            val ss = String.format("%02d", now.get(Calendar.SECOND))
            val filename = "entry-$day-$hh:$mm:$ss.txt"
            val path = "$DIARY_ROOT/$year/$month/$filename"

            val bytes = content.toByteArray(Charsets.UTF_8)
            client.files().uploadBuilder(path)
                .uploadAndFinish(bytes.inputStream())

            DiaryEntry(year, month, day, filename)
        }
}
