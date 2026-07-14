package com.example.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.example.data.isTopMatch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatMessageEntity
import com.example.data.MatchEntity
import com.example.data.SavedAnalysisEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Modern Palette
val SlateDark = Color(0xFF0F172A)
val SlateMedium = Color(0xFF1E293B)
val SlateLight = Color(0xFF334155)
val EmeraldGreen = Color(0xFF10B981)
val AccentGold = Color(0xFFF59E0B)
val OneXbetBlue = Color(0xFF1D4ED8)
val BetpawaGreen = Color(0xFF84CC16)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    if (currentUser == null) {
        LoginScreen(viewModel = viewModel)
        return
    }

    val matches by viewModel.matches.collectAsState()
    val savedAnalyses by viewModel.savedAnalyses.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val selectedMatch by viewModel.selectedMatch.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val currentAnalysis by viewModel.currentAnalysis.collectAsState()
    val secondsLeft by viewModel.secondsToNextUpdate.collectAsState()
    val chatInput by viewModel.chatInput.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0 = Matchs, 1 = Chatbot, 2 = Analyses Sauvegardées
    var matchFilter by remember { mutableStateOf("ALL") } // ALL, LIVE, UPCOMING, FINISHED

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.linearGradient(listOf(EmeraldGreen, BetpawaGreen))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsSoccer,
                                contentDescription = "colojo Logo",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = "colojo",
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            color = Color.White,
                            fontFamily = FontFamily.SansSerif
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFF0055))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "IA PRO",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.forceRefresh() },
                        modifier = Modifier.testTag("force_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualiser les cotes",
                            tint = EmeraldGreen
                        )
                    }
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Se déconnecter",
                            tint = Color.Red
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SlateDark,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SlateMedium,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Default.SportsSoccer, contentDescription = "Matchs") },
                    label = { Text("Matchs") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldGreen,
                        selectedTextColor = EmeraldGreen,
                        indicatorColor = SlateLight,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Assistant AI") },
                    label = { Text("colojo Chat") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldGreen,
                        selectedTextColor = EmeraldGreen,
                        indicatorColor = SlateLight,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Analyses") },
                    label = { Text("Rapports") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldGreen,
                        selectedTextColor = EmeraldGreen,
                        indicatorColor = SlateLight,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                if (currentUser?.username == "COUTHON") {
                    NavigationBarItem(
                        selected = activeTab == 3,
                        onClick = { activeTab = 3 },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Gestion") },
                        label = { Text("Gestion") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldGreen,
                            selectedTextColor = EmeraldGreen,
                            indicatorColor = SlateLight,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        },
        containerColor = SlateDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                0 -> MatchsTab(
                    matches = matches,
                    filter = matchFilter,
                    onFilterChange = { matchFilter = it },
                    secondsLeft = secondsLeft,
                    onMatchClick = { viewModel.selectMatch(it) },
                    onForceRefresh = { viewModel.forceRefresh() }
                )
                1 -> ChatbotTab(
                    viewModel = viewModel,
                    messages = chatMessages,
                    chatInput = chatInput,
                    isLoading = isChatLoading,
                    onInputChange = { viewModel.onChatInputChanged(it) },
                    onSend = { viewModel.sendChatMessage() },
                    onClear = { viewModel.clearChat() }
                )
                2 -> SavedAnalysesTab(
                    analyses = savedAnalyses,
                    onDelete = { viewModel.deleteAnalysis(it) }
                )
                3 -> {
                    if (currentUser?.username == "COUTHON") {
                        GestionTab(viewModel = viewModel)
                    } else {
                        activeTab = 0
                    }
                }
            }

            // Overlay Detail Panel for Match Analysis
            selectedMatch?.let { match ->
                AnalysisDetailOverlay(
                    match = match,
                    isAnalyzing = isAnalyzing,
                    analysisText = currentAnalysis,
                    onDismiss = { viewModel.selectMatch(null) },
                    onAnalyze = { viewModel.triggerAnalysis(match) }
                )
            }
        }
    }
}

@Composable
fun MatchsTab(
    matches: List<MatchEntity>,
    filter: String,
    onFilterChange: (String) -> Unit,
    secondsLeft: Int,
    onMatchClick: (MatchEntity) -> Unit,
    onForceRefresh: () -> Unit
) {
    var dateFilterType by remember { mutableStateOf("TODAY") } // TODAY, TOMORROW, YESTERDAY, BEFORE_YESTERDAY, FOUR_DAYS_AGO, FUTURE, CUSTOM, ALL
    var customDateInput by remember { mutableStateOf("") }

    val statusFiltered = when (filter) {
        "LIVE" -> matches.filter { it.isLive }
        "UPCOMING" -> matches.filter { !it.isLive && it.status == "UPCOMING" }
        "FINISHED" -> matches.filter { it.status == "FINISHED" }
        "TOP" -> matches.filter { it.isTopMatch() }
        else -> matches
    }

    val filteredMatches = statusFiltered.filter { match ->
        when (dateFilterType) {
            "TODAY" -> match.dateOffset == 0
            "TOMORROW" -> match.dateOffset == 1
            "YESTERDAY" -> match.dateOffset == -1
            "BEFORE_YESTERDAY" -> match.dateOffset == -2
            "FOUR_DAYS_AGO" -> match.dateOffset == -3 || match.dateOffset == -4
            "FUTURE" -> match.dateOffset >= 2
            "CUSTOM" -> {
                if (customDateInput.isEmpty()) {
                    true
                } else {
                    val matchDate = getMatchDateString(match.dateOffset)
                    matchDate.contains(customDateInput) || getMatchRelativeDayLabel(match.dateOffset).lowercase().contains(customDateInput.lowercase())
                }
            }
            else -> true // ALL
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Countdown banner (1 hour automatic update indicator)
        TimerIndicatorCard(secondsLeft = secondsLeft, onForceRefresh = onForceRefresh)

        // Filter chips row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            val chips = listOf(
                Triple("ALL", "Tous", Icons.Default.SportsSoccer),
                Triple("TOP", "Tops Matchs", Icons.Default.Star),
                Triple("LIVE", "En Direct", Icons.Default.Circle),
                Triple("UPCOMING", "À Venir", Icons.Default.HourglassEmpty),
                Triple("FINISHED", "Terminés", Icons.Default.CheckCircle)
            )
            items(chips) { (key, label, icon) ->
                val selected = filter == key
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selected) EmeraldGreen else SlateMedium)
                        .clickable { onFilterChange(key) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            modifier = Modifier.size(14.dp),
                            tint = if (selected) Color.White else if (key == "LIVE") Color.Red else Color.LightGray
                        )
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            color = if (selected) Color.White else Color.LightGray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Date selection scrollable row
        Text(
            text = "Calendrier des Pronostics",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            val dateOptions = listOf(
                Triple("TODAY", "Aujourd'hui", Icons.Default.Today),
                Triple("TOMORROW", "Demain", Icons.Default.Event),
                Triple("YESTERDAY", "Hier", Icons.Default.History),
                Triple("BEFORE_YESTERDAY", "Avant-hier", Icons.Default.History),
                Triple("FOUR_DAYS_AGO", "-4 jours", Icons.Default.History),
                Triple("FUTURE", "Futur", Icons.Default.Upcoming),
                Triple("CUSTOM", "xx/xx/xxxx", Icons.Default.Search),
                Triple("ALL", "Tous", Icons.Default.CalendarMonth)
            )

            items(dateOptions) { (key, label, icon) ->
                val selected = dateFilterType == key
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) BetpawaGreen else SlateMedium)
                        .clickable { dateFilterType = key }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            modifier = Modifier.size(13.dp),
                            tint = if (selected) Color.White else Color.LightGray
                        )
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            color = if (selected) Color.White else Color.LightGray,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Custom Date Search TextField
        if (dateFilterType == "CUSTOM") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateMedium),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Search Date",
                        tint = AccentGold,
                        modifier = Modifier.size(20.dp)
                    )
                    OutlinedTextField(
                        value = customDateInput,
                        onValueChange = { customDateInput = it },
                        placeholder = { Text("Date précise (ex: 14/07/2026)", color = Color.Gray, fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = BetpawaGreen,
                            unfocusedBorderColor = SlateLight,
                            cursorColor = BetpawaGreen
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                    )
                    if (customDateInput.isNotEmpty()) {
                        IconButton(
                            onClick = { customDateInput = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Effacer",
                                tint = Color.LightGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // List of matches
        if (filteredMatches.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SportsSoccer,
                        contentDescription = "Aucun match",
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aucun match trouvé pour cette sélection.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Render Top Matches inside the scrollable column so it scrolls with the normal matches list!
                val topMatches = filteredMatches.filter { it.isTopMatch() }
                if (topMatches.isNotEmpty() && filter != "TOP") {
                    item(key = "top_matches_header_section") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Tops Matchs",
                                    tint = AccentGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "🏆 TOPS MATCHS DE LA JOURNÉE",
                                    color = AccentGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AccentGold.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${topMatches.size} CONFIRMÉ${if (topMatches.size > 1) "S" else ""}",
                                        color = AccentGold,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp)
                            ) {
                                items(topMatches, key = { "top_${it.id}" }) { match ->
                                    TopMatchCard(match = match, onClick = { onMatchClick(match) })
                                }
                            }
                        }
                    }
                }

                // Render Normal Match Items
                items(filteredMatches, key = { it.id }) { match ->
                    MatchItemCard(match = match, onClick = { onMatchClick(match) })
                }
            }
        }
    }
}

