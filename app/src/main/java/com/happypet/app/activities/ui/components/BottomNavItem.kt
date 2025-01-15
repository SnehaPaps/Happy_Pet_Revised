package com.happypet.app.activities.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.happypet.app.R

sealed class BottomNavItem(
    val route: String,
    @DrawableRes val iconRes: Int,
    val label: String
) {
    object Onboarding : BottomNavItem("onboarding", R.drawable.ic_pet, "Onboarding")
    object Home : BottomNavItem("home", R.drawable.ic_pet, "Home")
    object Appointments : BottomNavItem("appointments", R.drawable.ic_calendar, "Appointments")
    object Medications : BottomNavItem("medications", R.drawable.ic_medicine, "Medications")
    object Activities : BottomNavItem("activities", R.drawable.ic_activity, "Activities")

    @Composable
    fun iconPainter(): Painter = painterResource(id = iconRes)
}
