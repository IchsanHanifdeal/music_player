package com.example.spotify

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.spotify.database.DatabaseHelper
import com.example.spotify.model.Song
import java.io.FileNotFoundException

class AddSongActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var songNameInput: EditText
    private lateinit var artistNameInput: EditText
    private lateinit var selectMp3Button: Button
    private lateinit var selectThumbnailButton: Button
    private lateinit var addSongButton: Button
    private lateinit var backButton: Button
    private lateinit var thumbnailImageView: ImageView

    private var selectedMusicPath: String = ""
    private var selectedThumbnailPath: String = ""

    companion object {
        private const val PICK_MP3_FILE = 1
        private const val PICK_IMAGE_FILE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_music_activity)

        // Initialize DatabaseHelper
        databaseHelper = DatabaseHelper(this)

        // Initialize Views
        songNameInput = findViewById(R.id.song_name_input)
        artistNameInput = findViewById(R.id.artist_name_input)
        selectMp3Button = findViewById(R.id.select_mp3_button)
        selectThumbnailButton = findViewById(R.id.select_thumbnail_button)
        addSongButton = findViewById(R.id.add_song_button)
        backButton = findViewById(R.id.back)
        thumbnailImageView = findViewById(R.id.thumbnail_image_view)

        // Select MP3 File
        selectMp3Button.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "audio/*"
            }
            startActivityForResult(intent, PICK_MP3_FILE)
        }

        // Select Thumbnail File
        selectThumbnailButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            startActivityForResult(intent, PICK_IMAGE_FILE)
        }

        // Add Song to Database
        addSongButton.setOnClickListener {
            val songName = songNameInput.text.toString()
            val artistName = artistNameInput.text.toString()

            if (songName.isNotEmpty() && artistName.isNotEmpty() && selectedMusicPath.isNotEmpty() && selectedThumbnailPath.isNotEmpty()) {
                val song = Song(
                    id_song = 0,
                    name = songName,
                    artist = artistName,
                    file = selectedMusicPath,
                    thumbnail = selectedThumbnailPath
                )
                val success = databaseHelper.addSong(song)
                if (success) {
                    Toast.makeText(this, "Song added successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to add song.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill all fields and select files.", Toast.LENGTH_SHORT).show()
            }
        }

        // Back to previous screen
        backButton.setOnClickListener {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                PICK_MP3_FILE -> {
                    val uri = data.data
                    if (uri != null) {
                        selectedMusicPath = uri.toString() // Simpan URI sebagai string
                        selectMp3Button.text = getFileNameFromURI(uri) ?: "Audio Selected"
                    } else {
                        Toast.makeText(this, "Failed to get audio file path", Toast.LENGTH_SHORT).show()
                    }
                }
                PICK_IMAGE_FILE -> {
                    val uri = data.data
                    if (uri != null) {
                        selectedThumbnailPath = uri.toString()
                        try {
                            val inputStream = contentResolver.openInputStream(uri)
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            thumbnailImageView.setImageBitmap(bitmap)
                        } catch (e: FileNotFoundException) {
                            e.printStackTrace()
                            Toast.makeText(this, "Failed to load thumbnail", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    /**
     * Mendapatkan nama file dari URI.
     */
    private fun getFileNameFromURI(uri: Uri): String? {
        var fileName: String? = null
        if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                if (cursor.moveToFirst()) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }
        return fileName ?: uri.lastPathSegment
    }

    private fun queryContentResolver(uri: Uri, selection: String?, selectionArgs: Array<String>?): String? {
        val projection = arrayOf(MediaStore.MediaColumns.DATA)
        contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            if (cursor.moveToFirst()) {
                return cursor.getString(columnIndex)
            }
        }
        return null
    }
}
