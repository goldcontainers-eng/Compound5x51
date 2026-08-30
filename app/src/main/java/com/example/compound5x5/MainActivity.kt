package com.example.compound5x5

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

data class Exercise(
    val name: String,
    val sets: Int,
    val reps: Int,
    val incrementKg: Double,
    val note: String = ""
)

data class TrainingDay(
    val day: String,
    val focus: String,
    val exercises: List<Exercise>
)

private val program = listOf(

    TrainingDay(
        "Monday",
        "Chest / Lats / Triceps",
        listOf(
            Exercise("Smith Flat Bench Press", 5, 5, 2.5),
            Exercise("Lat Pulldown", 5, 5, 2.5),
            Exercise("Seated Low Cable Row", 5, 5, 2.5),
            Exercise("Smith Close-Grip Bench Press", 5, 5, 2.5),
            Exercise("DB Overhead Triceps Extension", 3, 10, 0.0, "8–10 reps")
        )
    ),

    TrainingDay(
        "Tuesday",
        "Legs",
        listOf(
            Exercise("Smith Back Squat", 5, 5, 5.0),
            Exercise("Smith Romanian Deadlift", 5, 5, 5.0),
            Exercise("Smith Standing Calf Raise", 3, 12, 5.0, "10–15 reps")
        )
    ),

    TrainingDay(
        "Wednesday",
        "Shoulders / Biceps",
        listOf(
            Exercise("Smith Overhead Press", 5, 5, 2.5),
            Exercise("DB Lateral Raise", 3, 12, 0.0, "10–15 reps"),
            Exercise("Low-Cable Upright Row", 3, 10, 2.5, "8–10 reps"),
            Exercise("Cable Curl", 5, 5, 2.5),
            Exercise("DB Hammer Curl", 3, 10, 0.0, "8–10 reps")
        )
    ),

    TrainingDay(
        "Thursday",
        "Legs",
        listOf(
            Exercise("Smith Romanian Deadlift", 5, 5, 5.0),
            Exercise("Smith Reverse Lunge", 3, 8, 2.5, "8 each leg"),
            Exercise("Smith Back Squat - Light", 3, 8, 2.5),
            Exercise("Smith Standing Calf Raise", 3, 12, 5.0, "10–15 reps")
        )
    ),

    TrainingDay(
        "Friday",
        "Chest / Lats / Shoulders",
        listOf(
            Exercise("Smith Incline Bench Press", 5, 5, 2.5),
            Exercise("Lat Pulldown", 5, 5, 2.5),
            Exercise("Seated Low Cable Row", 5, 5, 2.5),
            Exercise("Smith Overhead Press", 5, 5, 2.5),
            Exercise("DB Lateral Raise", 3, 12, 0.0, "10–15 reps")
        )
    )
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            MaterialTheme(
                colorScheme = darkColorScheme()
            ) {

                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    CompoundApp(this)
                }
            }
        }
    }
}

@Composable
fun CompoundApp(context: Context) {

    var selectedTab by remember {
        mutableStateOf(0)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Text(
            text = "Compound 5×5 Pro",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        TabRow(
            selectedTabIndex = selectedTab
        ) {

            Tab(
                selected = selectedTab == 0,
                onClick = {
                    selectedTab = 0
                },
                text = {
                    Text("Workout")
                }
            )

            Tab(
                selected = selectedTab == 1,
                onClick = {
                    selectedTab = 1
                },
                text = {
                    Text("History")
                }
            )
        }

        when (selectedTab) {

            0 -> WorkoutScreen(context)

            1 -> HistoryScreen(context)
        }
    }
}

@Composable
fun WorkoutScreen(context: Context) {

    var selectedDay by remember {
        mutableStateOf(0)
    }

    val day = program[selectedDay]

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        ScrollableTabRow(
            selectedTabIndex = selectedDay
        ) {

            program.forEachIndexed { index, trainingDay ->

                Tab(
                    selected = selectedDay == index,
                    onClick = {
                        selectedDay = index
                    },
                    text = {
                        Text(trainingDay.day.take(3))
                    }
                )
            }
        }

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = day.day,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = day.focus
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            items(day.exercises) { exercise ->

                ExerciseCard(
                    context = context,
                    day = day.day,
                    exercise = exercise
                )
            }
        }
    }
}

