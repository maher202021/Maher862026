package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CategoryEntity::class,
        ServiceProviderEntity::class,
        PendingProviderEntity::class,
        BannerEntity::class,
        ChatEntity::class,
        MessageEntity::class,
        AppConfigEntity::class,
        LoyaltyPointsEntity::class,
        ActivityLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun serviceProviderDao(): ServiceProviderDao
    abstract fun pendingProviderDao(): PendingProviderDao
    abstract fun bannerDao(): BannerDao
    abstract fun chatDao(): ChatDao
    abstract fun appConfigDao(): AppConfigDao
    abstract fun loyaltyPointsDao(): LoyaltyPointsDao
    abstract fun activityLogDao(): ActivityLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "yemen_services_db"
                ).addCallback(DatabaseCallback(context))
                 .fallbackToDestructiveMigration()
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Seed database inside a coroutine
            CoroutineScope(Dispatchers.IO).launch {
                val database = getDatabase(context)
                seedInitialData(database)
            }
        }
    }
}

suspend fun seedInitialData(db: AppDatabase) {
    // 1. Seed Categories
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
        db.categoryDao().insertCategory(cat)
    }

    // 2. Seed Banners (Marquee & Banner Promos)
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
        db.bannerDao().insertBanner(banner)
    }

    // 3. Seed Service Providers (VIP + Moosa Behem)
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
        db.serviceProviderDao().insertProvider(p)
    }

    // 4. Seed App Config
    db.appConfigDao().insertConfig(AppConfigEntity())

    // 5. Seed Loyalty Points
    db.loyaltyPointsDao().insertPoints(LoyaltyPointsEntity(points = 150))

    // 6. Seed initial log
    db.activityLogDao().insertLog(ActivityLogEntity(action = "تهيئة أولية لتطبيق كل خدمات اليمن وتنشيط البيانات الافتراضية"))
}
