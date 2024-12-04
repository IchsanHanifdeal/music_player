package com.example.spotify

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.spotify.database.DatabaseHelper
import com.example.spotify.model.Song
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class TrackAdapter(
    private val context: Context,
    private var songs: List<Song>,
    private val onSongClickListener: (Song) -> Unit // Listener untuk klik lagu
) : RecyclerView.Adapter<TrackAdapter.SongViewHolder>() {

    private val databaseHelper: DatabaseHelper = DatabaseHelper(context)
    private var mediaPlayer: MediaPlayer? = null

    // ViewHolder untuk menyimpan elemen UI dari item lagu
    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val songName: TextView = itemView.findViewById(R.id.song_name)
        val artistName: TextView = itemView.findViewById(R.id.artist_name)
        val deleteButton: ImageButton = itemView.findViewById(R.id.delete_button)
        val addToPlaylistButton: ImageButton = itemView.findViewById(R.id.add_to_playlist_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.track_activity, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.songName.text = song.name
        holder.artistName.text = song.artist

        // Klik untuk memutar musik
        holder.itemView.setOnClickListener {
            // Panggil listener untuk menangani klik pada lagu
            onSongClickListener(song)
            playMusic(song) // Memutar musik saat klik lagu
        }

        // Hapus Lagu
        holder.deleteButton.setOnClickListener {
            val success = databaseHelper.deleteSong(song.id_song)
            if (success) {
                Toast.makeText(context, "Lagu dihapus", Toast.LENGTH_SHORT).show()
                updateSongs(databaseHelper.getAllSongs()) // Perbarui daftar lagu
            } else {
                Toast.makeText(context, "Gagal menghapus lagu", Toast.LENGTH_SHORT).show()
            }
        }

        // Tambahkan ke Playlist
        holder.addToPlaylistButton.setOnClickListener {
            // Logika untuk menambahkan ke playlist
            Toast.makeText(context, "Lagu ditambahkan ke playlist", Toast.LENGTH_SHORT).show()
        }
    }

    fun playMusic(song: Song) {
        try {
            // Menghentikan media player jika sedang diputar
            mediaPlayer?.apply {
                if (isPlaying) stop()
                reset()
            } ?: run {
                mediaPlayer = MediaPlayer()
            }

            val fileUri = Uri.parse(song.file)

            // Mengecek apakah file ada
            if (fileUri.scheme == "content") {
                // Coba menggunakan ContentResolver untuk mengakses file
                val resolver = context.contentResolver
                try {
                    val inputStream = resolver.openInputStream(fileUri)
                    if (inputStream != null) {
                        // Menggunakan InputStream langsung dengan setDataSource
                        mediaPlayer?.setDataSource(inputStream.fd)  // get file descriptor from input stream
                        inputStream.close()
                    } else {
                        throw FileNotFoundException("File tidak ditemukan di URI: $fileUri")
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "File tidak ditemukan: ${e.message}", Toast.LENGTH_SHORT).show()
                    return
                }
            } else {
                // File path langsung jika tidak menggunakan content resolver
                val file = File(fileUri.path)
                if (!file.exists()) {
                    Toast.makeText(context, "File tidak ditemukan: ${fileUri.path}", Toast.LENGTH_SHORT).show()
                    return
                }
                mediaPlayer?.setDataSource(file.absolutePath) // Menetapkan data source jika menggunakan file path langsung
            }

            // Menyiapkan dan memutar media player
            mediaPlayer?.apply {
                prepareAsync() // Menggunakan prepareAsync agar tidak memblokir UI thread
                setOnPreparedListener {
                    start() // Memulai pemutaran saat media siap
                    Toast.makeText(context, "Memutar: ${song.name}", Toast.LENGTH_SHORT).show()
                }
                setOnErrorListener { mp, what, extra ->
                    // Menangani error DRM jika diperlukan
                    if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
                        Toast.makeText(context, "Gagal memutar musik, error tidak diketahui", Toast.LENGTH_SHORT).show()
                    } else if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
                        Toast.makeText(context, "Server media tidak dapat diakses", Toast.LENGTH_SHORT).show()
                    }
                    // Jika terkait dengan DRM error
                    if (what == MediaPlayer.MEDIA_ERROR_UNSUPPORTED) {
                        Toast.makeText(context, "File ini dilindungi oleh DRM dan tidak dapat diputar", Toast.LENGTH_SHORT).show()
                    }
                    true // Menangani error
                }
            }

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            Toast.makeText(context, "File tidak ditemukan: ${e.message}", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal memutar musik: ${e.message}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }



    // Perbarui daftar lagu di adapter
    fun updateSongs(newSongs: List<Song>) {
        songs = newSongs
        notifyDataSetChanged()
    }

    // Fungsi untuk melepaskan MediaPlayer saat tidak diperlukan
    fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    // Pastikan MediaPlayer dilepas saat adapter dilepas
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        releaseMediaPlayer()
    }

    override fun getItemCount(): Int = songs.size
}
