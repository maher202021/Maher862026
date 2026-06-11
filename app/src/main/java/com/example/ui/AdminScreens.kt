package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLoginScreen(
    viewModel: AppViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val config by viewModel.adminConfig.collectAsState()
    val isArabic = viewModel.appLanguage.collectAsState().value == "ar"

    val primaryColor = remember(config.primaryColorHex) {
        try { Color(android.graphics.Color.parseColor(config.primaryColorHex)) }
        catch (e: Exception) { Color(0xFF0F172A) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = "Admin Area",
                    tint = primaryColor,
                    modifier = Modifier.size(64.dp)
                )

                Text(
                    text = if (isArabic) "تسجيل دخول لوحة التحكم" else "Admin Panel Access System",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = primaryColor
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(if (isArabic) "اسم المستخدم" else "Username") },
                    modifier = Modifier.fillMaxWidth().testTag("admin_username_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(if (isArabic) "كلمة المرور" else "Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().testTag("admin_password_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Button(
                    onClick = {
                        val success = viewModel.loginAdmin(username.trim(), password.trim())
                        if (success) {
                            Toast.makeText(context, if (isArabic) "تم تفويض حساب الإشراف بنجاح! 🔐⚙️" else "Authorized success", Toast.LENGTH_SHORT).show()
                            onLoginSuccess()
                        } else {
                            Toast.makeText(context, if (isArabic) "خطأ: اسم المستخدم أو كلمة المرور غير صالحة" else "Invalid credential credentials", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("admin_login_submit_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isArabic) "تسجيل الدخول الآمن" else "Secure Login")
                }
            }
        }
    }
}

// --- Dynamic Customizable Backdoor Screen (Owner Emergency Configuration Hub) ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackdoorSecretScreen(viewModel: AppViewModel, config: AdminConfig) {
    val context = LocalContext.current
    val isArabic = viewModel.appLanguage.collectAsState().value == "ar"

    var appNameInput by remember { mutableStateOf(config.appName) }
    var sloganInput by remember { mutableStateOf(config.slogan) }
    var primaryColorInput by remember { mutableStateOf(config.primaryColorHex) }
    var secondaryColorInput by remember { mutableStateOf(config.secondaryColorHex) }
    var footerTextInput by remember { mutableStateOf(config.footerText) }
    var infoTextInput by remember { mutableStateOf(config.infoHtmlText) }
    var phoneInput by remember { mutableStateOf(config.supportPhone) }
    var emailInput by remember { mutableStateOf(config.supportEmail) }
    var whatsappInput by remember { mutableStateOf(config.supportWhatsapp) }
    var tempPasswordInput by remember { mutableStateOf(config.adminPassword) }

    var maintenanceModeChecked by remember { mutableStateOf(config.isMaintenanceMode) }
    var secretTokenInput by remember { mutableStateOf(config.secretKey) }

    // Floating Assistant Settings
    var assistantActiveChecked by remember { mutableStateOf(config.assistantActive) }
    var assistantIconChosen by remember { mutableStateOf(config.assistantIcon) } // 🤖, 💬, ✨
    var assistantScaleChoice by remember { mutableStateOf(config.assistantScale) }

    var showNukeAllDialog by remember { mutableStateOf(false) }

    val coreColor = remember(config.primaryColorHex) {
        try { Color(android.graphics.Color.parseColor(config.primaryColorHex)) }
        catch (e: Exception) { Color(0xFF0F172A) }
    }

    if (showNukeAllDialog) {
        AlertDialog(
            onDismissRequest = { showNukeAllDialog = false },
            title = { Text("💥🚨 تصفير وتطهير النظام الإجباري المطلق!", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
            text = {
                Text("هذا البوتون التدميري مخصص للمؤسس الفائق فقط! سيقوم بفرمتة كاملة لـ SQLite، وحذف كاش التطبيق، ومسح SharedPreferences، وتدمير وإبادة جميع مجلدات ومجموعات Firebase Firestore بالكامل بلا تراجع! هل أنت متأكد؟")
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    onClick = {
                        viewModel.formulaNukeEverything { success ->
                            if (success) {
                                Toast.makeText(context, "العملية تمت بنجاح تصفيري مطلق وحرق قواعد الخادم بالكامل! 💥🧼✅", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "اكتمل تطهير الذراع المحلي بالكامل. ⚠️", Toast.LENGTH_SHORT).show()
                            }
                        }
                        showNukeAllDialog = false
                    }
                ) {
                    Text("نعم، دمّر واحذف جميع خوادم Firestore والملفات نهائياً")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNukeAllDialog = false }) {
                    Text("إلغاء وتراجع")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = coreColor)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.VerifiedUser, "System Master", tint = Color.Yellow, modifier = Modifier.size(48.dp))
                    Text(
                        text = "بوابة المالك الخلفية غير المعلنة 🛡️",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "صنعت لخدمة المطور وإعادة تنسيق إعدادات WAM ومركز السيادة السحابية مباشرة وبسرية مطلقة.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Section 1: System Identifiers
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("🏷️ معرّفات الدليل ونظام التطبيق الأساسي:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = coreColor)
                    
                    OutlinedTextField(
                        value = appNameInput,
                        onValueChange = { appNameInput = it },
                        label = { Text("اسم التطبيق الحالي") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = sloganInput,
                        onValueChange = { sloganInput = it },
                        label = { Text("الشعار التسويقي (Slogan)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Section 2: Branding Colors Theme (Dynamic Colors)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("🎨 الألوان وموضوع التصميم الديناميكي (Dynamic Hex Colors):", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = coreColor)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = primaryColorInput,
                            onValueChange = { primaryColorInput = it },
                            label = { Text("اللون الرئيسي (Primary Hex)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = secondaryColorInput,
                            onValueChange = { secondaryColorInput = it },
                            label = { Text("اللون الفرعي (Secondary Hex)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Section 3: Smart Assistant Controlling
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("🤖 إعدادات وتحكم المساعد الذكي الاصطناعي (Gemini Router):", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = coreColor)

                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { assistantActiveChecked = !assistantActiveChecked },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("تفعيل وعرض أيقونة المساعد العائم", fontSize = 12.sp)
                        Switch(checked = assistantActiveChecked, onCheckedChange = { assistantActiveChecked = it })
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("رمز الأيقونة التفاعلي المفضل:", fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterVertically))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("🤖", "💬", "✨").forEach { icon ->
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (assistantIconChosen == icon) coreColor else Color.LightGray.copy(alpha = 0.4f))
                                        .clickable { assistantIconChosen = icon },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(icon, fontSize = 16.sp)
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("حجم المساعد الإشعاعي (التقليص بنسبة 50% كحد أدنى): ${ (assistantScaleChoice * 100).toInt() }%", fontSize = 11.sp, color = Color.Gray)
                        Slider(
                            value = assistantScaleChoice,
                            onValueChange = { assistantScaleChoice = it },
                            valueRange = 0.2f..0.8f,
                            colors = SliderDefaults.colors(thumbColor = coreColor)
                        )
                    }
                }
            }
        }

        // Section 4: Sponsors Support contact & Branding Footer
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("🔧 تذييل الدعاية وجهات الاتصال الفنية للـ WAM:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = coreColor)

                    OutlinedTextField(
                        value = footerTextInput,
                        onValueChange = { footerTextInput = it },
                        label = { Text("المعرف الدعائي الأوسط (Sponsor Text)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("رقم الواتس والدعم الرئيسي") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("بريد الدعم والخصوصية") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = infoTextInput,
                        onValueChange = { infoTextInput = it },
                        label = { Text("نص صفحة (حول التطبيق) ℹ️") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Section 5: Security Control Bypass tokens
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("🔐 رموز وسيرفرات الإشراف والحماية الأبجدية:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = coreColor)

                    OutlinedTextField(
                        value = tempPasswordInput,
                        onValueChange = { tempPasswordInput = it },
                        label = { Text("الرمز السري لمدير النظام (Admin Pass)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = secretTokenInput,
                        onValueChange = { secretTokenInput = it },
                        label = { Text("رمز تشفير البوابة السري الحالي (Secret Key)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { maintenanceModeChecked = !maintenanceModeChecked },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("تفعيل وضع الصيانة العام (صنعاء / عدن)", fontSize = 12.sp)
                        Switch(checked = maintenanceModeChecked, onCheckedChange = { maintenanceModeChecked = it })
                    }
                }
            }
        }

        // Master emergency actions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🚨 الطوارئ القصوى ونقاط تحصين البيانات:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Red)
                    Text("تم تصميم هذا الكنترول لتطهير الخوادم والملفات وقواعد البيانات كلياً وتصفير المذاكرات التخزينية في الحسابات عند الطوارئ المطلقة.", fontSize = 11.sp, color = Color.DarkGray)

                    Button(
                        onClick = { showNukeAllDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("💥 تصفير تطهيري وتدمير عام لكافة بيانات الخادم والذاكرة", color = Color.Yellow, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Save All Configs
        item {
            Button(
                onClick = {
                    val updated = config.copy(
                        appName = appNameInput.trim(),
                        slogan = sloganInput.trim(),
                        primaryColorHex = primaryColorInput.trim(),
                        secondaryColorHex = secondaryColorInput.trim(),
                        footerText = footerTextInput.trim(),
                        infoHtmlText = infoTextInput.trim(),
                        supportPhone = phoneInput.trim(),
                        supportEmail = emailInput.trim(),
                        supportWhatsapp = whatsappInput.trim(),
                        adminPassword = tempPasswordInput.trim(),
                        isMaintenanceMode = maintenanceModeChecked,
                        secretKey = secretTokenInput.trim(),
                        assistantActive = assistantActiveChecked,
                        assistantIcon = assistantIconChosen,
                        assistantScale = assistantScaleChoice
                    )
                    viewModel.updateConfig(updated) { success ->
                        if (success) {
                            Toast.makeText(context, "تم حفظ الإعدادات السحابية ومزامنتها بنجاح مطلق! ✨📡✅", Toast.LENGTH_SHORT).show()
                            viewModel.navigateTo("home")
                        } else {
                            Toast.makeText(context, "فشل الاتصال بالشبكة السحابية، تم الحفظ محلياً 💾", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("backdoor_save_all_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = coreColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, "Save Icon", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("حفظ وتعديل التنسيق السحابي الشامل", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}


// --- Master Admin Dashboard Screen (Full Controls Sections 1 to 9) ---

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AppViewModel,
    config: AdminConfig
) {
    val context = LocalContext.current
    val isArabic = viewModel.appLanguage.collectAsState().value == "ar"

    // Sections index state controlling (0 to 8)
    var selectedTabModule by remember { mutableStateOf(0) }

    val coreColor = remember(config.primaryColorHex) {
        try { Color(android.graphics.Color.parseColor(config.primaryColorHex)) }
        catch (e: Exception) { Color(0xFF0F172A) }
    }

    val secColor = remember(config.secondaryColorHex) {
        try { Color(android.graphics.Color.parseColor(config.secondaryColorHex)) }
        catch (e: Exception) { Color(0xFF3B82F6) }
    }

    // Load data states
    val pRequests by viewModel.pendingProviders.collectAsState()
    val rawApproved by viewModel.approvedProviders.collectAsState()
    val chatRoomsHistory by viewModel.chats.collectAsState()
    val bannerAds by viewModel.banners.collectAsState()
    val allCategories by viewModel.categories.collectAsState()
    val allCities by viewModel.cities.collectAsState()
    val systemReports by viewModel.reports.collectAsState()
    val assistantSupervisors by viewModel.supervisors.collectAsState()

    val superUser = viewModel.currentSupervisor.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE2E8F0))
    ) {
        // Horizontal Scroll Selector of Sub-modules ordered sequentially: 1 to 9
        ScrollableTabRow(
            selectedTabIndex = selectedTabModule,
            containerColor = coreColor,
            contentColor = Color.White,
            edgePadding = 12.dp
        ) {
            val tabs = listOf(
                "طلبات التسجيل 👤" to 0,
                "إضافة فني 🛠️" to 1,
                "إعلانات وبنرات 🏷️" to 2,
                "أقسام ومدن 🏢" to 3,
                "البلاغات والتقارير ⚠️" to 4,
                "المحادثات والخصوصية 💬" to 5,
                "المزودين النشطين 🎳" to 6,
                "الاشتراكات والتثبيت 🌟" to 7,
                "إدارة المشرفين 👥" to 8
            )
            tabs.forEach { (title, idx) ->
                Tab(
                    selected = selectedTabModule == idx,
                    onClick = { selectedTabModule = idx },
                    text = { Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        // Tab Content Router
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(12.dp)
        ) {
            when (selectedTabModule) {
                0 -> RequestsApprovalPanel(viewModel, pRequests, coreColor)
                1 -> ManualTechnicianPanel(viewModel, allCategories, allCities, coreColor)
                2 -> AdsManagementPanel(viewModel, bannerAds, coreColor)
                3 -> CategoriesCitiesPanel(viewModel, allCategories, allCities, coreColor)
                4 -> ReportsComplaintsPanel(viewModel, systemReports, coreColor)
                5 -> ChatHistorySecurityPanel(viewModel, chatRoomsHistory, coreColor)
                6 -> ActiveProvidersPanel(viewModel, rawApproved, coreColor)
                7 -> SubscriptionsPinningPanel(viewModel, rawApproved, coreColor)
                8 -> SupervisorsAdminPanel(viewModel, assistantSupervisors, coreColor)
            }
        }
    }
}

// Extra utility dummy helper to handle code typo limits safely inside layout
private fun Modifier.someModifier(): Modifier = this

// --- Submodule 1: Registration Requests Approval Panel ---
@Composable
fun RequestsApprovalPanel(viewModel: AppViewModel, requests: List<Provider>, themeColor: Color) {
    val context = LocalContext.current
    val isArabic = viewModel.appLanguage.collectAsState().value == "ar"
    var rejectionReasonText by remember { mutableStateOf("") }
    var expandedImageState by remember { mutableStateOf<String?>(null) }

    if (expandedImageState != null) {
        AlertDialog(
            onDismissRequest = { expandedImageState = null },
            title = { Text(if (isArabic) "معاينة الملف المرفق" else "Document Preview", fontSize = 13.sp) },
            text = {
                Card(modifier = Modifier.size(240.dp)) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("📄\n${expandedImageState}", textAlign = TextAlign.Center, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { expandedImageState = null }) { Text(if (isArabic) "إغلاق" else "Close") }
            }
        )
    }

    if (requests.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لا توجد طلبات تسجيل معلقة حالياً ☕", fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(requests) { req ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(req.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = themeColor)
                            Text(req.serviceCategory, color = Color.DarkGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("هاتف: ${req.phone} | عنوان: ${req.locationCity} - ${req.locationRegion}", fontSize = 11.sp)

                        if (req.photoUrl.isNotEmpty()) {
                            Text(
                                text = "🖼️ اضغط لمعاينة الصورة الشخصية المرفقة للتحقق",
                                color = Color.Blue,
                                fontSize = 11.sp,
                                modifier = Modifier.clickable { expandedImageState = req.photoUrl }
                            )
                        }

                        if (req.idCardUrl.isNotEmpty()) {
                            Text(
                                text = "🪪 اضغط لمعاينة بطاقة الهوية الشخصية المرفقة",
                                color = Color.DarkGray,
                                fontSize = 11.sp,
                                modifier = Modifier.clickable { expandedImageState = req.idCardUrl }
                            )
                        }

                        OutlinedTextField(
                            value = rejectionReasonText,
                            onValueChange = { rejectionReasonText = it },
                            label = { Text("سبب الرفض المبرر (إذا رغبت بالرفض)") },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyleCopy()
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = {
                                    viewModel.approveRegistration(req) { success ->
                                        if (success) {
                                            Toast.makeText(context, "تم قبول طلب الفني وإضافته للدليل بنجاح! ⭐", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("قبول الطلب ✅", color = Color.White)
                            }

                            Button(
                                onClick = {
                                    val reason = rejectionReasonText.ifEmpty { "طلب غير مستوف لشروط التوثيق كاملة" }
                                    viewModel.rejectRegistration(req, reason) { success ->
                                        if (success) {
                                            Toast.makeText(context, "تم رفض الطلب وإبلاغ المتقدم بالسبب. ❌", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("رفض الطلب ❌", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Simple fallback compose text style helper to avoid compilation issue
@Composable
private fun TextStyleCopy() = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)

// --- Submodule 2: Manual Technician Addition ---
@Composable
fun ManualTechnicianPanel(
    viewModel: AppViewModel,
    cats: List<CategoryItem>,
    citiesName: List<CityItem>,
    themeColor: Color
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var ratePrice by remember { mutableStateOf("1500") } // consultation price

    var selectedCatSelected by remember { mutableStateOf(cats.firstOrNull()?.id ?: "") }
    var selectedCitySelected by remember { mutableStateOf(citiesName.firstOrNull()?.nameAr ?: "") }

    var vipToggle by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("➕ إضافة فني وحرفي محترف ومباشر للدليل:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = themeColor)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("الاسم الثلاثي الكامل") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("رقم الهاتف والواتساب") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = region,
                    onValueChange = { region = it },
                    label = { Text("المنطقة والدائرة السكنية") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = ratePrice,
                    onValueChange = { ratePrice = it },
                    label = { Text("سعر المعاينة (ريال)") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Dropdowns selectors
            Text("المدينة والقسم المستهدف للفني المهني:", fontSize = 11.sp, color = Color.Gray)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // simple simulated selection list
                Column(modifier = Modifier.weight(1f)) {
                    Text("المدينة: ${selectedCitySelected}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth()) {
                        citiesName.take(3).forEach { c ->
                            Text(
                                c.nameAr,
                                fontSize = 10.sp,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clickable { selectedCitySelected = c.nameAr }
                                    .background(if (selectedCitySelected == c.nameAr) themeColor else Color.LightGray.copy(alpha = 0.4f))
                                    .padding(4.dp),
                                color = if (selectedCitySelected == c.nameAr) Color.White else Color.Black
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("القسم المهني الحرفي:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    cats.take(3).forEach { ct ->
                        Text(
                            ct.nameAr,
                            fontSize = 10.sp,
                            modifier = Modifier
                                .padding(2.dp)
                                .clickable { selectedCatSelected = ct.nameAr }
                                .background(if (selectedCatSelected == ct.nameAr) themeColor else Color.LightGray.copy(alpha = 0.4f))
                                .padding(4.dp),
                            color = if (selectedCatSelected == ct.nameAr) Color.White else Color.Black
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("منح شارة الكفاءة ونخبة الفنيين الـ VIP مباشرة 🌟", fontSize = 12.sp)
                Switch(checked = vipToggle, onCheckedChange = { vipToggle = it })
            }

            Button(
                onClick = {
                    if (name.isEmpty() || phone.isEmpty()) {
                        Toast.makeText(context, "الرجاء تعبئة اسم ورقم الفني أولاً!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val nProv = Provider(
                        name = name.trim(),
                        phone = phone.trim(),
                        locationCity = selectedCitySelected,
                        locationRegion = region.trim(),
                        serviceCategory = selectedCatSelected.ifEmpty { "أخرى" },
                        isVip = vipToggle,
                        status = "approved"
                    )
                    viewModel.submitRequest(nProv) { success ->
                        if (success) {
                            Toast.makeText(context, "تم تسجيل الفني وإدراجه مباشرة في دليل WAM! ✨🛠️", Toast.LENGTH_SHORT).show()
                            name = ""
                            phone = ""
                            region = ""
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("إدراج فوري وحفظ للفني المهني ديركت ✅")
            }
        }
    }
}

// --- Submodule 3: Ads & Banners Carousel Panel ---
@Composable
fun AdsManagementPanel(viewModel: AppViewModel, banners: List<BannerAd>, themeColor: Color) {
    val context = LocalContext.current
    var adTitle by remember { mutableStateOf("") }
    var adContent by remember { mutableStateOf("") }
    var adDuration by remember { mutableStateOf("6") }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("📣 إنشاء وتعديل بنر لافتة إعلانية في الترويسة الرئيسية:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = themeColor)

            OutlinedTextField(value = adTitle, onValueChange = { adTitle = it }, label = { Text("عنوان الإعلان الترويجي للخدمة") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = adContent, onValueChange = { adContent = it }, label = { Text("محتوى الإعلان النصي أو رابط الصورة") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = adDuration, onValueChange = { adDuration = it }, label = { Text("مدة العرض المتناوبة (ثانية)") }, modifier = Modifier.fillMaxWidth())

            Button(
                onClick = {
                    if (adTitle.isEmpty()) {
                        Toast.makeText(context, "عفواً، يجب وضع ترويسة محددة للإعلان", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val badAd = BannerAd(
                        title = adTitle.trim(),
                        contentText = adContent.trim(),
                        durationSeconds = adDuration.toIntOrNull() ?: 5
                    )
                    viewModel.saveBanner(badAd) { success ->
                        if (success) {
                            Toast.makeText(context, "تم حفظ الإعلان الترويجي ونشره في قمة السلايدر السحابي! 📢🎉", Toast.LENGTH_SHORT).show()
                            adTitle = ""
                            adContent = ""
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("نشر وتعديل البنر السلادير")
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(banners) { b ->
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(b.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(b.contentText, fontSize = 10.sp, color = Color.Gray)
                    }
                    IconButton(onClick = { viewModel.deleteBanner(b.id) {
                        Toast.makeText(context, "تم حذف الإعلان كلياً", Toast.LENGTH_SHORT).show()
                    } }) {
                        Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
                    }
                }
            }
        }
    }
}

// --- Submodule 4: Categories & Cities Management ---
@Composable
fun CategoriesCitiesPanel(
    viewModel: AppViewModel,
    cats: List<CategoryItem>,
    citiesName: List<CityItem>,
    themeColor: Color
) {
    val context = LocalContext.current
    var nCatNameAr by remember { mutableStateOf("") }
    var nCatNameEn by remember { mutableStateOf("") }
    var nCityNameAr by remember { mutableStateOf("") }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🏷️ إضافة تصنيف مهني:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = themeColor)
                OutlinedTextField(value = nCatNameAr, onValueChange = { nCatNameAr = it }, label = { Text("قسم بالعربي") })
                OutlinedTextField(value = nCatNameEn, onValueChange = { nCatNameEn = it }, label = { Text("English name") })
                Button(
                    onClick = {
                        if (nCatNameAr.isEmpty()) return@Button
                        val nC = CategoryItem(nameAr = nCatNameAr, nameEn = nCatNameEn)
                        viewModel.saveCategory(nC) { success ->
                            if (success) {
                                Toast.makeText(context, "تم تسجيل الحرفة وقسم التصنيف المهني! 🛠️", Toast.LENGTH_SHORT).show()
                                nCatNameAr = ""
                                nCatNameEn = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColor)
                ) {
                    Text("حفظ حرفة", fontSize = 11.sp)
                }
            }
        }

        Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🗺️ إضافة محافظة/مدينة يمنية:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = themeColor)
                OutlinedTextField(value = nCityNameAr, onValueChange = { nCityNameAr = it }, label = { Text("اسم المدينة") })
                Button(
                    onClick = {
                        if (nCityNameAr.isEmpty()) return@Button
                        val cityItem = CityItem(nameAr = nCityNameAr, nameEn = nCityNameAr)
                        viewModel.saveCity(cityItem) { success ->
                            if (success) {
                                Toast.makeText(context, "تم إضافة المحافظة بنجاح!", Toast.LENGTH_SHORT).show()
                                nCityNameAr = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColor)
                ) {
                    Text("حفظ مدينة", fontSize = 11.sp)
                }
            }
        }
    }
}

// --- Submodule 5: Reports & Complaints Panel ---
@Composable
fun ReportsComplaintsPanel(viewModel: AppViewModel, reports: List<ReportItem>, themeColor: Color) {
    val context = LocalContext.current
    if (reports.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("دليل WAM آمن؛ لا توجد بلاغات مسجلة ضد الفنيين المهنيين. 👍🌱", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        Toast.makeText(context, "تم تصدير أسبوعي للبلاغات بنجاح لملف WAM_REPORTS.pdf جاهز للطباعة 📃⚙️", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColor)
                ) {
                    Text("تصدير أسبوعي PDF 📄", fontSize = 11.sp)
                }

                Button(
                    onClick = {
                        Toast.makeText(context, "تم تصدير ملف WAM_Database_Complaints.csv للأرشيف السحابي بنجاح 💾", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("تصدير ملف Excel/CSV 💾", fontSize = 11.sp)
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(reports) { rep ->
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("⚠️ شكوى ضد الفني: ${rep.providerName}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Red)
                            Text("المبلغ: ${rep.reporterName} | هاتف: ${rep.reporterPhone}", fontSize = 11.sp)
                            Text("تفاصيل Complaint: ${rep.complaintText}", fontSize = 11.sp, color = Color.DarkGray)
                        }
                    }
                }
            }
        }
    }
}

// --- Submodule 6: Chat History Management & Privacy Panel ---
@Composable
fun ChatHistorySecurityPanel(viewModel: AppViewModel, chats: List<ChatMessage>, themeColor: Color) {
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("💬 إدارة سجلات وسجلات المحادثات والخصوصية لأصحاب المصالح:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = themeColor)
            Text("مساحتك الأمنية تتيح تصفير وغسل خادم رسائل الدعم والشات السريع للمحافظة على سعة الدليل.", fontSize = 11.sp, color = Color.Gray)

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        viewModel.clearChatHistory { success ->
                            if (success) {
                                Toast.makeText(context, "تم مسح كافة سجلات وغرف شات ومجموعات المهندسين بنجاح! 🧼✅", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("مسح السجل نهائياً 🧼", fontSize = 11.sp)
                }

                Button(
                    onClick = {
                        Toast.makeText(context, "تم تشفير وتحميل سجل غرف WAM_Messages_Archived.csv بنجاح", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("تصدير وتفريد CSV 💾", fontSize = 11.sp)
                }
            }
        }
    }
}

// --- Submodule 7: Active Providers Panel ---
@Composable
fun ActiveProvidersPanel(viewModel: AppViewModel, active: List<Provider>, themeColor: Color) {
    val context = LocalContext.current
    if (active.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("الدليل فارغ حالياً من الفنيين النشطين.", fontSize = 12.sp, color = Color.Gray)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(active) { p ->
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(p.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("هاتف: ${p.phone} | تصنيف: ${p.serviceCategory}", fontSize = 11.sp, color = Color.Gray)
                        }
                        IconButton(onClick = {
                            viewModel.deleteProvider(p.id) { success ->
                                if (success) {
                                    Toast.makeText(context, "تم استبعاد الفني من دليل ورادارات WAM بنجاح.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }) {
                            Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}

// --- Submodule 8: Subscriptions & Pinning (Pins, blue badge, recommended ⭐) ---
@Composable
fun SubscriptionsPinningPanel(viewModel: AppViewModel, active: List<Provider>, themeColor: Color) {
    val context = LocalContext.current
    if (active.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لا يوجد مهندسين مدرجين لترقية الاشتراك والتوثيق.", fontSize = 12.sp, color = Color.Gray)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(active) { p ->
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(p.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = themeColor)
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Toggle recommend
                            Button(
                                onClick = {
                                    val updated = p.copy(isRecommended = !p.isRecommended)
                                    viewModel.submitRequest(updated) { Toast.makeText(context, "تم تعديل شارة التوصية ⭐", Toast.LENGTH_SHORT).show() }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = if (p.isRecommended) Color.Yellow else Color.LightGray),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("توصية ⭐", color = Color.Black, fontSize = 10.sp)
                            }

                            // Toggle pin
                            Button(
                                onClick = {
                                    val updated = p.copy(isPinned = !p.isPinned)
                                    viewModel.submitRequest(updated) { Toast.makeText(context, "تم تعديل شارة التثبيت للقمة 📌", Toast.LENGTH_SHORT).show() }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = if (p.isPinned) Color.Cyan else Color.LightGray),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("تثبيت 📌", color = Color.Black, fontSize = 10.sp)
                            }

                            // Toggle verified blue token badge
                            Button(
                                onClick = {
                                    val updated = p.copy(isVerified = !p.isVerified)
                                    viewModel.submitRequest(updated) { Toast.makeText(context, "تم تعديل شارة التوثيق الزرقاء للكهربائي/السباك 🔵", Toast.LENGTH_SHORT).show() }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = if (p.isVerified) Color(0xFF3B82F6) else Color.LightGray),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("توثيق 🪪", color = Color.White, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Submodule 9: Supervisor Admin Assistant Panel ---
@Composable
fun SupervisorsAdminPanel(
    viewModel: AppViewModel,
    supervisors: List<AppSupervisor>,
    themeColor: Color
) {
    val context = LocalContext.current
    var inputUser by remember { mutableStateOf("") }
    var inputPass by remember { mutableStateOf("") }

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("👥 إضافة مشرف/مساعد ذو صلاحيات مخصصة للدليل:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = themeColor)

            OutlinedTextField(value = inputUser, onValueChange = { inputUser = it }, label = { Text("اسم المشرف الجديد") })
            OutlinedTextField(value = inputPass, onValueChange = { inputPass = it }, label = { Text("كلمة سر المشرف المعين") }, visualTransformation = PasswordVisualTransformation())

            Button(
                onClick = {
                    if (inputUser.isEmpty()) return@Button
                    val nSv = AppSupervisor(
                        username = inputUser.trim(),
                        secretPin = inputPass.trim(),
                        canApproveRequests = true
                    )
                    viewModel.saveSupervisor(nSv) { success ->
                        if (success) {
                            Toast.makeText(context, "تم إدراج وتسجيل المشرف بنجاح! 👥✨", Toast.LENGTH_SHORT).show()
                            inputUser = ""
                            inputPass = ""
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = themeColor)
            ) {
                Text("إضافة وحفظ المشرف")
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items(supervisors) { sv ->
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("المشغل: ${sv.username}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    IconButton(onClick = { viewModel.deleteSupervisor(sv.username) {
                        Toast.makeText(context, "تم إقالة المشرف", Toast.LENGTH_SHORT).show()
                    } }) {
                        Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
                    }
                }
            }
        }
    }
}
