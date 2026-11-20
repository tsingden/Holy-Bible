package com.example.util

object DevanagariNumberUtils {
    private val digits = charArrayOf('०', '१', '२', '३', '४', '५', '६', '७', '८', '९')

    fun toDevanagari(number: Int): String {
        return number.toString().map { ch ->
            if (ch in '0'..'9') digits[ch - '0'] else ch
        }.joinToString("")
    }
}
