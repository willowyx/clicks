import kotlin.random.Random
import Constants as constants

object Upgrades {
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
    var hedgeFundSp: Int = 2000
    var hedgeRisk: Double = 0.5
    var hedgeGain: Double = 2.5
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
        if (hedgeFund && constants.currentMoney >= hedgeFundSp) {
            val investment = constants.currentMoney * 0.5
            val profit: Long = (investment * Random.nextDouble(hedgeRisk, hedgeGain))
                .toLong().coerceAtMost(constants.totalMoneyMax) - investment.toLong()
            constants.currentMoney += profit
            if(profit >= 0) {
                logger.log("[OK] Hedge fund profit: $profit")
            } else {
                if(hedgeRisk < 0.75) {
                    hedgeRisk += 0.05
                    logger.log("[WARN] Hedge fund loss: ${-profit}. Can't end on a loss...?")
                }
                logger.log("[WARN] Hedge fund loss: $profit. Ouch.")
            }
        } else if (!hedgeFund) {
            logger.log("[WARN] Start a hedge fund first")
        } else {
            logger.log("[WARN] insufficient funds to invest")
        }
    }


}
