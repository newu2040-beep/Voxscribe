package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TranscriptDao {
    @Query("SELECT * FROM transcripts ORDER BY timestamp DESC")
    fun getAllTranscripts(): Flow<List<TranscriptEntity>>

    @Query("SELECT * FROM transcripts WHERE id = :id")
    fun getTranscriptById(id: Int): Flow<TranscriptEntity?>

    @Query("SELECT * FROM transcripts WHERE id = :id")
    suspend fun getTranscriptByIdOneShot(id: Int): TranscriptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranscript(transcript: TranscriptEntity): Long

    @Update
    suspend fun updateTranscript(transcript: TranscriptEntity)

    @Delete
    suspend fun deleteTranscript(transcript: TranscriptEntity)

    @Query("UPDATE transcripts SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Int)
}
