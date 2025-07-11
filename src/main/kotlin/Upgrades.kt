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
            logger.log("[INFO] Autopack enabled.")
        } else if (autoPack) {
            logger.log("[INFO] Auto-pack disabled; initial cost refunded.")
            constants.currentMoney += autoPackSp
            autoPack = false
        } else {
            logger.log("[INFO] insufficient funds")
        }
    }
}
