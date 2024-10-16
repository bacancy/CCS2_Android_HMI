package com.bacancy.ccs2androidhmi.network

import com.bacancy.ccs2androidhmi.network.models.LoginRequest
import com.bacancy.ccs2androidhmi.network.models.LoginResponse
import com.bacancy.ccs2androidhmi.util.CommonUtils.LOGIN_ENDPOINT
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST(LOGIN_ENDPOINT)
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

}