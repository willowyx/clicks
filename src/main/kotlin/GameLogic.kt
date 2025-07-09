package dev.willowyx

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.text.compareTo

class GameLogic {
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    private var constants: Constants = Constants()

    fun genStart() {
        scope.launch {
            while (true) {
                val tickrtn: Int = runTick()
                constants.currentClicks += tickrtn                                          // update clicks
                tryPackage()                                                                // check if packageable
                constants.currentMoney += calcPrestige()                                    // apply bonuses
                delay(1000L)
            }
        }
    }

    suspend fun tryPackage() {
        val minSelect = constants.clicksPerPack - (constants.fuzzySelectRange - 1)
        val maxSelect = constants.clicksPerPack + (constants.fuzzySelectRange + 1)
        val specificity = abs(constants.clicksPerPack - constants.currentClicks)
        val maxPenaltyInt = (constants.maxPenalty * constants.packRewardAmount).toInt()
        val calculatedPenaltyInt = (specificity * constants.fuzzySelectPenaltyUnit).toInt()

        if (constants.currentClicks < minSelect) {
            println("${constants.currentClicks}/${constants.clicksPerPack} clicks")
            return
        }

        awaitInput("Press Enter to package...")

        when {
            constants.currentClicks == constants.clicksPerPack -> {
                constants.currentMoney += calcPackageReward()
                constants.currentMoney += calcPackBonus()
                constants.currentPacks += 1
                constants.currentClicks = 0
                constants.packBonusAmount += 1
                println("perfect package, applied full reward plus bonus")
            }
            constants.currentClicks in minSelect..maxSelect -> {
                val penalty = calculatedPenaltyInt.coerceAtMost(maxPenaltyInt)
                constants.currentMoney += calcPackageReward() - penalty
                constants.currentPacks += 1
                constants.currentClicks = 0
                constants.packBonusAmount += 1
                println("over/undershot but within range; applied penalty of $penalty")
            }
            else -> {
                constants.currentMoney += calcPackageReward() - maxPenaltyInt
                constants.currentClicks = 0
                println("overshot by more than max of ${constants.fuzzySelectRange}, consolation prize applied")
            }
        }

        if (constants.packBonusAmount % constants.bonusPayInterval == 0) {
            constants.packBonusAmount = 0
            constants.currentMoney += calcPackBonus()
            println("bonus interval applied & reset")
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
        println("clicks: $clickrtn")
        return clickrtn
    }

    fun calcPackageReward(): Int {      // calculate reward for a single package
        var moneyrtn: Int = constants.packRewardAmount
        if(constants.currentPacks % constants.bonusPayInterval == 0) {              // check if eligible for bonus
            moneyrtn = (calcUncertainty() * (moneyrtn * constants.bonusPayScale)).toInt()      // apply bonus
        }
        println("package reward: $moneyrtn, total: ${constants.currentMoney + moneyrtn}")
        return moneyrtn
    }

    fun calcUncertainty(): Double {     // random uncertainty scaler from floor to limit
        var uncertaintyrtn = constants.uncertaintyFloor + Math.random() * (constants.uncertaintyLimit - constants.uncertaintyFloor)
//        println("uncertainty: $uncertaintyrtn")
        return uncertaintyrtn
    }

    fun calcPrestige(): Int {           // calculate prestige bonus to add (includes stat scaler)
        val prestigeScale: Double = 1 + (constants.currentPrestige * 0.3)
        var prestigertn = constants.currentMoney * (1 + calcUncertainty())          // apply additional bonus scaler
        prestigertn = (prestigertn * prestigeScale)                                 // apply prestige scaler
        return prestigertn.toInt()
    }

    suspend fun awaitInput(message: String) {
        println(message)
        withContext(Dispatchers.IO) {
            readLine()
        }
    }

    fun stop() {
        // need to implement save state when stopping
        job.cancel()
    }
}

fun main() {
    val gl = GameLogic()
    gl.genStart()
    println("Press Enter to quit...")
    readLine()
    gl.stop()
}
