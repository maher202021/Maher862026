package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.local.*
import androidx.compose.ui.text.style.TextOverflow
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigationScaffold(viewModel: AppViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val config by viewModel.adminConfig.collectAsStateWithLifecycle()
    val theme = resolveTheme(config)

    val context = LocalContext.current
    val activity = remember(context) { context as? android.app.Activity }
    var backPressedTime by remember { mutableLongStateOf(0L) }

    BackHandler(enabled = true) {
        if (currentScreen != "home") {
            viewModel.navigateTo("home")
        } else {
            val now = System.currentTimeMillis()
            if (now - backPressedTime < 2000L) {
                activity?.finish()
            } else {
                backPressedTime = now
                Toast.makeText(context, "إضغط مرة أخرى للخروج من التطبيق 🏃", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Keep reference to selected provider for peer chat
    var selectedPeerChatProvider by remember { mutableStateOf<Provider?>(null) }

    Scaffold(
        topBar = {
            TopAppBarRtl(viewModel, config)
        },
        bottomBar = {}, // BOTTOM NAVIGATION COMPLETED DELETED FOR MAXIMUM POLISH & RTL SIMPLICITY
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF1F5FE))
            ) {
                when (currentScreen) {
                    "home" -> DirectoryScreen(viewModel, config) { provider ->
                        selectedPeerChatProvider = provider
                        viewModel.navigateTo("peer_chat")
                    }
                    "chat" -> ChatScreen(viewModel, config)
                    "register" -> RegistrationScreen(viewModel, config)
                    "bookmarks" -> BookmarkedScreen(viewModel, config) { provider ->
                        selectedPeerChatProvider = provider
                        viewModel.navigateTo("peer_chat")
                    }
                    "login" -> AdminScreen(viewModel, config)
                    "backdoor" -> BackdoorSecretScreen(viewModel, config)
                    "peer_chat" -> {
                        val activeProv = selectedPeerChatProvider
                        if (activeProv != null) {
                            PeerChatScreen(viewModel, config, activeProv)
                        } else {
                            viewModel.navigateTo("home")
                        }
                    }
                    else -> DirectoryScreen(viewModel, config) { provider ->
                        selectedPeerChatProvider = provider
                        viewModel.navigateTo("peer_chat")
                    }
                }

                // Sticky Dynamic App Footer - 50% smaller size
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = config.footerOpacity))
                        .navigationBarsPadding()
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Info Button (About) - 50% smaller visual scale
                        IconButton(
                            onClick = { viewModel.navigateTo("bookmarks") },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Bookmark, "Bookmarked", tint = theme.primary, modifier = Modifier.size(16.dp))
                        }

                        // Center: Sponsor Sponsor Advertising Banner
                        Text(
                            text = config.sponsorFooter,
                            fontSize = config.footerFontSize.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.clickable {
                                viewModel.navigateTo("bookmarks") // fallback info or saved listings
                            }
                        )

                        // Right: Floating Smart Assistant Toggle Button Bubble
                        if (config.smartAssistantEnabled) {
                            IconButton(
                                onClick = { viewModel.navigateTo("chat") },
                                modifier = Modifier.size(34.dp).background(theme.primary, CircleShape)
                            ) {
                                Icon(Icons.Default.AutoAwesome, "Assistant", tint = Color.White, modifier = Modifier.size(17.dp))
                            }
                        } else {
                            Spacer(modifier = Modifier.size(34.dp))
                        }
                    }
                }
            }
        }
    )
}

