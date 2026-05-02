package com.tenebris.health_tracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tenebris.health_tracker.data.model.CachedProduct

@Dao
interface CachedProductDao {
    @Query("SELECT * FROM cached_products WHERE barcode = :barcode")
    suspend fun getProductByBarcode(barcode: String): CachedProduct?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: CachedProduct)

    @Query("DELETE FROM cached_products WHERE barcode NOT IN (SELECT barcode FROM cached_products ORDER BY lastAccessed DESC LIMIT 50)")
    suspend fun trimCache()
}
