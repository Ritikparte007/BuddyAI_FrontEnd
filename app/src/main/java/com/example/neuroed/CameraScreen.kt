package com.example.neuroed

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController

// Enum for Camera Mode
enum class CameraMode {
    Photo, Video
}

// Data model for each subject item
data class SubjectItem(
    val label: String,
    val icon: ImageVector
)

@Composable
fun CameraScreen(
    navController: NavController,
    onEnableCameraClick: () -> Unit = {}
) {
    // List of 10 subjects
    val subjects = listOf(
        SubjectItem("Math", Icons.Default.Home),
        SubjectItem("English", Icons.Default.Edit),
        SubjectItem("Biology", Icons.Default.Email),
        SubjectItem("Chemistry", Icons.Default.Home),
        SubjectItem("Physics", Icons.Default.Edit),
        SubjectItem("History", Icons.Default.Email),
        SubjectItem("Geography", Icons.Default.Home),
        SubjectItem("Literature", Icons.Default.Edit),
        SubjectItem("Computer", Icons.Default.Email),
        SubjectItem("Art", Icons.Default.Home)
    )

    var selectedIndex by remember { mutableStateOf(0) }
    // Camera mode state: Photo or Video
    var cameraMode by remember { mutableStateOf(CameraMode.Photo) }
    // State to determine whether the front camera is used
    var useFrontCamera by remember { mutableStateOf(false) }
    // Determine current camera selector based on state
    val currentCameraSelector =
        if (useFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA
        else CameraSelector.DEFAULT_BACK_CAMERA

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Entire screen is black
    ) {
        // Show the camera preview (covers full screen)
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            cameraSelector = currentCameraSelector
        )

        // Top overlay: Camera Mode Selector and Flip Camera Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .align(Alignment.TopCenter)
        ) {
            // Camera mode selector in the center
            CameraModeSelector(
                selectedMode = cameraMode,
                onModeSelected = { selectedMode -> cameraMode = selectedMode },
                modifier = Modifier.align(Alignment.Center)
            )
            // Flip camera button on the top–right
            IconButton(
                onClick = { useFrontCamera = !useFrontCamera },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Flip Camera",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Bottom capture button: shows either photo capture or video record button based on mode
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
                .zIndex(1f), // Ensure capture button row is on top
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (cameraMode == CameraMode.Photo) {
                // Photo capture button
                FloatingActionButton(
                    onClick = { /* TODO: Insert image capture logic */ },
                    containerColor = Color.White,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Capture Image",
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }
            } else {
                // Video record button
                FloatingActionButton(
                    onClick = { /* TODO: Insert video record logic */ },
                    containerColor = Color.Red,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Record Video",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // Bottom subject bar with 10 subjects (drawn below capture button)
        SubjectBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            subjects = subjects,
            selectedIndex = selectedIndex,
            onSubjectClick = { newIndex ->
                selectedIndex = newIndex
                // Handle subject selection if needed
            }
        )
    }
}

@Composable
fun CameraPreview(modifier: Modifier = Modifier, cameraSelector: CameraSelector) {
    val context = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

    // AndroidView to host the PreviewView from CameraX
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                // Build the Preview use case
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview
                    )
                } catch (exc: Exception) {
                    Log.e("CameraPreview", "Camera binding failed", exc)
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        }
    )
}

@Composable
fun SubjectBar(
    modifier: Modifier = Modifier,
    subjects: List<SubjectItem>,
    selectedIndex: Int,
    onSubjectClick: (Int) -> Unit
) {
    // Subject bar with a black background and rounded corners
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        color = Color.Black
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(subjects.size) { index ->
                val subject = subjects[index]
                val isSelected = (index == selectedIndex)

                Column(
                    modifier = Modifier
                        .clickable { onSubjectClick(index) }
                        .padding(horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Circular icon container
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 56.dp else 44.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color.White else Color(0xFF333333)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = subject.icon,
                            contentDescription = subject.label,
                            tint = if (isSelected) Color(0xFF6F4F9E) else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(if (isSelected) 28.dp else 22.dp)
                        )
                    }
                    // Subject label
                    Text(
                        text = subject.label,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                        fontSize = if (isSelected) 14.sp else 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CameraModeSelector(
    selectedMode: CameraMode,
    onModeSelected: (CameraMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0x55000000))
            .padding(4.dp)
    ) {
        ModeButton(
            label = "Photo",
            isSelected = selectedMode == CameraMode.Photo,
            onClick = { onModeSelected(CameraMode.Photo) }
        )
        Spacer(modifier = Modifier.width(8.dp))
        ModeButton(
            label = "Video",
            isSelected = selectedMode == CameraMode.Video,
            onClick = { onModeSelected(CameraMode.Video) }
        )
    }
}

@Composable
fun ModeButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Color.White else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.Black else Color.White,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
