package dev.willowyx

class Constants {
    private var ticksPerSecond = 1                  // calculations are run every tick period
    private var uncertaintyScale: Double = 2.0      // range that "neutral" variance can affect generation
    private var bonusStatScale: Double = 1.1        // upper limit for positive stats bonus (before prestige)
    private var currentClicks = 0                   // unspent
    private var currentPacks = 0                    // number of packages completed
    private var packBonusAmount = 0                 // progress towards next bonus
    private var clicksPerPack = 350                 // clicks required per package
    private var packRewardAmount = 20               // base reward per package delivered
    private var bonusPayInterval = 50               // bonus given per bonusPayInterval packages
    private var bonusPayScale: Double = 1.1         // bonus = packRewardAmount * bonusPayScale
    private var clicksPerTick = 10                  // clicks generated per tick period
    private var currentMoney = 0
    private var currentPrestige = 0
    private var combinedClicks = 0
    private var fuzzySelectRange = 10               // acceptable range to allow packaging


    // GETTERS

    fun getTicksPerSecond(): Int {
        return ticksPerSecond
    }
    fun getUncertaintyScale(): Double {
        return uncertaintyScale
    }
    fun getBonusStatScale(): Double {
        return bonusStatScale
    }
    fun getCurrentClicks(): Int {
        return currentClicks
    }
    fun getCurrentPacks(): Int {
        return currentPacks
    }
    fun getPackBonusAmount(): Int {
        return packBonusAmount
    }
    fun getClicksPerPack(): Int {
        return clicksPerPack
    }
    fun getPackRewardAmount(): Int {
        return packRewardAmount
    }
    fun getBonusPayInterval(): Int {
        return bonusPayInterval
    }
    fun getBonusPayScale(): Double {
        return bonusPayScale
    }
    fun getClicksPerTick(): Int {
        return clicksPerTick
    }
    fun getCurrentMoney(): Int {
        return currentMoney
    }
    fun getCurrentPrestige(): Int {
        return currentPrestige
    }
    fun getCombinedClicks(): Int {
        return combinedClicks
    }
    fun getFuzzySelectRange(): Int {
        return fuzzySelectRange
    }

    // SETTERS

    fun setTicksPerSecond(value: Int) {
        ticksPerSecond = value
    }
    fun setUncertaintyScale(value: Double) {
        uncertaintyScale = value
    }
    fun setBonusStatScale(value: Double) {
        bonusStatScale = value
    }
    fun setCurrentClicks(value: Int) {
        currentClicks = value
    }
    fun setCurrentPacks(value: Int) {
        currentPacks = value
    }
    fun setPackBonusAmount(value: Int) {
        packBonusAmount = value
    }
    fun setClicksPerPack(value: Int) {
        clicksPerPack = value
    }
    fun setPackRewardAmount(value: Int) {
        packRewardAmount = value
    }
    fun setBonusPayInterval(value: Int) {
        bonusPayInterval = value
    }
    fun setBonusPayScale(value: Double) {
        bonusPayScale = value
    }
    fun setClicksPerTick(value: Int) {
        clicksPerTick = value
    }
    fun setCurrentMoney(value: Int) {
        currentMoney = value
    }
    fun setCurrentPrestige(value: Int) {
        currentPrestige = value
    }
    fun setCombinedClicks(value: Int) {
        combinedClicks = value
    }
    fun setFuzzySelectRange(value: Int) {
        fuzzySelectRange = value
    }
}