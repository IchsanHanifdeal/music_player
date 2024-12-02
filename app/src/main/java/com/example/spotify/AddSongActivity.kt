package com.example.spotify

import android.graphics.BitmapFactory
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.database.Cursor
import android.annotation.SuppressLint
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.Manifest
import android.widget.ImageView
import com.example.spotify.database.DatabaseHelper

class AddSongActivity : AppCompatActivity() {

    private lateinit var songNameInput: EditText
    private lateinit var artistNameInput: EditText
    private lateinit var selectMp3Button: Button
    private lateinit var addSongButton: Button
    private lateinit var selectThumbnailButton: Button
    private lateinit var thumbnailImageView: ImageView
    private lateinit var databaseHelper: DatabaseHelper
    private var mp3Uri: Uri? = null
    private var thumbnailUri: Uri? = null

    private val MP3_REQUEST_CODE = 1
    private val THUMBNAIL_REQUEST_CODE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_song_activity)

        songNameInput = findViewById(R.id.song_name_input)
        artistNameInput = findViewById(R.id.artist_name_input)
        selectMp3Button = findViewById(R.id.select_mp3_button)
        addSongButton = findViewById(R.id.add_song_button)
        selectThumbnailButton = findViewById(R.id.select_thumbnail_button)
        thumbnailImageView = findViewById(R.id.thumbnail_image_view)
        databaseHelper = DatabaseHelper(this)

        // Meminta izin untuk membaca file
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }

        selectMp3Button.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "audio/*"
            startActivityForResult(intent, MP3_REQUEST_CODE)
        }

        selectThumbnailButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, THUMBNAIL_REQUEST_CODE)
        }

        addSongButton.setOnClickListener {
            val songName = songNameInput.text.toString().trim()
            val artistName = artistNameInput.text.toString().trim()

            if (songName.isNotEmpty() && artistName.isNotEmpty() && mp3Uri != null) {
                // Mendapatkan path file MP3
                val mp3FilePath = getFilePathFromUri(mp3Uri!!)

                // Menambahkan lagu ke database
                val result = databaseHelper.addSong(songName, artistName, mp3FilePath, "") // Thumbnail bisa dikosongkan jika tidak ada
                if (result) {
                    Toast.makeText(this, "Song Added Successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to Add Song", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill all fields and select an MP3 file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            MP3_REQUEST_CODE -> {
                if (resultCode == RESULT_OK && data != null) {
                    mp3Uri = data.data
                    val fileName = getFileNameFromUri(mp3Uri!!)
                    selectMp3Button.text = fileName // Ganti teks tombol dengan nama file MP3
                }
            }
            THUMBNAIL_REQUEST_CODE -> {
                if (resultCode == RESULT_OK && data != null) {
                    thumbnailUri = data.data
                    val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(thumbnailUri!!))
                    thumbnailImageView.setImageBitmap(bitmap) // Menampilkan thumbnail yang dipilih
                }
            }
        }
    }

    @SuppressLint("Range")
    private fun getFileNameFromUri(uri: Uri): String {
        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
        cursor?.moveToFirst()
        val fileName = cursor?.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)) ?: ""
        cursor?.close()
        return fileName
    }

    @SuppressLint("Range")
    private fun getFilePathFromUri(uri: Uri): String {
        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
        cursor?.moveToFirst()
        val filePath = cursor?.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)) ?: ""
        cursor?.close()
        return filePath
    }
}
