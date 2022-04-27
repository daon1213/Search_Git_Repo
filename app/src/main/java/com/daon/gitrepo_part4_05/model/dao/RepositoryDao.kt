package com.daon.gitrepo_part4_05.model.dao

import androidx.room.*
import com.daon.gitrepo_part4_05.model.entity.GithubRepoEntity

@Dao
interface RepositoryDao {

    @Insert
    suspend fun insert(repo: GithubRepoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(repoList: List<GithubRepoEntity>)

    @Query("SELECT * FROM githubrepository")
    suspend fun getHistory(): List<GithubRepoEntity>

    @Query("DELETE FROM githubrepository")
    suspend fun clearAll()

    @Query("DELETE FROM githubrepository WHERE fullName = :fullName")
    suspend fun delRepository(fullName: String)

    @Query("SELECT * fROM githubrepository WHERE fullName = :fullName")
    suspend fun getRepository(fullName: String): GithubRepoEntity?
}