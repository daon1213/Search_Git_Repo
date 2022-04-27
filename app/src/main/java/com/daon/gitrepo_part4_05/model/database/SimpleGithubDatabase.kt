package com.daon.gitrepo_part4_05.model.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.daon.gitrepo_part4_05.model.dao.RepositoryDao
import com.daon.gitrepo_part4_05.model.entity.GithubRepoEntity

@Database(entities = [GithubRepoEntity::class], version = 1)
abstract class SimpleGithubDatabase: RoomDatabase() {

    abstract fun repositoryDao(): RepositoryDao
}