package com.shafeeq.shopee.utils


const val ITEM = 0

@Suppress("unused")
const val SECT = 1


data class ShopItem(
    var name: String = "",
    var manglish: String = "",
    var type: Int = ITEM,
    var id: String = "",
    var checked: Boolean = false,
    var purchase: Boolean = true
) {
    override fun toString(): String {
        return name
    }

    override fun equals(other: Any?): Boolean {
        return (other as ShopItem).name == name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

}