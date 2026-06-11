package com.example.ui

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.data.api.GeminiClient
import com.example.data.local.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase(application)
    private val repository = AppRepository(db)

    // --- Dynamic Settings / Streams from Repository ---
    val approvedProviders = repository.approvedProvidersFlow
    val pendingProviders = repository.pendingProvidersFlow
    val chats = repository.chatsFlow
    val categories = repository.categoriesFlow
    val cities = repository.citiesFlow
    val adminConfig = repository.adminConfigFlow
    val banners = repository.bannersFlow
    val reports = repository.reportsFlow
    val supervisors = repository.supervisorsFlow
    val bookmarkedIds = repository.bookmarkedIds

    // --- UI/UX Interactive Filter States ---
    private val _selectedCategory = MutableStateFlow<String>("")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _selectedCity = MutableStateFlow<String>("")
    val selectedCity = _selectedCity.asStateFlow()

    private val _searchQuery = MutableStateFlow<String>("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchRadiusKm = MutableStateFlow<Float>(10f) // GPS circle radius filter
    val searchRadiusKm = _searchRadiusKm.asStateFlow()

    // Combined filtered providers for real-time safe rendering
    val filteredProviders = combine(
        approvedProviders,
        _selectedCategory,
        _selectedCity,
        _searchQuery
    ) { list, cat, city, query ->
        list.filter { p ->
            val matchesCat = cat.isEmpty() || p.serviceCategory == cat || p.subCategory == cat
            val matchesCity = city.isEmpty() || p.locationCity == city
            val matchesSearch = query.isEmpty() || 
                    p.name.contains(query, ignoreCase = true) || 
                    p.phone.contains(query, ignoreCase = true) || 
                    p.locationRegion.contains(query, ignoreCase = true)
            matchesCat && matchesCity && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Navigation Flow ---
    private val _currentRoute = MutableStateFlow<String>("home")
    val currentRoute = _currentRoute.asStateFlow()

    private val routeBackstack = mutableListOf<String>()

    // App language preference ("ar" fallback / "en")
    private val _appLanguage = MutableStateFlow<String>("ar")
    val appLanguage = _appLanguage.asStateFlow()

    // --- Authentication States ---
    private val _isAdminAuthenticated = MutableStateFlow(false)
    val isAdminAuthenticated = _isAdminAuthenticated.asStateFlow()

    private val _currentSupervisor = MutableStateFlow<AppSupervisor?>(null)
    val currentSupervisor = _currentSupervisor.asStateFlow()

    private val _isBackdoorAuthenticated = MutableStateFlow(false)
    val isBackdoorAuthenticated = _isBackdoorAuthenticated.asStateFlow()

    private val _isBackdoorRemembered = MutableStateFlow(false)
    val isBackdoorRemembered = _isBackdoorRemembered.asStateFlow()

    // Active Chat Selection User
    private val _activeChatRoomId = MutableStateFlow<String>("general")
    val activeChatRoomId = _activeChatRoomId.asStateFlow()

    // --- Smart Assistant AI state ---
    private val _assistantChatLog = MutableStateFlow<List<Pair<String, Boolean>>>(
        listOf("أهلاً بك! أنا مساعد خدمات WAM الذكي 🤖. كيف يمكنني مساعدتك فلياً اليوم؟" to false)
    )
    val assistantChatLog = _assistantChatLog.asStateFlow()

    private val _isAssistantLoading = MutableStateFlow(false)
    val isAssistantLoading = _isAssistantLoading.asStateFlow()

    init {
        // Load persistent App settings from local SharedPreferences
        val prefs = application.getSharedPreferences("WAM_SERVICES_PREFS", Context.MODE_PRIVATE)
        _appLanguage.value = prefs.getString("lang", "ar") ?: "ar"
        _isBackdoorRemembered.value = prefs.getBoolean("backdoor_remember", false)
        if (_isBackdoorRemembered.value) {
            _isBackdoorAuthenticated.value = prefs.getBoolean("backdoor_authenticated", false)
        }
    }

    // --- Navigation Helpers ---
    fun navigateTo(route: String) {
        if (_currentRoute.value != route) {
            routeBackstack.add(_currentRoute.value)
            _currentRoute.value = route
        }
    }

    fun handleBackPress(onExit: () -> Unit) {
        if (routeBackstack.isNotEmpty()) {
            _currentRoute.value = routeBackstack.removeAt(routeBackstack.size - 1)
        } else {
            onExit()
        }
    }

    // --- Language Managers ---
    fun toggleLanguage() {
        val next = if (_appLanguage.value == "ar") "en" else "ar"
        _appLanguage.value = next
        val prefs = getApplication<Application>().getSharedPreferences("WAM_SERVICES_PREFS", Context.MODE_PRIVATE)
        prefs.edit().putString("lang", next).apply()
    }

    // --- Filter Mutator Triggers ---
    fun setCategoryFilter(id: String) {
        _selectedCategory.value = id
    }

    fun setCityFilter(id: String) {
        _selectedCity.value = id
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setRadiusKm(radius: Float) {
        _searchRadiusKm.value = radius
    }

    fun toggleBookmark(id: String) {
        repository.toggleBookmark(id)
    }

    // --- Cloud mutations proxy triggers ---
    fun submitRequest(provider: Provider, onComplete: (Boolean) -> Unit) {
        repository.submitProviderRequest(provider, onComplete)
    }

    fun approveRegistration(p: Provider, onComplete: (Boolean) -> Unit) {
        repository.approveRequest(p, onComplete)
    }

    fun rejectRegistration(p: Provider, reason: String, onComplete: (Boolean) -> Unit) {
        repository.rejectRequest(p, reason, onComplete)
    }

    fun deleteProvider(id: String, onComplete: (Boolean) -> Unit) {
        repository.deleteProvider(id, onComplete)
    }

    fun saveBanner(banner: BannerAd, onComplete: (Boolean) -> Unit) {
        repository.saveBanner(banner, onComplete)
    }

    fun deleteBanner(id: String, onComplete: (Boolean) -> Unit) {
        repository.deleteBanner(id, onComplete)
    }

    fun saveCategory(cat: CategoryItem, onComplete: (Boolean) -> Unit) {
        repository.saveCategory(cat, onComplete)
    }

    fun deleteCategory(id: String, onComplete: (Boolean) -> Unit) {
        repository.deleteCategory(id, onComplete)
    }

    fun saveCity(city: CityItem, onComplete: (Boolean) -> Unit) {
        repository.saveCity(city, onComplete)
    }

    fun deleteCity(id: String, onComplete: (Boolean) -> Unit) {
        repository.deleteCity(id, onComplete)
    }

    fun sendChatMessage(msg: ChatMessage, onComplete: (Boolean) -> Unit) {
        repository.sendChatMessage(msg, onComplete)
    }

    fun clearChatHistory(onComplete: (Boolean) -> Unit) {
        repository.clearChatHistory(onComplete)
    }

    fun submitReport(report: ReportItem, onComplete: (Boolean) -> Unit) {
        repository.submitReport(report, onComplete)
    }

    fun saveSupervisor(sv: AppSupervisor, onComplete: (Boolean) -> Unit) {
        repository.saveSupervisor(sv, onComplete)
    }

    fun deleteSupervisor(username: String, onComplete: (Boolean) -> Unit) {
        repository.deleteSupervisor(username, onComplete)
    }

    fun updateConfig(config: AdminConfig, onComplete: (Boolean) -> Unit) {
        repository.updateConfig(config, onComplete)
    }

    // --- Authentication Operations ---
    fun loginAdmin(user: String, pass: String): Boolean {
        val config = adminConfig.value
        if (user == config.adminUsername && pass == config.adminPassword) {
            _isAdminAuthenticated.value = true
            _currentSupervisor.value = AppSupervisor(
                username = config.adminUsername,
                secretPin = config.adminPassword,
                canApproveRequests = true,
                canManageCategories = true,
                canEditAds = true,
                canDeleteProviders = true,
                canReadReports = true
            )
            return true
        }

        // Check if supervisor login
        val matchedSupervisor = supervisors.value.find { it.username == user && it.secretPin == pass }
        if (matchedSupervisor != null) {
            _isAdminAuthenticated.value = true
            _currentSupervisor.value = matchedSupervisor
            return true
        }
        return false
    }

    fun logoutAdmin() {
        _isAdminAuthenticated.value = false
        _currentSupervisor.value = null
    }

    fun authenticateBackdoor(pin: String, remember: Boolean): Boolean {
        val config = adminConfig.value
        val isMatch = pin == config.secretKey || pin == "maher--736462"
        if (isMatch) {
            _isBackdoorAuthenticated.value = true
            _isBackdoorRemembered.value = remember
            val prefs = getApplication<Application>().getSharedPreferences("WAM_SERVICES_PREFS", Context.MODE_PRIVATE)
            prefs.edit().apply {
                putBoolean("backdoor_remember", remember)
                putBoolean("backdoor_authenticated", remember)
                apply()
            }
        }
        return isMatch
    }

    fun logoutBackdoor() {
        _isBackdoorAuthenticated.value = false
        val prefs = getApplication<Application>().getSharedPreferences("WAM_SERVICES_PREFS", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("backdoor_remember", false)
            putBoolean("backdoor_authenticated", false)
            apply()
        }
    }

    // --- Gemini Interactive AI Messaging Scope ---
    fun askAssistant(prompt: String) {
        if (prompt.trim().isEmpty()) return
        
        // Add user greeting text locally
        val currentLog = _assistantChatLog.value.toMutableList()
        currentLog.add(prompt to true)
        _assistantChatLog.value = currentLog
        _isAssistantLoading.value = true

        viewModelScope.launch {
            val responseText = GeminiClient.askGemini(prompt, "")
            _isAssistantLoading.value = false
            val updatedLog = _assistantChatLog.value.toMutableList()
            updatedLog.add(responseText to false)
            _assistantChatLog.value = updatedLog
        }
    }

    fun clearAssistantHistory() {
        _assistantChatLog.value = listOf("مرحباً بك! أنا مساعد WAM الذكي والمستعد لمساعدتك الفنية. كيف يمكنني خدمتك فلياً اليوم؟" to false)
    }

    // --- OWNER EMERGENCY NUKE OPERATION ---
    fun formulaNukeEverything(onComplete: (Boolean) -> Unit) {
        logoutAdmin()
        logoutBackdoor()
        repository.nukeAllAppFilesAndLocalData { success ->
            if (success) {
                // Clear state flows & defaults
                _selectedCategory.value = ""
                _selectedCity.value = ""
                _searchQuery.value = ""
                _currentRoute.value = "home"
                clearAssistantHistory()
            }
            onComplete(success)
        }
    }
}
