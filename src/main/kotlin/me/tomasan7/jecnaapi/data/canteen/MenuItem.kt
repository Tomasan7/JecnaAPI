package me.tomasan7.jecnaapi.data.canteen

data class MenuItem(
    val description: ItemDescription?,
    val allergens: List<String>? = null,
    val price: Float,
    val enabled: Boolean,
    val ordered: Boolean,
    var orderURL: String
)