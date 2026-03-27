package com.trackfi.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trackfi.ui.home.HomeViewModel
import com.trackfi.ui.theme.EmeraldGreen
import com.trackfi.ui.theme.PremiumGradientStart
import com.trackfi.ui.theme.glassMorphism
import androidx.compose.foundation.border
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val userName by viewModel.userName.collectAsState()
    val isPremiumUser by viewModel.isPremiumUser.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Profile", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color.White)) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.05f))) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.tertiary)
                        ),
                        CircleShape
                    )
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val initial = if (!userName.isNullOrEmpty()) userName!!.first().toString().uppercase() else ""
                    if (initial.isNotEmpty()) {
                        Text(
                            text = initial,
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = userName ?: "Unknown",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = Color.White,
                letterSpacing = (-1).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isPremiumUser) "PREMIUM MEMBER" else "FREE TIER",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.5.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onNavigateToSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .glassMorphism(cornerRadius = 32f, alpha = 0.03f, color = Color.White),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(32.dp),
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("App Settings", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                            Text("Preferences, Security, Data", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
                        }
                    }
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Navigate",
                        tint = Color.White.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}
