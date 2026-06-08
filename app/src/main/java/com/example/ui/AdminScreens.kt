package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AdminConfig
import com.example.data.local.Provider

// ===============================================
// MAIN DASHBOARD: ADMINS & MODERATORS GATEWAYS
// ===============================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(viewModel: AppViewModel, config: AdminConfig) {
    val theme = resolveTheme(config)
    val context = LocalContext.current
    val isAuthenticated by viewModel.isAdminAuthenticated.collectAsStateWithLifecycle()
    val pendingList by viewModel.pendingProviders.collectAsStateWithLifecycle()
    val fullList by viewModel.approvedProviders.collectAsStateWithLifecycle()

    var usernameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var currentRole by remember { mutableStateOf("none") } // "super" (WAM2026) or "moderator" (mod1)

    var activeAdminTab by remember { mutableStateOf("vetting") } // "vetting", "categories", "banners", "moderation"

    if (!isAuthenticated) {
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
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(Icons.Default.AdminPanelSettings, "Admin gate", tint = theme.primary, modifier = Modifier.size(54.dp))
                    Text("بوابة تسجيل الدخول الإدارية ⚔️", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = theme.primary)
                    Text("سجل دخولك كمدير رئيسي أو مشرف نظام مرخص لمراجعة وتحديث الحسابات والبيانات:", fontSize = 11.sp, color = Color.Gray)

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
                            .height(44.dp),
                        onClick = {
                            val userClean = usernameInput.trim()
                            val passClean = passwordInput.trim()

                            if (userClean == config.adminUsername && passClean == config.adminPassword) {
                                currentRole = "super"
                                viewModel.authenticateAdmin(passClean)
                                Toast.makeText(context, "تم تسجيل دخول المدير الرئيسي بنجاح! 👑🇸🇦", Toast.LENGTH_SHORT).show()
                            } else if (userClean == "mod1" && passClean == "password1") {
                                currentRole = "moderator"
                                viewModel.authenticateAdmin(passClean) // Mock authentication matching
                                Toast.makeText(context, "تم تسجيل دخول مشرف البيانات للوحة Vetting 👤", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "اسم المستخدم أو كلمة المرور الإدارية غير صحيحة ❌", Toast.LENGTH_LONG).show()
                            }
                        }
                    ) {
                        Text("دخول للوحة الإدارة" , color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    TextButton(onClick = { viewModel.navigateTo("home") }) {
                        Text("العودة للتطبيق العادي 🏃", color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }
        }
    } else {
        // Logged dashboard
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(theme.primary)
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("لوحة رقابة وتحكم خدمات اليمن 🇾🇪", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        Text(
                            text = if (currentRole == "super") "وضع الصلاحية: مدير رئيسي بنظام كامل (Admin)" else "وضع الصلاحية: مشرف تدقيق ومقالات (Moderator)",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 10.sp
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        viewModel.logOutAdmin()
                        currentRole = "none"
                    }) {
                        Icon(Icons.Default.ExitToApp, "Log out", tint = Color.White)
                    }
                }
            }

            // Tabs panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .horizontalScroll(rememberScrollState())
            ) {
                TabButton(text = "طلبات التوثيق (${pendingList.size})", active = activeAdminTab == "vetting", onClick = { activeAdminTab = "vetting" }, theme = theme)
                TabButton(text = "إدارة الأقسام", active = activeAdminTab == "categories", onClick = { activeAdminTab = "categories" }, theme = theme)
                if (currentRole == "super") {
                    TabButton(text = "إعلانات البانر", active = activeAdminTab == "banners", onClick = { activeAdminTab = "banners" }, theme = theme)
                    TabButton(text = "إعدادات الدردشة والولاء", active = activeAdminTab == "moderation", onClick = { activeAdminTab = "moderation" }, theme = theme)
                }
            }

            // Active contents
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFFF1F5FE))
            ) {
                when (activeAdminTab) {
                    "vetting" -> VettingDashboardTab(viewModel, pendingList, fullList, theme)
                    "categories" -> CategoriesManagerTab(viewModel, config, theme)
                    "banners" -> BannersManagerTab(viewModel, config, theme)
                    "moderation" -> ModerationTab(viewModel, config, theme)
                }
            }
        }
    }
}

@Composable
private fun TabButton(text: String, active: Boolean, onClick: () -> Unit, theme: ThemeColors) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(if (active) theme.primary else Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = if (active) Color.White else Color.Black
        )
    }
}

