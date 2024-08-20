package com.lukemartinrecords.encorehub.api
import com.adamratzman.spotify.SpotifyAppApi
import com.adamratzman.spotify.models.SpotifyPublicUser
import com.adamratzman.spotify.models.SpotifySearchResult
import com.adamratzman.spotify.spotifyAppApi
import com.adamratzman.spotify.utils.Market
import com.lukemartinrecords.encorehub.Constants

//courtesy of chvia223
//https://github.com/chvia223/searchSpotify-kotlin/blob/main/src/main/kotlin/SpotifyApiHandler.kt
class SpotifyApiHandler {
    private val clientID = "45e9e8cc4cac4ccd9405cd26eab04099"
    private val clientSecret = Constants.SECRET_KEY
    private var api: SpotifyAppApi? = null

    init {

    }

    /// Pulls the developer ClientID and ClientSecret tokens provided
    /// by Spotify and builds them into an object that can easily
    /// call public Spotify APIs.
    suspend fun buildSearchApi() {
        api = spotifyAppApi(clientID, clientSecret).build()
    }

    // Performs Spotify database query for queries related to user information. Returns
    // the results as a SpotifyPublicUser object.
    suspend fun userSearch(userQuery: String): SpotifyPublicUser? {
        return api!!.users.getProfile(userQuery)
    }

    // Performs Spotify database query for queries related to track information. Returns
    // the results as a SpotifySearchResult object.
    suspend fun trackSearch(searchQuery: String): SpotifySearchResult {
        return api!!.search.searchAllTypes(searchQuery, 10, 1, market = Market.US)
    }

}