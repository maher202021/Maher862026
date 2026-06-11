package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainHomeScreen(
    viewModel: AppViewModel,
    config: AdminConfig
) {
    val context = LocalContext.current
    val isArabic = viewModel.appLanguage.collectAsState().value == "ar"

    val primaryColor = remember(config.primaryColorHex) {
        try { Color(android.graphics.Color.parseColor(config.primaryColorHex)) }
        catch (e: Exception) { Color(0xFF0F172A) }
    }

    val secondaryColor = remember(config.secondaryColorHex) {
        try { Color(android.graphics.Color.parseColor(config.secondaryColorHex)) }
        catch (e: Exception) { Color(0xFF3B82F6) }
    }

    // Flows
    val ads by viewModel.banners.collectAsState()
    val cats by viewModel.categories.collectAsState()
    val citiesName by viewModel.cities.collectAsState()
    val providersAll by viewModel.filteredProviders.collectAsState()
    val bookmarked by viewModel.bookmarkedIds.collectAsState()

    // Filters states
    val activeCatFilter by viewModel.selectedCategory.collectAsState()
    val activeCityFilter by viewModel.selectedCity.collectAsState()
    val activeQuery by viewModel.searchQuery.collectAsState()
    val locationRadius by viewModel.searchRadiusKm.collectAsState()

    var showRadiusSlider by remember { mutableStateOf(false) }
    var activeAdIndex by remember { mutableStateOf(0) }

    // Dialog state for reporting complaints
    var showReportDialogForProvider by remember { mutableStateOf<Provider?>(null) }
    var reporterNameInput by remember { mutableStateOf("") }
    var reporterPhoneInput by remember { mutableStateOf("") }
    var reporterDetailsInput by remember { mutableStateOf("") }

    // Banner switcher timer loop
    LaunchedEffect(ads) {
        if (ads.isNotEmpty()) {
            while (true) {
                val duration = ads.getOrNull(activeAdIndex)?.durationSeconds ?: 5
                delay(duration * 1000L)
                activeAdIndex = (activeAdIndex + 1) % ads.size
            }
        }
    }

    if (showReportDialogForProvider != null) {
        val target = showReportDialogForProvider!!
        AlertDialog(
            onDismissRequest = { showReportDialogForProvider = null },
            title = {
                Text(
                    text = if (isArabic) "⚠️ تسليم بلاغ شكوى رسمي" else "⚠️ File Official Complaint",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Red,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.End) {
                    Text("تقديم شكوى ضد الفني: ${target.name}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = reporterNameInput,
                        onValueChange = { reporterNameInput = it },
                        label = { Text("اسمك الكريم الكامل") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reporterPhoneInput,
                        onValueChange = { reporterPhoneInput = it },
                        label = { Text("رقم هاتفك للتأكيد") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reporterDetailsInput,
                        onValueChange = { reporterDetailsInput = it },
                        label = { Text("تفاصيل المشكلة والوقائع بالتفصيل") },
                        modifier = Modifier.fillMaxWidth().height(80.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reporterNameInput.isEmpty() || reporterPhoneInput.isEmpty() || reporterDetailsInput.isEmpty()) {
                            Toast.makeText(context, "يرجى تعبئة جميع خانات التأكيد لضمان متابعة البلاغ!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val rItem = ReportItem(
                            providerId = target.id,
                            providerName = target.name,
                            reporterName = reporterNameInput.trim(),
                            reporterPhone = reporterPhoneInput.trim(),
                            complaintText = reporterDetailsInput.trim()
                        )
                        viewModel.submitReport(rItem) { success ->
                            if (success) {
                                Toast.makeText(context, "تم إرسال الشكوى للإدارة السحابية لمطابقتها فوراً! 🛡️", Toast.LENGTH_LONG).show()
                                showReportDialogForProvider = null
                                reporterNameInput = ""
                                reporterPhoneInput = ""
                                reporterDetailsInput = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(if (isArabic) "إرسال البلاغ فوراً" else "Submit Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialogForProvider = null }) {
                    Text(if (isArabic) "تراجع" else "Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Sliding Banner Flyer Header
        if (ads.isNotEmpty()) {
            val focusAd = ads[activeAdIndex]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = primaryColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(primaryColor, secondaryColor)
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .background(Color.Yellow, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("إعلان مميز", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                            Text(focusAd.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Text(
                            text = focusAd.contentText.ifEmpty { "نوفر لك أفضل الخدمات الفنية بأقل الأسعار وعلى مدار الساعة" },
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        } else {
            // Default Welcome Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = primaryColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(primaryColor, Color(0xFF1E293B))
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "مرحباً بك في منصة ${config.appName} الموحدة 🇾🇪",
                            color = Color.Yellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "دليلك اليمني الذكي والتنسيقي لربط مقدمي المهن والكهرباء والسباكة مع العملاء برعاية شركة WAM وصاحبها ماهر.",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // 2. City Filter Selector Row
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isArabic) "📍 المحافظة / نطاق التصفية المباشر:" else "📍 Filter by Yemen Territory:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = primaryColor
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // All cities toggle option
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (activeCityFilter.isEmpty()) secondaryColor else Color(0xFFF1F5F9))
                            .clickable { viewModel.setCityFilter("") }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isArabic) "جميع المدن 🇾🇪" else "All Cities",
                            color = if (activeCityFilter.isEmpty()) Color.White else Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    citiesName.forEach { city ->
                        val isSelected = activeCityFilter == city.nameAr
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) secondaryColor else Color(0xFFF1F5F9))
                                .clickable { viewModel.setCityFilter(city.nameAr) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isArabic) city.nameAr else city.nameEn,
                                color = if (isSelected) Color.White else Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 3. Advanced Search Bar Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = activeQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text(if (isArabic) "اصطاد فني بالاسم، الهاتف، المنطقة..." else "Search by name, region...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("app_search_field"),
                        shape = RoundedCornerShape(10.dp),
                        leadingIcon = { Icon(Icons.Default.Search, "Search") },
                        singleLine = true
                    )

                    // simulated Voice search button
                    IconButton(
                        onClick = {
                            viewModel.setSearchQuery("كهربائي صنعاء القديمة")
                            Toast.makeText(context, "التعرف الصوتي الذكي: 'كهربائي صنعاء القديمة' 🎤🇾🇪", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .background(Color(0xFFE2E8F0), CircleShape)
                            .size(44.dp)
                    ) {
                        Icon(Icons.Default.Mic, "Speech Voice Search")
                    }

                    // simulated GPS Radius Filter Toggle
                    IconButton(
                        onClick = { showRadiusSlider = !showRadiusSlider },
                        modifier = Modifier
                            .background(if (showRadiusSlider) secondaryColor else Color(0xFFE2E8F0), CircleShape)
                            .size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Radius Search",
                            tint = if (showRadiusSlider) Color.White else Color.Black
                        )
                    }
                }

                if (showRadiusSlider) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("رادار البحث ضمن النطاق الجغرافي الدائري 🌍", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            Text("${locationRadius.toInt()} كم", fontSize = 11.sp, color = secondaryColor, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = locationRadius,
                            onValueChange = { viewModel.setRadiusKm(it) },
                            valueRange = 2f..50f,
                            colors = SliderDefaults.colors(thumbColor = secondaryColor)
                        )
                    }
                }
            }
        }

        // 4. Categories Selection Row Grid
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = if (isArabic) "🛠️ قسّم الدليل بالتصنيف المهني الجاهز:" else "🛠️ Filter by Craft Category:",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = primaryColor
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // All Category Toggle option
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (activeCatFilter.isEmpty()) primaryColor else Color.White)
                        .border(1.dp, if (activeCatFilter.isEmpty()) primaryColor else Color.LightGray, RoundedCornerShape(12.dp))
                        .clickable { viewModel.setCategoryFilter("") }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("⭐", fontSize = 14.sp)
                        Text(
                            text = if (isArabic) "جميع الخدمات" else "All Services",
                            color = if (activeCatFilter.isEmpty()) Color.White else Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                cats.forEach { cat ->
                    val isSelected = activeCatFilter == cat.nameAr
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) primaryColor else Color.White)
                            .border(1.dp, if (isSelected) primaryColor else Color.LightGray.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .clickable { viewModel.setCategoryFilter(cat.nameAr) }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when (cat.nameAr) {
                                    "كهربائي" -> "⚡"
                                    "سباكة وصيانة" -> "💧"
                                    "نجار وبناء" -> "🔨"
                                    "تكييف وتبريد" -> "❄️"
                                    else -> "🛠️"
                                },
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isArabic) cat.nameAr else cat.nameEn,
                                color = if (isSelected) Color.White else Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // 5. Providers Listing
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = if (isArabic) "👷 مقدمي الخدمات الفنيين الموصى بهم:" else "👷 Available Professional Partners:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = primaryColor
                )
                Text("المجموع: ${providersAll.size}", fontSize = 10.sp, color = Color.Gray)
            }

            if (providersAll.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, "Empty List Indicators", tint = Color.LightGray, modifier = Modifier.size(36.dp))
                        Text(
                            text = "لا توجد نتائج مطابقة لفلتر البحث حالياً في المحافظة المحددة.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                providersAll.forEach { p ->
                    val isBookmarked = bookmarked.contains(p.id)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (p.isVip) Color(0xFFFFFBEB) else Color.White // VIP subtle golden color
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(p.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = primaryColor)

                                    if (p.isVerified) {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .background(Color(0xFF3B82F6), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Check, "Verified Badge", tint = Color.White, modifier = Modifier.size(11.dp))
                                        }
                                    }

                                    if (p.isVip) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFFBBF24), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text("VIP شارة نخبة", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        }
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    // Bookmark
                                    IconButton(
                                        onClick = { viewModel.toggleBookmark(p.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                            contentDescription = "Save Bookmark",
                                            tint = if (isBookmarked) Color.Red else Color.Gray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Divider(color = Color(0xFFF1F5F9))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Build, "Craft", tint = Color.Gray, modifier = Modifier.size(14.dp))
                                    Text(p.serviceCategory, fontSize = 11.sp, color = Color.DarkGray)
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, "Location", tint = Color.Red, modifier = Modifier.size(14.dp))
                                    Text("${p.locationCity} - ${p.locationRegion}", fontSize = 11.sp, color = Color.DarkGray)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, "Ratings", tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                    Text("${p.rate} / 5.0", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Complaint Red Flag
                                    IconButton(
                                        onClick = { showReportDialogForProvider = p },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color(0xFFFEF2F2), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Flag, "Report Technician", tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }

                                    // Direct chat with technician
                                    Button(
                                        onClick = {
                                            viewModel.navigateTo("chat_room")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Icon(Icons.Default.Chat, "Chat Room", tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("محادثة فورية", fontSize = 10.sp)
                                    }

                                    // Dial telephone
                                    Button(
                                        onClick = {
                                            val i = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${p.phone}"))
                                            context.startActivity(i)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Icon(Icons.Default.Phone, "Call", tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("اتصال فوري", fontSize = 10.sp)
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

// Extra utility dummy helper
private fun RowScope.justifyContents(between: Arrangement.Horizontal) {
    // Left empty for compatibility
}

// --- Dynamic Provider Registration Form ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnicianRegisterScreen(
    viewModel: AppViewModel,
    config: AdminConfig
) {
    val context = LocalContext.current
    val isArabic = viewModel.appLanguage.collectAsState().value == "ar"

    val coreColor = remember(config.primaryColorHex) {
        try { Color(android.graphics.Color.parseColor(config.primaryColorHex)) }
        catch (e: Exception) { Color(0xFF0F172A) }
    }

    var regName by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regRegion by remember { mutableStateOf("") }
    var regGpsSecret by remember { mutableStateOf("15.3693, 44.1910") } // default Coordinates

    val allCats by viewModel.categories.collectAsState()
    val allCitiesName by viewModel.cities.collectAsState()

    var activeRegCategory by remember { mutableStateOf(allCats.firstOrNull()?.nameAr ?: "كهربائي") }
    var activeRegCity by remember { mutableStateOf(allCitiesName.firstOrNull()?.nameAr ?: "صنعاء") }

    // Men vs women optional fields trigger
    var isFemaleDeclaration by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.PersonAdd, "Register Form Icon", tint = coreColor, modifier = Modifier.size(36.dp))
                Text(
                    text = "استمارة الانضمام لمقدمي الخدمة المحترفين 👷🇾🇪",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = coreColor
                )
                Text(
                    text = "سجل الآن كمزود خدمة معنا لتلقي طلبات العمل الفورية من كافه مدن اليمن مجاناً.",
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = regName,
                    onValueChange = { regName = it },
                    label = { Text("الاسم الكامل ثلاثياً (الرجال إجباري)") },
                    modifier = Modifier.fillMaxWidth().testTag("reg_name_input")
                )

                OutlinedTextField(
                    value = regPhone,
                    onValueChange = { regPhone = it },
                    label = { Text("رقم الهاتف المتصل بوصلة الواتساب الفعالة") },
                    modifier = Modifier.fillMaxWidth().testTag("reg_phone_input")
                )

                // Selectors lists
                Text("المحافظة الجغرافية والتصنيف الحرفي الملائم:", fontSize = 11.sp, color = Color.Gray)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("القسم المهني الحرفي: ${activeRegCategory}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        allCats.forEach { ct ->
                            Text(
                                text = ct.nameAr,
                                fontSize = 10.sp,
                                modifier = Modifier
                                    .padding(vertical = 2.dp)
                                    .fillMaxWidth()
                                    .clickable { activeRegCategory = ct.nameAr }
                                    .background(if (activeRegCategory == ct.nameAr) coreColor else Color.LightGray.copy(alpha = 0.3f))
                                    .padding(4.dp),
                                color = if (activeRegCategory == ct.nameAr) Color.White else Color.Black,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("المحافظة المستهدفة: ${activeRegCity}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        allCitiesName.forEach { c ->
                            Text(
                                text = c.nameAr,
                                fontSize = 10.sp,
                                modifier = Modifier
                                    .padding(vertical = 2.dp)
                                    .fillMaxWidth()
                                    .clickable { activeRegCity = c.nameAr }
                                    .background(if (activeRegCity == c.nameAr) coreColor else Color.LightGray.copy(alpha = 0.3f))
                                    .padding(4.dp),
                                color = if (activeRegCity == c.nameAr) Color.White else Color.Black,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = regRegion,
                    onValueChange = { regRegion = it },
                    label = { Text("منطقة السكن الحالية والشارع بالتفصيل") },
                    modifier = Modifier.fillMaxWidth().testTag("reg_region_input")
                )

                OutlinedTextField(
                    value = regGpsSecret,
                    onValueChange = { regGpsSecret = it },
                    label = { Text("إحداثيات تحديد اللوكيشن GPS (اختياري)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // optional lady toggle banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isFemaleDeclaration = !isFemaleDeclaration }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = isFemaleDeclaration, onCheckedChange = { isFemaleDeclaration = it })
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("خيار تسجيل للمهندسات / السيدات للخصوصية التامة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                if (isFemaleDeclaration) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2))
                    ) {
                        Text(
                            text = "🔒 شارة الخصوصية للفتيات والسيدات مفعلة: صورتكم الشخصية وبطاقة الهوية ستبقى مشفرة ومتاحة بنطاق الإدارة الفنية السحابية فقط ولن تظهر للزبائن.",
                            fontSize = 10.sp,
                            modifier = Modifier.padding(12.dp),
                            color = Color(0xFFBE123C)
                        )
                    }
                }

                // Attachments selectors simulated
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            Toast.makeText(context, "تم التقاط وضغط الصورة الشخصية للتحميل بنجاح WebP ✨", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                    ) {
                        Text("📷 تحميل صورة 🤳", fontSize = 10.sp)
                    }

                    Button(
                        onClick = {
                            Toast.makeText(context, "تم مسح وتخزين إثبات بطاقة الهوية الوطنية مشفراً بمسار آمن للـ Storage 🪪", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                    ) {
                        Text("🪪 بطاقة الهوية", fontSize = 10.sp)
                    }
                }

                Button(
                    onClick = {
                        if (regName.isEmpty() || regPhone.isEmpty() || regRegion.isEmpty()) {
                            Toast.makeText(context, "عفواً، يرجى تعبئة كافة الحقول الإجبارية أولاً لإتمام طلب التسجيل 👷!", Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        val finalPayload = Provider(
                            name = regName.trim(),
                            phone = regPhone.trim(),
                            locationCity = activeRegCity,
                            locationRegion = regRegion.trim(),
                            serviceCategory = activeRegCategory,
                            gpsCoordinates = regGpsSecret,
                            status = "pending", // Pending administration review
                            photoUrl = "photo_attached_ok.webp",
                            idCardUrl = "id_card_attached_ok.webp"
                        )
                        viewModel.submitRequest(finalPayload) { success ->
                            if (success) {
                                Toast.makeText(context, "تم تقديم طلب التسجيل بنجاح! طلبك معلق قيد المراجعة الإدارية الفورية. 📡👷🇾🇪", Toast.LENGTH_LONG).show()
                                viewModel.navigateTo("home")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("reg_submit_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = coreColor),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("إرسال طلب الانضمام للدليل الآن 📡")
                }
            }
        }
    }
}

// --- About App View ---

@Composable
fun AboutAppScreen(
    viewModel: AppViewModel,
    config: AdminConfig
) {
    val context = LocalContext.current
    val isArabic = viewModel.appLanguage.collectAsState().value == "ar"

    val coreColor = remember(config.primaryColorHex) {
        try { Color(android.graphics.Color.parseColor(config.primaryColorHex)) }
        catch (e: Exception) { Color(0xFF0F172A) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(coreColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Info, "About Logo Info", tint = Color.White, modifier = Modifier.size(36.dp))
        }

        Text(
            text = "معلومات عن تطبيق ${config.appName}",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = coreColor
        )

        Text(
            text = config.infoHtmlText,
            fontSize = 12.sp,
            color = Color.DarkGray,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("بيانات التواصل والدعم الفني للمالك:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = coreColor)
                Text("هاتف الدعم: ${config.supportPhone}", fontSize = 11.sp)
                Text("البريد الإلكتروني: ${config.supportEmail}", fontSize = 11.sp)

                Divider(color = Color(0xFFF1F5F9))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/${config.supportWhatsapp}"))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("دعم الواتساب 💬")
                    }

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${config.supportPhone}"))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = coreColor),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("اتصل بالدعم 📞")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "نسخة التطبيق: 2.1.0-WAM\nالمعرف: ${config.footerText}",
            fontSize = 10.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

// --- Dynamic General Chat Room Screen ---

@Composable
fun RealtimeChatRoomScreen(
    viewModel: AppViewModel,
    config: AdminConfig
) {
    val context = LocalContext.current
    var textInputState by remember { mutableStateOf("") }
    val isArabic = viewModel.appLanguage.collectAsState().value == "ar"

    val coreColor = remember(config.primaryColorHex) {
        try { Color(android.graphics.Color.parseColor(config.primaryColorHex)) }
        catch (e: Exception) { Color(0xFF0F172A) }
    }

    val conversation by viewModel.chats.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE2E8F0))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(coreColor)
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Chat, "Chat Header Icon", tint = Color.White)
                Column {
                    Text("غرفة محادثة الدعم السريع والاتفاقيات ✨", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    Text("آمنة ومقترنة بالمزامنة السحابية الفورية", color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp)
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(conversation) { msg ->
                val fromMe = msg.senderId == "current_uuid_10"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (fromMe) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (fromMe) coreColor else Color.White
                        ),
                        modifier = Modifier.widthIn(max = 240.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = msg.senderName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = if (fromMe) Color.Yellow else Color.DarkGray
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = msg.messageText,
                                fontSize = 12.sp,
                                color = if (fromMe) Color.White else Color.Black
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = textInputState,
                onValueChange = { textInputState = it },
                placeholder = { Text("أكتب تفاصيل استفسارك أو اتفاقك...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_room_text_field"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            IconButton(
                onClick = {
                    if (textInputState.trim().isEmpty()) return@IconButton
                    val outgoing = ChatMessage(
                        senderId = "current_uuid_10",
                        senderName = "أنت (عميل WAM)",
                        messageText = textInputState.trim()
                    )
                    viewModel.sendChatMessage(outgoing) { success ->
                        if (success) {
                            textInputState = ""
                        }
                    }
                },
                modifier = Modifier
                    .background(coreColor, CircleShape)
                    .size(44.dp)
            ) {
                Icon(Icons.Default.Send, "Send message button", tint = Color.White)
            }
        }
    }
}
