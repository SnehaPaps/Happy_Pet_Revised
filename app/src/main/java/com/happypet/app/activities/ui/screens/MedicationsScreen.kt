package com.happypet.app.activities.ui.screens

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.widget.TimePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.happypet.app.R
import com.happypet.app.base.ReminderReceiver
import java.util.*

@Composable
fun MedicationsScreen() {
    var showVaccineDialog by remember { mutableStateOf(false) }
    var showMedicineDialog by remember { mutableStateOf(false) }

    val vaccinations = remember { mutableStateListOf<String>() }
    val medications = remember { mutableStateListOf<String>() }

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val database = FirebaseDatabase.getInstance()
    val petsRef = userId?.let { database.getReference("users/$it/pets") }
    val vaccinesRef = userId?.let { database.getReference("users/$it/vaccines") }
    val medsRef = userId?.let { database.getReference("users/$it/meds") }

    var petNames by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(userId) {
        medsRef?.get()?.addOnSuccessListener { snapshot ->
            medications.clear() // Clear the existing list before adding new data
            snapshot.children.forEach { data ->
                val medicineName = data.key ?: ""
                val petName = data.child("petName").getValue(String::class.java) ?: ""
                medications.add("$medicineName for $petName")
            }
        }
    }

    // LaunchedEffect to fetch vaccinations
    LaunchedEffect(userId) {
        vaccinesRef?.get()?.addOnSuccessListener { snapshot ->
            vaccinations.clear() // Clear the list before adding new data
            snapshot.children.forEach { data ->
                val vaccineName = data.key ?: ""
                val petName = data.child("petName").getValue(String::class.java) ?: ""
                vaccinations.add("$vaccineName for $petName") // Add the vaccine to the list
            }
        }
    }



    LaunchedEffect(userId) {
        petsRef?.get()?.addOnSuccessListener { snapshot ->
            val pets = snapshot.children.map { it.child("name").getValue(String::class.java) }.filterNotNull()
            petNames = pets
        }
    }

    Scaffold(
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FloatingActionButton(
                    onClick = { showVaccineDialog = true }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_vaccine), // Replace with your drawable resource ID
                        contentDescription = "Select"
                    )
                }
                FloatingActionButton(
                    onClick = { showMedicineDialog = true }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_medicine), // Replace with your drawable resource ID
                        contentDescription = "Select"
                    )
                }
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Vaccinations",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(vaccinations) { vaccine ->
                        val (vaccineName, petName) = vaccine.split(" for ")
                        Card(
                            modifier = Modifier
                                .size(120.dp)
                                .padding(8.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF8E44AD))
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_vaccine),
                                    modifier = Modifier.size(18.dp),
                                    contentDescription = "Pet",
                                    tint = Color.White

                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Divider()
                                Text(
                                    text = vaccineName,
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "$petName",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Medicines",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn {
                    items(medications) { medicine ->
                        val (medicineName, petName) = medicine.split(" for ")
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 16.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF3498DB))
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically // Align items vertically
                            ) {
                                // Add icon on the left
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .background(Color(0xFF2980B9), shape = CircleShape), // Circle background for icon
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_medicine),
                                        modifier = Modifier.size(24.dp),
                                        contentDescription = "Pet",
                                        tint = Color.White

                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp)) // Space between icon and text

                                // Column for text
                                Column {
                                    Text(
                                        text = medicineName,
                                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                                        maxLines = 1, // Limit to one line for neatness
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "for $petName",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                                        textAlign = TextAlign.Start
                                    )
                                }
                            }
                        }
                    }
                }

            }
        }
    )

    // Vaccine Dialog
    if (showVaccineDialog) {
        VaccineDialog(
            petNames = petNames,
            onDismiss = { showVaccineDialog = false },
// In VaccineDialog
            onSave = { petName, vaccine ->
                vaccinations.add("$vaccine for $petName") // Update the list to show the new vaccine
                // Save vaccine to Firebase
                userId?.let {
                    vaccinesRef?.child(vaccine)?.setValue(
                        mapOf("petName" to petName, "vaccineDate" to vaccine)
                    )
                }
                showVaccineDialog = false
            }

        )
    }

    // Medicine Dialog
    if (showMedicineDialog) {
        MedicineDialog(
            petNames = petNames,
            onDismiss = { showMedicineDialog = false },
// In MedicineDialog
            onSave = { petName, medicineName, frequency ->
                medications.add("$medicineName for $petName (Frequency: $frequency)")
                userId?.let {
                    medsRef?.child(medicineName)?.setValue(
                        mapOf("petName" to petName, "dosage" to medicineName, "frequency" to frequency)
                    )
                }

                // Set reminders based on frequency
                if (frequency == "Daily") {
                    // Set daily reminder
                    // Example logic for daily reminder
                } else if (frequency == "Alternate Days") {
                    // Set alternate day reminder
                } else if (frequency == "Select Day") {
                    // Handle select day reminder
                }

                showMedicineDialog = false
            }


        )
    }
}



