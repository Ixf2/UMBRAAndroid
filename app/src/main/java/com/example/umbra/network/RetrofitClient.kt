
package com.example.umbra.network

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://127.0.0.1:8080/"
    private val cookieJar = object : CookieJar {

        private val cookies = mutableMapOf<String, Cookie>()

        override fun saveFromResponse(url: HttpUrl, receivedCookies: List<Cookie>) {
            synchronized(cookies) {
                receivedCookies.forEach { cookie ->
                    val key = "${cookie.name}|${cookie.domain}|${cookie.path}"

                    if (cookie.expiresAt > System.currentTimeMillis()) {
                        cookies[key] = cookie
                    } else {
                        cookies.remove(key)
                    }
                }
            }
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return synchronized(cookies) {
                cookies.values.filter { cookie ->
                    cookie.expiresAt > System.currentTimeMillis() &&
                            cookie.matches(url)
                }
            }
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
