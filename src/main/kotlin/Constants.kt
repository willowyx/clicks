import kotlin.math.abs
import kotlin.math.pow

object Constants {
    lateinit var logger: GameLogger

    fun Long.prettyFormat(): String {
        val absValue = abs(this)
        return when {
            absValue >= 1_000_000_000 -> String.format("%.2fB", this / 1_000_000_000.0)
            absValue >= 1_000_000     -> String.format("%.2fM", this / 1_000_000.0)
            absValue >= 1_000         -> String.format("%.2fk", this / 1_000.0)
            else                      -> this.toString()
        }
    }

    fun getRefundEligibility(): Boolean {
        return (clicksPerTickLv > 1 && clicksPerPackLv > 1 && ticksPerSecondLv > 1 &&
            packRewardAmountLv > 1 && bonusPayIntervalLv > 1 && bonusPayScaleLv > 1 &&
            uncertaintyFloorLv > 1 && abs(uncertaintyLimitLv) > 1 && abs(fuzzySelectRangeLv) > 1 &&
            fuzzySelectPenaltyUnitLv > 1 && abs(maxPenaltyLv) > 1 && minRewardLv > 1)
    }
    fun getRefundPrice(): Long {
        return if(getRefundEligibility()) {
            (clicksPerTickPrice() + clicksPerPackPrice() +
                    ticksPerSecondPrice() + packRewardAmountPrice() +
                    bonusPayIntervalPrice() + bonusPayScalePrice() +
                    uncertaintyFloorPrice() + uncertaintyLimitPrice() +
                    fuzzySelectRangePrice() + fuzzySelectPenaltyUnitPrice() +
                    maxPenaltyPrice() + minRewardPrice())
        } else {
            0L
        }
    }
    fun resetAll() {
        currentMoney += getRefundPrice()            // refund all upgrades
        clicksPerTickLv = 1
        clicksPerPackLv = 1
        ticksPerSecond = 1
        packRewardAmountLv = 1
        bonusPayIntervalLv = 1
        bonusPayScaleLv = 1
        uncertaintyFloorLv = 1
        uncertaintyLimitLv = 1
        fuzzySelectRangeLv = 1
        fuzzySelectPenaltyUnitLv = 1
        maxPenaltyLv = 1
        minRewardLv = 1
        logger.log("[WARN] All game constants reset to default values.")
    }

    var ticksPerSecond: Int = 1                     // calculations are run every tick period
    var ticksPerSecondLv: Int = 1                   // attribute level
    var ticksPerSecondSp: Int = 175                 // base upgrade price
    var ticksPerSecondIntv: Int = 1                 // interval by which the attribute is increased
    var ticksPerSecondMax: Int = 250                // soft cap (can be exceeded by no more than 1 additional interval)
    fun ticksPerSecondPrice(): Long {
        return (ticksPerSecondSp * 1.25.pow((ticksPerSecondLv - 1).toDouble())).toLong()
    }
    fun ticksPerSecondAdd(): String {
        if(ticksPerSecond >= ticksPerSecondMax) {
            return "[WARN] max level reached"
        }
        if(currentMoney >= ticksPerSecondPrice()) {
            currentMoney -= ticksPerSecondPrice()
            ticksPerSecondLv ++      // increase level
            ticksPerSecond += ticksPerSecondIntv        // increase attribute
            return "[OK] ticksPerSecond increased to $ticksPerSecondLv"
        } else {
            return "[WARN] insufficient funds"
        }
    }
    // can be increased; capped at 250

    var uncertaintyFloor: Double = 0.1              // smallest uncertainty value
    var uncertaintyFloorLv: Int = 1
    var uncertaintyFloorSp: Int = 500
    var uncertaintyFloorIntv: Double = 0.1
    var uncertaintyFloorMax: Double = 10.0
    fun uncertaintyFloorPrice(): Long {
        return (uncertaintyFloorSp * 1.0.pow((uncertaintyFloorLv - 1).toDouble())).toLong()
    }
    fun uncertaintyFloorAdd(): String {
        if(uncertaintyFloor >= uncertaintyFloorMax) {
            return "[WARN] limit reached"
        }
        if(currentMoney >= uncertaintyFloorPrice()) {
            currentMoney -= uncertaintyFloorPrice()
            uncertaintyFloorLv ++      // increase level
            uncertaintyFloor += uncertaintyFloorIntv        // increase attribute
            return "[OK] uncertaintyFloor increased to %.2f".format(uncertaintyFloor)
        } else {
            return "[WARN] insufficient funds"
        }
    }
    // can be increased; upper limit: uncertLimit

