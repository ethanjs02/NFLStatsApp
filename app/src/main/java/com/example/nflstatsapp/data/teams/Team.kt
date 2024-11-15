package com.example.nflstatsapp.data.teams

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teams")
data class Team(
    @PrimaryKey(autoGenerate = false) val id: Int,
    @ColumnInfo(name = "abbreviation") val abbreviation: String,
    @ColumnInfo(name = "displayName") val displayName: String,
    @ColumnInfo(name = "shortDisplayName") val shortDisplayName: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "location") val location: String,
    @ColumnInfo(name = "color") val color: String,
    @ColumnInfo(name = "alternateColor") val alternateColor: String?,
    @ColumnInfo(name = "logoUrl") val logoUrl: String
)