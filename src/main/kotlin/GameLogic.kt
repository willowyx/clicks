package dev.willowyx

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameLogic {
    // "package" groups of n Clicks and send them out for Money
    // money buys upgrades to change things
    // prestige for extra upgrades
    // more clicks don't always help as packages require a specific number of clicks

    private var constants: Constants = Constants()

    fun genStart() {
        GlobalScope.launch {
            while (true) {
                val tickrtn: Int = runTick()
                constants.setCurrentClicks(constants.getCurrentClicks() + tickrtn)      // update clicks
                tryPackage()
                constants.setCurrentMoney(constants.getCurrentMoney() + calcPrestige())
                delay(1000L)
            }
        }
    }

    fun tryPackage() {
        val minSelect = constants.getClicksPerPack() - (constants.getFuzzySelectRange() - 1)
        val maxSelect = constants.getClicksPerPack() + (constants.getFuzzySelectRange() + 1)
        if((constants.getCurrentClicks() > minSelect) && (constants.getCurrentClicks() < maxSelect)) {
            constants.setCurrentClicks(0)                                                   // reset clicks
            constants.setCurrentPacks(constants.getCurrentPacks() + 1)                      // increment packs counter
            constants.setCurrentMoney(constants.getCurrentMoney() + calcPackageReward())    // add package reward
        }
    }

    fun runTick(): Int {                // run tick, return clicks generated without updating stats
        var tickrtn = constants.getClicksPerTick()                                      // calc clicks
        tickrtn = (tickrtn * constants.getUncertaintyScale()).toInt()                   // calculate uncertainty

        return tickrtn
    }

    fun calcPackageReward(): Int {      // calculate reward for a single package
        var moneyrtn: Int = constants.getPackRewardAmount()
        if(constants.getCurrentPacks() % constants.getBonusPayInterval() == 0) {        // check if eligible for bonus
            moneyrtn = (moneyrtn * constants.getBonusPayScale()).toInt()                // apply bonus
        }

        return moneyrtn
    }

    fun calcUncertainty(): Double {     // calculate uncertainty
        return 1.0 + Math.random() * (constants.getUncertaintyScale() - 1.0)    // random from 1 to uncertainty scaler
    }

    fun calcPrestige(): Int {           // calculate prestige bonus to add
        var prestigeScale: Double = 1 + (constants.getCurrentPrestige() * 0.2)
        var prestigertn = constants.getCurrentMoney() * calcUncertainty()           // apply additional bonus scaler
        prestigertn = (prestigertn * prestigeScale)                                 // apply prestige scaler
        return prestigertn.toInt()
    }
}