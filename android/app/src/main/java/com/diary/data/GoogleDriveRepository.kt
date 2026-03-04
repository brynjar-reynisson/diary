package com.diary.data

import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File as DriveFile
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.*

class GoogleDriveRepository(accessToken: String) : DiaryRepository {

    private val drive: Drive

    init {
        val credentials = GoogleCredentials.create(AccessToken(accessToken, null))
        drive = Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            HttpCredentialsAdapter(credentials),
        ).setApplicationName("Diary").build()
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun findFolderQuery(name: String, parentId: String?): String {
        val parentClause = if (parentId != null) "'$parentId' in parents" else "'root' in parents"
        return "name='$name' and mimeType='application/vnd.google-apps.folder' and $parentClause and trashed=false"
    }

    private suspend fun findFolder(name: String, parentId: String?): String? =
        withContext(Dispatchers.IO) {
            val result = drive.files().list()
                .setQ(findFolderQuery(name, parentId))
                .setFields("files(id)")
                .execute()
            result.files.firstOrNull()?.id
        }

    private suspend fun findOrCreateFolder(name: String, parentId: String?): String =
        withContext(Dispatchers.IO) {
            findFolder(name, parentId) ?: run {
                val meta = DriveFile().apply {
                    this.name = name
                    mimeType = "application/vnd.google-apps.folder"
                    if (parentId != null) parents = listOf(parentId)
                }
                drive.files().create(meta).setFields("id").execute().id
            }
        }

    // ── DiaryRepository ───────────────────────────────────────────────────────

    override suspend fun listEntries(): Map<String, Map<String, List<DiaryEntry>>> =
        withContext(Dispatchers.IO) {
            val diaryId = findFolder("diary", null)
                ?: return@withContext emptyMap()

            val result = mutableMapOf<String, MutableMap<String, MutableList<DiaryEntry>>>()

            val years = drive.files().list()
                .setQ("mimeType='application/vnd.google-apps.folder' and '$diaryId' in parents and trashed=false")
                .setFields("files(id, name)").execute().files

            for (yearFile in years) {
                val year = yearFile.name
                result[year] = mutableMapOf()

                val months = drive.files().list()
                    .setQ("mimeType='application/vnd.google-apps.folder' and '${yearFile.id}' in parents and trashed=false")
                    .setFields("files(id, name)").execute().files

                for (monthFile in months) {
                    val month = monthFile.name
                    result[year]!![month] = mutableListOf()

                    val files = drive.files().list()
                        .setQ("mimeType='text/plain' and '${monthFile.id}' in parents and trashed=false")
                        .setOrderBy("name")
                        .setFields("files(id, name)").execute().files

                    for (f in files) {
                        val day = f.name.removePrefix("entry-").substringBefore("-")
                        result[year]!![month]!!.add(
                            DiaryEntry(year, month, day, f.name, "diary/$year/$month/${f.name}")
                        )
                    }
                }
            }
            result
        }

    override suspend fun getEntryContent(entry: DiaryEntry): String =
        withContext(Dispatchers.IO) {
            val diaryId = findFolder("diary", null) ?: error("No diary folder")
            val yearId = findFolder(entry.year, diaryId) ?: error("Year folder not found")
            val monthId = findFolder(entry.month, yearId) ?: error("Month folder not found")

            val files = drive.files().list()
                .setQ("name='${entry.filename}' and '$monthId' in parents and trashed=false")
                .setFields("files(id)").execute().files

            val fileId = files.firstOrNull()?.id ?: error("File not found")
            val out = ByteArrayOutputStream()
            drive.files().get(fileId).executeMediaAndDownloadTo(out)
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

            val diaryId = findOrCreateFolder("diary", null)
            val yearId = findOrCreateFolder(year, diaryId)
            val monthId = findOrCreateFolder(month, yearId)

            val meta = DriveFile().apply {
                name = filename
                mimeType = "text/plain"
                parents = listOf(monthId)
            }
            val mediaContent = com.google.api.client.http.ByteArrayContent(
                "text/plain",
                content.toByteArray(Charsets.UTF_8),
            )
            drive.files().create(meta, mediaContent).setFields("id").execute()

            DiaryEntry(year, month, day, filename, "diary/$year/$month/$filename")
        }
}