@Composable
fun ExerciseCard(
    context: Context,
    day: String,
    exercise: Exercise
) {

    val prefs =
        context.getSharedPreferences(
            "compound5x5",
            Context.MODE_PRIVATE
        )

    val weightKey =
        "weight_${day}_${exercise.name}"

    val bestKey =
        "best_${exercise.name}"

    val savedWeight =
        prefs.getFloat(weightKey, 0f)

    var weightText by remember(
        day,
        exercise.name
    ) {

        mutableStateOf(
            if (savedWeight > 0) {
                savedWeight.toDouble().clean()
            } else {
                ""
            }
        )
    }

    var completedSets by remember(
        day,
        exercise.name
    ) {

        mutableStateOf(
            List(exercise.sets) {
                false
            }
        )
    }

    var timerSeconds by remember {
        mutableIntStateOf(0)
    }

    var timerRunning by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(
        timerRunning,
        timerSeconds
    ) {

        if (
            timerRunning &&
            timerSeconds > 0
        ) {

            delay(1000)

            timerSeconds--

        } else if (
            timerSeconds == 0
        ) {

            timerRunning = false
        }
    }

    val weight =
        weightText.toDoubleOrNull()
            ?: 0.0

    val allCompleted =
        completedSets.all {
            it
        }

    val personalBest =
        prefs.getFloat(
            bestKey,
            0f
        ).toDouble()

    Card {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text =
                    "${exercise.sets} × ${exercise.reps}" +
                    if (
                        exercise.note.isNotBlank()
                    ) {
                        " • ${exercise.note}"
                    } else {
                        ""
                    }
            )

            if (
                personalBest > 0
            ) {

                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )

                Text(
                    text =
                        "Personal best: ${personalBest.clean()} kg"
                )
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            OutlinedTextField(
                value = weightText,
                onValueChange = { value ->

                    weightText =
                        value.filter {
                            it.isDigit() ||
                            it == '.'
                        }

                    value
                        .toDoubleOrNull()
                        ?.let {

                            prefs
                                .edit()
                                .putFloat(
                                    weightKey,
                                    it.toFloat()
                                )
                                .apply()
                        }
                },
                label = {
                    Text("Weight (kg)")
                },
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Decimal
                    ),
                modifier =
                    Modifier.fillMaxWidth()
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            Text(
                text = "Sets completed",
                fontWeight = FontWeight.SemiBold
            )

            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                completedSets
                    .forEachIndexed {
                        index,
                        completed ->

                        FilterChip(
                            selected =
                                completed,
                            onClick = {

                                completedSets =
                                    completedSets
                                        .toMutableList()
                                        .also {

                                            it[index] =
                                                !it[index]
                                        }
                            },
                            label = {
                                Text(
                                    "${index + 1}"
                                )
                            }
                        )
                    }
            }

            if (
                allCompleted &&
                weight > 0 &&
                exercise.incrementKg > 0
            ) {

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                Text(
                    text =
                        "Next target: ${
                            (
                                weight +
                                exercise.incrementKg
                            ).clean()
                        } kg",
                    fontWeight =
                        FontWeight.Bold
                )
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Button(
                    onClick = {

                        timerSeconds = 120

                        timerRunning = true
                    }
                ) {

                    if (
                        timerRunning
                    ) {

                        Text(
                            "Rest ${timerSeconds}s"
                        )

                    } else {

                        Text(
                            "2-min rest"
                        )
                    }
                }

                OutlinedButton(
                    onClick = {

                        if (
                            allCompleted &&
                            weight >
                            personalBest
                        ) {

                            prefs
                                .edit()
                                .putFloat(
                                    bestKey,
                                    weight.toFloat()
                                )
                                .apply()
                        }

                        saveWorkout(
                            context = context,
                            day = day,
                            exercise =
                                exercise.name,
                            weight = weight,
                            completed =
                                completedSets
                                    .count {
                                        it
                                    },
                            target =
                                exercise.sets,
                            reps =
                                exercise.reps
                        )
                    }
                ) {

                    Text("Save")
                }
            }
        }
    }
}

@Composable
fun HistoryScreen(
    context: Context
) {

    var history by remember {

        mutableStateOf(
            loadHistory(context)
        )
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp)
    ) {

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text = "Workout History",
                style =
                    MaterialTheme
                        .typography
                        .titleLarge,
                fontWeight =
                    FontWeight.Bold
            )

            TextButton(
                onClick = {

                    history =
                        loadHistory(context)
                }
            ) {

                Text("Refresh")
            }
        }

        if (
            history.isEmpty()
        ) {

            Text(
                "No saved workouts yet."
            )

        } else {

            LazyColumn(
                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                items(
                    history.reversed()
                ) { item ->

                    Card {

                        Text(
                            text = item,
                            modifier =
                                Modifier.padding(
                                    12.dp
                                )
                        )
                    }
                }
            }
        }
    }
}

fun saveWorkout(
    context: Context,
    day: String,
    exercise: String,
    weight: Double,
    completed: Int,
    target: Int,
    reps: Int
) {

    val prefs =
        context.getSharedPreferences(
            "compound5x5",
            Context.MODE_PRIVATE
        )

    val existing =
        prefs.getString(
            "history",
            "[]"
        ) ?: "[]"

    val array =
        JSONArray(existing)

    val text =
        "${LocalDate.now()} • " +
        "$day • " +
        "$exercise • " +
        "${weight.clean()} kg • " +
        "$completed/$target sets • " +
        "$reps reps"

    array.put(
        JSONObject().apply {
            put(
                "text",
                text
            )
        }
    )

    prefs
        .edit()
        .putString(
            "history",
            array.toString()
        )
        .apply()
}

fun loadHistory(
    context: Context
): List<String> {

    val prefs =
        context.getSharedPreferences(
            "compound5x5",
            Context.MODE_PRIVATE
        )

    val existing =
        prefs.getString(
            "history",
            "[]"
        ) ?: "[]"

    val array =
        JSONArray(existing)

    val result =
        mutableListOf<String>()

    for (
        index in
        0 until array.length()
    ) {

        result.add(
            array
                .getJSONObject(index)
                .optString("text")
        )
    }

    return result
}

fun Double.clean(): String {

    return if (
        this % 1.0 == 0.0
    ) {

        this
            .toInt()
            .toString()

    } else {

        String.format(
            "%.1f",
            this
        )
    }
}
