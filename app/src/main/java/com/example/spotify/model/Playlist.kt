package com.example.spotify.model

data class Playlist(
    val id_playlist: Int,
    val id_user: Int,  // Foreign key ke pengguna
    val id_song: Int,  // Foreign key ke lagu
    val name_playlist: String
)