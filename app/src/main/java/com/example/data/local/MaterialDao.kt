package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MaterialItem
import kotlinx.coroutines.flow.Flow

@Dao
interface MaterialDao {
    @Query("SELECT * FROM materials ORDER BY name ASC")
    fun getAllMaterials(): Flow<List<MaterialItem>>

    @Query("SELECT * FROM materials WHERE category = :category ORDER BY name ASC")
    fun getMaterialsByCategory(category: String): Flow<List<MaterialItem>>

    @Query("SELECT * FROM materials WHERE quantity <= minStockAlert ORDER BY quantity ASC")
    fun getLowStockMaterials(): Flow<List<MaterialItem>>

    @Query("SELECT * FROM materials WHERE id = :id")
    suspend fun getMaterialById(id: Long): MaterialItem?

    @Query("SELECT * FROM materials WHERE name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    fun searchMaterials(query: String): Flow<List<MaterialItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: MaterialItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(materials: List<MaterialItem>)

    @Update
    suspend fun updateMaterial(material: MaterialItem)

    @Delete
    suspend fun deleteMaterial(material: MaterialItem)

    @Query("DELETE FROM materials WHERE id = :id")
    suspend fun deleteMaterialById(id: Long)

    @Query("UPDATE materials SET quantity = quantity + :delta WHERE id = :id")
    suspend fun updateStockQuantity(id: Long, delta: Double)
}
