package com.example.neuroed

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.android.billingclient.api.ProductDetails
import com.example.neuroed.MONTHLY_SUBSCRIPTION_ID
import com.example.neuroed.SubscriptionManager
import com.example.neuroed.YEARLY_SUBSCRIPTION_ID
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun SubscriptionScreen(
    navController: NavController,
    onContinueClick: () -> Unit = {},
    onRestorePurchaseClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Create subscription manager
    val subscriptionManager = remember {
        SubscriptionManager(context as android.app.Activity, lifecycleOwner.lifecycleScope)
    }

    // Collect subscription data
    val subscriptionPlans by subscriptionManager.subscriptionPlans.collectAsState()
    val activePurchases by subscriptionManager.activePurchases.collectAsState()
    val purchaseInProgress by subscriptionManager.purchaseInProgress.collectAsState()
    val purchaseComplete by subscriptionManager.purchaseComplete.collectAsState()

    // UI state variables
    var showCancelDialog by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    // Observe purchase completion
    LaunchedEffect(purchaseComplete) {
        if (purchaseComplete) {
            showSuccessMessage = true
            delay(3000)
            showSuccessMessage = false
        }
    }

    // Check for subscriptions when screen opens
    LaunchedEffect(Unit) {
        subscriptionManager.queryActivePurchases()
    }

    // Animation for screen content
    val screenContentAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(500, easing = FastOutSlowInEasing)
    )

    // Determine if user has subscriptions
    val hasMonthlySubscription = activePurchases.any { purchase ->
        purchase.products.contains(MONTHLY_SUBSCRIPTION_ID)
    }

    val hasYearlySubscription = activePurchases.any { purchase ->
        purchase.products.contains(YEARLY_SUBSCRIPTION_ID)
    }

    // कोई भी सब्सक्रिप्शन एक्टिव है या नहीं
    val hasActiveSubscription = hasMonthlySubscription || hasYearlySubscription

    Log.d("SubscriptionScreen", "Plans count: ${subscriptionPlans.size}")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Subscription",
                        modifier = Modifier.graphicsLayer { alpha = screenContentAlpha }
                    )
                },
                navigationIcon = {
                    val buttonScale = remember { Animatable(0.8f) }
                    LaunchedEffect(Unit) {
                        buttonScale.animateTo(
                            1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    }
                    IconButton(
                        onClick = { navController.navigateUp() },
                        modifier = Modifier
                            .scale(buttonScale.value)
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main content with ScrollState
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState)  // 스크롤 가능하게 만들기
                    .graphicsLayer {
                        alpha = screenContentAlpha
                        translationY = (1f - screenContentAlpha) * 50f
                    },
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(Modifier.height(8.dp)) // Top spacing

                // No Active Plan screen if no subscription is active
                if (!hasActiveSubscription) {
                    NoActiveSubscriptionCard()
                } else {
                    // Active subscription card if any subscription is active
                    ActiveSubscriptionCard(
                        isMonthly = hasMonthlySubscription,
                        onCancelClick = { showCancelDialog = true }
                    )
                }

                // Always show subscription options
                MockSubscriptionOptionsSection(
                    onSubscribeClick = { productId ->
                        scope.launch {
                            subscriptionManager.launchSubscriptionFlow(productId)
                        }
                    }
                )

                // Bottom buttons (Restore Purchases)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp, top = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    var restorePressed by remember { mutableStateOf(false) }
                    val restoreScale by animateFloatAsState(
                        if (restorePressed) 0.95f else 1f,
                        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                    )

                    OutlinedButton(
                        onClick = {
                            restorePressed = true
                            scope.launch {
                                delay(200)
                                restorePressed = false
                                subscriptionManager.restorePurchases()
                            }
                        },
                        Modifier.scale(restoreScale),
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder,
                        enabled = !purchaseInProgress
                    ) {
                        Text(
                            "Restore Purchases",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp)) // Bottom spacing
            }

            // Purchase in progress overlay
            AnimatedVisibility(
                visible = purchaseInProgress,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        Modifier.padding(16.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text("Processing your purchase...", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }

            // Success message snackbar
            AnimatedVisibility(
                visible = showSuccessMessage,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Snackbar(
                    Modifier.padding(bottom = 16.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text("Subscription activated successfully!")
                }
            }
        }
    }

    // Cancel subscription dialog
    AnimatedVisibility(
        visible = showCancelDialog,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        CancelSubscriptionDialog(
            onDismiss = { showCancelDialog = false },
            onConfirm = {
                // Here you would redirect to Google Play subscription management
                // As per Google Play policy, subscriptions must be managed through Google Play
                showCancelDialog = false

                // Try to open Google Play subscription management
                val mainActivity = context as? MainActivity
                mainActivity?.openSubscriptionManagement()
            }
        )
    }

    // Cleanup subscription manager when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            subscriptionManager.endConnection()
        }
    }
}

