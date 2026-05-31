package com.tenebris.health_tracker.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductResponse(
    val code: String,
    val product: Product? = null,
    val status: Int,
    @SerialName("status_verbose")
    val statusVerbose: String
)

@Serializable
data class SearchResponse(
    val products: List<Product> = emptyList(),
    val count: Int = 0,
    @SerialName("page_size")
    val pageSize: Int = 0,
    val skip: Int = 0
)

@Serializable
data class Product(
    @SerialName("product_name")
    val productName: String? = null,
    val nutriments: Nutriments? = null,
    val brands: String? = null,
    val quantity: String? = null
)

@Serializable
data class Nutriments(
    @SerialName("energy-kcal_100g")
    val calories100g: Double? = null,
    @SerialName("proteins_100g")
    val proteins100g: Double? = null,
    @SerialName("carbohydrates_100g")
    val carbohydrates100g: Double? = null,
    @SerialName("fat_100g")
    val fat100g: Double? = null,
    @SerialName("fiber_100g")
    val fiber100g: Double? = null
)