    var uncertaintyLimit: Double = 3.0              // largest uncertainty variance (not for money except bonuses)
    var uncertaintyLimitLv: Int = 1
    var uncertaintyLimitSp: Int = 750
    var uncertaintyLimitIntv: Double = 0.1
    var uncertaintyLimitMin: Double = 1.0.coerceAtLeast(0.1 + uncertaintyFloor) // CHECK THIS
    var uncertaintyLimitMax: Double = 10.0
    fun uncertaintyLimitPrice(): Long {
        return (uncertaintyLimitSp * 1.0.pow(abs((abs(uncertaintyLimitLv) - 1)).toDouble())).toLong()
    }
    fun uncertaintyLimitAdd(): String {
        if(uncertaintyLimit >= uncertaintyLimitMax) {
            return "[WARN] max level reached"
        }
        if(currentMoney >= uncertaintyLimitPrice()) {
            currentMoney -= uncertaintyLimitPrice()
            uncertaintyLimitLv ++      // increase level
            uncertaintyLimit += uncertaintyLimitIntv        // increase attribute
            return "[OK] uncertaintyLimit increased to %.2f".format(uncertaintyLimit)
        } else {
            return "[WARN] insufficient funds"
        }
    }
    fun uncertaintyLimitSub(): String {
        if(uncertaintyLimit <= uncertaintyLimitMin) {
            return "[WARN] min level reached"
        }
        if(currentMoney >= uncertaintyLimitPrice()) {
            currentMoney -= uncertaintyLimitPrice()
            uncertaintyLimitLv --      // decrease level
            uncertaintyLimit -= uncertaintyLimitIntv        // decrease attribute
            return "[OK] uncertaintyLimit decreased to %.2f".format(uncertaintyLimit)
        } else {
            return "[WARN] insufficient funds"
        }
    }
    // can be increased or decreased; lower limit: uncertFloor + 0.1; upper limit: 10.0

    var clicksPerPack: Int = 250                    // clicks required per package
    var clicksPerPackLv: Int = 1
    var clicksPerPackSp: Int = 425
    var clicksPerPackIntv: Int = 250
    var clicksPerPackMax: Int = 10_000_000
    fun clicksPerPackPrice(): Long {
        return (clicksPerPackSp * 1.05.pow((clicksPerPackLv - 1).toDouble())).toLong()
    }
    fun clicksPerPackAdd(): String {
        if(clicksPerPack >= clicksPerPackMax) {
            return "[WARN] max level reached"
        }
        if(currentMoney >= clicksPerPackPrice()) {
            currentMoney -= clicksPerPackPrice()
            clicksPerPackLv ++      // increase level
            clicksPerPack += clicksPerPackIntv        // increase attribute
            return "[OK] clicksPerPack increased to $clicksPerPack"
        } else {
            return "[WARN] insufficient funds"
        }
    }
    fun clicksPerPackNatAdd() {
        if(clicksPerPack >= clicksPerPackMax) {
            return                                    // upper limit reached
        }
        val bpchance = Math.random()
        if(bpchance <= 0.01) {
            val clickIncreaseAmt = (clicksPerPack * 0.1).toInt()
            clicksPerPack += clickIncreaseAmt
        }
    }
    // can be increased or decreased; capped at 10 000 000

    var packRewardAmount: Long = 50                  // base reward per package delivered
    var packRewardAmountLv: Int = 1
    var packRewardAmountSp: Int = 250
    var packRewardAmountIntv: Int = 100
    var packRewardAmountMax: Int = 100_000_000 // CHECK THIS
    fun packRewardAmountPrice(): Long {
        return (packRewardAmountSp * 1.7.pow((packRewardAmountLv - 1).toDouble())).toLong()
    }
    fun packRewardAmountAdd(): String {
        if(packRewardAmount >= packRewardAmountMax) {
            return "[WARN] max level reached"
        }
        if(currentMoney >= packRewardAmountPrice()) {
            currentMoney -= packRewardAmountPrice()
            packRewardAmountLv ++      // increase level
            packRewardAmount += packRewardAmountIntv        // increase attribute
            return "[OK] packRewardAmount increased to $packRewardAmount"
        } else {
            return "[WARN] insufficient funds"
        }
    }
    // can be increased

