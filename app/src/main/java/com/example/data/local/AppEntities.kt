package com.example.data.local

data class Provider(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val locationCity: String = "",
    val locationRegion: String = "",
    val serviceCategory: String = "",
    val subCategory: String = "",
    val rate: Double = 5.0,
    val isVip: Boolean = false,
    val isPinned: Boolean = false,
    val isRecommended: Boolean = false,
    val isVerified: Boolean = false,
    val photoUrl: String = "",
    val gpsCoordinates: String = "",
    val idCardUrl: String = "",
    val previousWorksJson: String = "", // List of work image URLs separated by comma
    val status: String = "approved", // approved, pending, rejected
    val rejectionReason: String = "",
    val points: Int = 0
)

data class AdminConfig(
    val adminUsername: String = "WAM2026",
    val adminPassword: String = "maher736462",
    val appName: String = "كل خدمات اليمن",
    val slogan: String = "أكبر دليل خدمي وتنسيقي متكامل للكهرباء والسباكة والمهن الحرة",
    val primaryColorHex: String = "#0F172A", // Dark Slate Blue
    val secondaryColorHex: String = "#3B82F6", // Bright Blue
    val footerText: String = "MAW 777644670",
    val infoHtmlText: String = "مؤسسة الـ WAM للتنمية والخدمات - ريادة الأعمال التقنية في الجمهورية اليمنية.",
    val supportPhone: String = "777644670",
    val supportEmail: String = "support@yemenservices.com",
    val supportWhatsapp: String = "777644670",
    val isMaintenanceMode: Boolean = false,
    val footerHeightDp: Int = 56,
    val footerAlpha: Float = 1.0f,
    val footerCustomImage: String = "",
    val assistantActive: Boolean = true,
    val assistantIcon: String = "🤖", // 🤖, 💬, ✨
    val assistantScale: Float = 0.5f, // percentage size reduction
    val assistantX: Float = 0.9f,
    val assistantY: Float = 0.85f,
    val secretKey: String = "maher--736462"
)

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val messageText: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val chatRoomId: String = "general"
)

data class BannerAd(
    val id: String = "",
    val title: String = "",
    val type: String = "IMAGE", // IMAGE, TEXT
    val imageUrl: String = "",
    val contentText: String = "",
    val actionLink: String = "",
    val targetCategory: String = "",
    val durationSeconds: Int = 5
)

data class CategoryItem(
    val id: String = "",
    val nameAr: String = "",
    val nameEn: String = "",
    val description: String = "",
    val iconName: String = "",
    val sortOrder: Int = 0
)

data class CityItem(
    val id: String = "",
    val nameAr: String = "",
    val nameEn: String = ""
)

data class ReportItem(
    val id: String = "",
    val providerId: String = "",
    val providerName: String = "",
    val reporterName: String = "",
    val reporterPhone: String = "",
    val complaintText: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class AppSupervisor(
    val username: String = "",
    val secretPin: String = "",
    val canApproveRequests: Boolean = true,
    val canManageCategories: Boolean = true,
    val canEditAds: Boolean = true,
    val canDeleteProviders: Boolean = true,
    val canReadReports: Boolean = true
)
