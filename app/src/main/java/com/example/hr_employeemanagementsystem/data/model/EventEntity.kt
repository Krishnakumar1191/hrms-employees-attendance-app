package com.example.hr_employeemanagementsystem.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val date: String,
    val description: String,
    val type: String // Holiday, Birthday, Meeting, Announcement
)
