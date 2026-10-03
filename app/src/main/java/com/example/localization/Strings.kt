package com.example.localization

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    EN("en", "English", "English"),
    HI("hi", "Hindi", "हिन्दी"),
    HINGLISH("hinglish", "Hinglish", "Hinglish (रोमन हिन्दी)")
}

data class AppStrings(
    // App & Navigation
    val appTitle: String,
    val tabRepairs: String,
    val tabInventory: String,
    val tabCompatibility: String,
    val tabSettings: String,
    val languageSelect: String,

    // Dashboard & Stats
    val pendingRepairs: String,
    val readyPickup: String,
    val lowStockAlerts: String,
    val totalRevenue: String,
    val quickUpiBill: String,
    val newJob: String,
    val addItem: String,

    // Repairs
    val repairJobs: String,
    val searchJobs: String,
    val allJobs: String,
    val statusPending: String,
    val statusInProgress: String,
    val statusReady: String,
    val statusDelivered: String,
    val customerName: String,
    val customerPhone: String,
    val deviceModel: String,
    val issueDescription: String,
    val estimatedAmount: String,
    val paymentStatus: String,
    val unpaid: String,
    val paidUpi: String,
    val paidCash: String,
    val markReady: String,
    val markDelivered: String,
    val generateUpiQr: String,
    val sendWhatsApp: String,
    val viewJobDetails: String,
    val jobCreatedOn: String,
    val partUsed: String,

    // Inventory
    val inventoryTitle: String,
    val searchInventory: String,
    val categoryFilter: String,
    val allCategories: String,
    val inStock: String,
    val lowStock: String,
    val outOfStock: String,
    val costPrice: String,
    val sellingPrice: String,
    val profitMargin: String,
    val minAlertQty: String,
    val compatibleModels: String,
    val stockAlertBanner: String,
    val quickAddStock: String,

    // Compatibility
    val compatibilityTitle: String,
    val compatibilitySubtitle: String,
    val searchPhoneModel: String,
    val sharedLcdFolder: String,
    val sharedChargingJack: String,
    val sharedBattery: String,
    val matchingModels: String,
    val partNotes: String,
    val checkStock: String,

    // UPI & WhatsApp
    val upiBilling: String,
    val enterAmount: String,
    val scanToPay: String,
    val shopUpiId: String,
    val shopName: String,
    val verifyPayment: String,
    val enterTxnId: String,
    val txnIdPlaceholder: String,
    val paymentConfirmed: String,
    val notifyCustomerOnWhatsApp: String,
    val whatsAppMessageSent: String,

    // Settings
    val shopSettings: String,
    val ownerName: String,
    val ownerPhone: String,
    val saveSettings: String,
    val settingsSaved: String,
    val enterValidUpi: String,
    val sampleUpiHint: String,

    // Actions
    val cancel: String,
    val save: String,
    val edit: String,
    val delete: String,
    val confirm: String,
    val close: String
)

