package com.rivavafi.universal.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.rivavafi.universal.HomeActivity
import com.rivavafi.universal.R
import com.rivavafi.universal.ui.theme.*
import androidx.compose.ui.draw.shadow
import com.rivavafi.universal.ui.theme.glassMorphism
import com.rivavafi.universal.ui.components.RivavaBrandDisplay
import com.rivavafi.universal.ui.components.RivavaLoadingOverlay
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import android.util.Patterns
import androidx.compose.foundation.shape.CircleShape

@AndroidEntryPoint
class AuthActivity : ComponentActivity() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // We now rely on AuthViewModel's init block and LaunchedEffect(authState)
        // to handle the redirection safely after verifying backend status.

        handleIntent(intent)

        setContent {
            TrackFiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AmoledBlack
                ) {
                    AuthScreenContent(
                        viewModel = viewModel,
                        onLoginSuccess = { isNewUser -> goToHome(isNewUser) },
                        onNavigateToReset = { goToResetPassword() }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleIntent(it) }
    }

    private fun handleIntent(intent: Intent) {
        val action = intent.action
        val data = intent.data

        if (Intent.ACTION_VIEW == action && data != null) {
            val mode = data.getQueryParameter("mode")
            val oobCode = data.getQueryParameter("oobCode")

            if (oobCode != null) {
                when (mode) {
                    "verifyEmail" -> {
                        viewModel.verifyEmailActionCode(oobCode) {
                            Toast.makeText(this, "Email verified successfully!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    "resetPassword" -> {
                        val resetIntent = Intent(this, SetNewPasswordActivity::class.java).apply {
                            putExtra("oobCode", oobCode)
                        }
                        startActivity(resetIntent)
                    }
                    else -> {
                        // Fallback for paths if mode is not explicitly passed by Firebase
                        if (data.path?.contains("/verify") == true) {
                            viewModel.verifyEmailActionCode(oobCode) {
                                Toast.makeText(this, "Email verified successfully!", Toast.LENGTH_SHORT).show()
                            }
                        } else if (data.path?.contains("/reset") == true) {
                            val resetIntent = Intent(this, SetNewPasswordActivity::class.java).apply {
                                putExtra("oobCode", oobCode)
                            }
                            startActivity(resetIntent)
                        }
                    }
                }
            }
        }
    }

    private fun goToHome(isNewUser: Boolean) {
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("isNewUser", isNewUser)
        }
        startActivity(intent)
        finish()
    }

    private fun goToResetPassword() {
        val intent = Intent(this, ResetPasswordActivity::class.java)
        startActivity(intent)
    }
}

