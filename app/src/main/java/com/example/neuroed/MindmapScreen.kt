package com.example.neuroed

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

// Enhanced mind map theme with better colors and spacing
object MindMapTheme {
    // Improved color palette
    val primaryLight = Color(0xFF6366F1)
    val primaryDark = Color(0xFF8B5CF6)
    val accentLight = Color(0xFFFF6B6B)
    val accentDark = Color(0xFFFF8E8E)
    val backgroundLight = Color(0xFFF8FAFC)
    val backgroundDark = Color(0xFF0F172A)
    val surfaceLight = Color(0xFFFFFFFF)
    val surfaceDark = Color(0xFF1E293B)
    val onPrimaryLight = Color.White
    val onPrimaryDark = Color.White

    // Better gradient combinations
    val nodeGradientLight = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
    val nodeGradientDark = listOf(Color(0xFF8B5CF6), Color(0xFFA855F7))
    val connectionLight = Color(0xFF6366F1).copy(alpha = 0.4f)
    val connectionDark = Color(0xFF8B5CF6).copy(alpha = 0.4f)

    // Enhanced accent colors with better contrast
    val accentColors = listOf(
        Color(0xFF3B82F6), // Blue
        Color(0xFF10B981), // Emerald
        Color(0xFFF59E0B), // Amber
        Color(0xFFEF4444), // Red
        Color(0xFF8B5CF6), // Violet
        Color(0xFF06B6D4), // Cyan
        Color(0xFFEC4899), // Pink
        Color(0xFF84CC16)  // Lime
    )

    // Animation specs
    val fadeInSpec = tween<Float>(400, easing = EaseOutCubic)
    val scaleInSpec = tween<Float>(400, easing = EaseOutBack)
    val moveSpec = tween<IntOffset>(250, easing = EaseOutCubic)

    // Improved sizing and spacing
    val nodeWidth = 140.dp
    val nodeHeight = 80.dp
    val controlButtonSize = 44.dp
    val nodeCornerRadius = 12.dp
    val controlsCornerRadius = 16.dp
    val connectionWidth = 2.5f
    val shadowElevation = 6.dp
}

// Data model for mind map nodes
data class MindMapNode(
    val id: Int,
    var title: String,
    var children: List<Int> = emptyList(),
    var position: Offset = Offset.Zero,
    var isExpanded: Boolean = true,
    var colorIndex: Int = 0,
    var notes: String = "",
    var dateCreated: Date = Date(),
    var dateModified: Date = Date()
)

// Node action enum for operations
sealed class NodeAction {
    data class Select(val node: MindMapNode?) : NodeAction()
    data class Add(val parentId: Int) : NodeAction()
    data class Edit(val node: MindMapNode) : NodeAction()
    data class Delete(val node: MindMapNode) : NodeAction()
    data class Move(val nodeId: Int, val newPosition: Offset) : NodeAction()
    data class ToggleExpand(val node: MindMapNode) : NodeAction()
    data class ChangeColor(val nodeId: Int, val colorIndex: Int) : NodeAction()
}