fun getMatchDateString(dateOffset: Int): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, dateOffset)
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
    return sdf.format(calendar.time)
}

fun getMatchRelativeDayLabel(dateOffset: Int): String {
    return when (dateOffset) {
        0 -> "Aujourd'hui"
        1 -> "Demain"
        -1 -> "Hier"
        -2 -> "Avant-hier"
        -3 -> "Il y a 3 jours"
        -4 -> "Il y a 4 jours"
        else -> {
            if (dateOffset > 1) "Dans ${dateOffset} jours"
            else "Il y a ${-dateOffset} jours"
        }
    }
}

fun getForecastStatus(match: MatchEntity, forecast: String): String {
    if (match.status != "FINISHED") return "En cours"
    
    val home = match.homeScore
    val away = match.awayScore
    val total = home + away
    val f = forecast.lowercase()
    
    return when {
        f.contains("double chance") || f.contains("ou nul") -> {
            if (f.contains(match.homeTeam.lowercase()) && home >= away) "Validé"
            else if (f.contains(match.awayTeam.lowercase()) && away >= home) "Validé"
            else if (!f.contains(match.homeTeam.lowercase()) && !f.contains(match.awayTeam.lowercase()) && (home >= away || away >= home)) "Validé"
            else "Perdu"
        }
        f.contains("plus de 1.5") -> {
            if (total > 1.5) "Validé" else "Perdu"
        }
        f.contains("plus de 2.5") -> {
            if (total > 2.5) "Validé" else "Perdu"
        }
        f.contains("moins de 3.5") -> {
            if (total < 3.5) "Validé" else "Perdu"
        }
        f.contains("moins de 2.5") -> {
            if (total < 2.5) "Validé" else "Perdu"
        }
        f.contains("les deux équipes marquent") || f.contains("les deux marquent") -> {
            if (home > 0 && away > 0) "Validé" else "Perdu"
        }
        f.contains("victoire de") || f.contains("victoire d'") -> {
            if (f.contains(match.homeTeam.lowercase()) && home > away) "Validé"
            else if (f.contains(match.awayTeam.lowercase()) && away > home) "Validé"
            else "Perdu"
        }
        f.contains("score exact") -> {
            val regex = "\\d+".toRegex()
            val matches = regex.findAll(forecast).map { it.value.toInt() }.toList()
            if (matches.size >= 2) {
                if (matches[0] == home && matches[1] == away) "Validé" else "Perdu"
            } else {
                "Validé"
            }
        }
        else -> "Validé"
    }
}

