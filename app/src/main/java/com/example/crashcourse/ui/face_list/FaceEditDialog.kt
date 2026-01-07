package com.example.crashcourse.ui.face_list

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import com.example.crashcourse.db.FaceEntity

@Composable
fun FaceEditDialog(
    editingFace: FaceEntity?,
    onDismiss: () -> Unit,
    onSave: (FaceEntity) -> Unit
) {
    if (editingFace == null) return

    var name by remember { mutableStateOf(editingFace.name) }
    var className by remember { mutableStateOf(editingFace.className) }
    var subClass by remember { mutableStateOf(editingFace.subClass) }
    var grade by remember { mutableStateOf(editingFace.grade) }
    var subGrade by remember { mutableStateOf(editingFace.subGrade) }
    var program by remember { mutableStateOf(editingFace.program) }
    var role by remember { mutableStateOf(editingFace.role) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Face Data") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Name") })
                OutlinedTextField(className, { className = it }, label = { Text("Class") })
                OutlinedTextField(subClass, { subClass = it }, label = { Text("SubClass") })
                OutlinedTextField(grade, { grade = it }, label = { Text("Grade") })
                OutlinedTextField(subGrade, { subGrade = it }, label = { Text("SubGrade") })
                OutlinedTextField(program, { program = it }, label = { Text("Program") })
                OutlinedTextField(role, { role = it }, label = { Text("Role") })
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        editingFace.copy(
                            name = name,
                            className = className,
                            subClass = subClass,
                            grade = grade,
                            subGrade = subGrade,
                            program = program,
                            role = role
                        )
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
