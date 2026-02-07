package com.skul9x.danhvan.utils

object SyllableTokenizer {
    // Regex for Vietnamese characters (including tones)
    private val VIETNAMESE_REGEX = Regex("^[a-zA-ZàáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴÈÉẸẺẼÊỀẾỆỂỄÌÍỊỈĨÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠÙÚỤỦŨƯỪỨỰỬỮỲÝỴỶỸĐ\\s]+$")

    fun tokenize(text: String): List<String> {
        return text.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
    }

    fun isValidVietnamese(text: String): Boolean {
        return VIETNAMESE_REGEX.matches(text)
    }
}
