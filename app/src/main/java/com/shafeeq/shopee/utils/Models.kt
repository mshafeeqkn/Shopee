package com.shafeeq.shopee.utils

enum class Category {
    CAT_VEG,
    CAT_FRUITS
}

data class ShopItem(
    var id: String = "",
    val name: String = "",
    var checked: Boolean = false,
    val category: Category = Category.CAT_VEG,
) {
    constructor(): this("")

    override fun toString(): String {
        return name
    }
}