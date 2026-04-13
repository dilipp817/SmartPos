package com.autobill.smartpos.data.remote

import com.autobill.smartpos.data.local.SessionDataStore
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp interceptor that attaches the stored JWT to every request as a Bearer token.
 *
 * getToken() is now a plain synchronous call (SharedPreferences read via Android Keystore)
 * so no runBlocking or coroutine bridge is required on the network thread.
 *
 * Auth endpoints (login, validate) work without a token — the interceptor simply skips
 * the header when no session exists.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionDataStore: SessionDataStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = sessionDataStore.getToken()
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}
