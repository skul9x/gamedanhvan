package com.skul9x.danhvan.data

enum class ItemType {
    THEME, EFFECT, STICKER
}

data class ShopItem(
    val id: String,
    val name: String,
    val cost: Int,
    val color1: Long, // Start color for gradient (or background for sticker card)
    val color2: Long,  // End color for gradient
    val type: ItemType = ItemType.THEME,
    val resourceId: Int = 0, // For stickers
    val imageUri: String? = null // For custom stickers from storage
)

object ShopData {
    val items = listOf(
        // Themes
        ShopItem("theme_default", "Mặc định", 0, 0xFFE3F2FD, 0xFFE0F2F1, ItemType.THEME),
        ShopItem("theme_pink", "Hồng Mộng Mơ", 10, 0xFFFCE4EC, 0xFFF8BBD0, ItemType.THEME),
        ShopItem("theme_yellow", "Vàng Nắng Mai", 20, 0xFFFFFDE7, 0xFFFFF59D, ItemType.THEME),
        ShopItem("theme_dark", "Đêm Huyền Bí", 30, 0xFF263238, 0xFF37474F, ItemType.THEME),
        ShopItem("theme_forest", "Rừng Xanh", 40, 0xFFE8F5E9, 0xFFC8E6C9, ItemType.THEME),
        
        // Effects
        ShopItem("effect_sparkle", "Lấp Lánh", 30, 0xFFFFF176, 0xFFFFD54F, ItemType.EFFECT),
        ShopItem("effect_bubble", "Bong Bóng", 30, 0xFF81D4FA, 0xFF29B6F6, ItemType.EFFECT),
        ShopItem("effect_heart", "Trái Tim", 30, 0xFFF48FB1, 0xFFF06292, ItemType.EFFECT),
        
        // Stickers
        ShopItem("sticker_lion", "Sư Tử Dũng Mãnh", 25, 0xFFFFE0B2, 0xFFFFCC80, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_lion),
        ShopItem("sticker_elephant", "Voi Con Cute", 25, 0xFFBBDEFB, 0xFF90CAF9, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_elephant),
        ShopItem("sticker_monkey", "Khỉ Con Leo Trèo", 25, 0xFFD7CCC8, 0xFFBCAAA4, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_monkey),
        ShopItem("sticker_panda", "Gấu Trúc", 25, 0xFFF5F5F5, 0xFFE0E0E0, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_panda),
        ShopItem("sticker_giraffe", "Hươu Cao Cổ", 25, 0xFFFFF9C4, 0xFFFFF59D, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_giraffe),
        ShopItem("sticker_zebra", "Ngựa Vằn", 25, 0xFFEEEEEE, 0xFFE0E0E0, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_zebra),
        ShopItem("sticker_tiger", "Hổ Con", 25, 0xFFFFCC80, 0xFFFFB74D, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_tiger),
        ShopItem("sticker_rabbit", "Thỏ Con", 25, 0xFFF8BBD0, 0xFFF48FB1, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_rabbit),
        ShopItem("sticker_cat", "Mèo Con", 25, 0xFFE1BEE7, 0xFFCE93D8, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_cat),
        ShopItem("sticker_dog", "Cún Con", 25, 0xFFD7CCC8, 0xFFBCAAA4, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_dog),
        ShopItem("sticker_penguin", "Chim Cánh Cụt", 25, 0xFFB3E5FC, 0xFF81D4FA, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_penguin),
        ShopItem("sticker_koala", "Gấu Túi", 25, 0xFFCFD8DC, 0xFFB0BEC5, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_koala),
        ShopItem("sticker_fox", "Cáo Nhỏ", 25, 0xFFFFAB91, 0xFFFF8A65, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_fox),
        ShopItem("sticker_owl", "Cú Mèo", 25, 0xFFD1C4E9, 0xFFB39DDB, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_owl),
        
        // Numberblocks
        ShopItem("sticker_nb_1", "Numberblock 1", 25, 0xFFFFCDD2, 0xFFEF9A9A, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_nb_1),
        ShopItem("sticker_nb_2", "Numberblock 2", 25, 0xFFFFE0B2, 0xFFFFCC80, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_nb_2),
        ShopItem("sticker_nb_3", "Numberblock 3", 25, 0xFFFFF9C4, 0xFFFFF59D, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_nb_3),
        ShopItem("sticker_nb_4", "Numberblock 4", 25, 0xFFC8E6C9, 0xFFA5D6A7, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_nb_4),
        ShopItem("sticker_nb_5", "Numberblock 5", 25, 0xFF81D4FA, 0xFF4FC3F7, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_nb_5),
        ShopItem("sticker_nb_6", "Numberblock 6", 25, 0xFFCE93D8, 0xFFBA68C8, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_nb_6),
        ShopItem("sticker_nb_7", "Numberblock 7", 25, 0xFFFFF59D, 0xFFFFF176, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_nb_7),
        ShopItem("sticker_nb_8", "Numberblock 8", 25, 0xFFF48FB1, 0xFFF06292, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_nb_8),
        ShopItem("sticker_nb_9", "Numberblock 9", 25, 0xFFB0BEC5, 0xFF90A4AE, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_nb_9),
        ShopItem("sticker_nb_10", "Numberblock 10", 25, 0xFFFFFFFF, 0xFFE0E0E0, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_nb_10),
        ShopItem("sticker_nb_11", "Numberblock 11", 25, 0xFFFFEBEE, 0xFFEF9A9A, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_nb_11),
        ShopItem("sticker_nb_12", "Numberblock 12", 25, 0xFFF3E5F5, 0xFFCE93D8, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_nb_12),
        ShopItem("sticker_nb_13", "Numberblock 13", 25, 0xFFFFF3E0, 0xFFFFCC80, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_nb_13),
        ShopItem("sticker_nb_14", "Numberblock 14", 25, 0xFFEDE7F6, 0xFFB39DDB, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_nb_14),
        ShopItem("sticker_nb_15", "Numberblock 15", 25, 0xFFFCE4EC, 0xFFF48FB1, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_nb_15),
        ShopItem("sticker_nb_16", "Numberblock 16", 25, 0xFFFFF8E1, 0xFFFFE082, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_nb_16),
        ShopItem("sticker_nb_17", "Numberblock 17", 25, 0xFFE0F2F1, 0xFF80CBC4, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_nb_17),
        ShopItem("sticker_nb_18", "Numberblock 18", 25, 0xFFFFFDE7, 0xFFFFF59D, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_nb_18),
        ShopItem("sticker_nb_19", "Numberblock 19", 25, 0xFFEFEBE9, 0xFFBCAAA4, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_nb_19),
        ShopItem("sticker_nb_20", "Numberblock 20", 25, 0xFFE3F2FD, 0xFF90CAF9, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_nb_20),
        ShopItem("sticker_nb_21", "Numberblock 21", 25, 0xFFFFCDD2, 0xFFE57373, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_nb_21),
        ShopItem("sticker_nb_22", "Numberblock 22", 25, 0xFFFFE0B2, 0xFFFFB74D, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_nb_22),
        ShopItem("sticker_nb_23", "Numberblock 23", 25, 0xFFFFF9C4, 0xFFFFF176, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_nb_23),
        ShopItem("sticker_nb_24", "Numberblock 24", 25, 0xFFC8E6C9, 0xFF81C784, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_nb_24),
        ShopItem("sticker_nb_25", "Numberblock 25", 25, 0xFFB3E5FC, 0xFF4FC3F7, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_nb_25),
        ShopItem("sticker_nb_26", "Numberblock 26", 25, 0xFFE1BEE7, 0xFFBA68C8, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_nb_26),
        ShopItem("sticker_nb_27", "Numberblock 27", 25, 0xFFD7CCC8, 0xFFA1887F, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_nb_27),
        ShopItem("sticker_nb_28", "Numberblock 28", 25, 0xFFCFD8DC, 0xFF90A4AE, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_nb_28),
        ShopItem("sticker_nb_29", "Numberblock 29", 25, 0xFFFFCCBC, 0xFFFF8A65, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_nb_29),
        ShopItem("sticker_nb_30", "Numberblock 30", 25, 0xFFE0F7FA, 0xFF4DD0E1, ItemType.STICKER, com.skul9x.danhvan.R.drawable.sticker_nb_30)
    )
}
