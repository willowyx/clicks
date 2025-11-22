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

data class CoffeeOrderFormData ( // form data somewhat translated into coffee order
    val size: String,
    val temp: String,
    val syrup: String,
    val type: String,
    val dairy: String,
    val iceAmount: String,
    val sugar: String,
    val isDecaf: Boolean,
    val addEspresso: Boolean,
    val steamed: Boolean
)

class CoffeeGen {
    lateinit var logger: GameLogger

    // name & dept aren't important
    private val name = listOf("Lane", "Jerry", "Ben", "Alice", "Jay", "Eric", "Lee")
    private val dept = listOf("HR", "Accounting", "Legal", "IT", "Sales", "Marketing")

    private val size = listOf("extra small", "small", "medium", "large", "none")
    private val temp = listOf("iced", "hot", "none")
    private val syrup = listOf("caramel", "dark chocolate", "pecan", "pumpkin spice", "vanilla", "none")
    private val type = listOf("americano", "black", "breve", "cappucino", "cold brew", "espresso", "hot chocolate", "latte", "macchiato")
    private val dairy = listOf("2% milk", "almond milk", "cream", "half-and-half", "oat milk", "skim milk", "soy milk", "condensed milk", "whole milk", "no dairy")
    private val modA = listOf("decaf", "extra ice", "lactose free", "light ice", "no ice", "none")
    private val modB = listOf("add granulated sugar", "add sugar syrup", "extra espresso", "no sugar", "steamed milk", "none")

    private val withSugar = listOf("condensed milk", "dark chocolate", "add granulated sugar", "add sugar syrup", "pecan", "pumpkin spice", "vanilla")
    private val withLactose = arrayOf("2% milk", "breve", "condensed milk", "cream", "half-and-half", "skim milk", "whole milk")

    private var validatedOrder = initializeOrderGen()
    fun getValidatedOrder(): CoffeeOrder {
        return validatedOrder
    }
    fun setValidatedOrder(order: CoffeeOrder) {
        validatedOrder = order
    }

