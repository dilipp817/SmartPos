package com.autobill.smartpos.data

import com.autobill.smartpos.data.remote.FoodApiService
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object NetworkModule {

    fun provideMoshi(): Moshi = Moshi.Builder().build()

    // Modified: Accept isDebug parameter instead of using BuildConfig directly
    fun provideOkHttpClient(isDebug: Boolean = true): OkHttpClient {
        val logger = HttpLoggingInterceptor().apply {
            level = if (isDebug) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val builder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(logger)

        // For development only: Trust self-signed certificates
        /**
         * certificates are only for localhost. Don't use in production
         * Don't use it once backend is deployed on server
          */

        if (isDebug) {
            try {
                val trustAllCerts = arrayOf<TrustManager>(
                    object : X509TrustManager {
                        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                        override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {}
                        override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {}
                    }
                )

                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, SecureRandom())
                val sslSocketFactory = sslContext.socketFactory

                builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                builder.hostnameVerifier { _, _ -> true }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return builder.build()
    }

    // Modified: Accept baseUrl as parameter instead of using BuildConfig.BASE_URL
    fun provideRetrofit(baseUrl: String, isDebug: Boolean = true): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(provideOkHttpClient(isDebug))
        .addConverterFactory(MoshiConverterFactory.create(provideMoshi()))
        .build()

    // Modified: Accept baseUrl as parameter
    fun provideFoodApiService(baseUrl: String, isDebug: Boolean = true): FoodApiService =
        provideRetrofit(baseUrl, isDebug).create(FoodApiService::class.java)
}