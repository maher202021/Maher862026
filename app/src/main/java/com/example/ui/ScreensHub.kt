package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.local.AdminConfig
import com.example.data.local.ChatMessage
import com.example.data.local.Provider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ===================================
// SUB-SCREEN: BOOKMARKED ARCHIVE
// ===================================
@Composable
fun BookmarkedScreen(viewModel: AppViewModel, config: AdminConfig, onProviderClick: (Provider) -> Unit = {}) {
    val bookmarkedProviders by viewModel.bookmarkedProviders.collectAsStateWithLifecycle()
    val theme = resolveTheme(config)

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.primary)
                .padding(14.dp)
        ) {
            Column {
                Text(
                    text = "⭐ فنيين محفوظين أوفلاين",
                    fontFamily = AppMainFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = (15 * config.fontSizeModifier).sp,
                    color = Color.White
                )
                Text(
                    text = "الوصول السريع لجميع معلومات الاتصال المسجلة والواتساب بدون اتصال بالإنترنت.",
                    fontFamily = AppMainFont,
                    fontSize = (10 * config.fontSizeModifier).sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }

        if (bookmarkedProviders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Icon(imageVector = Icons.Default.BookmarkBorder, contentDescription = "لا توجد", modifier = Modifier.size(54.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("لا توجد محفوظات حالياً 📂", fontFamily = AppMainFont, fontWeight = FontWeight.Bold, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().weight(1f), contentPadding = PaddingValues(10.dp)) {
                items(bookmarkedProviders) { provider ->
                    TechnicianCard(provider, viewModel, config, onProviderClick)
                }
            }
        }
    }
}

// ===================================
// SUB-SCREEN: ABOUT APPLICATION & HELP
// ===================================
@Composable
fun InfoScreen(viewModel: AppViewModel, config: AdminConfig) {
    val context = LocalContext.current
    val theme = resolveTheme(config)
    val providers by viewModel.approvedProviders.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = config.appName,
                    fontFamily = AppMainFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = theme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("إصدار تطبيق فنيي اليمن: v2.4.0 • مستقر", fontSize = 11.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "الدليل الرقمي والخدمي الوطني الأول المحمول في اليمن. يوفر إمكانية الإتصال المباشر بالفنيين المهنيين في كافة المحافظات بمختلف الحرف والحصول على استشارات ذكية مجانية.",
                    fontFamily = AppMainFont,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }

        // Optional custom about app illustration/banner
        if (config.showImageInAbout && config.aboutAppImage.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().height(160.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                AsyncImage(
                    model = config.aboutAppImage,
                    contentDescription = "About App Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Stats Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${providers.size}", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = theme.primary)
                    Text("مهني معتمد", fontSize = 10.sp, color = Color.Gray)
                }
                Divider(modifier = Modifier.width(1.dp).height(36.dp), color = Color.LightGray)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("7+", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = theme.primary)
                    Text("تخصصات خدمية", fontSize = 10.sp, color = Color.Gray)
                }
                Divider(modifier = Modifier.width(1.dp).height(36.dp), color = Color.LightGray)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("100% Offline", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF25D366))
                    Text("دعم محلي مستقل", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }

        // Sponsoring Direct contact support cards
        if (config.showPhoneInAbout || config.showWhatsappInAbout || config.showEmailInAbout) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("📞 قنوات التواصل والدعم الفني", fontWeight = FontWeight.Bold, color = theme.primary, fontSize = 13.sp)

                    // Call
                    if (config.showPhoneInAbout && config.supportPhone.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${config.supportPhone}")))
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = "Phone", tint = theme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("رقم الإدارة المباشر: ${config.supportPhone}", fontSize = 12.sp)
                        }
                    }

                    // WhatsApp
                    if (config.showWhatsappInAbout && config.supportWhatsapp.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val msg = Uri.encode("السلام عليكم يا إدارة، استفسار بخصوص تطبيق خدمات اليمن...")
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=967${config.supportWhatsapp}&text=$msg")))
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = "whatsapp", tint = Color(0xFF25D366), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("الواتساب المباشر: ${config.supportWhatsapp}", fontSize = 12.sp)
                        }
                    }

                    // Email
                    if (config.showEmailInAbout && config.supportEmail.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${config.supportEmail}")))
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Mail, contentDescription = "Mail", tint = theme.secondary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("البريد الإلكتروني: ${config.supportEmail}", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Sharing action
        if (config.showShareInAbout && config.aboutAppShareLink.isNotEmpty()) {
            Button(
                onClick = {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "قم بتحميل التحديث الجديد لتطبيق '${config.appName} 🇾🇪' - دليل الفنيين مع لوحة إدارة وسرعة فائقة دون إنترنت! حمل من الرابط: ${config.aboutAppShareLink}")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "مشاركة التطبيق"))
                },
                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("مشاركة دليل ${config.appName} للآخرين 🚀", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ===================================
// SUB-SCREEN: REGISTRATION FORM
// ===================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(viewModel: AppViewModel, config: AdminConfig) {
    val theme = resolveTheme(config)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var whatsappInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var subCategoryInput by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf("") }
    var workAddressInput by remember { mutableStateOf("") }
    var genderOption by remember { mutableStateOf("ذكر") }
    var descriptionInput by remember { mutableStateOf("") }
    
    var showConditionCheckboxes by remember { mutableStateOf(false) }
    val conditionsList = remember(config.registrationConditions) {
        config.registrationConditions.split("\n").filter { it.trim().isNotEmpty() }
    }
    // Track accept status of each individual rule condition dynamically
    val checklistAgreedState = remember(conditionsList) {
        mutableStateMapOf<Int, Boolean>().apply {
            conditionsList.indices.forEach { put(it, false) }
        }
    }
    val allAgreed = checklistAgreedState.values.all { it }

    var profilePhotoUriSimulated by remember { mutableStateOf<String?>(null) }
    var idPhotoUriSimulated by remember { mutableStateOf<String?>(null) }
    var showFemaleAvatarSelected by remember { mutableStateOf(false) }

    val citiesList = listOf("صنعاء", "عدن", "تعز", "الحديدة", "حضرموت", "إب", "ذمار", "مأرب")
    val categoriesList = listOf("كهرباء وإلكترونيات", "سباكة وصحي", "نجارة وديكور", "تكييف وتبريد", "حدادة وألومنيوم", "خياطة وتفصيل", "أخرى")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("👤 نموذج تقديم تسجيل فني جديد", fontWeight = FontWeight.Bold, color = theme.primary, fontSize = 16.sp)
        Text("يرجى تعبئة كافة التفاصيل المهنية بدقة ليتم فحص حسابك وتوثيقه من قِبل المدققين بسرعة.", fontSize = 11.sp, color = Color.Gray)

        OutlinedTextField(
            value = nameInput,
            onValueChange = { nameInput = it },
            label = { Text("الاسم الكامل للحرفي/الفني") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = phoneInput,
                onValueChange = { phoneInput = it },
                label = { Text("رقم الجوال (9 أرقام)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = whatsappInput,
                onValueChange = { whatsappInput = it },
                label = { Text("رقم الواتساب (اختياري)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.weight(1f)
            )
        }

        // Gender toggles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("الجنس للتصنيف:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { genderOption = "ذكر" }) {
                    RadioButton(selected = genderOption == "ذكر", onClick = { genderOption = "ذكر" })
                    Text("ذكر 🚹", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { genderOption = "أنثى" }) {
                    RadioButton(selected = genderOption == "أنثى", onClick = { genderOption = "أنثى" })
                    Text("أنثى 🚺", fontSize = 12.sp)
                }
            }
        }

        // Dynamic categories selector dropdown logic
        var catExpanded by remember { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text("القسم الرئيسي للمهنة") },
                trailingIcon = { IconButton(onClick = { catExpanded = true }) { Icon(Icons.Default.ArrowDropDown, "Dropdown") } },
                modifier = Modifier.fillMaxWidth().clickable { catExpanded = true }
            )
            DropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                categoriesList.forEach { c ->
                    DropdownMenuItem(
                        text = { Text(c) },
                        onClick = {
                            selectedCategory = c
                            catExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = subCategoryInput,
            onValueChange = { subCategoryInput = it },
            label = { Text("المجال الدقيق (مثال: تركيب غرف نوم، طاقة شمسية)") },
            modifier = Modifier.fillMaxWidth()
        )

        // Cities selector dropdown
        var cityExpanded by remember { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedCity,
                onValueChange = {},
                readOnly = true,
                label = { Text("المحافظة المتواجد بها") },
                trailingIcon = { IconButton(onClick = { cityExpanded = true }) { Icon(Icons.Default.ArrowDropDown, "Dropdown") } },
                modifier = Modifier.fillMaxWidth().clickable { cityExpanded = true }
            )
            DropdownMenu(expanded = cityExpanded, onDismissRequest = { cityExpanded = false }) {
                citiesList.forEach { city ->
                    DropdownMenuItem(
                        text = { Text(city) },
                        onClick = {
                            selectedCity = city
                            cityExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = workAddressInput,
            onValueChange = { workAddressInput = it },
            label = { Text("عنوان العمل ورقم المحل بالضبط") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = descriptionInput,
            onValueChange = { descriptionInput = it },
            label = { Text("وصف خبراتك وأعمالك والأسعار التقريبية") },
            modifier = Modifier.fillMaxWidth().height(100.dp)
        )

        // Picture choices
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = if (profilePhotoUriSimulated != null) Color(0xFF2E7D32) else theme.secondary),
                onClick = {
                    if (genderOption == "أنثى") {
                        showFemaleAvatarSelected = true
                        profilePhotoUriSimulated = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=400"
                        Toast.makeText(context, "تم التقاط أيقونة المهنة التعبيرية المناسبة للصبايا! 🚺🎨", Toast.LENGTH_SHORT).show()
                    } else {
                        profilePhotoUriSimulated = "https://images.unsplash.com/photo-1540569014015-19a7be504e3a?w=400"
                        Toast.makeText(context, "تم التقاط وضغط صورة البروفايل بدقة مصغرة (60KB) 📸", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (profilePhotoUriSimulated != null) Icons.Default.CheckCircle else Icons.Default.PhotoCamera,
                    contentDescription = "camera",
                    tint = if (profilePhotoUriSimulated != null) Color.White else Color.Black
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (profilePhotoUriSimulated != null) "تم التقاط الصورة ✅" else (if (genderOption == "أنثى") "شعار المهنة (اختياري) 🎨" else "صورة شخصية 👤"),
                    fontSize = 10.sp,
                    color = if (profilePhotoUriSimulated != null) Color.White else Color.Black
                )
            }

            Button(
                colors = ButtonDefaults.buttonColors(containerColor = if (idPhotoUriSimulated != null) Color(0xFF2E7D32) else Color.Gray),
                onClick = {
                    idPhotoUriSimulated = "https://images.unsplash.com/photo-1554774853-aae0a22c8aa4?w=400"
                    Toast.makeText(context, "تم فحص ورفع صورة كرت المهنة اختيارياً 👍", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1.0f)
            ) {
                Icon(
                    imageVector = if (idPhotoUriSimulated != null) Icons.Default.CheckCircle else Icons.Default.CardMembership,
                    contentDescription = "License",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (idPhotoUriSimulated != null) "تم الرفع بنجاح ✅" else "رفع وثيقة المهنة 🗃️",
                    fontSize = 10.sp,
                    color = Color.White
                )
            }
        }

        // Accept dynamic regulation conditions terms lists
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Text("📝 شروط وتعليمات المصادقة والتسجيل:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = theme.primary)
            Spacer(modifier = Modifier.height(6.dp))
            conditionsList.forEachIndexed { idx, cond ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { checklistAgreedState[idx] = !(checklistAgreedState[idx] ?: false) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = checklistAgreedState[idx] ?: false,
                        onCheckedChange = { checklistAgreedState[idx] = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(cond, fontSize = 10.5.sp, modifier = Modifier.weight(1f))
                }
            }
        }

        SecureSubmitButton(
            enabled = allAgreed && nameInput.isNotEmpty() && phoneInput.isNotEmpty() && selectedCategory.isNotEmpty() && selectedCity.isNotEmpty(),
            primaryColor = theme.primary,
            onSubmit = {
                // Number validation
                if (!isValidYemeniMobile(phoneInput)) {
                    Toast.makeText(context, "الرجاء إدخال رقم جوال يمني صحيح يبدأ بـ (77, 73, 71, 70) ومن 9 خانات ❌", Toast.LENGTH_LONG).show()
                } else {
                    scope.launch {
                        Toast.makeText(context, "جاري معالجة وتصغير حجم الملفات والصور... 🔄", Toast.LENGTH_SHORT).show()
                        delay(1200)
                        viewModel.submitPendingProvider(
                            name = nameInput.trim(),
                            mainCategory = selectedCategory,
                            subCategory = subCategoryInput.trim(),
                            city = selectedCity,
                            phone = phoneInput.trim(),
                            whatsapp = whatsappInput.trim(),
                            gender = genderOption,
                            description = descriptionInput.trim(),
                            photoUri = profilePhotoUriSimulated,
                            idPhotoUri = idPhotoUriSimulated
                        )
                        Toast.makeText(context, "تم إرسال طلب انضمامك للمشرفين لمصادقته! شكراً لك 🇸🇦🇾🇪", Toast.LENGTH_LONG).show()
                        // Reset Inputs
                        nameInput = ""
                        phoneInput = ""
                        whatsappInput = ""
                        subCategoryInput = ""
                        workAddressInput = ""
                        descriptionInput = ""
                        viewModel.navigateTo("home")
                    }
                }
            }
        )
    }
}

@Composable
fun SecureSubmitButton(enabled: Boolean, primaryColor: Color, onSubmit: () -> Unit) {
    Button(
        enabled = enabled,
        onClick = onSubmit,
        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
    ) {
        Icon(Icons.Default.Verified, "Verify", tint = Color.White)
        Spacer(modifier = Modifier.width(6.dp))
        Text("تأكيد ورفع طلب الانضمام والمصادقة", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

// ===================================
// SUB-SCREEN: PEER CHAT SIMULATOR
// ===================================
@Composable
fun PeerChatScreen(viewModel: AppViewModel, config: AdminConfig, provider: Provider) {
    val theme = resolveTheme(config)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()

    var messageInput by remember { mutableStateOf("") }
    var isChatBlockedByAdmin by remember { mutableStateOf(false) }

    // Listen to live frozen state of this chat room
    LaunchedEffect(provider.id) {
        try {
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            firestore.collection("chats").document("chat_user_${provider.id}")
                .addSnapshotListener { snapshot, error ->
                    if (snapshot != null) {
                        isChatBlockedByAdmin = snapshot.getBoolean("isBlocked") ?: false
                    }
                }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    // Filter messages to show conversation with this provider
    val filteredMessages = remember(chatMessages, provider) {
        chatMessages.filter { 
            (it.sender == "user" && it.receiverId == provider.id) || 
            (it.sender == "peer" && it.receiverId == provider.id)
        }
    }

    if (!config.chatEnabled) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Card(
                modifier = Modifier.padding(24.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.PhoneCallback, "closed", tint = Color.Red, modifier = Modifier.size(54.dp))
                    Text("المحادثة الفورية معطلة 🛑", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Red)
                    Text(config.chatDisabledMessage, textAlign = TextAlign.Center, fontSize = 12.sp)
                    Button(colors = ButtonDefaults.buttonColors(containerColor = theme.primary), onClick = { viewModel.navigateTo("home") }) {
                        Text("العودة للدليل")
                    }
                }
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            // Screen header details
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = theme.primary)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.navigateTo("home") }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                    Box(modifier = Modifier.size(40.dp)) {
                        if (provider.photoUri != null) {
                            AsyncImage(
                                model = provider.photoUri,
                                contentDescription = "Face",
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize().background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
                                Text(provider.name.take(1), fontWeight = FontWeight.Bold, color = theme.primary)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(provider.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("نشط حالياً • رد فوري هاتف ذكي", color = Color.White.copy(alpha = 0.85f), fontSize = 10.sp)
                    }
                }
            }

            // Message list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFFE5DDD5))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (filteredMessages.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            Text("ابدأ الدردشة السريعة الآن مع الفني بكل أمان! 🤝", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                } else {
                    items(filteredMessages) { msg ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (msg.sender == "user") Arrangement.End else Arrangement.Start
                        ) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (msg.sender == "user") Color(0xFFDCF8C6) else Color.White
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = msg.message,
                                        fontSize = 13.sp,
                                        fontFamily = AppMainFont,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black // Explicit deep high-contrast black for total legibility
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Input Send Row
            if (isChatBlockedByAdmin) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFEE2E2))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚠️ هذه المحادثة مجمّدة ومقيدة مؤقتاً بقرار من الرقابة الإدارية ❄️",
                        fontFamily = AppMainFont,
                        color = Color(0xFF991B1B),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5FE))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        placeholder = { 
                            Text(
                                text = "أرسل رسالة فورية إلى الفني...", 
                                color = Color.Gray.copy(alpha = 0.8f),
                                fontFamily = AppMainFont, 
                                fontSize = 12.sp
                            ) 
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = theme.primary,
                            unfocusedBorderColor = Color.LightGray
                        ),
                        shape = RoundedCornerShape(24.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = AppMainFont, 
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    )
                    IconButton(
                        onClick = {
                            val text = messageInput.trim()
                            if (text.isNotEmpty()) {
                                scope.launch {
                                    // Add user message
                                    viewModel.saveAdminConfig(config) // touch configs
                                    // Insert Chat Message under simulated peers
                                    dbHelperInsert(viewModel, "user", text, provider.id)
                                    messageInput = ""
                                    
                                    // Simulate Peer replies in 1.2 seconds
                                    delay(1200)
                                    val replies = listOf(
                                        "أبشر من عيوني يا غالي! أنا جاهز ومستعد للعمل، تواصل معي هاتفياً لتحديد الوقت بالضبط 🤝.",
                                        "سابر يا سيدي، متى تحب أكون عندك؟ أرجو إرسال عنوانك وتفاصيل العطل 🛠️.",
                                        "حياك الله يا طيب، متوفر حالياً وبكافة معداتي، التفاصيل سبرت وسهلة بإذن الله."
                                    )
                                    dbHelperInsert(viewModel, "peer", replies.random(), provider.id)
                                }
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .background(theme.primary, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send, 
                            contentDescription = "Send", 
                            tint = Color.White,
                            modifier = Modifier
                                .size(20.dp)
                                .scale(scaleX = -1f, scaleY = 1f) // Mirror send arrow horizontally for pure RTL look
                        )
                    }
                }
            }
        }
    }
}

// Simulated insertion bridges safely using background coroutines on viewModel database states
private fun dbHelperInsert(vm: AppViewModel, sender: String, text: String, receiverId: Int) {
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
        // Safe SQLite direct insertion
        val dbHelper = com.example.data.local.AppDatabase(vm.getApplication())
        dbHelper.insertChatMessage(sender, text, senderName = "الطرف الآخر", receiverId = receiverId)
    }
}
