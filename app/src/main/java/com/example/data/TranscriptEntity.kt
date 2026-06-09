package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transcripts")
data class TranscriptEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val filePath: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationMs: Long = 0L,
    val rawText: String,
    val formattedTranscript: String, // This holds structured speaker turn data or final formatted text
    val languageName: String,
    val languageCode: String,
    val isSynced: Boolean = false,
    val speakerCount: Int = 1
)
