package me.tomasan7.jecnaapi.data.canteen

data class MenuItem(
    val description: String,
    val allergens: List<String>,
    val price: Float,
    val enabled: Boolean,
    val ordered: Boolean,
    val orderURL: String
)
