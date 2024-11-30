import android.graphics.Bitmap
import android.os.Parcelable
import com.example.nflstatsapp.data.api.PlayerStats
import com.example.nflstatsapp.data.api.Stat
import java.io.Serializable
import java.math.BigDecimal

data class PlayerStatsData(
    val fullName: String,
    val position: String,
    val teamName: String,
    val jerseyNumber: String,
    val headshotData: Bitmap?,
    val fantasyPointsPerGame: BigDecimal?,
    val stats: PlayerStats?
) : Serializable
