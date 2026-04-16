package com.autobill.smartpos.data.remote

import com.autobill.smartpos.data.di.ApplicationScope
import com.autobill.smartpos.data.local.SessionDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp interceptor that handles HTTP 401 Unauthorized responses.
 *
 * When any API call (including billing/payment) returns 401:
 *  1. The stored JWT has expired or been invalidated server-side.
 *  2. This interceptor clears the entire session (token + metadata).
 *  3. [SessionDataStore.observeUser] emits null.
 *  4. [MainViewModel.sessionState] resolves to [SessionResult.Resolved(null)].
 *  5. [MainActivity] recreates [AppNavHost] with Login as the start destination.
 *
 * This means a kitchen staff member in the middle of a payment who has a stale session
 * will be redirected to Login automatically — no cryptic "Unauthorised" error shown.
 *
 * Execution order in OkHttpClient:
 *   AuthInterceptor (attaches token) → UnauthorizedInterceptor (handles 401) → network
 *
 * Testability:
 *   Inject a [kotlinx.coroutines.test.TestScope] for [appScope] so tests can call
 *   advanceUntilIdle() and assert clearUser() completed deterministically.
 */
@Singleton
class UnauthorizedInterceptor @Inject constructor(
    private val sessionDataStore: SessionDataStore,
    @ApplicationScope private val appScope: CoroutineScope,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        if (response.code == 401) {
            // Close response body before doing any I/O to avoid leaking the connection.
            response.close()
            // Launch on the injected app-lifetime scope — never blocks the OkHttp thread.
            // NonCancellable ensures the session wipe completes even under cancellation.
            appScope.launch(NonCancellable) { sessionDataStore.clearUser() }

            // Re-proceed the original request — now without a token.
            // The result will be another 401 (or redirect), which propagates back
            // to the repository as a Result.Failure — but the session is already cleared,
            // so the UI redirects to Login before the error is even shown.
            return chain.proceed(chain.request())
        }

        return response
    }
}



