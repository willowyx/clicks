import kotlin.random.Random
import Constants.prettyFormat
import Constants as constants

object Mods {
    // these do not change constants, but rather modify game rules / gameplay
    lateinit var logger: GameLogger

    var autoPack: Boolean = false
    var autoPackSp: Int = 150
    fun autoPackToggle() {
        if (constants.currentMoney >= autoPackSp && !autoPack) {
            constants.currentMoney -= autoPackSp
            autoPack = true
            logger.log("[OK] Autopack enabled.")
        } else if (autoPack) {
            logger.log("[INFO] Auto-pack disabled; initial cost refunded.")
            constants.currentMoney += autoPackSp
            autoPack = false
        } else {
            logger.log("[WARN] insufficient funds")
        }
    }

    var hedgeFund: Boolean = false
    var hedgeFundSp: Int = 5000
    var hedgeRisk: Double = 0.15
    var hedgeGain: Double = 1.95
    fun startHedgeFund() {
        if (constants.currentMoney >= hedgeFundSp && !hedgeFund) {
            constants.currentMoney -= hedgeFundSp
            hedgeFund = true
            logger.log("[OK] Hedge fund enabled. Good luck.")
        } else if (hedgeFund) {
            logger.log("[WARN] Your money is gone. Never again?")
            hedgeFund = false
        } else {
            logger.log("[WARN] insufficient funds")
        }
    }
    fun buybuybuy() {
        if (constants.currentMoney >= 0.2 * constants.totalMoneyMax) {
            logger.log("[WARN] that much money would collapse the market.")
            return
        }
        if (hedgeFund && constants.currentMoney >= hedgeFundSp) {
            val investment = (constants.currentMoney * 0.75).coerceAtLeast(hedgeFundSp.toDouble())
            // 5000 minimum investment; otherwise invests 75% current money
            val profit: Long = (investment * Random.nextDouble(hedgeRisk, hedgeGain))
                .toLong().coerceAtMost(constants.totalMoneyMax) - investment.toLong()
            constants.currentMoney += profit
            constants.totalMoney += profit
            if(profit >= 0) {
                logger.log("[OK] Hedge fund profit: ${profit.prettyFormat()}")
            } else {
                if(hedgeRisk < 0.35) {
                    hedgeRisk += 0.01
                    logger.log("[WARN] Hedge fund loss: ${profit.prettyFormat()}. Can't end on a loss...?")
                    return
                }
                logger.log("[WARN] Hedge fund loss: ${profit.prettyFormat()}. Ouch.")
            }
        } else {
            logger.log("[WARN] insufficient funds to invest")
        }
    }

    // coffee run
    var CRInternStatus = false
    var CRInternSp = 950_000_000_000        // 950 billion
    fun startCRIntern() {
        if (constants.currentMoney >= CRInternSp) {
            constants.currentMoney -= CRInternSp
            CRInternStatus = true
        } else {
            logger.log("[WARN] insufficient funds. work harder!")
        }
    }
    fun calcApplyCRBonus(score: Int, rewardType: Int) {      // takes existing score and calculates percentage bonus
        if(constants.minReward < constants.minRewardMax) {
            // score up to 20; result should be between -2.00 and 2.00
            val adjustment: Double = ((score * 5 * 2) / 100.0).coerceIn(-2.00, 2.00)
            // todo: charge money for placing order
            if(rewardType == 1) {
                constants.minReward += (constants.minRewardIntv * adjustment).toLong()
                if(constants.minReward < constants.minRewardIntv) {                 // prevent negative amounts
                    constants.minReward = constants.minRewardIntv.toLong()
                }
            } else {
                constants.packRewardAmount += (constants.packRewardAmountIntv * adjustment).toLong()
                if(constants.packRewardAmount < constants.packRewardAmountIntv) {   // prevent negative amounts
                    constants.packRewardAmount = constants.packRewardAmountIntv.toLong()
                }
            }
        }
    }

    var prevClickTime: Long = 0L

    fun modResetClick() {
        val now = System.currentTimeMillis()
        if (now - prevClickTime <= 1000) {
            constants.resetAll()
            CRInternStatus = false
            prevClickTime = 0L
            logger.log("[OK] All mods except auto-pack have been reset.")
        } else {
            prevClickTime = now
            logger.log("[WARN] Double-click to activate. All attributes will be reset!")
        }
    }

    fun prestigeResetAuto() {
        constants.resetAll()
        CRInternStatus = false

        constants.currentClicks = 0
        constants.currentMoney = 0
        constants.packBonusProgress = 0

        constants.currentPrestige++
    }
}
