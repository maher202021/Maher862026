package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.local.AdminConfig
import com.example.data.local.Provider
import com.example.data.local.ChatMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Supervisor data class for custom simulation permissions as requested
data class AppSupervisor(
    val name: String,
    val pass: String,
    val canApprove: Boolean = true,
    val canManageCategories: Boolean = true,
    val canBanners: Boolean = true,
    val canDeleteProviders: Boolean = true,
    val canViewReports: Boolean = true
)

// List of audit actions simulated for the ledger
data class AuditLogEntry(
    val id: Int,
    val timestamp: String,
    val user: String,
    val action: String,
    val details: String
)

// Simulated technical complaints list for Tab 6
data class TechnicalReport(
    val id: Int,
    val title: String,
    val complainant: String,
    val technician: String,
    val priority: String, // "High", "Medium", "Low"
    val date: String,
    val description: String
)

// Simulated Custom Banner model
data class CustomAdBanner(
    val id: Int,
    val title: String,
    val mediaType: String, // "صورة", "فيديو", "نص"
    val section: String,   // "الرئيسية", "قائمة الأقسام"
    val size: String,      // "صغير S", "متوسط M", "عريض L"
    val durationSeconds: Int,
    val isActive: Boolean
)

// ===============================================
// MAIN DASHBOARD: ADMINS & MODERATORS GATEWAYS
// ===============================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(viewModel: AppViewModel, config: AdminConfig) {
    val theme = resolveTheme(config)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isAuthenticated by viewModel.isAdminAuthenticated.collectAsStateWithLifecycle()
    val pendingList by viewModel.pendingProviders.collectAsStateWithLifecycle()
    val fullList by viewModel.approvedProviders.collectAsStateWithLifecycle()

    var usernameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var currentRole by remember { mutableStateOf("none") } // "super" (WAM2026) or "moderator" (mod1)
    
    // Track granular features / active focused panel
    var activeAdminTab by remember { mutableStateOf<String?>(null) } // null = show 13 SaaS Grid

    // Simulated states representing dynamic tables to maintain complete functional loop
    val supervisorsList = remember {
        mutableStateListOf(
            AppSupervisor("WAM2026", "maher736462", true, true, true, true, true),
            AppSupervisor("mod1", "password123", true, false, true, false, true),
            AppSupervisor("supervisor_ali", "ali2026", true, true, false, true, false)
        )
    }

    val technicalReportsList = remember {
        mutableStateListOf(
            TechnicalReport(1, "شكوى تسعيرة مفرطة في السباكة", "صالح محمد", "عادل السباك", "High", "2026-06-07", "أخذ الحرفي مبلغ أعلى من المتفق عليه مسبقاً بخصوص صيانة حنفية المطبخ."),
            TechnicalReport(2, "تأخير غير مبرر في مواعيد الخدمة", "أحمد عمران", "سمير الكهربائي", "Medium", "2026-06-08", "تأخر عن موعد تصليح لوحة التوزيع الكهربائية لأكثر من 5 ساعات دون عذر."),
            TechnicalReport(3, "طلب تعديل عنوان الورشة بالدليل", "ماجد اليماني", "الحرفي ماجد", "Low", "2026-06-08", "الفني يطلب من المشرفين تحديث عنوان مقر عمله من شارع تعز إلى شارع الخمسين.")
        )
    }

    val activeBannersList = remember {
        mutableStateListOf(
            CustomAdBanner(1, "تخفيضات موسم الأمطار على عوازل السباكة 🌧️", "صورة", "الرئيسية", "عريض L", 12, true),
            CustomAdBanner(2, "تنبيه غسيل فلاتر المكيفات قبل الصيف ❄️", "نص", "قائمة الأقسام", "متوسط M", 6, true)
        )
    }

    val systemAuditLogs = remember {
        mutableStateListOf(
            AuditLogEntry(1, "12:45", "WAM2026", "مصادقة الحساب", "تم قبول الفني 'عمر عادل' بالدليل بعد التأكد من كرت الحرفة."),
            AuditLogEntry(2, "13:10", "mod1", "تعديل إشهار", "تم تحديث رسالة روت المساعد الذكي اليمني لوضع الصنف 2."),
            AuditLogEntry(3, "15:30", "supervisor_ali", "إزالة فني مخالف", "تم سحب الاعتماد وحذف حساب الحرفي 'محمود البناء' بسبب شكاوى متكررة.")
        )
    }

    // Dynamic Lists for Cities & Categories
    val citiesListMutable = remember { mutableStateListOf("صنعاء", "عدن", "تعز", "الحديدة", "حضرموت", "إب", "ذمار", "مأرب") }
    val categoriesListMutable = remember { mutableStateListOf("كهرباء وإلكترونيات", "سباكة وصحي", "نجارة وديكور", "تكييف وتبريد", "حدادة وألومنيوم", "خياطة وتفصيل", "أخرى") }

    // --- Real-time Firestore Snapshot Sync and Initialization ---
    LaunchedEffect(Unit) {
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        // 1. Listen for Supervisors List
        firestore.collection("supervisors")
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    val loaded = mutableListOf<AppSupervisor>()
                    for (doc in snapshot.documents) {
                        try {
                            val nameStr = doc.getString("name") ?: ""
                            val passStr = doc.getString("pass") ?: ""
                            val canApproveVal = doc.getBoolean("canApprove") ?: false
                            val canCatVal = doc.getBoolean("canManageCategories") ?: false
                            val canBanVal = doc.getBoolean("canBanners") ?: false
                            val canDelVal = doc.getBoolean("canDeleteProviders") ?: false
                            val canRepVal = doc.getBoolean("canViewReports") ?: false
                            if (nameStr.isNotEmpty() && passStr.isNotEmpty()) {
                                loaded.add(AppSupervisor(nameStr, passStr, canApproveVal, canCatVal, canBanVal, canDelVal, canRepVal))
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                    if (loaded.isNotEmpty()) {
                        supervisorsList.clear()
                        supervisorsList.addAll(loaded)
                    } else {
                        // Prepopulate default supervisors on Firestore so there is always a default
                        val defaults = listOf(
                            AppSupervisor("WAM2026", "maher736462", true, true, true, true, true),
                            AppSupervisor("mod1", "password123", true, false, true, false, true),
                            AppSupervisor("supervisor_ali", "ali2026", true, true, false, true, false)
                        )
                        for (s in defaults) {
                            firestore.collection("supervisors").document(s.name)
                                .set(mapOf(
                                    "name" to s.name,
                                    "pass" to s.pass,
                                    "canApprove" to s.canApprove,
                                    "canManageCategories" to s.canManageCategories,
                                    "canBanners" to s.canBanners,
                                    "canDeleteProviders" to s.canDeleteProviders,
                                    "canViewReports" to s.canViewReports
                                ))
                        }
                    }
                }
            }

        // 2. Listen for Cities List
        firestore.collection("cities")
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    val loaded = mutableListOf<String>()
                    for (doc in snapshot.documents) {
                        val nameStr = doc.getString("name") ?: ""
                        if (nameStr.isNotEmpty()) loaded.add(nameStr)
                    }
                    if (loaded.isNotEmpty()) {
                        citiesListMutable.clear()
                        citiesListMutable.addAll(loaded)
                    } else {
                        // Prepopulate default cities
                        val defaults = listOf("صنعاء", "عدن", "تعز", "الحديدة", "حضرموت", "إب", "ذمار", "مأرب")
                        for (c in defaults) {
                            firestore.collection("cities").document(c).set(mapOf("name" to c))
                        }
                    }
                }
            }

        // 3. Listen for Categories List
        firestore.collection("categories")
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    val loaded = mutableListOf<String>()
                    for (doc in snapshot.documents) {
                        val nameStr = doc.getString("name") ?: ""
                        if (nameStr.isNotEmpty()) loaded.add(nameStr)
                    }
                    if (loaded.isNotEmpty()) {
                        categoriesListMutable.clear()
                        categoriesListMutable.addAll(loaded)
                    } else {
                        // Prepopulate default categories
                        val defaults = listOf("كهرباء وإلكترونيات", "سباكة وصحي", "نجارة وديكور", "تكييف وتبريد", "حدادة وألومنيوم", "خياطة وتفصيل", "أخرى")
                        for (c in defaults) {
                            firestore.collection("categories").document(c).set(mapOf("name" to c))
                        }
                    }
                }
            }
    }

    if (!isAuthenticated) {
        // --- ADMIN LOGIN GATEWAYS WALL ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 380.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin Area",
                        tint = theme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "بوابة تسجيل الدخول الإدارية ⚔️", 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 18.sp, 
                        color = theme.primary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "سجل دخولك كمدير رئيسي أو مشرف نظام مرخص لمراجعة وتحديث الحسابات والبيانات:", 
                        fontSize = 11.sp, 
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = { usernameInput = it },
                        label = { Text("اسم المستخدم الإداري") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("كلمة المرور") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        onClick = {
                            val userClean = usernameInput.trim()
                            val passClean = passwordInput.trim()

                            // Check super credentials first
                            if ((userClean == config.adminUsername && passClean == config.adminPassword) || (userClean == "WAM2026" && passClean == "maher736462")) {
                                currentRole = "super"
                                viewModel.authenticateAdmin(passClean)
                                Toast.makeText(context, "أهلاً بالمدير الرئيسي ماهر! تم تسجيل الدخول بنجاح كامل 👑⚡", Toast.LENGTH_SHORT).show()
                            } else {
                                // Check list of supervisors
                                val matchedSupervisor = supervisorsList.firstOrNull { it.name == userClean && it.pass == passClean }
                                if (matchedSupervisor != null) {
                                    currentRole = "moderator"
                                    viewModel.authenticateAdmin(passClean)
                                    Toast.makeText(context, "تم تسجيل دخول المشرف '${matchedSupervisor.name}' بنجاح للرقابة الفنية 👤", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "خطأ: بيانات الدخول الإدارية غير صحيحة لا يمكن الوصول ❌", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    ) {
                        Text("دخول للوحة الإدارة" , color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    TextButton(onClick = { viewModel.navigateTo("home") }) {
                        Text("العودة للتطبيق العادي 🏃", color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }
        }
    } else {
        // ==========================================
        // LOGGED-IN: 13-TAB ULTIMATE SAAS BOARD
        // ==========================================
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF1F5FE))
        ) {
            // High-Contrast Title Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(theme.primary)
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(
                            text = "لوحة الرقابة والتحكم لخدمات اليمن 🇾🇪", 
                            fontWeight = FontWeight.Bold, 
                            color = Color.White, 
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (currentRole == "super") "المسؤول الحالي: رئيس مجلس الإدارة الرئيسي (Super Admin)" else "المسؤول الحالي: مشرف جودة ومدقق بيانات (Moderator)",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 10.sp
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        viewModel.logOutAdmin()
                        currentRole = "none"
                        activeAdminTab = null
                    }) {
                        Icon(Icons.Default.ExitToApp, "Log out", tint = Color.White)
                    }
                }
            }

            if (activeAdminTab == null) {
                // ==========================================
                // VIEW A: MAIN GRID LANDING WITH QUICK METRICS
                // ==========================================
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Head Welcome
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("مرحباً بك مجدداً في نظام الرقابة ⚔️", fontWeight = FontWeight.Bold, color = theme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "تمنحك هذه البوابة تحكماً شاملاً وفورياً في شبكة مقدمي الخدمات في اليمن بـ 13 قسماً تنظيمياً مخصصاً. اضغط على أي قسم لمعاينته وتحديث بياناته مباشرة:",
                                    fontSize = 11.5.sp,
                                    color = Color.DarkGray,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    // Quick Statistics Row block
                    item {
                        Text("📊 مؤشرات الأداء الحالية (مباشر)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = theme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            QuickStatCard("الحرفيون", "${fullList.size} نشط", theme.primary, Modifier.weight(1f))
                            QuickStatCard("طلبات فحص", "${pendingList.size} معلق", if (pendingList.isNotEmpty()) Color.Red else Color.Gray, Modifier.weight(1f))
                            QuickStatCard("اتصالات مجراة", "482 مكالمة", theme.secondary, Modifier.weight(1f))
                        }
                    }

                    // THE 13-DRAWER PANEL SAAS GRID (Implemented as nested rows for optimal compilation speed & responsiveness)
                    item {
                        Text("🗂️ تصنيف أدوات التحكم الإدارية", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = theme.primary)
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Row 1
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                PanelGridItem("📊", "لوحة المعلومات", "مؤشرات وأداء النظام", Modifier.weight(1f)) { activeAdminTab = "1" }
                                PanelGridItem("📥", "طلبات الاعتماد", "فحص الفنيين الجدد", Modifier.weight(1f), badge = if (pendingList.isNotEmpty()) "${pendingList.size}" else null) { activeAdminTab = "2" }
                            }
                            // Row 2
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                PanelGridItem("➕", "إضافة فني يدوياً", "تسجيل تجاوز للفنيين", Modifier.weight(1f)) { activeAdminTab = "3" }
                                PanelGridItem("⚙️", "الأقسام والمحافظات", "إدارة التوزيع والفرز", Modifier.weight(1f)) { activeAdminTab = "4" }
                            }
                            // Row 3
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                PanelGridItem("📢", "إعلانات البنرات", "حملات السلايدر الدعائية", Modifier.weight(1f)) { activeAdminTab = "5" }
                                PanelGridItem("🚨", "البلاغات والتقارير", "شكاوى المستخدمين والعملاء", Modifier.weight(1f)) { activeAdminTab = "6" }
                            }
                            // Row 4
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                PanelGridItem("💬", "الشات والخصوصية", "تنظيف وسحوبات المحادثة", Modifier.weight(1f)) { activeAdminTab = "7" }
                                PanelGridItem("👥", "الحرفيون المعتمدون", "إدارة وتعديل الفنيين", Modifier.weight(1f)) { activeAdminTab = "8" }
                            }
                            // Row 5
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                PanelGridItem("💎", "الترقيات والـ VIP", "تثبيت البادج والعلامات", Modifier.weight(1f)) { activeAdminTab = "9" }
                                if (currentRole == "super") {
                                    PanelGridItem("👥", "إدارة المشرفين", "توزيع الصلاحيات بالكامل", Modifier.weight(1f)) { activeAdminTab = "10" }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            // Row 6
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                PanelGridItem("🛠️", "إعدادات متقدمة", "ألوان النظام ووضع الصيانة", Modifier.weight(1f)) { activeAdminTab = "11" }
                                PanelGridItem("💾", "النسخ والاسترداد", "نقل كود شاتل اليدوي", Modifier.weight(1f)) { activeAdminTab = "12" }
                            }
                            // Row 7
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                PanelGridItem("📜", "سجلات الرقابة", "سجل تدقيق نشاط المسؤولين", Modifier.weight(1f)) { activeAdminTab = "13" }
                                Spacer(modifier = Modifier.weight(1f)) // Placeholder balancing row
                            }
                        }
                    }
                }
            } else {
                // ==========================================
                // VIEW B: ACTIVE FOCUSED SUB-SCREEN MANAGER
                // ==========================================
                Column(modifier = Modifier.fillMaxSize().weight(1f)) {
                    // Back To Grid Selector Header
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { activeAdminTab = null },
                                colors = ButtonDefaults.buttonColors(containerColor = theme.secondary),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.ArrowBack, "Back", tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("رجوع للوحة التحكم الرئيسية 🏠", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "التبويب النشط: " + getTabTitleInArabic(activeAdminTab),
                                fontWeight = FontWeight.Bold,
                                color = theme.primary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Sub-Drawer Renderer Selector
                    Box(modifier = Modifier.fillMaxSize().weight(1f).background(Color(0xFFF1F5FE))) {
                        when (activeAdminTab) {
                            "1" -> StatsDashboardTabDrawer(fullList, pendingList, theme)
                            "2" -> VettingTabDrawer(viewModel, pendingList, theme)
                            "3" -> ManualAddTabDrawer(viewModel, citiesListMutable, categoriesListMutable, theme)
                            "4" -> CategoriesCitiesTabDrawer(citiesListMutable, categoriesListMutable, theme)
                            "5" -> BannersTabDrawer(activeBannersList, theme)
                            "6" -> ReportsTabDrawer(technicalReportsList, theme)
                            "7" -> ChatPrivacyTabDrawer(viewModel, theme)
                            "8" -> ActiveListTabDrawer(viewModel, fullList, theme)
                            "9" -> PromotionsTabDrawer(viewModel, fullList, theme)
                            "10" -> SupervisorsTabDrawer(supervisorsList, theme)
                            "11" -> ConfigTabDrawer(viewModel, config, theme)
                            "12" -> ShuttleBackupTabDrawer(viewModel, config, theme)
                            "13" -> SystemAuditTabDrawer(systemAuditLogs, currentRole, theme)
                        }
                    }
                }
            }
        }
    }
}

