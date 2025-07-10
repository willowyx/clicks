data class Constants(
    var ticksPerSecond: Int = 1,                    // calculations are run every tick period
    // can be increased; capped at 25 000
    var uncertaintyFloor: Double = 0.1,             // smallest uncertainty value
    // can be increased; upper limit: uncertLimit
    var uncertaintyLimit: Double = 3.0,             // largest uncertainty variance (not for money except bonuses)
    // can be increased or decreased; lower limit: uncertFloor + 0.1; upper limit: 10.0
    var currentClicks: Int = 0,                     // unpackaged clicks
    // game state variable
    var currentPacks: Long = 0,                      // number of packages completed
    // game state variable
    var packBonusAmount: Int = 0,                   // progress towards next bonus
    // game state variable
    var clicksPerPack: Int = 250,                   // clicks required per package
    // can be increased; capped at 10 000 000
    var packRewardAmount: Long = 50,                 // base reward per package delivered
    // can be increased
    var bonusPayInterval: Int = 20,                 // bonus given per bonus interval or perfect package
    // can be decreased; lower limit: 1
    var bonusPayScale: Double = 1.1,                // bonus = packRewardAmount * bonusPayScale
    // can be increased; capped at 100.0
    var clicksPerTick: Int = 5,                     // clicks generated per tick period
    // can be increased; capped at 250 000 [cannot exceed 1/5 of clicksPerPack * uncertaintyLimit]
    var currentMoney: Long = 0,                      // accumulated unspent money
    // game state variable
    var currentPrestige: Int = 0,                   // prestige-linked multipliers are always positive
    // game state variable
    var combinedClicks: Long = 0,                    // total clicks statistic
    // game state variable
    var totalTicks: Long = 0,                        // total ticks statistic
    // game state variable
    var fuzzySelectRange: Int = 10,                 // allow packaging of clicks within this range
    // can be increased or decreased; lower limit: 1; upper limit: 1 000 000 [1/10 of clicksPerPack]
    var fuzzySelectPenaltyUnit: Int = 1,            // inaccuracy penalty applied per n clicks from target
    // can be increased; capped at 1 000 000
    var maxPenalty: Double = 0.9,                   // max penalty for inaccurate packs (as % of base reward)
    // can be decreased; lower limit: 0.1
    var minReward: Int = 10,                        // consolation prize for unpackable clicks (if way too high)
    // can be increased; capped at 1 000 000 (tentative)
)
