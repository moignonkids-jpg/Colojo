package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlin.random.Random

class AppRepository(private val appDao: AppDao) {

    val allMatches: Flow<List<MatchEntity>> = appDao.getAllMatches()
    val allSavedAnalyses: Flow<List<SavedAnalysisEntity>> = appDao.getAllSavedAnalyses()
    val allChatMessages: Flow<List<ChatMessageEntity>> = appDao.getAllChatMessages()
    val allUsers: Flow<List<UserEntity>> = appDao.getAllUsers()

    suspend fun insertSavedAnalysis(analysis: SavedAnalysisEntity) {
        appDao.insertSavedAnalysis(analysis)
    }

    suspend fun deleteSavedAnalysis(id: Int) {
        appDao.deleteSavedAnalysisById(id)
    }

    suspend fun insertChatMessage(message: ChatMessageEntity) {
        appDao.insertChatMessage(message)
    }

    suspend fun clearChatHistory() {
        appDao.clearChatHistory()
    }

    suspend fun insertUser(user: UserEntity) {
        appDao.insertUser(user)
    }

    suspend fun getUserByUsername(username: String): UserEntity? {
        return appDao.getUserByUsername(username)
    }

    suspend fun deleteUserByUsername(username: String) {
        appDao.deleteUserByUsername(username)
    }

