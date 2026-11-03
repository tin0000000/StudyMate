package com.st10457568.studymate.network

import com.st10457568.studymate.model.Quote
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class QuoteClient {
    fun fetchDailyQuote(): Quote {
        // The dashboard calls this on a worker thread; timeouts keep a slow endpoint from hanging it.
        val connection = URL("https://zenquotes.io/api/today").openConnection() as HttpURLConnection
        connection.connectTimeout = 5_000
        connection.readTimeout = 5_000
        connection.requestMethod = "GET"

        return connection.inputStream.bufferedReader().use { reader ->
            val body = reader.readText()
            val first = JSONArray(body).getJSONObject(0)
            Quote(
                text = first.optString("q", FALLBACK.text),
                author = first.optString("a", FALLBACK.author)
            )
        }
    }

    companion object {
        val FALLBACK = Quote(
            text = "Small study wins become real progress.",
            author = "StudyMate"
        )
    }
}
