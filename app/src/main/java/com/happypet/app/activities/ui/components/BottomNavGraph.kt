package com.happypet.app.activities.ui.components

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.happypet.app.activities.ui.onboarding.presentation.OnboardingJourneyScreen
import com.happypet.app.activities.ui.screens.ActivitiesScreen
import com.happypet.app.activities.ui.screens.AppointmentsScreen
import com.happypet.app.activities.ui.screens.HomeScreen
import com.happypet.app.activities.ui.screens.MedicationsScreen

@Composable
fun BottomNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = BottomNavItem.Onboarding.route) {
        composable(BottomNavItem.Onboarding.route) { OnboardingJourneyScreen(navController, onFinished = {}) }
        composable(BottomNavItem.Home.route) { HomeScreen() }
        composable(BottomNavItem.Appointments.route) { AppointmentsScreen() }
        composable(BottomNavItem.Medications.route) { MedicationsScreen() }
        composable(BottomNavItem.Activities.route) { ActivitiesScreen() }
    }
}
