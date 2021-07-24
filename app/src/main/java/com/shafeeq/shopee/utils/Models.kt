package com.shafeeq.shopee.utils


const val ITEM = 0
const val SECT = 1


data class ShopItem(
    var malayalam: String = "",
    var manglish: String = "",
    var name: String? = null,
    var type: Int = ITEM,
    var id: String = "",
    var checked: Boolean = false,
    var purchase: Boolean = true,
    var quantity: String = "",
    var category: String? = "1010"
) {
    override fun toString(): String {
        return name ?: if(malayalam.isNotEmpty()) malayalam else manglish
    }

    override fun equals(other: Any?): Boolean {
        other as ShopItem
        return other.getKey() == getKey()
    }

    override fun hashCode(): Int {
        return getKey().hashCode()
    }

    fun getKey(): String {
        return if(malayalam.isNotEmpty())
            malayalam.filter { !it.isWhitespace() }
        else manglish.filter { !it.isWhitespace() }
    }

    fun swapContent() {
        val tmp = name ?: malayalam
        malayalam = manglish
        manglish = tmp
        name = null
    }
}

data class CategoryItem(
    var name: String,
    var id: String?
) {
    override fun toString(): String {
        return name
    }
}
