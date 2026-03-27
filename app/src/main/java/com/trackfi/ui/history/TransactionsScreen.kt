package com.trackfi.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.draw.blur
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.border
import androidx.hilt.navigation.compose.hiltViewModel
import com.trackfi.data.local.TransactionEntity
import com.trackfi.ui.theme.bounceClick
import com.trackfi.ui.home.TransactionDetailsBottomSheet
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Security
import com.trackfi.ui.theme.glassMorphism
import com.trackfi.ui.theme.glowEffect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: TransactionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            // New design header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f))
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { /* Handle Back Navigation internally or via param */ }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RIVAVA",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp,
                            color = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary))),
                        contentAlignment = Alignment.Center
                    ) {
                        coil.compose.AsyncImage(
                            model = "https://lh3.googleusercontent.com/aida-public/AB6AXuAsBHwd6-XF0ZxqFSBzCChJAbDHzMPf5YUJSjKN-uGTbmT7_ZbwcBQqdKr5dzurY9ECkBgWxvezCG5wNTj5kMPl_mKB6r5SZUPQ5JPpY47O6Mb8t7cx4_1zxwpj2kU200A2hAAtIHxvPx8zV0uhTwBt70GnMV6WuJxmuhAf6eZfqkAdrxdpXXk_2owm39s0wFEc2u0ui2GozziYlcN3UeS8oZnBNo2gOO9i-oN34O3ktyvC_BTBef9C-pYco-8u6kS4bqddzAa1RxAJ",
                            contentDescription = "Profile",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                    IconButton(onClick = { /* Handle Notifications */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.primaryContainer)
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Search with embedded Sort icon
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                placeholder = { Text("Search merchants, categories, amounts...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                            }
                        }
                        Box {
                            IconButton(onClick = { sortMenuExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = "Sort",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            DropdownMenu(
                                expanded = sortMenuExpanded,
                                onDismissRequest = { sortMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Date (Newest)") },
                                    onClick = {
                                        viewModel.onSortOrderChanged(SortOrder.DATE_DESC)
                                        sortMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Date (Oldest)") },
                                    onClick = {
                                        viewModel.onSortOrderChanged(SortOrder.DATE_ASC)
                                        sortMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Amount (High)") },
                                    onClick = {
                                        viewModel.onSortOrderChanged(SortOrder.AMOUNT_DESC)
                                        sortMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Amount (Low)") },
                                    onClick = {
                                        viewModel.onSortOrderChanged(SortOrder.AMOUNT_ASC)
                                        sortMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Merchant") },
                                    onClick = {
                                        viewModel.onSortOrderChanged(SortOrder.MERCHANT_ASC)
                                        sortMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Category") },
                                    onClick = {
                                        viewModel.onSortOrderChanged(SortOrder.CATEGORY_ASC)
                                        sortMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

        when (val state = uiState) {
            is TransactionsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is TransactionsUiState.Empty -> {
                TransactionsEmptyState()
            }
            is TransactionsUiState.Success -> {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    TransactionList(transactions = state.transactions, viewModel = viewModel)
                }
            }
            is TransactionsUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TransactionList(transactions: List<TransactionEntity>, viewModel: TransactionsViewModel) {
    val showDetails by viewModel.showSmsDetails.collectAsState()
    var selectedTransactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }
    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var transactionToView by remember { mutableStateOf<TransactionEntity?>(null) }

    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        items(transactions, key = { it.id }) { transaction ->
            val swipeableState = rememberSwipeToDismissBoxState(
                confirmValueChange = { state ->
                    if (state == SwipeToDismissBoxValue.StartToEnd) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedTransactionToEdit = transaction
                        false // don't dismiss
                    } else {
                        false
                    }
                }
            )

            SwipeToDismissBox(
                state = swipeableState,
                backgroundContent = {
                    val color = if (swipeableState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    } else {
                        Color.Transparent
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp))
                            .background(color)
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (swipeableState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                },
                content = {
                    TransactionItem(
                        transaction = transaction,
                        showDetails = showDetails,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            transactionToView = transaction
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            transactionToDelete = transaction
                        }
                    )
                }
            )
        }
    }

    val categories by viewModel.categories.collectAsState()

    selectedTransactionToEdit?.let { transaction ->
        TransactionCategoryBottomSheet(
            transaction = transaction,
            customCategories = categories,
            onDismiss = { selectedTransactionToEdit = null },
            onSave = { updatedTransaction, createRule, ruleKeyword ->
                viewModel.updateTransaction(updatedTransaction, createRule, ruleKeyword)
                selectedTransactionToEdit = null
            },
            onAddCategory = { name, type ->
                viewModel.addCategory(name, type)
            }
        )
    }

    transactionToView?.let { transaction ->
        TransactionDetailsBottomSheet(
            transaction = transaction,
            onDismiss = { transactionToView = null },
            onEdit = {
                transactionToView = null
                selectedTransactionToEdit = transaction
            },
            onDelete = {
                transactionToView = null
                transactionToDelete = transaction
            }
        )
    }

    if (transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Delete Transaction") },
            text = { Text("Are you sure you want to delete this transaction? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(transactionToDelete!!)
                        transactionToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TransactionsEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Visual Anchor: Asymmetric Glass Card with Neon Accents
        Box(
            modifier = Modifier
                .size(256.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background decorative elements
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                    .blur(60.dp)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(128.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f), CircleShape)
                    .blur(40.dp)
            )

            // Central Iconography
            Box(
                modifier = Modifier
                    .size(192.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
                    .rotate(3f)
                    .glassMorphism(cornerRadius = 24f, alpha = 0.1f, strokeAlpha = 0.08f)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                // Floating wallet icon top left
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = (-24).dp, y = (-24).dp)
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .rotate(-12f),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(width = 32.dp, height = 4.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape))
                        Box(modifier = Modifier.size(width = 48.dp, height = 4.dp).background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), CircleShape))
                        Box(modifier = Modifier.size(width = 24.dp, height = 4.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), CircleShape))
                    }
                }

                // "No Data" floating badge bottom right
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 8.dp, y = 24.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(percent = 50))
                        .rotate(6f)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NO DATA",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Typography Cluster
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "History",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No transactions found. Your spending story starts with your first swipe.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Contextual Action
        Button(
            onClick = { /* TODO: Navigate to trade/add */ },
            modifier = Modifier
                .padding(top = 16.dp)
                .height(56.dp)
                .bounceClick { },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary)
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .glowEffect(color = MaterialTheme.colorScheme.primaryContainer, radius = 20f, isSelected = true)
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Go to Trade",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Secondary Information / Suggestions (Bento Style Hints)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Perks
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    .glassMorphism(cornerRadius = 16f, alpha = 0.05f)
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = "Perks",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "PERKS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Discover rewards and cashback offers while you wait.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Security
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    .glassMorphism(cornerRadius = 16f, alpha = 0.05f)
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Security",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "SECURITY",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Manage your card and spending limits anytime.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun TransactionItem(transaction: TransactionEntity, showDetails: Boolean = true, onClick: () -> Unit, onLongClick: () -> Unit) {
    val isCredit = transaction.type == "INCOME" || transaction.type == "REWARD"

    val catVisual = if (transaction.subcategory != null)
        com.trackfi.ui.theme.CategoryVisuals.getSubcategoryVisual(transaction.subcategory)
    else
        com.trackfi.ui.theme.CategoryVisuals.getCategoryVisual(transaction.category)

    val iconColor = catVisual.color

    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .bounceClick { onClick() }
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconColor.copy(alpha = 0.2f), androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = catVisual.icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (showDetails) transaction.merchantName else "Hidden",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val dateString = if (showDetails) formatter.format(Date(transaction.date)) else "****"

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (showDetails) catVisual.title else "Hidden",
                        style = MaterialTheme.typography.bodyMedium,
                        color = iconColor,
                        modifier = Modifier
                            .background(iconColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dateString,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "${if (isCredit) "+" else "-"}₹${String.format(Locale.getDefault(), "%.0f", transaction.amount)}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isCredit) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
            )
        }
    }
}
