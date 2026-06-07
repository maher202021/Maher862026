package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.local.AdminConfig
import com.example.data.local.Provider

// --- Yemeni Oasis Palette ---
val PrimaryColor = Color(0xFF0E6F4B) // Desert Oasis Green (#0E6F4B)
val GoldenAccent = Color(0xFFD4AF37) // Yemeni Gold (#D4AF37)
val DeepCoral = Color(0xFFEF5350) // Coral alert red
val SlateBackground = Color(0xFFF1F5FE) // Crisp light gray background
val DarkCardBg = Color(0xFF1E293B) // Premium dark slate card
val AdminVelvetBg = Color(0xFF0A0F1D) // Night theme for Admin panel

// --- Shared Display Typography ---
val AppTitleFont = FontFamily.SansSerif

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AppNavigationScaffold(viewModel: AppViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val config by viewModel.adminConfig.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Observe offline bookmarks & pending indices for badges
    val bookmarkedProviders by viewModel.bookmarkedProviders.collectAsStateWithLifecycle()
    val pendingProviders by viewModel.pendingProviders.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryColor,
                    titleContentColor = Color.White
                ),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(end = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "كل خدمات اليمن 🇾🇪",
                            fontFamily = AppTitleFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        // Secret gate lock trigger: long press icon opens admin gate
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "بوابة سرية",
                            tint = GoldenAccent,
                            modifier = Modifier
                                .size(28.dp)
                                .combinedClickable(
                                    onClick = {
                                        Toast.makeText(context, "اضغط مطولاً لفتح بوابة الإدارة السرية 🔒", Toast.LENGTH_SHORT).show()
                                    },
                                    onLongClick = {
                                        viewModel.navigateTo("admin")
                                        Toast.makeText(context, "مرحباً بك في البوابة السرية للإدارة! أدخل الرمز 🛠️", Toast.LENGTH_LONG).show()
                                    }
                                )
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                // Screen 1: Home
                NavigationBarItem(
                    selected = currentScreen == "home",
                    onClick = { viewModel.navigateTo("home") },
                    icon = { Icon(Icons.Default.Home, contentDescription = "الرئيسية") },
                    label = { Text("الدليل", fontFamily = AppTitleFont, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryColor,
                        selectedTextColor = PrimaryColor,
                        indicatorColor = PrimaryColor.copy(alpha = 0.1f)
                    )
                )

                // Screen 2: AI Assistance
                NavigationBarItem(
                    selected = currentScreen == "chat",
                    onClick = { viewModel.navigateTo("chat") },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "أبو يمن الذكي") },
                    label = { Text("المساعد", fontFamily = AppTitleFont, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryColor,
                        selectedTextColor = PrimaryColor,
                        indicatorColor = PrimaryColor.copy(alpha = 0.1f)
                    )
                )

                // Screen 3: Register
                NavigationBarItem(
                    selected = currentScreen == "register",
                    onClick = { viewModel.navigateTo("register") },
                    icon = { Icon(Icons.Default.PersonAdd, contentDescription = "تسجيل فني") },
                    label = { Text("انضم كمهني", fontFamily = AppTitleFont, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryColor,
                        selectedTextColor = PrimaryColor,
                        indicatorColor = PrimaryColor.copy(alpha = 0.1f)
                    )
                )

                // Screen 4: Offline Bookmarks
                NavigationBarItem(
                    selected = currentScreen == "bookmarks",
                    onClick = { viewModel.navigateTo("bookmarks") },
                    icon = {
                        Box {
                            Icon(Icons.Default.Bookmark, contentDescription = "المحفوظات")
                            if (bookmarkedProviders.isNotEmpty()) {
                                Badge(
                                    containerColor = GoldenAccent,
                                    modifier = Modifier.align(Alignment.TopEnd).offset(x = 6.dp, y = (-4).dp)
                                ) {
                                    Text(bookmarkedProviders.size.toString(), color = Color.Black, fontSize = 8.sp)
                                }
                            }
                        }
                    },
                    label = { Text("المحفوظات", fontFamily = AppTitleFont, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryColor,
                        selectedTextColor = PrimaryColor,
                        indicatorColor = PrimaryColor.copy(alpha = 0.1f)
                    )
                )
            }
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(SlateBackground)
            ) {
                when (currentScreen) {
                    "home" -> DirectoryScreen(viewModel, config)
                    "chat" -> ChatScreen(viewModel, config)
                    "register" -> RegistrationScreen(viewModel, config)
                    "bookmarks" -> BookmarkedScreen(viewModel, config)
                    "admin" -> AdminScreen(viewModel, config)
                }
            }
        }
    )
}

