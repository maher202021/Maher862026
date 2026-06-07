package com.example.data

import android.content.Context
import com.example.data.local.*
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AppRepository(context: Context) {
    private val firestore: FirebaseFirestore

    init {
        // Initialize Firebase with the user's specific credentials manually if not already configured
        if (FirebaseApp.getApps(context).isEmpty()) {
            val options = FirebaseOptions.Builder()
                .setApplicationId("1:363038603529:android:1f845fcf442c2e6693fbd8")
                .setProjectId("yemendate")
                .setApiKey("AIzaSyDOE3ta2r2j9lISFiCi5-9NfAZ4xi-RnZA")
                .setStorageBucket("yemendate.firebasestorage.app")
                .build()
            FirebaseApp.initializeApp(context, options)
        }
        firestore = FirebaseFirestore.getInstance()

        // Seed initial data in Firestore collection if it's currently empty
        CoroutineScope(Dispatchers.IO).launch {
            try {
                seedInitialFirestoreDataIfNeeded()
            } catch (e: Exception) {
                android.util.Log.e("AppRepository", "Error seeding Firestore data", e)
            }
        }
    }

    // Helper extension function to suspend/await Tasks from Firebase Google Play Services
    private suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T = suspendCancellableCoroutine { cont ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                cont.resume(task.result)
            } else {
                cont.resumeWithException(task.exception ?: RuntimeException("Task failed"))
            }
        }
    }

    // Real-time Flows utilizing Snapshot Listeners to feed UI state Flow dynamically
    val categories: Flow<List<CategoryEntity>> = callbackFlow {
        val subscription = firestore.collection("categories")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    android.util.Log.e("AppRepository", "Categories listener error", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            val idVal = doc.getLong("id")?.toInt() ?: doc.id.hashCode() and 0x7FFFFFFF
                            CategoryEntity(
                                id = idVal,
                                name = doc.getString("name") ?: "",
                                groupName = doc.getString("groupName") ?: "",
                                iconName = doc.getString("iconName") ?: "Build",
                                isPinned = doc.getBoolean("isPinned") ?: false
                            )
                        } catch (ex: Exception) {
                            null
                        }
                    }
                    trySend(list.sortedBy { it.id })
                }
            }
        awaitClose { subscription.remove() }
    }.flowOn(Dispatchers.IO)

    val providers: Flow<List<ServiceProviderEntity>> = callbackFlow {
        val subscription = firestore.collection("service_providers")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    android.util.Log.e("AppRepository", "Providers listener error", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            val idVal = doc.getLong("id")?.toInt() ?: doc.id.hashCode() and 0x7FFFFFFF
                            ServiceProviderEntity(
                                id = idVal,
                                name = doc.getString("name") ?: "",
                                mainCategory = doc.getString("mainCategory") ?: "",
                                subCategory = doc.getString("subCategory") ?: "",
                                rating = doc.getDouble("rating") ?: 4.5,
                                visitsCount = doc.getLong("visitsCount")?.toInt() ?: 0,
                                price = doc.getString("price") ?: "حسب الاتفاق",
                                location = doc.getString("location") ?: "صنعاء",
                                latitude = doc.getDouble("latitude") ?: 15.3694,
                                longitude = doc.getDouble("longitude") ?: 44.1910,
                                phone = doc.getString("phone") ?: "777644670",
                                whatsapp = doc.getString("whatsapp") ?: "777644670",
                                isVip = doc.getBoolean("isVip") ?: false,
                                isRecommended = doc.getBoolean("isRecommended") ?: false,
                                isVerified = doc.getBoolean("isVerified") ?: true,
                                isBlocked = doc.getBoolean("isBlocked") ?: false,
                                notes = doc.getString("notes") ?: "",
                                photoUri = doc.getString("photoUri"),
                                points = doc.getLong("points")?.toInt() ?: 50
                            )
                        } catch (ex: Exception) {
                            null
                        }
                    }
                    trySend(list)
                }
            }
        awaitClose { subscription.remove() }
    }.flowOn(Dispatchers.IO)

    val pendingProviders: Flow<List<PendingProviderEntity>> = callbackFlow {
        val subscription = firestore.collection("pending_providers")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            val idVal = doc.getLong("id")?.toInt() ?: doc.id.hashCode() and 0x7FFFFFFF
                            PendingProviderEntity(
                                id = idVal,
                                name = doc.getString("name") ?: "",
                                mainCategory = doc.getString("mainCategory") ?: "",
                                subCategory = doc.getString("subCategory") ?: "",
                                location = doc.getString("location") ?: "صنعاء",
                                phone = doc.getString("phone") ?: "777644670",
                                whatsapp = doc.getString("whatsapp") ?: "777644670",
                                photoUri = doc.getString("photoUri"),
                                idPhotoUri = doc.getString("idPhotoUri"),
                                userEmail = doc.getString("userEmail") ?: "",
                                genderOptional = doc.getString("genderOptional") ?: "مهني",
                                submissionDate = doc.getLong("submissionDate") ?: System.currentTimeMillis()
                            )
                        } catch (ex: Exception) {
                            null
                        }
                    }
                    trySend(list)
                }
            }
        awaitClose { subscription.remove() }
    }.flowOn(Dispatchers.IO)

    val banners: Flow<List<BannerEntity>> = callbackFlow {
        val subscription = firestore.collection("banners")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            val idVal = doc.getLong("id")?.toInt() ?: doc.id.hashCode() and 0x7FFFFFFF
                            BannerEntity(
                                id = idVal,
                                title = doc.getString("title") ?: "",
                                type = doc.getString("type") ?: "صورة",
                                contentUrl = doc.getString("contentUrl") ?: "",
                                textMessage = doc.getString("textMessage") ?: "",
                                link = doc.getString("link") ?: "",
                                durationSeconds = doc.getLong("durationSeconds")?.toInt() ?: 5,
                                isActive = doc.getBoolean("isActive") ?: true
                            )
                        } catch (ex: Exception) {
                            null
                        }
                    }
                    trySend(list)
                }
            }
        awaitClose { subscription.remove() }
    }.flowOn(Dispatchers.IO)

    val chats: Flow<List<ChatEntity>> = callbackFlow {
        val subscription = firestore.collection("chats")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            ChatEntity(
                                id = doc.id,
                                userEmail = doc.getString("userEmail") ?: "",
                                providerId = doc.getLong("providerId")?.toInt() ?: 0,
                                providerName = doc.getString("providerName") ?: "",
                                lastMessage = doc.getString("lastMessage") ?: "",
                                lastTimestamp = doc.getLong("lastTimestamp") ?: System.currentTimeMillis(),
                                isDisabled = doc.getBoolean("isDisabled") ?: false
                            )
                        } catch (ex: Exception) {
                            null
                        }
                    }
                    trySend(list.sortedByDescending { it.lastTimestamp })
                }
            }
        awaitClose { subscription.remove() }
    }.flowOn(Dispatchers.IO)

    val config: Flow<AppConfigEntity?> = callbackFlow {
        val subscription = firestore.collection("app_config").document("1")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    try {
                        val entity = AppConfigEntity(
                            id = 1,
                            appName = snapshot.getString("appName") ?: "كل خدمات اليمن",
                            marqueeText = snapshot.getString("marqueeText") ?: "",
                            footerText = snapshot.getString("footerText") ?: "",
                            footerColor = snapshot.getString("footerColor") ?: "#FF1A1A1A",
                            footerOpacity = snapshot.getDouble("footerOpacity")?.toFloat() ?: 0.9f,
                            primaryColorHex = snapshot.getString("primaryColorHex") ?: "#FF0D9488",
                            secondaryColorHex = snapshot.getString("secondaryColorHex") ?: "#FFF59E0B",
                            chatIconSize = snapshot.getLong("chatIconSize")?.toInt() ?: 50,
                            chatIconColorHex = snapshot.getString("chatIconColorHex") ?: "#FF25D366",
                            fontSizeModifier = snapshot.getDouble("fontSizeModifier")?.toFloat() ?: 1.0f,
                            isSpeechSearchEnabled = snapshot.getBoolean("isSpeechSearchEnabled") ?: true,
                            supportPhone = snapshot.getString("supportPhone") ?: "777644670",
                            shareLink = snapshot.getString("shareLink") ?: "",
                            adminPassword = snapshot.getString("adminPassword") ?: "maher--736462",
                            isMaintenanceMode = snapshot.getBoolean("isMaintenanceMode") ?: false,
                            is2FAEnabled = snapshot.getBoolean("is2FAEnabled") ?: false,
                            whitelistDevice = snapshot.getString("whitelistDevice") ?: ""
                        )
                        trySend(entity)
                    } catch (ex: Exception) {
                        trySend(AppConfigEntity())
                    }
                } else {
                    trySend(AppConfigEntity())
                }
            }
        awaitClose { subscription.remove() }
    }.flowOn(Dispatchers.IO)

    val loyaltyPoints: Flow<LoyaltyPointsEntity?> = callbackFlow {
        val subscription = firestore.collection("loyalty_points").document("1")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    try {
                        val entity = LoyaltyPointsEntity(
                            id = 1,
                            points = snapshot.getLong("points")?.toInt() ?: 150,
                            historyLog = snapshot.getString("historyLog") ?: "نقاط ترحيبية: 150"
                        )
                        trySend(entity)
                    } catch (ex: Exception) {
                        trySend(LoyaltyPointsEntity())
                    }
                } else {
                    trySend(LoyaltyPointsEntity())
                }
            }
        awaitClose { subscription.remove() }
    }.flowOn(Dispatchers.IO)

    val activityLogs: Flow<List<ActivityLogEntity>> = callbackFlow {
        val subscription = firestore.collection("activity_logs")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            val idVal = doc.getLong("id")?.toInt() ?: doc.id.hashCode() and 0x7FFFFFFF
                            ActivityLogEntity(
                                id = idVal,
                                action = doc.getString("action") ?: "",
                                user = doc.getString("user") ?: "أدمن",
                                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                            )
                        } catch (ex: Exception) {
                            null
                        }
                    }
                    trySend(list.sortedByDescending { it.timestamp })
                }
            }
        awaitClose { subscription.remove() }
    }.flowOn(Dispatchers.IO)

    // Category actions
    suspend fun addCategory(category: CategoryEntity) {
        val doc = firestore.collection("categories").document()
        val idVal = doc.id.hashCode() and 0x7FFFFFFF
        val finalCat = category.copy(id = idVal)
        doc.set(finalCat.toMap()).await()
        logActivity("إضافة قسم جديد: ${category.name}")
    }

    suspend fun removeCategory(id: Int) {
        val querySnapshot = firestore.collection("categories")
            .whereEqualTo("id", id)
            .get()
            .await()
        for (doc in querySnapshot.documents) {
            doc.reference.delete().await()
        }
        logActivity("حذف القسم بمعرف: $id")
    }

    // Provider actions
    suspend fun addProvider(provider: ServiceProviderEntity) {
        val doc = firestore.collection("service_providers").document()
        val idVal = doc.id.hashCode() and 0x7FFFFFFF
        val finalProvider = provider.copy(id = idVal)
        doc.set(finalProvider.toMap()).await()
        logActivity("إضافة فني يدوياً: ${provider.name}")
    }

    suspend fun updateProvider(provider: ServiceProviderEntity) {
        val querySnapshot = firestore.collection("service_providers")
            .whereEqualTo("id", provider.id)
            .get()
            .await()
        if (!querySnapshot.isEmpty) {
            for (doc in querySnapshot.documents) {
                doc.reference.set(provider.toMap()).await()
            }
        } else {
            firestore.collection("service_providers").document(provider.id.toString()).set(provider.toMap()).await()
        }
    }

    suspend fun removeProvider(id: Int) {
        val querySnapshot = firestore.collection("service_providers")
            .whereEqualTo("id", id)
            .get()
            .await()
        for (doc in querySnapshot.documents) {
            doc.reference.delete().await()
        }
        logActivity("حذف فني بمعرف: $id")
    }

    // Pending provider actions
    suspend fun submitRegistration(pending: PendingProviderEntity) {
        val doc = firestore.collection("pending_providers").document()
        val idVal = doc.id.hashCode() and 0x7FFFFFFF
        val finalPending = pending.copy(id = idVal)
        doc.set(finalPending.toMap()).await()
        logActivity("تقديم طلب تسجيل مقدم خدمة جديد باسم: ${pending.name}")
    }

    suspend fun approveProvider(pendingId: Int, pending: PendingProviderEntity) {
        val provider = ServiceProviderEntity(
            name = pending.name,
            mainCategory = pending.mainCategory,
            subCategory = pending.subCategory,
            location = pending.location,
            phone = pending.phone,
            whatsapp = pending.whatsapp,
            isVip = false,
            isRecommended = false,
            isVerified = true,
            photoUri = pending.photoUri
        )
        addProvider(provider)

        val querySnapshot = firestore.collection("pending_providers")
            .whereEqualTo("id", pendingId)
            .get()
            .await()
        for (doc in querySnapshot.documents) {
            doc.reference.delete().await()
        }
        logActivity("قبول طلب تسجيل وتفعيل الفني: ${pending.name}")
    }

    suspend fun rejectProvider(pendingId: Int, name: String, reason: String) {
        val querySnapshot = firestore.collection("pending_providers")
            .whereEqualTo("id", pendingId)
            .get()
            .await()
        for (doc in querySnapshot.documents) {
            doc.reference.delete().await()
        }
        logActivity("رفض طلب تسجيل الفني: $name بسبب: $reason")
    }

    // Banner actions
    suspend fun addBanner(banner: BannerEntity) {
        val doc = firestore.collection("banners").document()
        val idVal = doc.id.hashCode() and 0x7FFFFFFF
        val finalBanner = banner.copy(id = idVal)
        doc.set(finalBanner.toMap()).await()
        logActivity("إضافة حملة إعلان وبنر جديدة: ${banner.title}")
    }

    suspend fun removeBanner(id: Int) {
        val querySnapshot = firestore.collection("banners")
            .whereEqualTo("id", id)
            .get()
            .await()
        for (doc in querySnapshot.documents) {
            doc.reference.delete().await()
        }
        logActivity("حذف البنر بمعرف: $id")
    }

    // Config actions
    suspend fun updateConfig(config: AppConfigEntity) {
        firestore.collection("app_config").document("1").set(config.toMap()).await()
    }

    suspend fun getConfigSync(): AppConfigEntity {
        return try {
            val snapshot = firestore.collection("app_config").document("1").get().await()
            if (snapshot.exists()) {
                AppConfigEntity(
                    id = 1,
                    appName = snapshot.getString("appName") ?: "كل خدمات اليمن",
                    marqueeText = snapshot.getString("marqueeText") ?: "",
                    footerText = snapshot.getString("footerText") ?: "",
                    footerColor = snapshot.getString("footerColor") ?: "#FF1A1A1A",
                    footerOpacity = snapshot.getDouble("footerOpacity")?.toFloat() ?: 0.9f,
                    primaryColorHex = snapshot.getString("primaryColorHex") ?: "#FF0D9488",
                    secondaryColorHex = snapshot.getString("secondaryColorHex") ?: "#FFF59E0B",
                    chatIconSize = snapshot.getLong("chatIconSize")?.toInt() ?: 50,
                    chatIconColorHex = snapshot.getString("chatIconColorHex") ?: "#FF25D366",
                    fontSizeModifier = snapshot.getDouble("fontSizeModifier")?.toFloat() ?: 1.0f,
                    isSpeechSearchEnabled = snapshot.getBoolean("isSpeechSearchEnabled") ?: true,
                    supportPhone = snapshot.getString("supportPhone") ?: "777644670",
                    shareLink = snapshot.getString("shareLink") ?: "",
                    adminPassword = snapshot.getString("adminPassword") ?: "maher--736462",
                    isMaintenanceMode = snapshot.getBoolean("isMaintenanceMode") ?: false,
                    is2FAEnabled = snapshot.getBoolean("is2FAEnabled") ?: false,
                    whitelistDevice = snapshot.getString("whitelistDevice") ?: ""
                )
            } else {
                AppConfigEntity()
            }
        } catch (e: Exception) {
            AppConfigEntity()
        }
    }

    // Chat actions
    fun getMessages(chatId: String): Flow<List<MessageEntity>> = callbackFlow {
        val subscription = firestore.collection("messages")
            .whereEqualTo("chatId", chatId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            val idVal = doc.getLong("id")?.toInt() ?: doc.id.hashCode() and 0x7FFFFFFF
                            MessageEntity(
                                id = idVal,
                                chatId = doc.getString("chatId") ?: "",
                                senderEmail = doc.getString("senderEmail") ?: "",
                                text = doc.getString("text") ?: "",
                                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                            )
                        } catch (ex: Exception) {
                            null
                        }
                    }
                    trySend(list.sortedBy { it.timestamp })
                }
            }
        awaitClose { subscription.remove() }
    }.flowOn(Dispatchers.IO)

    suspend fun sendChatMessage(chatId: String, sender: String, text: String, providerId: Int, providerName: String) {
        val msgDoc = firestore.collection("messages").document()
        val msgId = msgDoc.id.hashCode() and 0x7FFFFFFF
        val message = MessageEntity(
            id = msgId,
            chatId = chatId,
            senderEmail = sender,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        msgDoc.set(message.toMap()).await()

        val parts = chatId.split("_")
        val userEmail = parts.getOrNull(0) ?: "user@example.com"

        val chat = ChatEntity(
            id = chatId,
            userEmail = userEmail,
            providerId = providerId,
            providerName = providerName,
            lastMessage = text,
            lastTimestamp = System.currentTimeMillis()
        )
        firestore.collection("chats").document(chatId).set(chat.toMap()).await()
    }

    suspend fun setChatDisabled(chatId: String, disabled: Boolean) {
        val parts = chatId.split("_")
        val userEmail = parts.getOrNull(0) ?: ""
        val providerId = parts.getOrNull(1)?.toIntOrNull() ?: 1
        val providerName = parts.getOrNull(2) ?: "الفني والمشرف"
        val chat = ChatEntity(
            id = chatId,
            userEmail = userEmail,
            providerId = providerId,
            providerName = providerName,
            lastMessage = if (disabled) "عذراً، تم تعطيل الدردشة حالياً من قبل الإدارة" else "تم تنشيط المحادثة ثنائية الأطراف",
            lastTimestamp = System.currentTimeMillis(),
            isDisabled = disabled
        )
        firestore.collection("chats").document(chatId).set(chat.toMap()).await()
        logActivity("تحديث حالة دردشة ($chatId): معطلة=$disabled")
    }

    // Loyalty points actions
    suspend fun addPoints(amount: Int, reason: String) {
        try {
            val snapshot = firestore.collection("loyalty_points").document("1").get().await()
            val existingPoints = if (snapshot.exists()) snapshot.getLong("points")?.toInt() ?: 150 else 150
            val existingHistory = if (snapshot.exists()) snapshot.getString("historyLog") ?: "" else ""
            val updatedPoints = existingPoints + amount
            val updatedHistory = existingHistory + "\nشحن نقاط: +$amount ($reason)"
            val updatedEntity = LoyaltyPointsEntity(
                id = 1,
                points = updatedPoints,
                historyLog = updatedHistory
            )
            firestore.collection("loyalty_points").document("1").set(updatedEntity.toMap()).await()
        } catch (e: Exception) {
            firestore.collection("loyalty_points").document("1").set(
                LoyaltyPointsEntity(id = 1, points = 150, historyLog = "خطأ في الاتصال، إعادة تعيين").toMap()
            ).await()
        }
    }

    suspend fun deductPoints(amount: Int, reason: String): Boolean {
        return try {
            val snapshot = firestore.collection("loyalty_points").document("1").get().await()
            val existingPoints = if (snapshot.exists()) snapshot.getLong("points")?.toInt() ?: 150 else 150
            val existingHistory = if (snapshot.exists()) snapshot.getString("historyLog") ?: "" else ""
            if (existingPoints >= amount) {
                val updatedPoints = existingPoints - amount
                val updatedHistory = existingHistory + "\nاستبدال نقاط: -$amount ($reason)"
                val updatedEntity = LoyaltyPointsEntity(
                    id = 1,
                    points = updatedPoints,
                    historyLog = updatedHistory
                )
                firestore.collection("loyalty_points").document("1").set(updatedEntity.toMap()).await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    // Activity log actions
    suspend fun logActivity(action: String, user: String = "أدمن") {
        try {
            val logDoc = firestore.collection("activity_logs").document()
            val logId = logDoc.id.hashCode() and 0x7FFFFFFF
            val log = ActivityLogEntity(
                id = logId,
                action = action,
                user = user,
                timestamp = System.currentTimeMillis()
            )
            logDoc.set(log.toMap()).await()
        } catch (e: Exception) {
            // fail-silent for logger
        }
    }

    // Map-conversions for Firestore documents
    private fun CategoryEntity.toMap() = mapOf(
        "id" to id,
        "name" to name,
        "groupName" to groupName,
        "iconName" to iconName,
        "isPinned" to isPinned
    )

    private fun ServiceProviderEntity.toMap() = mapOf(
        "id" to id,
        "name" to name,
        "mainCategory" to mainCategory,
        "subCategory" to subCategory,
        "rating" to rating,
        "visitsCount" to visitsCount,
        "price" to price,
        "location" to location,
        "latitude" to latitude,
        "longitude" to longitude,
        "phone" to phone,
        "whatsapp" to whatsapp,
        "isVip" to isVip,
        "isRecommended" to isRecommended,
        "isVerified" to isVerified,
        "isBlocked" to isBlocked,
        "notes" to notes,
        "photoUri" to photoUri,
        "points" to points
    )

    private fun PendingProviderEntity.toMap() = mapOf(
        "id" to id,
        "name" to name,
        "mainCategory" to mainCategory,
        "subCategory" to subCategory,
        "location" to location,
        "phone" to phone,
        "whatsapp" to whatsapp,
        "photoUri" to photoUri,
        "idPhotoUri" to idPhotoUri,
        "userEmail" to userEmail,
        "genderOptional" to genderOptional,
        "submissionDate" to submissionDate
    )

    private fun BannerEntity.toMap() = mapOf(
        "id" to id,
        "title" to title,
        "type" to type,
        "contentUrl" to contentUrl,
        "textMessage" to textMessage,
        "link" to link,
        "durationSeconds" to durationSeconds,
        "isActive" to isActive
    )

    private fun ChatEntity.toMap() = mapOf(
        "id" to id,
        "userEmail" to userEmail,
        "providerId" to providerId,
        "providerName" to providerName,
        "lastMessage" to lastMessage,
        "lastTimestamp" to lastTimestamp,
        "isDisabled" to isDisabled
    )

    private fun MessageEntity.toMap() = mapOf(
        "id" to id,
        "chatId" to chatId,
        "senderEmail" to senderEmail,
        "text" to text,
        "timestamp" to timestamp
    )

    private fun AppConfigEntity.toMap() = mapOf(
        "id" to id,
        "appName" to appName,
        "marqueeText" to marqueeText,
        "footerText" to footerText,
        "footerColor" to footerColor,
        "footerOpacity" to footerOpacity,
        "primaryColorHex" to primaryColorHex,
        "secondaryColorHex" to secondaryColorHex,
        "chatIconSize" to chatIconSize,
        "chatIconColorHex" to chatIconColorHex,
        "fontSizeModifier" to fontSizeModifier,
        "isSpeechSearchEnabled" to isSpeechSearchEnabled,
        "supportPhone" to supportPhone,
        "shareLink" to shareLink,
        "adminPassword" to adminPassword,
        "isMaintenanceMode" to isMaintenanceMode,
        "is2FAEnabled" to is2FAEnabled,
        "whitelistDevice" to whitelistDevice
    )

    private fun LoyaltyPointsEntity.toMap() = mapOf(
        "id" to id,
        "points" to points,
        "historyLog" to historyLog
    )

    private fun ActivityLogEntity.toMap() = mapOf(
        "id" to id,
        "action" to action,
        "user" to user,
        "timestamp" to timestamp
    )

    private suspend fun seedInitialFirestoreDataIfNeeded() {
        val categoriesCount = firestore.collection("categories").get().await().size()
        if (categoriesCount == 0) {
            val defaultCategories = listOf(
                CategoryEntity(name = "كهربائي منازل", groupName = "صيانة منزلية", iconName = "ElectricalServices", isPinned = true),
                CategoryEntity(name = "سباك صحي", groupName = "صيانة منزلية", iconName = "Plumbing", isPinned = true),
                CategoryEntity(name = "نجار وديكور", groupName = "صيانة منزلية", iconName = "Handyman", isPinned = false),
                CategoryEntity(name = "مهندس مكيفات", groupName = "صيانة منزلية", iconName = "AcUnit", isPinned = false),
                CategoryEntity(name = "صيانة جوالات", groupName = "برمجيات وتقنية", iconName = "PhonelinkSetup", isPinned = true),
                CategoryEntity(name = "مهندس كمبيوتر", groupName = "برمجيات وتقنية", iconName = "Computer", isPinned = false),
                CategoryEntity(name = "ميكانيكي سيارات", groupName = "سيارات ومحركات", iconName = "DirectionsCar", isPinned = true),
                CategoryEntity(name = "كهربائي سيارات", groupName = "سيارات ومحركات", iconName = "FlashOn", isPinned = false),
                CategoryEntity(name = "تمريض منزلي", groupName = "رعاية طبية", iconName = "LocalHospital", isPinned = false),
                CategoryEntity(name = "مدرس منزلي", groupName = "تعليم وتدريس", iconName = "School", isPinned = false)
            )
            for (cat in defaultCategories) {
                val doc = firestore.collection("categories").document()
                val idVal = doc.id.hashCode() and 0x7FFFFFFF
                doc.set(cat.copy(id = idVal).toMap()).await()
            }

            val defaultBanners = listOf(
                BannerEntity(
                    title = "خصم خاص على خدمات التكييف والتهوية",
                    type = "صورة",
                    contentUrl = "https://images.unsplash.com/photo-1517649763962-0c623066013b?w=600",
                    textMessage = "صيانة فورية ومضمونة بخصم 20% لفترة محدودة",
                    link = "https://wa.me/777644670",
                    durationSeconds = 6,
                    isActive = true
                ),
                BannerEntity(
                    title = "نخبة VIP فني جوالات وحواسيب",
                    type = "نص",
                    textMessage = "تم انضمام م. ماهر كأفضل مبرمج وصيانة برمجيات في اليمن بضمان معتمد وجاهزية تامة.",
                    link = "tel:777644670",
                    durationSeconds = 4,
                    isActive = true
                )
            )
            for (banner in defaultBanners) {
                val doc = firestore.collection("banners").document()
                val idVal = doc.id.hashCode() and 0x7FFFFFFF
                doc.set(banner.copy(id = idVal).toMap()).await()
            }

            val defaultProviders = listOf(
                ServiceProviderEntity(
                    name = "المهندس ماهر علوان",
                    mainCategory = "برمجيات وتقنية",
                    subCategory = "صيانة جوالات",
                    rating = 4.9,
                    price = "تبدأ من 3000 ريال",
                    location = "صنعاء - حدة",
                    latitude = 15.3340,
                    longitude = 44.1950,
                    phone = "777644670",
                    whatsapp = "777644670",
                    isVip = true,
                    isRecommended = true,
                    isVerified = true,
                    notes = "معتمد في فك الشفرات، برمجة وعمل سوفت وير لكافة الأجهزة الذكية."
                ),
                ServiceProviderEntity(
                    name = "الأسطى ناصر السباك",
                    mainCategory = "صيانة منزلية",
                    subCategory = "سباك صحي",
                    rating = 4.7,
                    price = "تبدأ من 5000 ريال",
                    location = "صنعاء - التحرير",
                    latitude = 15.3520,
                    longitude = 44.1880,
                    phone = "736462777",
                    whatsapp = "736462777",
                    isVip = false,
                    isRecommended = true,
                    isVerified = true,
                    notes = "تأسيس وصيانة شبكات المياه والصرف الصحي الداخلي والخارجي بجودة عالية وإتقان."
                ),
                ServiceProviderEntity(
                    name = "الأستاذ أحمد معلم كهرباء",
                    mainCategory = "صيانة منزلية",
                    subCategory = "كهربائي منازل",
                    rating = 4.8,
                    price = "تبدأ من 4000 ريال",
                    location = "صنعاء - الستين",
                    latitude = 15.3560,
                    longitude = 44.1720,
                    phone = "777123456",
                    whatsapp = "777123456",
                    isVip = true,
                    isRecommended = false,
                    isVerified = true,
                    notes = "تركيب وإصلاح شبكات الكهرباء المنزلية والإنارة الحديثة وأنظمة الطاقة الشمسية."
                )
            )
            for (p in defaultProviders) {
                val doc = firestore.collection("service_providers").document()
                val idVal = doc.id.hashCode() and 0x7FFFFFFF
                doc.set(p.copy(id = idVal).toMap()).await()
            }

            firestore.collection("app_config").document("1").set(AppConfigEntity().toMap()).await()
            firestore.collection("loyalty_points").document("1").set(LoyaltyPointsEntity(points = 150).toMap()).await()

            val logDoc = firestore.collection("activity_logs").document()
            val logId = logDoc.id.hashCode() and 0x7FFFFFFF
            logDoc.set(ActivityLogEntity(id = logId, action = "تهيئة أولية لتطبيق كل خدمات اليمن وتنشيط البيانات الافتراضية بـ Firestore").toMap()).await()
        }
    }
}
