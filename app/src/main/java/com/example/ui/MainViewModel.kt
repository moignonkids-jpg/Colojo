package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.ChatMessageEntity
import com.example.data.GeminiService
import com.example.data.MatchEntity
import com.example.data.SavedAnalysisEntity
import com.example.data.UserEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

import com.example.data.isTopMatch
import android.net.Uri
import java.io.InputStream
import android.util.Base64

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository
    private val geminiService = GeminiService()

    private val _matches = MutableStateFlow<List<MatchEntity>>(emptyList())
    val matches: StateFlow<List<MatchEntity>> = _matches.asStateFlow()

    private val _savedAnalyses = MutableStateFlow<List<SavedAnalysisEntity>>(emptyList())
    val savedAnalyses: StateFlow<List<SavedAnalysisEntity>> = _savedAnalyses.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessageEntity>> = _chatMessages.asStateFlow()

    // Attachment State
    private val _selectedAttachmentUri = MutableStateFlow<Uri?>(null)
    val selectedAttachmentUri: StateFlow<Uri?> = _selectedAttachmentUri.asStateFlow()

    private val _selectedAttachmentName = MutableStateFlow<String?>(null)
    val selectedAttachmentName: StateFlow<String?> = _selectedAttachmentName.asStateFlow()

    private val _selectedAttachmentMimeType = MutableStateFlow<String?>(null)
    val selectedAttachmentMimeType: StateFlow<String?> = _selectedAttachmentMimeType.asStateFlow()

    // UI state for Match Analysis
    private val _selectedMatch = MutableStateFlow<MatchEntity?>(null)
    val selectedMatch: StateFlow<MatchEntity?> = _selectedMatch.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _currentAnalysis = MutableStateFlow<String?>(null)
    val currentAnalysis: StateFlow<String?> = _currentAnalysis.asStateFlow()

    // Countdown for 1-hour automatic updates
    private val _secondsToNextUpdate = MutableStateFlow(3600) // 1 hour = 3600 seconds
    val secondsToNextUpdate: StateFlow<Int> = _secondsToNextUpdate.asStateFlow()

    // Text input state for chat
    private val _chatInput = MutableStateFlow("")
    val chatInput: StateFlow<String> = _chatInput.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // Authentication States
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _allUsers = MutableStateFlow<List<UserEntity>>(emptyList())
    val allUsers: StateFlow<List<UserEntity>> = _allUsers.asStateFlow()

    // Form fields
    private val _loginUsername = MutableStateFlow("")
    val loginUsername: StateFlow<String> = _loginUsername.asStateFlow()

    private val _loginPassword = MutableStateFlow("")
    val loginPassword: StateFlow<String> = _loginPassword.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _newUsername = MutableStateFlow("")
    val newUsername: StateFlow<String> = _newUsername.asStateFlow()

    private val _newPassword = MutableStateFlow("")
    val newPassword: StateFlow<String> = _newPassword.asStateFlow()

    private val _createAccountError = MutableStateFlow<String?>(null)
    val createAccountError: StateFlow<String?> = _createAccountError.asStateFlow()

    private val _createAccountSuccess = MutableStateFlow<String?>(null)
    val createAccountSuccess: StateFlow<String?> = _createAccountSuccess.asStateFlow()

    private var countdownJob: Job? = null
    private var liveSimulationJob: Job? = null

    init {
        val appDao = AppDatabase.getDatabase(application).appDao()
        repository = AppRepository(appDao)

        // Initialize and fetch matches
        viewModelScope.launch {
            repository.refreshOrPrepopulateMatches()
            
            // Collect matches
            launch {
                repository.allMatches.collectLatest {
                    _matches.value = it
                }
            }

            // Collect saved analyses
            launch {
                repository.allSavedAnalyses.collectLatest {
                    _savedAnalyses.value = it
                }
            }

            // Collect chat history
            launch {
                repository.allChatMessages.collectLatest {
                    if (it.isEmpty()) {
                        // Prepopulate with a friendly welcome message
                        repository.insertChatMessage(
                            ChatMessageEntity(
                                role = "model",
                                content = "Bonjour ! Je suis l'IA de colojo, votre conseiller de football expert de niveau mondial. Posez-moi n'importe quelle question sur les matchs d'aujourd'hui, l'état des équipes ou demandez mes prédictions à 99% de réussite !"
                            )
                        )
                    } else {
                        _chatMessages.value = it
                    }
                }
            }

            // Collect registered users list
            launch {
                repository.allUsers.collectLatest {
                    _allUsers.value = it
                }
            }
        }

        // Start countdown and updates
        startPeriodicUpdateTimer()
        startLiveSimulation()
    }

    private fun startPeriodicUpdateTimer() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_secondsToNextUpdate.value > 1) {
                    _secondsToNextUpdate.value -= 1
                } else {
                    // Trigger hourly update
                    _secondsToNextUpdate.value = 3600
                    repository.simulateLiveUpdates() // This simulates update
                }
            }
        }
    }

    private fun startLiveSimulation() {
        liveSimulationJob?.cancel()
        liveSimulationJob = viewModelScope.launch {
            while (true) {
                // Simulate quick live changes every 15 seconds to make the "LIVE" section look real-time
                delay(15000)
                repository.simulateLiveUpdates()
            }
        }
    }

    fun selectMatch(match: MatchEntity?) {
        _selectedMatch.value = match
        _currentAnalysis.value = null
        _isAnalyzing.value = false
    }

    fun triggerAnalysis(match: MatchEntity) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _currentAnalysis.value = null

            val result = geminiService.getMatchAnalysis(
                homeTeam = match.homeTeam,
                awayTeam = match.awayTeam,
                league = match.league,
                odds1X = match.odd1Xbet.toString(),
                oddsDraw = match.oddXXbet.toString(),
                odds2 = match.odd2Xbet.toString(),
                isLive = match.isLive
            )

            // Extract confidence percentage if possible (regex parsing)
            val confidenceMatch = Regex("Niveau de confiance global : (\\d+)").find(result)
            val confidenceVal = confidenceMatch?.groupValues?.get(1)?.toIntOrNull() ?: 85

            // Parse first score if possible
            val scoreMatch = Regex("Score 1 : (\\d+-\\d+)").find(result)
            val scoreVal = scoreMatch?.groupValues?.get(1) ?: "2-1"

            // Save to Room for history
            repository.insertSavedAnalysis(
                SavedAnalysisEntity(
                    matchId = match.id,
                    homeTeam = match.homeTeam,
                    awayTeam = match.awayTeam,
                    analysisText = result,
                    confidence = confidenceVal,
                    scores = scoreVal
                )
            )

            _currentAnalysis.value = result
            _isAnalyzing.value = false
        }
    }

    fun deleteAnalysis(id: Int) {
        viewModelScope.launch {
            repository.deleteSavedAnalysis(id)
        }
    }

    fun selectAttachment(uri: Uri) {
        _selectedAttachmentUri.value = uri
        val context = getApplication<Application>()
        val contentResolver = context.contentResolver
        var name = "Fichier"
        val mimeType = contentResolver.getType(uri) ?: "*/*"
        _selectedAttachmentMimeType.value = mimeType
        
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex("_display_name")
                if (nameIndex != -1) {
                    name = it.getString(nameIndex)
                }
            }
        }
        _selectedAttachmentName.value = name
    }

    fun clearAttachment() {
        _selectedAttachmentUri.value = null
        _selectedAttachmentName.value = null
        _selectedAttachmentMimeType.value = null
    }

    private fun readUriAsBase64(uri: Uri): String? {
        return try {
            val contentResolver = getApplication<Application>().contentResolver
            val inputStream = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) {
                Base64.encodeToString(bytes, Base64.NO_WRAP)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun readUriAsText(uri: Uri): String? {
        return try {
            val contentResolver = getApplication<Application>().contentResolver
            val inputStream = contentResolver.openInputStream(uri)
            val text = inputStream?.bufferedReader()?.use { it.readText() }
            inputStream?.close()
            text
        } catch (e: Exception) {
            null
        }
    }

    fun onChatInputChanged(input: String) {
        _chatInput.value = input
    }

    fun sendChatMessage() {
        val query = _chatInput.value.trim()
        if (query.isEmpty() && _selectedAttachmentUri.value == null) return

        val attachmentUri = _selectedAttachmentUri.value
        val attachmentName = _selectedAttachmentName.value
        val attachmentMime = _selectedAttachmentMimeType.value

        _chatInput.value = ""
        clearAttachment()

        viewModelScope.launch {
            var finalQuery = query
            var attachmentBase64ToSend: String? = null
            var attachmentMimeToSend: String? = null

            if (attachmentUri != null && attachmentMime != null) {
                val isTextFile = attachmentMime.startsWith("text/") || 
                                 attachmentMime.contains("json") || 
                                 attachmentMime.contains("csv") || 
                                 attachmentMime.contains("xml")
                
                if (isTextFile) {
                    val textContent = readUriAsText(attachmentUri)
                    if (textContent != null) {
                        finalQuery = "Voici le contenu du fichier attaché (${attachmentName ?: "fichier.txt"}) :\n" +
                                     "```\n$textContent\n```\n\n" +
                                     "Question : $query"
                    }
                } else {
                    attachmentBase64ToSend = readUriAsBase64(attachmentUri)
                    attachmentMimeToSend = attachmentMime
                }
            }

            // Append factual Top Matches context to guide colojo AI accurately
            val queryLower = query.lowercase()
            val containsTopMatchWord = queryLower.contains("top") || 
                                       queryLower.contains("meilleur") || 
                                       queryLower.contains("star") || 
                                       queryLower.contains("favori") ||
                                       queryLower.contains("pronostic")

            val topMatchesContext = if (containsTopMatchWord) {
                val matchesList = _matches.value
                val todayTop = matchesList.filter { it.dateOffset == 0 && it.isTopMatch() }
                val tomorrowTop = matchesList.filter { it.dateOffset == 1 && it.isTopMatch() }
                val yesterdayTop = matchesList.filter { it.dateOffset == -1 && it.isTopMatch() }
                
                buildString {
                    append("\n\n[CONTEXTE DE PRÉCISION SYSTEM : Voici la liste exacte des Top Matchs de la base de données :\n")
                    append("- Aujourd'hui : ")
                    if (todayTop.isEmpty()) append("Aucun Top Match aujourd'hui.")
                    else todayTop.forEach { append("${it.homeTeam} vs ${it.awayTeam} (Pronostic: Victoire de ${if (it.odd1Xbet < it.odd2Xbet) it.homeTeam else it.awayTeam}); ") }
                    append("\n- Demain : ")
                    if (tomorrowTop.isEmpty()) append("Aucun Top Match demain.")
                    else tomorrowTop.forEach { append("${it.homeTeam} vs ${it.awayTeam} (Pronostic: Victoire de ${if (it.odd1Xbet < it.odd2Xbet) it.homeTeam else it.awayTeam}); ") }
                    append("\n- Hier : ")
                    if (yesterdayTop.isEmpty()) append("Aucun Top Match hier.")
                    else yesterdayTop.forEach { append("${it.homeTeam} vs ${it.awayTeam} (Pronostic: Victoire de ${if (it.odd1Xbet < it.odd2Xbet) it.homeTeam else it.awayTeam}); ") }
                    append("\nFin du contexte]")
                }
            } else {
                ""
            }

            val promptToSend = finalQuery + topMatchesContext

            // Save user message in local history
            val userMsg = ChatMessageEntity(
                role = "user",
                content = if (query.isEmpty() && attachmentName != null) "A envoyé un fichier : $attachmentName" else query,
                attachmentName = attachmentName,
                attachmentMimeType = attachmentMime,
                attachmentData = attachmentBase64ToSend
            )
            repository.insertChatMessage(userMsg)

            _isChatLoading.value = true

            // Get AI answer (multimodal)
            val answer = geminiService.getChatAnswer(
                question = promptToSend, 
                chatHistory = _chatMessages.value,
                attachmentBase64 = attachmentBase64ToSend,
                attachmentMimeType = attachmentMimeToSend
            )

            // Save AI response
            repository.insertChatMessage(
                ChatMessageEntity(role = "model", content = answer)
            )

            _isChatLoading.value = false
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChatHistory()
        }
    }

    fun forceRefresh() {
        viewModelScope.launch {
            _secondsToNextUpdate.value = 3600
            repository.simulateLiveUpdates()
        }
    }

    // --- Authentication Actions ---
    fun onLoginUsernameChanged(value: String) {
        _loginUsername.value = value
        _loginError.value = null
    }

    fun onLoginPasswordChanged(value: String) {
        _loginPassword.value = value
        _loginError.value = null
    }

    fun onNewUsernameChanged(value: String) {
        _newUsername.value = value
        _createAccountError.value = null
        _createAccountSuccess.value = null
    }

    fun onNewPasswordChanged(value: String) {
        _newPassword.value = value
        _createAccountError.value = null
        _createAccountSuccess.value = null
    }

    fun login() {
        viewModelScope.launch {
            val username = _loginUsername.value.trim()
            val password = _loginPassword.value

            if (username.isEmpty() || password.isEmpty()) {
                _loginError.value = "Le nom d'utilisateur et le mot de passe sont obligatoires."
                return@launch
            }

            val user = repository.getUserByUsername(username)
            if (user != null && user.passwordHash == password) {
                _currentUser.value = user
                _loginError.value = null
                _loginUsername.value = ""
                _loginPassword.value = ""
            } else {
                _loginError.value = "Identifiants incorrects."
            }
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    fun createAccount() {
        viewModelScope.launch {
            val username = _newUsername.value.trim()
            val password = _newPassword.value

            if (username.isEmpty() || password.isEmpty()) {
                _createAccountError.value = "Veuillez remplir tous les champs."
                return@launch
            }

            if (!username.all { it.isLetterOrDigit() } || !password.all { it.isLetterOrDigit() }) {
                _createAccountError.value = "Le nom et le mot de passe doivent contenir uniquement des lettres ou des chiffres."
                return@launch
            }

            val existing = repository.getUserByUsername(username)
            if (existing != null) {
                _createAccountError.value = "Ce nom d'utilisateur existe déjà."
                return@launch
            }

            val newUser = UserEntity(username = username, passwordHash = password)
            repository.insertUser(newUser)
            _createAccountSuccess.value = "Compte '$username' créé avec succès !"
            _newUsername.value = ""
            _newPassword.value = ""
            _createAccountError.value = null
        }
    }

    fun deleteUser(username: String) {
        viewModelScope.launch {
            if (username == "COUTHON") {
                return@launch
            }
            repository.deleteUserByUsername(username)
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
        liveSimulationJob?.cancel()
    }
}
