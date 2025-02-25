package com.example.neuroed

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSubjectScreen(navController: NavController) {
    val context = LocalContext.current

    var describeSubject by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf("") }
    var isClassDropdownExpanded by remember { mutableStateOf(false) }

    var selectedSubject by remember { mutableStateOf("") }
    var isSubjectDropdownExpanded by remember { mutableStateOf(false) }

    var selectedGoals by remember { mutableStateOf("Goals") }
    var goalsInput by remember { mutableStateOf("") }
    var learningType by remember { mutableStateOf("") }

    val classOptions = listOf("Class 1", "Class 2", "Class 3", "Class 4")
    val subjectOptions = listOf("Math", "Science", "History", "English")

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // **Describe Subject**
        Text(text = "Describe Subject")
        OutlinedTextField(
            value = describeSubject,
            onValueChange = { describeSubject = it },
            modifier = Modifier.fillMaxWidth()
        )

        // **Select Class Dropdown**
        Text(text = "Select Class")
        ExposedDropdownMenuBox(
            expanded = isClassDropdownExpanded,
            onExpandedChange = { isClassDropdownExpanded = !isClassDropdownExpanded }
        ) {
            OutlinedTextField(
                value = selectedClass,
                onValueChange = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .clickable { isClassDropdownExpanded = true }, // Open dropdown on click
                readOnly = true,
                label = { Text("Select Class") },
//                trailingIcon = {
//                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Dropdown")
//                }
            )

            ExposedDropdownMenu(
                expanded = isClassDropdownExpanded,
                onDismissRequest = { isClassDropdownExpanded = false }
            ) {
                classOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedClass = option
                            isClassDropdownExpanded = false
                        }
                    )
                }
            }
        }

        // **Select Subject Dropdown**
        Text(text = "Select Subject")
        ExposedDropdownMenuBox(
            expanded = isSubjectDropdownExpanded,
            onExpandedChange = { isSubjectDropdownExpanded = !isSubjectDropdownExpanded }
        ) {
            OutlinedTextField(
                value = selectedSubject,
                onValueChange = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .clickable { isSubjectDropdownExpanded = true },
                readOnly = true,
                label = { Text("Select Subject") },
//                trailingIcon = {
//                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Dropdown")
//                }
            )

            ExposedDropdownMenu(
                expanded = isSubjectDropdownExpanded,
                onDismissRequest = { isSubjectDropdownExpanded = false }
            ) {
                subjectOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedSubject = option
                            isSubjectDropdownExpanded = false
                        }
                    )
                }
            }
        }

        // **Select Goals (Radio Buttons)**
        Text(text = "Select Goals")
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selectedGoals == "Goals",
                onClick = { selectedGoals = "Goals" }
            )
            Text("Goals")

            Spacer(modifier = Modifier.width(16.dp))

            RadioButton(
                selected = selectedGoals == "Custom Goals",
                onClick = { selectedGoals = "Custom Goals" }
            )
            Text("Custom Goals")
        }

        // Show Input Field if "Custom Goals" is selected
        if (selectedGoals == "Custom Goals") {
            OutlinedTextField(
                value = goalsInput,
                onValueChange = { goalsInput = it },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // **Learning Type**
        Text(text = "Learning Type")
        OutlinedTextField(
            value = learningType,
            onValueChange = { learningType = it },
            modifier = Modifier.fillMaxWidth()
        )

        // **Upload Image**
        Text(text = "Upload Image")
        Button(onClick = {
            Toast.makeText(context, "Select from Device", Toast.LENGTH_SHORT).show()
        }) {
            Text("Choose File")
        }

        // **Create Button**
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                Toast.makeText(context, "Created Successfully", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("CREATE")
        }
    }
}