@Composable
fun VaccineDialog(petNames: List<String>, onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var vaccineName by remember { mutableStateOf("") }
    var vaccineDate by remember { mutableStateOf("") }
    var selectedPet by remember { mutableStateOf(petNames.firstOrNull()) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            vaccineDate = String.format("%02d-%02d-%d", dayOfMonth, month + 1, year)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Vaccination") },
        text = {
            Column {
                TextField(
                    value = vaccineName,
                    onValueChange = { vaccineName = it },
                    label = { Text("Vaccine Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Use DropdownMenuBox here
                DropdownMenuBox(
                    label = "Select Pet",
                    options = petNames,
                    selectedValue = selectedPet ?: "",
                    onValueChange = { selectedPet = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(onClick = { datePickerDialog.show() }) {
                    Text(text = if (vaccineDate.isNotEmpty()) vaccineDate else "Select Date")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedPet?.let {
                        onSave(it, vaccineName)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun MedicineDialog(petNames: List<String>, onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var medicineName by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("Daily") }
    var setReminder by remember { mutableStateOf(false) }
    var selectedPet by remember { mutableStateOf(petNames.firstOrNull()) }
    var reminderTime by remember { mutableStateOf("") }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val timePickerDialog = TimePickerDialog(
        context, { _: TimePicker, hourOfDay: Int, minute: Int ->
            reminderTime = String.format("%02d:%02d", hourOfDay, minute)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Medicine") },
        text = {
            Column {
                TextField(
                    value = medicineName,
                    onValueChange = { medicineName = it },
                    label = { Text("Medicine Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosage") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Frequency Dropdown
                DropdownMenuBox(
                    label = "Select Frequency",
                    options = listOf("Daily", "Alternate Days", "Sundays", "Mondays", "Tuesdays", "Wednesdays", "Thursdays", "Fridays", "Saturdays"),
                    selectedValue = frequency,
                    onValueChange = { frequency = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Use DropdownMenuBox for selecting pet
                DropdownMenuBox(
                    label = "Select Pet",
                    options = petNames,
                    selectedValue = selectedPet ?: "",
                    onValueChange = { selectedPet = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Reminder time
                if (setReminder) {
                    OutlinedButton(onClick = { timePickerDialog.show() }) {
                        Text(text = if (reminderTime.isNotEmpty()) reminderTime else "Set Reminder Time")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedPet?.let {
                        onSave(it, medicineName, frequency)
                        setReminderForReminder(frequency, reminderTime, context)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

fun setReminderForReminder(frequency: String, reminderTime: String, context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, ReminderReceiver::class.java)
    intent.putExtra("medicineName", frequency) // Pass the frequency for the reminder

    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

    val calendar = Calendar.getInstance()
    val timeParts = reminderTime.split(":")
    val hour = timeParts[0].toInt()
    val minute = timeParts[1].toInt()

    calendar.set(Calendar.HOUR_OF_DAY, hour)
    calendar.set(Calendar.MINUTE, minute)
    calendar.set(Calendar.SECOND, 0)

    // Adjust the alarm based on frequency
    when (frequency) {
        "Daily" -> {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY, // Repeat every day
                pendingIntent
            )
        }
        "Alternate Days" -> {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY * 2, // Repeat every 2 days
                pendingIntent
            )
        }
        else -> {
            // Handle specific days of the week
            val daysOfWeek = listOf("Sundays", "Mondays", "Tuesdays", "Wednesdays", "Thursdays", "Fridays", "Saturdays")
            val dayOfWeek = daysOfWeek.indexOf(frequency)

            calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek + 1) // Set the specific day of the week

            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
}
