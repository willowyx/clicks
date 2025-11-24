import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import org.lwjgl.util.tinyfd.TinyFileDialogs
import java.util.Properties

data class SaveStateData (      // todo: refresh const values on load??
    val saveName: String,
    val minVersion: String,     // todo: validate version on load?

    val ticksPerSecond: Int,
    var ticksPerSecondLv: Int,

    val uncertaintyFloor: Double,
    var uncertaintyFloorLv: Int,

    val uncertaintyLimit: Double,
    val uncertaintyLimitLv: Int,

    val clicksPerPack: Int,
    val clicksPerPackLv: Int,

    val packRewardAmount: Long,
    val packRewardAmountLv: Int,

    val bonusPayInterval: Int,
    val bonusPayIntervalLv: Int,

    val bonusPayScale: Double,
    val bonusPayScaleLv: Int,

    val clicksPerTick: Int,
    val clicksPerTickLv: Int,

    val fuzzySelectRange: Int,
    val fuzzySelectRangeLv: Int,

    val fuzzySelectPenaltyUnit: Int,
    val fuzzySelectPenaltyUnitLv: Int,

    val maxPenalty: Double,
    val maxPenaltyLv: Int,
    val minReward: Long,
    val minRewardLv: Int,
    var currentClicks: Long,
    var currentPacks: Long,
    var packBonusProgress: Int,
    var currentMoney: Long,
    var currentPrestige: Int,
    var combinedClicks: Long,
    var totalTicks: Long,
    var totalMoney: Long,

    var autoPack: Boolean,
    var hedgeFund: Boolean,
    var hedgeRisk: Double,
    var CRInternStatus: Boolean
)

object State {
    lateinit var collectedSaveData: SaveStateData // for saving only
    lateinit var logger: GameLogger
    private val version: String by lazy {
        val props = Properties()
        val stream = State::class.java.getResourceAsStream("/version.properties")
        stream.use { props.load(it) }
        props.getProperty("version", "0.0.0")
    }
    private val objectMapper = ObjectMapper().registerKotlinModule()

    fun initializeStateSave(saveName: String) {
        collectedSaveData = SaveStateData (
            saveName = saveName,
            minVersion = version,
            ticksPerSecond = Constants.ticksPerSecond,
            ticksPerSecondLv = Constants.ticksPerSecondLv,
            uncertaintyFloor = Constants.uncertaintyFloor,
            uncertaintyFloorLv = Constants.uncertaintyFloorLv,
            uncertaintyLimit = Constants.uncertaintyLimit,
            uncertaintyLimitLv = Constants.uncertaintyLimitLv,
            clicksPerPack = Constants.clicksPerPack,
            clicksPerPackLv = Constants.clicksPerPackLv,
            packRewardAmount = Constants.packRewardAmount,
            packRewardAmountLv = Constants.packRewardAmountLv,
            bonusPayInterval = Constants.bonusPayInterval,
            bonusPayIntervalLv = Constants.bonusPayIntervalLv,
            bonusPayScale = Constants.bonusPayScale,
            bonusPayScaleLv = Constants.bonusPayScaleLv,
            clicksPerTick = Constants.clicksPerTick,
            clicksPerTickLv = Constants.clicksPerTickLv,
            fuzzySelectRange = Constants.fuzzySelectRange,
            fuzzySelectRangeLv = Constants.fuzzySelectRangeLv,
            fuzzySelectPenaltyUnit = Constants.fuzzySelectPenaltyUnit,
            fuzzySelectPenaltyUnitLv = Constants.fuzzySelectPenaltyUnitLv,
            maxPenalty = Constants.maxPenalty,
            maxPenaltyLv = Constants.maxPenaltyLv,
            minReward = Constants.minReward,
            minRewardLv = Constants.minRewardLv,
            currentClicks = Constants.currentClicks,
            currentPacks = Constants.currentPacks,
            packBonusProgress = Constants.packBonusProgress,
            currentMoney = Constants.currentMoney,
            currentPrestige = Constants.currentPrestige,
            combinedClicks = Constants.combinedClicks,
            totalTicks = Constants.totalTicks,
            totalMoney = Constants.totalMoney,
            autoPack = Upgrades.autoPack,
            hedgeFund = Upgrades.hedgeFund,
            hedgeRisk = Upgrades.hedgeRisk,
            CRInternStatus = Upgrades.CRInternStatus
        )
    }