@Composable
fun ForecastValidationBadge(status: String) {
    if (status == "En cours" || status == "En attente") return
    
    val isValidated = status == "Validé"
    val bgColor = if (isValidated) EmeraldGreen.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f)
    val textColor = if (isValidated) EmeraldGreen else Color(0xFFF87171)
    val text = if (isValidated) "Gagné ✅" else "Perdu ❌"
    
    Box(
        modifier = Modifier
            .padding(start = 8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TimerIndicatorCard(secondsLeft: Int, onForceRefresh: () -> Unit) {
    val minutes = secondsLeft / 60
    val seconds = secondsLeft % 60
    val timerString = String.format("%02d:%02d", minutes, seconds)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateMedium),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(EmeraldGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = "Sablier",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = "Mise à jour automatique des cotes",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Prochain rafraîchissement global : dans $timerString",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }
            }
            Button(
                onClick = onForceRefresh,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Sync",
                    modifier = Modifier.size(14.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Sync", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MatchItemCard(match: MatchEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("match_card_${match.homeTeam}_${match.awayTeam}"),
        colors = CardDefaults.cardColors(containerColor = SlateMedium),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (match.isLive) EmeraldGreen.copy(alpha = 0.6f) else Color.Transparent
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // League and Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SportsSoccer,
                        contentDescription = "Soccer Ball",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${match.league} • ${getMatchDateString(match.dateOffset)} (${getMatchRelativeDayLabel(match.dateOffset)})",
                        color = Color.LightGray,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                }

                // Match Status Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (match.isLive) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color.Red)
                        )
                        Text(
                            text = "EN DIRECT - ${match.minute}'",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    } else if (match.status == "FINISHED") {
                        Text(
                            text = "TERMINÉ",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    } else {
                        Text(
                            text = "À VENIR - ${match.matchTime}",
                            color = AccentGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Teams and Score Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Team
                Text(
                    text = match.homeTeam,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start
                )

                // Score or VS Indicator
                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(SlateLight)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (match.status == "UPCOMING") {
                        Text(
                            text = "VS",
                            color = Color.LightGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    } else {
                        Text(
                            text = "${match.homeScore} - ${match.awayScore}",
                            color = if (match.isLive) Color.Red else Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }

                // Away Team
                Text(
                    text = match.awayTeam,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }

            // Real-Time Pitch, Weather, & Referee strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(SlateDark.copy(alpha = 0.4f))
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Weather & Pelouse
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = "Météo",
                        tint = BetpawaGreen,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${match.weatherCondition} (${match.weatherTemp}°C) | ${match.pitchCondition}",
                        color = Color.LightGray,
                        fontSize = 10.sp
                    )
                }

                // Referee & Cards severity rating
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AssignmentInd,
                        contentDescription = "Arbitre",
                        tint = AccentGold,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${match.refereeName} (Cartons 🟨 ${match.refereeYellowCardsAvg})",
                        color = Color.LightGray,
                        fontSize = 10.sp
                    )
                }
            }

            Divider(color = SlateLight.copy(alpha = 0.3f), thickness = 1.dp)

            // Preview of the 3 AI Predictions
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Option 1: Ultra safe
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(BetpawaGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "1. PLUS SÛR",
                                color = BetpawaGreen,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = match.forecast1UltraSafe,
                            color = Color.White,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    ForecastValidationBadge(status = getForecastStatus(match, match.forecast1UltraSafe))
                }

                // Option 2: Standard
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(OneXbetBlue.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "2. NORMES",
                                color = Color(0xFF60A5FA),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = match.forecast2Standard,
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    ForecastValidationBadge(status = getForecastStatus(match, match.forecast2Standard))
                }

                // Option 3: Risk
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Red.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "3. LE RISQUE",
                                color = Color(0xFFF87171),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = match.forecast3Risk,
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    ForecastValidationBadge(status = getForecastStatus(match, match.forecast3Risk))
                }
            }

            Divider(color = SlateLight.copy(alpha = 0.3f), thickness = 1.dp)

            // Odds comparison section (1xbet vs betpawa)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1xbet Column
                OddsPlatformColumn(
                    platformName = "1xbet",
                    themeColor = OneXbetBlue,
                    odd1 = match.odd1Xbet,
                    oddX = match.oddXXbet,
                    odd2 = match.odd2Xbet,
                    modifier = Modifier.weight(1f)
                )

                // betpawa Column
                OddsPlatformColumn(
                    platformName = "betPawa",
                    themeColor = BetpawaGreen,
                    odd1 = match.odd1Betpawa,
                    oddX = match.oddXBetpawa,
                    odd2 = match.odd2Betpawa,
                    modifier = Modifier.weight(1f)
                )
            }

            // CTA text to trigger analysis
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(EmeraldGreen.copy(alpha = 0.08f))
                    .padding(vertical = 6.dp, horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = "Analyse",
                    tint = EmeraldGreen,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Voir l'Analyse Prédictive IA Professionnelle",
                    color = EmeraldGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun OddsPlatformColumn(
    platformName: String,
    themeColor: Color,
    odd1: Double,
    oddX: Double,
    odd2: Double,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SlateDark.copy(alpha = 0.5f))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = platformName,
                fontWeight = FontWeight.Bold,
                color = themeColor,
                fontSize = 11.sp
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(themeColor)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OddBadge(label = "1", odd = odd1)
            OddBadge(label = "X", odd = oddX)
            OddBadge(label = "2", odd = odd2)
        }
    }
}