// Main MindMap Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MindmapScreen(navController: NavController) {
    val isDarkTheme = isSystemInDarkTheme()
    val primaryColor = if (isDarkTheme) MindMapTheme.primaryDark else MindMapTheme.primaryLight
    val accentColor = if (isDarkTheme) MindMapTheme.accentDark else MindMapTheme.accentLight
    val backgroundColor = if (isDarkTheme) MindMapTheme.backgroundDark else MindMapTheme.backgroundLight
    val surfaceColor = if (isDarkTheme) MindMapTheme.surfaceDark else MindMapTheme.surfaceLight
    val nodeGradient = if (isDarkTheme) MindMapTheme.nodeGradientDark else MindMapTheme.nodeGradientLight
    val connectionColor = if (isDarkTheme) MindMapTheme.connectionDark else MindMapTheme.connectionLight
    val onPrimaryColor = if (isDarkTheme) MindMapTheme.onPrimaryDark else MindMapTheme.onPrimaryLight

    val configuration = LocalConfiguration.current
    val screenWidthPx = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }
    val scope = rememberCoroutineScope()

    // State management
    var mapOffset by remember { mutableStateOf(Offset(screenWidthPx / 2, screenHeightPx / 2)) }
    var mapScale by remember { mutableFloatStateOf(1f) }
    var selectedNode by remember { mutableStateOf<MindMapNode?>(null) }
    var actionInProgress by remember { mutableStateOf<NodeAction?>(null) }
    var showContextMenu by remember { mutableStateOf(false) }
    var contextMenuPosition by remember { mutableStateOf(Offset.Zero) }
    var editingNodeTitle by remember { mutableStateOf("") }
    var editingNodeNotes by remember { mutableStateOf("") }
    var movingNodeId by remember { mutableStateOf<Int?>(null) }
    var isGridVisible by remember { mutableStateOf(true) }
    var showHelp by remember { mutableStateOf(false) }

    // Sample node data with better initial positioning
    val nodes = remember {
        mutableStateListOf(
            MindMapNode(0, "Main Idea", listOf(1, 2, 3), Offset(0f, 0f), true, 0),
            MindMapNode(1, "Research", listOf(4, 5), Offset(-280f, -160f), true, 1),
            MindMapNode(2, "Development", listOf(6, 7), Offset(280f, -160f), true, 2),
            MindMapNode(3, "Marketing", listOf(8, 9), Offset(0f, 200f), true, 3),
            MindMapNode(4, "Market Analysis", emptyList(), Offset(-400f, -280f), true, 4),
            MindMapNode(5, "User Research", emptyList(), Offset(-160f, -280f), true, 5),
            MindMapNode(6, "UI/UX Design", emptyList(), Offset(160f, -280f), true, 6),
            MindMapNode(7, "Backend Dev", emptyList(), Offset(400f, -280f), true, 7),
            MindMapNode(8, "Social Media", emptyList(), Offset(-140f, 340f), true, 0),
            MindMapNode(9, "Content Strategy", emptyList(), Offset(140f, 340f), true, 1)
        )
    }

    // Animation states
    val animatedScale = remember { Animatable(0.8f) }
    LaunchedEffect(Unit) {
        animatedScale.animateTo(1f, MindMapTheme.scaleInSpec)
    }

    // Handle node actions
    fun handleNodeAction(action: NodeAction) {
        when (action) {
            is NodeAction.Select -> {
                selectedNode = action.node
                showContextMenu = false
            }
            is NodeAction.Add -> {
                val parentNode = nodes.find { it.id == action.parentId } ?: return
                val newId = nodes.maxOfOrNull { it.id }?.plus(1) ?: 1
                val childCount = parentNode.children.size

                // Better positioning algorithm
                val angle = when (childCount) {
                    0 -> -Math.PI / 2 // First child goes up
                    1 -> Math.PI / 2  // Second child goes down
                    2 -> Math.PI      // Third child goes left
                    3 -> 0.0          // Fourth child goes right
                    else -> (childCount - 4) * (Math.PI / 4) + (Math.PI / 8)
                }

                val distance = 180f + (childCount * 20f)
                val newPos = Offset(
                    parentNode.position.x + (distance * cos(angle)).toFloat(),
                    parentNode.position.y + (distance * sin(angle)).toFloat()
                )

                val newNode = MindMapNode(
                    id = newId,
                    title = "New Node",
                    position = newPos,
                    colorIndex = (parentNode.colorIndex + 1) % MindMapTheme.accentColors.size
                )

                nodes.add(newNode)
                val idx = nodes.indexOfFirst { it.id == parentNode.id }
                if (idx >= 0) {
                    nodes[idx] = parentNode.copy(
                        children = parentNode.children + newId,
                        isExpanded = true
                    )
                }

                selectedNode = newNode
                editingNodeTitle = newNode.title
                actionInProgress = NodeAction.Edit(newNode)
            }
            is NodeAction.Edit -> {
                editingNodeTitle = action.node.title
                editingNodeNotes = action.node.notes
                actionInProgress = NodeAction.Edit(action.node)
            }
            is NodeAction.Delete -> {
                val node = action.node
                if (node.id == 0) return // Don't delete root

                // Remove from parent's children
                nodes.forEach { parent ->
                    if (node.id in parent.children) {
                        val parentIdx = nodes.indexOf(parent)
                        nodes[parentIdx] = parent.copy(
                            children = parent.children.filter { it != node.id }
                        )
                    }
                }

                // Remove all descendants
                fun removeDescendants(nodeId: Int) {
                    val nodeToRemove = nodes.find { it.id == nodeId } ?: return
                    nodeToRemove.children.forEach { childId ->
                        removeDescendants(childId)
                    }
                    nodes.removeAll { it.id == nodeId }
                }

                removeDescendants(node.id)

                if (selectedNode?.id == node.id) {
                    selectedNode = null
                }
            }
            is NodeAction.Move -> {
                val idx = nodes.indexOfFirst { it.id == action.nodeId }
                if (idx >= 0) {
                    nodes[idx] = nodes[idx].copy(position = action.newPosition)
                }
                movingNodeId = null
            }
            is NodeAction.ToggleExpand -> {
                val idx = nodes.indexOf(action.node)
                if (idx >= 0) {
                    nodes[idx] = nodes[idx].copy(isExpanded = !nodes[idx].isExpanded)
                }
            }
            is NodeAction.ChangeColor -> {
                val idx = nodes.indexOfFirst { it.id == action.nodeId }
                if (idx >= 0) {
                    nodes[idx] = nodes[idx].copy(colorIndex = action.colorIndex)
                }
            }
        }
    }

    fun saveNodeChanges() {
        val currentNode = (actionInProgress as? NodeAction.Edit)?.node ?: return
        val idx = nodes.indexOfFirst { it.id == currentNode.id }
        if (idx >= 0) {
            nodes[idx] = currentNode.copy(
                title = editingNodeTitle.trim().takeIf { it.isNotBlank() } ?: "Untitled",
                notes = editingNodeNotes.trim(),
                dateModified = Date()
            )

            if (selectedNode?.id == currentNode.id) {
                selectedNode = nodes[idx]
            }
        }
        actionInProgress = null
        editingNodeTitle = ""
        editingNodeNotes = ""
    }

    fun cancelAction() {
        actionInProgress = null
        editingNodeTitle = ""
        editingNodeNotes = ""
    }

    // UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mind Map",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showHelp = !showHelp }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Help"
                        )
                    }

                    IconButton(onClick = { isGridVisible = !isGridVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (isGridVisible) R.drawable.grid else R.drawable.disconnected
                            ),
                            contentDescription = "Toggle Grid"
                        )
                    }

                    IconButton(onClick = {
                        mapOffset = Offset(screenWidthPx / 2, screenHeightPx / 2)
                        mapScale = 1f
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.focus),
                            contentDescription = "Center View"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surfaceColor
                )
            )
        },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Add node FAB
                AnimatedVisibility(
                    visible = selectedNode != null,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    FloatingActionButton(
                        onClick = {
                            selectedNode?.let { handleNodeAction(NodeAction.Add(it.id)) }
                        },
                        containerColor = accentColor,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Child Node",
                            tint = Color.White
                        )
                    }
                }

                // Main FAB
                FloatingActionButton(
                    onClick = {
                        if (selectedNode != null) {
                            handleNodeAction(NodeAction.Select(null))
                        } else {
                            // Add root node if empty
                            if (nodes.isEmpty()) {
                                val rootNode = MindMapNode(
                                    id = 0,
                                    title = "Main Idea",
                                    position = Offset.Zero
                                )
                                nodes.add(rootNode)
                            }
                        }
                    },
                    containerColor = primaryColor,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = if (selectedNode != null) Icons.Default.Close else Icons.Default.Person,
                        contentDescription = if (selectedNode != null) "Deselect" else "Mind Map",
                        tint = Color.White
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(backgroundColor)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        mapOffset += pan
                        mapScale = (mapScale * zoom).coerceIn(0.2f, 3f)
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            if (selectedNode != null) {
                                handleNodeAction(NodeAction.Select(null))
                            }
                            showContextMenu = false
                        },
                        onLongPress = { pos ->
                            showContextMenu = true
                            contextMenuPosition = pos
                        }
                    )
                }
        ) {
            // Background grid
            if (isGridVisible) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val gridSize = 40 * mapScale
                    val offsetX = (mapOffset.x % gridSize)
                    val offsetY = (mapOffset.y % gridSize)
                    val color = if (isDarkTheme) Color.White.copy(alpha = 0.03f) else Color.Black.copy(alpha = 0.03f)

                    // Vertical lines
                    var x = offsetX
                    while (x <= size.width) {
                        drawLine(
                            color = color,
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 0.5f
                        )
                        x += gridSize
                    }

                    // Horizontal lines
                    var y = offsetY
                    while (y <= size.height) {
                        drawLine(
                            color = color,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 0.5f
                        )
                        y += gridSize
                    }
                }
            }

            // Node connections
            Canvas(modifier = Modifier.fillMaxSize()) {
                nodes.forEach { node ->
                    if (node.isExpanded) {
                        node.children.forEach { childId ->
                            val child = nodes.find { it.id == childId } ?: return@forEach
                            val startPos = mapOffset + node.position * mapScale
                            val endPos = mapOffset + child.position * mapScale

                            val nodeColor = MindMapTheme.accentColors[node.colorIndex % MindMapTheme.accentColors.size]
                            val childColor = MindMapTheme.accentColors[child.colorIndex % MindMapTheme.accentColors.size]

                            // Bezier curve connection
                            val controlPoint1 = Offset(
                                startPos.x + (endPos.x - startPos.x) * 0.3f,
                                startPos.y
                            )
                            val controlPoint2 = Offset(
                                startPos.x + (endPos.x - startPos.x) * 0.7f,
                                endPos.y
                            )

                            val path = Path().apply {
                                moveTo(startPos.x, startPos.y)
                                cubicTo(
                                    controlPoint1.x, controlPoint1.y,
                                    controlPoint2.x, controlPoint2.y,
                                    endPos.x, endPos.y
                                )
                            }

                            drawPath(
                                path = path,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        nodeColor.copy(alpha = 0.6f),
                                        childColor.copy(alpha = 0.6f)
                                    ),
                                    start = startPos,
                                    end = endPos
                                ),
                                style = Stroke(width = MindMapTheme.connectionWidth * mapScale, cap = StrokeCap.Round)
                            )
                        }
                    }
                }
            }

            // Nodes
            nodes.forEachIndexed { index, node ->
                val isVisible = node.id == 0 || nodes.any { it.isExpanded && it.children.contains(node.id) }

                if (isVisible) {
                    key(node.id) {
                        NodeComponent(
                            node = node,
                            mapOffset = mapOffset,
                            mapScale = mapScale,
                            isSelected = selectedNode?.id == node.id,
                            onNodeAction = ::handleNodeAction,
                            onMoveStart = { movingNodeId = node.id },
                            onMove = { dragAmount ->
                                if (movingNodeId == node.id) {
                                    val idx = nodes.indexOfFirst { it.id == node.id }
                                    if (idx >= 0) {
                                        val newPos = nodes[idx].position + Offset(
                                            dragAmount.x / mapScale,
                                            dragAmount.y / mapScale
                                        )
                                        nodes[idx] = nodes[idx].copy(position = newPos)
                                    }
                                }
                            },
                            onMoveEnd = { movingNodeId = null },
                            animationDelay = index * 50L
                        )
                    }
                }
            }

            // Control panel
            ControlPanel(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                mapScale = mapScale,
                onZoomIn = { mapScale = (mapScale * 1.2f).coerceIn(0.2f, 3f) },
                onZoomOut = { mapScale = (mapScale * 0.8f).coerceIn(0.2f, 3f) },
                onReset = {
                    mapOffset = Offset(screenWidthPx / 2, screenHeightPx / 2)
                    mapScale = 1f
                },
                surfaceColor = surfaceColor,
                primaryColor = primaryColor
            )

            // Help overlay
            if (showHelp) {
                HelpOverlay(
                    modifier = Modifier.align(Alignment.Center),
                    onDismiss = { showHelp = false },
                    surfaceColor = surfaceColor
                )
            }

            // Status message
            AnimatedVisibility(
                visible = selectedNode == null && nodes.isNotEmpty() && !showHelp,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                StatusMessage(
                    modifier = Modifier.padding(bottom = 100.dp),
                    surfaceColor = surfaceColor
                )
            }

            // Node detail panel
            selectedNode?.let { node ->
                if (actionInProgress == null) {
                    NodeDetailPanel(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(16.dp),
                        node = node,
                        onNodeAction = ::handleNodeAction,
                        surfaceColor = surfaceColor,
                        primaryColor = primaryColor
                    )
                }
            }

            // Context menu
            if (showContextMenu) {
                ContextMenu(
                    position = contextMenuPosition,
                    selectedNode = selectedNode,
                    onAction = { action ->
                        handleNodeAction(action)
                        showContextMenu = false
                    },
                    onDismiss = { showContextMenu = false },
                    onResetView = {
                        mapOffset = Offset(screenWidthPx / 2, screenHeightPx / 2)
                        mapScale = 1f
                        showContextMenu = false
                    },
                    onToggleGrid = {
                        isGridVisible = !isGridVisible
                        showContextMenu = false
                    },
                    isGridVisible = isGridVisible,
                    surfaceColor = surfaceColor
                )
            }

            // Edit dialog
            if (actionInProgress is NodeAction.Edit) {
                EditNodeDialog(
                    title = editingNodeTitle,
                    notes = editingNodeNotes,
                    onTitleChange = { editingNodeTitle = it },
                    onNotesChange = { editingNodeNotes = it },
                    onSave = ::saveNodeChanges,
                    onCancel = ::cancelAction,
                    surfaceColor = surfaceColor,
                    primaryColor = primaryColor
                )
            }
        }
    }
}

