package ru.netology.nmedia.auth

import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.netology.nmedia.repository.TokenRepository

@Singleton
class AppAuth @Inject constructor(
    private val tokenRepository: TokenRepository
) {

    private val _authStateFlow: MutableStateFlow<AuthState>

    init {
        val id = tokenRepository.userId
        val token = tokenRepository.token

        if (id == 0L || token == null) {
            _authStateFlow = MutableStateFlow(AuthState())
            tokenRepository.clear()
        } else {
            _authStateFlow = MutableStateFlow(AuthState(id, token))
        }
    }

    val authStateFlow: StateFlow<AuthState> = _authStateFlow.asStateFlow()


    @Synchronized
    fun setAuth(id: Long, token: String) {
        _authStateFlow.value = AuthState(id, token)
        tokenRepository.userId = id
        tokenRepository.token = token
    }


    @Synchronized
    fun removeAuth() {
        _authStateFlow.value = AuthState()
        tokenRepository.clear()
    }

}

data class AuthState(val id: Long = 0, val token: String? = null)