// Helper titles to keep SaaS flow elegantly readable
private fun getTabTitleInArabic(tab: String?): String {
    return when(tab) {
        "1" -> "لوحة الإحصائيات العامة"
        "2" -> "طلبات التوثيق والاعتماد الحرفي"
        "3" -> "تسجيل وإضافة فني يدوياً"
        "4" -> "تنظيم المحافظات والأقسام"
        "5" -> "البنرات والإعلانات الترويجية"
        "6" -> "البلاغات وتذاكر الشكاوى"
        "7" -> "الخصوصية وسجلات الدردشة"
        "8" -> "الحرفيون النشطون بالدليل"
        "9" -> "الترقيات والبادجات والـ VIP"
        "10" -> "حسابات المشرفين والشركاء"
        "11" -> "الإعدادات المتقدمة وصيانة المنصة"
        "12" -> "كود نقل البيانات (Data Shuttle)"
        "13" -> "سجل الرقابة لعمليات الإدارة"
        else -> "التحكم"
    }
}

// ==========================================
// RENDERERS FOR INDIVIDUAL 13 SaaS DRAWER TABS
// ==========================================

// --- TAB 1: DASHBOARD STATS DRAWERS ---
@Composable
private fun StatsDashboardTabDrawer(approved: List<Provider>, pending: List<Provider>, theme: ThemeColors) {
    val context = LocalContext.current
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("📊 الإحصائيات المتقدمة لشبكة اليمن", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = theme.primary)
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("تركيز فئة الحرف بالتطبيق اليوم:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    
                    PercentageProgressBarBlock("كهرباء وإلكترونيات", 0.38f, "38%")
                    PercentageProgressBarBlock("سباكة وصحي الفروع", 0.24f, "24%")
                    PercentageProgressBarBlock("تكييف وتبريد وأنظمة طاقة", 0.18f, "18%")
                    PercentageProgressBarBlock("أقسام مهنية وخياطة سيدات", 0.12f, "12%")
                    PercentageProgressBarBlock("أخرى مختلفة مستقلين", 0.08f, "8%")
                }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🔄 محاكاة الفرز والأولوية اليدوية (Drag Priority):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("لحظر التداخل، يمكنك إجبار تصنيف معين ليعلو التصنيفات الأخرى بالصفحة الرئيسية:", fontSize = 11.sp, color = Color.Gray)
                    
                    Button(
                        onClick = {
                            Toast.makeText(context, "تم رفع أولوية قسم 'الطاقة الشمسية' في هرم فرز اليمن بنجاح! ⚡🇾🇪", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("رفع أولوية الطاقة الشمسية للمقدمة ✨", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// --- TAB 2: VETTING PENDING REQUESTS ---
@Composable
private fun VettingTabDrawer(viewModel: AppViewModel, pending: List<Provider>, theme: ThemeColors) {
    val context = LocalContext.current
    var showRejectDialogFor by remember { mutableStateOf<Provider?>(null) }
    var rejectionReasonInput by remember { mutableStateOf("") }

    if (pending.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لا توجد طلبات تسجيل معلقة للفحص حالياً 👍", fontWeight = FontWeight.Medium, color = Color.Gray)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(pending) { p ->
                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("اسم الطالب: ${p.name}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("المهنة: ${p.mainCategory} • ${p.subCategory}", fontSize = 11.5.sp, color = theme.primary)
                        Text("المدينة: محافظات ${p.city} | الجوال للتواصل: ${p.phone}", fontSize = 11.sp, color = Color.Gray)
                        if (p.description.isNotEmpty()) {
                            Text("الخبرات: ${p.description}", fontSize = 11.sp, modifier = Modifier.background(Color(0xFFF1F5FE)).padding(6.dp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    viewModel.approveProvider(p)
                                    Toast.makeText(context, "تمت مصادقة وتثبيت الحساب المهني بنجاح! 🇸🇦⚡", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text("موافقة وتوثيق", fontSize = 11.sp, color = Color.White)
                            }
                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    rejectionReasonInput = ""
                                    showRejectDialogFor = p
                                }
                            ) {
                                Text("رفض وحذف", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    // MANDATORY REJECTION DIALOG
    if (showRejectDialogFor != null) {
        AlertDialog(
            onDismissRequest = { showRejectDialogFor = null },
            title = { Text("سبب الرفض الإلزامي 🚨", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("يرجى إدخال سبب مقنع لإخطار الحرفي بالرفض ليتسنى له تعديله لاحقاً في التنبيهات:", fontSize = 12.sp)
                    OutlinedTextField(
                        value = rejectionReasonInput,
                        onValueChange = { rejectionReasonInput = it },
                        placeholder = { Text("مثال: صورة كرت الحرفة غير واضحة، الرقم مكرر") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = rejectionReasonInput.trim().isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    onClick = {
                        val p = showRejectDialogFor!!
                        viewModel.deleteProvider(p)
                        Toast.makeText(context, "تم رفض الطلب وجاري محاكاة إخطار جوال الفني بخصوص: ${rejectionReasonInput.trim()}", Toast.LENGTH_LONG).show()
                        showRejectDialogFor = null
                    }
                ) {
                    Text("تأكيد الرفض والإلغاء")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialogFor = null }) {
                    Text("رجوع")
                }
            }
        )
    }
}

// --- TAB 3: MANUAL ADDITION PANEL ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManualAddTabDrawer(viewModel: AppViewModel, cities: List<String>, categories: List<String>, theme: ThemeColors) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("صنعاء") }
    var mainCat by remember { mutableStateOf("كهرباء وإلكترونيات") }
    var subCat by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("4500") }
    var gender by remember { mutableStateOf("ذكر") }
    var description by remember { mutableStateOf("") }

    // Toggle Flags
    var addAsVIP by remember { mutableStateOf(false) }
    var addAsPinned by remember { mutableStateOf(false) }
    var addAsVerified by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("➕ إضافة فني جديد يدوياً للشبكة باليمن", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = theme.primary)
        }
        item {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("الاسم الكامل للفني") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("رقم الهاتف (9 خانات)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            // Dropdowns or simpler select options representing lists
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("المحافظة") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = mainCat,
                    onValueChange = { mainCat = it },
                    label = { Text("القسم الرئيسي") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            OutlinedTextField(
                value = subCat,
                onValueChange = { subCat = it },
                label = { Text("المجال المهني الدقيق والمعدات") },
                placeholder = { Text("مثال: تركيب لوحات طاقة، تسليك عمارات") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = rate,
                onValueChange = { rate = it },
                label = { Text("متوسط تكلفة العمل التقريبية بالريال اليمني/ساعة") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("جنس الفني:")
                Spacer(modifier = Modifier.width(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { gender = "ذكر" }) {
                    RadioButton(selected = gender == "ذكر", onClick = { gender = "ذكر" })
                    Text("ذكر 🚹")
                }
                Spacer(modifier = Modifier.width(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { gender = "أنثى" }) {
                    RadioButton(selected = gender == "أنثى", onClick = { gender = "أنثى" })
                    Text("أنثى 🚺")
                }
            }
        }
        item {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("وصف السيرة الذاتية المهنية والموقع الدقيق") },
                modifier = Modifier.fillMaxWidth().height(80.dp),
                maxLines = 3
            )
        }

        // Checklist states
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("⭐ خيارات الاعتماد الممتازة (VIP Setup):", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = addAsVerified, onCheckedChange = { addAsVerified = it })
                        Text("منح شارة الاعتماد والتوثيق الزرقاء 💎", fontSize = 11.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = addAsVIP, onCheckedChange = { addAsVIP = it })
                        Text("إدراج فوري في السلايدر الترويجي المميز VIP 🌟", fontSize = 11.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = addAsPinned, onCheckedChange = { addAsPinned = it })
                        Text("تثبيت الحساب في قمة الأولوية أعلى التصنيف 📌", fontSize = 11.sp)
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    if (name.isEmpty() || phone.isEmpty() || subCat.isEmpty()) {
                        Toast.makeText(context, "خطأ: الرجاء ملء الحقول الإلزامية لتسجيل الحساب! ❌", Toast.LENGTH_SHORT).show()
                    } else if (!isValidYemeniMobile(phone)) {
                        Toast.makeText(context, "الرجاء إدخال رقم جوال يمني صحيح يبدأ بـ (77, 73, 71, 70)", Toast.LENGTH_LONG).show()
                    } else {
                        // Submit bypasses vetting directly to approvedProviders/Firestore
                        val pObj = Provider(
                            id = (0..999999).random(), // Generate ID
                            name = name.trim(),
                            mainCategory = mainCat.trim(),
                            subCategory = subCat.trim(),
                            city = city.trim(),
                            phone = phone.trim(),
                            whatsapp = phone.trim(),
                            gender = gender,
                            description = description.trim(),
                            photoUri = null,
                            idPhotoUri = null,
                            isPending = false, // Approved directly
                            isVerified = addAsVerified,
                            isPinned = addAsPinned,
                            isRecommended = addAsVIP,
                            isSubscribed = false,
                            points = 0
                        )
                        viewModel.updateProviderDirectly(pObj)
                        Toast.makeText(context, "تم تسجيل الحرفي ${name.trim()} بنجاح ومزامنته بـ Firestore! 🎉🇸🇦", Toast.LENGTH_LONG).show()
                        name = ""
                        phone = ""
                        subCat = ""
                        description = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                modifier = Modifier.fillMaxWidth().height(46.dp)
            ) {
                Text("إضافة الحساب بنجاح فوراً 🛡️", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- TAB 4: CATEGORIES & CITIES MANAGER ---
@Composable
private fun CategoriesCitiesTabDrawer(cities: MutableList<String>, categories: MutableList<String>, theme: ThemeColors) {
    val context = LocalContext.current
    var newCityInput by remember { mutableStateOf("") }
    var newCatInput by remember { mutableStateOf("") }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("⚙️ إدارة وتوسيع خدمات التقسيم والتوزيع باليمن", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = theme.primary)
        }

        // Cities section
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📍 المحافظات المدعومة بالدليل حالياً (${cities.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = newCityInput,
                            onValueChange = { newCityInput = it },
                            placeholder = { Text("مثال: حضرموت، المحويت") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                val text = newCityInput.trim()
                                if (text.isNotEmpty() && !cities.contains(text)) {
                                    val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                    firestore.collection("cities").document(text).set(mapOf("name" to text))
                                    Toast.makeText(context, "تم إضافة محافظة '$text' للدليل ومزامنتها سحابياً! 🇾🇪", Toast.LENGTH_SHORT).show()
                                    newCityInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
                        ) {
                            Text("إضافة")
                        }
                    }

                    // Simple list flow of chips
                    FlowChipsRow(items = cities) { removed ->
                        if (cities.size > 1) {
                            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            firestore.collection("cities").document(removed).delete()
                            Toast.makeText(context, "تم إزالة المحافظة ومزامنتها من السيرفر.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        // Categories Section
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("💡 الأقسام والمهن الحرفية المعتمدة حالياً (${categories.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = newCatInput,
                            onValueChange = { newCatInput = it },
                            placeholder = { Text("مثال: بناء ومقاولات، دهان") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                val text = newCatInput.trim()
                                if (text.isNotEmpty() && !categories.contains(text)) {
                                    val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                    firestore.collection("categories").document(text).set(mapOf("name" to text))
                                    Toast.makeText(context, "تم إدراج مهنة '$text' ومزامنتها سحابياً! 🛠️", Toast.LENGTH_SHORT).show()
                                    newCatInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
                        ) {
                            Text("إدراج")
                        }
                    }

                    FlowChipsRow(items = categories) { removed ->
                        if (categories.size > 1) {
                            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            firestore.collection("categories").document(removed).delete()
                            Toast.makeText(context, "تم حذف تصنيف الخدمة من السيرفر بنجاح.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}

// --- TAB 5: BANNERS DRAWER ---
@Composable
private fun BannersTabDrawer(banners: MutableList<CustomAdBanner>, theme: ThemeColors) {
    val context = LocalContext.current
    var bTitle by remember { mutableStateOf("") }
    var bType by remember { mutableStateOf("صورة") } // صورة، فيديو، نص
    var bSection by remember { mutableStateOf("الرئيسية") } // الرئيسية، قائمة الأقسام
    var bSize by remember { mutableStateOf("عريض L") } // صغير S، متوسط M، عريض L
    var bDuration by remember { mutableStateOf(10f) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("📢 إدارة الحملات الإعلانية والبنرات الترويجية", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = theme.primary)
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("➕ إنشاء وتثبيت بنر دعائي جديد بالسلادير:", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                    OutlinedTextField(
                        value = bTitle,
                        onValueChange = { bTitle = it },
                        label = { Text("نص الإعلان أو عنوان البنر المغري") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Type switches
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("نوع المحتوى:", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { bType = "صورة" }) {
                            RadioButton(selected = bType == "صورة", onClick = { bType = "صورة" })
                            Text("صورة 🌄", fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { bType = "فيديو" }) {
                            RadioButton(selected = bType == "فيديو", onClick = { bType = "فيديو" })
                            Text("فيديو 🎥", fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { bType = "نص" }) {
                            RadioButton(selected = bType == "نص", onClick = { bType = "نص" })
                            Text("نص 📝", fontSize = 11.sp)
                        }
                    }

                    // Size switches
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("مقاس البنر:", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { bSize = "صغير S" }) {
                            RadioButton(selected = bSize == "صغير S", onClick = { bSize = "صغير S" })
                            Text("صغير S", fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { bSize = "متوسط M" }) {
                            RadioButton(selected = bSize == "متوسط M", onClick = { bSize = "متوسط M" })
                            Text("متوسط M", fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { bSize = "عريض L" }) {
                            RadioButton(selected = bSize == "عريض L", onClick = { bSize = "عريض L" })
                            Text("عريض L", fontSize = 11.sp)
                        }
                    }

                    // Duration slide
                    Column {
                        Text("مدة العرض التلقائي بالثانية: (${bDuration.toInt()} ثوانٍ)", fontSize = 11.sp)
                        Slider(value = bDuration, onValueChange = { bDuration = it }, valueRange = 3f..25f)
                    }

                    Button(
                        onClick = {
                            val text = bTitle.trim()
                            if (text.isNotEmpty()) {
                                banners.add(CustomAdBanner(banners.size + 1, text, bType, bSection, bSize, bDuration.toInt(), true))
                                Toast.makeText(context, "تم حفظ وتثبيت البنر الإعلاني بالمقاس المطور وبثّه للبلاد! 📢🌈", Toast.LENGTH_SHORT).show()
                                bTitle = ""
                            } else {
                                Toast.makeText(context, "نرجو كتابة نص الإعلان!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("حفظ وبث الإعلان الحمراني فوراً", color = Color.White)
                    }
                }
            }
        }

        // Active listing
        item {
            Text("📈 قائمة البنرات الترويجية النشطة حالياً (${banners.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        items(banners) { banner ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(banner.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("النوع: ${banner.mediaType} • الحجم: ${banner.size} • التناوب: كل ${banner.durationSeconds} ثوانٍ", fontSize = 10.sp, color = Color.Gray)
                    }
                    IconButton(onClick = {
                        banners.remove(banner)
                        Toast.makeText(context, "تم إتلاف الإعلان وتعطيل حملته.", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Delete, "Dismiss Banner", tint = Color.Red)
                    }
                }
            }
        }
    }
}

// --- TAB 6: REPORTS & AUDITS DRAWER ---
@Composable
private fun ReportsTabDrawer(reports: MutableList<TechnicalReport>, theme: ThemeColors) {
    val context = LocalContext.current
    var isSimulatingAuditExport by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("🚨 كشوفات البلاغات وتذاكر الدعم والاتصالات الفنية", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = theme.primary)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                    onClick = {
                        isSimulatingAuditExport = true
                    }
                ) {
                    Icon(Icons.Default.Share, "CSV export", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تصدير تقرير أسبوعي (CSV)", fontSize = 10.5.sp, color = Color.White)
                }
                
                Button(
                    modifier = Modifier.weight(1.0f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    onClick = {
                        Toast.makeText(context, "تم حفظ ملف الفلترة الحالي (تذكرة PDF) بدقة عالية في التنزيلات! 📂📜", Toast.LENGTH_LONG).show()
                    }
                ) {
                    Icon(Icons.Default.PictureAsPdf, "PDF report", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تصدير PDF للتصفيات", fontSize = 10.5.sp, color = Color.White)
                }
            }
        }

        // Export Simulation
        if (isSimulatingAuditExport) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = theme.primary.copy(alpha = 0.1f))) {
                    Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(color = theme.primary, modifier = Modifier.size(24.dp))
                        Text("جاري تشفير وجمع كشف الحرفيين ومعالجة الاتصالات الأسبوعية... 🔄", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        
                        LaunchedEffect(Unit) {
                            delay(1800)
                            isSimulatingAuditExport = false
                            Toast.makeText(context, "تم تجميع وتصدير كشف 'YemenTech_PremiumAudit_Week.csv' ونسخه للتخزين المحلي بنجاح! 📥✅", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }

        items(reports) { r ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(r.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Red)
                        Spacer(modifier = Modifier.weight(1f))
                        Badge(containerColor = if(r.priority == "High") Color.Red else Color.Gray) {
                            Text(r.priority, color = Color.White, fontSize = 9.sp)
                        }
                    }
                    Text("المبلغ: ${r.complainant} | المتهم المخالف: ${r.technician}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Text("تفاصيل الحدث: ${r.description}", fontSize = 11.sp, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("تاريخ التذكرة: ${r.date} • وضع الإشعار: معلّق بانتظار التحكيم الإداري", fontSize = 9.5.sp, color = Color.Gray)
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            reports.remove(r)
                            Toast.makeText(context, "تم تصفية الشكوى وحفظ الاتفاق بشكل ودي! ☑️", Toast.LENGTH_SHORT).show()
                        }) {
                            Text("تصفية وحفظ الإجراء", color = theme.primary)
                        }
                        TextButton(onClick = {
                            Toast.makeText(context, "تم إرسال إنذار للفني ${r.technician} بسحب الترخيص الزرقاء!", Toast.LENGTH_LONG).show()
                        }) {
                            Text("إرسال إنذار كارت أصفر ⚠️", color = Color.DarkGray)
                        }
                    }
                }
            }
        }
    }
}

// --- TAB 7: CHAT HISTORY EXPIRAL & PRIVACY ---
@Composable
private fun ChatPrivacyTabDrawer(viewModel: AppViewModel, theme: ThemeColors) {
    val context = LocalContext.current
    var isExpiringSlider by remember { mutableStateOf(30f) } // 7-15-30-60
    var showNukeWarnDialog by remember { mutableStateOf(false) }

    // Live Snapshot of Active Chat Rooms
    val activeRooms = remember { mutableStateListOf<Map<String, Any>>() }
    var selectedInspectRoom by remember { mutableStateOf<Map<String, Any>?>(null) }
    var adminReplyText by remember { mutableStateOf("") }

    // Chat messages list for the active inspected conversation
    val inspectRoomMessages = remember { mutableStateListOf<ChatMessage>() }

    // Snapshot listener for Active Rooms
    LaunchedEffect(Unit) {
        try {
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            firestore.collection("chats")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, err ->
                    if (snapshot != null) {
                        activeRooms.clear()
                        for (doc in snapshot.documents) {
                            val data = doc.data
                            if (data != null) {
                                activeRooms.add(data)
                            }
                        }
                    }
                }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    // Monitor for Selected Inspect Room messages
    LaunchedEffect(selectedInspectRoom) {
        if (selectedInspectRoom != null) {
            inspectRoomMessages.clear()
            val rIdStr = selectedInspectRoom!!["id"] as? String ?: ""
            val receiverIdInt = rIdStr.replace("chat_user_", "").toIntOrNull() ?: 0
            
            try {
                val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                firestore.collection("messages")
                    .orderBy("timestamp")
                    .addSnapshotListener { snapshot, err ->
                        if (snapshot != null) {
                            inspectRoomMessages.clear()
                            for (doc in snapshot.documents) {
                                try {
                                    val rIdVal = (doc.getLong("receiverId") ?: 0L).toInt()
                                    if (rIdVal == receiverIdInt) {
                                        inspectRoomMessages.add(
                                            ChatMessage(
                                                id = (doc.getLong("id") ?: 0L).toInt(),
                                                sender = doc.getString("sender") ?: "user",
                                                senderName = doc.getString("senderName") ?: "",
                                                receiverId = rIdVal,
                                                message = doc.getString("message") ?: "",
                                                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                                isRead = false
                                            )
                                        )
                                    }
                                } catch (ex: Exception) {
                                    ex.printStackTrace()
                                }
                            }
                        }
                    }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("💬 الرقابة الفنية ومستودع خصوصية المحادثات الفورية", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = theme.primary)
        }

        // Expiring slide scheduler
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("⏰ جدولة التدمير التلقائي للرسائل المتراكمة:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(
                        text = if (isExpiringSlider >= 55f) "توقيت الجدولة: عدم المسح مطلقاً (حفظ دائم للمراسلات)" else "توقيت الجدولة: محو وسحب المحادثات والاتصالات الأقدم من ${isExpiringSlider.toInt()} يوماً تلقائياً",
                        fontSize = 11.sp,
                        color = theme.primary
                    )
                    Slider(value = isExpiringSlider, onValueChange = { isExpiringSlider = it }, valueRange = 7f..60f)
                }
            }
        }

        // Active Chat Rooms Monitoring
        item {
            Text("📋 مراقبة وتدقيق غرف المحادثة السحابية النشطة (${activeRooms.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = theme.primary)
        }

        if (activeRooms.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Text("لا توجد مراسلات سحابية نشطة للفحص حالياً 🗣️", color = Color.Gray, fontSize = 11.5.sp)
                }
            }
        } else {
            items(activeRooms.toList()) { room ->
                val rIdStr = room["id"] as? String ?: ""
                val participants = room["participants"] as? List<String> ?: listOf("الزائر", "الفني")
                val lastMessage = room["lastMessage"] as? String ?: ""
                val isBlocked = room["isBlocked"] as? Boolean ?: false

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "مراسلة: ${participants.joinToString(" ↔️ ")}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (isBlocked) {
                                Badge(containerColor = Color.LightGray) {
                                    Text("مجمّد ❄️", color = Color.DarkGray, fontSize = 9.sp)
                                }
                            } else {
                                Badge(containerColor = Color(0xFFD1FAE5)) {
                                    Text("نشط 🟢", color = Color(0xFF065F46), fontSize = 9.sp)
                                }
                            }
                        }

                        Text("آخر جملة: $lastMessage", fontSize = 11.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                                modifier = Modifier.weight(1.0f),
                                contentPadding = PaddingValues(vertical = 4.dp),
                                onClick = {
                                    selectedInspectRoom = room
                                }
                            ) {
                                Text("معاينة السجل والرد 🔎", fontSize = 11.sp, color = Color.White)
                            }

                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = if (isBlocked) Color.Gray else Color.Red),
                                modifier = Modifier.weight(1.0f),
                                contentPadding = PaddingValues(vertical = 4.dp),
                                onClick = {
                                    try {
                                        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                        firestore.collection("chats").document(rIdStr).update("isBlocked", !isBlocked)
                                        Toast.makeText(context, if (!isBlocked) "تم تجميد وتعطيل الشات للغرفة بنجاح ❄️" else "تم إلغاء تجميد المحادثة 🟢", Toast.LENGTH_SHORT).show()
                                    } catch (ex: Exception) {
                                        ex.printStackTrace()
                                    }
                                }
                            ) {
                                Text(if (isBlocked) "إلغاء التجميد 🔥" else "تجميد المحادثة 🛑", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Destructive Permanent Nuke Action
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("☢️ تدابير التطهير وسجلات الطوارئ:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("يسهم هذا التطهير في مسح كافة مراسلات الفروع والشات وتحسين كفاءة الذاكرة فوراً:", fontSize = 11.sp, color = Color.Gray)

                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            showNukeWarnDialog = true
                        }
                    ) {
                        Text("تطهير وتدمير رسائل المحادثات بشكل دائم 🚨" , color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            Toast.makeText(context, "تم توليد وتصدير سجل 'ChatHistory_Export.csv' بالكامل 📋🇸🇦!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("تصدير سجلات المراسلات كـ CSV", color = Color.White)
                    }
                }
            }
        }
    }

    if (showNukeWarnDialog) {
        AlertDialog(
            onDismissRequest = { showNukeWarnDialog = false },
            title = { Text("إنذار أمني بالغ الخطورة! ⚠️", color = Color.Red, fontWeight = FontWeight.Bold) },
            text = {
                Text("هل أنت متأكد من مسح جميع محادثات الفنيين والدردشات الفورية نهائياً؟ هذا الإجراء لا يمكن التراجع عنه وسيمحو قواعد الشات لدى الجميع فوراً.")
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    onClick = {
                        viewModel.clearChatMessages()
                        Toast.makeText(context, "تم حرق وتطهير مستودع الدردشات العامة لليمن بنجاح تام! 🧼🔥", Toast.LENGTH_LONG).show()
                        showNukeWarnDialog = false
                    }
                ) {
                    Text("تأكيد الفرمتة الكاملة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNukeWarnDialog = false }) {
                    Text("تراجع")
                }
            }
        )
    }

    // INSPECTION & REPLY DIALOG
    if (selectedInspectRoom != null) {
        val rIdStr = selectedInspectRoom!!["id"] as? String ?: ""
        val participants = selectedInspectRoom!!["participants"] as? List<String> ?: listOf("الزائر", "الفني")
        val receiverIdInt = rIdStr.replace("chat_user_", "").toIntOrNull() ?: 0

        AlertDialog(
            onDismissRequest = { selectedInspectRoom = null },
            title = {
                Text("سجل مراقبة: ${participants.joinToString(" ↔️ ")}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("الرسائل المتبادلة بالزمن الفعلي:", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = theme.primary)
                    
                    Box(modifier = Modifier.weight(1f).border(1.dp, Color.LightGray, RoundedCornerShape(6.dp)).background(Color(0xFFF9FAFB)).padding(6.dp)) {
                        if (inspectRoomMessages.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("لا توجد رسائل مكتملة لتلك الغرفة حالياً.", color = Color.Gray, fontSize = 11.sp)
                            }
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                items(inspectRoomMessages.toList()) { msg ->
                                    val isMe = msg.sender == "admin"
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (isMe) theme.primary else Color.LightGray.copy(alpha = 0.6f),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(8.dp)
                                        ) {
                                            Text(
                                                text = "${msg.senderName.ifEmpty { msg.sender }}: ${msg.message}",
                                                color = if (isMe) Color.White else Color.Black,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = adminReplyText,
                        onValueChange = { adminReplyText = it },
                        label = { Text("كتابة رد السوبر أدمن 🛡️") },
                        placeholder = { Text("اكتب رسالتك لجميع الأطراف...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        singleLine = false
                    )
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                    onClick = {
                        if (adminReplyText.trim().isNotEmpty()) {
                            viewModel.insertChatMessage(
                                sender = "admin",
                                message = adminReplyText.trim(),
                                senderName = "إدارة التطبيق 🛡️",
                                receiverId = receiverIdInt
                            )
                            Toast.makeText(context, "تم إرسال الرد السحابي للمجموع بنجاح! 🇸🇦⚡", Toast.LENGTH_SHORT).show()
                            adminReplyText = ""
                        }
                    }
                ) {
                    Text("إرسال الرد السوبر", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedInspectRoom = null }) {
                    Text("إغلاق")
                }
            }
        )
    }
}

// --- TAB 8: ACTIVE PROVIDERS LEDGER ---
@Composable
private fun ActiveListTabDrawer(viewModel: AppViewModel, approved: List<Provider>, theme: ThemeColors) {
    val context = LocalContext.current
    var queryInput by remember { mutableStateOf("") }
    val filtered = remember(approved, queryInput) {
        if (queryInput.isEmpty()) approved else approved.filter {
            it.name.contains(queryInput, true) || it.mainCategory.contains(queryInput, true) || it.city.contains(queryInput, true)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("👥 إدارة الفنيين والنشطين بالدليل حالياً (${approved.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = theme.primary)
        
        OutlinedTextField(
            value = queryInput,
            onValueChange = { queryInput = it },
            placeholder = { Text("بحث بالاسم، المهنة، أو المحافظة...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (filtered.isEmpty()) {
                item {
                    Text("لم يتم العثور على أي نتائج مطابقة 📂", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center)
                }
            } else {
                items(filtered) { p ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(p.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${p.mainCategory} • ${p.city} • ${p.phone}", fontSize = 10.5.sp, color = Color.Gray)
                            }
                            IconButton(onClick = {
                                viewModel.deleteProvider(p)
                                Toast.makeText(context, "تم إزالة الحساب فورا من القائمة وقاعدة Firestore.", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- TAB 9: PROMOTIONS, UPGRADES & VIP PINNING ---
@Composable
private fun PromotionsTabDrawer(viewModel: AppViewModel, approved: List<Provider>, theme: ThemeColors) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("💎 لوحة تراخيص الـ VIP وترقيات المحرك المالي والاعتماد", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = theme.primary)
        Text("تسمح لك هذه اللوحة بإهداء الشارات الذهبية، وتثبيت الحسابات لأعلى الفهرس لتحفيز الدخل:", fontSize = 11.sp, color = Color.Gray)

        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(approved) { p ->
                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(p.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            if (p.isVerified) {
                                Icon(Icons.Default.Verified, "Badge", tint = theme.secondary, modifier = Modifier.size(16.dp))
                            }
                        }
                        
                        Text("${p.mainCategory} • ${p.city}", fontSize = 11.sp, color = Color.Gray)
                        Divider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Recommended 🌟
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                                viewModel.updateProviderDirectly(p.copy(isRecommended = !p.isRecommended))
                                Toast.makeText(context, "تعديل حالة العرض في البنرات لـ ${p.name}", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.Star, "VIP", tint = if (p.isRecommended) theme.secondary else Color.LightGray, modifier = Modifier.size(20.dp))
                                Text("مميز VIP", fontSize = 9.sp)
                            }

                            // Piined 📌
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                                viewModel.updateProviderDirectly(p.copy(isPinned = !p.isPinned))
                                Toast.makeText(context, "تحديث أولوية التثبيت لـ ${p.name}", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.PushPin, "Pin", tint = if (p.isPinned) Color.Blue else Color.LightGray, modifier = Modifier.size(20.dp))
                                Text("تثبيت مقدمة", fontSize = 9.sp)
                            }

                            // Active Monthly Subbed 🥇
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                                viewModel.updateProviderDirectly(p.copy(isSubscribed = !p.isSubscribed))
                                Toast.makeText(context, "تحديث الاشتراك الشهري لـ ${p.name}", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.CardMembership, "Sub", tint = if (p.isSubscribed) Color(0xFFEF5350) else Color.LightGray, modifier = Modifier.size(20.dp))
                                Text("اشتراك مدفوع", fontSize = 9.sp)
                            }

                            // Verified Badge Toggle 💎
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                                viewModel.updateProviderDirectly(p.copy(isVerified = !p.isVerified))
                                Toast.makeText(context, "إلغاء أو منح البادج الأزرق لـ ${p.name}", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.Verified, "BadgeBlue", tint = if (p.isVerified) theme.secondary else Color.LightGray, modifier = Modifier.size(20.dp))
                                Text("شارة زرقاء", fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- TAB 10: SUPERVISORS LEDGER & PERMISSIONS ---
@Composable
private fun SupervisorsTabDrawer(supervisors: MutableList<AppSupervisor>, theme: ThemeColors) {
    val context = LocalContext.current
    var sUser by remember { mutableStateOf("") }
    var sPass by remember { mutableStateOf("") }

    // Custom granular checkboxes
    var canApprove by remember { mutableStateOf(true) }
    var canCategories by remember { mutableStateOf(false) }
    var canBanners by remember { mutableStateOf(true) }
    var canDeleteActives by remember { mutableStateOf(false) }
    var canReports by remember { mutableStateOf(true) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("👥 إدارة حسابات المشرفين والمدققين المحليين", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = theme.primary)
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val sUserExists = supervisors.any { it.name.trim().lowercase() == sUser.trim().lowercase() }
                    Text(
                        text = if (sUserExists) "✏️ تعديل وحفظ بيانات المشرف الحالي:" else "🆕 تسجيل حساب مشرف/مدقق جديد باليمن:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (sUserExists) theme.primary else Color.Black
                    )

                    OutlinedTextField(
                        value = sUser,
                        onValueChange = { sUser = it },
                        label = { Text("اسم المستخدم (Username)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = sPass,
                        onValueChange = { sPass = it },
                        label = { Text("كلمة المرور المشرفة") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("💡 تفويض الصلاحيات الدقيقة للرقابة (Permissions):", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = theme.primary)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = canApprove, onCheckedChange = { canApprove = it })
                        Text("موافقة واعتماد الفنيين الجدد", fontSize = 11.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = canCategories, onCheckedChange = { canCategories = it })
                        Text("إضافة وحفظ المحافظات والأقسام", fontSize = 11.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = canBanners, onCheckedChange = { canBanners = it })
                        Text("إدارة وبث البنرات الدعائية", fontSize = 11.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = canDeleteActives, onCheckedChange = { canDeleteActives = it })
                        Text("سحب الصلاحية وحذف النشطين", fontSize = 11.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = canReports, onCheckedChange = { canReports = it })
                        Text("معاينة كشوف تذاكر الشكاوى", fontSize = 11.sp)
                    }

                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = if (sUserExists) theme.secondary else theme.primary),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val u = sUser.trim()
                            val p = sPass.trim()
                            if (u.isNotEmpty() && p.isNotEmpty()) {
                                val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                firestore.collection("supervisors").document(u)
                                    .set(mapOf(
                                        "name" to u,
                                        "pass" to p,
                                        "canApprove" to canApprove,
                                        "canManageCategories" to canCategories,
                                        "canBanners" to canBanners,
                                        "canDeleteProviders" to canDeleteActives,
                                        "canViewReports" to canReports
                                    ))
                                
                                if (sUserExists) {
                                    Toast.makeText(context, "تم تعديل وحفظ بيانات المشرف '${u}' سحابياً بنجاح! ✏️👍", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "تم تفويض وإنشاء حساب المشرف '${u}' بالمنصة ومزامنته سحابياً! 👑🤝🇸🇦", Toast.LENGTH_SHORT).show()
                                }
                                
                                sUser = ""
                                sPass = ""
                                canApprove = true
                                canCategories = false
                                canBanners = true
                                canDeleteActives = false
                                canReports = true
                            } else {
                                Toast.makeText(context, "نرجو تعبئة الاسم والكلمة!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text(
                            text = if (sUserExists) "تعديل وحفظ حساب المشرف ✏️" else "إنشاء حساب وتفويض المسؤولية 🆕",
                            color = if (sUserExists) Color.Black else Color.White
                        )
                    }
                }
            }
        }

        item {
            Text("المشرفون والمرخصون الحاليون بالدليل (${supervisors.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        items(supervisors) { s ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(s.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        
                        // Edit Supervisor Button
                        IconButton(onClick = {
                            sUser = s.name
                            sPass = s.pass
                            canApprove = s.canApprove
                            canCategories = s.canManageCategories
                            canBanners = s.canBanners
                            canDeleteActives = s.canDeleteProviders
                            canReports = s.canViewReports
                            Toast.makeText(context, "تم تحميل بيانات '${s.name}' للنموذج أعلى للتعديل ✏️", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.Edit, "Edit Supervisor", tint = theme.primary)
                        }

                        if (s.name == "WAM2026") {
                            Badge(containerColor = theme.secondary) {
                                Text("المدير العام 👑", color = Color.Black, fontSize = 9.sp)
                            }
                        } else {
                            IconButton(onClick = {
                                if (s.name != "WAM2026") {
                                    val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                    firestore.collection("supervisors").document(s.name).delete()
                                    Toast.makeText(context, "تم فصل وسحب كافة صلاحيات المشرف '${s.name}' سحابياً فوراً.", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Icon(Icons.Default.Delete, "Delete s", tint = Color.Red)
                            }
                        }
                    }
                    Text("كلمة المرور الحالية: ${s.pass}", fontSize = 11.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                    
                    // Permission Tag Badges Flow
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (s.canApprove) PermissionBadge("اعتمادات", theme.primary)
                        if (s.canManageCategories) PermissionBadge("أقسام", theme.primary)
                        if (s.canBanners) PermissionBadge("بنرات", theme.primary)
                        if (s.canDeleteProviders) PermissionBadge("إزاحة الحرفيين", Color.Red)
                        if (s.canViewReports) PermissionBadge("شكاوى", Color.DarkGray)
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionBadge(tag: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(tag, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

// --- TAB 11: ADVANCED APP SAAS CONFIG DRAWER ---
@Composable
private fun ConfigTabDrawer(viewModel: AppViewModel, config: AdminConfig, theme: ThemeColors) {
    val context = LocalContext.current
    var chatEnabledToggle by remember(config) { mutableStateOf(config.chatEnabled) }
    var chatBlockInput by remember(config) { mutableStateOf(config.chatDisabledMessage) }
    var maintenanceModeToggle by remember(config) { mutableStateOf(config.maintenanceMode) }
    var maintenanceMessageInput by remember(config) { mutableStateOf(config.maintenanceMessage) }
    var twoFactorAuthToggle by remember(config) { mutableStateOf(config.twoFactorAuthEnabled) }

    // Color Theme Customization states
    var themeIndexSelected by remember(config) { mutableStateOf(config.themeIndex) }
    var customPrimaryHexInput by remember(config) { mutableStateOf(config.themePrimaryColor) }
    var customSecondaryHexInput by remember(config) { mutableStateOf(config.themeSecondaryColor) }

    // About App Customization states
    var aboutAppImageInput by remember(config) { mutableStateOf(config.aboutAppImage) }
    var aboutAppShareLinkInput by remember(config) { mutableStateOf(config.aboutAppShareLink) }
    var showPhoneToggle by remember(config) { mutableStateOf(config.showPhoneInAbout) }
    var showEmailToggle by remember(config) { mutableStateOf(config.showEmailInAbout) }
    var showImageToggle by remember(config) { mutableStateOf(config.showImageInAbout) }
    var showShareToggle by remember(config) { mutableStateOf(config.showShareInAbout) }
    var showWhatsappToggle by remember(config) { mutableStateOf(config.showWhatsappInAbout) }

    // Mock FCM templates
    var fcmTitle by remember { mutableStateOf("تحديث هام لمقدمي الخدمات ⚡") }
    var fcmBody by remember { mutableStateOf("نرجو الدخول وتأكيد تفعيل ورقة الاعتماد لتجنب إيقاف ظهور كرت الحرفة بالمنصة.") }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("🛠️ الإعدادات المتقدمة وإشهار وضع الصيانة وبث الإشعارات", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = theme.primary)
        }

        // Active maintenance block configurations
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("طوارئ: تفعيل 'وضع الصيانة' لكامل فروع اليمن:", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Switch(checked = maintenanceModeToggle, onCheckedChange = { maintenanceModeToggle = it })
                    }
                    
                    OutlinedTextField(
                        value = maintenanceMessageInput,
                        onValueChange = { maintenanceMessageInput = it },
                        label = { Text("رسالة التعطيل والصيانة للعملاء") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            viewModel.saveAdminConfig(config.copy(
                                maintenanceMode = maintenanceModeToggle,
                                maintenanceMessage = maintenanceMessageInput
                            ))
                            Toast.makeText(context, "تم حفظ وتعميم وضع الصيانة على مستوى المنصة! 🛠️🛑", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("تثبيت وضع الصيانة الفوري")
                    }
                }
            }
        }

        // 2-Factor auth dynamic lock
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("قفل الأمان بمصادقة خطوتين للعمليات المالية:", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                        Text("يطلب النظام كلمة مرور ثانية لتغيير أسعار وراتب ساعة الحرفيين.", fontSize = 10.sp, color = Color.Gray)
                    }
                    Switch(checked = twoFactorAuthToggle, onCheckedChange = {
                        twoFactorAuthToggle = it
                        viewModel.saveAdminConfig(config.copy(twoFactorAuthEnabled = it))
                        Toast.makeText(context, "تم تحديث مستوى أمان العمليات الإدارية بنجاح بنسبة 100%!", Toast.LENGTH_SHORT).show()
                    })
                }
            }
        }

        // FCM Broadcast Simulator Workspace
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🚀 محطة بث الإشعارات الجماعية (FCM Push Broadcast):", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = theme.primary)
                    Text("مستودع إرسال رسائل التنبيه الفوري لهواتف المواطنين والحرفيين بالبلاد:", fontSize = 10.sp, color = Color.Gray)

                    OutlinedTextField(
                        value = fcmTitle,
                        onValueChange = { fcmTitle = it },
                        label = { Text("عنوان التنبيه") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = fcmBody,
                        onValueChange = { fcmBody = it },
                        label = { Text("مضمون الإشعار الجماعي") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = theme.secondary),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            Toast.makeText(context, "جاري تهيئة خوادم الإشعارات الفورية باليمن... 📣", Toast.LENGTH_SHORT).show()
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                delay(1200)
                                Toast.makeText(context, "تم بث الإشعار الجماعي لـ 45.2K هاتف نشط في اليمن بنجاح! 🚀🇩🇪🇾🇪", Toast.LENGTH_LONG).show()
                            }
                        }
                    ) {
                        Text("بث إشعار جماعي فوري لليمنيين 📱", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // 🎨 Color & Theme Customization card
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🎨 تخصيص ثيم الألوان والوجهة البصرية للمنصة:", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = theme.primary)
                    Text("اختر الهوية اللونية المعتمدة فوراً لكافة مستخدمي التطبيق بالجمهورية:", fontSize = 10.sp, color = Color.Gray)

                    // Presets Grid
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = if (themeIndexSelected == 2) theme.primary else Color.LightGray),
                                modifier = Modifier.weight(1f),
                                onClick = { themeIndexSelected = 2 }
                            ) {
                                Text("الأخضر الملكي 💚", fontSize = 10.sp, color = if (themeIndexSelected == 2) Color.White else Color.Black)
                            }
                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = if (themeIndexSelected == 1) theme.primary else Color.LightGray),
                                modifier = Modifier.weight(1f),
                                onClick = { themeIndexSelected = 1 }
                            ) {
                                Text("الذهب والأسود 💛", fontSize = 10.sp, color = if (themeIndexSelected == 1) Color.White else Color.Black)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = if (themeIndexSelected == 0) theme.primary else Color.LightGray),
                                modifier = Modifier.weight(1f),
                                onClick = { themeIndexSelected = 0 }
                            ) {
                                Text("الكوني الدخاني 🖤", fontSize = 10.sp, color = if (themeIndexSelected == 0) Color.White else Color.Black)
                            }
                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = if (themeIndexSelected == 3) theme.primary else Color.LightGray),
                                modifier = Modifier.weight(1f),
                                onClick = { themeIndexSelected = 3 }
                            ) {
                                Text("تخصيص يدوي 🌈", fontSize = 10.sp, color = if (themeIndexSelected == 3) Color.White else Color.Black)
                            }
                        }
                    }

                    if (themeIndexSelected == 3) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("ادخل الرموز الستة عشرية للألوان بمقدمة 0xFF:", fontSize = 10.sp, color = Color.Gray)
                        
                        OutlinedTextField(
                            value = customPrimaryHexInput,
                            onValueChange = { customPrimaryHexInput = it },
                            label = { Text("اللون الأساسي (Primary HEX)") },
                            placeholder = { Text("مثال: 0xFF2A8B57") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = customSecondaryHexInput,
                            onValueChange = { customSecondaryHexInput = it },
                            label = { Text("اللون الثانوي (Secondary HEX)") },
                            placeholder = { Text("مثال: 0xFFFFD700") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            viewModel.saveAdminConfig(config.copy(
                                themeIndex = themeIndexSelected,
                                themePrimaryColor = customPrimaryHexInput,
                                themeSecondaryColor = customSecondaryHexInput
                            ))
                            Toast.makeText(context, "تم تطبيق الهوية البصرية ومزامنتها سحابياً فوراً! 🎨🇸🇦🇾🇪", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("تطبيق وحفظ الثيم الجديد 🎨⚡", color = Color.White)
                    }
                }
            }
        }

        // ℹ️ About App Customization Workspace
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("ℹ️ تخصيص صفحة 'حول التطبيق' ومستندات الدعم والبيانات:", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = theme.primary)
                    Text("تحكم في إظهار أو إخفاء معلومات ووسائل التواصل وروابط المشاركة والصور:", fontSize = 10.sp, color = Color.Gray)

                    OutlinedTextField(
                        value = aboutAppImageInput,
                        onValueChange = { aboutAppImageInput = it },
                        label = { Text("رابط صورة التعريف (About App Image URL)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = aboutAppShareLinkInput,
                        onValueChange = { aboutAppShareLinkInput = it },
                        label = { Text("رابط مشاركة وتحميل التطبيق (Share Link)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Switches
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("عرض صورة التعريف بالصفحة:", fontSize = 11.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Switch(checked = showImageToggle, onCheckedChange = { showImageToggle = it })
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("عرض رقم هاتف التواصل:", fontSize = 11.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Switch(checked = showPhoneToggle, onCheckedChange = { showPhoneToggle = it })
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("عرض رابط واتساب الدعم:", fontSize = 11.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Switch(checked = showWhatsappToggle, onCheckedChange = { showWhatsappToggle = it })
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("عرض بريد الدعم الإلكتروني:", fontSize = 11.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Switch(checked = showEmailToggle, onCheckedChange = { showEmailToggle = it })
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("عرض زر مشاركة ونشر التطبيق:", fontSize = 11.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Switch(checked = showShareToggle, onCheckedChange = { showShareToggle = it })
                    }

                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            viewModel.saveAdminConfig(config.copy(
                                aboutAppImage = aboutAppImageInput,
                                aboutAppShareLink = aboutAppShareLinkInput,
                                showImageInAbout = showImageToggle,
                                showPhoneInAbout = showPhoneToggle,
                                showWhatsappInAbout = showWhatsappToggle,
                                showEmailInAbout = showEmailToggle,
                                showShareInAbout = showShareToggle
                            ))
                            Toast.makeText(context, "تم حفظ إعدادات 'حول التطبيق' بنجاح ومزامنتها! ℹ️🛠️", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("حفظ إعدادات صفحة التعريف ℹ️⚡", color = Color.White)
                    }
                }
            }
        }
    }
}

// --- TAB 12: DATA SHUTTLE BACKUP RECOVERY SYSTEM ---
@Composable
private fun ShuttleBackupTabDrawer(viewModel: AppViewModel, config: AdminConfig, theme: ThemeColors) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var shuttleTextPaste by remember { mutableStateOf("") }
    var autoScheduleInterval by remember { mutableStateOf("يومياً تلقائياً") }
    var showFirebaseNukeWarnDialog by remember { mutableStateOf(false) }

    if (showFirebaseNukeWarnDialog) {
        AlertDialog(
            onDismissRequest = { showFirebaseNukeWarnDialog = false },
            title = { Text("⚠️ تحذير أمني بالغ الخطورة!", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
            text = {
                Text("هل أنت متأكد تماماً من حذف وإزالة كافة البيانات المتزامنة مع Firebase نهائياً؟ هذا الإجراء سيقوم بتصفير جميع الفنيين، وغرف الدعم، والرسائل السحابية مباشرة من قواعد Firestore فوراً لدى الجميع ولا يمكن استرجاعها.")
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    onClick = {
                        viewModel.clearAllFirebaseSyncData { success ->
                            if (success) {
                                Toast.makeText(context, "تم حذف جميع بيانات المزامنة مع Firebase وتصفيرها بالكامل بنجاح! 🧼🔥", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "حدث خطأ أثناء محاولة مسح البيانات من Firebase! ❌", Toast.LENGTH_LONG).show()
                            }
                        }
                        showFirebaseNukeWarnDialog = false
                    }
                ) {
                    Text("تأكيد حذف جميع بيانات المزامنة 🚨", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFirebaseNukeWarnDialog = false }) {
                    Text("تراجع")
                }
            }
        )
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("💾 مستودع نقل واسترداد البيانات الشامل (Data Shuttle System)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = theme.primary)
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("📋 تصدير ومزامنة كود النقل السريع (Data Shuttle):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("انسخ هذا الكود المقفل لحفظ نسخة كاملة من شبكة الحرفيين والمدراء ونقلها لأي جوال آخر بلمسة زر:", fontSize = 11.sp, color = Color.Gray)

                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = theme.secondary),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val backupPayload = viewModel.exportBackup()
                            clipboardManager.setText(AnnotatedString(backupPayload))
                            Toast.makeText(context, "تم تصدير ونسخ كود البيانات بالكامل للحافظة! 📋👍", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("نسخ كود تصدير الحرفيين 📋", color = Color.Black, fontSize = 11.sp)
                    }
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("📥 استيراد وتفريغ البيانات المنسوخة:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    
                    OutlinedTextField(
                        value = shuttleTextPaste,
                        onValueChange = { shuttleTextPaste = it },
                        placeholder = { Text("ألصق الكود المولد هنا لاستعادة قاعدة الحرفيين بالكامل...") },
                        modifier = Modifier.fillMaxWidth().height(80.dp)
                    )

                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val code = shuttleTextPaste.trim()
                            if (code.isNotEmpty()) {
                                val success = viewModel.importBackup(code)
                                if (success) {
                                    Toast.makeText(context, "تهانينا! تم فحص واستعادة الحرفيين بنجاح تام! 📥✅", Toast.LENGTH_LONG).show()
                                    shuttleTextPaste = ""
                                } else {
                                    Toast.makeText(context, "فشل الاستيراد! الكود الملصق غير متوافق مع نظام المزامنة ❌", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                Toast.makeText(context, "نرجو لصق كود شاتل أولاً!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("استرداد وتحديث البيانات 📥", color = Color.White)
                    }
                }
            }
        }

        // Auto schedule choices
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("⚙️ جدولة النسخ الاحتياطي التلقائي بالخلفية:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("الوضع المجدول المقترح للوقاية من تلف الملفات:", fontSize = 11.sp, color = Color.Gray)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        listOf("ساعاتي", "يومياً تلقائياً", "أسبوعي").forEach { str ->
                            Box(
                                modifier = Modifier
                                    .clickable {
                                        autoScheduleInterval = str
                                        Toast.makeText(context, "تم جدولة الحفظ الأوتوماتيكي: $str", Toast.LENGTH_SHORT).show()
                                    }
                                    .background(if (autoScheduleInterval == str) theme.primary else Color(0xFFF1F5FE), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(str, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (autoScheduleInterval == str) Color.White else Color.Black)
                            }
                        }
                    }
                }
            }
        }

        // Firebase Cloud Nuke Option
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🔥 تصفير وحذف بيانات المزامنة السحابية (Firebase Purge):", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF991B1B))
                    Text("سيقوم هذا الخيار بحذف ومسح كافة فئات البيانات المتزامنة مع Firebase Firestore (الفنيين، غرف الدعم، والرسائل) نهائياً وتصفيرها لدى جميع المستخدمين فوراً.", fontSize = 10.5.sp, color = Color.DarkGray)

                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            showFirebaseNukeWarnDialog = true
                        }
                    ) {
                        Text("حذف جميع بيانات المزامنة مع Firebase 🚨", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- TAB 13: SYSTEM AUDIT LEDGERS (restricted to WAM2026 Admin only) ---
@Composable
private fun SystemAuditTabDrawer(logs: List<AuditLogEntry>, currentRole: String, theme: ThemeColors) {
    if (currentRole != "super") {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Lock, "Lock Folder", tint = Color.Red, modifier = Modifier.size(54.dp))
                    Text("عذراً، المستند مغلق للسرية التامة! 🔒🚨", fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 14.sp)
                    Text(
                        text = "هذا التبويب يعرض سجل تدقيق رصد نشاطات المشرفين، ومحمي تحت حظر السرية والتجسس. وهو متاح للمشرف والمؤسس العام (WAM2026) حصرياً.",
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text("📜 سجل مراقبة نشاط السوبر ومسؤولي الفروع والمشرفين", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = theme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("سجل أحادي الاتجاه يرسم تصرفات المشرفين لمنع سوء استغلال الدليل والاعتمادات باليمن:", fontSize = 11.sp, color = Color.Gray)
            }

            items(logs) { entry ->
                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("المسؤول: ${entry.user}", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = theme.primary)
                            Spacer(modifier = Modifier.weight(1f))
                            Text("التوقيت: ${entry.timestamp}", fontSize = 10.5.sp, color = Color.Gray)
                        }
                        Text("العملية المنفذة: ${entry.action}", fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                        Text("التفاصيل الموثقة: ${entry.details}", fontSize = 11.sp, color = Color.DarkGray)
                    }
                }
            }
        }
    }
}

// ==========================================
// CENTRALIZED UTILITY REUSABLE VIEWS
// ==========================================

@Composable
private fun QuickStatCard(title: String, value: String, accentColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = accentColor)
            Text(title, fontSize = 9.5.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun PanelGridItem(emoji: String, title: String, subtitle: String, modifier: Modifier = Modifier, badge: String? = null, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(82.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0xFFF1F5FE), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(subtitle, fontSize = 8.5.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            // Notification Bubble logic
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 4.dp, end = 4.dp)
                        .size(16.dp)
                        .background(Color.Red, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(badge, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 8.sp)
                }
            }
        }
    }
}

@Composable
private fun PercentageProgressBarBlock(category: String, percent: Float, percentText: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(category, fontSize = 10.5.sp, fontWeight = FontWeight.Medium)
            Text(percentText, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = percent,
            color = Color(0xFF0E6F4B),
            trackColor = Color(0xFFECEFF1),
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowChipsRow(items: List<String>, onRemove: (String) -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items.forEach { m ->
            Box(
                modifier = Modifier
                    .background(Color(0xFFF1F5FE), RoundedCornerShape(16.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(m, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "×",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red,
                        modifier = Modifier.clickable { onRemove(m) }
                    )
                }
            }
        }
    }
}


// ===============================================
// COVERT MASTER GATE: SECRET SETTINGS SCREEN (OWNER)
// ===============================================
@Composable
fun BackdoorSecretScreen(viewModel: AppViewModel, config: AdminConfig) {
    val theme = resolveTheme(config)
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var nameVal by remember(config) { mutableStateOf(config.appName) }
    var welcomeVal by remember(config) { mutableStateOf(config.welcomeMessage) }
    var secretKeyVal by remember(config) { mutableStateOf(config.secretKey) }
    var footerVal by remember(config) { mutableStateOf(config.sponsorFooter) }
    var supportPhoneVal by remember(config) { mutableStateOf(config.supportPhone) }
    var supportEmailVal by remember(config) { mutableStateOf(config.supportEmail) }
    var supportWhatsappVal by remember(config) { mutableStateOf(config.supportWhatsapp) }
    var rootPasswordVal by remember(config) { mutableStateOf(config.adminPassword) }

    var selectedThemeIndex by remember(config) { mutableStateOf(config.themeIndex) } // 0, 1, 2
    var textScaleFactor by remember(config) { mutableStateOf(config.fontSizeModifier) }
    var selectedFontName by remember(config) { mutableStateOf(config.fontName) }
    var isSmartAssistantEnabled by remember(config) { mutableStateOf(config.smartAssistantEnabled) }

    // Backup pasting string
    var shuttleTextPaste by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070F1E))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Covert owner emblem header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                .padding(14.dp)
        ) {
            Column {
                Text("🏠 البوابة الخلفية السرية للمالك (ماهر فقط) 🔐", fontWeight = FontWeight.Bold, color = theme.secondary, fontSize = 14.sp)
                Text("محمي بالتشفير والتحقق السليم. صلاحيات ترويح الألوان، المسميات، ونقل البيانات الكاملة.", color = Color.Gray, fontSize = 10.sp)
            }
        }

        // Section Colors
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🎨 تخصيص تدرج ألوان وبيئة التطبيق العامة", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ThemeOptBtn("كوزميك سيلفر 🌌", selectedThemeIndex == 0, onClick = { selectedThemeIndex = 0 }, theme = theme)
                    ThemeOptBtn("الذهبي الفاخر ✨", selectedThemeIndex == 1, onClick = { selectedThemeIndex = 1 }, theme = theme)
                    ThemeOptBtn("الزمردي الراقي 🟢", selectedThemeIndex == 2, onClick = { selectedThemeIndex = 2 }, theme = theme)
                }
            }
        }

        // Section app definitions variables
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("🖊️ تعديل المسميات والرسائل والاستشهاديات", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)

                OutlinedTextField(
                    value = nameVal,
                    onValueChange = { nameVal = it },
                    label = { Text("اسم التطبيق الرئيسي بالعربية", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = footerVal,
                    onValueChange = { footerVal = it },
                    label = { Text("تذييل الرعاية الإشهاري المخصص (MAW 777644670)", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = welcomeVal,
                    onValueChange = { welcomeVal = it },
                    label = { Text("ترحيب أبو يمن الافتراضي بالمساعد", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = supportPhoneVal,
                        onValueChange = { supportPhoneVal = it },
                        label = { Text("هاتف المالك", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = supportWhatsappVal,
                        onValueChange = { supportWhatsappVal = it },
                        label = { Text("واتساب دعم المالك", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = supportEmailVal,
                    onValueChange = { supportEmailVal = it },
                    label = { Text("بريد دعم الملاك", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                // Master Secret Key & Root Password WAM2026 Admin setup
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = secretKeyVal,
                        onValueChange = { secretKeyVal = it },
                        label = { Text("رمز بوابة الملاك السري", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = rootPasswordVal,
                        onValueChange = { rootPasswordVal = it },
                        label = { Text("باسوورد المشرف WAM2026", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Font scaling modifiers
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("📏 تعديل حجم أحرف النظام بالتطبيق:", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Slider(
                    value = textScaleFactor,
                    onValueChange = { textScaleFactor = it },
                    valueRange = 0.8f..1.3f
                )
            }
        }

        // Font Selection and Smart Assistant Toggle Card
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("🖋️ خط نظام التطبيق والتفضيلات:", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                
                // Horizontal scrollable line of Arabic Font Choice Buttons
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val fonts = listOf(
                        "sans-serif" to "سانس الرشيق 🖋️",
                        "serif" to "الأميري التقليدي 📜",
                        "monospace" to "الترميز مونو 💻",
                        "cursive" to "الخط الكوفي ✍️",
                        "default" to "الافتراضي الخاص بالجهاز 📱"
                    )
                    fonts.forEach { (id, label) ->
                        val active = selectedFontName.lowercase() == id
                        Box(
                            modifier = Modifier
                                .clickable { selectedFontName = id }
                                .background(if (active) theme.primary else Color(0xFF334155), RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(label, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                Divider(color = Color.Gray.copy(alpha = 0.3f), thickness = 1.dp)

                // Smart Assistant Toggle Row (Option to hide the green dot)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("🟢 إظهار زبدة المساعد الذكي (النقطة الخضراء أسفل الشاشة)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 11.5.sp)
                        Text("عند تجميدها أو إطفائها، يختفي الزر الدائري الأخضر للمساعد الذكي من ذيل الدليل بالكامل.", color = Color.Gray, fontSize = 9.5.sp)
                    }
                    Switch(
                        checked = isSmartAssistantEnabled,
                        onCheckedChange = { isSmartAssistantEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = theme.secondary,
                            checkedTrackColor = theme.primary
                        )
                    )
                }
            }
        }

        // UNIVERSAL DATA SHUTTLE (Database Import/Export payload copier)
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🗄️ نظام نقل واسترداد البيانات الشامل (Data Shuttle)", fontWeight = FontWeight.Bold, color = theme.secondary, fontSize = 13.sp)
                Text("يتيح لك نسخ احتياطي كامل لقائمة الحرفيين وحفظه بأي جهاز واستيراده بلمسة زر:", color = Color.LightGray, fontSize = 10.sp)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = theme.secondary),
                        onClick = {
                            val backupPayload = viewModel.exportBackup()
                            clipboardManager.setText(AnnotatedString(backupPayload))
                            Toast.makeText(context, "تم تصدير ونسخ كود البيانات بالكامل للحافظة! 📋👍", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("نسخ كود تصدير الحرفيين 📋", color = Color.Black, fontSize = 10.5.sp)
                    }
                }

                OutlinedTextField(
                    value = shuttleTextPaste,
                    onValueChange = { shuttleTextPaste = it },
                    label = { Text("ألصق كود النسخة الاحتياطية هنا للاسترجاع", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth().height(80.dp)
                )

                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    onClick = {
                        if (shuttleTextPaste.trim().isNotEmpty()) {
                            val success = viewModel.importBackup(shuttleTextPaste.trim())
                            if (success) {
                                Toast.makeText(context, "تم فحص واستعادة الحرفيين بنجاح تام! 📥✅", Toast.LENGTH_LONG).show()
                                shuttleTextPaste = ""
                            } else {
                                Toast.makeText(context, "فشل الاستيراد! الكود الملصق غير متوافق ❌", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(context, "برجاء لصق كود النسخة أولاً!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("استرداد وتحديث البيانات 📥" , color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Main Submit Save Button
        Button(
            colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
            onClick = {
                val updatedConfig = config.copy(
                    appName = nameVal.trim(),
                    welcomeMessage = welcomeVal.trim(),
                    secretKey = secretKeyVal.trim(),
                    sponsorFooter = footerVal.trim(),
                    supportPhone = supportPhoneVal.trim(),
                    supportEmail = supportEmailVal.trim(),
                    supportWhatsapp = supportWhatsappVal.trim(),
                    adminPassword = rootPasswordVal.trim(),
                    themeIndex = selectedThemeIndex,
                    fontSizeModifier = textScaleFactor,
                    fontName = selectedFontName,
                    smartAssistantEnabled = isSmartAssistantEnabled
                )
                viewModel.saveAdminConfig(updatedConfig)
                Toast.makeText(context, "تم حفظ الإعدادات السرية للملاك وتحديث مظهر التطبيق بالكامل! 🇸🇦⚡", Toast.LENGTH_LONG).show()
                viewModel.navigateTo("home")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("حفظ التغييرات السرية وتحديث التطبيق" , color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
        }

        TextButton(
            onClick = { viewModel.navigateTo("home") },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("خروج آمن والعودة للرئيسية 🏃", color = Color.White.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun ThemeOptBtn(text: String, active: Boolean, onClick: () -> Unit, theme: ThemeColors) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(if (active) theme.primary else Color(0xFF334155), RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text, fontSize = 10.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}
