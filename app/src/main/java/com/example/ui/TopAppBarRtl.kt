package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AdminConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarRtl(viewModel: AppViewModel, config: AdminConfig) {
    val theme = resolveTheme(config)
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()

    var backdoorTapCount by remember { mutableStateOf(0) }
    var showBackdoorPinDialog by remember { mutableStateOf(false) }
    var backdoorPinInput by remember { mutableStateOf("") }
    var rememberMeChecked by remember { mutableStateOf(false) }

    Surface(
        color = theme.primary,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left block: Dynamic App Title (5 continuous clicks triggers doorway)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    backdoorTapCount++
                    if (backdoorTapCount >= 5) {
                        backdoorTapCount = 0
                        showBackdoorPinDialog = true
                    }
                }
            ) {
                Text(
                    text = if (currentLanguage == "ar") config.appName else "Yemen Services Hub",
                    fontFamily = AppMainFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = (17 * config.fontSizeModifier).sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "🇾🇪", fontSize = 16.sp)
            }

            // Right block: Custom Arabic Navigation Icons ordered RTL: Home (🏠), Login (🔐), Registration (👤), Language (🌐), Refresh (🔄)
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Icon 🏠
                IconButton(onClick = { viewModel.navigateTo("home") }) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "الدليل",
                        tint = if (currentScreen == "home") theme.secondary else Color.White
                    )
                }

                // Login Icon 🔐
                IconButton(onClick = { viewModel.navigateTo("login") }) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "الدخول",
                        tint = if (currentScreen == "login" || currentScreen == "admin") theme.secondary else Color.White
                    )
                }

                // Register Professional Icon 👤
                IconButton(onClick = { viewModel.navigateTo("register") }) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "التسجيل",
                        tint = if (currentScreen == "register") theme.secondary else Color.White
                    )
                }

                // Language Toggle Icon 🌐
                IconButton(onClick = { viewModel.toggleLanguage() }) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "اللغة",
                        tint = Color.White
                    )
                }

                // Refresh state Icon 🔄
                IconButton(onClick = {
                    Toast.makeText(context, if (currentLanguage == "ar") "جاري مزامنة وتحديث البيانات... 🔄" else "Syncing data in real-time... 🔄", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "تحديث",
                        tint = Color.White
                    )
                }
            }
        }
    }

    // Covert Backdoor Authorization Dialog Box
    if (showBackdoorPinDialog) {
        AlertDialog(
            onDismissRequest = { 
                showBackdoorPinDialog = false
                backdoorPinInput = ""
            },
            title = {
                Text(
                    text = if (currentLanguage == "ar") "رمز التحقق السحابي للمشرفين 🔐" else "Admin Security Verification",
                    fontFamily = AppMainFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (currentLanguage == "ar") "يرجى كتابة رمز المصادقة السري للمالك لتجاوز نظام الحماية والعبور بالإعدادات:" else "Please enter the secret code to authenticate:",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                    OutlinedTextField(
                        value = backdoorPinInput,
                        onValueChange = { backdoorPinInput = it },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        placeholder = { Text("رمز الدخول (PIN)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { rememberMeChecked = !rememberMeChecked }
                    ) {
                        Checkbox(
                            checked = rememberMeChecked,
                            onCheckedChange = { rememberMeChecked = it }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (currentLanguage == "ar") "تذكر الحساب لتسجيل الدخول السريع لاحقاً" else "Remember login",
                            fontSize = 11.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                    onClick = {
                        val verified = viewModel.authenticateBackdoor(backdoorPinInput.trim(), rememberMeChecked)
                        if (verified) {
                            showBackdoorPinDialog = false
                            viewModel.navigateTo("backdoor")
                            Toast.makeText(context, "أهلاً بك يا ماهر بالبوابة السرية للمالك 🏠🇸🇦", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "الرمز المدخل غير صحيح لتخطي قفل المصادقة ❌", Toast.LENGTH_LONG).show()
                        }
                        backdoorPinInput = ""
                    }
                ) {
                    Text("عبور ومصادقة", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showBackdoorPinDialog = false
                    backdoorPinInput = ""
                }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
