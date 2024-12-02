package com.example.spotify

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.spotify.api.SpotifyTracksResponse


class SpotifyAdapter(
    private val trackList: List<SpotifyTracksResponse.Track>,
    private val onPlayMusic: (SpotifyTracksResponse.Track) -> Unit
) : RecyclerView.Adapter<SpotifyAdapter.TrackViewHolder>() {

    inner class TrackViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val thumbnail: ImageView = view.findViewById(R.id.song_thumbnail)
        val name: TextView = view.findViewById(R.id.song_name)
        val artist: TextView = view.findViewById(R.id.artist_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = trackList[position]
        holder.name.text = track.name
        holder.artist.text = track.artists.joinToString(", ") { it.name }

        // Load thumbnail
        Glide.with(holder.itemView.context)
            .load(track.album.images.firstOrNull()?.url)
            .placeholder(R.drawable.ic_music_logo)
            .into(holder.thumbnail)

        // Play music on item click
        holder.itemView.setOnClickListener {
            onPlayMusic(track)
        }
    }

    override fun getItemCount(): Int = trackList.size
}