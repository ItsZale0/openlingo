package com.alessandro.openlingo.data.remote

import okhttp3.Response

interface FeedService {

    suspend fun getRaw(url: String): Response

    suspend fun head(url: String): Response
}