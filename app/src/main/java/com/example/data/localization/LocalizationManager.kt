package com.example.data.localization

import android.content.Context
import android.content.res.Configuration
import com.example.model.AppLanguage
import com.example.model.CoverageScope
import com.example.model.DeliveryStatus
import com.example.model.PaymentMethod
import com.example.model.ServiceCategory
import com.example.model.UserRole
import java.util.Locale

/**
 * Centralized localization resource system for JubaGo.
 * Supports English and authentic Juba Arabic (عربي جوبا), with fallback support for Dinka and Nuer.
 * Synchronized with res/values/strings.xml and res/values-ar/strings.xml.
 */
object LocalizationManager {

    /**
     * Resolves a localized string given its resource key and the active AppLanguage.
     */
    fun getString(key: String, language: AppLanguage): String {
        return when (language) {
            AppLanguage.ENGLISH -> englishStrings[key] ?: key
            AppLanguage.JUBA_ARABIC -> jubaArabicStrings[key] ?: englishStrings[key] ?: key
            AppLanguage.DINKA -> dinkaStrings[key] ?: englishStrings[key] ?: key
            AppLanguage.NUER -> nuerStrings[key] ?: englishStrings[key] ?: key
        }
    }

    /**
     * Updates the Android configuration locale for the given AppLanguage so that
     * Android string resources (res/values-ar) and system components match the in-app language.
     */
    fun applyLocale(context: Context, language: AppLanguage): Context {
        val locale = when (language) {
            AppLanguage.JUBA_ARABIC -> Locale("ar")
            else -> Locale("en")
        }
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    fun getCategoryName(category: ServiceCategory, language: AppLanguage): String {
        return when (language) {
            AppLanguage.ENGLISH -> category.displayName
            AppLanguage.JUBA_ARABIC -> when (category) {
                ServiceCategory.ALL -> "الكل (Kulu)"
                ServiceCategory.FOOD -> "أكل ومطاعم (Akul)"
                ServiceCategory.HOTELS -> "فنادق وبيوت (Fanadiq)"
                ServiceCategory.CARS -> "عربات وبودا (Sayarat)"
                ServiceCategory.FLIGHTS -> "طيارات وشحن (Tayarat)"
                ServiceCategory.GROCERY -> "دكاكين وأغراض (Dukakin)"
            }
            AppLanguage.DINKA -> when (category) {
                ServiceCategory.ALL -> "Eben (All)"
                ServiceCategory.FOOD -> "Mith (Food)"
                ServiceCategory.HOTELS -> "Ghot (Hotels)"
                ServiceCategory.CARS -> "Miir (Vehicles)"
                ServiceCategory.FLIGHTS -> "Riäŋ nhiial (Flights)"
                ServiceCategory.GROCERY -> "Ɣɔ̈c (Shops)"
            }
            AppLanguage.NUER -> when (category) {
                ServiceCategory.ALL -> "Dial (All)"
                ServiceCategory.FOOD -> "Mïth (Food)"
                ServiceCategory.HOTELS -> "Dääc (Hotels)"
                ServiceCategory.CARS -> "Riɛy (Transport)"
                ServiceCategory.FLIGHTS -> "Deel nhial (Flights)"
                ServiceCategory.GROCERY -> "Ɣööc (Shops)"
            }
        }
    }

    fun getScopeName(scope: CoverageScope, language: AppLanguage): String {
        return when (language) {
            AppLanguage.ENGLISH -> scope.displayName
            AppLanguage.JUBA_ARABIC -> when (scope) {
                CoverageScope.ALL_REGIONS -> "كل المناطق (All Regions)"
                CoverageScope.JUBA_LOCAL -> "مدينة جوبا وضواحيها (Juba Local)"
                CoverageScope.SOUTH_SUDAN_NATIONAL -> "كل ولايات الجنوب (South Sudan)"
                CoverageScope.EAST_AFRICA_REGIONAL -> "دول شرق أفريقيا (East Africa)"
                CoverageScope.GLOBAL_BEYOND_AFRICA -> "دولياً وخارج أفريقيا (Global)"
            }
            AppLanguage.DINKA -> when (scope) {
                CoverageScope.ALL_REGIONS -> "Paguot Thudän Eben"
                CoverageScope.JUBA_LOCAL -> "Juba Ciɛlic"
                CoverageScope.SOUTH_SUDAN_NATIONAL -> "Pawut Eben"
                CoverageScope.EAST_AFRICA_REGIONAL -> "East Africa"
                CoverageScope.GLOBAL_BEYOND_AFRICA -> "Pinynhom Eben"
            }
            AppLanguage.NUER -> when (scope) {
                CoverageScope.ALL_REGIONS -> "Thudän Dial"
                CoverageScope.JUBA_LOCAL -> "Juba Cäŋ"
                CoverageScope.SOUTH_SUDAN_NATIONAL -> "Rɔw Dial"
                CoverageScope.EAST_AFRICA_REGIONAL -> "East Africa"
                CoverageScope.GLOBAL_BEYOND_AFRICA -> "Wiic Dial"
            }
        }
    }

    fun getPaymentMethodLabel(method: PaymentMethod, language: AppLanguage): String {
        return when (language) {
            AppLanguage.ENGLISH -> method.title
            AppLanguage.JUBA_ARABIC -> when (method) {
                PaymentMethod.M_GURUSH -> "إم-قروش (m-GURUSH Mobile)"
                PaymentMethod.MTN_MOMO -> "إم تي إن مومو (MTN MoMo)"
                PaymentMethod.DIGITEL_PAY -> "ديجيتل باي (Digitel Pay)"
                PaymentMethod.CASH_ON_DELIVERY -> "كاش يد بيد (Cash on Delivery)"
                PaymentMethod.BANK_CARD -> "بطاقة بنكية (Visa / Card)"
            }
            AppLanguage.DINKA -> when (method) {
                PaymentMethod.M_GURUSH -> "m-GURUSH Wëu"
                PaymentMethod.MTN_MOMO -> "MTN MoMo Wëu"
                PaymentMethod.DIGITEL_PAY -> "Digitel Wëu"
                PaymentMethod.CASH_ON_DELIVERY -> "Wëu cin (Cash)"
                PaymentMethod.BANK_CARD -> "Wëu Bank"
            }
            AppLanguage.NUER -> when (method) {
                PaymentMethod.M_GURUSH -> "m-GURUSH Jiöök"
                PaymentMethod.MTN_MOMO -> "MTN MoMo Jiöök"
                PaymentMethod.DIGITEL_PAY -> "Digitel Jiöök"
                PaymentMethod.CASH_ON_DELIVERY -> "Jiöök tet (Cash)"
                PaymentMethod.BANK_CARD -> "Jiöök Bank"
            }
        }
    }

    fun getRoleLabel(role: UserRole, language: AppLanguage): String {
        return when (language) {
            AppLanguage.ENGLISH -> role.label
            AppLanguage.JUBA_ARABIC -> when (role) {
                UserRole.CUSTOMER -> "زبون (Customer)"
                UserRole.MERCHANT -> "تاجر (Merchant)"
                UserRole.COURIER -> "سائق / بودا (Courier)"
            }
            AppLanguage.DINKA -> when (role) {
                UserRole.CUSTOMER -> "Raan ɣɔɔc (Customer)"
                UserRole.MERCHANT -> "Raan wëu (Merchant)"
                UserRole.COURIER -> "Raan riɛy (Courier)"
            }
            AppLanguage.NUER -> when (role) {
                UserRole.CUSTOMER -> "Raan ɣööc (Customer)"
                UserRole.MERCHANT -> "Raan jiöök (Merchant)"
                UserRole.COURIER -> "Raan cök (Courier)"
            }
        }
    }

    fun getDeliveryStatusLabel(status: DeliveryStatus, language: AppLanguage): String {
        return when (language) {
            AppLanguage.JUBA_ARABIC -> when (status) {
                DeliveryStatus.PLACED -> "تم استلام الطلب"
                DeliveryStatus.CONFIRMED -> "أكّد المتجر الطلب"
                DeliveryStatus.PREPARING -> "جاري التجهيز والتعبئة"
                DeliveryStatus.ON_THE_WAY -> "المندوب في الطريق ليك"
                DeliveryStatus.DELIVERED -> "تم التوصيل بنجاح"
                DeliveryStatus.CANCELLED -> "ملغي"
            }
            else -> status.label
        }
    }

    private val englishStrings = mapOf(
        // App & General
        "app_title" to "JubaGo",
        "app_country_badge" to "SOUTH SUDAN",
        "app_tagline" to "South Sudan's premier commerce, booking and delivery platform",
        "nav_home" to "Home",
        "nav_explore" to "Explore",
        "nav_tracking" to "Live GPS",
        "nav_support" to "Support",
        "nav_orders" to "Orders",
        "nav_profile" to "Profile",
        "nav_merchant" to "Merchant",

        // Explore Screen
        "hero_title" to "JubaGo South Sudan",
        "hero_subtitle" to "Cars • Flights • Hotels • Local Food Delivery",
        "hero_tagline" to "Empowering Juba businesses • Real-time GPS & Mobile Money",
        "hero_tutorial_button" to "New User? Take 1-Min Feature Guide",
        "search_placeholder" to "Search travel (cars, flights, hotels) & food listings...",
        "search_label" to "Search",
        "verified_only" to "Verified Only",
        "offline_cache_active" to "Offline Nile Map Active",
        "offline_notice" to "Using cached Juba coordinates. Bookings will sync when back online.",

        // Actions
        "book_now" to "Book Now",
        "quick_book" to "Quick Order",
        "view_details" to "View Details",
        "quick_book_cars" to "Ride / Rent",
        "quick_book_flights" to "Book Seat",
        "quick_book_hotels" to "Reserve",
        "quick_book_food" to "Order Food",
        "quick_book_default" to "Order Now",
        "cancel_button" to "Cancel",
        "confirm_button" to "Confirm",
        "close_button" to "Close",
        "save_button" to "Save",

        // Roles & Language
        "role_customer" to "Customer View",
        "role_merchant" to "Merchant Mode",
        "role_courier" to "Delivery Rider View",
        "select_language" to "Change Language",
        "language_toggle_label" to "Language / Thok / لغة",

        // Sync Indicators
        "sync_online" to "Online",
        "sync_syncing" to "Syncing...",
        "sync_offline" to "Offline",

        // Merchant Banner
        "register_business_banner_title" to "Register Your Business & Services",
        "register_business_banner_desc" to "Shops, apartments, hotels, car rentals & restaurants: provide services across South Sudan & beyond Africa.",
        "register_button" to "Register",

        // Onboarding Wizard Flow
        "onboarding_skip" to "Skip",
        "onboarding_back" to "Back",
        "onboarding_next" to "Next",
        "onboarding_finish" to "Start",
        "onboarding_step_welcome_header" to "Welcome to JubaGo",
        "onboarding_step_discovery_header" to "Tutorial: Service Discovery",
        "onboarding_step_booking_header" to "Tutorial: Booking System",
        "onboarding_step_tracking_header" to "Live Tracking & Nile Support",
        "onboarding_step_start_header" to "Get Started",

        // Onboarding Step 0: Welcome
        "onboarding_welcome_title" to "Welcome to JubaGo",
        "onboarding_welcome_desc" to "The unified commerce, booking, and delivery network tailored for Juba, Greater Equatoria, and beyond. Explore restaurants, book hotels, hail transport, and connect directly with verified merchants.",
        "onboarding_core_services_title" to "Core Services at Your Fingertips:",
        "onboarding_feature_food_title" to "Food & Nile Cuisine Delivery",
        "onboarding_feature_food_desc" to "Hot meals from Juba's best kitchens (Nile King, Da Vinci, Nimule Kitchen) delivered directly to your home or office.",
        "onboarding_feature_hotel_title" to "Hotels & Riverside Accommodations",
        "onboarding_feature_hotel_desc" to "Instant reservations for luxury suites, Nile views, and budget-friendly guest houses with transparent rates.",
        "onboarding_feature_transport_title" to "Boda Boda & Car Rental Logistics",
        "onboarding_feature_transport_desc" to "Book vetted drivers, airport pickups to JIA, and express courier dispatch across all major roads.",
        "onboarding_feature_cargo_title" to "Domestic Air & Regional Cargo",
        "onboarding_feature_cargo_desc" to "Connect flights between Juba, Wau, Malakal, and cross-border transport across East Africa.",
        "onboarding_local_advantage_header" to "Built Specifically for Local Reality",
        "onboarding_local_advantage_map" to "Offline Nile Map",
        "onboarding_local_advantage_battery" to "Battery Saver",
        "onboarding_local_advantage_payments" to "m-GURUSH / MoMo",

        // Onboarding Step 1: Discovery
        "onboarding_discovery_title" to "Step 1: Finding What You Need",
        "onboarding_discovery_desc" to "JubaGo makes it simple to discover businesses across South Sudan, even when internet connections fluctuate.",
        "onboarding_discovery_tip1_title" to "Contextual Search Bar",
        "onboarding_discovery_tip1_desc" to "Search by vendor name, meal type (e.g., 'Fresh Nile Perch'), neighborhood ('Hai Cinema', 'Munuki'), or keyword.",
        "onboarding_discovery_tip2_title" to "Geographic Scope Filter",
        "onboarding_discovery_tip2_desc" to "Toggle between Central Juba, Greater Equatoria, All South Sudan States, and Regional East Africa.",
        "onboarding_discovery_tip3_title" to "Offline Map Tile Mode",
        "onboarding_discovery_tip3_desc" to "Tap the Wi-Fi icon on the home screen to access cached maps and saved provider contacts without data.",
        "onboarding_discovery_practice_title" to "Interactive Practice: Try Filtering Below",

        // Onboarding Step 2: Booking
        "onboarding_booking_title" to "Step 2: 3-Step Fast Booking",
        "onboarding_booking_desc" to "Ordering meals, scheduling transport, or booking rooms takes less than 30 seconds.",
        "onboarding_booking_step1_title" to "Tap 'Book Now' or 'Quick Order'",
        "onboarding_booking_step1_desc" to "Choose your item directly from the list or service details screen.",
        "onboarding_booking_step2_title" to "Confirm Contact & Delivery Address",
        "onboarding_booking_step2_desc" to "Provide your phone number and landmark (e.g. Near Tongping Church, Juba).",
        "onboarding_booking_step3_title" to "Select Payment & Confirm",
        "onboarding_booking_step3_desc" to "Choose m-GURUSH, Eden Mobile, MTN MoMo, or Cash on Delivery.",
        "onboarding_booking_practice_title" to "Interactive Practice: Try a Demo Payment Choice",
        "onboarding_demo_success_title" to "🎉 Success! Order #JB-DEMO Placed",
        "onboarding_demo_success_desc" to "You will instantly receive an SMS confirmation, live GPS tracking coordinates, and status notifications.",

        // Onboarding Step 3: Tracking
        "onboarding_tracking_title" to "Step 3: Real-Time Tracking & Support",
        "onboarding_tracking_desc" to "Never wonder where your parcel, ride, or order is. JubaGo provides live position updates and instant messaging.",
        "onboarding_tracking_tip1_title" to "Live Nile Map Tracker",
        "onboarding_tracking_tip1_desc" to "Track couriers moving along Nile bridges, Airport Road, and Custom market in real time with estimated arrival times.",
        "onboarding_tracking_tip2_title" to "Real-Time Sync Indicators",
        "onboarding_tracking_tip2_desc" to "Monitor our smart indicator: 🟢 Online (live cloud updates), 🔵 Syncing (transmitting), or 🟠 Offline (queued safely on device).",
        "onboarding_tracking_tip3_title" to "24/7 Nile Concierge & Support",
        "onboarding_tracking_tip3_desc" to "Tap the 'Support Chat' tab anytime to message our friendly local team for booking help, changes, or directions.",

        // Onboarding Step 4: Final Step
        "onboarding_final_title" to "You're All Set!",
        "onboarding_final_desc" to "Whether you want to order food, book a room, hire a driver, or expand your business across South Sudan, JubaGo is your trusted partner.",
        "onboarding_explore_button" to "Start Exploring JubaGo",
        "onboarding_register_button" to "Register My Business / Merchant Mode",
        "onboarding_auth_button" to "Sign In or Create Account with Google",

        // Checkout & Booking Dialog
        "checkout_title" to "Checkout & Booking",
        "checkout_subtitle" to "Secure South Sudan Gateway",
        "checkout_provider_label" to "Provider",
        "checkout_customer_name_label" to "Your Full Name",
        "checkout_phone_label" to "Phone Number (e.g. +211 92...)",
        "checkout_landmark_label" to "Delivery Address / Landmark",
        "checkout_quick_locations_title" to "Quick Select Landmark in Juba:",
        "checkout_payment_method_title" to "Select Payment Method",
        "checkout_account_number_label" to "Wallet / Phone / Card Number",
        "checkout_confirm_order_btn" to "Confirm & Place Order",
        "checkout_processing" to "Processing Secure Order...",

        // Order Placed Dialog
        "order_success_title" to "Order Placed Successfully!",
        "order_success_subtitle" to "Your order has been transmitted and received by the provider in Juba.",
        "order_success_order_id" to "Order Reference:",
        "order_success_payment_status" to "Payment:",
        "order_success_eta" to "Estimated Arrival:",
        "order_success_eta_val" to "25 - 40 Mins",
        "order_success_track_btn" to "Track Order Live GPS",
        "order_success_done_btn" to "Back to Explore",

        // Service Details Modal
        "service_detail_title" to "Service Details",
        "service_detail_verified" to "Verified South Sudan Merchant",
        "service_detail_scope_coverage" to "Geographic Coverage:",
        "service_detail_reviews_header" to "Verified Customer Reviews",
        "service_detail_leave_review" to "Rate & Leave a Review",
        "service_detail_write_review_hint" to "Share your feedback about this Juba provider...",
        "service_detail_submit_review" to "Submit Review",
        "service_detail_book_now" to "Book This Service Now",

        // Business Registration
        "business_reg_title" to "Register Business / Merchant",
        "business_reg_subtitle" to "Join South Sudan's growing digital commerce network",
        "business_reg_name_label" to "Business / Shop Name",
        "business_reg_category_label" to "Primary Service Category",
        "business_reg_scope_label" to "Coverage Territory",
        "business_reg_phone_label" to "Contact Phone (+211)",
        "business_reg_location_label" to "Physical Location / Market in Juba",
        "business_reg_desc_label" to "Services & Offerings Description",
        "business_reg_submit" to "Submit Business Registration",

        // Live Tracking Screen
        "tracking_header_title" to "Live GPS Tracking",
        "tracking_header_subtitle" to "Real-time courier & vehicle navigation in Juba",
        "tracking_no_order" to "No Active Orders Yet",
        "tracking_no_order_desc" to "Place an order for food, boda boda ride, or hotel to track live courier movement.",
        "tracking_driver_label" to "Courier / Driver:",
        "tracking_destination_label" to "Delivery Landmark:",
        "tracking_eta_label" to "Estimated Arrival:",

        // Support Chat Screen
        "support_chat_header" to "Nile Concierge & Support",
        "support_chat_subtitle" to "Live 24/7 assistance across South Sudan",
        "support_chat_input_hint" to "Type your message or question...",
        "support_chat_send_btn" to "Send",

        // Orders History Screen
        "orders_screen_title" to "My Orders & Bookings",
        "orders_screen_subtitle" to "Track past orders, cloud-synced with Firestore and instant reordering",
        "orders_empty_title" to "No Bookings Yet",
        "orders_empty_desc" to "Browse our restaurants, hotels, cars, and flights to make your first order.",
        "orders_track_btn" to "Track GPS",
        "orders_reorder_btn" to "Reorder",
        "orders_reorder_fav_btn" to "Reorder Favorite",
        "orders_tab_all" to "All Orders",
        "orders_tab_favorites" to "Favorites",
        "orders_tab_active" to "In Transit",
        "orders_tab_delivered" to "Delivered",
        "orders_refresh_cloud" to "Sync Firestore",
        "orders_firestore_synced" to "Cloud Synced",
        "orders_empty_favorites_title" to "No Favorite Orders Yet",
        "orders_empty_favorites_desc" to "Tap the heart icon on any order to mark it as a favorite for instant 1-tap reordering!",
        "orders_search_placeholder" to "Search orders, meals, rides, ID...",
        "orders_confirm_reorder_title" to "Confirm Reorder",
        "orders_confirm_reorder_msg" to "Would you like to place a new order for this item with the same delivery details?",

        // Merchant Dashboard Screen
        "merchant_hub_title" to "Merchant Operations Hub",
        "merchant_hub_subtitle" to "Manage orders, stock availability, and revenue",
        "merchant_total_revenue" to "Total Revenue (SSP)",
        "merchant_active_orders" to "Active Orders",
        "merchant_active_listings" to "Listed Services",
        "merchant_export_csv" to "Export CSV Report",
        "merchant_manage_items" to "Manage Service Availability",

        // Notifications Sheet
        "notifications_title" to "Notifications & Alerts",
        "notifications_empty" to "No new alerts at this time.",

        // User Auth
        "auth_dialog_title" to "Account & Sign-In",
        "auth_dialog_subtitle" to "Sync your orders, bookings, and merchant store across devices",
        "auth_google_signin" to "Continue with Google Account",
        "auth_profile_signed_in" to "Signed In",
        "auth_sign_out" to "Sign Out"
    )

    private val jubaArabicStrings = mapOf(
        // App & General
        "app_title" to "جوبا قو (JubaGo)",
        "app_country_badge" to "جنوب السودان",
        "app_tagline" to "منصة جنوب السودان الأولى للتجارة والحجوزات والتوصيل",
        "nav_home" to "الرئيسية (Home)",
        "nav_explore" to "استكشف (Dawir)",
        "nav_tracking" to "تتبع حي (Live GPS)",
        "nav_support" to "مساعدة (Musaa'da)",
        "nav_orders" to "طلباتي (Orders)",
        "nav_profile" to "حسابي (Profile)",
        "nav_merchant" to "التاجر (Tajir)",

        // Explore Screen
        "hero_title" to "جوبا قو - جنوب السودان",
        "hero_subtitle" to "عربات • طيارات • فنادق • أكل وتوصيل سريع",
        "hero_tagline" to "منصة كل أصحاب المحلات في جوبا • دفع موبايل قروش وتتبع GPS",
        "hero_tutorial_button" to "جديد هنا؟ شوف دليل البرنامج بدقيقة",
        "search_placeholder" to "فتّش خدمات السفر (عربات، طيارات، فنادق) وأكل جوبا...",
        "search_label" to "بحث",
        "verified_only" to "مضمون ومرخص بس",
        "offline_cache_active" to "خريطة النيل شغالة بدون نت",
        "offline_notice" to "البيانات محفوظة محلياً. طلباتك حتترسل أوتوماتيك أول ما النت يرجع.",

        // Actions
        "book_now" to "إحجز هسة (Ihjiz Hassa)",
        "quick_book" to "طلب سريع (Sari')",
        "view_details" to "شوف التفاصيل",
        "quick_book_cars" to "إحجز مشوار",
        "quick_book_flights" to "إحجز مقعد",
        "quick_book_hotels" to "إحجز غرفة",
        "quick_book_food" to "أطلب أكل",
        "quick_book_default" to "أطلب هسة",
        "cancel_button" to "إلغاء",
        "confirm_button" to "تأكيد",
        "close_button" to "إغلاق",
        "save_button" to "حفظ",

        // Roles & Language
        "role_customer" to "وضع الزبون",
        "role_merchant" to "وضع صاحب العمل والتاجر",
        "role_courier" to "وضع السائق والمندوب",
        "select_language" to "إختر اللغة (Change Language)",
        "language_toggle_label" to "اللغة / Language",

        // Sync Indicators
        "sync_online" to "متصل (Online)",
        "sync_syncing" to "جاري المزامنة (Syncing)",
        "sync_offline" to "بدون نت (Offline)",

        // Merchant Banner
        "register_business_banner_title" to "سجّل شركتك أو مطعمك في جوبا قو",
        "register_business_banner_desc" to "دكاكين، شقق، فنادق، تأجير سيارات ومطاعم: قدّم خدماتك في كل ولايات جنوب السودان وخارجها.",
        "register_button" to "تسجيل",

        // Onboarding Wizard Flow
        "onboarding_skip" to "تخطي (Skip)",
        "onboarding_back" to "رجوع (Back)",
        "onboarding_next" to "التالي (Next)",
        "onboarding_finish" to "إبدأ هسة (Start)",
        "onboarding_step_welcome_header" to "مرحباً بيك في جوبا قو",
        "onboarding_step_discovery_header" to "دليل: تفتيش الخدمات",
        "onboarding_step_booking_header" to "دليل: نظام الحجز والطلب",
        "onboarding_step_tracking_header" to "تتبع حي ومساعدة النيل",
        "onboarding_step_start_header" to "إبدأ الاستخدام",

        // Onboarding Step 0: Welcome
        "onboarding_welcome_title" to "مرحباً بيك في جوبا قو 🇸🇸",
        "onboarding_welcome_desc" to "شبكة التجارة والخدمات والحجوزات والتوصيل المخصصة لجوبا، الإستوائية الكبرى، وكل ربوع جنوب السودان. استكشف المطاعم، إحجز الفنادق، أطلب بودا أو تاكسي، وتواصل مع التجار المعتمدين مباشرة.",
        "onboarding_core_services_title" to "خدماتنا الأساسية بين يديك:",
        "onboarding_feature_food_title" to "أكل وتوصيل وجبات النيل",
        "onboarding_feature_food_desc" to "أطيب الأكلات الشعبية والنيليّة (سمك كبر، كسرة، ومطاعم دا فينشي ونيمولي) واصلة لحدي بابك.",
        "onboarding_feature_hotel_title" to "فنادق ومقرات على النيل",
        "onboarding_feature_hotel_desc" to "حجز فوري للغرف والأجنحة الفاخرة والاستراحات بأسعار واضحة وموثوقة.",
        "onboarding_feature_transport_title" to "بودا بودا وتأجير عربات 4x4",
        "onboarding_feature_transport_desc" to "إحجز سائقين موثوقين، وتوصيل لمطار جوبا الدولي ومشاوير سريعة عبر شوارع المدينة.",
        "onboarding_feature_cargo_title" to "طيران داخلي وشحن إقليمي",
        "onboarding_feature_cargo_desc" to "رحلات بين جوبا وواو وملكال، وشحن بضائع بري وجوي لدول شرق أفريقيا.",
        "onboarding_local_advantage_header" to "مصمم خصيصاً للواقع والظروف المحلية",
        "onboarding_local_advantage_map" to "خريطة النيل بدون نت",
        "onboarding_local_advantage_battery" to "توفير البطارية",
        "onboarding_local_advantage_payments" to "إم-قروش / مومو",

        // Onboarding Step 1: Discovery
        "onboarding_discovery_title" to "الخطوة ١: كيف تفتش وتلقى المحتاج ليه",
        "onboarding_discovery_desc" to "جوبا قو بيسهّل عليك الوصول للخدمات في جنوب السودان حتى لو شبكة الإنترنت ضعيفة.",
        "onboarding_discovery_tip1_title" to "شريط البحث الذكي",
        "onboarding_discovery_tip1_desc" to "فتّش باسم المحل، نوع الوجبة (زي 'سمك نيلي طازة')، الحي ('هاي سينما'، 'منوكي')، أو الكلمات المفتاحية.",
        "onboarding_discovery_tip2_title" to "فلتر نطاق المناطق",
        "onboarding_discovery_tip2_desc" to "بدّل بين وسط جوبا، الإستوائية الكبرى، كل ولايات جنوب السودان، ودول الجوار.",
        "onboarding_discovery_tip3_title" to "وضع الخريطة بدون نت",
        "onboarding_discovery_tip3_desc" to "إضغط على أيقونة الواي فاي في الشاشة الرئيسية عشان تتصفح الخرائط وأرقام المحلات بدون استهلاك رصيد بيانات.",
        "onboarding_discovery_practice_title" to "تدريب عملي: جرّب الفلاتر تحت هسة",

        // Onboarding Step 2: Booking
        "onboarding_booking_title" to "الخطوة ٢: ٣ خطوات للطلب والحجز السريع",
        "onboarding_booking_desc" to "طلب الوجبات، حجز العربات، أو حجز الفنادق بياخد أقل من ٣٠ ثانية.",
        "onboarding_booking_step1_title" to "إضغط 'إحجز هسة' أو 'طلب سريع'",
        "onboarding_booking_step1_desc" to "إختر الخدمة أو الوجبة مباشرة من القائمة أو صفحة التفاصيل.",
        "onboarding_booking_step2_title" to "أكّد رقم تلفونك وعنوان التوصيل",
        "onboarding_booking_step2_desc" to "أدخل رقم تلفونك وعلامة مميزة في منطقتك (مثلاً: قريب كنيسة طونقبينق، جوبا).",
        "onboarding_booking_step3_title" to "إختر طريقة الدفع وأكّد الطلب",
        "onboarding_booking_step3_desc" to "إختر إم-قروش، إيدن بنك، إم تي إن مومو، أو كاش يد بيد عند الاستلام.",
        "onboarding_booking_practice_title" to "تدريب عملي: جرّب اختيار طريقة دفع تجريبية",
        "onboarding_demo_success_title" to "🎉 مبروك! تم إرسال الطلب التجريبي #JB-DEMO",
        "onboarding_demo_success_desc" to "حتصلك رسالة SMS فورية، وإحداثيات التتبع المباشر، وإشعارات تحديث الحالة لحظة بلحظة.",

        // Onboarding Step 3: Tracking
        "onboarding_tracking_title" to "الخطوة ٣: تتبع مباشر ودعم النيل",
        "onboarding_tracking_desc" to "ما تحتار طلبك وين. جوبا قو بتقدّم تتبع حي للبودا والمندوب وتواصل فوري.",
        "onboarding_tracking_tip1_title" to "خريطة تتبع النيل الحية",
        "onboarding_tracking_tip1_desc" to "تابع المندوب ماشي في كباري النيل، طريق المطار، وسوق كاستم لايف مع وقت الوصول التقريبي.",
        "onboarding_tracking_tip2_title" to "مؤشر المزامنة اللحظي",
        "onboarding_tracking_tip2_desc" to "راقب مؤشرنا الذكي: 🟢 متصل (تحديث سحابي فوري)، 🔵 بيزامن، أو 🟠 بدون نت (محفوظ بأمان في جهازك).",
        "onboarding_tracking_tip3_title" to "دعم وخدمة عملاء النيل ٢٤/٧",
        "onboarding_tracking_tip3_desc" to "إضغط على خانة 'مساعدة' في أي وقت للتحدث مع فريقنا المحلي للمساعدة في الحجوزات أو التعديلات.",

        // Onboarding Step 4: Final Step
        "onboarding_final_title" to "كدا إنت جاهز تماماً!",
        "onboarding_final_desc" to "سواء كنت عايز تطلب أكل، تحجز فندق، تطلب بودا، أو تكبّر شغلك في جنوب السودان، جوبا قو معاك خطوة بخطوة.",
        "onboarding_explore_button" to "إبدأ استكشف جوبا قو",
        "onboarding_register_button" to "سجّل شركتك / وضع التاجر",
        "onboarding_auth_button" to "سجّل دخول أو أنشئ حساب بقوقل",

        // Checkout & Booking Dialog
        "checkout_title" to "الدفع وإتمام الحجز",
        "checkout_subtitle" to "بوابة دفع آمنة في جنوب السودان",
        "checkout_provider_label" to "المزوّد",
        "checkout_customer_name_label" to "اسمك الكامل",
        "checkout_phone_label" to "رقم التلفون (مثال: +211 92...)",
        "checkout_landmark_label" to "عنوان التوصيل / أقرب علامة بارزة",
        "checkout_quick_locations_title" to "إختر منطقة سريعة في جوبا:",
        "checkout_payment_method_title" to "إختر طريقة الدفع",
        "checkout_account_number_label" to "رقم المحفظة / التلفون / البطاقة",
        "checkout_confirm_order_btn" to "تأكيد وإرسال الطلب",
        "checkout_processing" to "جاري معالجة الطلب بأمان...",

        // Order Placed Dialog
        "order_success_title" to "مبروك! تم إرسال طلبك بنجاح",
        "order_success_subtitle" to "تم إرسال طلبك واستلامه من قبل المحل أو المزوّد في جوبا.",
        "order_success_order_id" to "رقم الطلب:",
        "order_success_payment_status" to "طريقة الدفع:",
        "order_success_eta" to "الوقت التقريبي للوصول:",
        "order_success_eta_val" to "٢٥ - ٤٠ دقيقة",
        "order_success_track_btn" to "تتبع المندوب حي بالخريطة",
        "order_success_done_btn" to "رجوع للشاشة الرئيسية",

        // Service Details Modal
        "service_detail_title" to "تفاصيل الخدمة",
        "service_detail_verified" to "تاجر مضمون ومرخص في جنوب السودان",
        "service_detail_scope_coverage" to "نطاق التغطية الجغرافية:",
        "service_detail_reviews_header" to "تقييمات الزبائن الحقيقيين",
        "service_detail_leave_review" to "قيّم الخدمة وأكتب رأيك",
        "service_detail_write_review_hint" to "شارك رأيك وتجربتك مع هذا التاجر في جوبا...",
        "service_detail_submit_review" to "نشر التقييم",
        "service_detail_book_now" to "إحجز هذه الخدمة هسة",

        // Business Registration
        "business_reg_title" to "تسجيل نشاط تجاري / مزوّد خدمة",
        "business_reg_subtitle" to "إنضم لأكبر شبكة تجارية رقمية في جنوب السودان",
        "business_reg_name_label" to "اسم المحل أو الشركة",
        "business_reg_category_label" to "النشاط الأساسي",
        "business_reg_scope_label" to "نطاق العمل والتوصيل",
        "business_reg_phone_label" to "رقم التلفون للتواصل (+211)",
        "business_reg_location_label" to "الموقع والسوق في جوبا",
        "business_reg_desc_label" to "وصف الخدمات والأسعار",
        "business_reg_submit" to "إرسال طلب تسجيل التاجر",

        // Live Tracking Screen
        "tracking_header_title" to "متابعة الموقع وتتبع الـ GPS",
        "tracking_header_subtitle" to "تتبع حركة المندوبين والسيارات مباشرة في جوبا",
        "tracking_no_order" to "مافي طلبات نشطة حالياً",
        "tracking_no_order_desc" to "أطلب وجبة، أو مشوار بودا، أو احجز فندق لتتبع خط السير مباشرة على الخريطة.",
        "tracking_driver_label" to "المندوب / السائق:",
        "tracking_destination_label" to "مكان التوصيل:",
        "tracking_eta_label" to "الوصول المتوقع:",

        // Support Chat Screen
        "support_chat_header" to "فريق مساعدة ودعم النيل",
        "support_chat_subtitle" to "خدمة محلية ٢٤/٧ في كل ولايات جنوب السودان",
        "support_chat_input_hint" to "أكتب رسالتك أو استفسارك هنا...",
        "support_chat_send_btn" to "إرسال",

        // Orders History Screen
        "orders_screen_title" to "طلباتي وحجوزاتي",
        "orders_screen_subtitle" to "سجل الطلبات السابقة، متزامن مع فايرستور وإعادة طلب المفضلة بنقرة واحدة",
        "orders_empty_title" to "لسة ما طلبت حاجة",
        "orders_empty_desc" to "تصفح المطاعم، الفنادق، المشاوير، والرحلات وسجل أول طلب ليك هسة.",
        "orders_track_btn" to "تتبع GPS",
        "orders_reorder_btn" to "إعادة الطلب",
        "orders_reorder_fav_btn" to "إعادة طلب المفضلة ❤️",
        "orders_tab_all" to "كل الطلبات",
        "orders_tab_favorites" to "المفضلة ❤️",
        "orders_tab_active" to "قيد التوصيل 🚚",
        "orders_tab_delivered" to "تم التوصيل ✅",
        "orders_refresh_cloud" to "مزامنة سحابية",
        "orders_firestore_synced" to "متزامن سحابياً",
        "orders_empty_favorites_title" to "مافي طلبات مفضلة لسة",
        "orders_empty_favorites_desc" to "اضغط على علامة القلب في أي طلب عشان تحفظه في المفضلة وتعيد طلبه بنقرة واحدة!",
        "orders_search_placeholder" to "ابحث برقم الطلب، الوجبة، أو المزوّد...",
        "orders_confirm_reorder_title" to "تأكيد إعادة الطلب",
        "orders_confirm_reorder_msg" to "هل ترغب في إعادة طلب هذه الخدمة بنفس تفاصيل التوصيل السابقة؟",

        // Merchant Dashboard Screen
        "merchant_hub_title" to "لوحة عمليات التاجر",
        "merchant_hub_subtitle" to "إدارة الطلبات، وفرة المخزون، وحساب الأرباح",
        "merchant_total_revenue" to "إجمالي الدخل (جنوب سوداني)",
        "merchant_active_orders" to "طلبات نشطة",
        "merchant_active_listings" to "الخدمات المعروضة",
        "merchant_export_csv" to "تنزيل تقرير مبيعات CSV",
        "merchant_manage_items" to "إدارة توفر الخدمات",

        // Notifications Sheet
        "notifications_title" to "التنبيهات والإشعارات",
        "notifications_empty" to "مافي تنبيهات جديدة في الوقت الحالي.",

        // User Auth
        "auth_dialog_title" to "الحساب وتسجيل الدخول",
        "auth_dialog_subtitle" to "مزامنة طلباتك وحجوزاتك ومتجرك عبر كل أجهزتك",
        "auth_google_signin" to "المتابعة بحساب قوقل (Google)",
        "auth_profile_signed_in" to "مسجل الدخول",
        "auth_sign_out" to "تسجيل الخروج"
    )

    private val dinkaStrings = mapOf(
        "app_title" to "JubaGo",
        "app_country_badge" to "PAGUOT THUDÄN",
        "nav_explore" to "Yic (Explore)",
        "nav_tracking" to "Kuel GPS",
        "nav_support" to "Kuony (Help)",
        "nav_orders" to "Luɔi (Orders)",
        "nav_merchant" to "Wëu (Merchant)",
        "search_placeholder" to "Wic mith, miir, ghot, riäŋ...",
        "verified_only" to "Kɔc cï looi käŋ",
        "offline_cache_active" to "Map acï looi",
        "offline_notice" to "Käŋ aaye gɔ̈t tɛ̈n. Na bɛ̈r intanɛt, ka bï lɔ.",
        "book_now" to "Ɣɔɔc Emeeŋ",
        "quick_book" to "Ɣɔɔc Kat",
        "view_details" to "Dai käŋ",
        "hero_title" to "JubaGo Paguot Thudän",
        "hero_subtitle" to "Miir • Riäŋ • Ghot • Mith",
        "hero_tagline" to "Kɔc Juba kɔ̈k • Wëu cin ku Mobile Money",
        "hero_tutorial_button" to "Raan yam? Tïŋ kuel ë käŋ",
        "role_customer" to "Raan ɣɔɔc",
        "role_merchant" to "Raan ë mac käŋ",
        "register_business" to "Gɔ̈t thurumbïl ka ɣööt du",
        "chat_support_title" to "Kɔc kuony Juba",
        "live_tracking_title" to "Kuel ë rɔt GPS",
        "merchant_dashboard_title" to "Luɔi ë wëu",
        "select_language" to "Wɛ̈r thok (Language)",
        "sync_online" to "Acï tɔ̈ (Online)",
        "sync_syncing" to "Gɔ̈t (Syncing)",
        "sync_offline" to "Cïï tɔ̈ (Offline)"
    )

    private val nuerStrings = mapOf(
        "app_title" to "JubaGo",
        "app_country_badge" to "THUDÄN THÏK",
        "nav_explore" to "Gor (Explore)",
        "nav_tracking" to "Cök GPS",
        "nav_support" to "Luäk (Support)",
        "nav_orders" to "Yök (Orders)",
        "nav_merchant" to "Jiöök (Merchant)",
        "search_placeholder" to "Gor mïth, riɛy, dääc, deel...",
        "verified_only" to "Naath ti cï kuen",
        "offline_cache_active" to "Map thïn",
        "offline_notice" to "Kɛ̈k tɔ̈ amïn. Mɛ̈dɛ̈ intanɛt ka bï wä.",
        "book_now" to "Kɔŋ Ɛn",
        "quick_book" to "Kɔŋ Mal",
        "view_details" to "Nɛn kɛ̈k",
        "hero_title" to "JubaGo Thudän Thïk",
        "hero_subtitle" to "Riɛy • Deel nhial • Dääc • Mïth",
        "hero_tagline" to "Naath Juba • Jiöök tet ku Mobile Money",
        "hero_tutorial_button" to "Raan nyian? Nɛn cök",
        "role_customer" to "Raan ɣööc",
        "role_merchant" to "Raan kɔŋ",
        "register_business" to "Gɔ̈r baai ka riɛy du",
        "chat_support_title" to "Naath luääk",
        "live_tracking_title" to "Cök GPS kɛ̈n",
        "merchant_dashboard_title" to "Kuan ɣööc",
        "select_language" to "Liɛc thɔk (Language)",
        "sync_online" to "Tɔ̈ thïn (Online)",
        "sync_syncing" to "Lät (Syncing)",
        "sync_offline" to "Thile net (Offline)"
    )
}
