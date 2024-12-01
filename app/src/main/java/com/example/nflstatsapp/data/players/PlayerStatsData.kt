import com.example.nflstatsapp.data.api.PlayerStats
import java.io.Serializable
import java.math.BigDecimal

data class PlayerStatsData(
    val fullName: String?,
    val position: String?,
    val teamName: String?,
    val jerseyNumber: String?,
    val headshotData: ByteArray,
    val fantasyPointsPerGame: BigDecimal?,
    val stats: PlayerStats?
): Serializable