import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.math.abs

class GameLogic(private val logger: GameLogger) {
    private var job = Job()
    private var scope = CoroutineScope(Dispatchers.Default + job)

    private var constants = Constants()
    private var upgrades = Upgrades()

    private var awaitingInput = false
    fun isAwaitingInput(): Boolean = awaitingInput
    private var inputContinuation: (Continuation<Unit>)? = null

    private fun resetScope() {
        job = Job()
        scope = CoroutineScope(Dispatchers.Default + job)
    }

    fun genStart() {
        resetScope()
        scope.launch {
            while (true) {
                for( i in 1..constants.ticksPerSecond) {                                // run for each tick
                    logger.log("subtick $i/${constants.ticksPerSecond}")
                    val tickrtn: Int = runTick()
                    constants.currentClicks += tickrtn                                        // update clicks
                    tryPackage()                                                              // check if packageable
                    constants.currentMoney += calcPrestige()                                  // apply bonuses
//                    println(statDump())
//                    println("current money ${constants.currentMoney}")
                }
                delay(1000L)
            }
        }
    }

    suspend fun tryPackage() {
        val minSelect = constants.clicksPerPack - (constants.fuzzySelectRange - 1)
        val maxSelect = constants.clicksPerPack + (constants.fuzzySelectRange + 1)
        val specificity = abs(constants.clicksPerPack - constants.currentClicks)
        val maxPenaltyInt = (constants.maxPenalty * constants.packRewardAmount).toInt()
        val calculatedPenalty = ((specificity / constants.fuzzySelectPenaltyUnit) * 0.1)    // calc penalty %

        if (constants.currentClicks < minSelect) {
            logger.log("${constants.currentClicks}/${constants.clicksPerPack} clicks")
            return
        }

        if(!upgrades.autoPack) {
            awaitInput("[READY] Ready to package ${constants.currentClicks} clicks")
        }

        when {
            constants.currentClicks == constants.clicksPerPack -> {
                val prewarddef = calcPackageReward()
                constants.currentMoney += prewarddef
                constants.currentMoney += calcPackBonus()
                constants.currentPacks += 1
                constants.currentClicks = 0
                constants.packBonusAmount += 1
//                logger.log("perfect package, applied full reward of $prewarddef; new total: ${constants.currentMoney}")
            }
            constants.currentClicks in minSelect..maxSelect -> {
                val prewarddef = calcPackageReward()                    // define package reward amount
                val calcPenaltyInt = prewarddef * calculatedPenalty     // calculate reward after penalty
                val penalty = calcPenaltyInt.coerceAtMost(maxPenaltyInt.toDouble()).toInt()
                constants.currentMoney += (prewarddef - penalty).coerceAtLeast(constants.minReward)
                constants.currentPacks += 1
                constants.currentClicks = 0
                constants.packBonusAmount += 1
//                logger.log("over/undershot but within range; applied penalty of $penalty; new total: ${constants.currentMoney}")
            }
            else -> {
                constants.currentMoney += calcPackageReward() - maxPenaltyInt
                constants.currentClicks = 0
//                logger.log("overshot by more than max ${constants.fuzzySelectRange}. ${constants.minReward} applied; new total: ${constants.currentMoney}")
            }
        }

        if (constants.packBonusAmount % constants.bonusPayInterval == 0) {
            val packbonusrtn = calcPackBonus()
            constants.packBonusAmount = 0
            constants.currentMoney += packbonusrtn
//            logger.log("bonus $packbonusrtn applied & interval reset; total: ${constants.currentMoney}")
        }
    }

    fun runTick(): Int {                // run tick, return clicks generated without updating stats
        val tickrtn = calcClicks()
        constants.totalTicks++                                                             // increment total ticks
        return tickrtn
    }

    fun calcPackBonus(): Int {
        return ((constants.packBonusAmount * constants.bonusPayScale) * calcUncertainty()).toInt() // calc pack bonus
    }

    fun calcClicks(): Int {             // calculate clicks + uncertainty
        val clickrtn = (constants.clicksPerTick * calcUncertainty()).toInt()
//        logger.log("clicks: $clickrtn")
        return clickrtn
    }

    fun calcPackageReward(): Long {      // calculate reward for a single package
        var moneyrtn = constants.packRewardAmount
        if(constants.currentPacks % constants.bonusPayInterval == 0.toLong()) {              // check if eligible for bonus
            moneyrtn += (calcUncertainty() * (moneyrtn * constants.bonusPayScale)).toInt()      // apply bonus
        }
//        logger.log("package reward: $moneyrtn")
        return moneyrtn
    }

    fun calcUncertainty(): Double {     // random uncertainty scaler from floor to limit
        val uncertaintyrtn = constants.uncertaintyFloor + Math.random() * (constants.uncertaintyLimit - constants.uncertaintyFloor)
//        logger.log("uncertainty: $uncertaintyrtn")
        return uncertaintyrtn
    }

    fun calcPrestige(): Int {
        if (constants.currentPrestige == 0) return 0

        val base = constants.currentPacks + (constants.totalTicks / 10)
        val uncertainty = calcUncertainty().coerceAtMost(1.0 + (.1 * constants.uncertaintyLimit))
        val prestigeScale = 1 + (constants.currentPrestige * 0.3)
        val prestigeBonus = base * uncertainty * prestigeScale

        logger.log("Prestige bonus applied: ${prestigeBonus.toInt()}")
        return prestigeBonus.toInt()
    }

    suspend fun awaitInput(message: String) {
        logger.log(message)
        return suspendCancellableCoroutine { cont ->
            awaitingInput = true
            inputContinuation = cont
        }
    }

    fun confirmInput() {
        inputContinuation?.resume(Unit)
        inputContinuation = null
        awaitingInput = false
    }

    fun statDump(): String {
        return """
    === CURRENT ===
    clicks............${constants.currentClicks}
    money.............${constants.currentMoney}
    prestige..........${constants.currentPrestige}
    === VARIABLES ===
    baseClickAmt......${constants.clicksPerTick}
    subticks..........${constants.ticksPerSecond}
    baseReward........${constants.packRewardAmount}
    scaleBonus........${constants.packBonusAmount}
    uncertMin.........${constants.uncertaintyFloor}
    uncertMax.........${constants.uncertaintyLimit}
    fuzzyRange........${constants.fuzzySelectRange}
    fuzzyPenaltyIntv..${constants.fuzzySelectPenaltyUnit}
    maxPenalty........${constants.maxPenalty}
    minReward.........${constants.minReward}
    === STATS ===
    clicks............${constants.combinedClicks}
    packaged..........${constants.currentPacks}
    ticks.............${constants.totalTicks}
    """.trimIndent()
    }

    fun stop() {
        // need to implement save state when stopping
        job.cancel()
    }
}

// removed main as game interface should be handled by ui