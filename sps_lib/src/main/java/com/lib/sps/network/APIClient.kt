package com.lib.sps.network

import com.google.gson.Gson
import com.lib.sps.BuildConfig
import com.lib.sps.CommonMethods
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient {
    private val httpClient = OkHttpClient.Builder()
    private var retrofit: Retrofit? = null
    val commonMethods = CommonMethods()

    private val builder = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create(Gson()))
//        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())

    fun <S> createService(serviceClass: Class<S>): S {
        try {
            httpClient.addInterceptor(Interceptor { chain ->
                val original = chain.request()
                // Request customization: add request headers
                val requestBuilder = original.newBuilder()
                    .header("Content-Type", "application/json")
                    .header("accept", "application/json")
                    .header("User-Agent", commonMethods.getUserAgent())
                    .method(original.method, original.body)

                val request = requestBuilder.build()
                return@Interceptor chain.proceed(request)
            })

            httpClient.addNetworkInterceptor { chain ->
                //Need to add access token in header.
                val request = chain.request().newBuilder()
                    //.addHeader("connection", "close")
                    //.addHeader("access_token", ForesterApplication.accessToken ?: "")
                    .build()
                chain.proceed(request)
            }
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            val client =
                httpClient.addInterceptor(interceptor).connectTimeout(200, TimeUnit.SECONDS)
                    .writeTimeout(200, TimeUnit.SECONDS)
                    .readTimeout(200, TimeUnit.SECONDS).build()
            retrofit = builder.client(client).build()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return retrofit!!.create(serviceClass)
    }

}