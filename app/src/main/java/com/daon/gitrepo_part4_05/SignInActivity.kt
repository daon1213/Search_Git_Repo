package com.daon.gitrepo_part4_05

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.isVisible
import androidx.viewbinding.BuildConfig
import com.daon.gitrepo_part4_05.BuildConfig.GITHUB_CLIENT_ID
import com.daon.gitrepo_part4_05.BuildConfig.GITHUB_CLIENT_SECRET
import com.daon.gitrepo_part4_05.Utility.AuthTokenProvider
import com.daon.gitrepo_part4_05.Utility.RetrofitUtil
import com.daon.gitrepo_part4_05.databinding.ActivitySignInBinding
import com.daon.gitrepo_part4_05.model.Url.GITHUB_CLIENT_ID
import com.daon.gitrepo_part4_05.model.Url.GITHUB_CLIENT_SECRET
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class SignInActivity : AppCompatActivity(), CoroutineScope {

    private val binding by lazy { ActivitySignInBinding.inflate(layoutInflater) }
    private val authTokenProvider = AuthTokenProvider(this)

    private val job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (checkAuthCodeExist()) {
            launchMainActivity()
        } else {
            initViews()
        }
    }

    private fun checkAuthCodeExist() = authTokenProvider.token.isNullOrEmpty().not()

    private fun launchMainActivity() {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }

    private fun initViews() = with(binding) {
        loginButton.setOnClickListener {
            loginGitHub()
        }
    }

    // todo https://github.com/login/oauth/authorize?client_id=GITHUB_CLIENT_ID
    private fun loginGitHub() {
        val loginUri = Uri.Builder().scheme("https").authority("github.com")
            .appendPath("login")
            .appendPath("oauth")
            .appendPath("authorize")
            .appendQueryParameter("client_id", com.daon.gitrepo_part4_05.BuildConfig.GITHUB_CLIENT_ID)
            .build()

        // browser library: 넘겨준 URL 의 브라우저 실행
        CustomTabsIntent.Builder().build().also {
            it.launchUrl(this, loginUri)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        // access token 요청에 필요한 code
        intent?.data?.getQueryParameter("code")?.let {
            // todo github 에 access token 요청
            launch(coroutineContext) {
                showProgress()
                getAccessToken(it)
                dismissProgress()
            }
        }
    }

    private suspend fun showProgress() = withContext(coroutineContext) {
        with(binding) {
            progressbar.isVisible = true
            progressTextView.isVisible = true
        }
    }

    private suspend fun dismissProgress() = withContext(coroutineContext) {
        with(binding) {
            progressbar.isVisible = false
            progressTextView.isVisible = false
        }
    }

    private suspend fun getAccessToken(code: String) = withContext(Dispatchers.IO) {
        val response = RetrofitUtil.authApiService.getAccessToken(
            clientId = com.daon.gitrepo_part4_05.BuildConfig.GITHUB_CLIENT_ID,
            clientSecret = com.daon.gitrepo_part4_05.BuildConfig.GITHUB_CLIENT_SECRET,
            code = code
        )
        if (response.isSuccessful) {
            val accessToken = response.body()?.accessToken ?: ""

            if (accessToken.isNotEmpty()) {
                authTokenProvider.updateToken(accessToken)
                launchMainActivity()
            } else {
                Toast.makeText(this@SignInActivity, "Fail to get Access Token", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}