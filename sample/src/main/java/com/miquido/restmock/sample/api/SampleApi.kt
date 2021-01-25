package com.miquido.restmock.sample.api

import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SampleApi {

    @GET("api/v1/cars")
    fun getCars(
        @Query("make") make: Array<String>? = null
    ): Single<GetCarsResponseDto>

    @POST("api/v1/cars")
    fun addCar(@Body body: AddCarRequestDto): Completable

    @GET("api/v1/cars/{carId}")
    fun getCar(@Path("carId") carId: Long): Single<CarDto>

    @PUT("api/v1/cars/{carId}")
    fun updateCar(@Path("carId") carId: Long, @Body body: UpdateCarRequestDto): Completable
}