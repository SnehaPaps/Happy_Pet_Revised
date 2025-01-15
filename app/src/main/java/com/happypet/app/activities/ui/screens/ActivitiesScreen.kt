package com.happypet.app.activities.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ActivitiesScreen() {
    val context = LocalContext.current
    val petNames = remember { mutableStateListOf<String>() }
    val selectedPet = remember { mutableStateOf<String?>(null) }
    val activities = remember { mutableStateMapOf<String, MutableList<Map<String, String>>>() }
    val coroutineScope = rememberCoroutineScope()
    val showDialog = remember { mutableStateOf(false) }
    val selectedActivity = remember { mutableStateOf<String?>(null) }

    // Fetch pet names when the screen is first displayed
    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                fetchPetNamesFromFirebase(petNames) // Fetch and update pet names
            } catch (e: Exception) {
                println("Error fetching pet names: ${e.message}")
            }
        }
    }

    // Fetch activities when a pet is selected
    LaunchedEffect(selectedPet.value) {
        if (selectedPet.value != null) {
            coroutineScope.launch(Dispatchers.IO) {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                val petsRef = FirebaseDatabase.getInstance()
                    .getReference("users/$userId/pets")
                    .orderByChild("name")
                    .equalTo(selectedPet.value)

                petsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.children.firstOrNull()?.key?.let { petId ->
                            coroutineScope.launch(Dispatchers.IO) {
                                fetchActivitiesForPet(
                                    userId = userId,
                                    petId = petId,
                                    activities = activities
                                )
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Log Activities",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )

        if (petNames.isEmpty()) {
            Text(
                text = "Loading pets...",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
            )
        } else {
            DropdownMenuWithSelection(
                items = petNames,
                selectedItem = selectedPet.value,
                onItemSelected = { selectedPet.value = it }
            )

            // Sections for different activities
            listOf("Play Time", "Feed Time", "Sleep Time").forEach { activity ->
                ActivitySection(
                    title = activity,
                    onAddClick = {
                        selectedActivity.value = activity
                        showDialog.value = true
                    },
                    activities = activities[activity] ?: emptyList()
                )
            }
        }
    }

    if (showDialog.value) {
        ActivityDetailsDialog(
            petNames = petNames,
            selectedPet = selectedPet.value,
            selectedActivity = selectedActivity.value,
            onDismiss = { showDialog.value = false },
            onSave = { pet, time ->
                addActivityToFirebase(
                    context = context,
                    petName = pet,
                    activityType = selectedActivity.value.orEmpty()
                )
                showDialog.value = false
            }
        )
    }
}

@Composable
fun ActivitySection(title: String, onAddClick: () -> Unit, activities: List<Map<String, String>>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(16.dp)
        )
        IconButton(
            onClick = onAddClick,
            modifier = Modifier.align(Alignment.End).padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add $title")
        }

        // Display activities as cards
        if (activities.isNotEmpty()) {
            activities.forEach { activity ->
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Time: ${activity["time"]}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Date: ${activity["date"]}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        } else {
            Text(
                text = "No activities yet.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

suspend fun fetchActivitiesForPet(
    userId: String,
    petId: String,
    activities: MutableMap<String, MutableList<Map<String, String>>>
) {
    val database = FirebaseDatabase.getInstance().reference
    val activityTypes = listOf("Play Time", "Feed Time", "Sleep Time")

    activityTypes.forEach { activityType ->
        val activitiesRef = database.child("users").child(userId).child("pets")
            .child(petId).child("activities").child(activityType)

        try {
            val snapshot = activitiesRef.get().await()
            val fetchedActivities = snapshot.children.mapNotNull { activitySnapshot ->
                activitySnapshot.getValue<Map<String, String>>()
            }
            activities[activityType] = fetchedActivities.toMutableList()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}



suspend fun fetchPetNamesFromFirebase(petNames: MutableList<String>) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
        ?: throw Exception("User not authenticated")

    val database = FirebaseDatabase.getInstance().reference
    val petsRef = database.child("users").child(userId).child("pets")

    try {
        val snapshot = petsRef.get().await() // Get the snapshot of the "pets" node
        if (snapshot.exists()) {
            val names = snapshot.children.mapNotNull { it.child("name").getValue(String::class.java) }
            petNames.clear()
            petNames.addAll(names)
        } else {
            // No pets found, handle accordingly
            println("No pets found in database.")
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}



@Composable
fun ActivityDetailsDialog(
    petNames: List<String>,
    selectedPet: String?,
    selectedActivity: String?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    val selectedTime = remember { mutableStateOf("") }
    val selectedPetState = remember { mutableStateOf(selectedPet) }
    val context = LocalContext.current // Get the context here

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(text = "Add $selectedActivity Details")
        },
        text = {
            Column {
                // Dropdown to select pet
                DropdownMenuWithSelection(
                    items = petNames,
                    selectedItem = selectedPetState.value,
                    onItemSelected = { selectedPetState.value = it }
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Input for time
                OutlinedTextField(
                    value = selectedTime.value,
                    onValueChange = { selectedTime.value = it },
                    label = { Text("Select Time (e.g., 10:00 AM)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val pet = selectedPetState.value
                val time = selectedTime.value
                if (pet != null && time.isNotBlank()) {
                    onSave(pet, time)
                } else {
                    Toast.makeText(
                        context, // Use the context here
                        "Please fill all details.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun DropdownMenuWithSelection(
    items: List<String>,
    selectedItem: String?,
    onItemSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        TextButton(onClick = { expanded = true }) {
            Text(text = selectedItem ?: "Select Pet")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    },
                    text = { Text(item) }
                )
            }
        }
    }
}



fun addActivityToFirebase(context: Context, petName: String, activityType: String) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
        ?: return Toast.makeText(context, "User not authenticated.", Toast.LENGTH_SHORT).show()

    val ref = FirebaseDatabase.getInstance()
        .getReference("users/$userId/pets")
        .orderByChild("name")
        .equalTo(petName)

    val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val activityEntry = mapOf(
        "time" to currentTime,
        "date" to currentDate
    )

    ref.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                snapshot.children.forEach { petSnapshot ->
                    val petId = petSnapshot.key ?: return@forEach
                    val activityRef = FirebaseDatabase.getInstance()
                        .getReference("users/$userId/pets/$petId/activities/$activityType")
                    val newActivityKey = activityRef.push().key ?: return@forEach

                    activityRef.child(newActivityKey).setValue(activityEntry)
                        .addOnCompleteListener { activityTask ->
                            if (activityTask.isSuccessful) {
                                Toast.makeText(
                                    context,
                                    "$activityType added for $petName!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Failed to add $activityType for $petName.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            } else {
                Toast.makeText(context, "No pet found with name $petName.", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
        }
    })
}

suspend fun fetchActivitiesForPet(
    userId: String,
    petId: String,
    activityType: String,
    activities: MutableList<Map<String, String>>
) {
    val database = FirebaseDatabase.getInstance().reference
    val activitiesRef = database.child("users").child(userId).child("pets")
        .child(petId).child("activities").child(activityType)

    try {
        val snapshot = activitiesRef.get().await()
        val fetchedActivities = snapshot.children.mapNotNull { activitySnapshot ->
            activitySnapshot.getValue<Map<String, String>>()
        }
        activities.clear()
        activities.addAll(fetchedActivities)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

