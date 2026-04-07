package com.toester.toester

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profile: UserProfile,
    api: ToesterApi,
    onProfileUpdated: (UserProfile) -> Unit,
    onBack: () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val scope = rememberCoroutineScope()
    val s = LocalStrings.current

    // Editable fields
    var nickname by remember { mutableStateOf(profile.nickname) }
    var profilePicDataUri by remember { mutableStateOf(profile.profilePicUrl ?: "") }
    var isSaving by remember { mutableStateOf(false) }

    // Avatar bitmap decoded from data URI
    var avatarBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    // Decode avatar on load if profilePicUrl is a data URI
    LaunchedEffect(profilePicDataUri) {
        avatarBitmap = decodeDataUri(profilePicDataUri)
    }

    // Friends
    var friends by remember { mutableStateOf<List<Friend>>(emptyList()) }
    var incomingRequests by remember { mutableStateOf<List<FriendRequest>>(emptyList()) }
    var outgoingRequests by remember { mutableStateOf<List<FriendRequest>>(emptyList()) }
    var newFriendId by remember { mutableStateOf("") }
    var friendsLoading by remember { mutableStateOf(true) }
    var friendError by remember { mutableStateOf<String?>(null) }

    // XP history
    var xpHistory by remember { mutableStateOf<List<XpHistoryEntry>>(emptyList()) }

    // Level
    val level = levelFromXp(profile.xp)
    val xpInLevel = xpProgressInLevel(profile.xp)
    val xpToNext = xpNeededForNextLevel(profile.xp)
    val levelProgress = xpInLevel.toFloat() / XP_PER_LEVEL

    fun loadFriends() {
        friendsLoading = true
        friendError = null
        scope.launch {
            try {
                friends = api.getFriends(profile.id)
                incomingRequests = api.getIncomingFriendRequests(profile.id)
                outgoingRequests = api.getOutgoingFriendRequests(profile.id)
            } catch (e: Exception) {
                friendError = e.message
                friends = emptyList()
                incomingRequests = emptyList()
                outgoingRequests = emptyList()
            } finally {
                friendsLoading = false
            }
        }
    }

    LaunchedEffect(profile.id) {
        loadFriends()
        try {
            xpHistory = api.getXpHistory(profile.id)
        } catch (_: Exception) {
            xpHistory = emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s.profile) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("<")
                    }
                },
            )
        }
    ) { padding ->
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { 20 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
            // ── Avatar & Name ──────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Avatar – show image or initials
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    val bmp = avatarBitmap
                    if (bmp != null) {
                        Image(
                            bitmap = bmp,
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Text(
                            text = profile.nickname.take(2).uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Upload photo button
                TextButton(onClick = {
                    scope.launch {
                        val dataUri = pickImage()
                        if (dataUri != null) {
                            profilePicDataUri = dataUri
                        }
                    }
                }) {
                    Text(if (avatarBitmap != null) s.changePhoto else s.uploadPhoto)
                }

                Text(profile.name, style = MaterialTheme.typography.headlineSmall)
                Text(
                    "@${profile.nickname}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            HorizontalDivider()

            // ── Level / XP / Streak ────────────────────────
            Text(s.stats, style = MaterialTheme.typography.titleLarge)

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(s.level, style = MaterialTheme.typography.titleMedium)
                        Text("$level", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    LinearProgressIndicator(
                        progress = { levelProgress },
                        modifier = Modifier.fillMaxWidth().height(10.dp),
                    )

                    Text(
                        "$xpInLevel / $XP_PER_LEVEL XP  •  $xpToNext ${s.xpToLevel} ${level + 1}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(s.totalXp, style = MaterialTheme.typography.bodySmall)
                            Text(
                                "${profile.xp}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(s.streak, style = MaterialTheme.typography.bodySmall)
                            Text(
                                "🔥 ${profile.streakDays} ${s.days}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            // ── XP History Graph ─────────────────────────
            if (xpHistory.isNotEmpty()) {
                Text(s.xpProgressTitle, style = MaterialTheme.typography.titleLarge)
                XpLineChart(
                    entries = xpHistory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                )
            }

            HorizontalDivider()

            // ── Edit Profile ───────────────────────────────
            Text(s.editProfile, style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text(s.nickname) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = {
                    isSaving = true
                    scope.launch {
                        try {
                            val updated = api.updateProfile(
                                profile.copy(
                                    nickname = nickname,
                                    profilePicUrl = profilePicDataUri.ifBlank { null },
                                ),
                            )
                            onProfileUpdated(updated)
                        } catch (_: Exception) { /* ignore */ }
                        finally { isSaving = false }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving && (nickname != profile.nickname || profilePicDataUri != (profile.profilePicUrl ?: "")),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text(s.saveChanges)
            }

            HorizontalDivider()

            // ── Leaderboard ──────────────────────────────
            Text(s.leaderboard, style = MaterialTheme.typography.titleLarge)

            // Toggle: Total XP vs Weekly XP
            var showWeekly by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = !showWeekly,
                    onClick = { showWeekly = false },
                    label = { Text(s.totalXp) },
                )
                FilterChip(
                    selected = showWeekly,
                    onClick = { showWeekly = true },
                    label = { Text(s.thisWeek) },
                )
            }

            if (friendsLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                val sorted = if (showWeekly) {
                    friends.sortedByDescending { it.weeklyXp }
                } else {
                    friends.sortedByDescending { it.xp }
                }

                if (sorted.isEmpty()) {
                    Text(s.noFriends, style = MaterialTheme.typography.bodyMedium)
                }

                sorted.forEachIndexed { index, friend ->
                    val rank = index + 1
                    val isFirst = rank == 1

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = if (friend.isYou) CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        ) else CardDefaults.cardColors(),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            // Rank number / trophy
                            Box(
                                modifier = Modifier.width(32.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (isFirst) {
                                    Text("🏆", style = MaterialTheme.typography.titleLarge)
                                } else {
                                    Text(
                                        "#$rank",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }

                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (friend.isYou) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.secondaryContainer,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    friend.name.take(2).uppercase(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (friend.isYou) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSecondaryContainer,
                                )
                            }

                            // Name + level
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        friend.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = if (friend.isYou) FontWeight.Bold else FontWeight.Normal,
                                    )
                                    if (friend.isYou) {
                                        Text(
                                            s.you,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }
                                Text(
                                    "Lvl ${friend.level}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            // XP value
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    if (showWeekly) "+${friend.weeklyXp}" else "${friend.xp}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFirst) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    if (showWeekly) s.thisWeekLabel else s.totalXpLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Send friend request
                Text(s.addFriend, style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = newFriendId,
                        onValueChange = { newFriendId = it },
                        label = { Text(s.userId) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    Button(
                        onClick = {
                            if (newFriendId.isNotBlank()) {
                                scope.launch {
                                    try {
                                        api.sendFriendRequest(profile.id, newFriendId.trim())
                                        newFriendId = ""
                                        loadFriends()
                                    } catch (_: Exception) { /* ignore */ }
                                }
                            }
                        },
                        enabled = newFriendId.isNotBlank(),
                    ) {
                        Text(s.send)
                    }
                }

                // Outgoing pending requests
                if (outgoingRequests.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(s.sentRequests, style = MaterialTheme.typography.titleMedium)
                    outgoingRequests.forEach { req ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("→ ${req.toUserName} (${req.toUserId})")
                                Text(s.pending, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                // Incoming requests
                if (incomingRequests.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(s.incomingRequests, style = MaterialTheme.typography.titleMedium)
                    incomingRequests.forEach { req ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("${req.fromUserName} (${req.fromUserId})")
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = {
                                        scope.launch {
                                            try {
                                                api.respondToFriendRequest(req.id, accept = true)
                                                loadFriends()
                                            } catch (_: Exception) { /* ignore */ }
                                        }
                                    }) { Text(s.accept) }
                                    OutlinedButton(onClick = {
                                        scope.launch {
                                            try {
                                                api.respondToFriendRequest(req.id, accept = false)
                                                loadFriends()
                                            } catch (_: Exception) { /* ignore */ }
                                        }
                                    }) { Text(s.decline) }
                                }
                            }
                        }
                    }
                }

                friendError?.let {
                    Text("⚠️ $it", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
}

// ── Data URI → ImageBitmap helper ──────────────────────────
@OptIn(ExperimentalEncodingApi::class)
private fun decodeDataUri(dataUri: String): ImageBitmap? {
    if (!dataUri.startsWith("data:image")) return null
    return try {
        val base64 = dataUri.substringAfter(",")
        val bytes = Base64.decode(base64)
        decodeImageBytes(bytes)
    } catch (_: Exception) { null }
}

// ── XP Line Chart ─────────────────────────────────────────
@Composable
private fun XpLineChart(
    entries: List<XpHistoryEntry>,
    modifier: Modifier = Modifier,
) {
    if (entries.size < 2) return

    val lineColor = MaterialTheme.colorScheme.primary
    val fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()

    val minXp = (entries.minOf { it.xp } * 0.9f).toInt()
    val maxXp = entries.maxOf { it.xp }
    val range = (maxXp - minXp).coerceAtLeast(1)

    Card(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            val leftPad = 50f
            val bottomPad = 30f
            val chartW = size.width - leftPad
            val chartH = size.height - bottomPad

            // Y-axis grid lines (3 lines)
            for (i in 0..3) {
                val y = chartH - (chartH * i / 3f)
                val xpVal = minXp + (range * i / 3f).toInt()
                drawLine(gridColor, Offset(leftPad, y), Offset(size.width, y), strokeWidth = 1f)
                drawText(
                    textMeasurer = textMeasurer,
                    text = "$xpVal",
                    topLeft = Offset(0f, y - 8f),
                    style = TextStyle(color = labelColor, fontSize = 9.sp),
                )
            }

            // Build path
            val path = Path()
            val fillPath = Path()
            val stepX = chartW / (entries.size - 1)

            entries.forEachIndexed { i, entry ->
                val x = leftPad + stepX * i
                val yNorm = (entry.xp - minXp).toFloat() / range
                val y = chartH - (chartH * yNorm)

                if (i == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, chartH)
                    fillPath.lineTo(x, y)
                } else {
                    path.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }

                // Dot
                drawCircle(lineColor, radius = 4f, center = Offset(x, y))
            }

            // Close fill path
            fillPath.lineTo(leftPad + stepX * (entries.size - 1), chartH)
            fillPath.close()

            drawPath(fillPath, fillColor)
            drawPath(path, lineColor, style = Stroke(width = 3f, cap = StrokeCap.Round))

            // X-axis labels (first, middle, last)
            val labelIndices = listOf(0, entries.size / 2, entries.lastIndex)
            labelIndices.forEach { idx ->
                val x = leftPad + stepX * idx
                drawText(
                    textMeasurer = textMeasurer,
                    text = entries[idx].day,
                    topLeft = Offset(x - 15f, chartH + 6f),
                    style = TextStyle(color = labelColor, fontSize = 9.sp),
                )
            }
        }
    }
}
