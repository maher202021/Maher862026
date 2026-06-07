package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.data.local.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase(application.applicationContext)
    private val repository = AppRepository(db)

    // --- Filter States ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("الكل")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedCity = MutableStateFlow("الكل")
    val selectedCity: StateFlow<String> = _selectedCity.asStateFlow()

    private val _selectedGender = MutableStateFlow("الكل")
    val selectedGender: StateFlow<String> = _selectedGender.asStateFlow()

    // --- Active Interface Segment ---
    private val _currentScreen = MutableStateFlow("home")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // --- AI Chat State ---
    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // --- Admin Validation Gate ---
    private val _isAdminAuthenticated = MutableStateFlow(false)
    val isAdminAuthenticated: StateFlow<Boolean> = _isAdminAuthenticated.asStateFlow()

    // --- Real-time filtered approved provider listings ---
    val approvedProviders: StateFlow<List<Provider>> = combine(
        repository.approvedProviders,
        _searchQuery,
        _selectedCategory,
        _selectedCity,
        _selectedGender
    ) { providers, query, category, city, gender ->
        providers.filter { provider ->
            val matchesQuery = query.isEmpty() || 
                    provider.name.contains(query, ignoreCase = true) || 
                    provider.description.contains(query, ignoreCase = true) || 
                    provider.subCategory.contains(query, ignoreCase = true)
            val matchesCategory = category == "الكل" || provider.mainCategory == category
            val matchesCity = city == "الكل" || provider.city == city
            val matchesGender = gender == "الكل" || provider.gender == gender

            matchesQuery && matchesCategory && matchesCity && matchesGender
        }
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Other Database Flows ---
    val pendingProviders: StateFlow<List<Provider>> = repository.pendingProviders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedProviders: StateFlow<List<Provider>> = repository.bookmarkedProviders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminConfig: StateFlow<AdminConfig> = repository.adminConfig
        .map { it ?: AdminConfig() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdminConfig())

    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Pre-populate database with default configurations and popular technicians immediately on first load
        viewModelScope.launch(Dispatchers.IO) {
            repository.prepopulateDatabaseIfEmpty()
        }
    }

    // --- Interface Setters ---
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun updateSelectedCity(city: String) {
        _selectedCity.value = city
    }

    fun updateSelectedGender(gender: String) {
        _selectedGender.value = gender
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    // --- Provider Actions ---
    fun toggleBookmark(provider: Provider) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleBookmark(provider)
        }
    }

    fun submitPendingProvider(
        name: String,
        mainCategory: String,
        subCategory: String,
        city: String,
        phone: String,
        whatsapp: String,
        gender: String,
        description: String,
        photoUri: String?,
        idPhotoUri: String?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val provider = Provider(
                name = name,
                mainCategory = mainCategory,
                subCategory = subCategory,
                city = city,
                phone = phone,
                whatsapp = whatsapp,
                gender = gender,
                description = description,
                photoUri = photoUri,
                idPhotoUri = idPhotoUri,
                isPending = true, // Sent to Admin Inbox
                isVerified = false,
                rating = 4.5f,
                votes = 0
            )
            repository.insertProvider(provider)
        }
    }

    fun approveProvider(provider: Provider) {
        viewModelScope.launch(Dispatchers.IO) {
            val approved = provider.copy(isPending = false, isVerified = true)
            repository.insertProvider(approved)
        }
    }

    fun deleteProvider(provider: Provider) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteProvider(provider)
        }
    }

    // --- AI Chat Actions ---
    fun sendChatMessage(userText: String) {
        if (userText.trim().isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            // Save user message to database
            repository.insertChatMessage("user", userText)
            
            _isChatLoading.value = true
            
            // Fetch configuration to get current base pricing estimates and system rules
            val config = repository.getAdminConfigSingle()
            val history = chatMessages.value
            
            // Query local AI
            val botReply = repository.getGeminiResponse(userText, history, config.baseAppRateHourYER)
            
            // Save robot reply to database
            repository.insertChatMessage("ai", botReply)
            _isChatLoading.value = false
        }
    }

    fun clearChatMessages() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearChat()
        }
    }

    // --- Admin Authentication & Settings ---
    fun authenticateAdmin(pin: String): Boolean {
        val actualPin = adminConfig.value.secretKey
        val authenticated = pin == actualPin
        _isAdminAuthenticated.value = authenticated
        return authenticated
    }

    fun logOutAdmin() {
        _isAdminAuthenticated.value = false
    }

    fun saveAdminConfig(config: AdminConfig) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateAdminConfig(config)
        }
    }
}
