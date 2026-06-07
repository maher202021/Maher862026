package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY isPinned DESC, name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategoryById(id: Int)
}

@Dao
interface ServiceProviderDao {
    @Query("SELECT * FROM service_providers ORDER BY isVip DESC, isRecommended DESC, rating DESC")
    fun getAllProviders(): Flow<List<ServiceProviderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvider(provider: ServiceProviderEntity)

    @Update
    suspend fun updateProvider(provider: ServiceProviderEntity)

    @Query("DELETE FROM service_providers WHERE id = :id")
    suspend fun deleteProviderById(id: Int)
}

@Dao
interface PendingProviderDao {
    @Query("SELECT * FROM pending_providers ORDER BY submissionDate DESC")
    fun getAllPendingProviders(): Flow<List<PendingProviderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingProvider(provider: PendingProviderEntity)

    @Query("DELETE FROM pending_providers WHERE id = :id")
    suspend fun deletePendingProviderById(id: Int)
}

@Dao
interface BannerDao {
    @Query("SELECT * FROM banners ORDER BY id DESC")
    fun getAllBanners(): Flow<List<BannerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBanner(banner: BannerEntity)

    @Query("DELETE FROM banners WHERE id = :id")
    suspend fun deleteBannerById(id: Int)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chats ORDER BY lastTimestamp DESC")
    fun getAllChats(): Flow<List<ChatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)
}

@Dao
interface AppConfigDao {
    @Query("SELECT * FROM app_config WHERE id = 1")
    fun getConfig(): Flow<AppConfigEntity?>

    @Query("SELECT * FROM app_config WHERE id = 1")
    suspend fun getConfigSync(): AppConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: AppConfigEntity)
}

@Dao
interface LoyaltyPointsDao {
    @Query("SELECT * FROM loyalty_points WHERE id = 1")
    fun getPoints(): Flow<LoyaltyPointsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoints(points: LoyaltyPointsEntity)
}

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLogEntity)
}
