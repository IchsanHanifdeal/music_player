package com.example.spotify.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.spotify.model.Song
import com.example.spotify.model.User
import com.example.spotify.model.Playlist

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "music_app.db"
        const val DATABASE_VERSION = 1

        // Table Users
        const val TABLE_USER = "users"
        const val USER_ID = "id_user"
        const val USERNAME = "username"
        const val EMAIL = "email"
        const val PASSWORD = "password"
        const val ROLE = "role"

        // Table Songs
        const val TABLE_SONG = "songs"
        const val SONG_ID = "id_song"
        const val SONG_NAME = "name"
        const val ARTIST = "artist"
        const val FILE = "file"
        const val THUMBNAIL = "thumbnail"

        // Table Playlists
        const val TABLE_PLAYLIST = "playlists"
        const val PLAYLIST_ID = "id_playlist"
        const val USER_ID_FK = "id_user_fk"
        const val SONG_ID_FK = "id_song_fk"
        const val PLAYLIST_NAME = "name_playlist"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Create Users Table
        val createUserTable = "CREATE TABLE $TABLE_USER (" +
                "$USER_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$USERNAME TEXT, " +
                "$EMAIL TEXT, " +
                "$PASSWORD TEXT, " +
                "$ROLE TEXT)"
        db?.execSQL(createUserTable)

        // Create Songs Table
        val createSongTable = "CREATE TABLE $TABLE_SONG (" +
                "$SONG_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$SONG_NAME TEXT, " +
                "$ARTIST TEXT, " +
                "$FILE TEXT, " +
                "$THUMBNAIL TEXT)"
        db?.execSQL(createSongTable)

        // Create Playlists Table
        val createPlaylistTable = "CREATE TABLE $TABLE_PLAYLIST (" +
                "$PLAYLIST_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$USER_ID_FK INTEGER, " +
                "$SONG_ID_FK INTEGER, " +
                "$PLAYLIST_NAME TEXT, " +
                "FOREIGN KEY($USER_ID_FK) REFERENCES $TABLE_USER($USER_ID), " +
                "FOREIGN KEY($SONG_ID_FK) REFERENCES $TABLE_SONG($SONG_ID))"
        db?.execSQL(createPlaylistTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_SONG")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_PLAYLIST")
        onCreate(db)
    }

    // Add User
    fun addUser(user: User) {
        val db = writableDatabase
        val query = "INSERT INTO $TABLE_USER ($USERNAME, $EMAIL, $PASSWORD, $ROLE) VALUES (?, ?, ?, ?)"
        db.execSQL(query, arrayOf(user.username, user.email, user.password, user.role))
        db.close()
    }

    // Add Song
    fun addSong(songName: String, artistName: String, mp3FilePath: String, thumbnail: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(SONG_NAME, songName)
            put(ARTIST, artistName)
            put(FILE, mp3FilePath)
            put(THUMBNAIL, thumbnail)
        }
        val result = db.insert(TABLE_SONG, null, values)
        db.close()
        return result != -1L
    }

    @SuppressLint("Range")
    // Add Song to Playlist
    fun addSongToPlaylist(songId: Int, userId: Int, playlistName: String): Boolean {
        val db = writableDatabase

        // Check if the playlist already exists
        val queryCheck = "SELECT $PLAYLIST_ID FROM $TABLE_PLAYLIST WHERE $PLAYLIST_NAME = ? AND $USER_ID_FK = ?"
        val cursor = db.rawQuery(queryCheck, arrayOf(playlistName, userId.toString()))

        val playlistId: Int = if (cursor.moveToFirst()) {
            cursor.getInt(cursor.getColumnIndex(PLAYLIST_ID))
        } else {
            // Create new playlist if it does not exist
            val values = ContentValues().apply {
                put(USER_ID_FK, userId)
                put(PLAYLIST_NAME, playlistName)
            }
            db.insert(TABLE_PLAYLIST, null, values)
            db.rawQuery(queryCheck, arrayOf(playlistName, userId.toString())).use {
                if (it.moveToFirst()) it.getInt(it.getColumnIndex(PLAYLIST_ID)) else -1
            }
        }
        cursor.close()

        // Add song to playlist
        val values = ContentValues().apply {
            put(USER_ID_FK, userId)
            put(SONG_ID_FK, songId)
            put(PLAYLIST_NAME, playlistName)
        }
        val result = db.insert(TABLE_PLAYLIST, null, values)
        db.close()
        return result != -1L
    }

    // Get All Songs
    @SuppressLint("Range")
    fun getAllSongs(): List<Song> {
        val songList = mutableListOf<Song>()
        val db = this.readableDatabase

        val cursor = db.query(TABLE_SONG, null, null, null, null, null, null)

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex(SONG_ID))
                val name = cursor.getString(cursor.getColumnIndex(SONG_NAME))
                val artist = cursor.getString(cursor.getColumnIndex(ARTIST))
                val file = cursor.getString(cursor.getColumnIndex(FILE))
                val thumbnail = cursor.getString(cursor.getColumnIndex(THUMBNAIL))
                songList.add(Song(id_song = id, name = name, artist = artist, file = file, thumbnail = thumbnail))
            } while (cursor.moveToNext())
            cursor.close()
        }
        return songList
    }


    // Validate User
    fun validateUser(email: String, password: String): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_USER WHERE $EMAIL = ? AND $PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(email, password))

        val isValid = cursor.count > 0
        cursor.close()
        db.close()

        return isValid
    }

    // Delete Song
    fun deleteSong(songId: Int): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_SONG, "$SONG_ID = ?", arrayOf(songId.toString()))
        db.close()
        return result > 0
    }
}