    suspend fun refreshOrPrepopulateMatches() {
        // Prepopulate default admin user if none exists
        val existingUsers = allUsers.firstOrNull() ?: emptyList()
        if (existingUsers.isEmpty()) {
            appDao.insertUser(UserEntity(username = "COUTHON", passwordHash = "242526"))
        } else {
            if (appDao.getUserByUsername("COUTHON") == null) {
                appDao.insertUser(UserEntity(username = "COUTHON", passwordHash = "242526"))
            }
        }

        val existing = allMatches.firstOrNull() ?: emptyList()
        if (existing.isEmpty()) {
            val prepopulated = listOf(
                MatchEntity(
                    homeTeam = "Real Madrid",
                    awayTeam = "Manchester City",
                    league = "Champions League",
                    matchTime = "20:00",
                    status = "LIVE",
                    homeScore = 1,
                    awayScore = 1,
                    minute = 62,
                    odd1Xbet = 2.45,
                    oddXXbet = 3.40,
                    odd2Xbet = 2.80,
                    odd1Betpawa = 2.40,
                    oddXBetpawa = 3.35,
                    odd2Betpawa = 2.75,
                    isLive = true,
                    weatherTemp = 18,
                    weatherCondition = "Pluie légère",
                    pitchCondition = "Glissante",
                    refereeName = "Szymon Marciniak",
                    refereeYellowCardsAvg = 4.3,
                    refereeRedCardsAvg = 0.28,
                    refereePenaltiesAvg = 0.38,
                    forecast1UltraSafe = "Double Chance : Real Madrid ou Nul (Sécurité 95%)",
                    forecast2Standard = "Les deux équipes marquent (Équilibré 82%)",
                    forecast3Risk = "Score Exact : 2 - 2 (Audacieux 20%)",
                    dateOffset = 0
                ),
                MatchEntity(
                    homeTeam = "Paris Saint-Germain",
                    awayTeam = "Bayern Munich",
                    league = "Champions League",
                    matchTime = "20:00",
                    status = "LIVE",
                    homeScore = 0,
                    awayScore = 1,
                    minute = 35,
                    odd1Xbet = 3.10,
                    oddXXbet = 3.65,
                    odd2Xbet = 2.15,
                    odd1Betpawa = 3.00,
                    oddXBetpawa = 3.60,
                    odd2Betpawa = 2.10,
                    isLive = true,
                    weatherTemp = 14,
                    weatherCondition = "Nuageux",
                    pitchCondition = "Excellente",
                    refereeName = "Clément Turpin",
                    refereeYellowCardsAvg = 3.6,
                    refereeRedCardsAvg = 0.18,
                    refereePenaltiesAvg = 0.24,
                    forecast1UltraSafe = "Plus de 1.5 buts (Sécurité 91%)",
                    forecast2Standard = "Victoire de Bayern Munich (Équilibré 68%)",
                    forecast3Risk = "Score Exact : 1 - 2 (Audacieux 35%)",
                    dateOffset = 0
                ),
                MatchEntity(
                    homeTeam = "Arsenal",
                    awayTeam = "Chelsea",
                    league = "Premier League",
                    matchTime = "16:30",
                    status = "FINISHED",
                    homeScore = 2,
                    awayScore = 0,
                    minute = 90,
                    odd1Xbet = 1.72,
                    oddXXbet = 3.80,
                    odd2Xbet = 4.75,
                    odd1Betpawa = 1.70,
                    oddXBetpawa = 3.75,
                    odd2Betpawa = 4.60,
                    isLive = false,
                    weatherTemp = 16,
                    weatherCondition = "Ensoleillé",
                    pitchCondition = "Excellente (Sèche)",
                    refereeName = "Michael Oliver",
                    refereeYellowCardsAvg = 3.9,
                    refereeRedCardsAvg = 0.15,
                    refereePenaltiesAvg = 0.33,
                    forecast1UltraSafe = "Victoire d'Arsenal ou Nul (Sécurité 97%)",
                    forecast2Standard = "Moins de 3.5 buts (Équilibré 78%)",
                    forecast3Risk = "Score Exact : 2 - 0 (Audacieux 25%)",
                    dateOffset = 0
                ),
                MatchEntity(
                    homeTeam = "Liverpool",
                    awayTeam = "Manchester United",
                    league = "Premier League",
                    matchTime = "18:00",
                    status = "UPCOMING",
                    homeScore = 0,
                    awayScore = 0,
                    minute = 0,
                    odd1Xbet = 1.55,
                    oddXXbet = 4.50,
                    odd2Xbet = 5.25,
                    odd1Betpawa = 1.52,
                    oddXBetpawa = 4.40,
                    odd2Betpawa = 5.10,
                    isLive = false,
                    weatherTemp = 12,
                    weatherCondition = "Pluie Forte & Vent",
                    pitchCondition = "Détrempée",
                    refereeName = "Anthony Taylor",
                    refereeYellowCardsAvg = 4.6,
                    refereeRedCardsAvg = 0.32,
                    refereePenaltiesAvg = 0.42,
                    forecast1UltraSafe = "Plus de 1.5 buts (Sécurité 94%)",
                    forecast2Standard = "Victoire de Liverpool (Équilibré 75%)",
                    forecast3Risk = "Score Exact : 2 - 0 (Audacieux 40%)",
                    dateOffset = 0
                ),
                MatchEntity(
                    homeTeam = "Barcelona",
                    awayTeam = "Atletico Madrid",
                    league = "La Liga",
                    matchTime = "21:00",
                    status = "UPCOMING",
                    homeScore = 0,
                    awayScore = 0,
                    minute = 0,
                    odd1Xbet = 1.95,
                    oddXXbet = 3.45,
                    odd2Xbet = 3.80,
                    odd1Betpawa = 1.90,
                    oddXBetpawa = 3.40,
                    odd2Betpawa = 3.70,
                    isLive = false,
                    weatherTemp = 24,
                    weatherCondition = "Dégagé",
                    pitchCondition = "Parfaite",
                    refereeName = "Jesús Gil Manzano",
                    refereeYellowCardsAvg = 5.2,
                    refereeRedCardsAvg = 0.45,
                    refereePenaltiesAvg = 0.30,
                    forecast1UltraSafe = "Moins de 3.5 buts (Sécurité 92%)",
                    forecast2Standard = "Double Chance : Barcelone ou Nul (Équilibré 79%)",
                    forecast3Risk = "Score Exact : 2 - 1 (Audacieux 28%)",
                    dateOffset = 0
                ),
                MatchEntity(
                    homeTeam = "Marseille",
                    awayTeam = "Lyon",
                    league = "Ligue 1",
                    matchTime = "20:45",
                    status = "UPCOMING",
                    homeScore = 0,
                    awayScore = 0,
                    minute = 0,
                    odd1Xbet = 2.10,
                    oddXXbet = 3.30,
                    odd2Xbet = 3.50,
                    odd1Betpawa = 2.05,
                    oddXBetpawa = 3.25,
                    odd2Betpawa = 3.45,
                    isLive = false,
                    weatherTemp = 20,
                    weatherCondition = "Humide",
                    pitchCondition = "Excellente",
                    refereeName = "Benoît Bastien",
                    refereeYellowCardsAvg = 4.1,
                    refereeRedCardsAvg = 0.25,
                    refereePenaltiesAvg = 0.28,
                    forecast1UltraSafe = "Plus de 1.5 buts (Sécurité 90%)",
                    forecast2Standard = "Les deux équipes marquent (Équilibré 72%)",
                    forecast3Risk = "Score Exact : 2 - 1 (Audacieux 42%)",
                    dateOffset = 0
                ),
                MatchEntity(
                    homeTeam = "Inter Milan",
                    awayTeam = "AC Milan",
                    league = "Serie A",
                    matchTime = "18:00",
                    status = "FINISHED",
                    homeScore = 1,
                    awayScore = 2,
                    minute = 90,
                    odd1Xbet = 1.85,
                    oddXXbet = 3.50,
                    odd2Xbet = 4.20,
                    odd1Betpawa = 1.80,
                    oddXBetpawa = 3.45,
                    odd2Betpawa = 4.10,
                    isLive = false,
                    weatherTemp = 22,
                    weatherCondition = "Clair",
                    pitchCondition = "Excellente",
                    refereeName = "Davide Massa",
                    refereeYellowCardsAvg = 4.8,
                    refereeRedCardsAvg = 0.35,
                    refereePenaltiesAvg = 0.29,
                    forecast1UltraSafe = "Double Chance : Inter Milan ou Nul (Sécurité 89%)",
                    forecast2Standard = "Plus de 2.5 buts (Équilibré 70%)",
                    forecast3Risk = "Score Exact : 1 - 2 (Audacieux 18%)",
                    dateOffset = -1
                ),
                MatchEntity(
                    homeTeam = "Dortmund",
                    awayTeam = "Leipzig",
                    league = "Bundesliga",
                    matchTime = "15:30",
                    status = "FINISHED",
                    homeScore = 3,
                    awayScore = 1,
                    minute = 90,
                    odd1Xbet = 2.25,
                    oddXXbet = 3.60,
                    odd2Xbet = 2.95,
                    odd1Betpawa = 2.20,
                    oddXBetpawa = 3.50,
                    odd2Betpawa = 2.90,
                    isLive = false,
                    weatherTemp = 15,
                    weatherCondition = "Nuageux",
                    pitchCondition = "Excellente",
                    refereeName = "Felix Zwayer",
                    refereeYellowCardsAvg = 4.2,
                    refereeRedCardsAvg = 0.20,
                    refereePenaltiesAvg = 0.35,
                    forecast1UltraSafe = "Plus de 2.5 buts (Sécurité 95%)",
                    forecast2Standard = "Victoire de Dortmund (Équilibré 65%)",
                    forecast3Risk = "Score Exact : 3 - 1 (Audacieux 15%)",
                    dateOffset = -1
                ),
                MatchEntity(
                    homeTeam = "Juventus",
                    awayTeam = "Napoli",
                    league = "Serie A",
                    matchTime = "20:45",
                    status = "FINISHED",
                    homeScore = 1,
                    awayScore = 1,
                    minute = 90,
                    odd1Xbet = 2.05,
                    oddXXbet = 3.10,
                    odd2Xbet = 3.90,
                    odd1Betpawa = 2.00,
                    oddXBetpawa = 3.05,
                    odd2Betpawa = 3.80,
                    isLive = false,
                    weatherTemp = 19,
                    weatherCondition = "Pluie",
                    pitchCondition = "Humide",
                    refereeName = "Marco Guida",
                    refereeYellowCardsAvg = 4.4,
                    refereeRedCardsAvg = 0.18,
                    refereePenaltiesAvg = 0.25,
                    forecast1UltraSafe = "Double Chance : Juventus ou Nul (Sécurité 92%)",
                    forecast2Standard = "Moins de 2.5 buts (Équilibré 75%)",
                    forecast3Risk = "Score Exact : 1 - 1 (Audacieux 15%)",
                    dateOffset = -1
                ),
                MatchEntity(
                    homeTeam = "Tottenham",
                    awayTeam = "Arsenal",
                    league = "Premier League",
                    matchTime = "14:00",
                    status = "FINISHED",
                    homeScore = 1,
                    awayScore = 2,
                    minute = 90,
                    odd1Xbet = 3.40,
                    oddXXbet = 3.60,
                    odd2Xbet = 2.05,
                    odd1Betpawa = 3.35,
                    oddXBetpawa = 3.55,
                    odd2Betpawa = 2.00,
                    isLive = false,
                    weatherTemp = 17,
                    weatherCondition = "Nuageux",
                    pitchCondition = "Excellente",
                    refereeName = "Jarred Gillett",
                    refereeYellowCardsAvg = 4.1,
                    refereeRedCardsAvg = 0.20,
                    refereePenaltiesAvg = 0.30,
                    forecast1UltraSafe = "Plus de 1.5 buts (Sécurité 94%)",
                    forecast2Standard = "Les deux équipes marquent (Équilibré 78%)",
                    forecast3Risk = "Score Exact : 1 - 2 (Audacieux 20%)",
                    dateOffset = -1
                ),
                MatchEntity(
                    homeTeam = "Real Betis",
                    awayTeam = "Real Madrid",
                    league = "La Liga",
                    matchTime = "16:15",
                    status = "FINISHED",
                    homeScore = 1,
                    awayScore = 3,
                    minute = 90,
                    odd1Xbet = 4.50,
                    oddXXbet = 3.90,
                    odd2Xbet = 1.70,
                    odd1Betpawa = 4.40,
                    oddXBetpawa = 3.85,
                    odd2Betpawa = 1.68,
                    isLive = false,
                    weatherTemp = 28,
                    weatherCondition = "Ensoleillé",
                    pitchCondition = "Sèche",
                    refereeName = "Soto Grado",
                    refereeYellowCardsAvg = 5.0,
                    refereeRedCardsAvg = 0.30,
                    refereePenaltiesAvg = 0.25,
                    forecast1UltraSafe = "Victoire de Real Madrid (Sécurité 88%)",
                    forecast2Standard = "Plus de 2.5 buts (Équilibré 74%)",
                    forecast3Risk = "Score Exact : 1 - 3 (Audacieux 18%)",
                    dateOffset = -2
                ),
                MatchEntity(
                    homeTeam = "Milan",
                    awayTeam = "Juventus",
                    league = "Serie A",
                    matchTime = "18:00",
                    status = "FINISHED",
                    homeScore = 0,
                    awayScore = 0,
                    minute = 90,
                    odd1Xbet = 2.40,
                    oddXXbet = 3.00,
                    odd2Xbet = 3.20,
                    odd1Betpawa = 2.35,
                    oddXBetpawa = 2.95,
                    odd2Betpawa = 3.10,
                    isLive = false,
                    weatherTemp = 16,
                    weatherCondition = "Brouillard",
                    pitchCondition = "Excellente",
                    refereeName = "Maurizio Mariani",
                    refereeYellowCardsAvg = 4.6,
                    refereeRedCardsAvg = 0.22,
                    refereePenaltiesAvg = 0.28,
                    forecast1UltraSafe = "Moins de 3.5 buts (Sécurité 96%)",
                    forecast2Standard = "Moins de 2.5 buts (Équilibré 72%)",
                    forecast3Risk = "Score Exact : 0 - 0 (Audacieux 12%)",
                    dateOffset = -2
                ),
                MatchEntity(
                    homeTeam = "Lens",
                    awayTeam = "Lille",
                    league = "Ligue 1",
                    matchTime = "17:00",
                    status = "FINISHED",
                    homeScore = 2,
                    awayScore = 0,
                    minute = 90,
                    odd1Xbet = 2.20,
                    oddXXbet = 3.20,
                    odd2Xbet = 3.40,
                    odd1Betpawa = 2.15,
                    oddXBetpawa = 3.15,
                    odd2Betpawa = 3.30,
                    isLive = false,
                    weatherTemp = 15,
                    weatherCondition = "Nuageux",
                    pitchCondition = "Parfaite",
                    refereeName = "Benoît Millot",
                    refereeYellowCardsAvg = 4.0,
                    refereeRedCardsAvg = 0.15,
                    refereePenaltiesAvg = 0.20,
                    forecast1UltraSafe = "Double Chance : Lens ou Nul (Sécurité 90%)",
                    forecast2Standard = "Moins de 2.5 buts (Équilibré 72%)",
                    forecast3Risk = "Score Exact : 2 - 0 (Audacieux 22%)",
                    dateOffset = -3
                ),
                MatchEntity(
                    homeTeam = "Bayern Munich",
                    awayTeam = "Leverkusen",
                    league = "Bundesliga",
                    matchTime = "18:30",
                    status = "FINISHED",
                    homeScore = 2,
                    awayScore = 2,
                    minute = 90,
                    odd1Xbet = 1.90,
                    oddXXbet = 3.80,
                    odd2Xbet = 3.50,
                    odd1Betpawa = 1.85,
                    oddXBetpawa = 3.70,
                    odd2Betpawa = 3.40,
                    isLive = false,
                    weatherTemp = 12,
                    weatherCondition = "Pluie",
                    pitchCondition = "Glissante",
                    refereeName = "Christian Dingert",
                    refereeYellowCardsAvg = 4.5,
                    refereeRedCardsAvg = 0.25,
                    refereePenaltiesAvg = 0.35,
                    forecast1UltraSafe = "Plus de 2.5 buts (Sécurité 93%)",
                    forecast2Standard = "Les deux équipes marquent (Équilibré 80%)",
                    forecast3Risk = "Score Exact : 2 - 2 (Audacieux 15%)",
                    dateOffset = -4
                ),
                MatchEntity(
                    homeTeam = "Manchester City",
                    awayTeam = "Liverpool",
                    league = "Premier League",
                    matchTime = "12:30",
                    status = "UPCOMING",
                    homeScore = 0,
                    awayScore = 0,
                    minute = 0,
                    odd1Xbet = 1.85,
                    oddXXbet = 4.00,
                    odd2Xbet = 3.65,
                    odd1Betpawa = 1.80,
                    oddXBetpawa = 3.90,
                    odd2Betpawa = 3.55,
                    isLive = false,
                    weatherTemp = 14,
                    weatherCondition = "Ensoleillé",
                    pitchCondition = "Parfaite",
                    refereeName = "Michael Oliver",
                    refereeYellowCardsAvg = 3.8,
                    refereeRedCardsAvg = 0.12,
                    refereePenaltiesAvg = 0.30,
                    forecast1UltraSafe = "Plus de 1.5 buts (Sécurité 95%)",
                    forecast2Standard = "Victoire de Manchester City (Équilibré 65%)",
                    forecast3Risk = "Score Exact : 2 - 1 (Audacieux 24%)",
                    dateOffset = 1
                ),
                MatchEntity(
                    homeTeam = "Real Madrid",
                    awayTeam = "Barcelona",
                    league = "La Liga",
                    matchTime = "21:00",
                    status = "UPCOMING",
                    homeScore = 0,
                    awayScore = 0,
                    minute = 0,
                    odd1Xbet = 2.00,
                    oddXXbet = 3.75,
                    odd2Xbet = 3.30,
                    odd1Betpawa = 1.95,
                    oddXBetpawa = 3.65,
                    odd2Betpawa = 3.20,
                    isLive = false,
                    weatherTemp = 22,
                    weatherCondition = "Clair",
                    pitchCondition = "Excellente",
                    refereeName = "Sánchez Martínez",
                    refereeYellowCardsAvg = 5.5,
                    refereeRedCardsAvg = 0.40,
                    refereePenaltiesAvg = 0.35,
                    forecast1UltraSafe = "Double Chance : Real Madrid ou Nul (Sécurité 91%)",
                    forecast2Standard = "Les deux équipes marquent (Équilibré 82%)",
                    forecast3Risk = "Score Exact : 3 - 2 (Audacieux 20%)",
                    dateOffset = 1
                ),
                MatchEntity(
                    homeTeam = "Chelsea",
                    awayTeam = "Aston Villa",
                    league = "Premier League",
                    matchTime = "15:00",
                    status = "UPCOMING",
                    homeScore = 0,
                    awayScore = 0,
                    minute = 0,
                    odd1Xbet = 1.95,
                    oddXXbet = 3.60,
                    odd2Xbet = 3.75,
                    odd1Betpawa = 1.90,
                    oddXBetpawa = 3.50,
                    odd2Betpawa = 3.65,
                    isLive = false,
                    weatherTemp = 13,
                    weatherCondition = "Nuageux",
                    pitchCondition = "Excellente",
                    refereeName = "Robert Jones",
                    refereeYellowCardsAvg = 4.2,
                    refereeRedCardsAvg = 0.15,
                    refereePenaltiesAvg = 0.28,
                    forecast1UltraSafe = "Double Chance : Chelsea ou Nul (Sécurité 88%)",
                    forecast2Standard = "Plus de 1.5 buts (Équilibré 75%)",
                    forecast3Risk = "Score Exact : 2 - 0 (Audacieux 25%)",
                    dateOffset = 2
                ),
                MatchEntity(
                    homeTeam = "Paris Saint-Germain",
                    awayTeam = "Marseille",
                    league = "Ligue 1",
                    matchTime = "21:00",
                    status = "UPCOMING",
                    homeScore = 0,
                    awayScore = 0,
                    minute = 0,
                    odd1Xbet = 1.50,
                    oddXXbet = 4.40,
                    odd2Xbet = 6.00,
                    odd1Betpawa = 1.48,
                    oddXBetpawa = 4.30,
                    odd2Betpawa = 5.80,
                    isLive = false,
                    weatherTemp = 18,
                    weatherCondition = "Clair",
                    pitchCondition = "Excellente",
                    refereeName = "Benoît Bastien",
                    refereeYellowCardsAvg = 4.8,
                    refereeRedCardsAvg = 0.35,
                    refereePenaltiesAvg = 0.32,
                    forecast1UltraSafe = "Victoire de Paris Saint-Germain (Sécurité 90%)",
                    forecast2Standard = "Plus de 2.5 buts (Équilibré 78%)",
                    forecast3Risk = "Score Exact : 3 - 1 (Audacieux 18%)",
                    dateOffset = 3
                )
            )
            appDao.insertMatches(prepopulated)
        }
    }

