package com.example.bottomnavitemplate

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface SignUpService {
    @POST("/users")
    fun signUp(@Body user: User): Call<AuthResponse>
}