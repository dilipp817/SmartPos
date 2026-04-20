package com.autobill.smartpos.data.di

import android.content.Context
import com.autobill.smartpos.data.BuildConfig
import com.autobill.smartpos.data.remote.AuthApiService
import com.autobill.smartpos.data.remote.AuthInterceptor
import com.autobill.smartpos.data.remote.BillApiService
import com.autobill.smartpos.data.remote.CategoryApiService
import com.autobill.smartpos.data.remote.FeatureFlagApiService
import com.autobill.smartpos.data.remote.FoodApiService
import com.autobill.smartpos.data.remote.OrderApiService
import com.autobill.smartpos.data.remote.PaymentApiService
import com.autobill.smartpos.data.remote.RestaurantApiService
import com.autobill.smartpos.data.remote.TableApiService
import com.autobill.smartpos.data.remote.UnauthorizedInterceptor
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        unauthorizedInterceptor: UnauthorizedInterceptor,
        @ApplicationContext context: Context,
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            // Render free-tier cold start: 25–35s — contract §12.2
            .connectTimeout(40, TimeUnit.SECONDS)
            .readTimeout(40, TimeUnit.SECONDS)
            .writeTimeout(40, TimeUnit.SECONDS)

        // Auth interceptor — attaches Bearer token to every request
        builder.addInterceptor(authInterceptor)
        // Unauthorized interceptor — clears session and triggers re-login on 401
        builder.addInterceptor(unauthorizedInterceptor)
        // Chucker — in-app HTTP inspector (dev flavor: full UI; other flavors: no-op)
        builder.addInterceptor(ChuckerInterceptor.Builder(context).build())

        if (BuildConfig.DEBUG) {
            // Add logging interceptor for debug builds
            builder.addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })

            // Trust all certificates (self-signed) in debug builds only
            // WARNING: This is ONLY for development/testing with self-signed certificates
            // NEVER use this in production builds
            try {
                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                    override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {}
                    override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {}
                })

                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, java.security.SecureRandom())

                // Cast to X509TrustManager properly
                val x509TrustManager = trustAllCerts[0] as X509TrustManager
                builder.sslSocketFactory(sslContext.socketFactory, x509TrustManager)
                builder.hostnameVerifier { _, _ -> true }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideFoodApiService(retrofit: Retrofit): FoodApiService {
        return retrofit.create(FoodApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideOrderApiService(retrofit: Retrofit): OrderApiService {
        return retrofit.create(OrderApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideTableApiService(retrofit: Retrofit): TableApiService {
        return retrofit.create(TableApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideBillApiService(retrofit: Retrofit): BillApiService {
        return retrofit.create(BillApiService::class.java)
    }

    @Provides
    @Singleton
    fun providePaymentApiService(retrofit: Retrofit): PaymentApiService {
        return retrofit.create(PaymentApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideCategoryApiService(retrofit: Retrofit): CategoryApiService {
        return retrofit.create(CategoryApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideRestaurantApiService(retrofit: Retrofit): RestaurantApiService {
        return retrofit.create(RestaurantApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideFeatureFlagApiService(retrofit: Retrofit): FeatureFlagApiService {
        return retrofit.create(FeatureFlagApiService::class.java)
    }
}