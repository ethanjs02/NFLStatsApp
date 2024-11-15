package com.example.nflstatsapp.data.players

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class Player (
    @PrimaryKey(autoGenerate = false) val id: Int?,
    @ColumnInfo(name = "teamId") val teamId: Int,
    @ColumnInfo(name = "firstName") val firstName: String,
    @ColumnInfo(name = "lastName") val lastName: String,
    @ColumnInfo(name = "fullName") val fullName: String,
    @ColumnInfo(name = "displayName") val displayName: String,
    @ColumnInfo(name = "shortName") val shortName: String,
    @ColumnInfo(name = "weight") val weight: Int,
    @ColumnInfo(name = "displayWeight") val displayWeight: String,
    @ColumnInfo(name = "height") val height: Int,
    @ColumnInfo(name = "displayHeight") val displayHeight: String,
    @ColumnInfo(name = "age") val age: Int,
    @ColumnInfo(name = "dateOfBirth") val dateOfBirth: String, // Store as a string (e.g., "1992-04-27T07:00Z")
    @ColumnInfo(name = "jersey") val jersey: String?,
    @ColumnInfo(name = "position") val position: String,
    @ColumnInfo(name = "positionAbbreviation") val positionAbbreviation: String,
    @ColumnInfo(name = "headshotUrl") val headshotUrl: String, // URL to the player's headshot image
    @ColumnInfo(name = "headshotAltText") val headshotAltText: String
)