    var bonusPayInterval: Int = 20                  // bonus given per bonus interval or perfect package
    var bonusPayIntervalLv: Int = 1
    var bonusPayIntervalSp: Int = 50
    var bonusPayIntervalIntv: Int = 1
    var bonusPayIntervalMin: Int = 1
    fun bonusPayIntervalPrice(): Long {
        return (bonusPayIntervalSp * 1.9.pow((bonusPayIntervalLv - 1).toDouble())).toLong()
    }
    fun bonusPayIntervalAdd(): String {
        if(bonusPayInterval <= bonusPayIntervalMin) {
            return "[WARN] max level reached"
        }
        if(currentMoney >= bonusPayIntervalPrice()) {
            currentMoney -= bonusPayIntervalPrice()
            bonusPayIntervalLv ++      // increase level
            bonusPayInterval -= bonusPayIntervalIntv        // decrease attribute
            return "[OK] bonusPayInterval decreased to $bonusPayInterval"
        } else {
            return "[WARN] insufficient funds"
        }
    }
    // can be decreased; lower limit: 1


    var bonusPayScale: Double = 1.1                 // bonus = packRewardAmount * bonusPayScale
    var bonusPayScaleLv: Int = 1
    var bonusPayScaleSp: Int = 50
    var bonusPayScaleIntv: Double = 0.3
    var bonusPayScaleMax: Double = 100.0
    fun bonusPayScalePrice(): Long {
        return (bonusPayScaleSp * 1.25.pow((bonusPayScaleLv - 1).toDouble())).toLong()
    }
    fun bonusPayScaleAdd(): String {
        if(bonusPayScale >= bonusPayScaleMax) {
            return "[WARN] max level reached"
        }
        if(currentMoney >= bonusPayIntervalPrice()) {
            currentMoney -= bonusPayIntervalPrice()
            bonusPayScaleLv ++      // increase level
            bonusPayScale += bonusPayScaleIntv        // increase attribute
            return "[OK] bonusPayScale increased to %.2f".format(bonusPayScale)
        } else {
            return "[WARN] insufficient funds"
        }
    }
    // can be increased; capped at 100.0

    var clicksPerTick: Int = 5                      // clicks generated per tick period
    var clicksPerTickLv: Int = 1
    var clicksPerTickSp: Int = 350
    var clicksPerTickIntv: Int = 75
    var clicksPerTickMax: Int = 250_000
    fun clicksPerTickPrice(): Long {
        return (clicksPerTickSp * 1.15.pow((clicksPerTickLv - 1).toDouble())).toLong()
    }
    fun clicksPerTickAdd(): String {
        if(clicksPerTick >= clicksPerTickMax) {
            return "[WARN] max level reached"
        }
        if(currentMoney >= clicksPerTickPrice()) {
            currentMoney -= clicksPerTickPrice()
            clicksPerTickLv ++                          // increase level
            clicksPerTick += clicksPerTickIntv          // increase attribute
            if(clicksPerTick * 6 > clicksPerPack) {     // calculate balancing adjustment after increase
                clicksPerPack = (clicksPerTick * 6)     // ensure clicks/pack is always at least 6x clicks/tick
                logger.log("[INFO] clicksPerPack increased to $clicksPerPack to maintain balance")
            }

            return "[OK] clicksPerTick increased to $clicksPerTick"
        } else {
            return "[WARN] insufficient funds"
        }
    }
    // can be increased; capped at 250 000 [cannot exceed 1/5 of clicksPerPack * uncertaintyLimit]

    var fuzzySelectRange: Int = 10                  // allow packaging of clicks within this range
    var fuzzySelectRangeLv: Int = 1
    var fuzzySelectRangeSp: Int = 250
    var fuzzySelectRangeIntv: Int = 1
    var fuzzySelectRangeMin: Int = 1
    var fuzzySelectRangeMax: Int = 1_000_000
    fun fuzzySelectRangePrice(): Long {
        return (fuzzySelectRangeSp * 1.0.pow((fuzzySelectRangeLv - 1).toDouble())).toLong()
    }
    fun fuzzySelectRangeAdd(): String {
        if(fuzzySelectRange >= fuzzySelectRangeMax) {
            return "[WARN] max level reached"
        }
        if(currentMoney >= fuzzySelectRangePrice()) {
            currentMoney -= fuzzySelectRangePrice()
            fuzzySelectRangeLv ++      // increase level
            fuzzySelectRange += fuzzySelectRangeIntv        // increase attribute
            return "[OK] fuzzySelectRange increased to $fuzzySelectRange"
        } else {
            return "[WARN] insufficient funds"
        }
    }
    fun fuzzySelectRangeSub(): String {
        if(fuzzySelectRange <= fuzzySelectRangeMin) {
            return "[WARN] min level reached"
        }
        if(currentMoney >= fuzzySelectRangePrice()) {
            currentMoney -= fuzzySelectRangePrice()
            fuzzySelectRangeLv --      // decrease level
            fuzzySelectRange -= fuzzySelectRangeIntv        // decrease attribute
            return "[OK] fuzzySelectRange decreased to $fuzzySelectRange"
        } else {
            return "[WARN] insufficient funds"
        }
    }
    // can be increased or decreased; lower limit: 1;
    // upper limit: 1 000 000 [1/10 of clicksPerPack]

