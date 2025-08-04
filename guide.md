# clicks: playing guide
###### for version 0.14.x

#### Note: for clarity, game variables and terminology will be formatted `like this` whenever possible.

#### Note: this guide describes fictional game mechanics and currencies, and does not attempt to provide any advice regarding real-world investments or financial decisions.

***

## I. Game Mechanics

### a. Defining Terms
* `Clicks` are the main currency of the game, generated passively.
* These Clicks are `packaged` into `packs` when a certain threshold is reached (clicks per pack).
* `Money` is the secondary currency of the game, which is earned primarily by packaging Clicks into Packs. Money can be used to purchase two types of modifiers:
  * `Upgrades`, which linearly modify existing game variables; and
  * `Mods`, which introduce new mechanics that affect the game.
* A `tick` is a unit of time in the game equal to 1 second, during which game mechanics and variables are updated.
* `Prestige` is a game mechanic that allows you to reset your game progress in exchange for a permanent stat bonus, speeding up subsequent runs on the same save.
  * This mechanic is not yet fully implemented but will be by the 1.0 release.

###### These mechanics will be described in more detail or elaborated on as necessary throughout the rest of guide.

### b. An Overview
Clicks is a game in which the currency `clicks` are passively generated.
These `clicks` are `packaged` into `packs` when they reach a certain amount in exchange  for `money`, which in turn can be used to purchase `upgrades`
and `mods` affecting nearly every aspect of the game, including the speed of generating `clicks`, the amount of `money` rewarded for `packs`, and the
number of `clicks` required to create a `pack`.

### c. Game State Variables
These variables track the game state and cannot be directly modified by the player. They are updated every tick.
* `clicks` measures the number of unpackaged clicks you have accumulated.
  * `clicksTotal` measures the total number of clicks generated during your current run
* `money` measures the amount of unspent money you currently have.
  * `moneyTotal` measures the total amount of money you have earned during the run, including money you have spent.
* `prestige` measures the number of times you have reset your game progress in exchange for a permanent stat bonus.
* `bonusIntvProg` measures your progress towards the next bonus reward, which is rewarded every `bonusIntv` packs packaged (20 by default).
* `packaged` measures the number of times you have packaged a set of clicks.
* `ticksElapsed` measures the number of ticks that have passed since the game began.

###### Game upgrades are listed and explained under Game Actions (II.b)

### d. Game parameters
These variables can be directly modified as upgrades or mods
* `baseClickAmt` is the base number of clicks generated each tick.
  * This value is modified each tick by `uncertainty`, another game parameter
  * This value is directly modified by the "base clicks per tick" upgrade
* `clicksPerPack` is the target number of clicks required to create a pack.
  * This value is directly modified by the "clicks required per pack" upgrade
* `subticks` is the number of ticks operations that are performed each tick (second). Basically, this is the number of ticks run every second. More subticks means clicks are generated faster
  * This value is directly modified by the "subticks" upgrade
* `baseReward` is the amount of money rewarded for a perfect package (one with exactly as many clicks as `clicksPerPack`).
  * This value can be affected by the packaging penalty, `uncertainty` (positive), and `prestige` (positive)
  * This value is directly modified by the "base pack reward" upgrade
* `bonusRewardInterval` is the number of successful packages elapsed before the bonus multiplier is applied to the next pack's base reward.
  * This value is directly modified by the "bonus interval" upgrade
* `bonusScaleAmt` is the multiplier applied to `baseReward` for each bonus package
  * This value is directly modified by the "bonus pay multiplier" upgrade
* `uncertMin` is the lower limit of the game-wide `uncertainty` value, which affects various aspects of generation, including bonus calculation and click generation.
  * For certain calculations like bonuses and rewards, `uncertMin` may be increased so that the resulting value is always greater than the initial value
  * The value of `uncertainty` is recalculated within the set generation parameters before every use
  * This value is directly modified by the "uncertainty floor" upgrade
* `uncertMax` is the upper limit of the game-wide `uncertainty` value
  * The value of `uncertainty` is recalculated within the set generation parameters before every use
  * This value is directly modified by the "uncertainty limit" upgrade
* `fuzzyRange` is the range in which the value of clicks is considered for packaging.
  * As soon as the number of clicks acquired enters the packaging range (between `clicksPerPack - fuzzyRange` and `fuzzyRange + clicksPerPack`), click generation stops until the current amount is packaged
  * If the clicks generated exceeds the packaging range without first entering it, the minimum reward value is applied instead of the normal reward calculation
    * Exceeding `fuzzyRange` and receiving the `minReward` value does not count as a successful package
  * This value is directly modified by the "pack penalty range" upgrade
