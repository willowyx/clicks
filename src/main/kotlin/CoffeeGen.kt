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
    // name & dept aren't important
    private val name = listOf("Lane", "Jerry", "Ben", "Alice", "Jay", "Eric", "Lee")
    private val dept = listOf("HR", "Accounting", "Legal", "IT", "Sales", "Marketing")

    private val size = listOf("extra small", "small", "medium", "large", "None")
    private val temp = listOf("iced", "hot", "None")
    private val syrup = listOf("caramel", "dark chocolate", "pecan", "pumpkin spice", "vanilla", "None")
    private val type = listOf("americano", "black", "breve", "cappucino", "cold brew", "espresso", "hot chocolate", "latte", "macchiato")
    private val dairy = listOf("2% milk", "almond milk", "cream", "half-and-half", "oat milk", "skim milk", "soy milk", "condensed milk", "whole milk", "no dairy")
    private val modA = listOf("decaf", "extra ice", "lactose free", "light ice", "no ice", "None")
    private val modB = listOf("add granulated sugar", "add sugar syrup", "extra espresso", "no sugar", "steamed milk", "None")

    private val withSugar = listOf("condensed milk", "dark chocolate", "add granulated sugar", "add sugar syrup", "pecan", "pumpkin spice", "vanilla")
    private val withLactose = arrayOf("2% milk", "breve", "condensed milk", "cream", "half-and-half", "skim milk", "whole milk")

    private val validatedOrder = initializeOrderGen()
    fun getValidatedOrder(): CoffeeOrder {
        return validatedOrder
    }

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
        if (type == "black") copy(temp = "", syrup = "", dairy = "", special = "black coffee") else this

    private fun CoffeeOrder.validateBreve() = if (type == "breve") copy(dairy = "None") else this

    private fun CoffeeOrder.validateEspresso() =
        if (type == "espresso") copy(size = if (size == "large") "medium" else size, modA = if (modA == "decaf") "None" else modA) else this

    private fun CoffeeOrder.validateHotChocolate() =
        if (type == "hot chocolate" && temp == "iced") copy(temp = "hot") else this

    private fun CoffeeOrder.validateLactoseFree() =
        if (modA == "lactose free" && dairy in withLactose) copy(dairy = "almond milk") else this

    private fun CoffeeOrder.validateDecaf() =
        if (modA == "decaf" && modB == "extra espresso") copy(modB = "None") else this

    private fun CoffeeOrder.validateNoSugar() =
        if (modB == "no sugar") copy(
            syrup = if (syrup in withSugar) "None" else syrup,
            dairy = if (dairy == "condensed milk") "2% milk" else dairy
        ) else this

    private fun CoffeeOrder.validateSteamedMilk() =
        if (modB == "steamed milk") copy(
            temp = if (temp == "iced") "hot" else temp,
            dairy = if (dairy == "None") "2% milk" else dairy
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

        // validate size
        if (validatedOrder.size == userOrderIn.size) {
            tempscore += rsizepts[0]
        } else if (validatedOrder.size == "None") {             // if size not provided
            if (validatedOrder.type == "espresso") {            //      if type is espresso
                if (userOrderIn.size == "medium") {             //          size should be Med
                    tempscore += rsizepts[0]
                } else {                                        //      else
                    tempscore += rsizepts[1]                    //          no points
                }
            } else if (userOrderIn.size == "large") {           //      else if type not espresso
                tempscore += rsizepts[1]                        //          no points
            }
        } else {                                                // else
            tempscore += rsizepts[1]                            //      no points
        }

        // validate temp
        if(validatedOrder.temp == userOrderIn.temp) {
            tempscore += rtemppts[0]
        } else if (validatedOrder.temp == "None" && userOrderIn.temp == "hot") {
            tempscore += rtemppts[0]
        } else {
            tempscore += rtemppts[1]
        }

        // validate syrup
        if (validatedOrder.syrup == userOrderIn.syrup) {
            tempscore += rsyruppts[0]
        } else {
            tempscore += rsyruppts[1]
        }

        // validate drink type
        if (validatedOrder.type == userOrderIn.type) {
            tempscore += rtypepts[0]
        } else {
            tempscore += rtypepts[1]
        }

        // validate dairy
        if (validatedOrder.dairy == userOrderIn.dairy) {
            tempscore += rdairypts[0]
        } else if(validatedOrder.type == "breve" && userOrderIn.dairy == "half-and-half") {         // special case
            tempscore += rdairypts[0]
        } else {
            tempscore += rdairypts[1]
        }

        // validate mod part A
        if(validatedOrder.modA == userOrderIn.iceAmount) {                                          // check ice amount
            tempscore += rmodApts[0]
        } else if(validatedOrder.modA == "decaf" && userOrderIn.isDecaf) {                          // check decaf
            tempscore += rmodApts[0]
        } else if(validatedOrder.modA == "lactose free" && userOrderIn.dairy !in (withLactose)) {   // check dairy
            tempscore += rmodApts[0]
        } else if(validatedOrder.modA == "None" && (userOrderIn.iceAmount == "regular ice" || (userOrderIn.temp == "hot" && userOrderIn.iceAmount == "no ice"))) {
            tempscore += rmodApts[0]
        } else {
            tempscore += rmodApts[1]
        }

        // validate mod part B
        if(validatedOrder.modB == userOrderIn.sugar) {
            tempscore += rmodBpts[0]
        } else if(validatedOrder.modB == "extra espresso" && userOrderIn.addEspresso) {
            tempscore += rmodBpts[0]
        } else if(validatedOrder.modB == "steamed milk" && userOrderIn.steamed) {
            tempscore += rmodBpts[0]
        } else if(validatedOrder.modB == "no sugar" && (userOrderIn.syrup !in withSugar)) {
            tempscore += rmodBpts[0]
        } else {
            tempscore += rmodBpts[1]
        }

        // validate extra rules
        if(validatedOrder.type == "breve" && userOrderIn.dairy != "half-and-half") {
            tempscore += adlrulepts[1]
        }
        if(validatedOrder.special == "black coffee" && !(userOrderIn.syrup == "None" && userOrderIn.dairy == "None")) {
            tempscore += adlrulepts[1]
        }
        if(validatedOrder.size == "None" && userOrderIn.size != "large") {
            if(!(userOrderIn.type == "espresso" && userOrderIn.size == "medium")) {
                tempscore += adlrulepts[1]
            }
        }

        return tempscore
    }

    fun formatOrderData(order: CoffeeOrder): String {
        val intro = "${order.name} from ${order.dept} wants:"
        if (order.special.isNotBlank()) {
            val size = order.size.takeIf { it != "None" } ?: ""
            return "$intro ${size.trim()} ${order.special}.".replace("  ", " ")
        }
        val baseDrinkParts = listOfNotNull(
            order.size.takeIf { it != "None" },
            order.temp.takeIf { it != "None" },
            order.syrup.takeIf { it != "None" },
            order.type
        )
        val baseDrink = baseDrinkParts.joinToString(" ")
        val additions = listOfNotNull(
            order.dairy.takeIf { it != "None" },
            order.modA.takeIf { it != "None" },
            order.modB.takeIf { it != "None" }
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