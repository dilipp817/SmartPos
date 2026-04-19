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
            // Launch on the injected app-lifetime scope — never blocks the OkHttp thread.
            // NonCancellable ensures the session wipe completes even under cancellation.
            appScope.launch(NonCancellable) { sessionDataStore.clearUser() }

            // Return the 401 response as-is — session is already cleared, and
            // MainViewModel.sessionState will emit Resolved(null) → Login screen.
            //
            // ⚠️ DO NOT close + re-proceed here:
            //   1. POST/PUT request bodies are single-read streams; re-proceeding after
            //      the body has been consumed throws IllegalStateException.
            //   2. The second call would also return 401 (token is gone), wasting a
            //      round-trip and potentially confusing the caller with a different error.
        }

        return response
    }
}



