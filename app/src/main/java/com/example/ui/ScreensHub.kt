package com.example.ui

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AdminConfig
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreensHub(viewModel: AppViewModel) {
    val context = LocalContext.current
    val currentRoute by viewModel.currentRoute.collectAsState()
    val config by viewModel.adminConfig.collectAsState()
    val isArabic = viewModel.appLanguage.collectAsState().value == "ar"

    // Theme Config colors parsers
    val primaryColor = remember(config.primaryColorHex) {
        try { Color(android.graphics.Color.parseColor(config.primaryColorHex)) }
        catch (e: Exception) { Color(0xFF0F172A) }
    }

    val secondaryColor = remember(config.secondaryColorHex) {
        try { Color(android.graphics.Color.parseColor(config.secondaryColorHex)) }
        catch (e: Exception) { Color(0xFF3B82F6) }
    }

    // Double Back tap Exit handler states
    var backPressedOnce by remember { mutableStateOf(false) }
    LaunchedEffect(backPressedOnce) {
        if (backPressedOnce) {
            delay(2000L)
            backPressedOnce = false
        }
    }

    BackHandler {
        if (currentRoute != "home") {
            viewModel.navigateTo("home")
        } else {
            if (backPressedOnce) {
                (context as? Activity)?.finish()
            } else {
                backPressedOnce = true
                Toast.makeText(context, if (isArabic) "اضغط مرة أخرى للخروج من تطبيق خدمات WAM 🛑" else "Double press back to exit", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Interactive Floating AI Assistant dialog
    var showAssistantChatState by remember { mutableStateOf(false) }
    var assistantPromptState by remember { mutableStateOf("") }
    val assistantLog by viewModel.assistantChatLog.collectAsState()
    val assistantLoading by viewModel.isAssistantLoading.collectAsState()

    if (showAssistantChatState) {
        AlertDialog(
            onDismissRequest = { showAssistantChatState = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            viewModel.clearAssistantHistory()
                            Toast.makeText(context, "تم تطهير ذاكرة شات المساعد الذكي بنجاح", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.DeleteSweep, "Clear AI history", tint = Color.Red)
                    }

                    Text(
                        text = "💬 مساعد خدمات الـ WAM الذكي 🤖",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = primaryColor
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            reverseLayout = false
                        ) {
                            items(assistantLog) { pair ->
                                val (txt, isMe) = pair
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isMe) secondaryColor else Color.White)
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = txt,
                                            fontSize = 11.sp,
                                            color = if (isMe) Color.White else Color.Black
                                        )
                                    }
                                }
                            }

                            if (assistantLoading) {
                                item {
                                    Row(horizontalArrangement = Arrangement.Start, modifier = Modifier.fillMaxWidth()) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.LightGray.copy(alpha = 0.4f))
                                                .padding(6.dp)
                                        ) {
                                            Text("جاري معالجة الإجابة الفنية... ⏳🤖", fontSize = 10.sp, color = Color.DarkGray)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = assistantPromptState,
                        onValueChange = { assistantPromptState = it },
                        placeholder = { Text("ماهي أرقام الدعم؟ كيف أتصل بالسباك؟...") },
                        modifier = Modifier.fillMaxWidth().testTag("ai_assistant_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (assistantPromptState.trim().isNotEmpty()) {
                            viewModel.askAssistant(assistantPromptState.trim())
                            assistantPromptState = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    modifier = Modifier.testTag("submit_ai_query")
                ) {
                    Text(if (isArabic) "إرسال سؤال" else "Ask AI")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAssistantChatState = false }) {
                    Text(if (isArabic) "إغلاق" else "Close")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBarRtl(viewModel) { destination ->
                viewModel.navigateTo(destination)
            }
        },
        bottomBar = {
            // Elegant Floating and Customizable Footer Bar - No bottom bar overlaps or cuts
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                color = primaryColor,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left element: Info icon `ℹ️` (About App Router)
                    IconButton(
                        onClick = { viewModel.navigateTo("about_app") },
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.15f), CircleShape)
                            .size(38.dp)
                            .testTag("footer_about_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About Software",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Middle Element: Sponsor Copyright Text line
                    Text(
                        text = config.footerText,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 10.sp, // Small font size 50%
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("footer_copyright_label")
                    )

                    // Right Element: Small Smart Floating assistant bubble
                    // Circular small button size 50% of original
                    IconButton(
                        onClick = { showAssistantChatState = true },
                        modifier = Modifier
                            .background(secondaryColor, CircleShape)
                            .size(38.dp) // Size reduced 50%
                            .testTag("footer_assistant_trigger_btn")
                    ) {
                        Text(
                            text = config.assistantIcon,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentRoute) {
                "home" -> MainHomeScreen(viewModel, config)
                "admin_login" -> AdminLoginScreen(viewModel) {
                    viewModel.navigateTo("admin_dashboard")
                }
                "admin_dashboard" -> {
                    if (viewModel.isAdminAuthenticated.collectAsState().value) {
                        AdminDashboardScreen(viewModel, config)
                    } else {
                        viewModel.navigateTo("admin_login")
                    }
                }
                "backdoor" -> {
                    if (viewModel.isBackdoorAuthenticated.collectAsState().value) {
                        BackdoorSecretScreen(viewModel, config)
                    } else {
                        viewModel.navigateTo("home")
                    }
                }
                "technician_register" -> TechnicianRegisterScreen(viewModel, config)
                "about_app" -> AboutAppScreen(viewModel, config)
                "chat_room" -> RealtimeChatRoomScreen(viewModel, config)
                else -> MainHomeScreen(viewModel, config)
            }
        }
    }
}
