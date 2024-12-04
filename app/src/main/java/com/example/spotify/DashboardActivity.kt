package com.example.spotify

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spotify.database.DatabaseHelper
import com.example.spotify.model.Song
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DashboardActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: TrackAdapter
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var fabAddSong: FloatingActionButton

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true &&
                permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true ||
                permissions[Manifest.permission.READ_MEDIA_AUDIO] == true) {
                initRecyclerView()
            } else {
                // Izin ditolak
                Toast.makeText(this, "Izin untuk akses media diperlukan", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard_activity)

        // Periksa izin saat aplikasi dijalankan
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Untuk Android 10 ke bawah
            val readStoragePermission = ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            )
            val writeStoragePermission = ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE
            )

            // Untuk Android 13 ke atas
            val readMediaAudioPermission = ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_MEDIA_AUDIO
            )

            // Jika izin belum diberikan, minta izin
            if (readStoragePermission != PackageManager.PERMISSION_GRANTED ||
                writeStoragePermission != PackageManager.PERMISSION_GRANTED ||
                readMediaAudioPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissionsLauncher.launch(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_MEDIA_AUDIO
                    )
                )
            } else {
                initRecyclerView()
            }
        } else {
            // Untuk Android versi lama, langsung jalankan fungsi yang membutuhkan izin
            initRecyclerView()
        }

        fabAddSong = findViewById(R.id.fab_add_song)
        fabAddSong.setOnClickListener {
            val intent = Intent(this, AddSongActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initRecyclerView() {
        // Inisialisasi RecyclerView dan Adapter setelah izin diberikan
        databaseHelper = DatabaseHelper(this)
        recyclerView = findViewById(R.id.recycler_view)

        // Pass listener ke TrackAdapter
        songAdapter = TrackAdapter(this, databaseHelper.getAllSongs()) { song ->
            // Tindakan yang diinginkan saat lagu diklik (misalnya memutar musik)
            playMusic(song)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = songAdapter
    }

    // Fungsi untuk memutar musik
    private fun playMusic(song: Song) {
        // Logika untuk memutar musik (sesuaikan dengan implementasi playMusic Anda)
        Toast.makeText(this, "Memutar lagu: ${song.name}", Toast.LENGTH_SHORT).show()
    }

    fun logout(view: View) {
        finish()
    }
}
