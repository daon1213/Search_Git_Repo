package com.daon.gitrepo_part4_05

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.daon.gitrepo_part4_05.Utility.RetrofitUtil
import com.daon.gitrepo_part4_05.databinding.ActivitySearchBinding
import com.daon.gitrepo_part4_05.views.RepositoryAdapter
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class SearchActivity: AppCompatActivity(), CoroutineScope {

    private val binding by lazy { ActivitySearchBinding.inflate(layoutInflater) }
    private lateinit var adapter: RepositoryAdapter

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initAdapter()
        initViews()
        bindViews()
    }

    private fun initViews() = with(binding) {
        emptyResultTextView.isVisible = false
        recyclerView.adapter = adapter
    }

    private fun initAdapter() = with(binding) {
        adapter = RepositoryAdapter { repository ->
            startActivity(
                Intent(this@SearchActivity, RepoActivity::class.java).apply {
                    putExtra(REPOSITORY_OWNER_KEY, repository.owner.login)
                    putExtra(REPOSITORY_NAME_KEY, repository.name)
                }
            )
        }
    }

    private fun bindViews() = with(binding) {
        searchButton.setOnClickListener {
            searchRepository(searchEditView.text.toString())
        }
    }

    private fun searchRepository(keyword: String) = launch(coroutineContext) {
        withContext(Dispatchers.IO) {
            val response = RetrofitUtil.searchApiService.searchRepositories(keyword)

            if (response.isSuccessful) {
                response.body()?.let {
                    withContext(Dispatchers.Main) {
                        adapter.setRepositories(it.items)
                    }
                }
            }
        }
    }

    companion object {
        private const val REPOSITORY_OWNER_KEY = "REPOSITORY_OWNER_KEY "
        private const val REPOSITORY_NAME_KEY = "REPOSITORY_NAME_KEY"
    }
}