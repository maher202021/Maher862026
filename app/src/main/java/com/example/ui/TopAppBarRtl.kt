package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarRtl(
    viewModel: AppViewModel,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val config by viewModel.adminConfig.collectAsState()
    val currentRoute by viewModel.currentRoute.collectAsState()
    val lang by viewModel.appLanguage.collectAsState()

    // House Icon Click Trigger Counter for Secret Backdoor Entry (5 times sequential)
    var houseClickCounter by remember { mutableStateOf(0) }
    var lastClickTime by remember { mutableStateOf(0L) }
    var showBackdoorPinDialog by remember { mutableStateOf(false) }

    val primaryColor = remember(config.primaryColorHex) {
        try { Color(android.graphics.Color.parseColor(config.primaryColorHex)) }
        catch (e: Exception) { Color(0xFF0F172A) }
    }

    val isArabic = lang == "ar"

    // Backdoor PIN state
    var backdoorPinInput by remember { mutableStateOf("") }
    var rememberMeChecked by remember { mutableStateOf(false) }
    var backdoorLoginError by remember { mutableStateOf("") }

    if (showBackdoorPinDialog) {
        AlertDialog(
            onDismissRequest = { showBackdoorPinDialog = false },
            title = {
                Text(
                    text = if (isArabic) "🔐 بوابة مخصصة للمؤسسين" else "🔐 Developer Private Gateway",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = primaryColor,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = if (isArabic) "أدخل الرمز السري الفائق لإعداد الخادم:" else "Enter master developer token to adjust app coordinates:",
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = backdoorPinInput,
                        onValueChange = {
                            backdoorPinInput = it
                            backdoorLoginError = ""
                        },
                        label = { Text(if (isArabic) "الرمز السري الخاص" else "Private Secret Key") },
                        placeholder = { Text("••••••••") },
                        modifier = Modifier.fillMaxWidth().testTag("backdoor_pin_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            focusedLabelColor = primaryColor
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { rememberMeChecked = !rememberMeChecked },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = if (isArabic) "تذكرني في هذا الجهاز" else "Remember device session",
                            fontSize = 11.sp,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Checkbox(
                            checked = rememberMeChecked,
                            onCheckedChange = { rememberMeChecked = it },
                            modifier = Modifier.testTag("remember_me_checkbox")
                        )
                    }

                    if (backdoorLoginError.isNotEmpty()) {
                        Text(
                            text = backdoorLoginError,
                            color = Color.Red,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val verified = viewModel.authenticateBackdoor(backdoorPinInput.trim(), rememberMeChecked)
                        if (verified) {
                            showBackdoorPinDialog = false
                            backdoorPinInput = ""
                            viewModel.navigateTo("backdoor")
                            Toast.makeText(context, if (isArabic) "دخول المالك الفائق للمنصة السحابية منحن بنجاح! 👑" else "Master session active!", Toast.LENGTH_SHORT).show()
                        } else {
                            backdoorLoginError = if (isArabic) "عذراً، الرمز السري غير صالح!" else "Error authentication key"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    modifier = Modifier.testTag("submit_backdoor_auth")
                ) {
                    Text(if (isArabic) "تأكيد الدخول" else "Confirm Authorization")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showBackdoorPinDialog = false
                    backdoorPinInput = ""
                    backdoorLoginError = ""
                }) {
                    Text(if (isArabic) "إلغاء الأمر" else "Dismiss")
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = primaryColor,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp, bottom = 8.dp) // Edge-to-edge status bar safety padding
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Title & Logo
                Box(
                    modifier = Modifier
                        .clickable {
                            val now = System.currentTimeMillis()
                            if (now - lastClickTime < 1000) {
                                houseClickCounter++
                            } else {
                                houseClickCounter = 1
                            }
                            lastClickTime = now

                            if (houseClickCounter >= 5) {
                                showBackdoorPinDialog = true
                                houseClickCounter = 0
                            } else {
                                viewModel.navigateTo("home")
                            }
                        }
                        .padding(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⚡", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(
                                text = config.appName,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                            Text(
                                text = config.slogan,
                                fontSize = 9.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                        }
                    }
                }

                // Row of Action icons order Right-to-Left RTL:
                // 🏠 (Home), 🔐 (Admin Screen), 👤 (Submit Technician Request), 🌐 (Language Change), 🔄 (Sync/Reload Data)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 🏠 Home Icon Button
                    IconButton(
                        onClick = {
                            val now = System.currentTimeMillis()
                            if (now - lastClickTime < 1000) {
                                houseClickCounter++
                            } else {
                                houseClickCounter = 1
                            }
                            lastClickTime = now

                            if (houseClickCounter >= 5) {
                                showBackdoorPinDialog = true
                                houseClickCounter = 0
                            } else {
                                viewModel.navigateTo("home")
                            }
                        },
                        modifier = Modifier.testTag("nav_home_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home Logo",
                            tint = if (currentRoute == "home") Color.Yellow else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // 🔐 Login / Access Button
                    IconButton(
                        onClick = {
                            if (viewModel.isAdminAuthenticated.value) {
                                viewModel.navigateTo("admin_dashboard")
                            } else {
                                viewModel.navigateTo("admin_login")
                            }
                        },
                        modifier = Modifier.testTag("nav_admin_login_btn")
                    ) {
                        Icon(
                            imageVector = if (viewModel.isAdminAuthenticated.value) Icons.Default.Dashboard else Icons.Default.Lock,
                            contentDescription = "Admin Control Backdoor",
                            tint = if (currentRoute == "admin_dashboard" || currentRoute == "admin_login") Color.Yellow else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // 👤 Technician Registration Intent
                    IconButton(
                        onClick = { viewModel.navigateTo("technician_register") },
                        modifier = Modifier.testTag("nav_register_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Register Professional Partner",
                            tint = if (currentRoute == "technician_register") Color.Yellow else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // 🌐 Switch Interface Language
                    IconButton(
                        onClick = {
                            viewModel.toggleLanguage()
                            val msg = if (viewModel.appLanguage.value == "ar") "تم تبديل لغة التطبيق إلى العربية بنجاح واجهة مرنة" else "App interface set to English mode"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("nav_lang_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Translate Client Interface",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // 🔄 Manual Dynamic Reload / Trigger sync
                    IconButton(
                        onClick = {
                            // Instant Reload toast feedback
                            Toast.makeText(context, if (isArabic) "جاري تحديث ومزامنة البيانات الفورية ومطابقة الدليل... 🔄" else "Syncing with cloud Backplane...", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("nav_sync_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Manual Cloud Sync",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
