package com.abd.dev.album.domain.repository

import com.abd.dev.album.data.local.db.AlbumDatabase
import com.abd.dev.album.data.local.model.AlbumEntity
import com.abd.dev.album.data.remote.api.AlbumApi
import com.abd.dev.album.domain.model.Album
import javax.inject.Inject


class AlbumRepositoryImpl @Inject constructor(
    private val api: AlbumApi,
    private val localDataSource: AlbumDatabase,
    private val mapperAlbum: AlbumMappers
) : AlbumRepository {

    override suspend fun loadAlbums(): Result<List<Album>> {
        val remoteAlbums = loadRemoteAlbums()
        return if (remoteAlbums.isSuccess) {
            localDataSource.albumDao().insertAllAlbum(remoteAlbums.getOrDefault(emptyList()))
            Result.success(
                mapperAlbum.localToDomainMapper.mapList(
                    localDataSource.albumDao().findAllAlbums()
                )
            )
        } else {
            Result.failure(remoteAlbums.exceptionOrNull() ?: Exception())
        }
    }

    private suspend fun loadRemoteAlbums(): Result<List<AlbumEntity>> {
        return try {
            val response = api.getAlbums()
            if (response.isSuccessful && response.body() != null) {
                Result.success(
                    mapperAlbum
                        .remoteToLocalAlbumMapper
                        .mapList(response.body()!!)
                )
            } else {
                Result.failure(Exception())
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}