* `fuzzyPenaltyIntv` is the interval in which the packaging penalty is applied for inaccurate packaging of clicks (-10% non-compounding base reward penalty per _n_ clicks from `clicksPerPack`)
  * For example, if `fuzzyPenaltyIntv` was set to 3 and the amount of clicks generated was 5 away from the target amount (`clicksPerPack`)
    * Only a 10% penalty would be applied in this scenario, since the penalty is incremented every 3 clicks you're off by
  * This value is directly modified by the "pack penalty interval" upgrade
* `maxPenalty` is the maximum penalty that can be applied as a result of packaging inaccuracy, calculated as a percentage of the base packaging reward.
  * The maximum penalty only applies when within range of `clicksPerPack`, as no penalty is applied to the consolation reward (`minReward`)
  * This value is directly modified by the "max penalty" upgrade
* `minReward` is the consolation reward applied when exceeding the maximum packaging range.
  * Receiving this reward does not count toward packaging bonuses
  * This value is directly modified by the "min reward" upgrade

## II. Controls window
The Controls window (labeled `Controls`) has multiple subsections, all of which contain various actions you can perform.

### a. Basic game controls
The first subsection of the Controls window contains controls for basic game actions:
* `New Game` starts a new game, provided one is not already in progress
* `Load Game` allows you to load a previously saved game from a save file
* `Save Game` allows you to save your current game to a save file
* `How to play` opens this guide

### b. Game Actions
The Game Actions subsection (labeled `Game Actions`) contains controls for the game's various mechanics.

Controls in the Game Actions subsection appear as they become relevant, and as such these controls may not always be visible:
* `Package` appears when you have acquired a sufficient number of Clicks, and resets your clicks to 0, rewarding you with `money` based on the number of Clicks generated.
  * This control will no longer appear after purchasing the `Autopack` Mod.
* `BUY BUY BUY` appears after purchasing the `Hedge fund` Mod, and allows you to spend your earned `money` with a chance to gain more or lose it.
  * At least $2,000 or 50% of your money (whichever is higher) will be `invested` each time you activate it
  * The money you invest in the hedge fund is multiplied by a random value between 0.15 and 1.85
  * Each time you invest and lose money here, the lower limit of the random value generated is increased by 0.01, with a maximum lower limit of 0.35
  * Any profit from this mod cannot cause your total money to exceed the global maximum.
    * If this happens, you will instead continually lose the money you invest until any given profit made no longer exceeds this limit.

### c. Mods
Mods can be purchased with the `money` currency and, unlike Upgrades, affect or modify gameplay instead of only affecting one parameter.

* `Autopack` allows the game to automatically pack Clicks into `packages` instead of requiring you to manually confirm each packaging operation.
  * This Mod can be refunded by clicking it again.
* `Hedge fund` enables the `BUY BUY BUY` Action.
  * This Mod can be returned, but not refunded, by clicking it again.
* `Sell everything` resets all purchased Upgrades in exchange for a partial refund.
  * This Mod can only be activated after purchasing every upgrade at least once.
  * The amount of `money` refunded is capped such that it can never cause your total money to exceed the global maximum.
  * This action cannot be reverted. Double-click the Mod to activate it.

## III. Event Log window
The Event Log window displays game events every tick and immediate feedback on your actions. A maximum of 300 events are stored. Once the limit is reached, the oldest events are overwritten as new ones are added.

* The Event Log can be filtered using the `Filter` search box to find matching text
* The Event Log history can be cleared by pressing the `Clear` button next to the Auto-scroll toggle.
* By default, the Event Log scrolls to the newest additions as they are added. To unlock scrolling and view previous events, uncheck the `Auto-scroll` checkbox.

## IV. Stats window
The Stats window displays game state information as well as the values of generation variables that may be upgraded. The Stats window is refreshed each tick.

The individual parameters found in the Stats window are described in detail under "Game parameters" (I.d)

## V. ImGui controls
Clicks uses ImGui to render and manage its user interface. Some of these features allow in-game window management that you may find useful:
* Move in-game windows by clicking and dragging on their title bar
* Resize in-game windows by clicking and dragging on their borders
* Collapse in-game windows by clicking on the arrow in their upper left corner
* Group in-game windows by clicking and dragging a window on top of another one, and releasing over the icon indicating the position you want to group them in
