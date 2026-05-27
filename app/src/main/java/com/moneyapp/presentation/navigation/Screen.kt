package com.moneyapp.presentation.navigation

/**
 * Sealed class yang mendefinisikan semua route navigasi dalam aplikasi.
 * Setiap object merepresentasikan satu layar/destination.
 */
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object Transaction : Screen("transaction")
    object TransactionForm : Screen("transaction/form/{transactionId}") {
        fun createRoute(id: Long? = null) = "transaction/form/${id ?: -1}"
    }
    object TransactionDetail : Screen("transaction/detail/{transactionId}") {
        fun createRoute(id: Long) = "transaction/detail/$id"
    }
    object Investment : Screen("investment")
    object InvestmentForm : Screen("investment/form/{investmentId}") {
        fun createRoute(id: Long? = null) = "investment/form/${id ?: -1}"
    }
    object Saving : Screen("saving")
    object SavingForm : Screen("saving/form/{savingId}") {
        fun createRoute(id: Long? = null) = "saving/form/${id ?: -1}"
    }
    object Report : Screen("report")
    object Settings : Screen("settings")
}
