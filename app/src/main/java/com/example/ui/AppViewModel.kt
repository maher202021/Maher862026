package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.data.api.GeminiService
import com.example.data.local.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(application)
    private val geminiService = GeminiService()

    // Database core states using StateFlow
    val categories = repository.categories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val providers = repository.providers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val pendingProviders = repository.pendingProviders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val banners = repository.banners.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val chats = repository.chats.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val config = repository.config.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppConfigEntity())
    val loyaltyPoints = repository.loyaltyPoints.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LoyaltyPointsEntity())
    val activityLogs = repository.activityLogs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _selectedLocation = MutableStateFlow<String?>(null)
    val selectedLocation = _selectedLocation.asStateFlow()

    private val _onlyVip = MutableStateFlow(false)
    val onlyVip = _onlyVip.asStateFlow()

    // Filtered providers
    val filteredProviders = combine(providers, searchQuery, selectedCategory, selectedLocation, onlyVip) { provs, query, cat, loc, vipOnly ->
        provs.filter { provider ->
            val matchesQuery = query.isEmpty() || 
                    provider.name.contains(query, ignoreCase = true) || 
                    provider.subCategory.contains(query, ignoreCase = true) || 
                    provider.location.contains(query, ignoreCase = true) ||
                    provider.phone.contains(query)
            
            val matchesCategory = cat == null || provider.mainCategory == cat || provider.subCategory == cat
            val matchesLocation = loc == null || provider.location.contains(loc, ignoreCase = true)
            val matchesVip = !vipOnly || provider.isVip || provider.isRecommended

            matchesQuery && matchesCategory && matchesLocation && matchesVip && !provider.isBlocked
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Chat room messaging
    private val _activeChatId = MutableStateFlow<String?>(null)
    val activeChatId = _activeChatId.asStateFlow()

    val currentChatMessages = _activeChatId.flatMapLatest { id ->
        if (id != null) repository.getMessages(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Smart assistant state
    private val _assistantChat = MutableStateFlow<List<Pair<String, Boolean>>>(
        // Message -> IsAI
        listOf("أهلاً بك في خدمات اليمن الذكية! 🤖 كيف أستطيع مساعدتك اليوم؟" to true)
    )
    val assistantChat = _assistantChat.asStateFlow()

    private val _isAssistantTyping = MutableStateFlow(false)
    val isAssistantTyping = _isAssistantTyping.asStateFlow()

    // Current screen navigation state
    private val _currentScreen = MutableStateFlow("home") // "home", "register", "secret_backdoor", "admin"
    val currentScreen = _currentScreen.asStateFlow()

    // Verification metrics (telemetry simulation)
    val visitsCount = providers.map { list -> list.sumOf { it.visitsCount } + 850 }
    val approvedCalls = MutableStateFlow(128)

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(cat: String?) {
        _selectedCategory.value = cat
    }

    fun selectLocation(loc: String?) {
        _selectedLocation.value = loc
    }

    fun toggleOnlyVip(onlyVip: Boolean) {
        _onlyVip.value = onlyVip
    }

    // Call service increment
    fun triggerCall(provider: ServiceProviderEntity) {
        viewModelScope.launch {
            repository.updateProvider(provider.copy(visitsCount = provider.visitsCount + 1))
            approvedCalls.value += 1
            repository.logActivity("اتصال هاتفي بأرقام الفني والمهندس: ${provider.name}")
        }
    }

    fun updateProvider(provider: ServiceProviderEntity) {
        viewModelScope.launch {
            repository.updateProvider(provider)
        }
    }

    // Floating AI response
    fun askAssistant(prompt: String) {
        if (prompt.trim().isEmpty()) return
        val currentList = _assistantChat.value.toMutableList()
        currentList.add(prompt to false)
        _assistantChat.value = currentList

        _isAssistantTyping.value = true
        viewModelScope.launch {
            val answer = geminiService.getResponse(prompt)
            val updatedList = _assistantChat.value.toMutableList()
            updatedList.add(answer to true)
            _assistantChat.value = updatedList
            _isAssistantTyping.value = false
        }
    }

    fun clearAssistantChat() {
        _assistantChat.value = listOf("أهلاً بك في خدمات اليمن الذكية! 🤖 كيف أستطيع مساعدتك اليوم؟" to true)
    }

    // Submit provider from UI
    fun submitPendingProvider(
        name: String,
        mainCategory: String,
        subCategory: String,
        city: String,
        phone: String,
        whatsapp: String,
        gender: String,
        photoUri: String?,
        idPhotoUri: String?
    ) {
        viewModelScope.launch {
            val pending = PendingProviderEntity(
                name = name,
                mainCategory = mainCategory,
                subCategory = subCategory,
                location = city,
                phone = phone,
                whatsapp = whatsapp,
                genderOptional = gender,
                photoUri = photoUri,
                idPhotoUri = idPhotoUri
            )
            repository.submitRegistration(pending)
        }
    }

    // Admin direct integrations
    fun acceptPendingProvider(pendingId: Int, pending: PendingProviderEntity) {
        viewModelScope.launch {
            repository.approveProvider(pendingId, pending)
        }
    }

    fun rejectPendingProvider(pendingId: Int, name: String, reason: String) {
        viewModelScope.launch {
            repository.rejectProvider(pendingId, name, reason)
        }
    }

    fun removeProvider(id: Int) {
        viewModelScope.launch {
            repository.removeProvider(id)
        }
    }

    fun addProviderManually(
        name: String,
        mainCat: String,
        subCat: String,
        location: String,
        phone: String,
        whatsapp: String,
        isVip: Boolean,
        isRecommended: Boolean,
        notes: String
    ) {
        viewModelScope.launch {
            val p = ServiceProviderEntity(
                name = name,
                mainCategory = mainCat,
                subCategory = subCat,
                location = location,
                phone = phone,
                whatsapp = whatsapp,
                isVip = isVip,
                isRecommended = isRecommended,
                notes = notes
            )
            repository.addProvider(p)
        }
    }

    fun addCategory(name: String, group: String, icon: String, pinned: Boolean) {
        viewModelScope.launch {
            repository.addCategory(CategoryEntity(name = name, groupName = group, iconName = icon, isPinned = pinned))
        }
    }

    fun removeCategory(id: Int) {
        viewModelScope.launch {
            repository.removeCategory(id)
        }
    }

    fun addBanner(title: String, type: String, linkUrl: String, textMessage: String) {
        viewModelScope.launch {
            repository.addBanner(
                BannerEntity(
                    title = title,
                    type = type,
                    contentUrl = if (type == "صورة") "https://images.unsplash.com/photo-1542744094-3a31f103e35f?w=600" else "",
                    textMessage = textMessage,
                    link = linkUrl
                )
            )
        }
    }

    fun removeBanner(id: Int) {
        viewModelScope.launch {
            repository.removeBanner(id)
        }
    }

    fun exchangePoints(pointsToExchange: Int, rewardTitle: String) {
        viewModelScope.launch {
            repository.deductPoints(pointsToExchange, rewardTitle)
        }
    }

    fun sendLiveChatMessage(text: String) {
        val chatId = _activeChatId.value ?: "guest_admin_777644670"
        viewModelScope.launch {
            // Send user message
            repository.sendChatMessage(
                chatId = chatId,
                sender = "user",
                text = text,
                providerId = 0,
                providerName = "الإدارة والدعم الفني"
            )

            // Simulate admin instant response
            if (chatId.contains("admin")) {
                kotlinx.coroutines.delay(1200)
                repository.sendChatMessage(
                    chatId = chatId,
                    sender = "admin",
                    text = "شكراً لتواصلك معنا فني كل خدمات اليمن، المطور والمهندس ماهر علوان وفريق الإدارة يرحبون بك. تم استلام رسالتك وسيجيب عليك المشرف فوراً.",
                    providerId = 0,
                    providerName = "الإدارة والدعم الفني"
                )
            }
        }
    }

    fun sendAdminReplyMessage(chatId: String, text: String) {
        viewModelScope.launch {
            repository.sendChatMessage(
                chatId = chatId,
                sender = "admin",
                text = text,
                providerId = 0,
                providerName = "الإدارة والدعم الفني"
            )
        }
    }

    fun toggleChatRoomStatus(chatId: String, disabled: Boolean) {
        viewModelScope.launch {
            repository.setChatDisabled(chatId, disabled)
        }
    }

    fun openSupportChat(userEmail: String = "user_${System.currentTimeMillis() % 1000}@yemen.com") {
        _activeChatId.value = "${userEmail}_admin_المشرف"
    }

    fun updateConfig(updated: AppConfigEntity) {
        viewModelScope.launch {
            repository.updateConfig(updated)
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.logActivity("تصفير وتدوير كافة سجلات النشاط يدوياً من قبل الإدارة")
        }
    }
}
