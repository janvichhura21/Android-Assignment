package com.example.assignment.network

import com.example.assignment.model.Phone
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {
    companion object {
        val BASE_URL="http://143.244.138.96:2110/api/"
    }

    @FormUrlEncoded
    @POST("status")
     fun setPhone(
        @Field("internetconnected")internetconnected:Boolean,
        @Field("charging")charging:Boolean,
        @Field("battery")battery:String,
        @Field("location")location:String,
     ): Call<Phone>
}