package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.YearlyGoal
import com.example.ui.DaysLeftUiState
import com.example.ui.DaysLeftViewModel
import com.example.ui.DaysLeftViewModelFactory
import com.example.ui.theme.*
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val app = application as DaysLeftApplication
        val viewModel: DaysLeftViewModel by viewModels {
            DaysLeftViewModelFactory(app.repository)
        }
        
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val state by viewModel.uiState.collectAsStateWithLifecycle()
                    DaysLeftAppContent(
                        state = state,
                        onSaveSettings = { dob, expectancy ->
                            viewModel.saveUserSettings(dob, expectancy)
                        },
                        onAddGoal = { title, year ->
                            viewModel.addGoal(title, year)
                        },
                        onToggleGoal = { goal ->
                            viewModel.toggleGoal(goal)
                        },
                        onDeleteGoal = { id ->
                            viewModel.deleteGoal(id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DaysLeftAppContent(
    state: DaysLeftUiState,
    onSaveSettings: (LocalDate, Int) -> Unit,
    onAddGoal: (String, Int) -> Unit,
    onToggleGoal: (YearlyGoal) -> Unit,
    onDeleteGoal: (Int) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = state.hasBirthDate,
                label = "ScreenTransition",
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                }
            ) { isConfigured ->
                if (isConfigured) {
                    DashboardScreen(
                        state = state,
                        onOpenSettings = { showEditDialog = true },
                        onAddGoal = onAddGoal,
                        onToggleGoal = onToggleGoal,
                        onDeleteGoal = onDeleteGoal
                    )
                } else {
                    OnboardingScreen(
                        onSaveSettings = onSaveSettings
                    )
                }
            }
            
            if (showEditDialog && state.birthDate != null) {
                SettingsDialog(
                    initialBirthDate = state.birthDate,
                    initialExpectancy = state.lifeExpectancyYears,
                    onDismiss = { showEditDialog = false },
                    onConfirm = { dob, expectancy ->
                        onSaveSettings(dob, expectancy)
                        showEditDialog = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onSaveSettings: (LocalDate, Int) -> Unit
) {
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var expectancyInput by remember { mutableStateOf("66") }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = Instant.now().toEpochMilli()
    )
    
    val formattedDate = remember(selectedDate) {
        selectedDate?.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")) ?: "Not Selected"
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .widthIn(max = 500.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Identity Header
        Text(
            text = "DAYS LEFT",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.ExtraBold,
            color = SunsetCrimson,
            letterSpacing = 4.sp,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "A reverse lifespan countdown to inspire intentional, mindful, and courageous living.",
            style = MaterialTheme.typography.bodyLarge,
            color = MutedGray,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Setup Form Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, CardBorder), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Configure Your Journey",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                
                // DOB Selection Trigger
                Column {
                    Text(
                        text = "Date of Birth",
                        style = MaterialTheme.typography.labelMedium,
                        color = MutedGray,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DeepSlate)
                            .border(BorderStroke(1.dp, CardBorder), RoundedCornerShape(8.dp))
                            .clickable { showDatePicker = true }
                            .padding(16.dp)
                            .testTag("onboarding_dob_button"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formattedDate,
                            color = if (selectedDate != null) White else MutedGray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Select Birth Date",
                            tint = SunsetCrimson
                        )
                    }
                }
                
                // Life Expectancy Input
                Column {
                    Text(
                        text = "Life Expectancy (Years)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MutedGray,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = expectancyInput,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.all { it.isDigit() }) {
                                expectancyInput = input
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_expectancy_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SunsetCrimson,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = White,
                            unfocusedTextColor = White,
                            focusedContainerColor = DeepSlate,
                            unfocusedContainerColor = DeepSlate
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Expectancy Timer",
                                tint = MutedGray
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Average life expectancy is 66 years.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedGray
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Begin Button
                Button(
                    onClick = {
                        val dob = selectedDate
                        val exp = expectancyInput.toIntOrNull() ?: 66
                        if (dob != null) {
                            onSaveSettings(dob, exp)
                        }
                    },
                    enabled = selectedDate != null && expectancyInput.isNotEmpty() && (expectancyInput.toIntOrNull() ?: 0) > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("start_countdown_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SunsetCrimson,
                        disabledContainerColor = SunsetCrimson.copy(alpha = 0.3f),
                        contentColor = White,
                        disabledContentColor = White.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Start Counting Down",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
    
    // M3 DatePickerDialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    showDatePicker = false
                }) {
                    Text("Select", color = SunsetCrimson, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = MutedGray)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = DarkCharcoal
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    titleContentColor = White,
                    headlineContentColor = White,
                    weekdayContentColor = MutedGray,
                    subheadContentColor = MutedGray,
                    navigationContentColor = White,
                    yearContentColor = White,
                    selectedYearContentColor = ObsidianBlack,
                    selectedYearContainerColor = SunsetCrimson,
                    dayContentColor = White,
                    selectedDayContentColor = ObsidianBlack,
                    selectedDayContainerColor = SunsetCrimson,
                    todayContentColor = SunsetCrimson,
                    todayDateBorderColor = SunsetCrimson
                )
            )
        }
    }
}

@Composable
fun DashboardScreen(
    state: DaysLeftUiState,
    onOpenSettings: () -> Unit,
    onAddGoal: (String, Int) -> Unit,
    onToggleGoal: (YearlyGoal) -> Unit,
    onDeleteGoal: (Int) -> Unit
) {
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showYouthDetailsDialog by remember { mutableStateOf(false) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // App Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DAYS LEFT",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = SunsetCrimson,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Memento Mori",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedGray
                    )
                }
                
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .background(DeepSlate, CircleShape)
                        .testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = White
                    )
                }
            }
        }
        
        // Huge Bold Reverse Countdown HERO Card
        item {
            val formattedDaysLeft = remember(state.daysLeft) {
                NumberFormat.getNumberInstance(Locale.US).format(state.daysLeft)
            }
            val formattedDaysLived = remember(state.daysLived) {
                NumberFormat.getNumberInstance(Locale.US).format(state.daysLived)
            }
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .border(BorderStroke(1.dp, CardBorder), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "REMAINING ALIVE",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MutedGray,
                        letterSpacing = 2.sp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (state.daysLeft >= 0) {
                        Text(
                            text = formattedDaysLeft,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Black
                            ),
                            color = SunsetCrimson,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("days_left_text")
                        )
                        Text(
                            text = "DAYS",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = White,
                            letterSpacing = 6.sp,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        // Bonus days lived past expectancy
                        val bonusDays = -state.daysLeft
                        val formattedBonus = NumberFormat.getNumberInstance(Locale.US).format(bonusDays)
                        Text(
                            text = "+$formattedBonus",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Black
                            ),
                            color = GoldWisdom,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "BONUS DAYS LIVED",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = White,
                            letterSpacing = 2.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Comprehensive Life Progress Bar
                    val percentageText = String.format("%.1f%%", state.lifeProgress * 100)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Journey Complete",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedGray
                        )
                        Text(
                            text = percentageText,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = SunsetCrimson
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { state.lifeProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = SunsetCrimson,
                        trackColor = DeepSlate
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "DAYS LIVED",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedGray,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = formattedDaysLived,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = White
                            )
                        }
                        
                        Divider(
                            modifier = Modifier
                                .height(36.dp)
                                .width(1.dp),
                            color = CardBorder
                        )
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "CURRENT AGE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedGray,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${state.currentAge} YRS",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = White
                            )
                        }
                    }
                }
            }
        }
        
        // Progress Bars & Milestones Title
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = null,
                    tint = ElectricAmber,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LIFE MILESTONES",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = White,
                    letterSpacing = 1.sp
                )
            }
        }
        
        // 1. Current Year Cycle Milestone Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .border(BorderStroke(1.dp, CardBorder), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Year ${state.currentAge} in Review",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = White
                            )
                            Text(
                                text = "Annual personal cycle countdown",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedGray
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .background(SunsetCrimson.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Annual",
                                style = MaterialTheme.typography.labelSmall,
                                color = SunsetCrimson,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LinearProgressIndicator(
                        progress = { state.currentYearOfLifeProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = SunsetCrimson,
                        trackColor = DeepSlate
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${state.daysToNextBirthday} days until age ${state.nextBirthdayAge}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = White
                        )
                        Text(
                            text = String.format("%.0f%%", state.currentYearOfLifeProgress * 100),
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedGray
                        )
                    }
                }
            }
        }
        
        // 2. Youthfulness Milestone Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .border(BorderStroke(1.dp, CardBorder), RoundedCornerShape(16.dp))
                    .clickable { showYouthDetailsDialog = true }
                    .testTag("youth_milestone_card"),
                colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Youthfulness Phase",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = White
                            )
                            Text(
                                text = "Ages 0 to 45 · Click to view youthfulness remaining",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedGray
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .background(YouthBlue.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (state.isYouthPassed) "Completed" else "Active",
                                style = MaterialTheme.typography.labelSmall,
                                color = YouthBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LinearProgressIndicator(
                        progress = { state.youthProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = YouthBlue,
                        trackColor = DeepSlate
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (state.isYouthPassed) {
                                "Youthfulness phase completed."
                            } else {
                                val formattedYouthDays = NumberFormat.getNumberInstance(Locale.US).format(state.youthDaysLeft)
                                "$formattedYouthDays days left of youthfulness"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = White
                        )
                        Text(
                            text = String.format("%.0f%%", state.youthProgress * 100),
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedGray
                        )
                    }
                }
            }
        }
        
        // 3. Wisdom Milestone Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .border(BorderStroke(1.dp, CardBorder), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Wisdom Phase",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = White
                            )
                            Text(
                                text = "Ages 45 to ${state.lifeExpectancyYears} · Reflection, mentorship & legacy",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedGray
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .background(GoldWisdom.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = when {
                                    state.isWisdomPassed -> "Completed"
                                    state.isWisdomStarted -> "Active"
                                    else -> "Locked"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldWisdom,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LinearProgressIndicator(
                        progress = { state.wisdomProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = GoldWisdom,
                        trackColor = DeepSlate
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when {
                                state.isWisdomPassed -> "Wisdom phase completed."
                                state.isWisdomStarted -> {
                                    val formattedWisdomDays = NumberFormat.getNumberInstance(Locale.US).format(state.wisdomDaysLeft)
                                    "$formattedWisdomDays days remaining in wisdom"
                                }
                                else -> {
                                    // Calculate days until age 45
                                    val formattedYouthDays = NumberFormat.getNumberInstance(Locale.US).format(state.youthDaysLeft)
                                    "$formattedYouthDays days until entry (at age 45)"
                                }
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = White
                        )
                        Text(
                            text = String.format("%.0f%%", state.wisdomProgress * 100),
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedGray
                        )
                    }
                }
            }
        }
        
        // Custom Intentional Goals Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ListAlt,
                        contentDescription = null,
                        tint = SunsetCrimson,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DAILY INTENTIONS",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = White,
                        letterSpacing = 1.sp
                    )
                }
                
                TextButton(
                    onClick = { showAddGoalDialog = true },
                    modifier = Modifier.testTag("add_goal_trigger")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Goal",
                        tint = SunsetCrimson,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Add Intention",
                        color = SunsetCrimson,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
        
        // Empty State / List of Intentions
        if (state.goals.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .border(BorderStroke(1.dp, CardBorder), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.EditCalendar,
                            contentDescription = null,
                            tint = MutedGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No active intentions",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add daily guidelines, yearly dreams, or rules to make every single day intentional.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(state.goals) { goal ->
                GoalItemRow(
                    goal = goal,
                    onToggle = onToggleGoal,
                    onDelete = onDeleteGoal
                )
            }
        }
    }
    
    // Add Intention Dialog
    if (showAddGoalDialog) {
        AddGoalDialog(
            currentAge = state.currentAge,
            onDismiss = { showAddGoalDialog = false },
            onConfirm = { title, targetYear ->
                onAddGoal(title, targetYear)
                showAddGoalDialog = false
            }
        )
    }

    // Youthfulness Details Dialog
    if (showYouthDetailsDialog) {
        AlertDialog(
            onDismissRequest = { showYouthDetailsDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = YouthBlue
                    )
                    Text(
                        text = "Youthfulness Countdown",
                        color = White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "REMAINING YOUTHFULNESS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedGray,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    
                    val formattedYouthDays = remember(state.youthDaysLeft) {
                        NumberFormat.getNumberInstance(Locale.US).format(state.youthDaysLeft)
                    }
                    
                    if (state.isYouthPassed) {
                        Text(
                            text = "0",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            color = YouthBlue
                        )
                        Text(
                            text = "You have transitioned beautifully into the wisdom phase of your life.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = White,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = formattedYouthDays,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 48.sp
                            ),
                            color = YouthBlue,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("youthfulness_dialog_days_text")
                        )
                        Text(
                            text = "DAYS LEFT",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = White,
                            letterSpacing = 4.sp
                        )
                        
                        LinearProgressIndicator(
                            progress = { state.youthProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = YouthBlue,
                            trackColor = DeepSlate
                        )
                        
                        Text(
                            text = "Youthfulness spans from age 0 to 45. Live boldly, intentionally, and without fear.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedGray,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showYouthDetailsDialog = false },
                    modifier = Modifier.testTag("youthfulness_dialog_close")
                ) {
                    Text("Understood", color = YouthBlue, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkCharcoal
        )
    }
}

@Composable
fun GoalItemRow(
    goal: YearlyGoal,
    onToggle: (YearlyGoal) -> Unit,
    onDelete: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .border(BorderStroke(1.dp, CardBorder), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Checkbox(
                    checked = goal.isCompleted,
                    onCheckedChange = { onToggle(goal) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = SunsetCrimson,
                        checkmarkColor = ObsidianBlack,
                        uncheckedColor = MutedGray
                    )
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (goal.isCompleted) MutedGray else White,
                        textDecoration = if (goal.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                    Text(
                        text = "Intention Target Age: ${goal.year}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedGray
                    )
                }
            }
            
            IconButton(
                onClick = { onDelete(goal.id) },
                modifier = Modifier.testTag("delete_goal_${goal.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Intention",
                    tint = MutedGray.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun AddGoalDialog(
    currentAge: Int,
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetYearInput by remember { mutableStateOf(currentAge.toString()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Intentional Goal",
                color = White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text(
                        text = "Goal Intention",
                        style = MaterialTheme.typography.labelMedium,
                        color = MutedGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("e.g. Daily cold shower, Express gratitude") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_goal_title_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SunsetCrimson,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = White,
                            unfocusedTextColor = White,
                            focusedContainerColor = DeepSlate,
                            unfocusedContainerColor = DeepSlate
                        ),
                        singleLine = true
                    )
                }
                
                Column {
                    Text(
                        text = "Target Age for Goal",
                        style = MaterialTheme.typography.labelMedium,
                        color = MutedGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = targetYearInput,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.all { it.isDigit() }) {
                                targetYearInput = input
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_goal_year_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SunsetCrimson,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = White,
                            unfocusedTextColor = White,
                            focusedContainerColor = DeepSlate,
                            unfocusedContainerColor = DeepSlate
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val target = targetYearInput.toIntOrNull() ?: currentAge
                    if (title.isNotEmpty()) {
                        onConfirm(title, target)
                    }
                },
                enabled = title.isNotEmpty() && targetYearInput.isNotEmpty(),
                modifier = Modifier.testTag("new_goal_submit_button")
            ) {
                Text("Add", color = SunsetCrimson, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MutedGray)
            }
        },
        containerColor = DarkCharcoal
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    initialBirthDate: LocalDate,
    initialExpectancy: Int,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, Int) -> Unit
) {
    var selectedDate by remember { mutableStateOf(initialBirthDate) }
    var expectancyInput by remember { mutableStateOf(initialExpectancy.toString()) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )
    
    val formattedDate = remember(selectedDate) {
        selectedDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Life Parameters",
                color = White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Date of Birth Selection Trigger
                Column {
                    Text(
                        text = "Date of Birth",
                        style = MaterialTheme.typography.labelMedium,
                        color = MutedGray,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DeepSlate)
                            .border(BorderStroke(1.dp, CardBorder), RoundedCornerShape(8.dp))
                            .clickable { showDatePicker = true }
                            .padding(16.dp)
                            .testTag("settings_dob_trigger"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formattedDate,
                            color = White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Select Birth Date",
                            tint = SunsetCrimson
                        )
                    }
                }
                
                // Life Expectancy Input
                Column {
                    Text(
                        text = "Life Expectancy (Years)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MutedGray,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = expectancyInput,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.all { it.isDigit() }) {
                                expectancyInput = input
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_expectancy_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SunsetCrimson,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = White,
                            unfocusedTextColor = White,
                            focusedContainerColor = DeepSlate,
                            unfocusedContainerColor = DeepSlate
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val exp = expectancyInput.toIntOrNull() ?: 66
                    onConfirm(selectedDate, exp)
                },
                enabled = expectancyInput.isNotEmpty() && (expectancyInput.toIntOrNull() ?: 0) > 0,
                modifier = Modifier.testTag("settings_save_button")
            ) {
                Text("Save", color = SunsetCrimson, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MutedGray)
            }
        },
        containerColor = DarkCharcoal
    )
    
    // M3 DatePickerDialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    showDatePicker = false
                }) {
                    Text("Select", color = SunsetCrimson, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = MutedGray)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = DarkCharcoal
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    titleContentColor = White,
                    headlineContentColor = White,
                    weekdayContentColor = MutedGray,
                    subheadContentColor = MutedGray,
                    navigationContentColor = White,
                    yearContentColor = White,
                    selectedYearContentColor = ObsidianBlack,
                    selectedYearContainerColor = SunsetCrimson,
                    dayContentColor = White,
                    selectedDayContentColor = ObsidianBlack,
                    selectedDayContainerColor = SunsetCrimson,
                    todayContentColor = SunsetCrimson,
                    todayDateBorderColor = SunsetCrimson
                )
            )
        }
    }
}