    fun initializeOrderGen(): CoffeeOrder { // todo: doesn't validate irrelevant choices
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
        if (modA == "lactose free" && dairy in withLactose) copy(dairy = "almond milk") else this

    private fun CoffeeOrder.validateDecaf() =
        if (modA == "decaf" && modB == "extra espresso") copy(modB = "none") else this

    private fun CoffeeOrder.validateNoSugar() =
        if (modB == "no sugar") copy(
            syrup = if (syrup in withSugar) "none" else syrup,
            dairy = if (dairy == "condensed milk") "2% milk" else dairy
        ) else this

    private fun CoffeeOrder.validateSteamedMilk() =
        if (modB == "steamed milk") copy(
            temp = if (temp == "iced") "hot" else temp,
            dairy = if (dairy == "none") "2% milk" else dairy
        ) else this

    fun scoreCoffeeGen(userOrderIn: CoffeeOrderFormData): Int {
        var tempscore = 0

        // points array (gain, lose)
        val rsizepts = arrayOf(2, 0)
        val rtemppts = arrayOf(2, -5)
        val rsyruppts = arrayOf(3, -3)
        val rtypepts = arrayOf(4, -4)
        val rdairypts = arrayOf(1, -5)
        val rmodApts = arrayOf(4, -2)
        val rmodBpts = arrayOf(4, -2)
        val adlrulepts = arrayOf(0, -2)

        // !! Validate relevant order details !!
        // validate size
        if (validatedOrder.size == userOrderIn.size) {
            tempscore += rsizepts[0]
            logger.log("[OK] size matches")
        } else if(validatedOrder.size == "none" && userOrderIn.size == "large") {
            tempscore += rsizepts[0]
            logger.log("[OK] implicit size ok")
        } else if (validatedOrder.size == "none" && validatedOrder.type == "espresso" && userOrderIn.size == "medium") {
            tempscore += rsizepts[0]
            logger.log("[OK] implicit espresso size ok")
        } else {
            tempscore += rsizepts[1]
            logger.log("[WARN] size wrong")
        }

        // validate temp
        if(validatedOrder.temp == userOrderIn.temp) {
            tempscore += rtemppts[0]
            logger.log("[OK] temp matches")
        } else if (validatedOrder.temp == "none" && userOrderIn.temp == "hot") {
            tempscore += rtemppts[0]
            logger.log("[OK] implicit temp ok")
        } else {
            tempscore += rtemppts[1]
            logger.log("[WARN] temp wrong")
        }

        // validate syrup
        if (validatedOrder.syrup == userOrderIn.syrup) {
            tempscore += rsyruppts[0]
            logger.log("[OK] syrup matches")
        } else {
            tempscore += rsyruppts[1]
            logger.log("[WARN] syrup wrong")
        }

        // validate drink type
        if (validatedOrder.type == userOrderIn.type) {
            tempscore += rtypepts[0]
            logger.log("[OK] drink type matches")
        } else {
            tempscore += rtypepts[1]
            logger.log("[WARN] drink type wrong")
        }

        // validate dairy
        if (validatedOrder.dairy == userOrderIn.dairy) {
            tempscore += rdairypts[0]
            logger.log("[OK] dairy matches")
        } else if(validatedOrder.type == "breve" && userOrderIn.dairy == "half-and-half") {         // special case
            tempscore += rdairypts[0]
            logger.log("[OK] special drink matches")
        } else if(validatedOrder.dairy == "no dairy" && userOrderIn.dairy == "none") {
            tempscore += rdairypts[0]
            logger.log("[OK] no dairy matches")
        } else {
            tempscore += rdairypts[1]
            logger.log("[WARN] dairy wrong")
        }

        // validate mod part A
        if(validatedOrder.modA == userOrderIn.iceAmount) {                                          // check ice amount
            tempscore += rmodApts[0]
            logger.log("[OK] ice amount matches")
        } else if(validatedOrder.modA == "decaf" && userOrderIn.isDecaf) {                          // check decaf
            tempscore += rmodApts[0]
            logger.log("[OK] decaf matches")
        } else if(validatedOrder.modA == "lactose free" && userOrderIn.dairy !in (withLactose)) {   // check dairy
            tempscore += rmodApts[0]
            logger.log("[OK] lactose free matches")
        } else if(validatedOrder.modA == "none" && (userOrderIn.iceAmount == "regular ice" || (userOrderIn.temp == "hot" && userOrderIn.iceAmount == "no ice"))) {
            tempscore += rmodApts[0]
            logger.log("[OK] ice amount matches")
        } else {
            tempscore += rmodApts[1]
            logger.log("[WARN] modA wrong")
        }

        // validate mod part B
        if(validatedOrder.modB == userOrderIn.sugar) {
            tempscore += rmodBpts[0]
            logger.log("[OK] sugar matches")
        } else if(validatedOrder.modB == "extra espresso" && userOrderIn.addEspresso) {
            tempscore += rmodBpts[0]
            logger.log("[OK] extra espresso matches")
        } else if(validatedOrder.modB == "steamed milk" && userOrderIn.steamed) {
            tempscore += rmodBpts[0]
            logger.log("[OK] steamed matches")
        } else if(validatedOrder.modB == "no sugar" && (userOrderIn.syrup == "none")) {
            tempscore += rmodBpts[0]
            logger.log("[OK] no sugar matches")
        } else if(validatedOrder.modB in listOf("add granulated sugar", "add sugar syrup", "no sugar") && userOrderIn.sugar == "regular sugar") {
            if(validatedOrder.special == "black coffee") {
                tempscore += rmodBpts[0]
                logger.log("[OK] special rule for black coffee matches")
            } else {
                tempscore += rmodBpts[1]
                logger.log("[WARN] regular sugar is wrong")
            }
        } else {
            tempscore += rmodBpts[1]
            logger.log("[WARN] modB wrong")
        }

        // !! validate other order details !!
        // validate form checkboxes
        if(userOrderIn.isDecaf) {      // if selected decaf unnecessarily
            if (validatedOrder.modA != "decaf") {
                tempscore += rmodApts[1]
                logger.log("[WARN] decaf is wrong")
            }
        }
        if(userOrderIn.steamed) {       // if selected steamed milk unnecessarily
            if(validatedOrder.modB != "steamed milk") {
                tempscore += rmodBpts[1]
                logger.log("[WARN] steamed milk is wrong")
            }
        } else if(userOrderIn.addEspresso) {  // if added espresso unnecessarily
            if (validatedOrder.modB != "extra espresso") {
                tempscore += rmodBpts[1]
                logger.log("[WARN] add espresso is wrong")
            }
        }

        // validate extra rules
        if(validatedOrder.type == "breve" && userOrderIn.dairy != "half-and-half") {
            tempscore += adlrulepts[1]
            logger.log("[WARN] breve needs half-and-half")
        }
        if(validatedOrder.special == "black coffee" && !(userOrderIn.syrup == "none" && userOrderIn.dairy == "none")) {
            tempscore += adlrulepts[1]
            logger.log("[WARN] black coffee cannot have dairy or syrup")
        }
        if(validatedOrder.size == "none" && userOrderIn.size != "large") {
            if((userOrderIn.type == "espresso" && userOrderIn.size != "medium")) {
                tempscore += adlrulepts[1]
                logger.log("[WARN] implicit espresso size must be medium")
            } else {
                tempscore += adlrulepts[1]
                logger.log("[WARN] implicit drink size must be large")
            }
        }

        return tempscore
    }

    fun formatOrderData(order: CoffeeOrder): String {
        val intro = "${order.name} from ${order.dept} wants:"
        if (order.special.isNotBlank()) {
            val size = order.size.takeIf { it != "none" } ?: ""
            return "$intro ${size.trim()} ${order.special}.".replace("  ", " ")
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
fun main() {
    val coffeegen = CoffeeGen()
    println(coffeegen.formatOrderData(coffeegen.getValidatedOrder()))
    println(coffeegen.getValidatedOrder().toString())
}