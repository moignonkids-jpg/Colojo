package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val homeTeam: String,
    val awayTeam: String,
    val league: String,
    val matchTime: String,
    val status: String, // "LIVE", "UPCOMING", "FINISHED"
    val homeScore: Int,
    val awayScore: Int,
    val minute: Int,
    val odd1Xbet: Double,
    val oddXXbet: Double,
    val odd2Xbet: Double,
    val odd1Betpawa: Double,
    val oddXBetpawa: Double,
    val odd2Betpawa: Double,
    val isLive: Boolean,
    
    // Real-time weather and environment variables
    val weatherTemp: Int = 20,
    val weatherCondition: String = "Nuageux",
    val pitchCondition: String = "Excellente",
    
    // Real-time Referee stats
    val refereeName: String = "C. Turpin",
    val refereeYellowCardsAvg: Double = 3.8,
    val refereeRedCardsAvg: Double = 0.22,
    val refereePenaltiesAvg: Double = 0.31,
    
    // Core custom 3 predictions
    val forecast1UltraSafe: String = "Double chance ou Nul",
    val forecast2Standard: String = "Plus de 1.5 buts",
    val forecast3Risk: String = "Score exact 2-1",

    val dateOffset: Int = 0, // 0 = today, 1 = tomorrow, -1 = yesterday, etc.

    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_analyses")
data class SavedAnalysisEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val matchId: Int,
    val homeTeam: String,
    val awayTeam: String,
    val analysisText: String,
    val confidence: Int, // Confidence percentage (e.g., 85)
    val scores: String, // Comma-separated scores or JSON string
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val role: String, // "user" or "model"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val attachmentName: String? = null,
    val attachmentMimeType: String? = null,
    val attachmentData: String? = null // Base64 representation for display/input
)

fun MatchEntity.isTopMatch(): Boolean {
    // 1. Determine favorite (odd < 2.3 and lower than the other)
    val isHomeFavorite = odd1Xbet < odd2Xbet && odd1Xbet < 2.3
    val isAwayFavorite = odd2Xbet < odd1Xbet && odd2Xbet < 2.3
    val favoriteTeam = if (isHomeFavorite) homeTeam else if (isAwayFavorite) awayTeam else null ?: return false
    
    // 2. High confidence prediction of direct victory for favorite in at least one option
    val f1 = forecast1UltraSafe.lowercase()
    val f2 = forecast2Standard.lowercase()
    val f3 = forecast3Risk.lowercase()
    val favLower = favoriteTeam.lowercase()
    
    val hasFavWin = f1.contains("victoire de $favLower") || f1.contains("victoire d'$favLower") ||
                    f2.contains("victoire de $favLower") || f2.contains("victoire d'$favLower") ||
                    f3.contains("victoire de $favLower") || f3.contains("victoire d'$favLower")
                    
    // 3. No "nul" or "double chance" predictions among the chosen options
    val hasNulOrDouble = f1.contains("nul") || f2.contains("nul") || f3.contains("nul") ||
                         f1.contains("double chance") || f2.contains("double chance") || f3.contains("double chance")
                         
    // 4. If finished, favorite must have actually won
    val noDrawResult = if (status == "FINISHED") {
        if (isHomeFavorite) homeScore > awayScore else awayScore > homeScore
    } else {
        true
    }
    
    return hasFavWin && !hasNulOrDouble && noDrawResult
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val username: String,
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis()
)
