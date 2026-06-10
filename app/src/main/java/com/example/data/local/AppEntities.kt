package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "providers")
data class Provider(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val mainCategory: String,
    val subCategory: String,
    val city: String,
    val phone: String,
    val whatsapp: String = "",
    val description: String = "",
    val rating: Float = 4.5f,
    val votes: Int = 12,
    val isVerified: Boolean = false,
    val photoUri: String? = null,
    val idPhotoUri: String? = null,
    val gender: String = "ذكر",
    val registerDate: Long = System.currentTimeMillis(),
    val isBookmarked: Boolean = false,
    val isPending: Boolean = false, // Needs admin approval if true
    val isPinned: Boolean = false,  // Pinned at the very top of lists
    val isRecommended: Boolean = false, // Displayed in the high-impact VIP slider
    val isSubscribed: Boolean = false,  // Active monthly VIP subscription
    val points: Int = 0,                // Loyalty points accrued
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

@Entity(tableName = "admin_config")
data class AdminConfig(
    @PrimaryKey val id: Int = 1,
    val registrationConditions: String = "اللائحة التنظيمية المفتوحة:\n1. الأمانة والمصداقية المطلقة مع العملاء.\n2. الالتزام بالمواعيد ومستوى جودة الخدمة.\n3. رفع صورة مهنية صحيحة لزيادة التوثيق والأفضلية.\n4. يحق للإدارة تجميد أو حظر أي حساب في حال وجود شكاوى متكررة.",
    val welcomeMessage: String = "أهلاً بك يا غالي! أنا أبو يمن مساعدك الذكي للتنظيف والكهرباء والسباكة وكل الحرف. كيف أقدر أساعدك اليوم بخصوص أعطال البيت أو تسعير الخدمات؟ 🛠️⚡",
    val baseAppRateHourYER: Int = 4500, // Standard hourly rate guidance in Yemeni Rials
    val fontSizeModifier: Float = 1.0f,
    val secretKey: String = "maher--736462", // Default secret key for backdoor entry

    // Extended Customize Fields
    val appName: String = "كل خدمات اليمن",
    val themeIndex: Int = 2, // 0 = Cosmic Slate, 1 = Charcoal Gold, 2 = Royal Emerald (default)
    val themePrimaryColor: String = "0xFF0E6F4B",
    val themeSecondaryColor: String = "0xFFD4AF37",
    val sponsorFooter: String = "wam 2026",
    val supportPhone: String = "777644670",
    val supportEmail: String = "support@yemenservices.com",
    val supportWhatsapp: String = "777644670",
    val adminPassword: String = "maher736462", // Main admin password
    val adminUsername: String = "WAM2026",    // Main admin username
    val footerFontSize: Float = 10.0f,         // 50% smaller than average text
    val footerOpacity: Float = 0.5f,           // transparent opacity
    val smartAssistantSizePercent: Int = 50,  // 50% smaller bubble
    val smartAssistantEnabled: Boolean = true,
    val smartAssistantIcon: String = "🤖 المساعد",
    val chatEnabled: Boolean = true,            // Chat toggle
    val chatDisabledMessage: String = "تنبيه: تم تعطيل الميزة مؤقتاً للتحديث ومراجعة الاتصالات 🛠️",
    val chatIcon: String = "💬",
    val chatColor: String = "0xFFD4AF37",
    val chatBubbleSizePercent: Int = 50,
    val maxRadiusSearch: Int = 50,
    val voiceSearchEnabled: Boolean = true,
    val loyaltyPointsEnabled: Boolean = true,
    val maintenanceMode: Boolean = false,
    val maintenanceMessage: String = "التطبيق قيد التطوير والصيانة الدورية حالياً. سنعود بشكل أفضل قريباً جداً 🛠️",
    val twoFactorAuthEnabled: Boolean = false,
    val monthlySubscriptionEnabled: Boolean = true,
    val topBarLayout: String = "home,login,register,lang,refresh", // Sortable top layout order
    val fontName: String = "sans-serif",
    val aboutAppImage: String = "https://images.unsplash.com/photo-1581092921461-eab62e97a780?w=400",
    val aboutAppShareLink: String = "https://yemenservices.com/download",
    val showPhoneInAbout: Boolean = true,
    val showEmailInAbout: Boolean = true,
    val showImageInAbout: Boolean = true,
    val showShareInAbout: Boolean = true,
    val showWhatsappInAbout: Boolean = true
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "user" or "ai" or "peer" or "admin"
    val senderName: String = "",
    val receiverId: Int = 0,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class AppSupervisor(
    val name: String,
    val pass: String,
    val canApprove: Boolean = true,
    val canManageCategories: Boolean = true,
    val canBanners: Boolean = true,
    val canDeleteProviders: Boolean = true,
    val canViewReports: Boolean = true
)

data class TechnicalReport(
    val id: Int,
    val title: String,
    val complainant: String,
    val technician: String,
    val priority: String, // "High", "Medium", "Low"
    val date: String,
    val description: String
)

data class CustomAdBanner(
    val id: Int,
    val title: String,
    val mediaType: String, // "صورة", "فيديو", "نص"
    val section: String,   // "الرئيسية", "قائمة الأقسام"
    val size: String,      // "صغير S", "متوسط M", "عريض L"
    val durationSeconds: Int,
    val isActive: Boolean
)

