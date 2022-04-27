package com.daon.gitrepo_part4_05

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import com.daon.gitrepo_part4_05.databinding.ActivityMainBinding
import com.daon.gitrepo_part4_05.model.database.DataBaseProvider
import com.daon.gitrepo_part4_05.model.entity.GithubOwner
import com.daon.gitrepo_part4_05.model.entity.GithubRepoEntity
import com.daon.gitrepo_part4_05.views.RepositoryAdapter
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext

class MainActivity: AppCompatActivity(), CoroutineScope {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var adapter: RepositoryAdapter

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val repositoryDao by lazy {
        DataBaseProvider.provideDB(applicationContext)
            .repositoryDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

//        launch {
//            addMockData()
//            val githubRepositories = loadGithubRepository()
//            withContext(Dispatchers.Main) {
//                Log.d("repositories >", githubRepositories.toString())
//            }
//        }

        initAdapter()
        initViews()
    }

    override fun onResume() {
        super.onResume()
        loadRepositories()
    }

    private fun initViews() = with(binding) {
        searchButton.setOnClickListener {
            startActivity(
                Intent(this@MainActivity, SearchActivity::class.java)
            )
        }
        recyclerView.adapter = adapter
    }

    private fun initAdapter() {
        adapter = RepositoryAdapter { repository ->
            startActivity(
                Intent(this@MainActivity, RepoActivity::class.java).apply {
                    putExtra(REPOSITORY_OWNER_KEY, repository.owner.login)
                    putExtra(REPOSITORY_NAME_KEY, repository.name)
                }
            )
        }
    }

    private fun loadRepositories() = launch(coroutineContext) {
        withContext(Dispatchers.IO) {
            val repositories = repositoryDao.getHistory()
            withContext(Dispatchers.Main) {
                setData(repositories)
            }
        }
    }

    private fun setData(repositories: List<GithubRepoEntity>) = with(binding) {
        if (repositories.isEmpty()) {
            emptyResultTextView.isVisible = true
            recyclerView.isVisible = false
        } else {
            adapter.setRepositories(repositories)
            emptyResultTextView.isVisible = false
            recyclerView.isVisible = true
        }
    }

    private suspend fun addMockData() = withContext(Dispatchers.IO){
        var mocData = (0 until 10).map {
            GithubRepoEntity(
                name = "repo $it",
                fullName = "name $it",
                owner = GithubOwner(
                    "login",
                    ""
                ),
                description = null,
                language = null,
                updatedAt = Date().toString(),
                stargazersCount = it
            )
        }

        repositoryDao.insertAll(mocData)
    }

    private suspend fun loadGithubRepository() = withContext(Dispatchers.IO){
        return@withContext repositoryDao.getHistory()
    }

    companion object {
        private const val REPOSITORY_OWNER_KEY = "REPOSITORY_OWNER_KEY "
        private const val REPOSITORY_NAME_KEY = "REPOSITORY_NAME_KEY"
    }
}