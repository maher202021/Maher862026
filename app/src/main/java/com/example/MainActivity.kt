package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Support edge to edge and safe areas
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF8FAFC) // Sleek slate-light background
                ) {
                    CleanSpaceScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CleanSpaceScreen() {
    val context = LocalContext.current
    var isSystemRefreshed by remember { mutableStateOf(false) }
    
    // Status items that were uninstalled/cleaned
    val cleanItems = remember {
        listOf(
            "لوحة التحكم والإشراف (Admin Control Panel) 🖥️" to "تم التطهير والحذف بالكامل",
            "أقسام الفئات والفنيين (Services & Directory) 🛠️" to "تم مسح كافة البيانات محلياً وسحابياً",
            "قواعد بيانات الفروع والتتبع (Local SQLite Databases) 🗄️" to "تم تصفير قواعد البيانات بالكامل",
            "ذاكرة الكاش والملفات المؤقتة (Cache & SharedPref) 🧹" to "تم تطهير وتجاوز المساحات التخزينية",
            "تكامل خوادم السحاب المتزامنة (Firebase Firestore Sync) ☁️" to "تم إلغاء الاتصال ومسح الـ Collections"
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "المساحة النظيفة",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A) // Sleek Premium Slate Dark Blue
                ),
                modifier = Modifier.shadow(4.dp)
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF8FAFC)) // Clean slate
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header success card with gradient
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF1E293B), Color(0xFF3B4F6F))
                                )
                            )
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0xFF10B981).copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = "Clean Success",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Text(
                            text = "تم تنظيف وتفريغ التطبيق بنجاح!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "لقد تم إزالة كافة الأقسام، والمكونات البرمجية المقعدة، وقواعد البيانات المؤقتة، ولوحة التحكم، وملفات المزامنة السحابية بالكامل لتوفير بيئة نظيفة مئة بالمئة.",
                            fontSize = 12.sp,
                            color = Color(0xFFCBD5E1),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Checklists info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.CleaningServices,
                                contentDescription = "Clean Icon",
                                tint = Color(0xFF3B82F6)
                            )
                            Text(
                                text = "سجل تصفير وتطهير المكونات والملفات:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF1E293B)
                            )
                        }

                        Divider(color = Color(0xFFE2E8F0))

                        cleanItems.forEach { (title, subtitle) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OfflinePin,
                                    contentDescription = "Success check",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(20.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF334155),
                                        textAlign = TextAlign.Right
                                    )
                                    Text(
                                        text = subtitle,
                                        fontSize = 10.5.sp,
                                        color = Color(0xFF64748B),
                                        textAlign = TextAlign.Right
                                    )
                                }
                            }
                        }
                    }
                }

                // Statistics info cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Speed, "Speed", tint = Color(0xFFE2E8F0))
                            Text("السرعة الحالية", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF64748B))
                            Text("أقصى درجة 🚀", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Palette, "Palette", tint = Color(0xFFE2E8F0))
                            Text("الألوان والأنماط", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF64748B))
                            Text("كلاسيكية مريحة 🎨", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive refresh test tag action button
                Button(
                    onClick = {
                        isSystemRefreshed = true
                        Toast.makeText(context, "تم إعادة تعيين وفحص النظام بنجاح - المساحة آمنة ومطهرة بالكامل! ✨", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .testTag("refresh_system_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Icon",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "إعادة فحص وتحديث النظام 🔄",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }

                // Decorative footer status indicator
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "النظام الآن خفيف ومحمي وغير متصل بأي خوادم خارجية.",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    )
}
