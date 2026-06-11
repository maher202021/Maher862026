package com.example.data

import com.example.data.local.*
import kotlinx.coroutines.flow.StateFlow

class AppRepository(private val db: AppDatabase) {

    val approvedProvidersFlow: StateFlow<List<Provider>> = db.approvedProvidersFlow
    val pendingProvidersFlow: StateFlow<List<Provider>> = db.pendingProvidersFlow
    val chatsFlow: StateFlow<List<ChatMessage>> = db.chatsFlow
    val categoriesFlow: StateFlow<List<CategoryItem>> = db.categoriesFlow
    val citiesFlow: StateFlow<List<CityItem>> = db.citiesFlow
    val adminConfigFlow: StateFlow<AdminConfig> = db.adminConfigFlow
    val bannersFlow: StateFlow<List<BannerAd>> = db.bannersFlow
    val reportsFlow: StateFlow<List<ReportItem>> = db.reportsFlow
    val supervisorsFlow: StateFlow<List<AppSupervisor>> = db.supervisorsFlow
    val bookmarkedIds: StateFlow<Set<String>> = db.bookmarkedIds

    fun toggleBookmark(id: String): Boolean = db.toggleBookmark(id)

    fun updateConfig(config: AdminConfig, onComplete: (Boolean) -> Unit) {
        db.updateConfigInFirestore(config, onComplete)
    }

    fun submitProviderRequest(provider: Provider, onComplete: (Boolean) -> Unit) {
        db.submitProviderRequest(provider, onComplete)
    }

    fun approveRequest(p: Provider, onComplete: (Boolean) -> Unit) {
        db.approveRequest(p, onComplete)
    }

    fun rejectRequest(p: Provider, reason: String, onComplete: (Boolean) -> Unit) {
        db.rejectRequest(p, reason, onComplete)
    }

    fun deleteProvider(id: String, onComplete: (Boolean) -> Unit) {
        db.deleteProvider(id, onComplete)
    }

    fun sendChatMessage(msg: ChatMessage, onComplete: (Boolean) -> Unit) {
        db.sendChatMessage(msg, onComplete)
    }

    fun clearChatHistory(onComplete: (Boolean) -> Unit) {
        db.clearChatHistory(onComplete)
    }

    fun saveBanner(banner: BannerAd, onComplete: (Boolean) -> Unit) {
        db.saveBanner(banner, onComplete)
    }

    fun deleteBanner(id: String, onComplete: (Boolean) -> Unit) {
        db.deleteBanner(id, onComplete)
    }

    fun saveCategory(cat: CategoryItem, onComplete: (Boolean) -> Unit) {
        db.saveCategory(cat, onComplete)
    }

    fun deleteCategory(id: String, onComplete: (Boolean) -> Unit) {
        db.deleteCategory(id, onComplete)
    }

    fun saveCity(city: CityItem, onComplete: (Boolean) -> Unit) {
        db.saveCity(city, onComplete)
    }

    fun deleteCity(id: String, onComplete: (Boolean) -> Unit) {
        db.deleteCity(id, onComplete)
    }

    fun submitReport(report: ReportItem, onComplete: (Boolean) -> Unit) {
        db.submitReport(report, onComplete)
    }

    fun saveSupervisor(sv: AppSupervisor, onComplete: (Boolean) -> Unit) {
        db.saveSupervisor(sv, onComplete)
    }

    fun deleteSupervisor(username: String, onComplete: (Boolean) -> Unit) {
        db.deleteSupervisor(username, onComplete)
    }

    fun nukeAllAppFilesAndLocalData(onComplete: (Boolean) -> Unit) {
        db.nukeAllAppFilesAndLocalData(onComplete)
    }
}
