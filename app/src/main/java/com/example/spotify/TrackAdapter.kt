package com.example.spotify

import android.content.Context
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class TrackAdapter(
    private val context: Context,
    private val songs: MutableList<Map<String, String>>, // List of songs as Map
    private val onSongClickListener: (Map<String, String>) -> Unit, // Click listener to play song
    private val onAddToPlaylistListener: (Map<String, String>) -> Unit, // Add to playlist listener
) : RecyclerView.Adapter<TrackAdapter.SongViewHolder>() {

    private val playlist = mutableListOf<Map<String, String>>() // Playlist

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val songName: TextView = itemView.findViewById(R.id.song_name)
        val artistName: TextView = itemView.findViewById(R.id.artist_name)
        val addToPlaylistButton: ImageButton = itemView.findViewById(R.id.add_to_playlist_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.track_activity, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]

        // Set song name and artist
        holder.songName.text = song["name"] ?: "Unknown Title"
        holder.artistName.text = "Unknown Artist" // Add artist info if available

        // Set click listener for item to play song
        holder.itemView.setOnClickListener {
            onSongClickListener(song)
        }

        // Update button state (added/not added)
        updateButtonState(holder.addToPlaylistButton, playlist.contains(song))

        // Click to add song to playlist
        holder.addToPlaylistButton.setOnClickListener {
            onAddToPlaylistListener(song)
        }
    }

    override fun getItemCount(): Int = songs.size

    // Update the button's state
    private fun updateButtonState(button: ImageButton, isAdded: Boolean) {
        val context = button.context

        if (isAdded) {
            button.backgroundTintList = ContextCompat.getColorStateList(context, android.R.color.holo_red_light)
        } else {
            button.backgroundTintList = ContextCompat.getColorStateList(context, android.R.color.white)
            button.setBackgroundResource(R.drawable.not_add)
        }

        button.setImageResource(R.drawable.add)
    }

    // Get the playlist
    fun getPlaylist(): List<Map<String, String>> = playlist
}
