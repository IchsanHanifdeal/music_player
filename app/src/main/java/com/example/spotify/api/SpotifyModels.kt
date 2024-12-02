package com.example.spotify.api

data class SpotifyTracksResponse(
    val items: List<Track>
)

data class Track(
    val name: String,
    val album: Album,
    val artists: List<Artist>,
    val previewurl: String?
)

data class Album(
    val name: String,
    val images: List<Image>
)

data class Artist(
    val name: String
)

data class Image(
    val url: String
)
