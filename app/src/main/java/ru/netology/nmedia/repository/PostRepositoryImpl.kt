package ru.netology.nmedia.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okio.IOException
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Ad
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.model.PhotoModel
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random


@Singleton
class PostRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val apiService: ApiService,
    remoteMediator: PostRemoteMediator
) : PostRepository {

    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(pageSize = 5, enablePlaceholders = false),
        pagingSourceFactory = postDao::pagingSource,
        remoteMediator = remoteMediator
    ).flow.map { pagingData ->
        pagingData.map(PostEntity::toDto)
            .insertSeparators { previuos, next ->
                if (previuos?.id?.rem(5) == 0L) {
                    Ad(Random.nextLong(), "figma.jpg")
                } else {
                    null
                }
            }
    }



override suspend fun getAll() {
    try {
        val response = apiService.getAll()
        if (!response.isSuccessful) {
            throw ApiError(response.message())
        }

        val body = response.body() ?: throw ApiError(response.message())
        if (postDao.isEmpty()) {
            postDao.insert(body.toEntity(false))
            postDao.readNewPost()
        }
        if (body.size > postDao.countPosts()) {
            val notInRoomPosts = body.takeLast(body.size - postDao.countPosts())
            postDao.insert(notInRoomPosts.toEntity(true))
        }
    } catch (e: IOException) {
        throw NetworkError
    } catch (e: Exception) {
        throw UnknownError
    }
}

override suspend fun save(post: Post) {
    try {
        val response = apiService.save(post)
        if (!response.isSuccessful) {
            throw ApiError(response.message())
        }

        val body = response.body() ?: throw ApiError(response.message())
        postDao.insert(PostEntity.fromDto(body))
    } catch (e: IOException) {
        throw NetworkError
    } catch (e: Exception) {
        throw UnknownError
    }
}

override suspend fun removeById(id: Long) {
    try {
        postDao.removeById(id)
        val response = apiService.removeById(id)
        if (!response.isSuccessful) {
            throw ApiError(response.message())
        }


    } catch (e: IOException) {
        throw NetworkError
    } catch (e: Exception) {
        throw UnknownError
    }
}

override suspend fun likeById(id: Long) {
    try {
        postDao.likeById(id)
        val response = apiService.likeById(id)
        if (!response.isSuccessful) {
            throw ApiError(response.message())
        }


    } catch (e: IOException) {
        postDao.dislikeById(id)
        throw NetworkError
    } catch (e: Exception) {
        postDao.dislikeById(id)
        throw UnknownError
    }
}

override suspend fun dislikeById(id: Long) {
    try {
        postDao.dislikeById(id)
        val response = apiService.dislikeById(id)
        if (!response.isSuccessful) {
            throw ApiError(response.message())
        }


    } catch (e: IOException) {
        postDao.likeById(id)
        throw NetworkError
    } catch (e: Exception) {
        postDao.likeById(id)
        throw UnknownError
    }
}

override suspend fun saveWithAttachment(post: Post, photoModel: PhotoModel) {
    try {
        val media = upload(photoModel.file)
        val response = apiService.save(
            post.copy(
                attachment = Attachment(
                    url = media.id,
                    AttachmentType.IMAGE
                )
            )
        )
        if (!response.isSuccessful) {
            throw ApiError(response.message())
        }

        val body = response.body() ?: throw ApiError(response.message())
        postDao.insert(PostEntity.fromDto(body))
    } catch (e: IOException) {
        throw NetworkError
    } catch (e: Exception) {
        throw UnknownError
    }
}

override fun getNewerCount(id: Long): Flow<Int> = flow {
    while (true) {
        delay(10_000L)
        val response = apiService.getNewer(id)
        if (!response.isSuccessful) {
            throw ApiError(response.message())
        }
        val body = response.body() ?: throw ApiError(response.message())

        postDao.insert(body.toEntity(false))
        val newPosts = postDao.getNewer()
        emit(newPosts.size)
    }
}
    .catch { e -> AppError.from(e) }
    .flowOn(Dispatchers.Default)


override suspend fun readPosts() {
    postDao.readNewPost()

}


private suspend fun upload(file: File): Media {
    try {
        val response = apiService.upload(
            MultipartBody.Part.createFormData(
                "file",
                file.name,
                file.asRequestBody()
            )
        )

        if (!response.isSuccessful) {
            throw ApiError(response.message())
        }

        return response.body() ?: throw ApiError(response.message())
    } catch (e: IOException) {
        throw NetworkError
    } catch (e: Exception) {
        throw UnknownError
    }
}

suspend fun authUser(login: String, pass: String): ru.netology.nmedia.dto.User {
    try {
        val response = apiService.updateUser(login, pass)
        if (!response.isSuccessful) {
            throw ApiError(response.message())
        }

        return response.body() ?: throw ApiError(response.message())
    } catch (e: IOException) {
        throw NetworkError
    } catch (e: Exception) {
        throw UnknownError
    }
}
}