// Updated No Active Subscription Card Component with Free Plan Features
@Composable
fun NoActiveSubscriptionCard() {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )

            Text(
                "Currently on Free Plan",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                "You're using the basic free version. Upgrade to a premium plan for advanced features.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Add free plan features
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Free Plan Features:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("Basic AI assistance", style = MaterialTheme.typography.bodyMedium)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("Limited learning sessions", style = MaterialTheme.typography.bodyMedium)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("Standard tracking", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

// Updated Subscription Options Section with Enhanced Features
@Composable
fun MockSubscriptionOptionsSection(
    onSubscribeClick: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "Choose Your Plan",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Yearly subscription option
        MockSubscriptionCard(
            title = "Pro Yearly",
            price = "₹11,999/year",
            benefits = listOf(
                "Save 17% compared to monthly",
                "Better memory AI",
                "High reasoning capabilities",
                "Advanced tracking",
                "Advanced tools search",
                "Advanced learning AI",
                "Priority support"
            ),
            isRecommended = true,
            onClick = { onSubscribeClick(YEARLY_SUBSCRIPTION_ID) }
        )

        // Monthly subscription option - Now also highlighted
        MockSubscriptionCard(
            title = "Pro Monthly",
            price = "₹999/month",
            benefits = listOf(
                "Better memory AI",
                "High reasoning capabilities",
                "Advanced tracking",
                "Advanced tools search",
                "Advanced learning AI",
                "Priority support"
            ),
            isRecommended = true, // Now also highlighted like the yearly plan
            isMonthly = true, // Added to distinguish from yearly plan
            onClick = { onSubscribeClick(MONTHLY_SUBSCRIPTION_ID) }
        )
    }
}

// Updated Subscription Card with Monthly Plan Highlighting
@Composable
fun MockSubscriptionCard(
    title: String,
    price: String,
    benefits: List<String>,
    isRecommended: Boolean = false,
    isMonthly: Boolean = false, // New parameter to distinguish monthly from yearly
    onClick: () -> Unit
) {
    val scope = rememberCoroutineScope()

    Card(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRecommended) {
                if (isMonthly) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
            } else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isRecommended) {
                            if (isMonthly) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                        } else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        price,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (isRecommended) {
                            if (isMonthly) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                        } else MaterialTheme.colorScheme.onSurface
                    )
                }

                if (isRecommended && !isMonthly) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(
                            "BEST VALUE",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(12.dp, 6.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                if (isMonthly) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(
                            "POPULAR",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(12.dp, 6.dp),
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            benefits.forEach { benefit ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isMonthly) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        benefit,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isRecommended) {
                            if (isMonthly) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                        } else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onClick,
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isMonthly) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    if (isRecommended) "Subscribe Now" else "Select Plan",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ActiveSubscriptionCard(
    isMonthly: Boolean,
    onCancelClick: () -> Unit
) {
    var elevation by remember { mutableStateOf(4.dp) }
    val animatedElevation by animateDpAsState(elevation, tween(300))

    Card(
        Modifier
            .fillMaxWidth()
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable { elevation = if (elevation == 4.dp) 8.dp else 4.dp },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation)
    ) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                "Active Subscription",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Divider(thickness = 1.dp)

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        if (isMonthly) "Pro Monthly" else "Pro Yearly",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Calculate next billing date (example)
                    val nextBillingDate = "June 12, 2025" // This should be calculated based on purchase data

                    Text(
                        "Next billing date: $nextBillingDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isMonthly) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        if (isMonthly) "₹999/month" else "₹11,999/year",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(12.dp, 6.dp),
                        color = if (isMonthly) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = onCancelClick,
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(
                    "Manage Subscription",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun CancelSubscriptionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cancel Subscription", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Text("Your subscription is managed through Google Play. You'll be redirected to Google Play to manage your subscription.")
        },
        confirmButton = {
            var pressed by remember { mutableStateOf(false) }
            val scale by animateFloatAsState(
                if (pressed) 0.95f else 1f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            )
            Button(
                onClick = {
                    pressed = true
                    scope.launch {
                        delay(200)
                        pressed = false
                        onConfirm()
                    }
                },
                Modifier.scale(scale)
            ) {
                Text("Go to Google Play")
            }
        },
        dismissButton = {
            var pressed by remember { mutableStateOf(false) }
            val scale by animateFloatAsState(
                if (pressed) 0.95f else 1f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            )
            OutlinedButton(
                onClick = {
                    pressed = true
                    scope.launch {
                        delay(200)
                        pressed = false
                        onDismiss()
                    }
                },
                Modifier.scale(scale)
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}