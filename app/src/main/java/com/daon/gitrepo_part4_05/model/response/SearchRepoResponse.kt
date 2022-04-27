package com.daon.gitrepo_part4_05.model.response

import com.daon.gitrepo_part4_05.model.entity.GithubRepoEntity

data class SearchRepoResponse(
    val totalCount: Int,
    val items: List<GithubRepoEntity>
)