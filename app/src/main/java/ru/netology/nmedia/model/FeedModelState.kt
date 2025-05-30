package ru.netology.nmedia.model

data class FeedModelState(
    val loading: Boolean = false,
    val error: Boolean = false,
    val refreshing: Boolean = false,
    val loginError: Boolean = false,
    val passwordError: Boolean = false,
)