// ==========================================
// SCREEN 1: COMPREHENSIVE DIRECTORY HUB
// ==========================================
@Composable
fun DirectoryScreen(viewModel: AppViewModel, config: AdminConfig) {
    val searchVal by viewModel.searchQuery.collectAsStateWithLifecycle()
    val activeCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val activeCity by viewModel.selectedCity.collectAsStateWithLifecycle()
    val activeGender by viewModel.selectedGender.collectAsStateWithLifecycle()
    val providers by viewModel.approvedProviders.collectAsStateWithLifecycle()

    val categoriesList = listOf("الكل", "كهرباء وإلكترونيات", "سباكة وصحي", "نجارة وديكور", "تكييف وتبريد", "حدادة وألومنيوم", "خياطة وتفصيل", "أخرى")
    val citiesList = listOf("الكل", "صنعاء", "عدن", "تعز", "الحديدة", "حضرموت", "إب", "ذمار")
    val genderList = listOf("الكل", "ذكر", "أنثى")

    Column(modifier = Modifier.fillMaxSize()) {
        // Welcoming Statistics banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(PrimaryColor.copy(alpha = 0.9f), PrimaryColor.copy(alpha = 0.1f)),
                        startY = 0f,
                        endY = 320f
                    )
                )
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "دليل المهن الشامل والخدمات 🛠️",
                    fontFamily = AppTitleFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = (18 * config.fontSizeModifier).sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "تواصل مباشرة مع فنيين يمنيين بدون إنترنت بعد التحميل! متوفر حالياً ${providers.size} مقدم خدمة نشط.",
                    fontFamily = AppTitleFont,
                    fontSize = (11 * config.fontSizeModifier).sp,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Actionable filter bar cards
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .offset(y = (-10).dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Search box
                OutlinedTextField(
                    value = searchVal,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("ابحث باسم الفني، الحرفة، المهارة...", fontFamily = AppTitleFont, fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث", tint = PrimaryColor) },
                    trailingIcon = {
                        if (searchVal.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "مسح", tint = Color.Gray)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_field"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Category filters row
                Text("تخصص المهنة:", fontFamily = AppTitleFont, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categoriesList) { cat ->
                        FilterChip(
                            selected = activeCategory == cat,
                            onClick = { viewModel.updateSelectedCategory(cat) },
                            label = { Text(cat, fontFamily = AppTitleFont, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryColor,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable Governorates/City filters row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("المحافظة:", fontFamily = AppTitleFont, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(citiesList) { city ->
                                FilterChip(
                                    selected = activeCity == city,
                                    onClick = { viewModel.updateSelectedCity(city) },
                                    label = { Text(city, fontFamily = AppTitleFont, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryColor,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(0.4f)) {
                        Text("الجنس:", fontFamily = AppTitleFont, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(genderList) { gender ->
                                FilterChip(
                                    selected = activeGender == gender,
                                    onClick = { viewModel.updateSelectedGender(gender) },
                                    label = { Text(gender, fontFamily = AppTitleFont, fontSize = 9.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryColor,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Technicians list section
        if (providers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Icon(imageVector = Icons.Default.SearchOff, contentDescription = "لا توجد نتائج", modifier = Modifier.size(54.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "عذراً يا غالي! لم نعثر على نتائج مأهولة للفلاتر المحددة 🔍",
                        fontFamily = AppTitleFont,
                        fontSize = (13 * config.fontSizeModifier).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "جرب تغيير المحافظة أو مسح شريط البحث لإيجاد المهنيين النشطين.",
                        fontFamily = AppTitleFont,
                        fontSize = (11 * config.fontSizeModifier).sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                items(providers) { provider ->
                    TechnicianCard(provider, viewModel, config)
                }
            }
        }
    }
}

// ==========================================
// COMPONENT: HIGHLY POLISHED TECHNICIAN CARD
// ==========================================
@Composable
fun TechnicianCard(provider: Provider, viewModel: AppViewModel, config: AdminConfig) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .shadow(2.dp, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Profile Photo Frame with online/offline loading or default placeholder
                Box(modifier = Modifier.size(54.dp)) {
                    if (provider.photoUri != null) {
                        AsyncImage(
                            model = provider.photoUri,
                            contentDescription = "صورة الفني",
                            modifier = Modifier.fillMaxSize().clip(CircleShape).border(1.5.dp, PrimaryColor, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(PrimaryColor.copy(alpha = 0.1f))
                                .border(1.5.dp, PrimaryColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = provider.name.take(1),
                                color = PrimaryColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                fontFamily = AppTitleFont
                            )
                        }
                    }
                    
                    // Small gender indicator badge at bottom right
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(if (provider.gender == "ذكر") Color(0xFF64B5F6) else Color(0xFFF06292)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (provider.gender == "ذكر") "🚹" else "🚺",
                            fontSize = 8.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Detail specs
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = provider.name,
                            fontFamily = AppTitleFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = (14 * config.fontSizeModifier).sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        if (provider.isVerified) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "فني معتمد وموثق",
                                tint = GoldenAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "موثق",
                                color = GoldenAccent,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AppTitleFont
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${provider.mainCategory} • ${provider.subCategory}",
                        fontFamily = AppTitleFont,
                        fontSize = (11 * config.fontSizeModifier).sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = "موقع", tint = PrimaryColor, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "اليمن، محافظة ${provider.city}",
                            fontFamily = AppTitleFont,
                            fontSize = (10 * config.fontSizeModifier).sp,
                            color = Color.DarkGray
                        )
                    }
                }

                // Bookmark icon action
                IconButton(
                    onClick = {
                        viewModel.toggleBookmark(provider)
                        Toast.makeText(
                            context,
                            if (provider.isBookmarked) "تم الإزالة من المفضلة المحلية ❌" else "تمت الإضافة للمفضلة للوصول السريع دون إنترنت ⭐️",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                ) {
                    Icon(
                        imageVector = if (provider.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "تثبيت المفضل",
                        tint = if (provider.isBookmarked) GoldenAccent else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            // Description of craftsmanship tools or rate guidelines
            if (provider.description.isNotEmpty()) {
                Text(
                    text = provider.description,
                    fontFamily = AppTitleFont,
                    fontSize = (10.5 * config.fontSizeModifier).sp,
                    color = Color.DarkGray,
                    lineHeight = 15.sp,
                    modifier = Modifier.fillMaxWidth().background(SlateBackground.copy(alpha = 0.5f)).padding(8.dp).clip(RoundedCornerShape(6.dp))
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Direct Communication actions (Fully Offline-capable intents!)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Call Phone Action
                Button(
                    onClick = {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${provider.phone}"))
                        context.startActivity(dialIntent)
                    },
                    modifier = Modifier.weight(1f).height(38.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(2.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = "اتصال تلفوني", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("اتصال جوال", fontFamily = AppTitleFont, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                // Direct WhatsApp Action
                if (provider.whatsapp.isNotEmpty()) {
                    Button(
                        onClick = {
                            val msg = "السلام عليكم يا باشا، تواصلت معك من تطبيق 'كل خدمات اليمن' بخصوص خدمة مطلوبة..."
                            val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=967${provider.whatsapp}&text=${Uri.encode(msg)}"))
                            try {
                                context.startActivity(waIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "الواتساب لم يُثبّت، الرقم الخاص به: ${provider.whatsapp}", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.weight(1f).height(38.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(2.dp)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = "واتساب مباشر", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("واتساب مباشر", fontFamily = AppTitleFont, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 2: INTELLIGENT AI GEMINI ASSISTANT
// ==========================================
@Composable
fun ChatScreen(viewModel: AppViewModel, config: AdminConfig) {
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isChatLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()
    var userTextInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    val quickPrompts = listOf(
        "نصائح ربط لوحة طاقة شمسية 🔋",
        "خطوات تصليح صنبور ماء يسرب 💧",
        "كم يكلف تقريباً فحص خزان؟ 💰",
        "تعليمات أمان تمديد كهرباء البيت ⚡"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // AI Title Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryColor)
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(GoldenAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🇾🇪", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "المساعد الذكي 'أبو يمن' 🤖",
                        fontFamily = AppTitleFont,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = (14 * config.fontSizeModifier).sp
                    )
                    Text(
                        text = "يتحدث بلهجتك وينصحك في السباكة والكهرباء والتسعير",
                        fontFamily = AppTitleFont,
                        fontSize = (10 * config.fontSizeModifier).sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                
                // Clear chat history
                IconButton(onClick = {
                    viewModel.clearChatMessages()
                    Toast.makeText(context, "تم مسح سجل الدردشة للتنظيف 🗑️", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "تنظيف", tint = Color.White)
                }
            }
        }

        // Suggested Rate Bar info
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(GoldenAccent.copy(alpha = 0.15f))
                .border(BorderStroke(1.dp, GoldenAccent.copy(alpha = 0.25f)))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💸 السعر الاسترشادي المعتمد للمهني الفني بالساعة:",
                    fontSize = (10 * config.fontSizeModifier).sp,
                    fontFamily = AppTitleFont,
                    color = Color.DarkGray
                )
                Text(
                    text = "${config.baseAppRateHourYER} ريال يمني / ساعة",
                    fontSize = (11 * config.fontSizeModifier).sp,
                    fontFamily = AppTitleFont,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryColor
                )
            }
        }

        // Messages list
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (messages.isEmpty()) {
                // Welcoming Empty Chat Box Helper
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "مرحباً يا غالي ببروفايل المساعد الذكي! 👋",
                        fontFamily = AppTitleFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = (14 * config.fontSizeModifier).sp,
                        color = PrimaryColor,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = config.welcomeMessage,
                        fontFamily = AppTitleFont,
                        fontSize = (12 * config.fontSizeModifier).sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    
                    Text(
                        text = "أو اختر سؤالاً فوريّاً لبدء المحادثة السريعة:",
                        fontFamily = AppTitleFont,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Easy select prompt grid buttons
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        quickPrompts.forEach { q ->
                            Button(
                                onClick = {
                                    userTextInput = q.substring(0, q.length - 2) // remove emoji
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor.copy(alpha = 0.08f), contentColor = PrimaryColor),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(q, fontFamily = AppTitleFont, fontSize = 11.sp)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { msg ->
                        val isUser = msg.sender == "user"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Card(
                                shape = RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isUser) 12.dp else 0.dp,
                                    bottomEnd = if (isUser) 0.dp else 12.dp
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isUser) PrimaryColor else Color.White
                                ),
                                border = if (!isUser) BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)) else null,
                                modifier = Modifier
                                    .widthIn(max = 280.dp)
                                    .shadow(1.dp, RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = msg.message,
                                        color = if (isUser) Color.White else Color.Black,
                                        fontFamily = AppTitleFont,
                                        fontSize = (12.5 * config.fontSizeModifier).sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // In-progress AI thinking loader
            if (isChatLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .align(Alignment.BottomCenter),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = PrimaryColor, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("أبو يمن يكتب الرد الآن... ⚡", fontFamily = AppTitleFont, fontSize = 11.sp, color = PrimaryColor)
                        }
                    }
                }
            }
        }

        // Typing inputs bar
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = userTextInput,
                    onValueChange = { userTextInput = it },
                    placeholder = { Text("اطرح مشكلتك، سأقوم بحلها فورا معك...", fontFamily = AppTitleFont, fontSize = 11.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    maxLines = 3,
                    singleLine = false,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                
                Spacer(modifier = Modifier.width(8.dp))

                FloatingActionButton(
                    onClick = {
                        if (userTextInput.trim().isNotEmpty()) {
                            viewModel.sendChatMessage(userTextInput.trim())
                            userTextInput = ""
                        }
                    },
                    containerColor = PrimaryColor,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "إرسال", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// ==========================================
// SCREEN 3: CRAFTSMAN REGISTRATION APPLICATION
// ==========================================
@Composable
fun RegistrationScreen(viewModel: AppViewModel, config: AdminConfig) {
    val context = LocalContext.current

    // Fields values
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("صنعاء") }
    var selectedMainCategory by remember { mutableStateOf("كهرباء وإلكترونيات") }
    var selectedSubCategory by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("ذكر") }
    var description by remember { mutableStateOf("") }
    
    // Pictures choosing indicators
    var profilePhotoSource by remember { mutableStateOf<String?>(null) }
    var idPhotoSource by remember { mutableStateOf<String?>(null) }

    var isAgreedToConditions by remember { mutableStateOf(false) }

    // Dialog pickers
    var showProfilePickerModal by remember { mutableStateOf(false) }
    var showIdPickerModal by remember { mutableStateOf(false) }

    val categoriesList = listOf("كهرباء وإلكترونيات", "سباكة وصحي", "نجارة وديكور", "تكييف وتبريد", "حدادة وألومنيوم", "خياطة وتفصيل", "أخرى")
    val citiesList = listOf("صنعاء", "عدن", "تعز", "الحديدة", "حضرموت", "إب", "ذمار")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "انضم إلينا كفني أو مهني ممتاز 🤝",
                    fontFamily = AppTitleFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = (17 * config.fontSizeModifier).sp,
                    color = PrimaryColor,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "سجل تفاصيل كفاءتك المهنية لتظهر للعملاء في محافظتك وتصلك الطلبات مباشرة للمحمول دون تعقيد.",
                    fontFamily = AppTitleFont,
                    fontSize = (11 * config.fontSizeModifier).sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Registry Form Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("📝 تعبئة بيانات الحساب", fontFamily = AppTitleFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryColor)

                    // Profile Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("الاسم الكامل فني/مهني (مثال: م. علي صالح)", fontFamily = AppTitleFont, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryColor)
                    )

                    // Phone contacts (Yemeni dialing numbers: 77/73/71...)
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم جوال الاتصال المباشر (تسعة أرقام)", fontFamily = AppTitleFont, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryColor)
                    )

                    // WhatsApp contact info
                    OutlinedTextField(
                        value = whatsapp,
                        onValueChange = { whatsapp = it },
                        label = { Text("رقم واتساب المباشر (اختياري - بدون صفار أولى ومفتاح)", fontFamily = AppTitleFont, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryColor)
                    )

                    // Categories Selection Selector
                    Column {
                        Text("الحرفة الرئيسية بالتصنيف:", fontFamily = AppTitleFont, fontSize = 10.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(categoriesList) { cat ->
                                FilterChip(
                                    selected = selectedMainCategory == cat,
                                    onClick = { selectedMainCategory = cat },
                                    label = { Text(cat, fontFamily = AppTitleFont, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryColor,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    // Specialty Detail Input
                    OutlinedTextField(
                        value = selectedSubCategory,
                        onValueChange = { selectedSubCategory = it },
                        label = { Text("التخصص الدقيق (مثال: تركيب لوحات طاقة منزلية)", fontFamily = AppTitleFont, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryColor)
                    )

                    // Yemeni Governate dropdown chooser (as row select)
                    Column {
                        Text("محافظة العمل الأساسية باليمن:", fontFamily = AppTitleFont, fontSize = 10.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(citiesList) { ct ->
                                FilterChip(
                                    selected = city == ct,
                                    onClick = { city = ct },
                                    label = { Text(ct, fontFamily = AppTitleFont, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryColor,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    // Detail Skills description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("وصف مختصر لمهاراتك والعدة التي تمتلكها وضمانتك للعمل...", fontFamily = AppTitleFont, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryColor)
                    )

                    // Gender chooser options
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("نوع الجنس:", fontFamily = AppTitleFont, fontSize = 11.sp, modifier = Modifier.weight(0.5f))
                        Row(modifier = Modifier.weight(1.5f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { gender = "ذكر" }) {
                                RadioButton(selected = gender == "ذكر", onClick = { gender = "ذكر" })
                                Text("ذكر 🚹", fontFamily = AppTitleFont, fontSize = 11.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { gender = "أنثى" }) {
                                RadioButton(selected = gender == "أنثى", onClick = { gender = "أنثى" })
                                Text("أنثى 🚺", fontFamily = AppTitleFont, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Camera uploads Mocking block
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "التقاط الوثائق والصور",
                    tint = PrimaryColor,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "تحميل المستندات والصور الشخصية والمهنية",
                    fontFamily = AppTitleFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = PrimaryColor
                )
                Text(
                    text = "الملفات والبطائق الشخصية تجعل حسابك موثقاً وتزيد فرصة تواصل الزبائن معك بصفتك مهني قانوني.",
                    fontFamily = AppTitleFont,
                    fontSize = 10.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Indicator displays
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("صورة البروفايل:", fontFamily = AppTitleFont, fontSize = 9.sp, color = Color.Gray)
                        Text(
                            text = profilePhotoSource ?: "لم يتم الاختيار ❌",
                            fontFamily = AppTitleFont,
                            fontSize = 10.sp,
                            color = if (profilePhotoSource != null) PrimaryColor else DeepCoral,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("رابط الترخيص المهني / الجواز:", fontFamily = AppTitleFont, fontSize = 9.sp, color = Color.Gray)
                        Text(
                            text = idPhotoSource ?: "لم يتم الاختيار ❌",
                            fontFamily = AppTitleFont,
                            fontSize = 10.sp,
                            color = if (idPhotoSource != null) PrimaryColor else DeepCoral,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showProfilePickerModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor.copy(alpha = 0.15f), contentColor = PrimaryColor),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("صورة البروفايل", fontFamily = AppTitleFont, fontSize = 11.sp)
                    }
                    Button(
                        onClick = { showIdPickerModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor.copy(alpha = 0.15f), contentColor = PrimaryColor),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("كرت المهنة/الهوية", fontFamily = AppTitleFont, fontSize = 11.sp)
                    }
                }
            }
        }

        // Registration Guidelines & Conditions print layout
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, PrimaryColor.copy(alpha = 0.3f)),
                colors = CardDefaults.cardColors(containerColor = PrimaryColor.copy(alpha = 0.02f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.MenuBook, contentDescription = "كتاب الشروط والالتزامات", tint = PrimaryColor)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "📜 لائحة شروط الانضمام والالتزامات",
                            fontFamily = AppTitleFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = PrimaryColor
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = config.registrationConditions,
                        fontFamily = AppTitleFont,
                        fontSize = (11 * config.fontSizeModifier).sp,
                        lineHeight = 16.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isAgreedToConditions,
                            onCheckedChange = { isAgreedToConditions = it }
                        )
                        Text(
                            text = "أوافق تماماً على بنود ولائحة الانضمام المذكورة أعلاه وأقر بصحتها.",
                            fontFamily = AppTitleFont,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Submit Button Action
        item {
            Button(
                onClick = {
                    if (name.isNotEmpty() && phone.isNotEmpty()) {
                        // Generate mock URLs for pictures as proof of simulation
                        val finalPhoto = if (profilePhotoSource != null) "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=400" else null
                        val finalId = if (idPhotoSource != null) "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=400" else null

                        viewModel.submitPendingProvider(
                            name = name.trim(),
                            mainCategory = selectedMainCategory,
                            subCategory = selectedSubCategory.ifEmpty { "فني حرفي" },
                            city = city,
                            phone = phone.trim(),
                            whatsapp = whatsapp.trim(),
                            gender = gender,
                            description = description.trim(),
                            photoUri = finalPhoto,
                            idPhotoUri = finalId
                        )
                        Toast.makeText(
                            context,
                            "تم تقديم طلبك بنجاح للتوثيق الآلي! يرجى الانتظار لحين مراجعته من المشرفين والمصادقة عليه للتفعيل ⚡.",
                            Toast.LENGTH_LONG
                        ).show()
                        
                        // Reset forms
                        name = ""
                        phone = ""
                        whatsapp = ""
                        selectedSubCategory = ""
                        description = ""
                        profilePhotoSource = null
                        idPhotoSource = null
                        isAgreedToConditions = false

                        viewModel.navigateTo("home")
                    } else {
                        Toast.makeText(context, "الرجاء تعبئة حقل الاسم والجوال للتقديم!", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = isAgreedToConditions,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("submit_registration_button"),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("إرسال طلب الانضمام والمصادقة 🇾🇪", fontFamily = AppTitleFont, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }

    // Capture Profile modal chooser dialer
    if (showProfilePickerModal) {
        AlertDialog(
            onDismissRequest = { showProfilePickerModal = false },
            title = { Text("صورة فوتوغرافية شخصية 📸", fontFamily = AppTitleFont, fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = { Text("اختر مصدر التقاط صورة الفني الشخصية لرفعها للملف:", fontFamily = AppTitleFont, fontSize = 11.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        profilePhotoSource = "كاميرا الهاتف (صورة شخصية) - مَمْحيّة الحجم 📸"
                        showProfilePickerModal = false
                        Toast.makeText(context, "تم التقاط وصورة البروفايل بأبعاد مناسبة!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("كاميرا الهاتف", fontFamily = AppTitleFont, color = Color.White, fontSize = 11.sp)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        profilePhotoSource = "معرض الصور (بروفايل المهنة) - مضغوطة التخزين 🖼️"
                        showProfilePickerModal = false
                        Toast.makeText(context, "تم استيراد وضغط الصورة!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("ألبوم الصور", fontFamily = AppTitleFont, color = Color.White, fontSize = 11.sp)
                }
            }
        )
    }

    // Capture License ID modal chooser dialer
    if (showIdPickerModal) {
        AlertDialog(
            onDismissRequest = { showIdPickerModal = false },
            title = { Text("كرت المهنة أو بطاقة الهوية 🗃️", fontFamily = AppTitleFont, fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = { Text("اختر مصدر التقاط ترخيص المهنة أو كرت التصنيف لزيادة مصداقية الحساب:", fontFamily = AppTitleFont, fontSize = 11.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        idPhotoSource = "تأكيد فوري عبر كاميرا المستندات 📸"
                        showIdPickerModal = false
                        Toast.makeText(context, "تم مسح الترخيص وحفظه بشكل مؤمن!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("كاميرا المستندات", fontFamily = AppTitleFont, color = Color.White, fontSize = 11.sp)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        idPhotoSource = "استيراد ترخيص PDF معتمد 📁"
                        showIdPickerModal = false
                        Toast.makeText(context, "تم استيراد المستند بنجاح!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("الملفات المحلية", fontFamily = AppTitleFont, color = Color.White, fontSize = 11.sp)
                }
            }
        )
    }
}

// ==========================================
// SCREEN 4: BOOKMARKED OFF-LINE DIRECTORY INDEX
// ==========================================
@Composable
fun BookmarkedScreen(viewModel: AppViewModel, config: AdminConfig) {
    val bookmarkedProviders by viewModel.bookmarkedProviders.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryColor)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "⭐ المفضلة والفنيين المحفوظين",
                    fontFamily = AppTitleFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = (16 * config.fontSizeModifier).sp,
                    color = Color.White
                )
                Text(
                    text = "جميع الأرقام والتفاصيل محفوظة في ذاكرة هاتفك للاتصال السريع دون اتصال بالإنترنت في أي وقت.",
                    fontFamily = AppTitleFont,
                    fontSize = (10 * config.fontSizeModifier).sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }

        if (bookmarkedProviders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(imageVector = Icons.Default.BookmarkBorder, contentDescription = "لا توجد محفوظات", modifier = Modifier.size(60.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "لا يوجد أي فني محفوظ حالياً 🥺",
                        fontFamily = AppTitleFont,
                        fontSize = (14 * config.fontSizeModifier).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "تصفح الفنيين في الصفحة الرئيسية لخدمات اليمن المذكورة واضغط علامة النجمة/التثبيت لحفظهم هنا للوضعية غير المتصلة بالإنترنت.",
                        fontFamily = AppTitleFont,
                        fontSize = (11 * config.fontSizeModifier).sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(bookmarkedProviders) { provider ->
                    TechnicianCard(provider, viewModel, config)
                }
            }
        }
    }
}


// =========================================================
// SCREEN 5: SECRET MANAGER PORTAL & ACCOUNT VETTING INBOX
// =========================================================
@Composable
fun AdminScreen(viewModel: AppViewModel, config: AdminConfig) {
    val isAuthenticated by viewModel.isAdminAuthenticated.collectAsStateWithLifecycle()
    val pendingList by viewModel.pendingProviders.collectAsStateWithLifecycle()
    val fullList by viewModel.approvedProviders.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var pinInput by remember { mutableStateOf("") }
    var activeAdminTab by remember { mutableStateOf("vetting") } // "vetting", "config"

    // If not authenticated, prompt secure PIN lock
    if (!isAuthenticated) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AdminVelvetBg),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillModifier()
                    .padding(24.dp)
                    .widthIn(max = 350.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "قفل الإدارة",
                        tint = GoldenAccent,
                        modifier = Modifier.size(54.dp)
                    )

                    Text(
                        text = "البوابة السرية لمشرفي النظام 🛡️",
                        fontFamily = AppTitleFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = (14 * config.fontSizeModifier).sp,
                        color = PrimaryColor,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "أدخل رمز المرور السري الخاص بالتحكم في الإعدادات، المصادقة على الحسابات، وصلاحيات المراجعة (الرمز الافتراضي: 9999):",
                        fontFamily = AppTitleFont,
                        fontSize = (11 * config.fontSizeModifier).sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { pinInput = it },
                        label = { Text("رمز الدخول السري (PIN)", fontFamily = AppTitleFont, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("admin_pin_field"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryColor)
                    )

                    Button(
                        onClick = {
                            val success = viewModel.authenticateAdmin(pinInput.trim())
                            if (success) {
                                pinInput = ""
                                Toast.makeText(context, "تم التحقق السري بنجاح! مرحباً بالمدير 🇸🇦🇾🇪", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "الرمز السري غير صحيح ❌ (جرب 9999)", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("فتح لوحة التحكم الإدارية", fontFamily = AppTitleFont, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    TextButton(onClick = { viewModel.navigateTo("home") }) {
                        Text("العودة للتطبيق العادي 🏃", fontFamily = AppTitleFont, color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }
        }
    } else {
        // Authenticated Admin Panel Control Dashboard
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AdminVelvetBg)
        ) {
            // Header stats & Logout
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryColor)
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(
                            text = "لوحة تحكم مشرفي خدمات اليمن 🇾🇪",
                            fontFamily = AppTitleFont,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = (14 * config.fontSizeModifier).sp
                        )
                        Text(
                            text = "المصادقة على الفنيين وضبط معدلات التسعير والتحكم العام",
                            fontFamily = AppTitleFont,
                            fontSize = (9.5 * config.fontSizeModifier).sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { viewModel.logOutAdmin() }) {
                        Icon(Icons.Default.PowerSettingsNew, contentDescription = "خروج", tint = Color.White)
                    }
                }
            }

            // Inline admin sub-navigation tabs
            Row(modifier = Modifier.fillMaxWidth().background(Color.White)) {
                Button(
                    onClick = { activeAdminTab = "vetting" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeAdminTab == "vetting") PrimaryColor else Color.White,
                        contentColor = if (activeAdminTab == "vetting") Color.White else Color.Black
                    ),
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Text("طلبات الحرفيين (${pendingList.size})", fontFamily = AppTitleFont, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { activeAdminTab = "config" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeAdminTab == "config") PrimaryColor else Color.White,
                        contentColor = if (activeAdminTab == "config") Color.White else Color.Black
                    ),
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Text("إعدادات التطبيق والتسعير", fontFamily = AppTitleFont, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Tabs Content View
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (activeAdminTab == "vetting") {
                    // Pending join requests Inbox Vetting
                    if (pendingList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                                Icon(imageVector = Icons.Default.Inbox, contentDescription = "لا يوجد", modifier = Modifier.size(48.dp), tint = GoldenAccent)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "صندوق طلبات التوثيق خالي تماماً 👍",
                                    fontFamily = AppTitleFont,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "لا توجد أي حسابات لمقدمي الخدمة بانتظار المراجعة حالياً.",
                                    fontFamily = AppTitleFont,
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(pendingList) { pending ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(10.dp))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "👤 طلب انضمام فني جديد:",
                                            fontFamily = AppTitleFont,
                                            color = PrimaryColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("الاسم: ${pending.name}", fontFamily = AppTitleFont, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("الهاتف: ${pending.phone} | الواتساب: ${pending.whatsapp}", fontFamily = AppTitleFont, fontSize = 11.sp)
                                        Text("الحرفة: ${pending.mainCategory} • ${pending.subCategory}", fontFamily = AppTitleFont, fontSize = 11.sp, color = Color.Gray)
                                        Text("المحافظة: ${pending.city} | الجنس: ${pending.gender}", fontFamily = AppTitleFont, fontSize = 11.sp)
                                        
                                        if (pending.description.isNotEmpty()) {
                                            Text(
                                                text = "الوصف: ${pending.description}",
                                                fontFamily = AppTitleFont,
                                                fontSize = 11.sp,
                                                color = Color.DarkGray,
                                                modifier = Modifier.padding(vertical = 4.dp).background(SlateBackground).padding(6.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Action triggers
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Button(
                                                onClick = {
                                                    viewModel.approveProvider(pending)
                                                    Toast.makeText(context, "تمت المصادقة ونشره للزبائن فورا! 🇾🇪⚡", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                                                modifier = Modifier.weight(1f).height(36.dp),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = "موافقة", modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("مصادقة الحساب", fontFamily = AppTitleFont, fontSize = 11.sp, color = Color.White)
                                            }

                                            Button(
                                                onClick = {
                                                    viewModel.deleteProvider(pending)
                                                    Toast.makeText(context, "تم رفض وحذف الطلب المعلق.", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = DeepCoral),
                                                modifier = Modifier.weight(1f).height(36.dp),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "رفض", modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("رفض وحذف", fontFamily = AppTitleFont, fontSize = 11.sp, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (activeAdminTab == "config") {
                    // Config form adjustments and hourly rating controls
                    var editableConditions by remember(config) { mutableStateOf(config.registrationConditions) }
                    var editableWelcomeMessage by remember(config) { mutableStateOf(config.welcomeMessage) }
                    var editableBaseRate by remember(config) { mutableStateOf(config.baseAppRateHourYER.toString()) }
                    var editableSecretKey by remember(config) { mutableStateOf(config.secretKey) }
                    var editableFontSizeModifier by remember(config) { mutableStateOf(config.fontSizeModifier) }

                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("⚙️ تعديل الإعدادات والأسعار استرشادياً", fontFamily = AppTitleFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryColor)

                                    // Hourly base rate in Yemen Rials
                                    OutlinedTextField(
                                        value = editableBaseRate,
                                        onValueChange = { editableBaseRate = it },
                                        label = { Text("المعدل الاسترشادي لساعة العمل (YER)", fontFamily = AppTitleFont, fontSize = 11.sp) },
                                        modifier = Modifier.fillMaxWidth().testTag("base_rate_input"),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )

                                    // Text registration rules editor
                                    OutlinedTextField(
                                        value = editableConditions,
                                        onValueChange = { editableConditions = it },
                                        label = { Text("لائحة شروط وتعليمات تسجيل المهنيين بالفورم", fontFamily = AppTitleFont, fontSize = 11.sp) },
                                        modifier = Modifier.fillMaxWidth().height(120.dp),
                                        maxLines = 10
                                    )

                                    // Custom welcome greeting text
                                    OutlinedTextField(
                                        value = editableWelcomeMessage,
                                        onValueChange = { editableWelcomeMessage = it },
                                        label = { Text("ترحيب المساعد الذكي اليمني لبدء الدردشة", fontFamily = AppTitleFont, fontSize = 11.sp) },
                                        modifier = Modifier.fillMaxWidth().height(80.dp),
                                        maxLines = 4
                                    )

                                    // Dynamic text scale modifier slider bar
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                            Text("حجم خطوط النظام العام بالتطبيق (معاينة):", fontFamily = AppTitleFont, fontSize = 11.sp)
                                            Text("${(editableFontSizeModifier * 100).toInt()}%", fontFamily = AppTitleFont, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Slider(
                                            value = editableFontSizeModifier,
                                            onValueChange = { editableFontSizeModifier = it },
                                            valueRange = 0.8f..1.4f
                                        )
                                    }

                                    // App Secret Pin
                                    OutlinedTextField(
                                        value = editableSecretKey,
                                        onValueChange = { editableSecretKey = it },
                                        label = { Text("رمز مرور بوابة الإدارة السرية (PIN)", fontFamily = AppTitleFont, fontSize = 11.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = {
                                            val parsedRate = editableBaseRate.toIntOrNull() ?: config.baseAppRateHourYER
                                            val updated = config.copy(
                                                registrationConditions = editableConditions,
                                                welcomeMessage = editableWelcomeMessage,
                                                baseAppRateHourYER = parsedRate,
                                                secretKey = editableSecretKey,
                                                fontSizeModifier = editableFontSizeModifier
                                            )
                                            viewModel.saveAdminConfig(updated)
                                            Toast.makeText(context, "تم حفظ وتحديث الإعدادات والإرشادات بنجاح ⚡📝", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                                        modifier = Modifier.fillMaxWidth().height(42.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("حفظ التغييرات الجديدة فورا", fontFamily = AppTitleFont, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Listing of approved providers with direct remove action
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("🗑️ إبطال تفعيل خدمات الحرفيين المهنيين (${fullList.size})", fontFamily = AppTitleFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryColor)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        fullList.forEach { p ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth().border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(6.dp)).padding(8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(p.name, fontFamily = AppTitleFont, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    Text("${p.mainCategory} • ${p.city}", fontFamily = AppTitleFont, fontSize = 10.sp, color = Color.Gray)
                                                }
                                                IconButton(
                                                    onClick = {
                                                        viewModel.deleteProvider(p)
                                                        Toast.makeText(context, "تم حذف ${p.name} من الدليل بنجاح.", Toast.LENGTH_SHORT).show()
                                                    }
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "حظر المهني", tint = DeepCoral, modifier = Modifier.size(20.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Dynamic layout measuring modifier extension helper ---
fun Modifier.fillModifier(): Modifier = this.fillMaxWidth()
fun Modifier.size(dp: androidx.compose.ui.unit.Dp): Modifier = this.width(dp).height(dp)
