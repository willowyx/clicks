data class Constants(
    var ticksPerSecond: Int = 1,                    // calculations are run every tick period
    // can be increased
    var uncertaintyFloor: Double = 0.1,             // smallest uncertainty value
    // can be increased
    var uncertaintyLimit: Double = 3.0,             // largest uncertainty variance (not for money except bonuses)
    // can be increased or decreased
    var currentClicks: Int = 0,                     // unpackaged clicks
    // game state variable
    var currentPacks: Int = 0,                      // number of packages completed
    // game state variable
    var packBonusAmount: Int = 0,                   // progress towards next bonus
    //
    var clicksPerPack: Int = 250,                   // clicks required per package

    var packRewardAmount: Int = 50,                 // base reward per package delivered

    var bonusPayInterval: Int = 20,                 // bonus given per bonus interval or perfect package

    var bonusPayScale: Double = 1.1,                // bonus = packRewardAmount * bonusPayScale

    var clicksPerTick: Int = 5,                     // clicks generated per tick period

    var currentMoney: Int = 0,                      // accumulated unspent money

    var currentPrestige: Int = 0,                   // prestige-linked multipliers are always positive

    var combinedClicks: Int = 0,                    // total clicks statistic

    var totalTicks: Int = 0,                        // total ticks statistic

    var fuzzySelectRange: Int = 10,                 // allow packaging of clicks within this range

    var fuzzySelectPenaltyUnit: Int = 1,            // penalty for inaccurate packs (per n clicks from target)
    // can be increased
    var maxPenalty: Double = 0.9,                   // max penalty for inaccurate packs (as % of base reward)
    // can be decreased
    var minReward: Int = 10,                        // consolation prize for unpackable clicks (if way too high)
    // can be increased
)
