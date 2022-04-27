package com.daon.gitrepo_part4_05.views

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.daon.gitrepo_part4_05.databinding.ItemRepositoryBinding
import com.daon.gitrepo_part4_05.extensions.loadCenterInside
import com.daon.gitrepo_part4_05.model.entity.GithubRepoEntity


class RepositoryAdapter(
    val repositoryClickListener: (GithubRepoEntity) -> Unit
): RecyclerView.Adapter<RepositoryAdapter.RepositoryViewHolder>() {

    private var repositories: List<GithubRepoEntity> = emptyList()

    inner class RepositoryViewHolder(
        private val binding: ItemRepositoryBinding
    ): RecyclerView.ViewHolder(binding.root) {

        fun bind(repository: GithubRepoEntity) = with(binding) {
            if (repository.owner.avatarUrl.isNotEmpty())
                ownerProfileImageView.loadCenterInside(repository.owner.avatarUrl, 24f)
            ownerNameTextView.text = repository.owner.login
            nameTextView.text = repository.fullName
            subtextTextView.text = repository.description
            stargazersCountTextView.text = repository.stargazersCount.toString()

            repository.language?.let {
                langTextView.isVisible = true
                langTextView.text = it
            } ?: kotlin.run {
                langTextView.isVisible = false
                langTextView.text = ""
            }

            root.setOnClickListener { repositoryClickListener(repository) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepositoryViewHolder {
        return RepositoryViewHolder(
            ItemRepositoryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: RepositoryViewHolder, position: Int) {
        holder.bind(repositories[position])
    }

    override fun getItemCount() = repositories.size

    @SuppressLint("NotifyDataSetChanged")
    fun setRepositories(repositories: List<GithubRepoEntity>) {
        this.repositories = repositories
        notifyDataSetChanged()
    }
}