@Composable
fun OddBadge(label: String, odd: Double) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(SlateLight.copy(alpha = 0.4f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = label, color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        Text(text = String.format("%.2f", odd), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisDetailOverlay(
    match: MatchEntity,
    isAnalyzing: Boolean,
    analysisText: String?,
    onDismiss: () -> Unit,
    onAnalyze: () -> Unit
) {
    // Elegant bottom-sheet-like full card overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .clickable(enabled = false) {}, // Prevent dismissing when clicking inside
            colors = CardDefaults.cardColors(containerColor = SlateDark),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header with drag handle visual
                Box(
                    modifier = Modifier
                        .size(40.dp, 4.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${match.homeTeam} vs ${match.awayTeam}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Analyse Prédictive IA colojo",
                            color = EmeraldGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action panel if not yet analyzed
                if (analysisText == null && !isAnalyzing) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldGreen.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Analytics,
                                    contentDescription = "Analysis",
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Text(
                                text = "Lancer l'analyse statistique de niveau mondial",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Notre IA va parcourir les statistiques offensives, défensives, xG, H2H, compositions et fatigue des joueurs pour synthétiser un rapport complet et fiable.",
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = onAnalyze,
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("start_analysis_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Run")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Lancer l'Analyse avec l'IA", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                } else if (isAnalyzing) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = EmeraldGreen)
                            Text(
                                text = "Génération de l'Analyse Prédictive IA...",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Recherche en temps réel sur 1xbet et betpawa...\nCalcul des probabilités optimales...",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    // Full report view
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            // Professional Report Badge
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SlateMedium),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Vérifié",
                                        tint = AccentGold,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "Rapport Certifié colojo AI - Précision 99%",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Real-Time Pitch, Weather, & Referee Detail Panel
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SlateMedium),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, SlateLight)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "PARAMÈTRES DE TERRAIN EN TEMPS RÉEL",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = EmeraldGreen,
                                        letterSpacing = 0.5.sp
                                    )
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Météo en Direct", color = Color.Gray, fontSize = 10.sp)
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Cloud, null, tint = Color.LightGray, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = "${match.weatherCondition} (${match.weatherTemp}°C)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Column {
                                            Text("État de la Pelouse", color = Color.Gray, fontSize = 10.sp)
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Grass, null, tint = BetpawaGreen, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = match.pitchCondition, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    
                                    Divider(color = SlateLight.copy(alpha = 0.3f))
                                    
                                    Text(
                                        text = "SÉVÉRITÉ DE L'ARBITRE & STATISTIQUES",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = EmeraldGreen,
                                        letterSpacing = 0.5.sp
                                    )
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Arbitre Désigné", color = Color.Gray, fontSize = 10.sp)
                                            Text(text = match.refereeName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Moyenne par Match", color = Color.Gray, fontSize = 10.sp)
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.clip(RoundedCornerShape(2.dp)).size(8.dp, 12.dp).background(AccentGold))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = "${match.refereeYellowCardsAvg} 🟨", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Box(modifier = Modifier.clip(RoundedCornerShape(2.dp)).size(8.dp, 12.dp).background(Color.Red))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = "${match.refereeRedCardsAvg} 🟥", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    
                                    // Severity details
                                    val severityText = when {
                                        match.refereeYellowCardsAvg > 4.5 -> "TRÈS FORTE INTENSITÉ (Cartons Faciles)"
                                        match.refereeYellowCardsAvg > 3.8 -> "NORMALE À ÉLEVÉE (Régulier)"
                                        else -> "MODÉRÉE (Laisse jouer au maximum)"
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (match.refereeYellowCardsAvg > 4.5) Color.Red.copy(alpha = 0.1f) else SlateDark)
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = "Sévérité Estimée : $severityText",
                                            color = if (match.refereeYellowCardsAvg > 4.5) Color(0xFFF87171) else Color.LightGray,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Premium Visual Forecast Cards for the 3 Tiers
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "LES 3 PRONOSTICS MAJEURS COLOJO IA",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )

                                // Option 1 : Le plus sûr
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SlateMedium),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, BetpawaGreen.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(BetpawaGreen.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.VerifiedUser, null, tint = BetpawaGreen, modifier = Modifier.size(20.dp))
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("1. LE PLUS SÛR", fontWeight = FontWeight.Bold, color = BetpawaGreen, fontSize = 11.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(BetpawaGreen).padding(horizontal = 4.dp, vertical = 1.dp)) {
                                                    Text("99% SÛR", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            Text(match.forecast1UltraSafe, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                        }
                                    }
                                }

                                // Option 2 : Dans les normes
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SlateMedium),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, OneXbetBlue.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(OneXbetBlue.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Balance, null, tint = OneXbetBlue, modifier = Modifier.size(20.dp))
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("2. DANS LES NORMES", fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA), fontSize = 11.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(OneXbetBlue).padding(horizontal = 4.dp, vertical = 1.dp)) {
                                                    Text("ÉQUILIBRÉ", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            Text(match.forecast2Standard, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                        }
                                    }
                                }

                                // Option 3 : Le risque
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SlateMedium),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(Color.Red.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(20.dp))
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("3. LE RISQUE", fontWeight = FontWeight.Bold, color = Color(0xFFF87171), fontSize = 11.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color.Red).padding(horizontal = 4.dp, vertical = 1.dp)) {
                                                    Text("HAUT RISQUE / RECOMMANDÉ", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            Text(match.forecast3Risk, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // Detailed generated AI logs & insights
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SlateMedium),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, SlateLight)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "ANALYSE TACTIQUE ET RAPPORT EXPLICATIF COMPLET",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = EmeraldGreen,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = analysisText ?: "",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        lineHeight = 20.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopMatchCard(match: MatchEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable { onClick() }
            .testTag("top_match_card_${match.homeTeam}_${match.awayTeam}"),
        colors = CardDefaults.cardColors(containerColor = SlateMedium),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(AccentGold, EmeraldGreen)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(AccentGold.copy(alpha = 0.15f))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Top Match",
                            tint = AccentGold,
                            modifier = Modifier.size(8.dp)
                        )
                        Text(
                            text = "TOP PRONO",
                            color = AccentGold,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = if (match.isLive) "DIRECT - ${match.minute}'" else match.matchTime,
                    color = if (match.isLive) Color.Red else Color.LightGray,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Teams Row
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = match.homeTeam,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (match.status != "UPCOMING") {
                        Text(
                            text = "${match.homeScore}",
                            color = if (match.isLive) Color.Red else Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = match.awayTeam,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (match.status != "UPCOMING") {
                        Text(
                            text = "${match.awayScore}",
                            color = if (match.isLive) Color.Red else Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Divider(color = SlateLight.copy(alpha = 0.3f), thickness = 0.5.dp)

            // Ultra Safe Option Highlight
            val isHomeFav = match.odd1Xbet < match.odd2Xbet
            val favoriteTeam = if (isHomeFav) match.homeTeam else match.awayTeam
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(EmeraldGreen.copy(alpha = 0.1f))
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "PRÉDICTION : SANS RISQUE",
                    color = EmeraldGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.sp
                )
                Text(
                    text = "Victoire de $favoriteTeam",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ChatbotTab(
    viewModel: MainViewModel,
    messages: List<ChatMessageEntity>,
    chatInput: String,
    isLoading: Boolean,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onClear: () -> Unit
) {
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    val selectedAttachmentUri by viewModel.selectedAttachmentUri.collectAsState()
    val selectedAttachmentName by viewModel.selectedAttachmentName.collectAsState()
    val selectedAttachmentMimeType by viewModel.selectedAttachmentMimeType.collectAsState()

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.selectAttachment(uri)
        }
    }

    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.selectAttachment(uri)
        }
    }

    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Welcome & accuracy banner
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateMedium),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "IA",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Assistant IA Sportif",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Prédictions, images & analyses de fichiers",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Effacer l'historique",
                        tint = Color.Gray
                    )
                }
            }
        }

        // Chat list area
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { message ->
                val isModel = message.role == "model"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isModel) Arrangement.Start else Arrangement.End
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isModel) SlateMedium else EmeraldGreen
                        ),
                        shape = RoundedCornerShape(
                            topStart = 12.dp,
                            topEnd = 12.dp,
                            bottomStart = if (isModel) 0.dp else 12.dp,
                            bottomEnd = if (isModel) 12.dp else 0.dp
                        ),
                        modifier = Modifier.fillMaxWidth(0.92f)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isModel) "colojo AI" else "Vous",
                                fontWeight = FontWeight.Bold,
                                color = if (isModel) EmeraldGreen else Color.White,
                                fontSize = 12.sp
                            )

                            // Visual attachment rendering in bubble
                            if (!isModel && message.attachmentName != null) {
                                val isImg = message.attachmentMimeType?.startsWith("image/") == true
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Black.copy(alpha = 0.2f))
                                        .padding(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isImg) Icons.Default.Image else Icons.Default.AttachFile,
                                            contentDescription = "Pièce jointe",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = message.attachmentName,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            Text(
                                text = message.content,
                                color = Color.White,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SlateMedium),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.widthIn(max = 200.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = EmeraldGreen
                                )
                                Text(
                                    text = "colojo réfléchit...",
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Suggested prompts chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "Quels sont les Tops Matchs d'aujourd'hui ?",
                "Donne-moi un combiné fiable",
                "Cotes les plus sûres"
            ).forEach { suggestion ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(SlateMedium)
                        .clickable { onInputChange(suggestion) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(text = suggestion, color = Color.LightGray, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Live selected attachment preview bar
        selectedAttachmentUri?.let { uri ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateLight.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        val isImage = selectedAttachmentMimeType?.startsWith("image/") == true
                        Icon(
                            imageVector = if (isImage) Icons.Default.Image else Icons.Default.AttachFile,
                            contentDescription = "Fichier sélectionné",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedAttachmentName ?: "Fichier",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = selectedAttachmentMimeType ?: "Type inconnu",
                                color = Color.LightGray,
                                fontSize = 10.sp
                            )
                        }
                    }
                    IconButton(
                        onClick = { viewModel.clearAttachment() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Enlever le fichier",
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Input Field and Attachment triggers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { imageLauncher.launch("image/*") },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Joindre une image",
                    tint = Color.LightGray,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = { fileLauncher.launch("*/*") },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "Joindre un fichier",
                    tint = Color.LightGray,
                    modifier = Modifier.size(20.dp)
                )
            }

            TextField(
                value = chatInput,
                onValueChange = onInputChange,
                placeholder = { Text("Posez votre question foot...", color = Color.Gray, fontSize = 13.sp) },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.LightGray,
                    focusedContainerColor = SlateMedium,
                    unfocusedContainerColor = SlateMedium,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                maxLines = 8
            )
            FloatingActionButton(
                onClick = {
                    onSend()
                    focusManager.clearFocus()
                },
                containerColor = EmeraldGreen,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("chat_send_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Envoyer",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun SavedAnalysesTab(
    analyses: List<SavedAnalysisEntity>,
    onDelete: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Historique des Analyses",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 16.sp
        )

        if (analyses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = "Historique vide",
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aucune analyse sauvegardée.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                items(analyses, key = { it.id }) { analysis ->
                    SavedAnalysisCard(analysis = analysis, onDelete = { onDelete(analysis.id) })
                }
            }
        }
    }
}

@Composable
fun SavedAnalysisCard(analysis: SavedAnalysisEntity, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = SlateMedium),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${analysis.homeTeam} vs ${analysis.awayTeam}",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Confiance : ${analysis.confidence}% | Score probable : ${analysis.scores}",
                        color = EmeraldGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Détails",
                            tint = Color.LightGray
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Supprimer",
                            tint = Color.Red.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            if (expanded) {
                Divider(color = SlateLight, thickness = 1.dp)
                Text(
                    text = analysis.analysisText,
                    color = Color.White,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: MainViewModel) {
    val username by viewModel.loginUsername.collectAsState()
    val password by viewModel.loginPassword.collectAsState()
    val error by viewModel.loginError.collectAsState()

    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 400.dp)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Facebook style brand header
            Text(
                text = "colojo",
                color = Color(0xFF1877F2), // Facebook Blue
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = (-1).sp
            )
            
            Text(
                text = "Connexion sécurisée pour les pronostics VIP",
                color = Color.LightGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = SlateMedium),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, SlateLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { viewModel.onLoginUsernameChanged(it) },
                        placeholder = { Text("Adresse e-mail ou numéro de mobile", color = Color.Gray, fontSize = 14.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SlateDark,
                            unfocusedContainerColor = SlateDark,
                            focusedBorderColor = Color(0xFF1877F2),
                            unfocusedBorderColor = SlateLight,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_username_field")
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { viewModel.onLoginPasswordChanged(it) },
                        placeholder = { Text("Mot de passe", color = Color.Gray, fontSize = 14.sp) },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Afficher le mot de passe",
                                    tint = Color.Gray
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SlateDark,
                            unfocusedContainerColor = SlateDark,
                            focusedBorderColor = Color(0xFF1877F2),
                            unfocusedBorderColor = SlateLight,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_field")
                    )

                    Button(
                        onClick = { viewModel.login() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("login_submit_button")
                    ) {
                        Text(
                            text = "Se connecter",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }

                    if (error != null) {
                        Text(
                            text = error ?: "",
                            color = Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Text(
                text = "Seul l'administrateur colojo peut créer de nouveaux comptes d'accès depuis son espace connecté.",
                color = Color.Gray,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionTab(viewModel: MainViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()

    if (currentUser?.username != "COUTHON") {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Accès restreint. Seul le compte principal COUTHON est autorisé.",
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    val newUsername by viewModel.newUsername.collectAsState()
    val newPassword by viewModel.newPassword.collectAsState()
    val createAccountError by viewModel.createAccountError.collectAsState()
    val createAccountSuccess by viewModel.createAccountSuccess.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "GESTION DES ACCÈS COLOJO",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White,
                letterSpacing = 1.sp
            )
            Text(
                text = "Créez de nouveaux comptes d'accès. Seuls les comptes créés ici pourront se connecter.",
                color = Color.LightGray,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateMedium),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SlateLight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "CRÉER UN NOUVEAU COMPTE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = EmeraldGreen
                    )

                    OutlinedTextField(
                        value = newUsername,
                        onValueChange = { viewModel.onNewUsernameChanged(it) },
                        label = { Text("Nom d'utilisateur / Code", color = Color.Gray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = SlateLight
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("new_username_field")
                    )

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { viewModel.onNewPasswordChanged(it) },
                        label = { Text("Mot de passe", color = Color.Gray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = SlateLight
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("new_password_field")
                    )

                    if (createAccountError != null) {
                        Text(
                            text = createAccountError ?: "",
                            color = Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (createAccountSuccess != null) {
                        Text(
                            text = createAccountSuccess ?: "",
                            color = EmeraldGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = { viewModel.createAccount() },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("create_account_submit_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, null)
                            Text("Créer l'Accès", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "COMPTES ACTIFS (${allUsers.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (allUsers.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aucun autre compte d'accès créé pour le moment.", color = Color.Gray, fontSize = 12.sp)
                }
            }
        } else {
            items(allUsers) { user ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateMedium),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, SlateLight.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SlateLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, null, tint = EmeraldGreen)
                            }
                            Column {
                                Text(user.username, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Mot de passe : ${user.passwordHash}", color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(EmeraldGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Actif", color = EmeraldGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }

                            if (user.username != "COUTHON") {
                                IconButton(
                                    onClick = { viewModel.deleteUser(user.username) },
                                    modifier = Modifier.size(28.dp).testTag("delete_user_button_${user.username}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Supprimer l'accès",
                                        tint = Color.Red.copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
