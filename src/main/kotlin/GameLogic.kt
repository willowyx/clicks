import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.math.abs
import Constants.prettyFormat
import Constants as constants
import CoffeeGen as cgenlogic
import Mods as mods

class GameLogic(private val logger: GameLogger) {
    fun getTargetOrder(): CoffeeOrder {
        return cgenlogic.getValidatedOrder()
    }
    fun regenCoffeeOrder() {
        cgenlogic.setValidatedOrder(cgenlogic.initializeOrderGen())
    }

    private lateinit var userOrderFormData: CoffeeOrderFormData
    fun getUserOrder(): CoffeeOrderFormData { return userOrderFormData }
    fun setUserOrder(order: CoffeeOrderFormData) { userOrderFormData = order }

    private var job = Job()
    private var scope = CoroutineScope(Dispatchers.Default + job)

    private var awaitingInput = false
    fun isAwaitingInput(): Boolean = awaitingInput
    private var inputContinuation: (Continuation<Unit>)? = null

    private var jobIsRunning: Boolean = false
    fun getJobRunStateInd(): Boolean = jobIsRunning
    fun setJobRunStateInd(state: Boolean) { jobIsRunning = state }

    private var isGameStarted: Boolean = false
    fun getIsGameStarted(): Boolean = isGameStarted
    fun setIsGameStarted(state: Boolean) { isGameStarted = state }

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
            setIsGameStarted(true)
        }
        scope.launch {
            while (true) {
                for( i in 1..constants.ticksPerSecond) {                                // run for each tick
//                    logger.log("subtick $i/${constants.ticksPerSecond}")
                    val tickrtn: Int = runTick()
                    constants.currentClicks += tickrtn                                        // update clicks
                    constants.combinedClicks += tickrtn                                       // update total clicks
                    tryPackage()                                                              // check if packageable
                }
                logger.log("${constants.currentClicks.prettyFormat()}/${constants.clicksPerPack.prettyFormat()} clicks")
                constants.refreshConstValues()
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

        if(!mods.autoPack) {
            awaitInput("[READY] Ready to package ${constants.currentClicks} clicks")
        } else {
            logger.log("[INFO] Packaging ${constants.currentClicks} clicks")
        }

        when {
            constants.currentClicks == constants.clicksPerPack.toLong() -> {
                val prewarddef = (calcPackageReward().coerceAtLeast(constants.minReward) * calcPrestige()).toLong()
                val pcalcbonusdef = calcPackBonus()                // define package bonus amount
                constants.currentMoney += prewarddef + pcalcbonusdef
                constants.totalMoney += prewarddef + pcalcbonusdef
                constants.currentPacks += 1
                constants.currentClicks = 0
                constants.packBonusProgress ++
                logger.log("[INFO] perfect package, applied full reward plus bonus")
            }
            constants.currentClicks in minSelect..maxSelect -> {
                val prewarddef = (calcPackageReward().coerceAtLeast(constants.minReward) * calcPrestige()).toLong()
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
                constants.currentMoney += (constants.minReward * calcPrestige()).toLong()           // give minReward
                constants.totalMoney += (constants.minReward * calcPrestige()).toLong()
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

    fun calcPrestige(): Double {
        if (constants.currentPrestige == 0) return 1.0 // return multiplier 1 (none) if not prestiged

//        val base = constants.currentPacks + (constants.totalTicks / 100)
        val prestigeScale = 1 + (constants.currentPrestige * 0.3)
        val prestigeBonus = calcUncertainty("boostMin") * prestigeScale

//        logger.log("Prestige bonus applied: ${prestigeBonus.toInt()}")
        return prestigeBonus
    }

    fun getValidatedCredits(): String {
        fun getStrFor(p: Array<Int>): String { var compstr = ""
            for(i in 0..<p.size) { compstr += p[i].toChar().toString() }
            return compstr }
        val aa = arrayOf(83, 112, 101, 99, 105, 97, 108, 32, 116, 104, 97, 110, 107, 115, 32, 116, 111)
        val ba = getStrFor(aa)
        val ab = arrayOf(103, 97, 98)
        val bb = getStrFor(ab)
        val ac = arrayOf(97, 110, 100)
        val bc = getStrFor(ac)
        val ad = arrayOf(119, 101, 115, 116, 111)
        val bd = getStrFor(ad)
        val ae = arrayOf(102, 111, 114, 32, 116, 101, 115, 116, 105, 110, 103, 32, 97, 110, 100, 32, 109, 111, 114, 97, 108, 32, 115, 117, 112, 112, 111, 114, 116)
        val be = getStrFor(ae)
        val af = arrayOf(33, 33, 32, 94, 45, 94)
        val bf = getStrFor(af)
        return listOfNotNull(ba, bb.takeIf { UI.getLayoutMode() !in listOf(2) }, bc.takeIf { UI.getLayoutMode() !in listOf(2, 3) }, bd.takeIf { UI.getLayoutMode() !in listOf(3) }, be, bf).joinToString(" ")
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
    clicksMin.........${constants.realTickRange_min.prettyFormat()}
    clicksMax.........${constants.realTickRange_max.prettyFormat()}
    
    === VARIABLES ===
    baseClickAmt......${constants.clicksPerTick.prettyFormat()}
    clicksPerPack.....${constants.clicksPerPack.prettyFormat()}
    subticks..........${constants.ticksPerSecond}
    baseReward........${constants.packRewardAmount.prettyFormat()}
    bonusRewardIntv...${constants.bonusPayInterval}
    bonusScaleAmt.....${constants.bonusPayScale.prettyFormat()}
    uncertMin.........${constants.uncertaintyFloor.prettyFormat()}
    uncertMax.........${constants.uncertaintyLimit.prettyFormat()}
    fuzzyRange........${constants.fuzzySelectRange}
    fuzzyPenaltyIntv..${constants.fuzzySelectPenaltyUnit}
    maxPenalty........${constants.maxPenalty.prettyFormat()}
    minReward.........${constants.minReward.prettyFormat()}
    
    === STATS ===
    clicksTotal.......${(constants.combinedClicks).prettyFormat()}
    moneyTotal........${(constants.totalMoney).prettyFormat()}
    packaged..........${constants.currentPacks}
    ticksElapsed......${constants.totalTicks}
    
    === QA ===
    layoutMode@UI.....${UI.getLayoutMode()}
    """.trimIndent()
    }

    fun stop() {
        job.cancel()
        setJobRunStateInd(false)
    }
}
