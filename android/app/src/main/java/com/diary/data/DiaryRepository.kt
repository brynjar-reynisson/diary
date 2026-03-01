package com.diary.data

interface DiaryRepository {
    /** Returns entries grouped as year → month → list of entries */
    suspend fun listEntries(): Map<String, Map<String, List<DiaryEntry>>>

    /** Downloads and returns the text content of an entry */
    suspend fun getEntryContent(entry: DiaryEntry): String

    /** Creates a new entry for today with the given content, returns the new entry */
    suspend fun createEntry(content: String): DiaryEntry
}
