package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.DaysLeftUiState
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleState = DaysLeftUiState(
        hasBirthDate = true,
        birthDate = LocalDate.of(1993, 7, 1),
        lifeExpectancyYears = 66,
        daysLeft = 12053,
        daysLived = 12053,
        totalExpectedDays = 24106,
        lifeProgress = 0.5f,
        currentAge = 33,
        youthProgress = 0.73f,
        youthDaysLeft = 4380,
        isYouthPassed = false,
        wisdomProgress = 0.0f,
        wisdomDaysLeft = 7665,
        isWisdomPassed = false,
        isWisdomStarted = false,
        currentYearOfLifeProgress = 0.0f,
        daysToNextBirthday = 365,
        nextBirthdayAge = 34,
        goals = emptyList()
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        DaysLeftAppContent(
            state = sampleState,
            onSaveSettings = { _, _ -> },
            onAddGoal = { _, _ -> },
            onToggleGoal = {},
            onDeleteGoal = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