object LocalizationManager {
    private val englishStrings = AppStrings(
        appTitle = "MobiRepair Pro",
        tabRepairs = "Repairs & Bills",
        tabInventory = "Inventory",
        tabCompatibility = "Part Matcher",
        tabSettings = "Shop & UPI",
        languageSelect = "Language",
        pendingRepairs = "Pending Jobs",
        readyPickup = "Ready for Delivery",
        lowStockAlerts = "Low Stock Alerts",
        totalRevenue = "Collected Amount",
        quickUpiBill = "Quick UPI QR Bill",
        newJob = "New Repair Job",
        addItem = "Add Spare Part",
        repairJobs = "Repair Tickets",
        searchJobs = "Search by customer name, phone, or model...",
        allJobs = "All",
        statusPending = "Pending",
        statusInProgress = "In Progress",
        statusReady = "Ready for Pickup",
        statusDelivered = "Delivered",
        customerName = "Customer Name",
        customerPhone = "Customer WhatsApp / Mobile",
        deviceModel = "Phone Model (e.g. Realme C2)",
        issueDescription = "Problem / Repair Work",
        estimatedAmount = "Bill Amount (₹)",
        paymentStatus = "Payment Status",
        unpaid = "Unpaid",
        paidUpi = "Paid via UPI",
        paidCash = "Paid in Cash",
        markReady = "Mark Ready (Repair Done)",
        markDelivered = "Mark Delivered",
        generateUpiQr = "Pay via UPI QR",
        sendWhatsApp = "Send WhatsApp Update",
        viewJobDetails = "Job Details",
        jobCreatedOn = "Received On",
        partUsed = "Part Used from Inventory",
        inventoryTitle = "Parts & Spares Inventory",
        searchInventory = "Search parts, batteries, jacks, combos...",
        categoryFilter = "Category",
        allCategories = "All Spares",
        inStock = "In Stock",
        lowStock = "Low Stock",
        outOfStock = "Out of Stock",
        costPrice = "Cost Price (₹)",
        sellingPrice = "Selling Price (₹)",
        profitMargin = "Margin",
        minAlertQty = "Alert Threshold",
        compatibleModels = "Compatible Models (e.g. Oppo A1k, Realme C2)",
        stockAlertBanner = "Low Inventory Alert: Some essential parts are running low!",
        quickAddStock = "Quick Restock",
        compatibilityTitle = "Shared Parts & Compatibility Finder",
        compatibilitySubtitle = "Find identical display folders, charging jacks & batteries across phone models",
        searchPhoneModel = "Type phone model (e.g. Vivo Y12, Redmi Note 7, Realme 5)...",
        sharedLcdFolder = "Display (LCD Combo)",
        sharedChargingJack = "Charging Jack / CC Board",
        sharedBattery = "Battery Model",
        matchingModels = "Shared / Interchangeable Models",
        partNotes = "Technician Notes & Pinout",
        checkStock = "Stock in Inventory",
        upiBilling = "Dynamic UPI QR Payment",
        enterAmount = "Enter Amount (₹)",
        scanToPay = "Scan with any UPI App (GPay, PhonePe, Paytm, BHIM)",
        shopUpiId = "Owner UPI ID (VPA)",
        shopName = "Shop / Business Name",
        verifyPayment = "Confirm UPI Payment",
        enterTxnId = "UPI UTR / Transaction Reference ID",
        txnIdPlaceholder = "e.g. 428190382910 or customer handle",
        paymentConfirmed = "Payment Verified Successfully!",
        notifyCustomerOnWhatsApp = "Notify Customer on WhatsApp",
        whatsAppMessageSent = "WhatsApp message dispatched",
        shopSettings = "Shop Profile & Payment Setup",
        ownerName = "Owner / Tech Name",
        ownerPhone = "Shop Contact Number",
        saveSettings = "Save Shop Details",
        settingsSaved = "Shop Details & UPI ID Updated!",
        enterValidUpi = "Please set your UPI ID in Shop Settings first",
        sampleUpiHint = "e.g. sharmamobile@okaxis or 9876543210@paytm",
        cancel = "Cancel",
        save = "Save",
        edit = "Edit",
        delete = "Delete",
        confirm = "Confirm",
        close = "Close"
    )

