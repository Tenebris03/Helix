package com.tenebris.health_tracker.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenFoodFactsApi {
    @GET("api/v2/product/{barcode}")
    suspend fun getProduct(
        @Path("barcode") barcode: String,
        @Query("fields") fields: String = "product_name,nutriments,brands,quantity",
    ): ProductResponse

    @GET("cgi/search.pl")
    suspend fun searchProducts(
        @Query("search_terms") query: String,
        @Query("page_size") pageSize: Int = 5,
        @Query("json") json: Boolean = true,
    ): SearchResponse

    companion object {
        const val BASE_URL = "https://world.openfoodfacts.net/"
    }
}
