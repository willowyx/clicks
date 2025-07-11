object Constants {
    var ticksPerSecond: Int = 1                     // calculations are run every tick period
    var ticksPerSecondLv: Int = 1
    var ticksPerSecondSp: Int = 250
    var ticksPerSecondIntv: Int = 1
    var ticksPerSecondMax: Int = 25_000
    // can be increased; capped at 25 000
    var uncertaintyFloor: Double = 0.1              // smallest uncertainty value
    var uncertaintyFloorLv: Int = 1
    var uncertaintyFloorSp: Int = 500
    var uncertaintyFloorIntv: Double = 0.1
    var uncertaintyFloorMax: Double = 10.0
    // can be increased; upper limit: uncertLimit
    var uncertaintyLimit: Double = 3.0              // largest uncertainty variance (not for money except bonuses)
    var uncertaintyLimitLv: Int = 1
    var uncertaintyLimitSp: Int = 750
    var uncertaintyLimitIntv: Double = 0.1
    var uncertaintyLimitMin: Double = 1.0.coerceAtLeast(uncertaintyFloor) // CHECK THIS
    var uncertaintyLimitMax: Double = 10.0
    // can be increased or decreased; lower limit: uncertFloor + 0.1; upper limit: 10.0
    var clicksPerPack: Int = 250                    // clicks required per package
    var clicksPerPackLv: Int = 1
    var clicksPerPackSp: Int = 750
    var clicksPerPackIntv: Int = 1_000
    var clicksPerPackMax: Int = 10_000_000
    // can be increased or decreased; capped at 10 000 000
    var packRewardAmount: Long = 50                  // base reward per package delivered
    var packRewardAmountLv: Int = 1
    var packRewardAmountSp: Int = 250
    var packRewardAmountIntv: Int = 100
    var packRewardAmountMax: Int = 100_000_000 // CHECK THIS
    // can be increased
    var bonusPayInterval: Int = 20                  // bonus given per bonus interval or perfect package
    var bonusPayIntervalLv: Int = 1
    var bonusPayIntervalSp: Int = 100
    var bonusPayIntervalIntv: Int = 1
    var bonusPayIntervalMin: Int = 1
    // can be decreased; lower limit: 1
    var bonusPayScale: Double = 1.1                 // bonus = packRewardAmount * bonusPayScale
    var bonusPayScaleLv: Int = 1
    var bonusPayScaleSp: Int = 100
    var bonusPayScaleIntv: Double = 0.3
    var bonusPayScaleMax: Double = 100.0
    // can be increased; capped at 100.0
    var clicksPerTick: Int = 5                      // clicks generated per tick period
    var clicksPerTickLv: Int = 1
    var clicksPerTickSp: Int = 500
    var clicksPerTickIntv: Int = 75
    var clicksPerTickMax: Int = 250_000
    // can be increased; capped at 250 000 [cannot exceed 1/5 of clicksPerPack * uncertaintyLimit]
    var fuzzySelectRange: Int = 10                  // allow packaging of clicks within this range
    var fuzzySelectRangeLv: Int = 1
    var fuzzySelectRangeSp: Int = 250
    var fuzzySelectRangeIntv: Int = 1
    var fuzzySelectRangeMin: Int = 1
    var fuzzySelectRangeMax: Int = 1_000_000
    // can be increased or decreased; lower limit: 1;
    // upper limit: 1 000 000 [1/10 of clicksPerPack]
    var fuzzySelectPenaltyUnit: Int = 1             // inaccuracy penalty is applied per n clicks from target
    var fuzzySelectPenaltyUnitLv: Int = 1
    var fuzzySelectPenaltyUnitSp: Int = 150
    var fuzzySelectPenaltyUnitIntv: Int = 25
    var fuzzySelectPenaltyUnitMax: Int = 100_000_000
    // can be increased; capped at 1 000 000
    var maxPenalty: Double = 0.9                    // max penalty for inaccurate packs (as % of base reward)
    var maxPenaltyLv: Int = 1
    var maxPenaltySp: Int = 250
    var maxPenaltyIntv: Double = 0.1
    var maxPenaltyMin: Double = 0.1
    // can be decreased; lower limit: 0.1
    var minReward: Long = 10                         // consolation prize for unpackable clicks (if way too high)
    var minRewardLv: Int = 1
    var minRewardSp: Int = 100
    var minRewardIntv: Int = 25
    var minRewardMax: Int = 1_000_000
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
    // game state variable
}
