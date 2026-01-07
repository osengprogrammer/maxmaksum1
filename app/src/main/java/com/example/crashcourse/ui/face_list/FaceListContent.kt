package com.example.crashcourse.ui.face_list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.crashcourse.db.FaceEntity
import com.example.crashcourse.ui.components.FaceAvatar

@Composable
fun FaceListContent(
    state: FaceListState,
    padding: PaddingValues,
    onEditUser: (FaceEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = state::onSearchChange,
            label = { Text("Search by name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        if (state.filteredFaces.isEmpty()) {
            Text(
                "No records found",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.filteredFaces, key = { it.studentId }) { face ->
                    FaceListItem(
                        face = face,
                        onEdit = { state.startEdit(face) },
                        onEditWithPhoto = { onEditUser(face) },
                        onDelete = { state.delete(face) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FaceListItem(
    face: FaceEntity,
    onEdit: () -> Unit,
    onEditWithPhoto: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FaceAvatar(
                    photoPath = face.photoUrl,
                    size = 64
                )

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(face.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "ID: ${face.studentId}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onEdit) {
                    Text("Quick Edit")
                }
                TextButton(onClick = onEditWithPhoto) {
                    Text("Edit + Photo")
                }
                TextButton(onClick = onDelete) {
                    Text("Delete")
                }
            }
        }
    }
}