@Composable
private fun NodeComponent(
    node: MindMapNode,
    mapOffset: Offset,
    mapScale: Float,
    isSelected: Boolean,
    onNodeAction: (NodeAction) -> Unit,
    onMoveStart: () -> Unit,
    onMove: (Offset) -> Unit,
    onMoveEnd: () -> Unit,
    animationDelay: Long
) {
    val density = LocalDensity.current
    val nodePos = mapOffset + node.position * mapScale
    val nodeWidthPx = with(density) { MindMapTheme.nodeWidth.toPx() } * mapScale
    val nodeHeightPx = with(density) { MindMapTheme.nodeHeight.toPx() } * mapScale
    val xPos = nodePos.x - (nodeWidthPx / 2f)
    val yPos = nodePos.y - (nodeHeightPx / 2f)

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(animationDelay)
        isVisible = true
    }

    val pulseAnimation = rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val nodeColor = MindMapTheme.accentColors[node.colorIndex % MindMapTheme.accentColors.size]
    val nodeScale = if (isSelected) pulseAnimation.value else 1f

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(MindMapTheme.fadeInSpec) +
                scaleIn(MindMapTheme.scaleInSpec) +
                slideInVertically(initialOffsetY = { -100 })
    ) {
        Card(
            modifier = Modifier
                .offset { IntOffset(xPos.roundToInt(), yPos.roundToInt()) }
                .graphicsLayer {
                    scaleX = nodeScale
                    scaleY = nodeScale
                }
                .size(
                    width = MindMapTheme.nodeWidth * mapScale,
                    height = MindMapTheme.nodeHeight * mapScale
                )
                .clickable { onNodeAction(NodeAction.Select(node)) }
                .pointerInput(node.id) {
                    detectDragGestures(
                        onDragStart = { onMoveStart() },
                        onDrag = { _, dragAmount -> onMove(dragAmount) },
                        onDragEnd = { onMoveEnd() }
                    )
                }
                .pointerInput(node.id) {
                    detectTapGestures(
                        onLongPress = { onNodeAction(NodeAction.Select(node)) },
                        onDoubleTap = {
                            if (node.children.isNotEmpty()) {
                                onNodeAction(NodeAction.ToggleExpand(node))
                            }
                        }
                    )
                },
            shape = RoundedCornerShape(MindMapTheme.nodeCornerRadius),
            elevation = CardDefaults.cardElevation(defaultElevation = MindMapTheme.shadowElevation),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = if (node.id == 0) {
                                listOf(
                                    MindMapTheme.primaryLight,
                                    MindMapTheme.primaryDark
                                )
                            } else {
                                listOf(
                                    nodeColor.copy(alpha = 0.8f),
                                    nodeColor
                                )
                            }
                        ),
                        shape = RoundedCornerShape(MindMapTheme.nodeCornerRadius)
                    )
                    .then(
                        if (isSelected) {
                            Modifier.border(
                                2.dp,
                                Color.White,
                                RoundedCornerShape(MindMapTheme.nodeCornerRadius)
                            )
                        } else Modifier
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = node.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = maxOf(13f * mapScale, 8f).sp,
                            color = Color.White,
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.3f),
                                offset = Offset(1f, 1f),
                                blurRadius = 2f
                            )
                        ),
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )

                    if (node.children.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size((12 * mapScale).dp.coerceAtLeast(8.dp))
                                .background(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (node.isExpanded)
                                    Icons.Default.ArrowDropDown else Icons.Default.KeyboardArrowUp,
                                contentDescription = if (node.isExpanded) "Collapse" else "Expand",
                                tint = Color.White,
                                modifier = Modifier.size((8 * mapScale).dp.coerceAtLeast(6.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ControlPanel(
    modifier: Modifier = Modifier,
    mapScale: Float,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onReset: () -> Unit,
    surfaceColor: Color,
    primaryColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(MindMapTheme.controlsCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Zoom in
            IconButton(
                onClick = onZoomIn,
                modifier = Modifier
                    .size(MindMapTheme.controlButtonSize)
                    .background(primaryColor, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Zoom In",
                    tint = Color.White
                )
            }

            // Zoom out
            IconButton(
                onClick = onZoomOut,
                modifier = Modifier
                    .size(MindMapTheme.controlButtonSize)
                    .background(primaryColor, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Zoom Out",
                    tint = Color.White
                )
            }

            // Reset
            IconButton(
                onClick = onReset,
                modifier = Modifier
                    .size(MindMapTheme.controlButtonSize)
                    .background(primaryColor, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset View",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun HelpOverlay(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    surfaceColor: Color
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "How to Use",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                HelpItem("👆", "Tap a node to select it")
                HelpItem("✌️", "Pinch to zoom in/out")
                HelpItem("👋", "Drag to move around the map")
                HelpItem("🖱️", "Drag nodes to reposition them")
                HelpItem("👆👆", "Double-tap to expand/collapse")
                HelpItem("👆⏰", "Long press for context menu")
                HelpItem("➕", "Use FAB to add child nodes")
                HelpItem("🎨", "Change node colors in detail panel")

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Got it!")
                }
            }
        }
    }
}

@Composable
private fun HelpItem(icon: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.width(32.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun StatusMessage(
    modifier: Modifier = Modifier,
    surfaceColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.9f))
    ) {
        Text(
            text = "👆 Select a node to explore • ✌️ Pinch to zoom • 👋 Drag to navigate",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun NodeDetailPanel(
    modifier: Modifier = Modifier,
    node: MindMapNode,
    onNodeAction: (NodeAction) -> Unit,
    surfaceColor: Color,
    primaryColor: Color
) {
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = node.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${node.children.size} children • Created ${formatDate(node.dateCreated)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { onNodeAction(NodeAction.Edit(node)) },
                            modifier = Modifier
                                .background(
                                    primaryColor.copy(alpha = 0.1f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = primaryColor
                            )
                        }

                        if (node.id != 0) {
                            IconButton(
                                onClick = { onNodeAction(NodeAction.Delete(node)) },
                                modifier = Modifier
                                    .background(
                                        Color.Red.copy(alpha = 0.1f),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }

                if (node.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = node.notes,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Color picker
                Text(
                    text = "Node Color",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(MindMapTheme.accentColors) { index, color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(color, CircleShape)
                                .border(
                                    width = if (node.colorIndex == index) 3.dp else 0.dp,
                                    color = if (node.colorIndex == index) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable {
                                    onNodeAction(NodeAction.ChangeColor(node.id, index))
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onNodeAction(NodeAction.Add(node.id)) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Child")
                    }

                    if (node.children.isNotEmpty()) {
                        Button(
                            onClick = { onNodeAction(NodeAction.ToggleExpand(node)) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MindMapTheme.accentColors[2]
                            )
                        ) {
                            Icon(
                                if (node.isExpanded) Icons.Default.ArrowDropDown else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (node.isExpanded) "Collapse" else "Expand")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContextMenu(
    position: Offset,
    selectedNode: MindMapNode?,
    onAction: (NodeAction) -> Unit,
    onDismiss: () -> Unit,
    onResetView: () -> Unit,
    onToggleGrid: () -> Unit,
    isGridVisible: Boolean,
    surfaceColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() }
    ) {
        Card(
            modifier = Modifier
                .offset {
                    IntOffset(
                        position.x.roundToInt(),
                        position.y.roundToInt()
                    )
                }
                .width(200.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                if (selectedNode != null) {
                    ContextMenuItem(
                        icon = Icons.Default.Edit,
                        label = "Edit Node"
                    ) { onAction(NodeAction.Edit(selectedNode)) }

                    ContextMenuItem(
                        icon = Icons.Default.Add,
                        label = "Add Child"
                    ) { onAction(NodeAction.Add(selectedNode.id)) }

                    if (selectedNode.children.isNotEmpty()) {
                        ContextMenuItem(
                            icon = if (selectedNode.isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                            label = if (selectedNode.isExpanded) "Collapse" else "Expand"
                        ) { onAction(NodeAction.ToggleExpand(selectedNode)) }
                    }

                    if (selectedNode.id != 0) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        ContextMenuItem(
                            icon = Icons.Default.Delete,
                            label = "Delete Node",
                            iconTint = Color.Red,
                            textColor = Color.Red
                        ) { onAction(NodeAction.Delete(selectedNode)) }
                    }
                } else {
                    ContextMenuItem(
                        icon = Icons.Default.Refresh,
                        label = "Reset View"
                    ) { onResetView() }

                    ContextMenuItem(
                        iconRes = if (isGridVisible) R.drawable.grid else R.drawable.disconnected,
                        label = if (isGridVisible) "Hide Grid" else "Show Grid"
                    ) { onToggleGrid() }
                }
            }
        }
    }
}

@Composable
private fun ContextMenuItem(
    icon: ImageVector? = null,
    iconRes: Int? = null,
    label: String,
    iconTint: Color = Color.Unspecified,
    textColor: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when {
            icon != null -> {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            iconRes != null -> {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor
        )
    }
}

@Composable
private fun EditNodeDialog(
    title: String,
    notes: String,
    onTitleChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    surfaceColor: Color,
    primaryColor: Color
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Edit Node",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("Node Title") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        focusedLabelColor = primaryColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = onNotesChange,
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        focusedLabelColor = primaryColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = onSave,
                        enabled = title.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

// Helper function to format dates
private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
    return formatter.format(date)
}

// Easing functions
private val EaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
private val EaseInOutSine = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)
private val EaseOutBack = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)