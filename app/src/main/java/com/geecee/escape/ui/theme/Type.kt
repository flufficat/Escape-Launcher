package com.geecee.escape.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.geecee.escape.R

val Jost = FontFamily(
    Font(R.font.jost)
)

val Josefin = FontFamily(
    Font(R.font.josefinnormal)
)

val JosefinBold = FontFamily(
    Font(R.font.josefinsemibold)
)

val Lora = FontFamily(
    Font(R.font.lora)
)

val NotoJP = FontFamily(
    Font(R.font.notojpnormal)
)

val NotoJPBold = FontFamily(
    Font(R.font.notojpsemibold)
)

// Set of Material typography styles to start with
val JostTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = Jost,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Jost,
        fontWeight = FontWeight.SemiBold,
        fontSize = 48.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

val JosefinTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = Josefin,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleMedium = TextStyle(
        fontFamily = JosefinBold,
        fontWeight = FontWeight.SemiBold,
        fontSize = 48.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

val LoraTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = Lora,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Lora,
        fontWeight = FontWeight.SemiBold,
        fontSize = 48.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

val JPTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = NotoJP,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleMedium = TextStyle(
        fontFamily = NotoJPBold,
        fontWeight = FontWeight.SemiBold,
        fontSize = 48.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)