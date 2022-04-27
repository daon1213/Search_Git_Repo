package com.daon.gitrepo_part4_05

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlin.coroutines.CoroutineContext
import android.widget.Toast
import androidx.core.view.isVisible
import com.daon.gitrepo_part4_05.Utility.RetrofitUtil
import com.daon.gitrepo_part4_05.databinding.ActivityRepoBinding
import com.daon.gitrepo_part4_05.extensions.loadCenterInside
import com.daon.gitrepo_part4_05.model.database.DataBaseProvider
import com.daon.gitrepo_part4_05.model.entity.GithubRepoEntity
import kotlinx.coroutines.*

class RepoActivity : AppCompatActivity(), CoroutineScope {

    private val binding by lazy { ActivityRepoBinding.inflate(layoutInflater) }

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val repositoryDao by lazy {
        DataBaseProvider.provideDB(applicationContext)
            .repositoryDao()
    }

    private var currentRepositoryEntity: GithubRepoEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val ownerLogin = intent.getStringExtra(REPOSITORY_OWNER_KEY) ?: kotlin.run {
            toast("Repository Owner 이름이 없습니다.")
            finish()
        }
        val repoName = intent.getStringExtra(REPOSITORY_NAME_KEY) ?: kotlin.run {
            toast("Repository 이름이 없습니다.")
            finish()
        }

        launch {
            loadRepository(ownerLogin.toString(), repoName.toString())?.let {
                setData(it)
            } ?: kotlin.run {
                toast("Repository 정보가 없습니다.")
            }
        }
        showLoading(true)
    }

    private suspend fun loadRepository(owner: String, repoName: String): GithubRepoEntity? =
        withContext(Dispatchers.IO) {
            val response = RetrofitUtil.searchApiService.getRepository(owner, repoName)

            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        }

    @SuppressLint("SetTextI18n")
    private fun setData(repository: GithubRepoEntity) = with(binding) {
        showLoading(false)
        ownerProfileImageView.loadCenterInside(repository.owner.avatarUrl, 42f)
        ownerNameAndRepoNameTextView.text = "${repository.owner.login}/${repository.fullName}"
        stargazersCountTextView.text = repository.stargazersCount.toString()

        repository.language?.let {
            langTextView.isVisible = true
            langTextView.text = repository.language
        } ?: kotlin.run {
            langTextView.isVisible = false
            langTextView.text = ""
        }
        descriptionTextView.text = repository.description
        updateTimeTextView.text = repository.updatedAt
        setLikeState(repository)
    }

    private fun setLikeState(githubRepoEntity: GithubRepoEntity) = launch {
        withContext(Dispatchers.IO) {
            val repository = repositoryDao.getRepository(githubRepoEntity.fullName)
            val isLike = repository != null

            withContext(Dispatchers.Main) {
                currentRepositoryEntity = githubRepoEntity
                setLikeImage(isLike)
                binding.likeButton.setOnClickListener {
                    insertRepositoryToDB(it.tag as Boolean)
                }
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setLikeImage(isLike: Boolean) = with(binding) {
        if (isLike) {
            likeButton.background = getDrawable(R.drawable.background_like_button_selected)
            likeButton.setTextColor(Color.WHITE)
            likeButton.text = "LIKE"
        } else {
            likeButton.background = getDrawable(R.drawable.background_like_button)
            likeButton.setTextColor(Color.BLACK)
            likeButton.text = "DISLIKE"
        }
        likeButton.tag = isLike
    }

    private fun insertRepositoryToDB(isLike: Boolean) = launch(coroutineContext) {
        withContext(Dispatchers.IO) {
            currentRepositoryEntity?.let {
                if (isLike) { // like 상태였다면 DB 에서 제거
                    repositoryDao.delRepository(it.fullName)
                } else { // dislike 상태였다면 DB 에 추가
                    repositoryDao.insert(it)
                }

                withContext(Dispatchers.Main) {
                    setLikeImage(isLike.not())
                }
            }
        }
    }

    private fun showLoading(isShown: Boolean) = with(binding) {
        progressbar.isVisible = isShown
    }

    private fun Context.toast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    companion object {
        private const val REPOSITORY_OWNER_KEY = "REPOSITORY_OWNER_KEY "
        private const val REPOSITORY_NAME_KEY = "REPOSITORY_NAME_KEY"
    }
}