    private val hindiStrings = AppStrings(
        appTitle = "मोबीरिपेयर प्रो",
        tabRepairs = "रिपेयर व बिलिंग",
        tabInventory = "इन्वेंट्री (सामान)",
        tabCompatibility = "पार्ट्स मैच",
        tabSettings = "दुकान व यूपीआई",
        languageSelect = "भाषा चुनें",
        pendingRepairs = "बाकी रिपेयर काम",
        readyPickup = "ग्राहक को देने को तैयार",
        lowStockAlerts = "स्टॉक अलर्ट (कम सामान)",
        totalRevenue = "कुल प्राप्त राशि",
        quickUpiBill = "क्विक UPI QR बिल",
        newJob = "नया रिपेयर जॉब",
        addItem = "नया स्पेयर पार्ट जोड़ें",
        repairJobs = "रिपेयर पर्चियां",
        searchJobs = "ग्राहक का नाम, फोन या मॉडल खोजें...",
        allJobs = "सभी",
        statusPending = "पेंडिंग (काम बाकी)",
        statusInProgress = "चालू है (प्रगति पर)",
        statusReady = "तैयार है (Pickup Ready)",
        statusDelivered = "दे दिया (Delivered)",
        customerName = "ग्राहक का नाम",
        customerPhone = "ग्राहक का व्हाट्सएप / मोबाइल नंबर",
        deviceModel = "मोबाइल मॉडल (उदा. Realme C2)",
        issueDescription = "समस्या / रिपेयर काम",
        estimatedAmount = "बिल राशि (₹)",
        paymentStatus = "भुगतान स्थिति",
        unpaid = "बाकी (Unpaid)",
        paidUpi = "UPI से प्राप्त",
        paidCash = "नकद (Cash) प्राप्त",
        markReady = "रिपेयर पूरा हुआ (तैयार करें)",
        markDelivered = "ग्राहक को दे दिया",
        generateUpiQr = "UPI QR कोड बनाएं",
        sendWhatsApp = "व्हाट्सएप पर सूचित करें",
        viewJobDetails = "पर्ची का विवरण",
        jobCreatedOn = "आने की तारीख",
        partUsed = "इन्वेंट्री से इस्तेमाल हुआ पार्ट",
        inventoryTitle = "स्पेयर पार्ट्स व सामान इन्वेंट्री",
        searchInventory = "फोल्डर, चार्जिंग जैक, बैटरी, ग्लास खोजें...",
        categoryFilter = "श्रेणी (Category)",
        allCategories = "सभी पार्ट्स",
        inStock = "स्टॉक में है",
        lowStock = "स्टॉक कम है",
        outOfStock = "स्टॉक खत्म",
        costPrice = "खरीद मूल्य / लागत (₹)",
        sellingPrice = "बिक्री मूल्य (₹)",
        profitMargin = "मुनाफा",
        minAlertQty = "कम स्टॉक चेतावनी संख्या",
        compatibleModels = "किन-किन मॉडल्स में लगेगा (उदा. Oppo A1k, Realme C2)",
        stockAlertBanner = "चेतावनी: कुछ जरूरी पार्ट्स का स्टॉक बहुत कम हो गया है!",
        quickAddStock = "स्टॉक बढ़ाएं",
        compatibilityTitle = "समान पार्ट्स व मॉडल मैचिंग",
        compatibilitySubtitle = "जानिए कौन सा फोल्डर, चार्जिंग जैक और बैटरी किस-किस मॉडल में लगती है",
        searchPhoneModel = "मोबाइल का मॉडल लिखें (उदा. Vivo Y12, Redmi Note 7, Realme 5)...",
        sharedLcdFolder = "डिस्प्ले फोल्डर (LCD Combo)",
        sharedChargingJack = "चार्जिंग जैक / सीसी बोर्ड",
        sharedBattery = "बैटरी मॉडल",
        matchingModels = "ये सभी मॉडल्स एक ही पार्ट इस्तेमाल करते हैं",
        partNotes = "टेक्नीशियन जानकारी व पिन विवरण",
        checkStock = "दुकान में उपलब्ध स्टॉक",
        upiBilling = "UPI QR कोड भुगतान",
        enterAmount = "राशि दर्ज करें (₹)",
        scanToPay = "किसी भी ऐप से स्कैन करें (GPay, PhonePe, Paytm, BHIM)",
        shopUpiId = "दुकानदार की UPI ID",
        shopName = "दुकान का नाम",
        verifyPayment = "भुगतान कन्फर्म करें",
        enterTxnId = "UPI रेफरेंस / UTR नंबर दर्ज करें",
        txnIdPlaceholder = "उदा. 428190382910 या ग्राहक हैंडल",
        paymentConfirmed = "भुगतान सफलता से सत्यापित हुआ!",
        notifyCustomerOnWhatsApp = "ग्राहक को व्हाट्सएप संदेश भेजें",
        whatsAppMessageSent = "व्हाट्सएप संदेश भेज दिया गया",
        shopSettings = "दुकान विवरण व UPI सेटअप",
        ownerName = "दुकानदार / टेक्नीशियन का नाम",
        ownerPhone = "दुकान का मोबाइल नंबर",
        saveSettings = "विवरण सुरक्षित करें",
        settingsSaved = "दुकान का विवरण व UPI ID सेव हो गई!",
        enterValidUpi = "कृपया पहले दुकान सेटिंग में अपनी UPI ID दर्ज करें",
        sampleUpiHint = "उदा. sharmamobile@okaxis या 9876543210@paytm",
        cancel = "रद्द करें",
        save = "सुरक्षित करें",
        edit = "संपादित करें",
        delete = "हटाएं",
        confirm = "कन्फर्म करें",
        close = "बंद करें"
    )