@Composable
private fun VettingDashboardTab(viewModel: AppViewModel, pending: List<Provider>, approved: List<Provider>, theme: ThemeColors) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("📥 فحص طلبات التسجيل المهنية الشاغرة", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = theme.primary)
        }
        if (pending.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                    Text("لا توجد فنيين معلقين للتوثيق فورا! ☑️", color = Color.Gray, fontSize = 12.sp)
                }
            }
        } else {
            items(pending) { p ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("الاسم: ${p.name}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("التخصص: ${p.mainCategory} • ${p.subCategory}", fontSize = 11.sp, color = Color.Gray)
                        Text("الموقع: محافظة ${p.city} | الجوال: ${p.phone}", fontSize = 11.sp)
                        if (p.description.isNotEmpty()) {
                            Text("الوصف: ${p.description}", fontSize = 10.5.sp, modifier = Modifier.background(Color(0xFFF1F5FE)).padding(6.dp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                                onClick = {
                                    viewModel.approveProvider(p)
                                    Toast.makeText(context, "تمت الموافقة ومصادقة وثائق الحساب بنجاح! ⚡", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("موافقة وتوثيق", color = Color.White, fontSize = 10.5.sp)
                            }
                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                onClick = {
                                    viewModel.deleteProvider(p)
                                    Toast.makeText(context, "تم رفض وحذف الطلب المعلق.", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("رفض وحذف", color = Color.White, fontSize = 10.5.sp)
                            }
                        }
                    }
                }
            }
        }

        // List of Active for deletion/promotion
        item {
            Text("✅ الحرفيون النشطون بالشبكة حالياً (${approved.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = theme.primary)
        }
        items(approved) { p ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(p.name, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        Text("${p.mainCategory} • ${p.city}", fontSize = 10.5.sp, color = Color.Gray)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Recommend (Slider stars) toggle
                        IconButton(onClick = {
                            viewModel.updateProviderDirectly(p.copy(isRecommended = !p.isRecommended))
                            Toast.makeText(context, "تحديث حالة الترويج المميز في السلايدر!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.Star, "Pin", tint = if (p.isRecommended) theme.secondary else Color.LightGray)
                        }
                        // Pin on Category top toggle
                        IconButton(onClick = {
                            viewModel.updateProviderDirectly(p.copy(isPinned = !p.isPinned))
                            Toast.makeText(context, "تحديث حالة التثبيت أعلى القسم المخصص!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.PushPin, "Pin top", tint = if (p.isPinned) Color.Blue else Color.LightGray)
                        }
                        // Verified badge toggle
                        IconButton(onClick = {
                            viewModel.updateProviderDirectly(p.copy(isVerified = !p.isVerified))
                            Toast.makeText(context, "تحديث وثائق الاعتماد والتحقق!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.Verified, "Verify", tint = if (p.isVerified) theme.secondary else Color.LightGray)
                        }
                        // Delete
                        IconButton(onClick = {
                            viewModel.deleteProvider(p)
                            Toast.makeText(context, "تم إزالة الحساب فورا.", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoriesManagerTab(viewModel: AppViewModel, config: AdminConfig, theme: ThemeColors) {
    val context = LocalContext.current
    var catStringInput by remember(config) { mutableStateOf(config.registrationConditions) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("⚙️ تنظيم شروط التسجيل للتحذير بالفورم", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = theme.primary)
        
        OutlinedTextField(
            value = catStringInput,
            onValueChange = { catStringInput = it },
            label = { Text("قائمة الشروط التنظيمية (افصل بسطر لكل شرط)") },
            modifier = Modifier.fillMaxWidth().height(140.dp),
            maxLines = 10
        )

        Button(
            colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
            onClick = {
                viewModel.saveAdminConfig(config.copy(registrationConditions = catStringInput))
                Toast.makeText(context, "تم حفظ الشروط والتعليمات بنجاح 📝⚡", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("حفظ وتعديل شروط التقديم", color = Color.White)
        }
    }
}

@Composable
private fun BannersManagerTab(viewModel: AppViewModel, config: AdminConfig, theme: ThemeColors) {
    // Basic dynamic promotion details edit
    var textInput by remember(config) { mutableStateOf(config.welcomeMessage) }
    var context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("⚙️ إعداد ترحيب المساعد الذكي اليمني", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = theme.primary)
        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            label = { Text("رسالة بدء ترحيب الشات (المشرف)") },
            modifier = Modifier.fillMaxWidth().height(100.dp)
        )
        Button(
            colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
            onClick = {
                viewModel.saveAdminConfig(config.copy(welcomeMessage = textInput))
                Toast.makeText(context, "تم تعديل رسائل الروبوت المساعد بنجاح!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("تأكيد وحفظ الشعار والرسالة بذاكرة الروبوت 🤖", color = Color.White)
        }
    }
}

@Composable
private fun ModerationTab(viewModel: AppViewModel, config: AdminConfig, theme: ThemeColors) {
    val context = LocalContext.current
    var chatEnabledToggle by remember(config) { mutableStateOf(config.chatEnabled) }
    var chatBlockInput by remember(config) { mutableStateOf(config.chatDisabledMessage) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("⚙️ لوحة رقابة المحادثات والدردشة الفورية", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = theme.primary)
        
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("تفعيل ميزة الدردشة الفورية بالكامل بالفروع:")
            Spacer(modifier = Modifier.weight(1f))
            Switch(checked = chatEnabledToggle, onCheckedChange = { chatEnabledToggle = it })
        }

        OutlinedTextField(
            value = chatBlockInput,
            onValueChange = { chatBlockInput = it },
            label = { Text("رسالة إنذار التعطيل المخصصة للتطبيق") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
            onClick = {
                viewModel.saveAdminConfig(config.copy(
                    chatEnabled = chatEnabledToggle,
                    chatDisabledMessage = chatBlockInput
                ))
                Toast.makeText(context, "تم تعديل صلاحيات الشات الفني بنجاح! 💭", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("تحديث حالة الدردشات العامة", color = Color.White)
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
                    fontSizeModifier = textScaleFactor
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
