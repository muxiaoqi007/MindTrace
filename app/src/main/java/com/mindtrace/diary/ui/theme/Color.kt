package com.mindtrace.diary.ui.theme

import androidx.compose.ui.graphics.Color

// Light Theme Colors
val Purple40 = Color(0xFF6750A4)
val PurpleGrey40 = Color(0xFF625B71)
val Pink40 = Color(0xFF7D5260)

val PrimaryLight = Color(0xFF6750A4)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFEADDFF)
val OnPrimaryContainerLight = Color(0xFF21005D)

val SecondaryLight = Color(0xFF625B71)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFE8DEF8)
val OnSecondaryContainerLight = Color(0xFF1D192B)

val TertiaryLight = Color(0xFF7D5260)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFFFD8E4)
val OnTertiaryContainerLight = Color(0xFF31111D)

val ErrorLight = Color(0xFFB3261E)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFF9DEDC)
val OnErrorContainerLight = Color(0xFF410E0B)

val BackgroundLight = Color(0xFFFFFBFE)
val OnBackgroundLight = Color(0xFF1C1B1F)
val SurfaceLight = Color(0xFFFFFBFE)
val OnSurfaceLight = Color(0xFF1C1B1F)
val SurfaceVariantLight = Color(0xFFE7E0EC)
val OnSurfaceVariantLight = Color(0xFF49454F)
val OutlineLight = Color(0xFF79747E)

// Dark Theme Colors
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val PrimaryDark = Color(0xFFD0BCFF)
val OnPrimaryDark = Color(0xFF381E72)
val PrimaryContainerDark = Color(0xFF4F378B)
val OnPrimaryContainerDark = Color(0xFFEADDFF)

val SecondaryDark = Color(0xFFCCC2DC)
val OnSecondaryDark = Color(0xFF332D41)
val SecondaryContainerDark = Color(0xFF4A4458)
val OnSecondaryContainerDark = Color(0xFFE8DEF8)

val TertiaryDark = Color(0xFFEFB8C8)
val OnTertiaryDark = Color(0xFF492532)
val TertiaryContainerDark = Color(0xFF633B48)
val OnTertiaryContainerDark = Color(0xFFFFD8E4)

val ErrorDark = Color(0xFFF2B8B5)
val OnErrorDark = Color(0xFF601410)
val ErrorContainerDark = Color(0xFF8C1D18)
val OnErrorContainerDark = Color(0xFFF9DEDC)

val BackgroundDark = Color(0xFF1C1B1F)
val OnBackgroundDark = Color(0xFFE6E1E5)
val SurfaceDark = Color(0xFF1C1B1F)
val OnSurfaceDark = Color(0xFFE6E1E5)
val SurfaceVariantDark = Color(0xFF49454F)
val OnSurfaceVariantDark = Color(0xFFCAC4D0)
val OutlineDark = Color(0xFF938F99)

// Mood Colors - 5 级心情系统
val MoodGreat = Color(0xFF4CAF50)   // 非常好 - 绿色
val MoodGood = Color(0xFF8BC34A)    // 好 - 浅绿
val MoodOkay = Color(0xFFFFC107)    // 一般 - 黄色
val MoodBad = Color(0xFFFF9800)     // 差 - 橙色
val MoodAwful = Color(0xFFF44336)   // 非常差 - 红色

// 旧版心情颜色（保留兼容）
@Deprecated("使用新的 5 级心情颜色")
val MoodVeryHappy = MoodGreat
@Deprecated("使用新的 5 级心情颜色")
val MoodHappy = MoodGood
@Deprecated("使用新的 5 级心情颜色")
val MoodNeutral = MoodOkay
@Deprecated("使用新的 5 级心情颜色")
val MoodSad = MoodBad
@Deprecated("使用新的 5 级心情颜色")
val MoodVerySad = MoodAwful
@Deprecated("使用新的 5 级心情颜色")
val MoodAngry = Color(0xFFE91E63)
@Deprecated("使用新的 5 级心情颜色")
val MoodAnxious = Color(0xFF9C27B0)
@Deprecated("使用新的 5 级心情颜色")
val MoodExcited = Color(0xFF2196F3)

// Priority Colors
val PriorityHigh = Color(0xFFF44336)
val PriorityMedium = Color(0xFFFF9800)
val PriorityLow = Color(0xFF4CAF50)
