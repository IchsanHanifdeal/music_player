package com.example.spotify

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.spotify.api.SpotifyApiService
import com.example.spotify.api.SpotifyTracksResponse
import com.example.spotify.api.Track // Import Track
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class HomeActivity : AppCompatActivity() {
    private lateinit var spotifyAdapter: SpotifyAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyMessage: TextView
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        recyclerView = findViewById(R.id.recycler_view)
        emptyMessage = findViewById(R.id.empty_message)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val token = intent.getStringExtra("306ad45dfeb34eba9793cbe8f2227542")
        if (token != null) {
            fetchTopTracks(token)
        } else {
            emptyMessage.text = "Token tidak ditemukan. Harap autentikasi ulang."
            emptyMessage.visibility = View.VISIBLE
        }
    }

    private fun fetchTopTracks(token: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.spotify.com/")
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(SpotifyApiService::class.java)
        val call = apiService.getTopTracks("Bearer $token")

        call.enqueue(object : Callback<SpotifyTracksResponse> {
            override fun onResponse(call: Call<SpotifyTracksResponse>, response: Response<SpotifyTracksResponse>) {
                if (response.isSuccessful) {
                    val tracks = response.body()?.items ?: emptyList()
                    if (tracks.isNotEmpty()) {
                        emptyMessage.visibility = View.GONE
                        setupRecyclerView(tracks)
                    } else {
                        emptyMessage.visibility = View.VISIBLE
                    }
                } else {
                    Log.e("HomeActivity", "Failed to fetch tracks: ${response.message()}")
                    emptyMessage.text = "Gagal memuat lagu: ${response.message()}"
                    emptyMessage.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<SpotifyTracksResponse>, t: Throwable) {
                Log.e("HomeActivity", "Error: ${t.message}")
                emptyMessage.text = "Kesalahan koneksi: ${t.message}"
                emptyMessage.visibility = View.VISIBLE
            }
        })
    }

    private fun setupRecyclerView(tracks: List<Track>) {
        spotifyAdapter = SpotifyAdapter(
            tracks,
            onPlayMusic = { track: Track -> playMusic(track.previewurl) }
        )
        recyclerView.adapter = spotifyAdapter
    }


    private fun playMusic(url: String?) {
        if (url == null) {
            Log.e("HomeActivity", "Track URL is null, cannot play")
            return
        }

        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(url)
                prepare()
                start()
            } catch (e: IOException) {
                Log.e("HomeActivity", "Error playing song: ${e.message}")
            }
        }
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        super.onDestroy()
    }
}