@Composable
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
fun AuthScreenContent(
    viewModel: AuthViewModel,
    onLoginSuccess: (Boolean) -> Unit,
    onNavigateToReset: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val phoneAuthState by viewModel.phoneAuthState.collectAsState()
    val isNewUser by viewModel.isNewUser.collectAsState()

    var showPhoneConfirmDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var authMethod by remember { mutableStateOf("INITIAL") }
    var phoneNumber by remember { mutableStateOf("") }


    BackHandler(enabled = authState == AuthState.LOADING) {
        viewModel.resetState()
    }


    LaunchedEffect(phoneAuthState) {
        if (phoneAuthState == PhoneAuthState.CODE_SENT) {
        }
    }

    LaunchedEffect(authState) {

        if (authState == AuthState.SUCCESS) {
            onLoginSuccess(isNewUser == true)
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            if (it.isNotEmpty()) {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.setErrorMessage("") // Clear after showing
            }
        }
    }
    var tempName by remember { mutableStateOf("") }
    var tempEmail by remember { mutableStateOf("") }
    var tempPhotoUrl by remember { mutableStateOf("") }

    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null && account.idToken != null) {
                // Verify with Firebase
                viewModel.onGoogleSignInSuccess(
                    idToken = account.idToken!!,
                    name = account.displayName ?: "User",
                    email = account.email ?: "",
                    photoUrl = account.photoUrl?.toString() ?: ""
                )
                tempName = account.displayName ?: "User"
                tempEmail = account.email ?: ""
                tempPhotoUrl = account.photoUrl?.toString() ?: ""
                authMethod = "PROFILE_COMPLETION"
            } else {
                viewModel.setErrorMessage("Sign-in failed: ID Token is null")
            }
        } catch (e: ApiException) {
            viewModel.setErrorMessage("Google Sign-in failed (Code: ${e.statusCode}): ${e.message}")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Moving Image Carousel
            val images = listOf(
                "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1590283603385-17ffb3a7f29f?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1460925895917-afdab827c52f?auto=format&fit=crop&w=800&q=80"
            )
            val titles = listOf("Track smarter", "Invest better", "Grow faster")

            val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { images.size })

            LaunchedEffect(Unit) {
                while (true) {
                    kotlinx.coroutines.delay(3000)
                    val nextPage = (pagerState.currentPage + 1) % images.size
                    pagerState.animateScrollToPage(nextPage)
                }
            }

            val cachedUser = remember { viewModel.getCachedUser() }
            val auth = FirebaseAuth.getInstance()
            val showCachedUser = auth.currentUser == null && cachedUser != null

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.4f)
            ) {
                androidx.compose.foundation.pager.HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        coil.compose.AsyncImage(
                            model = images[page],
                            contentDescription = null,
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color(0xFF0A0A0A)),
                                        startY = 100f
                                    )
                                )
                        )
                        Text(
                            text = titles[page],
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            ),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (showCachedUser && cachedUser != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    if (cachedUser.photo != null) {
                        coil.compose.AsyncImage(
                            model = cachedUser.photo,
                            contentDescription = "Profile Photo",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.AccountCircle,
                            contentDescription = "Profile Photo",
                            modifier = Modifier.size(64.dp),
                            tint = Color.White.copy(alpha = 0.5f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Last logged in as ${cachedUser.name ?: "User"}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.7f))
                    )
                }
            }

            // Glassmorphism Login Container
            androidx.compose.material3.Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color(0xFF111111)),
                elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )

                    RivavaBrandDisplay(showQuote = true)

                    Spacer(modifier = Modifier.height(16.dp))




                    if (showPhoneConfirmDialog) {
                        AlertDialog(
                            onDismissRequest = { showPhoneConfirmDialog = false },
                            title = {
                                Text(text = "Confirm Phone Number", fontWeight = FontWeight.Bold)
                            },
                            text = {
                                Column {
                                    Text(text = "Is this your correct phone number?", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = phoneNumber, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PrimarySky)
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    showPhoneConfirmDialog = false
                                    viewModel.startPhoneVerification(phoneNumber) {
                                        // Handled by launched effect
                                    }
                                }) {
                                    Text("YES CONTINUE", fontWeight = FontWeight.Bold, color = PrimarySky)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showPhoneConfirmDialog = false }) {
                                    Text("EDIT NUMBER", color = Color.Gray)
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            containerColor = AmoledBlack,
                            titleContentColor = Color.White,
                            textContentColor = Color.White
                        )
                    }


                    if (authMethod == "PROFILE_COMPLETION") {
                        var preference by remember { mutableStateOf("Rivava Portfolio") }
                        var showConfirmDialog by remember { mutableStateOf(false) }

                        Text(
                            text = "Complete Profile",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = tempName,
                            onValueChange = { tempName = it },
                            label = { Text("Full Name", color = Color.White.copy(0.7f)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = Color.White.copy(0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )

                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = {
                                val filtered = it.filter { char -> char.isDigit() }
                                if (filtered.length <= 10) phoneNumber = filtered
                            },
                            label = { Text("Phone Number", color = Color.White.copy(0.7f)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = Color.White.copy(0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Text("+91", modifier = Modifier.padding(start = 16.dp, end = 8.dp), color = Color.White)
                            },
                            shape = RoundedCornerShape(16.dp)
                        )

                        // Dropdown for Preference
                        var expanded by remember { mutableStateOf(false) }
                        val options = listOf("Rivava Portfolio", "Rivava Elite", "Both")

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = preference,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Preference", color = Color.White.copy(0.7f)) },
                                trailingIcon = {
                                    IconButton(onClick = { expanded = !expanded }) {
                                        Icon(if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown, "dropdown", tint=Color.White)
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF3B82F6),
                                    unfocusedBorderColor = Color.White.copy(0.2f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            )
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(Color(0xFF131313))
                            ) {
                                options.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption, color = Color.White) },
                                        onClick = {
                                            preference = selectionOption
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (phoneNumber.length == 10 && tempName.isNotBlank()) {
                                    showConfirmDialog = true
                                } else {
                                    Toast.makeText(context, "Please enter a valid 10-digit number and name.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = authState != AuthState.LOADING,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34D399), contentColor = Color.White),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("Continue", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                        }

                        if (showConfirmDialog) {
                            AlertDialog(
                                onDismissRequest = { showConfirmDialog = false },
                                title = { Text("Confirm Number", fontWeight = FontWeight.Bold) },
                                text = {
                                    Column {
                                        Text("Is this your correct phone number?", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("+91 $phoneNumber", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PrimarySky)
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = {
                                        showConfirmDialog = false
                                        viewModel.saveUserProfileCompletion(
                                            name = tempName,
                                            phone = "+91$phoneNumber",
                                            preference = preference,
                                            email = tempEmail,
                                            photoUrl = tempPhotoUrl
                                        ) { isNew ->
                                            onLoginSuccess(isNew)
                                        }
                                    }) {
                                        Text("Yes, Continue", fontWeight = FontWeight.Bold, color = PrimarySky)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showConfirmDialog = false }) {
                                        Text("Edit Number", color = Color.Gray)
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                containerColor = AmoledBlack,
                                titleContentColor = Color.White,
                                textContentColor = Color.White
                            )
                        }
                    } else if (authMethod == "INITIAL") {

                        // Google Login Button
                        Button(
                            onClick = {
                                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestIdToken(context.getString(R.string.default_web_client_id))
                                    .requestEmail()
                                    .build()
                                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                                googleSignInClient.signOut().addOnCompleteListener {
                                    launcher.launch(googleSignInClient.signInIntent)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .shadow(elevation = 12.dp, spotColor = Color.White, shape = RoundedCornerShape(20.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
                            shape = RoundedCornerShape(20.dp),
                            enabled = authState != AuthState.LOADING
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AccountCircle,
                                contentDescription = "Google",
                                modifier = Modifier.size(24.dp),
                                tint = AmoledBlack
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Continue with Google", color = AmoledBlack, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Phone Login Button
                        Button(
                            onClick = { authMethod = "PHONE" },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(20.dp),
                            enabled = authState != AuthState.LOADING
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Phone,
                                contentDescription = "Phone",
                                modifier = Modifier.size(24.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Continue with Phone", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    } else if (authMethod == "PHONE") {
                        Text(
                            text = "Phone Sign In",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Phone Number", color = Color.White.copy(0.7f)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = Color.White.copy(0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Text("+91", modifier = Modifier.padding(start = 16.dp, end = 8.dp), color = Color.White)
                            },
                            shape = RoundedCornerShape(16.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val normalized = viewModel.normalizePhoneNumber(phoneNumber)
                                if (normalized != null) {
                                    phoneNumber = normalized
                                    showPhoneConfirmDialog = true
                                } else {
                                    Toast.makeText(context, "Invalid phone number format.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = authState != AuthState.LOADING,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34D399), contentColor = Color.White),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 1.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            if (authState == AuthState.LOADING) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text("Send OTP", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(onClick = { authMethod = "INITIAL" }) {
                            Text("Back to options", color = Color.White.copy(alpha = 0.7f))
                        }
                    }

                }
            }
        }

        RivavaLoadingOverlay(isLoading = authState == AuthState.LOADING)
    }
}
