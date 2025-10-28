package com.renttracker.app.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Owners : Screen("owners")
    object OwnerDetail : Screen("owner_detail/{ownerId}") {
        fun createRoute(ownerId: Long) = "owner_detail/$ownerId"
    }
    object Buildings : Screen("buildings")
    object BuildingDetail : Screen("building_detail/{buildingId}") {
        fun createRoute(buildingId: Long) = "building_detail/$buildingId"
    }
    object Tenants : Screen("tenants")
    object TenantDetail : Screen("tenant_detail/{tenantId}") {
        fun createRoute(tenantId: Long) = "tenant_detail/$tenantId"
    }
    object Payments : Screen("payments")
    object PaymentDetail : Screen("payment_detail/{leaseId}") {
        fun createRoute(leaseId: Long) = "payment_detail/$leaseId"
    }
    object PaymentEdit : Screen("payment_edit/{paymentId}") {
        fun createRoute(paymentId: Long) = "payment_edit/$paymentId"
    }
    object TenantPaymentHistory : Screen("tenant_payment_history/{tenantId}") {
        fun createRoute(tenantId: Long) = "tenant_payment_history/$tenantId"
    }
    object Documents : Screen("documents")
    object Reports : Screen("reports")
    object Settings : Screen("settings")
}
