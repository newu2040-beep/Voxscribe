package com.example.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.TranscriptEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranscriptionDashboard(viewModel: TranscriptionViewModel) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val recordingState by viewModel.recordingState.collectAsStateWithLifecycle()
    val allTranscripts by viewModel.allTranscripts.collectAsStateWithLifecycle()
    val activeId by viewModel.activeTranscriptId.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val speakerCount by viewModel.speakerCount.collectAsStateWithLifecycle()
    val amplitudes by viewModel.amplitudes.collectAsStateWithLifecycle()
    val realtimeText by viewModel.realtimeText.collectAsStateWithLifecycle()
    val isCloudBackupEnabled by viewModel.isCloudBackupEnabled.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val lastSyncTime by viewModel.lastSyncTime.collectAsStateWithLifecycle()
    val syncingProgress by viewModel.syncingProgress.collectAsStateWithLifecycle()
    val currentTheme by viewModel.selectedTheme.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("record") } // "record" or "history"
    var showLanguageSheet by remember { mutableStateOf(false) }
    var renameDialogTarget by remember { mutableStateOf<TranscriptEntity?>(null) }
    var renameText by remember { mutableStateOf("") }

    // Dynamic Permission Checks
    var hasRecordPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val launcherRecord = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasRecordPermission = granted }
    )

    val launcherNotification = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasNotificationPermission = granted }
    )

    // Request permissions proactively on first load
    LaunchedEffect(Unit) {
        if (!hasRecordPermission) {
            launcherRecord.launch(Manifest.permission.RECORD_AUDIO)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            launcherNotification.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    CompositionLocalProvider(com.example.ui.components.LocalAppTheme provides currentTheme) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("scaffold_root"),
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
        FrostedGlassBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Brand Header with Premium Cloud & Status Dial
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.RecordVoiceOver,
                                contentDescription = "App Icon",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "VoxScribe",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Text(
                            text = "Diarized offline speech engines",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }

                    // Cloud Backups Toggle Status Visualizer
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
                                RoundedCornerShape(30.dp)
                            )
                            .clickable { viewModel.triggerMockCloudSync() }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSyncing) Icons.Default.CloudSync else Icons.Default.CloudQueue,
                            contentDescription = "Sync Status",
                            tint = if (isSyncing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSyncing) "Syncing..." else "Sync Cloud",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                        )
                    }
                }

                // Backup Status Card (Visual reassurance of backups being active)
                AnimatedVisibility(visible = isCloudBackupEnabled) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Backup,
                                    contentDescription = "Backup Info",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Auto backup active: ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = lastSyncTime,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    progress = { syncingProgress },
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Synced Check",
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Modern 3D Segmented Tab Picker (Capsule Layout)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f), RoundedCornerShape(24.dp))
                        .padding(4.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Record Tab Item
                        val bgRecord = if (activeTab == "record") MaterialTheme.colorScheme.primary else Color.Transparent
                        val textColorRecord = if (activeTab == "record") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(bgRecord)
                                .clickable { activeTab = "record" }
                                .testTag("record_tab"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Mic,
                                    contentDescription = "Record icon",
                                    tint = textColorRecord,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Live Record",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = textColorRecord
                                )
                            }
                        }

                        // History/Conversations Tab Item
                        val bgHist = if (activeTab == "history") MaterialTheme.colorScheme.primary else Color.Transparent
                        val textColorHist = if (activeTab == "history") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(bgHist)
                                .clickable { activeTab = "history" }
                                .testTag("history_tab"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Forum,
                                    contentDescription = "History icon",
                                    tint = textColorHist,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Conversations (${allTranscripts.size})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = textColorHist
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Core Main Section Content
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = {
                        slideInHorizontally { width -> if (targetState == "history") width else -width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> if (targetState == "history") -width else width } + fadeOut()
                    },
                    label = "tab_switch_animation"
                ) { currentTab ->
                    if (currentTab == "record") {
                        // 1. RECORDER TAB
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Beautiful Selector 3D Card for Language
                            Soft3DCard(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "TRANSCRIPTION LANGUAGE",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.secondary,
                                            letterSpacing = 1.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = selectedLanguage.flag,
                                                fontSize = 24.sp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = selectedLanguage.name,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    // Custom 3D Change button
                                    Soft3DButton(
                                        onClick = { showLanguageSheet = true },
                                        depth = 3.dp,
                                        modifier = Modifier.wrapContentSize(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Change", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Conversation settings: Number of Speakers to Diarize
                            Soft3DCard(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "SPEAKER IDENTIFICATION DIAL",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.secondary,
                                            letterSpacing = 1.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Identify conversational turns for up to $speakerCount speakers",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }

                                    // Rounded dial controls
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Soft3DButton(
                                            onClick = { viewModel.setSpeakerCount(speakerCount - 1) },
                                            enabled = speakerCount > 1,
                                            depth = 2.dp,
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Remove,
                                                contentDescription = "Decrease speakersCount",
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }

                                        Text(
                                            text = speakerCount.toString(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.width(18.dp),
                                            textAlign = TextAlign.Center
                                        )

                                        Soft3DButton(
                                            onClick = { viewModel.setSpeakerCount(speakerCount + 1) },
                                            enabled = speakerCount < 5,
                                            depth = 2.dp,
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Increase speakersCount",
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Interactive 3D Audio Visualizer / Waveform
                            Soft3DCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (recordingState == RecordingState.RECORDING || recordingState == RecordingState.PAUSED) {
                                        // Living Animated Bars
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            amplitudes.forEachIndexed { _, ampValue ->
                                                val animatedHeight by animateFloatAsState(
                                                    targetValue = ampValue * 80f,
                                                    animationSpec = spring(
                                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                                        stiffness = Spring.StiffnessLow
                                                    ),
                                                    label = "amp_animation"
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .width(4.dp)
                                                        .height(animatedHeight.coerceAtLeast(4f).dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            Brush.verticalGradient(
                                                                listOf(
                                                                    MaterialTheme.colorScheme.primary,
                                                                    MaterialTheme.colorScheme.secondary
                                                                )
                                                            )
                                                        )
                                                )
                                            }
                                        }
                                    } else {
                                        // Silent wave preview
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = Icons.Outlined.GraphicEq,
                                                contentDescription = "Visualizer inactive",
                                                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                                modifier = Modifier.size(40.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Awaiting conversation start...",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Real-time Text Box (Translates physically in real-time)
                            if (recordingState == RecordingState.RECORDING || recordingState == RecordingState.PAUSED || realtimeText.isNotBlank()) {
                                Text(
                                    text = "LIVE SPEECH PREVIEW",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.align(Alignment.Start)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Soft3DTextFieldSlot(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                ) {
                                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                                        item {
                                            Text(
                                                text = if (realtimeText.isBlank()) {
                                                    if (recordingState == RecordingState.PAUSED) "Recording is Paused. Tap Resume to continue transcribing."
                                                    else "Listening for speech... (${selectedLanguage.name})"
                                                } else realtimeText,
                                                fontSize = 14.sp,
                                                lineHeight = 20.sp,
                                                color = if (realtimeText.isBlank()) {
                                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                }
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            // Dynamic Physical 3D Record Controls pad
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                when (recordingState) {
                                    RecordingState.IDLE -> {
                                        Soft3DButton(
                                            onClick = { viewModel.startRecording(context) },
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.fillMaxWidth(0.8f),
                                            testTag = "start_recording_button"
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Mic,
                                                contentDescription = "Mic Icon"
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Start Recording Session", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                                        }
                                    }

                                    RecordingState.RECORDING, RecordingState.PAUSED -> {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(0.9f),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // 1. Pause/Resume button
                                            if (recordingState == RecordingState.RECORDING) {
                                                Soft3DButton(
                                                    onClick = { viewModel.pauseRecording() },
                                                    containerColor = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier.weight(0.38f),
                                                    testTag = "pause_recording_button"
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Pause,
                                                        contentDescription = "Pause Icon"
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Pause", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                }
                                            } else {
                                                Soft3DButton(
                                                    onClick = { viewModel.resumeRecording(context) },
                                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                                    modifier = Modifier.weight(0.38f),
                                                    testTag = "resume_recording_button"
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PlayArrow,
                                                        contentDescription = "Resume Icon"
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Resume", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                }
                                            }

                                            // 2. Finish button
                                            Soft3DButton(
                                                onClick = { viewModel.stopRecording(context) },
                                                containerColor = AlertRed,
                                                modifier = Modifier.weight(0.62f),
                                                testTag = "stop_recording_button"
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Stop,
                                                    contentDescription = "Stop Icon",
                                                    tint = Color.White
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Finish & Analyze", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color.White)
                                            }
                                        }
                                    }

                                    RecordingState.PROCESSING -> {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(56.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.dp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = "Diarizing & Transcribing with Gemini...",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    else -> {}
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Settings toggles in small subtle container at bottom
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    // Theme Selection Row
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Palette,
                                                contentDescription = "Theme selection",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Select App UI Theme Style",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onBackground
                                             )
                                         }
                                         Spacer(modifier = Modifier.height(8.dp))
                                         Row(
                                             modifier = Modifier.fillMaxWidth(),
                                             horizontalArrangement = Arrangement.spacedBy(6.dp)
                                         ) {
                                             com.example.ui.components.AppTheme.values().forEach { themeVariant ->
                                                 val isThemeSelected = currentTheme == themeVariant
                                                 Box(
                                                     modifier = Modifier
                                                         .weight(1f)
                                                         .clip(RoundedCornerShape(10.dp))
                                                         .background(
                                                             if (isThemeSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                             else Color.Transparent
                                                         )
                                                         .border(
                                                             1.dp,
                                                             if (isThemeSelected) MaterialTheme.colorScheme.primary
                                                             else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                                                             RoundedCornerShape(10.dp)
                                                         )
                                                         .clickable { viewModel.setTheme(themeVariant) }
                                                         .padding(vertical = 6.dp),
                                                     contentAlignment = Alignment.Center
                                                 ) {
                                                     Text(
                                                         text = themeVariant.displayName,
                                                         fontSize = 10.sp,
                                                         fontWeight = FontWeight.Bold,
                                                         color = if (isThemeSelected) MaterialTheme.colorScheme.primary
                                                         else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                                     )
                                                 }
                                             }
                                         }
                                     }

                                     HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))

                                     // Backup Settings Row
                                     Row(
                                         modifier = Modifier.fillMaxWidth(),
                                         horizontalArrangement = Arrangement.SpaceBetween,
                                         verticalAlignment = Alignment.CenterVertically
                                     ) {
                                         Row(verticalAlignment = Alignment.CenterVertically) {
                                             Icon(
                                                 imageVector = Icons.Default.CloudQueue,
                                                 contentDescription = "Backup settings",
                                                 tint = MaterialTheme.colorScheme.secondary,
                                                 modifier = Modifier.size(18.dp)
                                             )
                                             Spacer(modifier = Modifier.width(8.dp))
                                             Column {
                                                 Text(
                                                     text = "Enable Cloud Backups",
                                                     fontWeight = FontWeight.Bold,
                                                     fontSize = 13.sp,
                                                     color = MaterialTheme.colorScheme.onBackground
                                                 )
                                                 Text(
                                                     text = "Never lose your files",
                                                     fontSize = 11.sp,
                                                     color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                                 )
                                             }
                                         }

                                         Switch(
                                             checked = isCloudBackupEnabled,
                                             onCheckedChange = { viewModel.setCloudBackupEnabled(it) },
                                             thumbContent = {
                                                 if (isCloudBackupEnabled) {
                                                     Icon(
                                                         imageVector = Icons.Filled.Check,
                                                         contentDescription = null,
                                                         modifier = Modifier.size(SwitchDefaults.IconSize),
                                                     )
                                                 }
                                             }
                                         )
                                     }
                                 }
                             }
                        }
                    } else {
                        // 2. CONVERSATIONS LIST TAB
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (allTranscripts.isEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 48.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ChatBubbleOutline,
                                        contentDescription = "Empty discussions log",
                                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No saved conversations yet",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Record dynamic dialogues & get beautiful diarized transcripts immediately.",
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(horizontal = 24.dp)
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Soft3DButton(
                                        onClick = { activeTab = "record" },
                                        depth = 3.dp,
                                        modifier = Modifier.wrapContentSize()
                                    ) {
                                        Text("Record first message", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    contentPadding = PaddingValues(bottom = 24.dp)
                                ) {
                                    items(allTranscripts) { entity ->
                                        Soft3DConversationItem(
                                            entity = entity,
                                            onSelect = { viewModel.selectTranscript(entity.id) },
                                            onDelete = { viewModel.deleteTranscript(context, entity) },
                                            onRename = {
                                                renameDialogTarget = entity
                                                renameText = entity.title
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // High-fidelity full-screen Transcript Detail Sheet
            activeId?.let { transcriptId ->
                val transcript = allTranscripts.find { it.id == transcriptId }
                if (transcript != null) {
                    SwipeTranscriptDetailView(
                        entity = transcript,
                        viewModel = viewModel,
                        onClose = { viewModel.selectTranscript(null) }
                    )
                }
            }

            // Language Picker Dialog Sheet overlay
            if (showLanguageSheet) {
                AlertDialog(
                    onDismissRequest = { showLanguageSheet = false },
                    title = {
                        Text(
                            text = "Choose Speech Language",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    text = {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(viewModel.languages) { lang ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (lang.code == selectedLanguage.code) {
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                            } else {
                                                Color.Transparent
                                            }
                                        )
                                        .border(
                                            1.dp,
                                            if (lang.code == selectedLanguage.code) {
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                            } else {
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                            },
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            viewModel.setLanguage(lang)
                                            showLanguageSheet = false
                                        }
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = lang.flag,
                                        fontSize = 24.sp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = lang.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Tag: ${lang.code}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showLanguageSheet = false }) {
                            Text("Cancel", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // Rename dialog overlay
            renameDialogTarget?.let { entity ->
                AlertDialog(
                    onDismissRequest = { renameDialogTarget = null },
                    title = { Text("Rename Transcript File", fontWeight = FontWeight.Bold) },
                    text = {
                        OutlinedTextField(
                            value = renameText,
                            onValueChange = { renameText = it },
                            label = { Text("Short Descriptive Title") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                viewModel.updateTranscriptTitle(entity, renameText)
                                renameDialogTarget = null
                                keyboardController?.hide()
                            }),
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    dismissButton = {
                        TextButton(onClick = { renameDialogTarget = null }) {
                            Text("Cancel")
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.updateTranscriptTitle(entity, renameText)
                                renameDialogTarget = null
                            }
                        ) {
                            Text("Apply")
                        }
                    }
                )
            }
        }
    }
    }
}

// 3D Conversation Item representation in History Tab
@Composable
fun Soft3DConversationItem(
    entity: TranscriptEntity,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }

    Soft3DCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circle language badge icon
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    val flag = when (entity.languageCode) {
                        "ne-NP" -> "🇳🇵"
                        "hi-IN" -> "🇮🇳"
                        "es-ES" -> "🇪🇸"
                        "fr-FR" -> "🇫🇷"
                        "de-DE" -> "🇩🇪"
                        "zh-CN" -> "🇨🇳"
                        "ja-JP" -> "🇯🇵"
                        "ar-AE" -> "🇦🇪"
                        "pt-PT" -> "🇵🇹"
                        else -> "🇺🇸"
                    }
                    Text(text = flag, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = entity.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = entity.languageName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        )
                        Text(
                            text = "${entity.durationMs / 1000}s duration",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        )
                        Text(
                            text = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(entity.timestamp)),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // More Menu controls
            Box {
                IconButton(onClick = { expandedMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options context",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                DropdownMenu(
                    expanded = expandedMenu,
                    onDismissRequest = { expandedMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Open Transcript") },
                        onClick = {
                            expandedMenu = false
                            onSelect()
                        },
                        leadingIcon = { Icon(Icons.Default.OpenInNew, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Rename Title") },
                        onClick = {
                            expandedMenu = false
                            onRename()
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, null) }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Delete Log", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            expandedMenu = false
                            onDelete()
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                    )
                }
            }
        }
    }
}

// Full screen detail view with elegant fluid swipes and high accuracy representation
@Composable
fun SwipeTranscriptDetailView(
    entity: TranscriptEntity,
    viewModel: TranscriptionViewModel,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val recordingState by viewModel.recordingState.collectAsStateWithLifecycle()
    val playingFile by viewModel.playingFile.collectAsStateWithLifecycle()
    val playbackProgress by viewModel.playbackProgress.collectAsStateWithLifecycle()

    var isEditing by remember { mutableStateOf(false) }
    var editedText by remember(entity.formattedTranscript) { mutableStateOf(entity.formattedTranscript) }

    FrostedGlassBackground(
        modifier = Modifier
            .fillMaxSize()
            .clickable(enabled = true) { /* Consume clicks to prevent dialog dismiss */ }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Close & Share Toolbar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Soft3DButton(
                    onClick = onClose,
                    depth = 2.dp,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close details",
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = "Conversation Transcript",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Share conversation log
                Soft3DButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Transcription: ${entity.title}\n\n${entity.formattedTranscript}")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Discussion text"))
                    },
                    depth = 2.dp,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share transcript",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable central transcript container
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Main Header Info
                item {
                    Soft3DCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Audiotrack,
                                    contentDescription = "Audio log",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = entity.title,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Language: ${entity.languageName}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "Diarized: Yes (${entity.speakerCount} Speakers)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }

                // Diarized Text Block
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "INTELLIGENT RECONSTRUCTION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.secondary,
                            letterSpacing = 1.sp
                        )
                        Soft3DButton(
                            onClick = { isEditing = !isEditing },
                            depth = 2.dp,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(30.dp),
                            containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
                        ) {
                            Icon(
                                imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = if (isEditing) "Done" else "Edit",
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isEditing) "Done" else "Edit Text", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                item {
                    Soft3DCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            if (isEditing) {
                                Text(
                                    text = "Tap block to adjust details. Retain 'Speaker A:' lines to keep speaker highlight styles.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                OutlinedTextField(
                                    value = editedText,
                                    onValueChange = {
                                        editedText = it
                                        viewModel.updateFormattedTranscript(entity, it)
                                    },
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp, max = 320.dp),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            } else {
                                // Render transcript layout beautifully
                                val paragraphs = entity.formattedTranscript.split("\n")
                                for (line in paragraphs) {
                                    if (line.trim().isNotBlank()) {
                                        if (line.startsWith("**Conversation Summary") || line.startsWith("**Diarized")) {
                                            Text(
                                                text = line.replace("**", ""),
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                                            )
                                        } else if (line.contains(":") && !line.startsWith("http")) {
                                            // Highlight speaker bubbles representation
                                            val parts = line.split(":", limit = 2)
                                            val speakerTag = parts[0].replace("**", "").replace("`", "").trim()
                                            val talkText = parts[1].replace("**", "").trim()

                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(
                                                        if (speakerTag.contains("Speaker A") || speakerTag.contains("Advisor")) {
                                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                                        } else {
                                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
                                                        }
                                                    )
                                                    .padding(8.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.AccountCircle,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp),
                                                        tint = if (speakerTag.contains("Speaker A") || speakerTag.contains("Advisor")) {
                                                            MaterialTheme.colorScheme.primary
                                                        } else {
                                                            MaterialTheme.colorScheme.secondary
                                                        }
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = speakerTag,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = if (speakerTag.contains("Speaker A") || speakerTag.contains("Advisor")) {
                                                            MaterialTheme.colorScheme.primary
                                                        } else {
                                                            MaterialTheme.colorScheme.secondary
                                                        }
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(3.dp))
                                                Text(
                                                    text = talkText,
                                                    fontSize = 13.sp,
                                                    lineHeight = 18.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = line,
                                                fontSize = 13.sp,
                                                lineHeight = 18.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                                modifier = Modifier.padding(vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Append Conversation Pad
                item {
                    Text(
                        text = "ADD CONVERSATIONAL RECORDING",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 1.sp
                    )
                }

                item {
                    Soft3DCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (recordingState == RecordingState.IDLE) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Soft3DButton(
                                        onClick = { 
                                            // Select this transcript as the active one, and start appending
                                            viewModel.selectTranscript(entity.id)
                                            viewModel.startRecording(context, isAppending = true) 
                                        },
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.fillMaxWidth(),
                                        testTag = "append_recording_button"
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = "Append audio dialogue icon"
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Record & Append Voice", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            } else if (viewModel.activeTranscriptId.value == entity.id && 
                                (recordingState == RecordingState.RECORDING || recordingState == RecordingState.PAUSED || recordingState == RecordingState.PROCESSING)
                            ) {
                                // Currently appending to THIS conversation
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Currently appending voice to this dialogue...",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    
                                    if (recordingState == RecordingState.PROCESSING) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    } else {
                                        Text(
                                            text = if (recordingState == RecordingState.PAUSED) "Recording is Paused" else "Listening and transcribing live...",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            // Pause / Resume Append trigger
                                            if (recordingState == RecordingState.RECORDING) {
                                                Soft3DButton(
                                                    onClick = { viewModel.pauseRecording() },
                                                    containerColor = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(Icons.Default.Pause, contentDescription = "Pause append")
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Pause", fontSize = 11.sp)
                                                }
                                            } else {
                                                Soft3DButton(
                                                    onClick = { viewModel.resumeRecording(context) },
                                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(Icons.Default.PlayArrow, contentDescription = "Resume append")
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Resume", fontSize = 11.sp)
                                                }
                                            }
                                            
                                            // Stop and complete append triggers save query in stopRecording()
                                            Soft3DButton(
                                                onClick = { viewModel.stopRecording(context) },
                                                containerColor = AlertRed,
                                                modifier = Modifier.weight(1.5f)
                                            ) {
                                                Icon(Icons.Default.Stop, contentDescription = "Complete append", tint = Color.White)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Finish Append", color = Color.White, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Recording elsewhere or idle
                                Text(
                                    text = "Can record and append voice dialog into this transcript timeline offline.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }

            // Audio Player Controls Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isPlayingThis = playingFile == entity.filePath && recordingState == RecordingState.PLAYING
                    
                    // Soft 3D Play button
                    Soft3DButton(
                        onClick = {
                            if (isPlayingThis) {
                                viewModel.stopAudioPlayback()
                            } else {
                                viewModel.playAudioPlayback(context, entity.filePath)
                            }
                        },
                        depth = 2.dp,
                        shape = RoundedCornerShape(12.dp),
                        containerColor = if (isPlayingThis) AlertRed else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlayingThis) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Media Playback Icon",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isPlayingThis) "Playing Audio..." else "Recorded Audio",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = if (isPlayingThis) "${(playbackProgress * 100).toInt()}%" else "Play Rec",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { if (isPlayingThis) playbackProgress else 0f },
                            modifier = Modifier.fillMaxWidth(),
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Actions Layout: Copy, Export Formats (PDF, TXT, CSV)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Copy Action
                Soft3DButton(
                    onClick = { viewModel.copyToClipboard(context, entity.formattedTranscript) },
                    depth = 3.dp,
                    shape = RoundedCornerShape(14.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f),
                    testTag = "copy_to_clipboard_button"
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy text",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Copy",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Export Text
                Soft3DButton(
                    onClick = { viewModel.exportTranscript(context, entity, "TXT") },
                    depth = 3.dp,
                    shape = RoundedCornerShape(14.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Text file export",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "TXT",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Export CSV
                Soft3DButton(
                    onClick = { viewModel.exportTranscript(context, entity, "CSV") },
                    depth = 3.dp,
                    shape = RoundedCornerShape(14.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.TableChart,
                        contentDescription = "CSV spreadsheet export",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "CSV",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Export PDF
                Soft3DButton(
                    onClick = { viewModel.exportTranscript(context, entity, "PDF") },
                    depth = 3.dp,
                    shape = RoundedCornerShape(14.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f),
                    testTag = "export_pdf_button"
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = "PDF document export",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "PDF",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

// Inline fix extension function to clean up filling spelling
@Suppress("unused")
@Composable
fun Modifier.fillSomeMaxWidth() : Modifier {
    return this.fillMaxWidth()
}
