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
    val isPending: Boolean = false // Needs admin approval if true
)

@Entity(tableName = "admin_config")
data class AdminConfig(
    @PrimaryKey val id: Int = 1,
    val registrationConditions: String = "اللائحة التنظيمية المفتوحة: 1. الأمانة والمصداقية المطلقة مع العملاء. 2. الالتزام بالمواعيد ومستوى جودة الخدمة. 3. كرت المهنة والبطاقة الشخصية اختيارية للتوثيق لكن تزيد فرصة ظهورك كمهني موثوق. 4. يحق للإدارة حظر أي حساب في حال وجود شكاوي متكررة.",
    val welcomeMessage: String = "أهلاً بك يا غالي! أنا أبو يمن مساعدك الذكي للتنظيف والكهرباء والسباكة وكل الحرف. كيف أقدر أساعدك اليوم بخصوص أعطال البيت أو تسعير الخدمات؟ 🛠️⚡",
    val baseAppRateHourYER: Int = 4500, // standard guidance in Yemeni Rials
    val fontSizeModifier: Float = 1.0f,
    val secretKey: String = "9999" // Secret gate access code
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "user" or "ai"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
