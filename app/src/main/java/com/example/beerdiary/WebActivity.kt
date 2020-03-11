package com.example.beerdiary

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.*

class WebActivity() {
    private val url = "https://ucum.nlm.nih.gov/ucum-service/v1/ucumtransform/"
    object Model {
        data class Result(val UCUMWebServiceResponse: WebServiceResponse)
        data class WebServiceResponse(val Response: Response)
        data class Response(val TargetUnit: String, val ResultQuantity: Double, val SourceUnit: String, val SourceQuantity: Double)
    }
    interface Service {
        @Headers("Accept: application/json")
        @GET("{quantity}/from/ml/to/%5Bfoz_us%5D")
        fun unitConversion(@Path("quantity") quantity: Double): Call<Model.Result>
    }
    private val retrofit = Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val service: Service = retrofit.create(Service::class.java)
}