// ==========================================
// SCREEN 1: COMPREHENSIVE DIRECTORY HUB
// ==========================================
@Composable
fun DirectoryScreen(viewModel: AppViewModel, config: AdminConfig, onProviderClick: (Provider) -> Unit) {
    val theme = resolveTheme(config)
    val context = LocalContext.current
    val searchVal by viewModel.searchQuery.collectAsStateWithLifecycle()
    val activeCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val activeCity by viewModel.selectedCity.collectAsStateWithLifecycle()
    val activeGender by viewModel.selectedGender.collectAsStateWithLifecycle()
    val providers by viewModel.approvedProviders.collectAsStateWithLifecycle()

    val categoriesListRaw by viewModel.categories.collectAsStateWithLifecycle()
    val citiesListRaw by viewModel.cities.collectAsStateWithLifecycle()

    val defaultCategories = listOf("الكل", "كهرباء وإلكترونيات", "سباكة وصحي", "نجارة وديكور", "تكييف وتبريد", "حدادة وألومنيوم", "خياطة وتفصيل", "أخرى")
    val defaultCities = listOf("الكل", "صنعاء", "عدن", "تعز", "الحديدة", "حضرموت", "إب", "ذمار")

    val categoriesList = if (categoriesListRaw.isNotEmpty()) {
        listOf("الكل") + categoriesListRaw.filter { it != "الكل" }
    } else {
        defaultCategories
    }
    val citiesList = if (citiesListRaw.isNotEmpty()) {
        listOf("الكل") + citiesListRaw.filter { it != "الكل" }
    } else {
        defaultCities
    }
    val genderList = listOf("الكل", "ذكر", "أنثى")

    // Filter recommended providers for high impact slider
    val recommendedSliderList = providers.filter { it.isRecommended }

    // Banners collection fetched in real-time
    val activeBanners = remember { mutableStateListOf<CustomAdBanner>() }
    LaunchedEffect(Unit) {
        try {
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            firestore.collection("banners")
                .addSnapshotListener { snapshot, error ->
                    if (snapshot != null) {
                        val loaded = mutableListOf<CustomAdBanner>()
                        for (doc in snapshot.documents) {
                            try {
                                val idVal = (doc.getLong("id") ?: 0L).toInt()
                                val titleStr = doc.getString("title") ?: ""
                                val mediaTypeStr = doc.getString("mediaType") ?: "صورة"
                                val sectionStr = doc.getString("section") ?: "الرئيسية"
                                val sizeStr = doc.getString("size") ?: "عريض L"
                                val durationInt = (doc.getLong("durationSeconds") ?: 10L).toInt()
                                val isActiveVal = doc.getBoolean("isActive") ?: true
                                
                                if (titleStr.isNotEmpty() && isActiveVal) {
                                    loaded.add(CustomAdBanner(idVal, titleStr, mediaTypeStr, sectionStr, sizeStr, durationInt, isActiveVal))
                                }
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                            }
                        }
                        if (loaded.isNotEmpty()) {
                            activeBanners.clear()
                            activeBanners.addAll(loaded)
                        }
                    }
                }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(bottom = 36.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(theme.primary.copy(alpha = 0.95f), theme.primary.copy(alpha = 0.15f)),
                        startY = 0f,
                        endY = 300f
                    )
                )
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "دليل المهن والخدمات الشامل 🇾🇪",
                    fontFamily = AppMainFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = (16 * config.fontSizeModifier).sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "تواصل مباشرة ومجاناً مع مئات الفنيين والحرفيين بدون إنترنت!",
                    fontFamily = AppMainFont,
                    fontSize = (11 * config.fontSizeModifier).sp,
                    color = Color.White.copy(alpha = 0.9f)
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
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // Search box
                OutlinedTextField(
                    value = searchVal,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("ابحث باسم الفني أو تخصص الحرفة...", fontFamily = AppMainFont, fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث", tint = theme.primary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_field"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.primary,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Category filters row
                Text("تخصص المهنة:", fontFamily = AppMainFont, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categoriesList) { cat ->
                        FilterChip(
                            selected = activeCategory == cat,
                            onClick = { viewModel.updateSelectedCategory(cat) },
                            label = { Text(cat, fontFamily = AppMainFont, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = theme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("المحافظة:", fontFamily = AppMainFont, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.primary)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(citiesList) { city ->
                                FilterChip(
                                    selected = activeCity == city,
                                    onClick = { viewModel.updateSelectedCity(city) },
                                    label = { Text(city, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = theme.primary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(0.4f)) {
                        Text("الجنس:", fontFamily = AppMainFont, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.primary)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(genderList) { g ->
                                FilterChip(
                                    selected = activeGender == g,
                                    onClick = { viewModel.updateSelectedGender(g) },
                                    label = { Text(g, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = theme.primary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- REAL-TIME LIVE SPONSOR / SYSTEM BANNERS SLIDER ---
        if (activeBanners.isNotEmpty()) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                Text("📢 إعلانات وخدمات حصرية عاجلة:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = theme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(activeBanners) { banner ->
                        Card(
                            modifier = Modifier
                                .width(300.dp)
                                .height(80.dp)
                                .clickable {
                                    Toast.makeText(context, "الخدمات الدعائية: ${banner.title}", Toast.LENGTH_SHORT).show()
                                },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = theme.primary.copy(alpha = 0.08f)),
                            border = BorderStroke(1.dp, theme.primary.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Dynamic Content Icon based on Media Type
                                Box(
                                    modifier = Modifier
                                        .size(45.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(theme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val icon = when (banner.mediaType) {
                                        "صورة" -> "🌄"
                                        "فيديو" -> "🎥"
                                        else -> "📝"
                                    }
                                    Text(icon, fontSize = 20.sp)
                                }
                                
                                Spacer(modifier = Modifier.width(10.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = banner.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.Black,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Badge(containerColor = theme.secondary) {
                                            Text(banner.size, fontSize = 8.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("سلايدر ${banner.section}", fontSize = 9.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // VIP Slider Promotion Campaign (Banners Manager)
        if (recommendedSliderList.isNotEmpty() && activeCategory == "الكل" && searchVal.isEmpty()) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)) {
                Text("🔥 فنيين متميزين وموصى بهم بقوة:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = theme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(recommendedSliderList) { recProv ->
                        Card(
                            modifier = Modifier
                                .width(220.dp)
                                .clickable { onProviderClick(recProv) }
                                .border(1.5.dp, theme.secondary, RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = theme.primary.copy(alpha = 0.05f))
                        ) {
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(40.dp)) {
                                    if (recProv.photoUri != null) {
                                        AsyncImage(
                                            model = recProv.photoUri,
                                            contentDescription = "Slide Pic",
                                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(modifier = Modifier.fillMaxSize().background(theme.primary, CircleShape), contentAlignment = Alignment.Center) {
                                            Text(recProv.name.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(recProv.name, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                                    Text(recProv.subCategory, fontSize = 9.sp, color = Color.Gray, maxLines = 1)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, "Star", tint = theme.secondary, modifier = Modifier.size(10.dp))
                                        Text("${recProv.rating} • موثق", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
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
                        text = "عذراً يا غالي! لم نعثر على نتائج مأهولة 🔍",
                        fontFamily = AppMainFont,
                        fontSize = (13 * config.fontSizeModifier).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
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
                    TechnicianCard(provider, viewModel, config, onProviderClick)
                }
            }
        }
    }
}

// ==========================================
// COMPONENT: HIGHLY POLISHED TECHNICIAN CARD
// ==========================================
@Composable
fun TechnicianCard(provider: Provider, viewModel: AppViewModel, config: AdminConfig, onProviderClick: (Provider) -> Unit) {
    val context = LocalContext.current
    val theme = resolveTheme(config)

    var showReviewDialog by remember { mutableStateOf(false) }
    var reviewStarsSelected by remember { mutableStateOf(5f) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .shadow(1.5.dp, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.size(50.dp)) {
                    if (provider.photoUri != null) {
                        AsyncImage(
                            model = provider.photoUri,
                            contentDescription = "صورة الفني",
                            modifier = Modifier.fillMaxSize().clip(CircleShape).border(1.5.dp, theme.primary, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(theme.primary.copy(alpha = 0.1f))
                                .border(1.5.dp, theme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(provider.name.take(1), color = theme.primary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                    // Gender badge bottom right
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(if (provider.gender == "ذكر") Color(0xFF64B5F6) else Color(0xFFF06292)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (provider.gender == "ذكر") "🚹" else "🚺", fontSize = 8.sp)
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = provider.name,
                            fontFamily = AppMainFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = (13 * config.fontSizeModifier).sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        if (provider.isVerified) {
                            Icon(Icons.Default.Verified, "Verified", tint = theme.secondary, modifier = Modifier.size(15.dp))
                        }
                        if (provider.isPinned) {
                            Icon(Icons.Default.PushPin, "Pinned", tint = Color.Blue, modifier = Modifier.size(14.dp))
                        }
                    }

                    Text(
                        text = "${provider.mainCategory} • ${provider.subCategory}",
                        fontFamily = AppMainFont,
                        fontSize = (11 * config.fontSizeModifier).sp,
                        color = Color.Gray
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, "Loc", tint = theme.primary, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("محافظة ${provider.city} • النقاط: ${provider.points} pts", fontSize = 10.sp, color = Color.DarkGray)
                    }
                }

                // Bookmark trigger
                IconButton(onClick = { viewModel.toggleBookmark(provider) }) {
                    Icon(
                        imageVector = if (provider.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Save",
                        tint = if (provider.isBookmarked) theme.secondary else Color.Gray
                    )
                }
            }

            if (provider.description.isNotEmpty()) {
                Text(
                    text = provider.description,
                    fontSize = 11.sp,
                    color = Color.DarkGray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(Color(0xFFF1F5FE))
                        .padding(6.dp)
                )
            }

            // Star Rating Display Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth().clickable { showReviewDialog = true }
            ) {
                Icon(Icons.Default.Star, "rating", tint = theme.secondary, modifier = Modifier.size(16.dp))
                Text(
                    text = "${String.format("%.1f", provider.rating)} (${provider.votes} تقييم) • تفاصيل المراجعات ★",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.primary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text("نقاط الفني: ${provider.points} 🏆", fontSize = 10.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Phone Dialer & Whatsapp Action row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Dialer Button
                Button(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${provider.phone}")))
                    },
                    modifier = Modifier.weight(1f).height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(2.dp)
                ) {
                    Icon(Icons.Default.Call, "call", tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("اتصال جوال", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                // WhatsApp direct trigger with auto point rewards
                Button(
                    onClick = {
                        viewModel.shareProvider(provider) // Share yields +20 loyalty points
                        val greet = Uri.encode("السلام عليكم يا فني، تواصلت معك عن طريق 'كل خدمات اليمن' بخصوص عمل صيانة...")
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=967${provider.whatsapp}&text=$greet")))
                    },
                    modifier = Modifier.weight(1f).height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(2.dp)
                ) {
                    Icon(Icons.Default.Share, "whatsapp", tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("واتساب مباشر", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                // P2P Chat Button inside App
                Button(
                    onClick = { onProviderClick(provider) },
                    modifier = Modifier.weight(1f).height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.secondary),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(2.dp)
                ) {
                    Icon(Icons.Default.Chat, "app chat", tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("دردشة فورية", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Modal dialogue box to input dynamic reviews & score loyalty points
    if (showReviewDialog) {
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text("أضف مراجعة وتقييم فني ★", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("اختر تقييم النجوم لتقييم الحرفي، وسوف يتم كسبك +15 نقطة ولاء إضافية له فوراً لحفظ ترويجه:", fontSize = 11.sp)
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        (1..5).forEach { i ->
                            val starCol = if (i <= reviewStarsSelected) theme.secondary else Color.LightGray
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Reviews star",
                                tint = starCol,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable { reviewStarsSelected = i.toFloat() }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                    onClick = {
                        viewModel.submitReview(provider, reviewStarsSelected)
                        showReviewDialog = false
                        Toast.makeText(context, "تم رفع تقييمك بنظام النجوم وتحصيل المكافأة بنجاح! 🏆★", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("إرسال التقييم", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false }) { Text("إلغاء") }
            }
        )
    }
}

// ==========================================
// SCREEN 2: INTELLIGENT AI GEMINI ASSISTANT
// ==========================================
@Composable
fun ChatScreen(viewModel: AppViewModel, config: AdminConfig) {
    val theme = resolveTheme(config)
    val context = LocalContext.current
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isChatLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()

    var userTextVal by remember { mutableStateOf("") }
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }

    // Initialize TTS
    DisposableEffect(Unit) {
        ttsEngine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsEngine?.language = Locale("ar")
            }
        }
        onDispose {
            ttsEngine?.stop()
            ttsEngine?.shutdown()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(bottom = 36.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.primary)
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("🤖 أبو يمن - مبرمج ومساعد الحرف والبيوت", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Text("شات تقني سريع وموثوق • يعمل بمفتاح Gemini السحابي والـ Offline", color = Color.White.copy(alpha = 0.85f), fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { viewModel.clearChatMessages() }) {
                    Icon(Icons.Default.DeleteSweep, "مسح", tint = Color.White)
                }
            }
        }

        // Help banners
        if (messages.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.QuestionAnswer, "Prompt", tint = theme.secondary, modifier = Modifier.size(54.dp))
                    Text(
                        text = config.welcomeMessage,
                        fontSize = 12.5.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 18.sp
                    )
                }
            }
        } else {
            // Conversational bubble views
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFFECE5DD))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { msg ->
                    val isUser = msg.sender == "user"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUser) Color(0xFFDCF8C6) else Color.White
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(msg.message, fontSize = 13.sp, color = Color.Black)
                                if (!isUser) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    // Audio text readout support for accessibility
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable {
                                            ttsEngine?.speak(msg.message, TextToSpeech.QUEUE_FLUSH, null, null)
                                        }
                                    ) {
                                        Icon(Icons.Default.VolumeUp, "Speak out loud", tint = theme.primary, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("قراءة الإجابة صوتياً 🔈", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = theme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Loading Progress Indicator spinner
        if (isChatLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = theme.secondary)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = userTextVal,
                onValueChange = { userTextVal = it },
                placeholder = { Text("اطرح استشارة فنية (تصليح مكيف، شبكة طاقة)...") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            IconButton(
                onClick = {
                    val prompt = userTextVal.trim()
                    if (prompt.isNotEmpty()) {
                        viewModel.sendChatMessage(prompt)
                        userTextVal = ""
                    }
                },
                modifier = Modifier.background(theme.primary, CircleShape)
            ) {
                Icon(Icons.Default.Send, "Send prompt", tint = Color.White)
            }
        }
    }
}
