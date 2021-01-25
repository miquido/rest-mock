package com.miquido.restmock.sample.api


data class GetCarsResponseDto(
    val cars: List<CarDto>
)

data class CarDto(
    val id: Long,
    val make: String,
    val model: String
)

data class AddCarRequestDto(
    val make: String,
    val model: String
)

data class UpdateCarRequestDto(
    val model: String
)
