data class CoffeeOrder (
    val name: String,
    val dept: String,
    val size: String,
    val temp: String,
    val syrup: String,
    val type: String,
    val dairy: String,
    val modA: String,
    val modB: String,
    val special: String = ""
)

data class CoffeeOrderFormData (    // form data somewhat translated into coffee order
    val size: String,
    val temp: String,
    val syrup: String,
    val type: String,
    val dairy: String,
    val iceAmount: String,
    val sugar: String,
    val isDecaf: Boolean,
    val addEspresso: Boolean,
    val steamed: Boolean,
    val reward: Int,
    val debug: Boolean
)

data class CoffeeScoreRow(
    val label: String,
    val expected: String,
    val actual: String,
    val isCorrect: Boolean,
    val points: Int,
    val logMessage: String
)

data class CoffeeScoreResult(
    val score: Int,
    val rows: List<CoffeeScoreRow>
)

object CoffeeGen {
    lateinit var logger: GameLogger

    // name & dept aren't important
    private val name = listOf("Lane", "Jerry", "Ben", "Alice", "Jo", "Eric", "Lee", "Ronald")
    private val dept = listOf("HR", "Accounting", "Legal", "IT", "Sales", "Marketing")

    private val size = listOf("extra small", "small", "medium", "large", "none")
    private val temp = listOf("iced", "hot", "none")
    private val syrup = listOf("caramel", "dark chocolate", "pecan", "pumpkin spice", "vanilla", "none")
    private val type = listOf("americano", "black", "breve", "cappucino", "cold brew", "espresso", "hot chocolate", "latte", "macchiato")
    private val dairy = listOf("2 percent milk", "almond milk", "cream", "half-and-half", "oat milk", "skim milk", "soy milk", "condensed milk", "whole milk", "no dairy")
    private val modA = listOf("decaf", "extra ice", "lactose free", "light ice", "no ice", "none")
    private val modB = listOf("add granulated sugar", "add sugar syrup", "extra espresso", "no sugar", "steamed milk", "none")

    private val withSugar = listOf("condensed milk", "dark chocolate", "add granulated sugar", "add sugar syrup", "pecan", "pumpkin spice", "vanilla")
    private val withLactose = arrayOf("2 percent milk", "breve", "condensed milk", "cream", "half-and-half", "skim milk", "whole milk")

    private var validatedOrder = initializeOrderGen()
    fun getValidatedOrder(): CoffeeOrder { return validatedOrder }
    fun setValidatedOrder(order: CoffeeOrder) { validatedOrder = order }

    private var crDebugEnabled: Boolean = false     // default
    fun getDebugEnabled(): Boolean { return crDebugEnabled }
    fun setDebugEnabled(enabled: Boolean) { crDebugEnabled = enabled }

    private var rewardTypeVar = 1     // default
    fun getRewardType(): Int { return rewardTypeVar }
    fun setRewardType(type: Int) { rewardTypeVar = type }

    fun initializeOrderGen(): CoffeeOrder {
        val rname = name.random()
        val rdept = dept.random()
        val rsize = size.random()
        val rtemp = temp.random()
        val rsyrup = syrup.random()
        val rtype = type.random()
        val rdairy = dairy.random()
        val rmodA = modA.random()
        val rmodB = modB.random()

        val generatedOrder = CoffeeOrder(
            name = rname,
            dept = rdept,
            size = rsize,
            temp = rtemp,
            syrup = rsyrup,
            type = rtype,
            dairy = rdairy,
            modA = rmodA,
            modB = rmodB,
        )

        return generatedOrder
            .validateBlackCoffee()
            .validateBreve()
            .validateEspresso()
            .validateHotChocolate()
            .validateLactoseFree()
            .validateDecaf()
            .validateNoSugar()
            .validateSteamedMilk()
    }

    private fun CoffeeOrder.validateBlackCoffee() =
        if (type == "black") copy(temp = "none", syrup = "none", dairy = "none", special = "black coffee") else this

    private fun CoffeeOrder.validateBreve() = if (type == "breve") copy(dairy = "none") else this

    private fun CoffeeOrder.validateEspresso() =
        if (type == "espresso") copy(size = if (size == "large") "medium" else size, modA = if (modA == "decaf") "none" else modA) else this

    private fun CoffeeOrder.validateHotChocolate() =
        if (type == "hot chocolate" && temp == "iced") copy(temp = "hot") else this

    private fun CoffeeOrder.validateLactoseFree() =
        when {
            modA == "lactose free" && type == "breve" -> copy(modA = "none")
            modA == "lactose free" && dairy in withLactose -> copy(dairy = "almond milk")
            else -> this
        }

    private fun CoffeeOrder.validateDecaf() =
        if (modA == "decaf" && modB == "extra espresso") copy(modB = "none") else this

    private fun CoffeeOrder.validateNoSugar() =
        if (modB == "no sugar") copy(
            syrup = if (syrup in withSugar) "none" else syrup,
            dairy = if (dairy == "condensed milk") "2 percent milk" else dairy
        ) else this