    var fuzzySelectPenaltyUnit: Int = 1             // inaccuracy penalty is applied per n clicks from target
    var fuzzySelectPenaltyUnitLv: Int = 1
    var fuzzySelectPenaltyUnitSp: Int = 325
    var fuzzySelectPenaltyUnitIntv: Int = 9
    var fuzzySelectPenaltyUnitMax: Int = 1_000_000
    fun fuzzySelectPenaltyUnitPrice(): Long {
        return (fuzzySelectPenaltyUnitSp * 1.5.pow((fuzzySelectPenaltyUnitLv - 1).toDouble())).toLong()
    }
    fun fuzzySelectPenaltyUnitAdd(): String {
        if(fuzzySelectPenaltyUnit >= fuzzySelectPenaltyUnitMax) {
            return "[WARN] max level reached"
        }
        if(currentMoney >= fuzzySelectPenaltyUnitPrice()) {
            currentMoney -= fuzzySelectPenaltyUnitPrice()
            fuzzySelectPenaltyUnitLv ++      // increase level
            fuzzySelectPenaltyUnit += fuzzySelectPenaltyUnitIntv        // increase attribute
            return "[OK] fuzzySelectPenaltyUnit increased to $fuzzySelectPenaltyUnit"
        } else {
            return "[WARN] insufficient funds"
        }
    }
    // can be increased; capped at 1 000 000

    var maxPenalty: Double = 0.9                    // max penalty for inaccurate packs (as % of base reward)
    var maxPenaltyLv: Int = 1
    var maxPenaltySp: Int = 150
    var maxPenaltyIntv: Double = 0.1
    var maxPenaltyMin: Double = 0.1
    fun maxPenaltyPrice(): Long {
        return (maxPenaltySp * 1.5.pow((maxPenaltyLv - 1).toDouble())).toLong()
    }
    fun maxPenaltyAdd(): String {
        if(maxPenalty <= maxPenaltyMin) {
            return "[WARN] max level reached"
        }
        if(currentMoney >= maxPenaltyPrice()) {
            currentMoney -= maxPenaltyPrice()
            maxPenaltyLv ++      // increase level
            maxPenalty -= maxPenaltyIntv        // decrease attribute
            return "[OK] maxPenalty decreased to %.2f".format(maxPenalty)
        } else {
            return "[WARN] insufficient funds"
        }
    }
    // can be decreased; lower limit: 0.1


    var minReward: Long = 10                         // consolation prize for unpackable clicks (if way too high)
    var minRewardLv: Int = 1
    var minRewardSp: Int = 50
    var minRewardIntv: Int = 25
    var minRewardMax: Int = 1_000_000
    fun minRewardPrice(): Long {
        return (minRewardSp * 1.35.pow((minRewardLv - 1).toDouble())).toLong()
    }
    fun minRewardAdd(): String {
        if(minReward >= minRewardMax) {
            return "[WARN] max level reached"
        }
        if(currentMoney >= minRewardPrice()) {
            currentMoney -= minRewardPrice()
            minRewardLv ++      // increase level
            minReward += minRewardIntv        // increase attribute
            return "[OK] minReward increased to $minReward"
        } else {
            return "[WARN] insufficient funds"
        }
    }
    // can be increased; capped at 1 000 000 (tentative)


    // game state variables (cannot be directly upgraded)
    var currentClicks: Int = 0                      // unpackaged clicks
    // game state variable
    var currentPacks: Long = 0                       // number of packages completed
    // game state variable
    var packBonusProgress: Int = 0                    // progress towards next bonus
    // game state variable
    var currentMoney: Long = 0                       // accumulated unspent money
    // game state variable
    var currentPrestige: Int = 0                    // prestige-linked multipliers are always positive
    // game state variable
    var combinedClicks: Long = 0                     // total clicks statistic
    // game state variable
    var totalTicks: Long = 0                         // total ticks statistic
    // game state variable
    var totalMoney: Long = 0                         // total money statistic
    var totalMoneyMax: Long = 1_000_000_000
    // game state variable; upper limit: 1 000 000 000
}
