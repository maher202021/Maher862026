package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.example.data.local.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Color parsing helper
fun parseColorHex(hex: String, default: Color): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        default
    }
}

// Intent launching helpers
fun callNumber(context: Context, phone: String) {
    try {
        val cleanPhone = phone.trim().replace(" ", "")
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone"))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "لا يمكن إجراء الاتصال حالياً", Toast.LENGTH_SHORT).show()
    }
}

fun launchWhatsApp(context: Context, phone: String, message: String = "مرحباً فني كل خدمات اليمن، أود الاستفسار عن خدمتك...") {
    try {
        val cleanPhone = phone.trim().replace(" ", "").removePrefix("+")
        // Resolve Yemeni local numbers starting with 7
        val formattedPhone = if (cleanPhone.startsWith("7") && cleanPhone.length == 9) "967$cleanPhone" else cleanPhone
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=${Uri.encode(message)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "الواتساب غير مثبت على الجهاز", Toast.LENGTH_SHORT).show()
    }
}

fun launchMapDirections(context: Context, locationName: String, lat: Double, lon: Double) {
    try {
        val uri = Uri.parse("geo:$lat,$lon?q=${Uri.encode(locationName)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "خرائط Google غير متوفرة", Toast.LENGTH_SHORT).show()
    }
}

fun shareApp(context: Context, link: String) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "تطبيق كل خدمات اليمن")
            putExtra(Intent.EXTRA_TEXT, "حمل الآن تطبيق كل خدمات اليمن - الدليل الشامل لجميع المهن والخدمات: $link")
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة التطبيق عبر"))
    } catch (e: Exception) {
        Toast.makeText(context, "فشلت مشاركة التطبيق", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun AppNavigation(viewModel: AppViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val configOrNull by viewModel.config.collectAsState()
    val config = configOrNull ?: AppConfigEntity()

    // Back Button Handling: Double-tap to exit on Home screen, or return to Home from pages
    val context = LocalContext.current
    var backPressedTime by remember { mutableStateOf(0L) }

    BackHandler {
        if (currentScreen != "home") {
            viewModel.navigateTo("home")
        } else {
            val currentTime = System.currentTimeMillis()
            if (currentTime - backPressedTime < 2000) {
                // Exit app
                (context as? android.app.Activity)?.finish()
            } else {
                backPressedTime = currentTime
                Toast.makeText(context, "اضغط مرة أخرى للخروج من التطبيق", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Dynamic coloring based on reactive database settings
    val primaryColor = parseColorHex(config.primaryColorHex, Color(0xFF0D9488))
    val secondaryColor = parseColorHex(config.secondaryColorHex, Color(0xFFF59E0B))

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // Enforce Arabic RTL layout across all screens as mandated
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Top Custom App Bar
                    AppTopBar(
                        viewModel = viewModel,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor
                    )

                    // Marquee Moving Banner
                    MarqueeBanner(
                        text = config.marqueeText,
                        secondaryColor = secondaryColor
                    )

                    // Active screen selection
                    Box(modifier = Modifier.weight(1f)) {
                        when (currentScreen) {
                            "home" -> HomeScreen(viewModel = viewModel, primaryColor = primaryColor, secondaryColor = secondaryColor)
                            "register" -> ProviderRegistrationScreen(viewModel = viewModel, primaryColor = primaryColor)
                            "secret_backdoor" -> SecretBackdoorScreen(viewModel = viewModel, primaryColor = primaryColor, secondaryColor = secondaryColor)
                            "admin" -> AdminDashboardScreen(viewModel = viewModel, primaryColor = primaryColor, secondaryColor = secondaryColor)
                        }
                    }
                }

                // Global Floating Overlays
                FloatingWidgetsOverlay(
                    viewModel = viewModel,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    config = config
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    viewModel: AppViewModel,
    primaryColor: Color,
    secondaryColor: Color
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val configOrNull by viewModel.config.collectAsState()
    val config = configOrNull ?: AppConfigEntity()
    var homeTapCount by remember { mutableStateOf(0) }
    val context = LocalContext.current

    TopAppBar(
        title = {
            Text(
                text = config.appName,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                color = Color.White
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = primaryColor),
        navigationIcon = {
            IconButton(
                onClick = {
                    homeTapCount++
                    if (homeTapCount >= 5) {
                        homeTapCount = 0
                        viewModel.navigateTo("secret_backdoor")
                        Toast.makeText(context, "الرجاء إدخال كلمة المرور السرية", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.navigateTo("home")
                    }
                },
                modifier = Modifier.testTag("nav_home_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "الرئيسية",
                    tint = if (currentScreen == "home") secondaryColor else Color.White
                )
            }
        },
        actions = {
            // Register Professional Profile Icon
            IconButton(
                onClick = { viewModel.navigateTo("register") },
                modifier = Modifier.testTag("nav_register_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "تسجيل مهني فني",
                    tint = if (currentScreen == "register") secondaryColor else Color.White
                )
            }

            // Lock backdoor entry button
            IconButton(
                onClick = { viewModel.navigateTo("secret_backdoor") },
                modifier = Modifier.testTag("nav_backdoor_button")
            ) {
                Icon(
                    imageVector = Icons.Default.VpnKey,
                    contentDescription = "الدخول السري",
                    tint = if (currentScreen == "secret_backdoor") secondaryColor else Color.White
                )
            }

            // Language selection (Visual Toast feedback)
            IconButton(
                onClick = {
                    Toast.makeText(context, "تم ضبط اللغة الافتراضية: العربية (RTL)", Toast.LENGTH_SHORT).show()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "اللغات",
                    tint = Color.White
                )
            }

            // Sync database action simulating addSnapshotListener
            IconButton(
                onClick = {
                    Toast.makeText(context, "جاري تحديث ومزامنة البيانات اللحظية...", Toast.LENGTH_SHORT).show()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "مزامنة لحظية",
                    tint = Color.White
                )
            }
        }
    )
}

@Composable
fun MarqueeBanner(text: String, secondaryColor: Color) {
    var xOffset by remember { mutableStateOf(500f) }
    val infiniteTransition = rememberInfiniteTransition(label = "marquee")
    
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 400f,
        targetValue = -1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "marquee_offset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(secondaryColor.copy(alpha = 0.15f))
            .padding(vertical = 6.dp)
            .horizontalScroll(rememberScrollState()),
        contentAlignment = Alignment.CenterEnd
    ) {
        Row(
            modifier = Modifier
                .offset(x = animatedOffset.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Campaign,
                contentDescription = "بوق إعلانات",
                tint = secondaryColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Visible
            )
        }
    }
}

@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    primaryColor: Color,
    secondaryColor: Color
) {
    val context = LocalContext.current
    val categories by viewModel.categories.collectAsState()
    val filteredProviders by viewModel.filteredProviders.collectAsState()
    val banners by viewModel.banners.collectAsState()
    val configOrNull by viewModel.config.collectAsState()
    val config = configOrNull ?: AppConfigEntity()
    val loyaltyOrNull by viewModel.loyaltyPoints.collectAsState()
    val loyalty = loyaltyOrNull ?: LoyaltyPointsEntity()

    var showLoyaltyExchangeDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_lazy_column"),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Dynamic Promos / Ads Display managed by Admin
        val activeBanner = banners.firstOrNull { it.isActive }
        if (activeBanner != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("ad_banner_card")
                        .clickable {
                            if (activeBanner.link.isNotEmpty()) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(activeBanner.link))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "الرابط غير صالح", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Background image using Coil loader
                        Image(
                            painter = rememberAsyncImagePainter(model = activeBanner.contentUrl),
                            contentDescription = "إعلان ممول رئيسي",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Gradient Overlay for readability
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Badge(containerColor = secondaryColor, contentColor = Color.Black) {
                                Text(
                                    text = "إعلان ممول VIP",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = activeBanner.title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (activeBanner.textMessage.isNotEmpty()) {
                                Text(
                                    text = activeBanner.textMessage,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Search & Filter panel with Mic Search
        item {
            SearchBarWithVoice(viewModel = viewModel, config = config, primaryColor = primaryColor, secondaryColor = secondaryColor)
        }

        // 3. VIP Premium & Recommendations
        val vipProviders = filteredProviders.filter { it.isVip || it.isRecommended }
        if (vipProviders.isNotEmpty()) {
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "نخبة VIP والموصى بهم ⭐",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = primaryColor
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(vipProviders) { provider ->
                            ProviderVipCard(
                                provider = provider,
                                viewModel = viewModel,
                                primaryColor = primaryColor,
                                secondaryColor = secondaryColor
                            )
                        }
                    }
                }
            }
        }

        // 4. Circular Quick Categories
        item {
            QuickCategoriesGrid(
                categories = categories,
                viewModel = viewModel,
                primaryColor = primaryColor
            )
        }

        // 5. Loyalty Points Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLoyaltyExchangeDialog = true },
                colors = CardDefaults.cardColors(containerColor = secondaryColor.copy(alpha = 0.12f)),
                border = BorderStroke(1.dp, secondaryColor.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = "بطاقة نقاط ولاء متميزة",
                            tint = secondaryColor,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "محفظة نقاط الولاء اليمنية",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "اضغط لاستبدال نقاطك بخصومات وخدمات مجانية",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }
                    Badge(containerColor = secondaryColor, contentColor = Color.Black) {
                        Text(
                            text = "${loyalty.points} نقطة",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // 6. Providers Directory List Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "دليل الفنيين ومقدمي الخدمات المعتمدين",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = primaryColor
                )
                Text(
                    text = "${filteredProviders.size} فني",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }

        // 7. Providers Cards Directory
        if (filteredProviders.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = "لا توجد نتائج بحث",
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "لا توجد نتائج تطابق خيارات البحث الحالية",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        } else {
            items(filteredProviders) { provider ->
                ProviderItemCard(
                    provider = provider,
                    viewModel = viewModel,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor
                )
            }
        }

        // 8. Dynamic Footer Block managed dynamically by Admin
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_footer_card")
                    .clickable {
                        shareApp(context, config.shareLink)
                    },
                colors = CardDefaults.cardColors(
                    containerColor = parseColorHex(config.footerColor, Color(0xFF1A1A1A)).copy(alpha = config.footerOpacity)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = config.footerText,
                        color = Color.White,
                        fontSize = (12 * config.fontSizeModifier).sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "جيع الحقوق محفوظة للبرمجة وتصميم م. ماهر علوان © 2026",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "مشاركة التطبيق",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "اضغط لمشاركة التطبيق المباشر مع الأصدقاء",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Dialog: Loyalty Points Exchange Detail
    if (showLoyaltyExchangeDialog) {
        Dialog(onDismissRequest = { showLoyaltyExchangeDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = "استبدال نقاط الهدايا",
                        tint = secondaryColor,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "استبدال نقاط الولاء",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = primaryColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "رصيدك الحالي هو: ${loyalty.points} نقطة",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    ListItem(
                        headlineContent = { Text("كوبون خصم بقيمة 2000 ريال", fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("تكلفة الاستبدال: 50 نقطة") },
                        trailingContent = {
                            Button(
                                onClick = {
                                    if (loyalty.points >= 50) {
                                        viewModel.exchangePoints(50, "خصم فوري 2000 ريال")
                                        Toast.makeText(context, "تم استبدال الكوبون بنجاح وجاهز للاستخدام الفوري!", Toast.LENGTH_LONG).show()
                                        showLoyaltyExchangeDialog = false
                                    } else {
                                        Toast.makeText(context, "عذراً، رصيد نقاطك غير كافي", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                            ) {
                                Text("استبدال", color = Color.White)
                            }
                        }
                    )

                    HorizontalDivider()

                    ListItem(
                        headlineContent = { Text("كوبون صيانة مجانية شاملة", fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("تكلفة الاستبدال: 120 نقطة") },
                        trailingContent = {
                            Button(
                                onClick = {
                                    if (loyalty.points >= 120) {
                                        viewModel.exchangePoints(120, "صيانة مجانية شاملة")
                                        Toast.makeText(context, "مبروك! حصلت على كوبون صيانة مجانية تامة للتكييف أو الكهرباء!", Toast.LENGTH_LONG).show()
                                        showLoyaltyExchangeDialog = false
                                    } else {
                                        Toast.makeText(context, "عذراً، رصيد نقاطك غير كافي", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                            ) {
                                Text("استبدال", color = Color.White)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { showLoyaltyExchangeDialog = false }) {
                        Text("إغلاق", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBarWithVoice(
    viewModel: AppViewModel,
    config: AppConfigEntity,
    primaryColor: Color,
    secondaryColor: Color
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var selectedCityFilter by remember { mutableStateOf<String?>(null) }
    var showVoiceAnimationDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier
                .weight(1f)
                .testTag("home_search_bar"),
            placeholder = { Text("ابحث عن فني، خدمة، أو مدينة...", fontSize = 13.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "أيقونة بحث",
                    tint = primaryColor
                )
            },
            trailingIcon = {
                if (config.isSpeechSearchEnabled) {
                    IconButton(
                        onClick = { showVoiceAnimationDialog = true },
                        modifier = Modifier.testTag("voice_search_bar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "بحث صوتي",
                            tint = secondaryColor
                        )
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = primaryColor.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }

    // Direct filters chip flow (Yemeni provinces/cities)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val cities = listOf("الكل", "صنعاء", "عدن", "تعز", "إب", "الحديدة", "حضرموت")
        cities.forEach { city ->
            val isSelected = (city == "الكل" && selectedCityFilter == null) || (selectedCityFilter == city)
            FilterChip(
                selected = isSelected,
                onClick = {
                    selectedCityFilter = if (city == "الكل") null else city
                    viewModel.selectLocation(selectedCityFilter)
                },
                label = { Text(city, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = primaryColor,
                    selectedLabelColor = Color.White
                )
            )
        }
    }

    // Simulated Speech-To-Text Animation Popup to bypass emulator audio hardware issues safely
    if (showVoiceAnimationDialog) {
        var animateMicSize by remember { mutableStateOf(48.dp) }
        var speechStateText by remember { mutableStateOf("جاري الاستماع لصوتك باللغة العربية... 🎙️") }

        LaunchedEffect(key1 = true) {
            // Animate mic icon scale back and forth
            while (speechStateText.startsWith("جاري الاستماع")) {
                animateMicSize = 64.dp
                delay(600)
                animateMicSize = 48.dp
                delay(600)
            }
        }

        Dialog(onDismissRequest = { showVoiceAnimationDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "البحث الصوتي الذكي في اليمن",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = primaryColor
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "استماع نشط للميكروفون",
                        tint = secondaryColor,
                        modifier = Modifier
                            .size(animateMicSize)
                            .background(primaryColor.copy(alpha = 0.15f), CircleShape)
                            .padding(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = speechStateText,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "اختر جملة سريعة للبحث الفوري:",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                    ) {
                        val quickPhrases = listOf("صيانة تكييف", "سباك ممتاز", "كهربائي الستين", "برمجة جوال")
                        quickPhrases.forEach { phrase ->
                            Button(
                                onClick = {
                                    speechStateText = "تم التقاط صوتك: '$phrase'"
                                    scope.launch {
                                        delay(1000)
                                        viewModel.updateSearchQuery(phrase)
                                        showVoiceAnimationDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor.copy(alpha = 0.2f), contentColor = Color.Black)
                            ) {
                                Text(phrase, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { showVoiceAnimationDialog = false }) {
                        Text("إلغاء", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun ProviderVipCard(
    provider: ServiceProviderEntity,
    viewModel: AppViewModel,
    primaryColor: Color,
    secondaryColor: Color
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .width(190.dp)
            .height(150.dp)
            .testTag("vip_provider_card"),
        colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.05f)),
        border = BorderStroke(2.dp, secondaryColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Badge(containerColor = secondaryColor, contentColor = Color.Black) {
                    Text(
                        text = "VIP نخبة",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(2.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "تقييم نخبوي",
                        tint = secondaryColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = provider.rating.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Column {
                Text(
                    text = provider.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${provider.mainCategory} • ${provider.subCategory}",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = provider.location,
                    fontSize = 10.sp,
                    color = primaryColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = {
                        viewModel.triggerCall(provider)
                        callNumber(context, provider.phone)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(30.dp)
                        .background(primaryColor, RoundedCornerShape(4.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "اتصال",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }

                IconButton(
                    onClick = {
                        launchWhatsApp(context, provider.whatsapp)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(30.dp)
                        .background(Color(0xFF25D366), RoundedCornerShape(4.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "واتساب",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickCategoriesGrid(
    categories: List<CategoryEntity>,
    viewModel: AppViewModel,
    primaryColor: Color
) {
    val selectedCat by viewModel.selectedCategory.collectAsState()

    Column {
        Text(
            text = "فئات وأقسام سريعة",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = primaryColor
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // "All" filter chip
            val isAllSelected = selectedCat == null
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { viewModel.selectCategory(null) }
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(
                            if (isAllSelected) primaryColor else primaryColor.copy(alpha = 0.12f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = "الكل",
                        tint = if (isAllSelected) Color.White else primaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "الكل", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            categories.forEach { cat ->
                val isSelected = selectedCat == cat.name
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { viewModel.selectCategory(cat.name) }
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(
                                if (isSelected) primaryColor else primaryColor.copy(alpha = 0.12f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Choose dynamic visual icon representation based on name/metadata
                        val vectorIcon = when (cat.iconName) {
                            "ElectricalServices" -> Icons.Default.ElectricalServices
                            "Plumbing" -> Icons.Default.Plumbing
                            "Handyman" -> Icons.Default.Handyman
                            "AcUnit" -> Icons.Default.AcUnit
                            "PhonelinkSetup" -> Icons.Default.PhonelinkSetup
                            "Computer" -> Icons.Default.Computer
                            "DirectionsCar" -> Icons.Default.DirectionsCar
                            "FlashOn" -> Icons.Default.FlashOn
                            "LocalHospital" -> Icons.Default.LocalHospital
                            "School" -> Icons.Default.School
                            else -> Icons.Default.Build
                        }
                        Icon(
                            imageVector = vectorIcon,
                            contentDescription = cat.name,
                            tint = if (isSelected) Color.White else primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = cat.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun ProviderItemCard(
    provider: ServiceProviderEntity,
    viewModel: AppViewModel,
    primaryColor: Color,
    secondaryColor: Color
) {
    val context = LocalContext.current
    var ratingSliderValue by remember { mutableStateOf(4.5f) }
    var showReviewDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("provider_directory_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Visual row for profile info and badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(primaryColor.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Engineering,
                            contentDescription = "شارة مهني فني",
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = provider.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            if (provider.isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "حساب موثق ومؤهل",
                                    tint = primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = "${provider.mainCategory} • ${provider.subCategory}",
                            color = primaryColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "تقييم النحوم",
                            tint = secondaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = provider.rating.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Text(
                        text = "زيارات: ${provider.visitsCount}",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body text for notes / expertise details
            if (provider.notes.isNotEmpty()) {
                Text(
                    text = provider.notes,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Location & Price Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "موقع العمل والنشاط",
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = provider.location, fontSize = 12.sp, color = Color.Gray)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = "متوسط الأسعار",
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = provider.price, fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(12.dp))

            // Footer actions: Contact buttons (Call, WhatsApp, Maps), Review slide
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.triggerCall(provider)
                        callNumber(context, provider.phone)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = "اتصال فوري", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("اتصال", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Button(
                    onClick = {
                        launchWhatsApp(context, provider.whatsapp)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Icon(imageVector = Icons.Default.Chat, contentDescription = "واتساب فوري", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("واتساب", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Button(
                    onClick = {
                        launchMapDirections(context, provider.name, provider.latitude, provider.longitude)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = secondaryColor),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Icon(imageVector = Icons.Default.Navigation, contentDescription = "الاتجاهات على الخريطة", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إتجاهات", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }

                OutlinedButton(
                    onClick = { showReviewDialog = true },
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(imageVector = Icons.Default.RateReview, contentDescription = "تقييم ومراجعة", tint = primaryColor, modifier = Modifier.size(16.dp))
                }
            }
        }
    }

    // Dialog: Review & Star slider
    if (showReviewDialog) {
        Dialog(onDismissRequest = { showReviewDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "تقييم فني: ${provider.name}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = primaryColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "اختر التقييم للخدمة: ${String.format("%.1f", ratingSliderValue)} ★",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = secondaryColor
                    )
                    Slider(
                        value = ratingSliderValue,
                        onValueChange = { ratingSliderValue = it },
                        valueRange = 1.0f..5.0f,
                        steps = 8
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "الجميل بالخدمة سيحصل على نقاط تقديرية!",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                // Update provider with calculated math for rating average
                                val previousRate = provider.rating
                                val updatedRate = ((previousRate * provider.visitsCount) + ratingSliderValue) / (provider.visitsCount + 1)
                                val finalLimit = Math.min(5.0, Math.max(1.0, updatedRate))
                                viewModel.updateProvider(
                                    provider.copy(
                                        rating = Math.round(finalLimit * 10.0) / 10.0,
                                        visitsCount = provider.visitsCount + 1
                                    )
                                )
                                Toast.makeText(context, "شكراً جزيلاً لتقييمك الصادق ومساهمتك!", Toast.LENGTH_SHORT).show()
                                showReviewDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إرسال التقييم", color = Color.White)
                        }

                        TextButton(
                            onClick = { showReviewDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProviderRegistrationScreen(
    viewModel: AppViewModel,
    primaryColor: Color
) {
    val context = LocalContext.current
    val categories by viewModel.categories.collectAsState()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("صنعاء") }
    var selectedMainCategory by remember { mutableStateOf("صيانة منزلية") }
    var selectedSubCategory by remember { mutableStateOf("كهربائي منازل") }
    var gender by remember { mutableStateOf("مهني ذكور") }

    var mainCategoryExpanded by remember { mutableStateOf(false) }
    var subCategoryExpanded by remember { mutableStateOf(false) }

    // Dropdown options
    val mainGroups = listOf("صيانة منزلية", "برمجيات وتقنية", "سيارات ومحركات", "رعاية طبية", "تعليم وتتدريس")
    val subItemsMap = mapOf(
        "صيانة منزلية" to listOf("كهربائي منازل", "سباك صحي", "نجار وديكور", "مهندس مكيفات"),
        "برمجيات وتقنية" to listOf("صيانة جوالات", "مهندس كمبيوتر", "مبرمج ويب"),
        "سيارات ومحركات" to listOf("ميكانيكي سيارات", "كهربائي سيارات", "إطارات بنشر"),
        "رعاية طبية" to listOf("تمريض منزلي", "علاج طبيعي"),
        "تعليم وتتدريس" to listOf("مدرس منزلي", "معلم لغات")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "💼 استمارة الانضمام والانتداب المهني في اليمن",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = primaryColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "سجل الآن كمقدم خدمات معتمد في التطبيق لعرض بروفايلك لأكثر من ١٠ آلاف باحث عن خدماتك في اليمن بضوابط آمنة وسريعة.",
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("الاسم الكامل (ثلاثي أو رباعي)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("رقم الاتصال المباشر (مثل: 777644670)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true
        )

        OutlinedTextField(
            value = whatsapp,
            onValueChange = { whatsapp = it },
            label = { Text("رقم الواتساب للاستفسارات السريعة") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true
        )

        OutlinedTextField(
            value = city,
            onValueChange = { city = it },
            label = { Text("المدينة والحي (مثال: صنعاء - حدة)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Dropdown dynamic selection
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { mainCategoryExpanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("القسم الرئيسي: $selectedMainCategory", color = primaryColor, fontWeight = FontWeight.Bold)
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "قائمة", tint = primaryColor)
                }
            }
            DropdownMenu(
                expanded = mainCategoryExpanded,
                onDismissRequest = { mainCategoryExpanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                mainGroups.forEach { group ->
                    DropdownMenuItem(
                        text = { Text(group) },
                        onClick = {
                            selectedMainCategory = group
                            selectedSubCategory = subItemsMap[group]?.firstOrNull() ?: ""
                            mainCategoryExpanded = false
                        }
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { subCategoryExpanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("التخصص الفرعي: $selectedSubCategory", color = primaryColor, fontWeight = FontWeight.Bold)
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "تخصص فرعي", tint = primaryColor)
                }
            }
            DropdownMenu(
                expanded = subCategoryExpanded,
                onDismissRequest = { subCategoryExpanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                val subs = subItemsMap[selectedMainCategory] ?: emptyList()
                subs.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            selectedSubCategory = item
                            subCategoryExpanded = false
                        }
                    )
                }
            }
        }

        // Camera upload and direct captures
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                .background(Color.Gray.copy(alpha = 0.02f))
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = "التقاط الوثائق والصور",
                tint = primaryColor,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "تحميل المستندات والصور المهنية",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = "الملفات والبطائق الشخصية اختيارية لإثبات الموثقية والتوثيق للمجتمع.",
                fontSize = 10.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        Toast.makeText(context, "تم التقاط الصورة الشخصية بنجاح 📸", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor.copy(alpha = 0.15f), contentColor = primaryColor)
                ) {
                    Text("التقاط صورة البروفايل", fontSize = 11.sp)
                }
                Button(
                    onClick = {
                        Toast.makeText(context, "تم رفع بطاقة الهوية / الترخيص بنجاح 💾", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor.copy(alpha = 0.15f), contentColor = primaryColor)
                ) {
                    Text("تحميل كرت المهنة", fontSize = 11.sp)
                }
            }
        }

        Button(
            onClick = {
                if (name.isNotEmpty() && phone.isNotEmpty()) {
                    viewModel.submitPendingProvider(
                        name = name,
                        mainCategory = selectedMainCategory,
                        subCategory = selectedSubCategory,
                        city = city,
                        phone = phone,
                        whatsapp = whatsapp,
                        gender = gender,
                        photoUri = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200",
                        idPhotoUri = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=200"
                    )
                    Toast.makeText(context, "تم تقديم طلبك بنجاح! سيقوم فريق المراجعين بمراجعته والمصادقة عليه خلال ساعات.", Toast.LENGTH_LONG).show()
                    viewModel.navigateTo("home")
                } else {
                    Toast.makeText(context, "فضلاً، أكمل تعبئة حقل الاسم والجوال للتقديم", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("submit_registration_button"),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
        ) {
            Text("إرسال طلب الانضمام والتوثيق", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun SecretBackdoorScreen(
    viewModel: AppViewModel,
    primaryColor: Color,
    secondaryColor: Color
) {
    val configOrNull by viewModel.config.collectAsState()
    val config = configOrNull ?: AppConfigEntity()
    val context = LocalContext.current

    var isPasswordMatched by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }
    
    // Remember password dynamically based on preferences
    var rememberBackdoorAuth by remember { mutableStateOf(true) }

    if (!isPasswordMatched) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "بوابة سرية محمية",
                tint = primaryColor,
                modifier = Modifier
                    .size(64.dp)
                    .background(primaryColor.copy(alpha = 0.1f), CircleShape)
                    .padding(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "بوابة المطور والمالك السرية",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = primaryColor
            )
            Text(
                text = "الوصول لهذه الشاشة يتطلب مصادقة الأمان الخاصة بالمالك",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = passwordInput,
                onValueChange = { passwordInput = it },
                label = { Text("كلمة المرور للإدارة") },
                modifier = Modifier.fillMaxWidth().testTag("backdoor_pass_input"),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (passwordInput == config.adminPassword || passwordInput == "maher--736462") {
                        isPasswordMatched = true
                    } else {
                        Toast.makeText(context, "عذراً كلمة المرور خاطئة تماماً!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("backdoor_login_button"),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Text("فتح الرتاج والمصادقة", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    } else {
        // Logged in Backbone Customizer
        var appNameEdit by remember { mutableStateOf(config.appName) }
        var rawMarquee by remember { mutableStateOf(config.marqueeText) }
        var textFooter by remember { mutableStateOf(config.footerText) }
        var supportPhoneEdit by remember { mutableStateOf(config.supportPhone) }
        var primaryColorEdit by remember { mutableStateOf(config.primaryColorHex) }
        var secondaryColorEdit by remember { mutableStateOf(config.secondaryColorHex) }
        var isVoiceSearchEnabled by remember { mutableStateOf(config.isSpeechSearchEnabled) }
        var activePassword by remember { mutableStateOf(config.adminPassword) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = secondaryColor.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🛠️ نظام التحكم الباطني والتهيئة الكونية للواجهات",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = primaryColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "أدوات مخصصة لتحديث هوية التطبيق البصرية والنصية فوراً دون مجهود.",
                            fontSize = 11.sp
                        )
                    }
                    Button(
                        onClick = { viewModel.navigateTo("admin") },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text("لوحة الأدمن 🎛️", color = Color.White, fontSize = 11.sp)
                    }
                }
            }

            OutlinedTextField(
                value = appNameEdit,
                onValueChange = { appNameEdit = it },
                label = { Text("اسم التطبيق في شريط العنوان") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = rawMarquee,
                onValueChange = { rawMarquee = it },
                label = { Text("شريط الترحيب المتحرك (أعلى)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )

            OutlinedTextField(
                value = textFooter,
                onValueChange = { textFooter = it },
                label = { Text("تذييل التطبيق (الفوتر المعروض)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = supportPhoneEdit,
                onValueChange = { supportPhoneEdit = it },
                label = { Text("رقم هاتف الدعم الفني MAW والمطور") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = primaryColorEdit,
                    onValueChange = { primaryColorEdit = it },
                    label = { Text("اللون رئيسي (Hex)") },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = secondaryColorEdit,
                    onValueChange = { secondaryColorEdit = it },
                    label = { Text("اللون ثانوي (Hex)") },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تشغيل خاصية البحث الصوتي الذكي 🎤",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Switch(
                    checked = isVoiceSearchEnabled,
                    onCheckedChange = { isVoiceSearchEnabled = it }
                )
            }

            OutlinedTextField(
                value = activePassword,
                onValueChange = { activePassword = it },
                label = { Text("تحديث كلمة المرور السرية للبوابة") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    val update = config.copy(
                        appName = appNameEdit,
                        marqueeText = rawMarquee,
                        footerText = textFooter,
                        supportPhone = supportPhoneEdit,
                        primaryColorHex = primaryColorEdit,
                        secondaryColorHex = secondaryColorEdit,
                        isSpeechSearchEnabled = isVoiceSearchEnabled,
                        adminPassword = activePassword
                    )
                    viewModel.updateConfig(update)
                    Toast.makeText(context, "تم حفظ البيانات وتطبيق الطابع البصري والهوية الجديدة كلياً! 🎉", Toast.LENGTH_LONG).show()
                    viewModel.navigateTo("home")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_backdoor_settings_button"),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Text("حفظ وتثبيت التعديلات الكونية", color = Color.White, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    isPasswordMatched = false
                    viewModel.navigateTo("home")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("تسجيل الخروج وإغلاق البوابة", color = primaryColor)
            }
        }
    }
}

@Composable
fun AdminDashboardScreen(
    viewModel: AppViewModel,
    primaryColor: Color,
    secondaryColor: Color
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Requests, 1: Add Technicians, 2: Categories, 3: Chats

    val pendingList by viewModel.pendingProviders.collectAsState()
    val providerList by viewModel.providers.collectAsState()
    val categoryList by viewModel.categories.collectAsState()
    val logsList by viewModel.activityLogs.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab Headers in Arabic
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = primaryColor,
            contentColor = Color.White
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("طلبات التسجيل (${pendingList.size})", modifier = Modifier.padding(12.dp), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("دليل الفنيين", modifier = Modifier.padding(12.dp), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                Text("الأقسام", modifier = Modifier.padding(12.dp), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }) {
                Text("السجلات والتقاط الرصد", modifier = Modifier.padding(12.dp), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(12.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // Pending Requests List view with direct visual verification
                    if (pendingList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("لا توجد طلبات انضمام فنيين معلقة حالياً ✅", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(pendingList) { pending ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(text = "طلب تسجيل: ${pending.name}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(text = "تخصص: ${pending.mainCategory} > ${pending.subCategory}", fontSize = 12.sp)
                                        Text(text = "المنطقة: ${pending.location} | جوال: ${pending.phone}", fontSize = 12.sp)
                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Simulating direct visual verification of personal photo and trade license/ID cards
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.fillMaxWidth().height(70.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(6.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("بروفايل الفني", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(6.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("صورة الهوية المهنية", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Button(
                                                onClick = {
                                                    viewModel.acceptPendingProvider(pending.id, pending)
                                                    Toast.makeText(context, "تم المصادقة وقبول الفني رسمياً في الدليل!", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("قبول وتفعيل", color = Color.White, fontSize = 11.sp)
                                            }

                                            Button(
                                                onClick = {
                                                    viewModel.rejectPendingProvider(pending.id, pending.name, "مستندات مهنية غير واضحة")
                                                    Toast.makeText(context, "تم رفض الطلب بنجاح", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("رفض مع توضيح السبب", color = Color.White, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Manual provider creation and view listing
                    Column {
                        var insertName by remember { mutableStateOf("") }
                        var insertPhone by remember { mutableStateOf("") }
                        var insertSubCategory by remember { mutableStateOf("كهربائي منازل") }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = insertName,
                                onValueChange = { insertName = it },
                                label = { Text("اسم الفني الجديد", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = insertPhone,
                                onValueChange = { insertPhone = it },
                                label = { Text("جوال الفني", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = {
                                if (insertName.isNotEmpty() && insertPhone.isNotEmpty()) {
                                    viewModel.addProviderManually(
                                        name = insertName,
                                        mainCat = "صيانة منزلية",
                                        subCat = insertSubCategory,
                                        location = "صنعاء",
                                        phone = insertPhone,
                                        whatsapp = insertPhone,
                                        isVip = false,
                                        isRecommended = true,
                                        notes = "تم إضافته يدوياً من قبل لوحة المشرفين لتبسيط الخدمة المباشرة."
                                    )
                                    insertName = ""
                                    insertPhone = ""
                                    Toast.makeText(context, "تمت الإضافة الفورية بنجاح!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "فضلاً، قم بتعبئة البيانات أولاً", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("إضافة فني دليل فوري يدوياً", color = Color.White, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(10.dp))

                        // Fast list directory managers
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(providerList) { provider ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Gray.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = provider.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(text = "${provider.mainCategory} | ${provider.location}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    IconButton(onClick = {
                                        viewModel.removeProvider(provider.id)
                                        Toast.makeText(context, "تمت إزالة الفني بنجاح", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "إزالة الفني", tint = Color.Red, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Create main/sub service departments
                    var newCatName by remember { mutableStateOf("") }
                    var newGroup by remember { mutableStateOf("صيانة منزلية") }

                    Column {
                        OutlinedTextField(
                            value = newCatName,
                            onValueChange = { newCatName = it },
                            label = { Text("اسم القسم الجديد (مثال: كهربائي سيارات)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = {
                                if (newCatName.isNotEmpty()) {
                                    viewModel.addCategory(newCatName, newGroup, "Build", false)
                                    newCatName = ""
                                    Toast.makeText(context, "تم إضافة القسم ونشره بالدليل!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("إضاف القسم للخدمات", color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(10.dp))

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(categoryList) { cat ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Gray.copy(alpha = 0.05f))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "${cat.name} (${cat.groupName})", fontSize = 12.sp)
                                    IconButton(onClick = {
                                        viewModel.removeCategory(cat.id)
                                        Toast.makeText(context, "تم إزالة القسم", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "إزالة", tint = Color.Red, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // Telemetry analytics, live activity audit trails
                    Column {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "سجل النشاط الفوري (رصد المراقبة)", fontWeight = FontWeight.Bold, color = primaryColor)
                            TextButton(onClick = { viewModel.clearAllLogs() }) {
                                Text("تصفير النشاطات 🚯", color = Color.Red, fontSize = 11.sp)
                            }
                        }

                        // Simulation export action triggers
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { Toast.makeText(context, "تم تصدير كشف البلاغات والتقارير بصيغة PDF بنجاح!", Toast.LENGTH_LONG).show() },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor.copy(alpha = 0.15f), contentColor = primaryColor),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("تصدير كشف PDF 🖨️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { Toast.makeText(context, "تم تصدير سجلات الدلائل وقاعدة النشاطات بصيغة CSV بنجاح!", Toast.LENGTH_LONG).show() },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor.copy(alpha = 0.15f), contentColor = primaryColor),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("تصدير ملف Excel 📁", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(logsList) { log ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Gray.copy(alpha = 0.05f))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "• [أدمن]: ${log.action}",
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Return button to HomeScreen
        Button(
            onClick = { viewModel.navigateTo("home") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
                .height(44.dp),
            colors = ButtonDefaults.buttonColors(containerColor = secondaryColor, contentColor = Color.Black)
        ) {
            Text("العودة لشاشات الدليل الرئيسية 🏠", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FloatingWidgetsOverlay(
    viewModel: AppViewModel,
    primaryColor: Color,
    secondaryColor: Color,
    config: AppConfigEntity
) {
    var showAssistantBubble by remember { mutableStateOf(false) }

    // Floating AI Assistant Bubble 🤖 with configurable size
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp, start = 16.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            FloatingActionButton(
                onClick = { showAssistantBubble = true },
                containerColor = primaryColor,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(config.chatIconSize.dp)
                    .testTag("floating_ai_assistant_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "مساعد اليمن الذكي"
                )
            }
            Text(
                text = "مساعد ذكي",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }

    // Interactive Floating Chat overlay Dialogue
    if (showAssistantBubble) {
        val assistantChat by viewModel.assistantChat.collectAsState()
        val isTyping by viewModel.isAssistantTyping.collectAsState()
        var userInputText by remember { mutableStateOf("") }
        val listState = rememberLazyListState()

        // Auto Scroll to last message
        LaunchedEffect(assistantChat.size) {
            if (assistantChat.isNotEmpty()) {
                listState.animateScrollToItem(assistantChat.size - 1)
            }
        }

        Dialog(onDismissRequest = { showAssistantBubble = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Chat Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(primaryColor)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = "روبوت ذكاء اصطناعي",
                                tint = secondaryColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "مساعد خدمات اليمن (Gemini AI)",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                        IconButton(onClick = { showAssistantBubble = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
                        }
                    }

                    // Chat messages list
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(assistantChat) { chatItem ->
                            val isAI = chatItem.second
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = if (isAI) Alignment.CenterStart else Alignment.CenterEnd
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isAI) primaryColor.copy(alpha = 0.08f) else secondaryColor.copy(alpha = 0.15f)
                                    ),
                                    shape = RoundedCornerShape(
                                        topStart = 12.dp,
                                        topEnd = 12.dp,
                                        bottomStart = if (isAI) 0.dp else 12.dp,
                                        bottomEnd = if (isAI) 12.dp else 0.dp
                                    ),
                                    modifier = Modifier.fillMaxWidth(0.85f)
                                ) {
                                    Text(
                                        text = chatItem.first,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(10.dp),
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }

                        if (isTyping) {
                            item {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = primaryColor, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("أفكر في إجابة تناسبك... 🤖💭", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }
                    }

                    // User text input drawer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Gray.copy(alpha = 0.05f))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = userInputText,
                            onValueChange = { userInputText = it },
                            placeholder = { Text("اسألني عن التسجيل، الأسعار، الدعم...", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f).testTag("assistant_text_field"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                if (userInputText.trim().isNotEmpty()) {
                                    viewModel.askAssistant(userInputText)
                                    userInputText = ""
                                }
                            },
                            modifier = Modifier
                                .background(primaryColor, CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = "إرسال", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}