    private fun CoffeeOrder.validateSteamedMilk() =
        if (modB == "steamed milk") copy(
            temp = if (temp == "iced") "hot" else temp,
            dairy = when {
                type == "breve" -> dairy
                dairy == "none" || dairy == "no dairy" -> "2 percent milk"
                else -> dairy
            }
        ) else this

    fun scoreCoffeeGenDetailed(userOrderIn: CoffeeOrderFormData): CoffeeScoreResult {
        var tempscore = 0
        val rows = mutableListOf<CoffeeScoreRow>()
        setRewardType(userOrderIn.reward)
        setDebugEnabled(userOrderIn.debug)

        fun displayValue(value: String): String {
            return if (value.isBlank() || value == "none") "none" else value
        }

        fun addRow(label: String, expected: String, actual: String, isCorrect: Boolean, points: Int, logMessage: String) {
            tempscore += points
            rows.add(CoffeeScoreRow(label, displayValue(expected), displayValue(actual), isCorrect, points, logMessage))
            if (crDebugEnabled) {
                logger.log(logMessage)
            }
        }

        // points array (gain, lose)
        val rsizepts = arrayOf(2, 0)
        val rtemppts = arrayOf(2, -5)
        val rsyruppts = arrayOf(3, -3)
        val rtypepts = arrayOf(4, -4)
        val rdairypts = arrayOf(1, -5)
        val rmodApts = arrayOf(4, -2)
        val rmodBpts = arrayOf(4, -2)
        val adlrulepts = arrayOf(0, -2)

        val expectedSize = when {
            validatedOrder.size != "none" -> validatedOrder.size
            validatedOrder.type == "espresso" -> "medium"
            else -> "large"
        }
        val sizeCorrect = userOrderIn.size == expectedSize
        addRow(
            "Size",
            if (validatedOrder.size == "none") "$expectedSize (implicit)" else expectedSize,
            userOrderIn.size,
            sizeCorrect,
            if (sizeCorrect) rsizepts[0] else rsizepts[1],
            if (sizeCorrect) {
                if (validatedOrder.size == "none") "[OK] implicit size ok" else "[OK] size matches"
            } else {
                "[WARN] size wrong"
            }
        )

        val expectedTemp = if (validatedOrder.temp == "none") "hot" else validatedOrder.temp
        val tempCorrect = userOrderIn.temp == expectedTemp
        addRow(
            "Temperature",
            if (validatedOrder.temp == "none") "$expectedTemp (implicit)" else expectedTemp,
            userOrderIn.temp,
            tempCorrect,
            if (tempCorrect) rtemppts[0] else rtemppts[1],
            if (tempCorrect) {
                if (validatedOrder.temp == "none") "[OK] implicit temp ok" else "[OK] temp matches"
            } else {
                "[WARN] temp wrong"
            }
        )

        val syrupCorrect = validatedOrder.syrup == userOrderIn.syrup
        addRow(
            "Syrup",
            validatedOrder.syrup,
            userOrderIn.syrup,
            syrupCorrect,
            if (syrupCorrect) rsyruppts[0] else rsyruppts[1],
            if (syrupCorrect) "[OK] syrup matches" else "[WARN] syrup wrong"
        )

        val typeCorrect = validatedOrder.type == userOrderIn.type
        addRow(
            "Drink",
            validatedOrder.type,
            userOrderIn.type,
            typeCorrect,
            if (typeCorrect) rtypepts[0] else rtypepts[1],
            if (typeCorrect) "[OK] drink type matches" else "[WARN] drink type wrong"
        )

        val expectedDairy = when {
            validatedOrder.type == "breve" -> "half-and-half"
            validatedOrder.dairy == "no dairy" -> "none"
            else -> validatedOrder.dairy
        }
        val dairyCorrect = userOrderIn.dairy == expectedDairy
        addRow(
            "Dairy",
            expectedDairy,
            userOrderIn.dairy,
            dairyCorrect,
            if (dairyCorrect) rdairypts[0] else rdairypts[1],
            if (dairyCorrect) {
                when {
                    validatedOrder.type == "breve" -> "[OK] special drink matches"
                    validatedOrder.dairy == "no dairy" -> "[OK] no dairy matches"
                    else -> "[OK] dairy matches"
                }
            } else {
                "[WARN] dairy wrong"
            }
        )

        when (validatedOrder.modA) {
            "decaf" -> {
                val correct = userOrderIn.isDecaf
                addRow("Modifier A", "decaf", if (userOrderIn.isDecaf) "decaf" else "not decaf", correct, if (correct) rmodApts[0] else rmodApts[1], if (correct) "[OK] decaf matches" else "[WARN] modA wrong")
            }
            "lactose free" -> {
                val correct = userOrderIn.dairy !in withLactose
                addRow("Modifier A", "lactose free dairy", userOrderIn.dairy, correct, if (correct) rmodApts[0] else rmodApts[1], if (correct) "[OK] lactose free matches" else "[WARN] modA wrong")
            }
            "none" -> {
                val correct = userOrderIn.iceAmount == "regular ice" || (userOrderIn.temp == "hot" && userOrderIn.iceAmount == "no ice")
                addRow("Modifier A", "regular ice or no ice for hot drinks", userOrderIn.iceAmount, correct, if (correct) rmodApts[0] else rmodApts[1], if (correct) "[OK] ice amount matches" else "[WARN] modA wrong")
            }
            else -> {
                val correct = validatedOrder.modA == userOrderIn.iceAmount
                addRow("Modifier A", validatedOrder.modA, userOrderIn.iceAmount, correct, if (correct) rmodApts[0] else rmodApts[1], if (correct) "[OK] ice amount matches" else "[WARN] modA wrong")
            }
        }

        when {
            validatedOrder.modB == "no sugar" -> {
                val correct = userOrderIn.sugar == "no sugar" && userOrderIn.syrup == "none"
                val actual = "${userOrderIn.sugar}, syrup: ${userOrderIn.syrup}"
                addRow("Modifier B", "no sugar, no syrup", actual, correct, if (correct) rmodBpts[0] else rmodBpts[1], if (correct) "[OK] no sugar matches" else "[WARN] modB wrong")
            }
            validatedOrder.modB == userOrderIn.sugar -> {
                addRow("Modifier B", validatedOrder.modB, userOrderIn.sugar, true, rmodBpts[0], "[OK] sugar matches")
            }
            validatedOrder.modB == "extra espresso" && userOrderIn.addEspresso -> {
                addRow("Modifier B", "extra espresso", "extra espresso", true, rmodBpts[0], "[OK] extra espresso matches")
            }
            validatedOrder.modB == "steamed milk" && userOrderIn.steamed -> {
                addRow("Modifier B", "steamed milk", "steamed milk", true, rmodBpts[0], "[OK] steamed matches")
            }
            validatedOrder.modB == "none" && userOrderIn.sugar == "regular sugar" -> {
                addRow("Modifier B", "regular sugar", userOrderIn.sugar, true, rmodBpts[0], "[OK] implicit sugar amount ok")
            }
            validatedOrder.special == "black coffee" && userOrderIn.sugar == "regular sugar" -> {
                addRow("Modifier B", "regular sugar", userOrderIn.sugar, true, rmodBpts[0], "[OK] special rule for black coffee matches")
            }
            else -> {
                addRow("Modifier B", validatedOrder.modB, userOrderIn.sugar, false, rmodBpts[1], "[WARN] modB wrong")
            }
        }

        if (userOrderIn.isDecaf && validatedOrder.modA != "decaf") {
            addRow("Extra decaf", "off", "on", false, rmodApts[1], "[WARN] decaf is wrong")
        }
        if (userOrderIn.steamed && validatedOrder.modB != "steamed milk") {
            addRow("Extra steamed", "off", "on", false, rmodBpts[1], "[WARN] steamed milk is wrong")
        }
        if (userOrderIn.addEspresso && validatedOrder.modB != "extra espresso") {
            addRow("Extra espresso", "off", "on", false, rmodBpts[1], "[WARN] add espresso is wrong")
        }

        if(validatedOrder.type == "breve" && userOrderIn.dairy != "half-and-half") {
            addRow("Breve rule", "half-and-half", userOrderIn.dairy, false, adlrulepts[1], "[WARN] breve needs half-and-half")
        }
        if(validatedOrder.special == "black coffee" && !(userOrderIn.syrup == "none" && userOrderIn.dairy == "none")) {
            addRow("Black coffee rule", "no syrup or dairy", "${userOrderIn.syrup}, ${userOrderIn.dairy}", false, adlrulepts[1], "[WARN] black coffee cannot have dairy or syrup")
        }
        if(validatedOrder.size == "none" && userOrderIn.size != expectedSize) {
            val label = if (validatedOrder.type == "espresso") "Implicit espresso size rule" else "Implicit size rule"
            val message = if (validatedOrder.type == "espresso") "[WARN] implicit espresso size must be medium" else "[WARN] implicit drink size must be large"
            addRow(label, expectedSize, userOrderIn.size, false, adlrulepts[1], message)
        }

        return CoffeeScoreResult(tempscore, rows)
    }

    fun formatOrderData(order: CoffeeOrder): String {
        val intro = "${order.name} from ${order.dept} wants:"
        if (order.special.isNotBlank()) {
            val size = order.size.takeIf { it != "none" } ?: ""
            return "$intro ${size.trim()} ${order.special}, ${order.modA} and ${order.modB}.".replace("  ", " ")
        }
        val baseDrinkParts = listOfNotNull(
            order.size.takeIf { it != "none" },
            order.temp.takeIf { it != "none" },
            order.syrup.takeIf { it != "none" },
            order.type
        )
        val baseDrink = baseDrinkParts.joinToString(" ")
        val additions = listOfNotNull(
            order.dairy.takeIf { it != "none" },
            order.modA.takeIf { it != "none" },
            order.modB.takeIf { it != "none" }
        )
        return when {
            additions.isEmpty() -> "$intro $baseDrink."
            else -> "$intro $baseDrink with ${additions.joinToString(", ")}.".replace("%", "%%")
        }
    }
}
