package ru.netology.nmedia.error

import java.io.IOError
import java.sql.SQLException

sealed class AppError(var code: String) : RuntimeException() {
    companion object {
        fun from(e: Throwable): AppError = when(e) {
            is AppError -> e
            is SQLException -> DbError
            is IOError -> NetworkError
            else -> UnknownError
        }
    }
}

class ApiError(code: String) : AppError(code)
object NetworkError : AppError("error_network")
object DbError: AppError("error_db")
object UnknownError : AppError("error_unknown")