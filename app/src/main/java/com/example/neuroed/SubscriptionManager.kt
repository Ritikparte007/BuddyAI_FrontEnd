package com.example.neuroed

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

const val MONTHLY_SUBSCRIPTION_ID = "neuroed.sub.monthly"
const val YEARLY_SUBSCRIPTION_ID = "neuroed.sub.yearly"

class SubscriptionManager(
    private val activity: Activity,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val useMockData: Boolean = true // टेस्टिंग के लिए डिफॉल्ट मॉक मोड सक्षम है
) : PurchasesUpdatedListener {

    private val tag = "SubscriptionManager"

    // State flows for UI updates
    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    private val _subscriptionPlans = MutableStateFlow<List<ProductDetails>>(emptyList())
    val subscriptionPlans = _subscriptionPlans.asStateFlow()

    private val _activePurchases = MutableStateFlow<List<Purchase>>(emptyList())
    val activePurchases = _activePurchases.asStateFlow()

    private val _purchaseInProgress = MutableStateFlow(false)
    val purchaseInProgress = _purchaseInProgress.asStateFlow()

    private val _purchaseComplete = MutableStateFlow(false)
    val purchaseComplete = _purchaseComplete.asStateFlow()

    private val isInitializing = AtomicBoolean(false)

    private val billingClient = BillingClient.newBuilder(activity)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    init {
        if (useMockData) {
            // यदि मॉक मोड सक्षम है, तो मॉक डेटा के साथ तुरंत फ्लो को अपडेट करें
            loadMockSubscriptionPlans()
        } else {
            // वास्तविक Play Billing से कनेक्ट करें
            connectToPlayBilling()
        }
    }

    // मॉक सब्सक्रिप्शन प्लान लोड करें
    private fun loadMockSubscriptionPlans() {
        Log.d(tag, "Loading mock subscription plans")
        // NOTE: यहां हम वास्तविक ProductDetails ऑब्जेक्ट्स नहीं बना सकते (यह एक फाइनल क्लास है),
        // इसलिए UI को मैन्युअल रूप से मॉक डेटा हैंडल करना चाहिए

        // मॉक डेटा: हम सूचित करते हैं कि हम कनेक्टेड हैं, लेकिन खाली प्लान्स भेजते हैं
        _isConnected.value = true

        // रिक्त प्लान्स रखें - UI को मैन्युअल रूप से मॉक UI प्रदर्शित करना चाहिए
        // _subscriptionPlans.value = emptyList()
    }

    fun connectToPlayBilling() {
        if (useMockData) {
            // मॉक मोड में नहीं कनेक्ट करते
            return
        }

        if (isInitializing.getAndSet(true)) {
            return
        }

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(tag, "Billing client connected")
                    _isConnected.value = true
                    isInitializing.set(false)

                    // सब्सक्रिप्शन प्लान्स को फेच करें
                    querySubscriptionPlans()

                    // एक्टिव सब्सक्रिप्शन को चेक करें
                    queryActivePurchases()
                } else {
                    Log.e(tag, "Billing client connection failed: ${billingResult.responseCode}")
                    isInitializing.set(false)
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.d(tag, "Billing service disconnected")
                _isConnected.value = false
                isInitializing.set(false)

                // बिलिंग सर्विस से कनेक्शन टूट गया, 5 सेकंड बाद फिर से कनेक्ट करने का प्रयास करें
                lifecycleScope.launch {
                    kotlinx.coroutines.delay(5000)
                    connectToPlayBilling()
                }
            }
        })
    }

    // सभी सब्सक्रिप्शन प्लान्स को फेच करें
    private fun querySubscriptionPlans() {
        if (useMockData) {
            // मॉक मोड - प्लान्स फेच नहीं करते
            return
        }

        if (!isConnected.value) {
            connectToPlayBilling()
            return
        }

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(MONTHLY_SUBSCRIPTION_ID)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(YEARLY_SUBSCRIPTION_ID)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(tag, "Product details query successful, products: ${productDetailsList.size}")
                _subscriptionPlans.value = productDetailsList
            } else {
                Log.e(tag, "Failed to query product details: ${billingResult.responseCode}")
            }
        }
    }

    // उपयोगकर्ता के एक्टिव सब्सक्रिप्शन्स को चेक करें
    fun queryActivePurchases() {
        if (useMockData) {
            // मॉक मोड - कोई एक्टिव सब्सक्रिप्शन नहीं
            _activePurchases.value = emptyList()
            return
        }

        if (!isConnected.value) {
            connectToPlayBilling()
            return
        }

        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(tag, "Active purchases: ${purchasesList.size}")
                processPurchases(purchasesList)
            } else {
                Log.e(tag, "Failed to query purchases: ${billingResult.responseCode}")
            }
        }
    }

    // सब्सक्रिप्शन खरीदने का फ्लो शुरू करें
    fun launchSubscriptionFlow(productId: String) {
        if (useMockData) {
            // मॉक मोड - सिम्युलेटेड खरीदी (बस सफल हो जाती है)
            _purchaseInProgress.value = true

            // सिम्युलेटेड प्रोसेसिंग दिखाने के लिए थोड़ा डिले
            lifecycleScope.launch {
                kotlinx.coroutines.delay(1500)
                _purchaseInProgress.value = false
                _purchaseComplete.value = true

                // बाद में प्रोग्रेस रीसेट करें
                kotlinx.coroutines.delay(3000)
                _purchaseComplete.value = false
            }
            return
        }

        // पहले संबंधित प्रोडक्ट डिटेल्स को फाइंड करें
        val productDetails = subscriptionPlans.value.find { it.productId == productId }

        if (productDetails == null) {
            Log.e(tag, "Product details not found for $productId")
            return
        }

        // प्रोडक्ट की सब्सक्रिप्शन ऑफर डिटेल्स प्राप्त करें
        val subscriptionOfferDetails = productDetails.subscriptionOfferDetails

        if (subscriptionOfferDetails.isNullOrEmpty()) {
            Log.e(tag, "No subscription offer details available for $productId")
            return
        }

        // सामान्य तौर पर हम पहला base plan उपयोग करेंगे
        val selectedOfferToken = subscriptionOfferDetails[0].offerToken

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(selectedOfferToken)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        _purchaseInProgress.value = true

        val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)

        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.e(tag, "Failed to launch billing flow: ${billingResult.responseCode}")
            _purchaseInProgress.value = false
        }
    }

    private fun processPurchases(purchases: List<Purchase>) {
        _activePurchases.value = purchases.filter { purchase ->
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED
        }

        for (purchase in _activePurchases.value) {
            if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(params) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(tag, "Purchase acknowledged: ${purchase.products}")
            } else {
                Log.e(tag, "Failed to acknowledge purchase: ${billingResult.responseCode}")
            }
        }
    }

    fun restorePurchases() {
        if (useMockData) {
            return
        }
        queryActivePurchases()
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        _purchaseInProgress.value = false

        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            processPurchases(purchases)
            _purchaseComplete.value = true

            lifecycleScope.launch {
                kotlinx.coroutines.delay(3000)
                _purchaseComplete.value = false
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(tag, "Purchase canceled by user")
        } else {
            Log.e(tag, "Purchase failed: ${billingResult.responseCode}")
        }
    }

    fun hasActiveSubscription(): Boolean {
        return activePurchases.value.isNotEmpty()
    }

    fun hasSubscription(productId: String): Boolean {
        return activePurchases.value.any { purchase ->
            purchase.products.contains(productId)
        }
    }

    fun endConnection() {
        if (!useMockData) {
            billingClient.endConnection()
        }
    }
}