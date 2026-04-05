package com.rivavafi.universal.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.unit.sp
import com.rivavafi.universal.R

// Approximating SamsungOne with standard available sans-serif fonts
// if custom font file is not available, but setting up the structure.
val SamsungOneFontFamily = FontFamily.SansSerif

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = SamsungOneFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp, // Title
        lineHeight = 42.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = SamsungOneFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = SamsungOneFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp, // Section Title
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = SamsungOneFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp, // Card Title
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = SamsungOneFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp, // Body text
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = SamsungOneFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp, // Secondary text
        lineHeight = 16.sp,
        letterSpacing = 0.1.sp
    )
)
