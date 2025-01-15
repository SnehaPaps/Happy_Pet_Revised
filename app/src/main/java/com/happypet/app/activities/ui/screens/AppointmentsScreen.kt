package com.happypet.app.activities.ui.screens

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.happypet.app.R
import com.happypet.app.base.ReminderReceiver
import java.util.*

@Composable
fun AppointmentsScreen() {
    var showDialog by remember { mutableStateOf(false) }
    val appointments = remember { mutableStateListOf<Appointment>() }
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    LaunchedEffect(Unit) {
        val database = FirebaseDatabase.getInstance().reference.child("users").child(userId).child("appointments")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                appointments.clear()
                for (child in snapshot.children) {
                    val appointment = child.getValue(Appointment::class.java)
                    appointment?.let { appointments.add(it) }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Appointment")
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
                    text = "Appointments",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (appointments.isNotEmpty()) {
                    AppointmentList(appointments)
                }
            }
        }
    )

    if (showDialog) {
        AppointmentDialog(onDismiss = { showDialog = false })
    }
}

@Composable
fun AppointmentDialog(onDismiss: () -> Unit) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    var clinicName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var appointmentDate by remember { mutableStateOf("") }
    var selectedPet by remember { mutableStateOf<String?>(null) }
    val petNames = remember { mutableStateListOf<String>() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val petDatabase = FirebaseDatabase.getInstance().reference.child("users").child(userId).child("pets")
        petDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                petNames.clear()
                for (child in snapshot.children) {
                    val name = child.child("name").getValue(String::class.java)
                    name?.let { petNames.add(it) }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            appointmentDate = String.format("%02d-%02d-%d", selectedDay, selectedMonth + 1, selectedYear)
        },
        year,
        month,
        day
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Appointment") },
        text = {
            Column {
                TextField(
                    value = clinicName,
                    onValueChange = { clinicName = it },
                    label = { Text("Pet Clinic/Vet Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(onClick = { datePickerDialog.show() }) {
                    Text(text = if (appointmentDate.isNotEmpty()) appointmentDate else "Select Appointment Date")
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (petNames.isNotEmpty()) {
                    DropdownMenuBox(
                        label = "Select Pet",
                        options = petNames,
                        selectedValue = selectedPet ?: "",
                        onValueChange = { selectedPet = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val database = FirebaseDatabase.getInstance().reference
                        .child("users").child(userId).child("appointments")
                    val appointmentId = database.push().key
                    val appointment = Appointment(clinicName, address, appointmentDate, selectedPet ?: "")
                    appointmentId?.let { database.child(it).setValue(appointment) }

                    // Set reminder
                    setAppointmentReminder(context, calendar.timeInMillis, clinicName)

                    onDismiss()
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


fun setAppointmentReminder(context: Context, timeInMillis: Long, clinicName: String) {
    val intent = Intent(context, ReminderReceiver::class.java).apply {
        putExtra("TITLE", "Appointment Reminder")
        putExtra("CONTENT", "It's time for your appointment at $clinicName.")
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
}

data class Appointment(
    val clinicName: String = "",
    val address: String = "",
    val appointmentDate: String = "",
    val selectedPet: String = "",  // Fixed naming issue here
)

@Composable
fun AppointmentList(appointments: List<Appointment>) {
    LazyColumn {
        items(appointments) { appointment ->
            AppointmentItem(appointment)
            HorizontalDivider() // Optional divider between items
        }
    }
}

@Composable
fun AppointmentItem(appointment: Appointment) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFDF0054))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading icon for the appointment
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color(0xFFDF007F), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_calendar),
                    contentDescription = "Select Date",
                    tint = Color.White

                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Column for appointment details
            Column(modifier = Modifier.weight(1f)) {
                // Pet name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_pet),
                        modifier = Modifier.size(18.dp),
                        contentDescription = "Pet",
                        tint = Color.White

                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pet: ${appointment.selectedPet}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Clinic name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_pet),
                        modifier = Modifier.size(18.dp),
                        contentDescription = "Hospital",
                        tint = Color.White

                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Clinic: ${appointment.clinicName}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Address
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_address),
                        modifier = Modifier.size(18.dp),
                        contentDescription = "Address",
                        tint = Color.White

                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Address: ${appointment.address}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Appointment date
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = "Date Icon",
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Date: ${appointment.appointmentDate}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}