    private val hinglishStrings = AppStrings(
        appTitle = "MobiRepair Pro",
        tabRepairs = "Repairs & Bills",
        tabInventory = "Inventory Stock",
        tabCompatibility = "Parts Matcher",
        tabSettings = "Shop & UPI QR",
        languageSelect = "Bhasha Select Karein",
        pendingRepairs = "Pending Repair",
        readyPickup = "Repair Done / Ready",
        lowStockAlerts = "Low Stock Alert",
        totalRevenue = "Kul Kamai (₹)",
        quickUpiBill = "Instant UPI QR Bill",
        newJob = "Naya Repair Ticket",
        addItem = "Naya Part Add Karein",
        repairJobs = "Repair Jobs",
        searchJobs = "Customer name, phone ya phone model search karein...",
        allJobs = "Sabhi",
        statusPending = "Pending Kaam",
        statusInProgress = "Repair Chal Raha Hai",
        statusReady = "Ready Hai (Pick Up)",
        statusDelivered = "Customer Ko De Diya",
        customerName = "Customer Ka Naam",
        customerPhone = "Customer Mobile / WhatsApp Number",
        deviceModel = "Mobile Model (jaise Realme C2)",
        issueDescription = "Problem / Repairing Details",
        estimatedAmount = "Total Bill Amount (₹)",
        paymentStatus = "Payment Status",
        unpaid = "Baaki (Unpaid)",
        paidUpi = "UPI Se Payment Hua",
        paidCash = "Cash Me Payment Hua",
        markReady = "Repair Complete (Ready Karein)",
        markDelivered = "Handover / Delivered Karein",
        generateUpiQr = "UPI QR Code Banayein",
        sendWhatsApp = "WhatsApp Pe Message Bhejein",
        viewJobDetails = "Ticket Ki Details",
        jobCreatedOn = "Job Date",
        partUsed = "Stock Se Konsa Part Laga",
        inventoryTitle = "Parts & Spares Inventory",
        searchInventory = "Folder, jack, battery, glass dhoondhein...",
        categoryFilter = "Category",
        allCategories = "Sabhi Spares",
        inStock = "Stock Me Hai",
        lowStock = "Stock Kam Hai",
        outOfStock = "Stock Khatam",
        costPrice = "Khareed Daam / Cost (₹)",
        sellingPrice = "Bikri Daam / MRP (₹)",
        profitMargin = "Munafa",
        minAlertQty = "Alert Quantity Level",
        compatibleModels = "Kis Kis Model Me Lagega (jaise Oppo A1k, Realme C2)",
        stockAlertBanner = "Alert: Kuch zaroori parts ka stock bohot kam ho chuka hai!",
        quickAddStock = "Stock Badhayein",
        compatibilityTitle = "Shared Parts & Model Matcher",
        compatibilitySubtitle = "Check karein ek model ka folder, jack ya battery kis kis dusre phone me lagti hai",
        searchPhoneModel = "Phone model likhein (jaise Vivo Y12, Redmi Note 7, Realme 5)...",
        sharedLcdFolder = "Display (LCD Combo Folder)",
        sharedChargingJack = "Charging Jack / CC Board",
        sharedBattery = "Battery Model",
        matchingModels = "In Sabhi Models Me Same Part Fit Hoga",
        partNotes = "Technician Fitting Note",
        checkStock = "Dukaan Me Kitna Stock Hai",
        upiBilling = "UPI QR Payment",
        enterAmount = "Kitna Amount Hai (₹)",
        scanToPay = "Kisi bhi UPI App (GPay, PhonePe, Paytm, BHIM) se scan karein",
        shopUpiId = "Shop Owner UPI ID",
        shopName = "Dukaan Ka Naam",
        verifyPayment = "Payment Confirm Karein",
        enterTxnId = "UPI UTR / Reference ID Dalein",
        txnIdPlaceholder = "jaise 428190382910 ya customer UPI ID",
        paymentConfirmed = "Payment Verify Ho Gaya!",
        notifyCustomerOnWhatsApp = "Customer Ko WhatsApp Bhejein",
        whatsAppMessageSent = "WhatsApp message open ho gaya",
        shopSettings = "Shop Details & UPI Setup",
        ownerName = "Owner / Tech Ka Naam",
        ownerPhone = "Shop Ka Mobile Number",
        saveSettings = "Details Save Karein",
        settingsSaved = "Shop Details aur UPI ID Save Ho Gayi!",
        enterValidUpi = "Pehle Shop Settings me apni UPI ID set karein",
        sampleUpiHint = "jaise sharmamobile@okaxis ya 9876543210@paytm",
        cancel = "Cancel",
        save = "Save",
        edit = "Edit",
        delete = "Delete",
        confirm = "Confirm",
        close = "Close"
    )

