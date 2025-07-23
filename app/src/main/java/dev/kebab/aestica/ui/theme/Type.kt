package dev.kebab.aestica.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.kebab.aestica.R

// Instagram Sans font ailesi tanımı
val InstagramSans = FontFamily(
    Font(R.font.instagram_sans_light, FontWeight.Light),
    Font(R.font.instagram_sans, FontWeight.Normal),
    Font(R.font.instagram_sans_medium, FontWeight.Medium),
    Font(R.font.instagram_sans_bold, FontWeight.Bold),
    Font(R.font.instagram_sans_headline, FontWeight.SemiBold) // varsa SemiBold yerine Headline gibi özel kullanabilirsin
)

// Material3 Typography yapısı
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = InstagramSans,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = InstagramSans,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = InstagramSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = InstagramSans,
        fontWeight = FontWeight.Light,
        fontSize = 12.sp
    )
)
