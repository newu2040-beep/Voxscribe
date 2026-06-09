package com.example.data

import kotlinx.coroutines.flow.Flow

class TranscriptRepository(private val transcriptDao: TranscriptDao) {
    val allTranscripts: Flow<List<TranscriptEntity>> = transcriptDao.getAllTranscripts()

    fun getTranscriptById(id: Int): Flow<TranscriptEntity?> {
        return transcriptDao.getTranscriptById(id)
    }

    suspend fun insertTranscript(transcript: TranscriptEntity): Long {
        return transcriptDao.insertTranscript(transcript)
    }

    suspend fun updateTranscript(transcript: TranscriptEntity) {
        transcriptDao.updateTranscript(transcript)
    }

    suspend fun deleteTranscript(transcript: TranscriptEntity) {
        transcriptDao.deleteTranscript(transcript)
    }

    suspend fun markAsSynced(id: Int) {
        transcriptDao.markAsSynced(id)
    }
}
