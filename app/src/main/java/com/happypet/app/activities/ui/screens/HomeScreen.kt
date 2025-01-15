package com.happypet.app.activities.ui.screens

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.happypet.app.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HomeScreen() {
    val showDialog = remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog.value = true },
                content = { Icon(Icons.Default.Add, contentDescription = "Add Pet") }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp) // Add padding for layout alignment
            ) {
                // Title Text
                Text(
                    text = "Your Pets",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 16.dp) // Spacing from the list
                )

                // Pet List
                PetListScreen()

                // Add Pet Dialog
                if (showDialog.value) {
                    AddPetDialog(onDismiss = { showDialog.value = false })
                }
            }
        }
    }
}





@Composable
fun AddPetDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val name = remember { mutableStateOf("") }
    val photoUri = remember { mutableStateOf<Uri?>(null) }
    val petType = remember { mutableStateOf("") }
    val breed = remember { mutableStateOf("") }
    val birthDate = remember { mutableStateOf("") }
    val breedOptions = remember { mutableStateListOf<String>() }
    val typeOptions = listOf("Dog", "Cat", "Bird") // Example pet types
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            photoUri.value = uri // Update the photoUri with the selected image's URI
        }
    }
    val calendar = remember { Calendar.getInstance() }
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        android.app.DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                birthDate.value = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                showDatePicker = false
            },
            year,
            month,
            day
        ).show()
    }

    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Photo Upload Button
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .clickable {
                            launcher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (photoUri.value == null) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Upload Photo",
                            modifier = Modifier.size(50.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        AsyncImage(
                            model = photoUri.value,
                            contentDescription = "Pet Photo",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                        )
                    }
                }

                // Pet Name
                OutlinedTextField(
                    value = name.value,
                    onValueChange = { name.value = it },
                    label = { Text("Pet Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Pet Type Dropdown
                DropdownMenuBox(
                    label = "Pet Type",
                    options = typeOptions,
                    selectedValue = petType.value,
                    onValueChange = {
                        petType.value = it
                        breedOptions.clear()
                        breedOptions.addAll(
                            when (it) {
                                "Dog" -> listOf("Labrador", "Beagle", "Poodle")
                                "Cat" -> listOf("Siamese", "Persian", "Bengal")
                                "Bird" -> listOf("Parrot", "Canary", "Finch")
                                else -> emptyList()
                            }
                        )
                    }
                )

                // Breed Dropdown
                DropdownMenuBox(
                    label = "Breed",
                    options = breedOptions,
                    selectedValue = breed.value,
                    onValueChange = { breed.value = it }
                )

                // Birth Date with Calendar Icon
                OutlinedTextField(
                    value = birthDate.value,
                    onValueChange = { },
                    label = { Text("Select Birth Date") },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_pet), // Replace with your drawable resource ID
                            contentDescription = "Select Date",
                            modifier = Modifier.clickable {
                                showDatePicker = true
                            }
                        )
                    },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().clickable {
                        showDatePicker = true
                    }
                )

                // Add Pet Button
                val coroutineScope = rememberCoroutineScope()

                Button(
                    onClick = {
                        if (name.value.isBlank() || petType.value.isBlank() || breed.value.isBlank() || birthDate.value.isBlank()) {
                            Toast.makeText(context, "All fields are required!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        coroutineScope.launch {
                            val photoUrl = uploadPhotoToFirebase(photoUri.value)
                            addPetToFirebase(
                                name.value,
                                photoUrl,
                                petType.value,
                                breed.value,
                                birthDate.value
                            )
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Pet")
                }


            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuBox(
    label: String,
    options: List<String>,
    selectedValue: String,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = { },
            label = { Text(label) },
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor() // Aligns the dropdown with the text box
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}




suspend fun addPetToFirebase(name: String, photoUrl: String?, type: String, breed: String, birthDate: String) {
    try {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: throw Exception("User not authenticated")
        val database = FirebaseDatabase.getInstance().reference
        val petId = database.child("users").child(userId).child("pets").push().key ?: throw Exception("Failed to generate pet ID")

        val petData = mapOf(
            "name" to name,
            "photo_url" to (photoUrl ?: ""),
            "pet_type" to type,
            "breed" to breed,
            "birthdate" to birthDate
        )

        database.child("users").child(userId).child("pets").child(petId).setValue(petData).await()
    } catch (e: Exception) {
        Log.e("FirebaseDatabase", "Failed to save pet data: ${e.message}", e)
    }
}



suspend fun uploadPhotoToFirebase(photoUri: Uri?): String? {
    if (photoUri == null) return null

    // Get a reference to Firebase Storage
    val storageReference = FirebaseStorage.getInstance().reference
    val fileName = "pets/${System.currentTimeMillis()}.jpg"
    val fileReference = storageReference.child(fileName)

    return try {
        // Upload the photo to Firebase Storage
        val uploadTask = fileReference.putFile(photoUri).await()
        // Get the download URL
        fileReference.downloadUrl.await().toString()
    } catch (e: Exception) {
        // Handle upload errors
        Log.e("FirebaseUpload", "Photo upload failed: ${e.message}", e)
        null
    }
}


@Composable
fun PetCard(pet: Pet) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .height(120.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFDF0054))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pet Image
            AsyncImage(
                model = pet.photo_url,
                contentDescription = "Pet Image",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(4.dp, Color.White, CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Pet Details
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                // Pet Name
                Text(
                    text = pet.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )

                // Pet Age
                Text(
                    text = calculatePetAge(pet.birthdate),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )

                // Chips for Type and Breed
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    DetailChip(label = pet.pet_type)
                    DetailChip(label = pet.breed)
                }
            }
        }
    }
}

@Composable
fun DetailChip(label: String) {
    Card(
        shape = RoundedCornerShape(50),
        modifier = Modifier.wrapContentSize(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFDF0054),
                textAlign = TextAlign.Center
            )
        }
    }
}


// Helper to calculate pet age
fun calculatePetAge(birthdate: String): String {
    return try {
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val birthDate = dateFormatter.parse(birthdate)
        val currentDate = Calendar.getInstance().time
        val diff = currentDate.time - birthDate.time
        val years = diff / (365 * 24 * 60 * 60 * 1000L)
        val days = (diff % (365 * 24 * 60 * 60 * 1000L)) / (24 * 60 * 60 * 1000L)
        "${years} years ${days} days old"
    } catch (e: Exception) {
        "Unknown age"
    }
}

@Composable
fun PetListScreen() {
    val pets = remember { mutableStateListOf<Pet>() }
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    FirebaseDatabase.getInstance().reference.child("users").child(userId).child("pets")
        .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pets.clear()
                snapshot.children.forEach {
                    val pet = it.getValue(Pet::class.java)
                    if (pet != null) pets.add(pet)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

    LazyColumn {
        items(pets) { pet ->
            PetCard(pet)
        }
    }
}



data class Pet(
    val name: String = "",
    val photo_url: String = "",
    val pet_type: String = "",
    val breed: String = "",
    val birthdate: String = ""
)
