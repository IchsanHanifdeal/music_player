package com.example.spotify.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import android.util.Log
import com.example.spotify.model.Playlist
import com.example.spotify.model.Song
import java.io.File

class DatabaseHelper(private val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "user_db"
        private const val DATABASE_VERSION = 1

        // Tabel Users
        private const val TABLE_NAME_USERS = "users"
        private const val COLUMN_ID_USER = "id"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PASSWORD = "password"

        // Tabel Songs
        private const val TABLE_NAME_SONGS = "songs"
        private const val COLUMN_ID_SONG = "id_song"
        private const val COLUMN_SONG_NAME = "name"
        private const val COLUMN_ARTIST = "artist"
        private const val COLUMN_FILE = "file"
        private const val COLUMN_THUMBNAIL = "thumbnail"

        // Tabel Playlists
        private const val TABLE_NAME_PLAYLISTS = "playlists"
        private const val COLUMN_ID_PLAYLIST = "id_playlist"
        private const val COLUMN_NAME_PLAYLIST = "name_playlist"
        private const val COLUMN_ID_USER_PLAYLIST = "id_user"
        private const val COLUMN_ID_SONG_PLAYLIST = "id_song"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUserTable = "CREATE TABLE $TABLE_NAME_USERS (" +
                "$COLUMN_ID_USER INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_EMAIL TEXT, " +
                "$COLUMN_PASSWORD TEXT)"
        db.execSQL(createUserTable)

        val createSongsTable = "CREATE TABLE $TABLE_NAME_SONGS (" +
                "$COLUMN_ID_SONG INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_SONG_NAME TEXT, " +
                "$COLUMN_ARTIST TEXT, " +
                "$COLUMN_FILE TEXT, " +
                "$COLUMN_THUMBNAIL TEXT)"
        db.execSQL(createSongsTable)

        val createPlaylistsTable = "CREATE TABLE $TABLE_NAME_PLAYLISTS (" +
                "$COLUMN_ID_PLAYLIST INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_NAME_PLAYLIST TEXT, " +
                "$COLUMN_ID_USER_PLAYLIST INTEGER, " +
                "$COLUMN_ID_SONG_PLAYLIST INTEGER)"
        db.execSQL(createPlaylistsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_SONGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_PLAYLISTS")
        onCreate(db)
    }

    fun addUser(email: String, password: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_EMAIL, email)
            put(COLUMN_PASSWORD, password)
        }
        val result = db.insert(TABLE_NAME_USERS, null, values)
        db.close()
        return result != -1L
    }

    fun validateLogin(email: String, password: String): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NAME_USERS WHERE $COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?"
        val cursor: Cursor = db.rawQuery(query, arrayOf(email, password))
        val isValid = cursor.count > 0
        cursor.close()
        db.close()
        return isValid
    }

    fun isEmailExist(email: String): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NAME_USERS WHERE $COLUMN_EMAIL = ?"
        val cursor: Cursor = db.rawQuery(query, arrayOf(email))
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    fun addSong(song: Song, fileUri: Uri? = null): Boolean {
        val savedFilePath = fileUri?.let { saveFileToDirectory(it) } ?: song.file

        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_SONG_NAME, song.name)
            put(COLUMN_ARTIST, song.artist)
            put(COLUMN_FILE, savedFilePath)
            put(COLUMN_THUMBNAIL, song.thumbnail)
        }
        val result = db.insert(TABLE_NAME_SONGS, null, values)
        db.close()
        return result != -1L
    }

    @SuppressLint("Range")
    fun getAllSongs(): List<Song> {
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_NAME_SONGS", null)
        val songs = mutableListOf<Song>()

        if (cursor.moveToFirst()) {
            do {
                val song = Song(
                    cursor.getInt(cursor.getColumnIndex(COLUMN_ID_SONG)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_SONG_NAME)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_ARTIST)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_FILE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_THUMBNAIL))
                )
                songs.add(song)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return songs
    }

    fun deleteSong(songId: Int): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_NAME_SONGS, "$COLUMN_ID_SONG = ?", arrayOf(songId.toString()))
        db.close()
        return result > 0
    }

    @SuppressLint("Range")
    fun getPlaylistsByUser(userId: Int): List<Playlist> {
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM $TABLE_NAME_PLAYLISTS WHERE $COLUMN_ID_USER_PLAYLIST = ?",
            arrayOf(userId.toString())
        )
        val playlists = mutableListOf<Playlist>()

        if (cursor.moveToFirst()) {
            do {
                val playlist = Playlist(
                    cursor.getInt(cursor.getColumnIndex(COLUMN_ID_PLAYLIST)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_ID_USER_PLAYLIST)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_ID_SONG_PLAYLIST)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_NAME_PLAYLIST))
                )
                playlists.add(playlist)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return playlists
    }

    private fun saveFileToDirectory(fileUri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(fileUri)
            val directory = File(context.getExternalFilesDir(null), "Music")
            if (!directory.exists()) directory.mkdirs()

            val fileName = "${System.currentTimeMillis()}.mp3"
            val outputFile = File(directory, fileName)

            inputStream?.use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            outputFile.absolutePath
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error saving file: ${e.message}")
            null
        }
    }
}
