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
    private val syrup = listOf("vanilla", "pumpkin spice", "dark chocolate", "pecan", "None")
    private val type = listOf("latte", "espresso", "macchiato", "cappucino", "americano", "breve", "cold brew", "black", "hot chocolate")
    private val dairy = listOf("2% milk", "skim milk", "whole milk", "oat milk", "almond milk", "soy milk", "cream", "half-and-half", "condensed milk", "no dairy")
    private val modA = listOf("decaf", "no ice", "light ice", "extra ice", "lactose free", "None")
    private val modB = listOf("extra sugar syrup", "extra sugar", "no sugar", "extra espresso", "steamed milk", "None")

    private val withSugar = listOf("vanilla", "pumpkin spice", "dark chocolate", "pecan", "condensed milk", "extra sugar syrup", "extra sugar")
    private val withLactose = arrayOf("breve", "2% milk", "skim milk", "whole milk", "cream", "half-and-half", "condensed milk")

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

    fun scoreCoffeeGen(userOrderIn: CoffeeOrderFormData): String {
        // points array (gain, lose)
        val rsize_pts = arrayOf(2, 0)
        val rtemp_pts = arrayOf(2, 5)
        val rsyrup_pts = arrayOf(3, 3)
        val rtype_pts = arrayOf(4, 4)
        val rdairy_pts = arrayOf(1, 5)
        val rmodA_pts = arrayOf(4, 2)
        val rmodB_pts = arrayOf(4, 2)
        val adlrule_pts = arrayOf(0, 2)



        // todo: if size omitted, should use largest: L except type: espresso should be M

        return TODO("return score")
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