    fun loadStateData() {
        val data = collectedSaveData
        logger.log("[INFO] Loading save file: ${data.saveName}...")
        Constants.ticksPerSecond = data.ticksPerSecond
        Constants.ticksPerSecondLv = data.ticksPerSecondLv
        Constants.uncertaintyFloor = data.uncertaintyFloor
        Constants.uncertaintyFloorLv = data.uncertaintyFloorLv
        Constants.uncertaintyLimit = data.uncertaintyLimit
        Constants.uncertaintyLimitLv = data.uncertaintyLimitLv
        Constants.clicksPerPack = data.clicksPerPack
        Constants.clicksPerPackLv = data.clicksPerPackLv
        Constants.packRewardAmount = data.packRewardAmount
        Constants.packRewardAmountLv = data.packRewardAmountLv
        Constants.bonusPayInterval = data.bonusPayInterval
        Constants.bonusPayIntervalLv = data.bonusPayIntervalLv
        Constants.bonusPayScale = data.bonusPayScale
        Constants.bonusPayScaleLv = data.bonusPayScaleLv
        Constants.clicksPerTick = data.clicksPerTick
        Constants.clicksPerTickLv = data.clicksPerTickLv
        Constants.fuzzySelectRange = data.fuzzySelectRange
        Constants.fuzzySelectRangeLv = data.fuzzySelectRangeLv
        Constants.fuzzySelectPenaltyUnit = data.fuzzySelectPenaltyUnit
        Constants.fuzzySelectPenaltyUnitLv = data.fuzzySelectPenaltyUnitLv
        Constants.maxPenalty = data.maxPenalty
        Constants.maxPenaltyLv = data.maxPenaltyLv
        Constants.minReward = data.minReward
        Constants.minRewardLv = data.minRewardLv
        Constants.currentClicks = data.currentClicks
        Constants.currentPacks = data.currentPacks
        Constants.packBonusProgress = data.packBonusProgress
        Constants.currentMoney = data.currentMoney
        Constants.currentPrestige = data.currentPrestige
        Constants.combinedClicks = data.combinedClicks
        Constants.totalTicks = data.totalTicks
        Constants.totalMoney = data.totalMoney
        Upgrades.autoPack = data.autoPack
        Upgrades.hedgeFund = data.hedgeFund
        Upgrades.hedgeRisk = data.hedgeRisk
        Upgrades.CRInternStatus = data.CRInternStatus
        logger.log("[OK] Save data successfully loaded.")
    }

    fun saveStateDialog(): Boolean {
        val path = TinyFileDialogs.tinyfd_saveFileDialog(
            "Save game - clicks",
            "${collectedSaveData.saveName}.json",
            null,
            "JSON files (*.json)"
        )
        if (path != null) {
            val saveFile = File(path)
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(saveFile, collectedSaveData)
            logger.log("[OK] Game saved to ${saveFile.name}")
            return true
        }
        logger.log("[INFO] cancelled saving game")
        return false
    }

    fun loadStateDialog(): Boolean {
        val path = TinyFileDialogs.tinyfd_openFileDialog(
            "Load save - clicks",
            "",
            null,
            "JSON files (*.json)",
            false
        )
        if (path != null) {
            val saveFile = File(path)
            if (saveFile.exists()) {
                val data = objectMapper.readValue(saveFile, SaveStateData::class.java)
                collectedSaveData = data
                loadStateData()
                return true
            } else {
                logger.log("[ERROR] data file not found: ${saveFile.path}")
                return false
            }
        }
        logger.log("[INFO] cancelled loading save")
        return false
    }
}
