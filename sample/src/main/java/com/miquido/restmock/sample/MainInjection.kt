package com.miquido.restmock.sample

import com.miquido.restmock.sample.api.SampleApi
import com.miquido.restmock.sample.api.sampleApiMocks
import com.miquido.restmock.setMocks
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

private val apiUrl = "https://miquido.sample.api/"
private var apiMocked: Boolean = false

private val mainModule = module {

    single {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    single {
        OkHttpClient.Builder()
            .setMocks(apiMocked) {
                arrayOf(
                    sampleApiMocks
                    // add other api mocks here
                )
            }
            .build()
    }

    single { MoshiConverterFactory.create(get()).asLenient() } bind Converter.Factory::class

    single {
        buildApi(
            baseUrl = apiUrl,
            okHttpClient = get(),
            converterFactory = get(),
            clazz = SampleApi::class.java
        )
    }
}

fun mainModule(mockApi: Boolean): Module {
    apiMocked = mockApi
    return mainModule
}

private fun <T> buildApi(
    baseUrl: String,
    okHttpClient: OkHttpClient,
    converterFactory: Converter.Factory,
    clazz: Class<T>
): T =
    Retrofit.Builder()
        .addConverterFactory(converterFactory)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .client(okHttpClient)
        .baseUrl(baseUrl)
        .build()
        .create(clazz)