    fun getStrings(language: AppLanguage): AppStrings {
        return when (language) {
            AppLanguage.EN -> englishStrings
            AppLanguage.HI -> hindiStrings
            AppLanguage.HINGLISH -> hinglishStrings
        }
    }

    fun buildWhatsAppMessage(
        language: AppLanguage,
        shopName: String,
        customerName: String,
        deviceModel: String,
        issue: String,
        amount: Double,
        isPaid: Boolean,
        txnId: String?
    ): String {
        val cleanShop = if (shopName.isNotBlank()) shopName else "Mobile Care"
        val cleanCust = if (customerName.isNotBlank()) customerName else "Customer"
        val formattedAmount = String.format("%.0f", amount)
        val txnText = if (!txnId.isNullOrBlank()) " (Txn/UTR: $txnId)" else ""

        return when (language) {
            AppLanguage.HI -> {
                val payStatus = if (isPaid) "✅ UPI द्वारा भुगतान प्राप्त$txnText" else "⏳ भुगतान बाकी: ₹$formattedAmount"
                """
                📱 *${cleanShop}*
                नमस्ते ${cleanCust} जी,
                
                आपके मोबाइल *${deviceModel}* का रिपेयरिंग कार्य सफलतापूर्वक पूरा हो चुका है! ✅
                
                🔧 समस्या / काम: ${issue}
                💰 कुल बिल राशि: ₹${formattedAmount}
                💳 भुगतान स्थिति: ${payStatus}
                
                📍 आपका फोन टेस्टिंग के बाद तैयार है, कृपया दुकान आकर प्राप्त करें।
                
                धन्यवाद!
                *${cleanShop}*
                """.trimIndent()
            }
            AppLanguage.HINGLISH -> {
                val payStatus = if (isPaid) "✅ UPI Se Payment Recvd$txnText" else "⏳ Baaki Amount: ₹$formattedAmount"
                """
                📱 *${cleanShop}*
                Namaste ${cleanCust} ji,
                
                Aapke mobile *${deviceModel}* ki repairing complete ho gayi hai aur phone ready hai! ✅
                
                🔧 Problem / Work: ${issue}
                💰 Bill Amount: ₹${formattedAmount}
                💳 Payment: ${payStatus}
                
                📍 Testing ho chuki hai, aap shop aakar phone collect kar sakte hain.
                
                Thank you!
                *${cleanShop}*
                """.trimIndent()
            }
            AppLanguage.EN -> {
                val payStatus = if (isPaid) "✅ Payment Received via UPI$txnText" else "⏳ Payment Due: ₹$formattedAmount"
                """
                📱 *${cleanShop}*
                Dear ${cleanCust},
                
                Your mobile device *${deviceModel}* repair is COMPLETED and ready for pickup! ✅
                
                🔧 Service / Repair: ${issue}
                💰 Total Bill: ₹${formattedAmount}
                💳 Payment Status: ${payStatus}
                
                📍 Device has been thoroughly tested. Please visit our shop to collect it.
                
                Thank you for choosing us!
                *${cleanShop}*
                """.trimIndent()
            }
        }
    }
}
