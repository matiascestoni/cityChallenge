package com.test.citychallenge.common

sealed class Response<T> {
    class Success<T>(val result: T) : Response<T>()
    class Error<T>(val message: String) : Response<T>()
}