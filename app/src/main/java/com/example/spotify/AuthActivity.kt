package com.example.spotify

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class AuthActivity : AppCompatActivity() {
    companion object {
        private const val CLIENT_ID = "YOUR_CLIENT_ID"
        private const val REDIRECT_URI = "yourapp://callback"
        private const val AUTH_URL = "https://accounts.spotify.com/authorize"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authUri = Uri.parse(AUTH_URL).buildUpon()
            .appendQueryParameter("client_id", CLIENT_ID)
            .appendQueryParameter("response_type", "token")
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .appendQueryParameter("scope", "user-top-read")
            .build()

        val intent = Intent(Intent.ACTION_VIEW, authUri)
        startActivity(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val uri = intent?.data ?: return

        if (uri.toString().startsWith(REDIRECT_URI)) {
            val token = uri.fragment?.split("&")?.find { it.startsWith("access_token=") }
                ?.split("=")?.get(1)
            Log.d("SpotifyAuth", "Access Token: $token")

            val mainIntent = Intent(this, HomeActivity::class.java)
            mainIntent.putExtra("ACCESS_TOKEN", token)
            startActivity(mainIntent)
            finish()
        }
    }
}
