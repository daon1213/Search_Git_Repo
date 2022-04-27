package com.daon.gitrepo_part4_05.model.response

data class GithubAccessTokenResponse (
    val accessToken: String,
    val scope: String,
    val tokenType: String
)