package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,         // e.g., "صيانة كهرباء"
    val groupName: String,    // Main Category, e.g., "صيانة منزلية" or "خدمات تقنية"
    val iconName: String,     // Name of Material Icon or graphic reference
    val isPinned: Boolean = false
)

@Entity(tableName = "service_providers")
data class ServiceProviderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val mainCategory: String,
    val subCategory: String,
    val rating: Double = 4.5,
    val visitsCount: Int = 0,
    val price: String = "حسب الاتفاق",
    val location: String = "صنعاء",
    val latitude: Double = 15.3694, // صنعاء center
    val longitude: Double = 44.1910,
    val phone: String = "777644670",
    val whatsapp: String = "777644670",
    val isVip: Boolean = false,
    val isRecommended: Boolean = false,
    val isVerified: Boolean = true,
    val isBlocked: Boolean = false,
    val notes: String = "",
    val photoUri: String? = null,
    val points: Int = 50
)

@Entity(tableName = "pending_providers")
data class PendingProviderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val mainCategory: String,
    val subCategory: String,
    val location: String,
    val phone: String,
    val whatsapp: String,
    val photoUri: String? = null,
    val idPhotoUri: String? = null,
    val userEmail: String = "",
    val genderOptional: String = "مهني",
    val submissionDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "banners")
data class BannerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val type: String = "صورة", // "صورة" / "فيديو" / "نص"
    val contentUrl: String = "", // Placeholders
    val textMessage: String = "",
    val link: String = "",
    val durationSeconds: Int = 5,
    val isActive: Boolean = true
)

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String, // format: "user_email_provider_id" or "user_email_admin"
    val userEmail: String,
    val providerId: Int, // 0 for Admin, >0 for providers
    val providerName: String,
    val lastMessage: String,
    val lastTimestamp: Long = System.currentTimeMillis(),
    val isDisabled: Boolean = false
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val chatId: String,
    val senderEmail: String, // "user", "provider", "admin"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_config")
data class AppConfigEntity(
    @PrimaryKey val id: Int = 1,
    val appName: String = "كل خدمات اليمن",
    val marqueeText: String = "أهلاً ومرحباً بكم مع تطبيق كل خدمات اليمن - الدليل الشامل لجميع المهن والخدمات على مدار الساعة",
    val footerText: String = "ℹ️ عن التطبيق | MAW 777644670",
    val footerColor: String = "#FF1A1A1A",
    val footerOpacity: Float = 0.9f,
    val primaryColorHex: String = "#FF0D9488", // Teal 600
    val secondaryColorHex: String = "#FFF59E0B", // Amber 500
    val chatIconSize: Int = 50, // default 50% / custom padding
    val chatIconColorHex: String = "#FF25D366", // WhatsApp green
    val fontSizeModifier: Float = 1.0f,
    val isSpeechSearchEnabled: Boolean = true,
    val supportPhone: String = "777644670",
    val shareLink: String = "https://t.me/yemenservices",
    val adminPassword: String = "maher--736462",
    val isMaintenanceMode: Boolean = false,
    val is2FAEnabled: Boolean = false,
    val whitelistDevice: String = ""
)

@Entity(tableName = "loyalty_points")
data class LoyaltyPointsEntity(
    @PrimaryKey val id: Int = 1,
    val points: Int = 100,
    val historyLog: String = "نقاط ترحيبية: 100"
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val action: String,
    val user: String = "أدمن",
    val timestamp: Long = System.currentTimeMillis()
)
