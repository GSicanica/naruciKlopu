package com.appbosna.shared.domain

import com.appbosna.shared.fonts.Resources
import org.jetbrains.compose.resources.DrawableResource

enum class Country(
    val dialCode: Int,
    val code: String,
    val flag: DrawableResource
) {
    Bosnia(
        dialCode = 387,
        code = "BH",
        flag = Resources.Flag.Bosnia
    ),
    India(
        dialCode = 91,
        code = "IN",
        flag = Resources.Flag.India
    ),
    Usa(
        dialCode = 1,
        code = "US",
        flag = Resources.Flag.Usa
    )
}