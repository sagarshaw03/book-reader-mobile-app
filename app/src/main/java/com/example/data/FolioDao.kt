package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FolioDao {
    @Query("SELECT * FROM folios ORDER BY lastOpenedTimestamp DESC")
    fun getAllFolios(): Flow<List<FolioEntity>>

    @Query("SELECT * FROM folios WHERE id = :folioId LIMIT 1")
    fun observeFolioById(folioId: String): Flow<FolioEntity?>

    @Query("SELECT * FROM folios WHERE id = :folioId LIMIT 1")
    suspend fun getFolioById(folioId: String): FolioEntity?

    @Query("SELECT COUNT(*) FROM folios")
    suspend fun getFolioCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolios(folios: List<FolioEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolio(folio: FolioEntity)

    @Update
    suspend fun updateFolio(folio: FolioEntity)

    @Query("UPDATE folios SET currentPage = :pageIndex, lastOpenedTimestamp = :timestamp WHERE id = :folioId")
    suspend fun updateReadingProgress(folioId: String, pageIndex: Int, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE folios SET isFavorite = :isFavorite WHERE id = :folioId")
    suspend fun updateFavorite(folioId: String, isFavorite: Boolean)

    // Chapters
    @Query("SELECT * FROM chapters WHERE folioId = :folioId ORDER BY startPage ASC, level ASC, chapterNumber ASC")
    fun getChaptersForFolio(folioId: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE folioId = :folioId ORDER BY startPage ASC, level ASC, chapterNumber ASC")
    suspend fun getChapterListForFolioSync(folioId: String): List<ChapterEntity>

    @Query("SELECT COUNT(*) FROM chapters WHERE folioId = :folioId")
    suspend fun getChapterCountForFolio(folioId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity)

    @Query("DELETE FROM chapters WHERE folioId = :folioId")
    suspend fun deleteChaptersForFolio(folioId: String)

    // Bookmarks
    @Query("SELECT * FROM bookmarks WHERE folioId = :folioId ORDER BY pageIndex ASC")
    fun getBookmarksForFolio(folioId: String): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE folioId = :folioId AND pageIndex = :pageIndex LIMIT 1")
    suspend fun getBookmarkForPage(folioId: String, pageIndex: Int): BookmarkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmarks(bookmarks: List<BookmarkEntity>)

    @Query("DELETE FROM bookmarks WHERE id = :bookmarkId")
    suspend fun deleteBookmarkById(bookmarkId: Long)

    @Query("DELETE FROM bookmarks WHERE folioId = :folioId AND pageIndex = :pageIndex")
    suspend fun deleteBookmarkForPage(folioId: String, pageIndex: Int)
}
