import Constants.prettyFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.math.abs
import Constants as constants
import Upgrades as upgrades

class GameLogic(private val logger: GameLogger) {
    val cgenlogic = CoffeeGen()
    fun getTargetOrder(): CoffeeOrder {
        return cgenlogic.getValidatedOrder()
    }
    fun regenCoffeeOrder() {
        cgenlogic.setValidatedOrder(cgenlogic.initializeOrderGen())
    }

    private lateinit var userOrderFormData: CoffeeOrderFormData
    fun getUserOrder(): CoffeeOrderFormData {
        return userOrderFormData
    }
    fun setUserOrder(order: CoffeeOrderFormData) {
        userOrderFormData = order
    }

    private var job = Job()
    private var scope = CoroutineScope(Dispatchers.Default + job)

    private var awaitingInput = false
    fun isAwaitingInput(): Boolean = awaitingInput
    private var inputContinuation: (Continuation<Unit>)? = null
    private var jobIsRunning: Boolean = false
    fun getJobRunStateInd(): Boolean = jobIsRunning
    fun setJobRunStateInd(state: Boolean) {
        jobIsRunning = state
    }

    private fun resetScope() {
        job = Job()
        scope = CoroutineScope(Dispatchers.Default + job)
    }

    fun genStart() {
        resetScope()
        if(getJobRunStateInd()) {
            logger.log("[ERROR] Game process is already running. Please stop it first.")
            return
        } else {
            setJobRunStateInd(true)
        }
        scope.launch {
            while (true) {
                for( i in 1..constants.ticksPerSecond) {                                // run for each tick
//                    logger.log("subtick $i/${constants.ticksPerSecond}")
                    val tickrtn: Int = runTick()
                    constants.currentClicks += tickrtn                                        // update clicks
                    constants.combinedClicks += tickrtn                                       // update total clicks
                    tryPackage()                                                              // check if packageable
                    constants.currentMoney += calcPrestige()                                  // apply bonuses
                }
                logger.log("${constants.currentClicks}/${constants.clicksPerPack} clicks") // prints per tick
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
//            logger.log("${constants.currentClicks}/${constants.clicksPerPack} clicks") // prints per subtick
            return
        }

        if(!upgrades.autoPack) {
            awaitInput("[READY] Ready to package ${constants.currentClicks} clicks")
        } else {
            logger.log("[INFO] Packaging ${constants.currentClicks} clicks")
        }

        when {
            constants.currentClicks == constants.clicksPerPack.toLong() -> {
                val prewarddef = calcPackageReward().coerceAtLeast(constants.minReward)
                val pcalcbonusdef = calcPackBonus()                // define package bonus amount
                constants.currentMoney += prewarddef + pcalcbonusdef
                constants.totalMoney += prewarddef + pcalcbonusdef
                constants.currentPacks += 1
                constants.currentClicks = 0
                constants.packBonusProgress ++
                logger.log("[INFO] perfect package, applied full reward plus bonus")
            }
            constants.currentClicks in minSelect..maxSelect -> {
                val prewarddef = calcPackageReward().coerceAtLeast(constants.minReward)
                val calcPenaltyInt = prewarddef * calculatedPenalty     // calculate reward after penalty
                val penalty = calcPenaltyInt.coerceAtMost(maxPenaltyInt.toDouble()).toInt()
                constants.currentMoney += (prewarddef - penalty).coerceAtLeast(constants.minReward)
                constants.totalMoney += (prewarddef - penalty).coerceAtLeast(constants.minReward)
                constants.currentPacks += 1
                constants.currentClicks = 0
                constants.packBonusProgress ++
                logger.log("[INFO] over/undershot but within range; applied penalty of $penalty")
            }
            else -> {
                constants.currentMoney += constants.minReward           // give minReward
                constants.totalMoney += constants.minReward
                constants.currentClicks = 0
                logger.log("[INFO] overshot by more than max ${constants.fuzzySelectRange} clicks")
            }
        }

        if (constants.packBonusProgress % constants.bonusPayInterval == 0 && constants.packBonusProgress != 0) {
            val packbonusrtn = calcPackBonus()
            constants.packBonusProgress = 0
            constants.currentMoney += packbonusrtn
            constants.totalMoney += packbonusrtn
//            logger.log("bonus $packbonusrtn applied & interval reset; total: ${constants.currentMoney}")
        }
    }

    fun runTick(): Int {                // run tick, return clicks generated without updating stats
        constants.refreshConstValues()
        val tickrtn = calcClicks()
        constants.totalTicks++                                              // increment total ticks
        constants.clicksPerPackNatAdd()                                     // 5% chance to increase clicks/pack by 1%
        return tickrtn
    }

    fun calcPackBonus(): Int {
        return ((constants.packRewardAmount * constants.bonusPayScale) * calcUncertainty()).toInt() // calc pack bonus
    }

    fun calcClicks(): Int {             // calculate clicks + uncertainty
        val clickrtn = (constants.clicksPerTick * calcUncertainty()).toInt()
//        logger.log("clicks: $clickrtn")
        return clickrtn
    }

    fun calcPackageReward(): Long {      // calculate reward for a single package
        var moneyrtn = constants.packRewardAmount
        val packRwdUncert = calcUncertainty("boostMin")
//        logger.log("reward uncertainty: $packRwdUncert")
        if(constants.currentPacks % constants.bonusPayInterval == 0.toLong()) {              // check if eligible for bonus
            moneyrtn += (packRwdUncert * (moneyrtn * constants.bonusPayScale)).toInt()      // apply bonus
        }
//        logger.log("package reward: $moneyrtn")
        return moneyrtn
    }

    fun calcUncertainty(adj: String = ""): Double {     // random uncertainty scaler from floor to limit
        return if(adj == "boostMin") {
            val effUncertFloor = constants.uncertaintyFloor.coerceAtLeast(1.0)
            effUncertFloor + Math.random() * (constants.uncertaintyLimit - effUncertFloor)
        } else {
            constants.uncertaintyFloor + Math.random() * (constants.uncertaintyLimit - constants.uncertaintyFloor)
        }
//        logger.log("uncertainty: $uncertaintyrtn")
    }

    fun calcPrestige(): Int {
        if (constants.currentPrestige == 0) return 0

//        val base = constants.currentPacks + (constants.totalTicks / 100)
        val uncertainty = calcUncertainty("boostMin")
        val prestigeScale = 1 + (constants.currentPrestige * 0.3)
        val prestigeBonus = uncertainty * prestigeScale

//        logger.log("Prestige bonus applied: ${prestigeBonus.toInt()}")
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
    clicks............${constants.currentClicks.prettyFormat()}
    money.............${constants.currentMoney.prettyFormat()}
    bonusIntvProg.....${constants.packBonusProgress}
    prestige..........${constants.currentPrestige}
    
    === ACTUAL ===
    clicksMin.........${constants.realTickRange_min}
    clicksMax.........${constants.realTickRange_max}
    
    === VARIABLES ===
    baseClickAmt......${constants.clicksPerTick}
    clicksPerPack.....${constants.clicksPerPack}
    subticks..........${constants.ticksPerSecond}
    baseReward........${constants.packRewardAmount}
    bonusRewardIntv...${constants.bonusPayInterval}
    bonusScaleAmt.....${"%.2f".format(constants.bonusPayScale)}
    uncertMin.........${"%.2f".format(constants.uncertaintyFloor)}
    uncertMax.........${"%.2f".format(constants.uncertaintyLimit)}
    fuzzyRange........${constants.fuzzySelectRange}
    fuzzyPenaltyIntv..${constants.fuzzySelectPenaltyUnit}
    maxPenalty........${"%.2f".format(constants.maxPenalty)}
    minReward.........${constants.minReward}
    
    === STATS ===
    clicksTotal.......${(constants.combinedClicks).prettyFormat()}
    moneyTotal........${(constants.totalMoney).prettyFormat()}
    packaged..........${constants.currentPacks}
    ticksElapsed......${constants.totalTicks}
    """.trimIndent()
    }

    fun stop() {
        // TODO: need to implement save state when stopping
        job.cancel()
        setJobRunStateInd(false)
    }
}
