package ru.netology.nmedia.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import ru.netology.nmedia.api.PostsApiService
import ru.netology.nmedia.dto.User
import ru.netology.nmedia.model.FeedModelState


@HiltViewModel
class SignInViewModel @Inject constructor(
    private val postsApiService: PostsApiService
) : ViewModel() {

    private val _data = MutableLiveData<User>()
    val data: LiveData<User>
        get() = _data

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    fun loginAttempt(login: String, password: String, @ApplicationContext context : Context) = viewModelScope.launch {
        val response = postsApiService.updateUser(login, password)
        if (!response.isSuccessful) {
            Toast.makeText(context, "Incorrect login or password", Toast.LENGTH_LONG).show()
            return@launch
        }
        val userKey: User = response.body() ?: throw RuntimeException("Body is null")
        _data.value = userKey
    }

}