    // This function simulates real-time match events and slight odds fluctuations for active matches
    suspend fun simulateLiveUpdates() {
        val current = allMatches.firstOrNull() ?: return
        val updated = current.map { match ->
            if (match.isLive && match.status == "LIVE") {
                val nextMinute = match.minute + 1
                val newStatus = if (nextMinute >= 90) "FINISHED" else "LIVE"
                val newIsLive = nextMinute < 90

                // 2% chance of a goal being scored
                val scoreGoal = Random.nextFloat() < 0.02f
                val (newHomeScore, newAwayScore) = if (scoreGoal) {
                    if (Random.nextBoolean()) {
                        Pair(match.homeScore + 1, match.awayScore)
                    } else {
                        Pair(match.homeScore, match.awayScore + 1)
                    }
                } else {
                    Pair(match.homeScore, match.awayScore)
                }

                // Simulate slight fluctuation in odds (1xbet and betpawa)
                val variation1 = (Random.nextDouble(-0.15, 0.15))
                val variationX = (Random.nextDouble(-0.15, 0.15))
                val variation2 = (Random.nextDouble(-0.15, 0.15))

                // Simulate small weather temp and referee card fluctuations to represent real-time updates
                val newTemp = maxOf(5, minOf(40, match.weatherTemp + Random.nextInt(-1, 2)))

                match.copy(
                    minute = if (newIsLive) nextMinute else 90,
                    status = newStatus,
                    isLive = newIsLive,
                    homeScore = newHomeScore,
                    awayScore = newAwayScore,
                    weatherTemp = newTemp,
                    odd1Xbet = maxOf(1.05, String.format("%.2f", match.odd1Xbet + variation1).replace(",", ".").toDouble()),
                    oddXXbet = maxOf(1.05, String.format("%.2f", match.oddXXbet + variationX).replace(",", ".").toDouble()),
                    odd2Xbet = maxOf(1.05, String.format("%.2f", match.odd2Xbet + variation2).replace(",", ".").toDouble()),
                    odd1Betpawa = maxOf(1.05, String.format("%.2f", match.odd1Betpawa + variation1 * 0.95).replace(",", ".").toDouble()),
                    oddXBetpawa = maxOf(1.05, String.format("%.2f", match.oddXBetpawa + variationX * 0.95).replace(",", ".").toDouble()),
                    odd2Betpawa = maxOf(1.05, String.format("%.2f", match.odd2Betpawa + variation2 * 0.95).replace(",", ".").toDouble())
                )
            } else {
                // For upcoming matches, let's also slightly fluctuate odds or simulate pre-game weather shifts!
                val variation1 = (Random.nextDouble(-0.02, 0.02))
                val variationX = (Random.nextDouble(-0.02, 0.02))
                val variation2 = (Random.nextDouble(-0.02, 0.02))
                val tempShift = Random.nextInt(-1, 2)
                
                match.copy(
                    weatherTemp = maxOf(5, minOf(40, match.weatherTemp + tempShift)),
                    odd1Xbet = maxOf(1.05, String.format("%.2f", match.odd1Xbet + variation1).replace(",", ".").toDouble()),
                    oddXXbet = maxOf(1.05, String.format("%.2f", match.oddXXbet + variationX).replace(",", ".").toDouble()),
                    odd2Xbet = maxOf(1.05, String.format("%.2f", match.odd2Xbet + variation2).replace(",", ".").toDouble()),
                    odd1Betpawa = maxOf(1.05, String.format("%.2f", match.odd1Betpawa + variation1 * 0.95).replace(",", ".").toDouble()),
                    oddXBetpawa = maxOf(1.05, String.format("%.2f", match.oddXBetpawa + variationX * 0.95).replace(",", ".").toDouble()),
                    odd2Betpawa = maxOf(1.05, String.format("%.2f", match.odd2Betpawa + variation2 * 0.95).replace(",", ".").toDouble())
                )
            }
        }
        appDao.insertMatches(updated)
    }
}
