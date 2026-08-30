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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Exercise(val name:String,val sets:Int,val reps:Int,val incrementKg:Double,val note:String="")
data class TrainingDay(val day:String,val focus:String,val exercises:List<Exercise>)
data class ExerciseLog(val date:String,val day:String,val exercise:String,val weight:Double,val completedSets:Int,val targetSets:Int,val reps:Int)

private val program = listOf(
    TrainingDay("Monday","Chest / Lats / Triceps", listOf(
        Exercise("Smith Flat Bench Press",5,5,2.5), Exercise("Lat Pulldown",5,5,2.5),
        Exercise("Seated Low Cable Row",5,5,2.5), Exercise("Smith Close-Grip Bench Press",5,5,2.5),
        Exercise("DB Overhead Triceps Extension",3,10,2.5,"8–10 reps"))),
    TrainingDay("Tuesday","Legs", listOf(
        Exercise("Smith Back Squat",5,5,5.0), Exercise("Smith Romanian Deadlift",5,5,5.0),
        Exercise("Smith Standing Calf Raise",3,12,5.0,"10–15 reps"))),
    TrainingDay("Wednesday","Shoulders / Biceps", listOf(
        Exercise("Smith Overhead Press",5,5,2.5), Exercise("DB Lateral Raise",3,12,0.0,"10–15 reps"),
        Exercise("Low-Cable Upright Row",3,10,2.5,"8–10 reps"), Exercise("Cable Curl",5,5,2.5),
        Exercise("DB Hammer Curl",3,10,0.0,"8–10 reps"))),
    TrainingDay("Thursday","Legs", listOf(
        Exercise("Smith Romanian Deadlift",5,5,5.0), Exercise("Smith Reverse Lunge",3,8,2.5,"8 each leg"),
        Exercise("Smith Back Squat (Light)",3,8,2.5), Exercise("Smith Standing Calf Raise",3,12,5.0,"10–15 reps"))),
    TrainingDay("Friday","Chest / Lats / Shoulders", listOf(
        Exercise("Smith Incline Bench Press",5,5,2.5), Exercise("Lat Pulldown",5,5,2.5),
        Exercise("Seated Low Cable Row",5,5,2.5), Exercise("Smith Overhead Press",5,5,2.5),
        Exercise("DB Lateral Raise",3,12,0.0,"10–15 reps")))
)

class MainActivity: ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?){ super.onCreate(savedInstanceState); setContent { App(this) } }
}

@Composable fun App(context: Context){
    val prefs = context.getSharedPreferences("compound5x5",Context.MODE_PRIVATE)
    var dark by remember { mutableStateOf(prefs.getBoolean("dark", true)) }
    val colors = if(dark) darkColorScheme() else lightColorScheme()
    MaterialTheme(colorScheme = colors){
        Surface(Modifier.fillMaxSize()){
            var tab by remember { mutableStateOf(0) }
            Column{
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween){
                    Column{ Text("Compound 5×5",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold); Text("Smith • Cable • Dumbbells") }
                    Switch(checked=dark,onCheckedChange={ dark=it; prefs.edit().putBoolean("dark",it).apply() })
                }
                TabRow(tab){ listOf("Today","Program","History").forEachIndexed{ i,t-> Tab(selected=tab==i,onClick={tab=i},text={Text(t)}) } }
                when(tab){0->TodayScreen(context);1->ProgramScreen(context);2->HistoryScreen(context)}
            }
        }
    }
}

fun todayIndex(): Int? = when(LocalDate.now().dayOfWeek){ DayOfWeek.MONDAY->0; DayOfWeek.TUESDAY->1; DayOfWeek.WEDNESDAY->2; DayOfWeek.THURSDAY->3; DayOfWeek.FRIDAY->4; else->null }

@Composable fun TodayScreen(context: Context){
    val idx=todayIndex()
    if(idx==null){ Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){ Column(horizontalAlignment=Alignment.CenterHorizontally){Text("Rest day",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Text("Saturday and Sunday are recovery days.")}} }
    else DayWorkout(context,program[idx])
}

@Composable fun ProgramScreen(context: Context){ var i by remember{ mutableStateOf(todayIndex()?:0) }; Column{ ScrollableTabRow(i){program.forEachIndexed{ix,d->Tab(selected=i==ix,onClick={i=ix},text={Text(d.day.take(3))})}}; DayWorkout(context,program[i]) } }

@Composable fun DayWorkout(context: Context, day: TrainingDay){
    Column(Modifier.fillMaxSize()){
        Column(Modifier.padding(16.dp)){ Text(day.day,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);Text(day.focus) }
        LazyColumn(contentPadding=PaddingValues(horizontal=16.dp,vertical=4.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){ items(day.exercises){ ExerciseCard(context,day.day,it) }; item{Spacer(Modifier.height(24.dp))} }
    }
}

@Composable fun ExerciseCard(context: Context, day:String, e:Exercise){
    val prefs=context.getSharedPreferences("compound5x5",Context.MODE_PRIVATE)
    val base="${day}_${e.name}"; val last=prefs.getFloat("last_$base",0f).toDouble(); val pb=prefs.getFloat("pb_${e.name}",0f).toDouble(); val suggested=prefs.getFloat("next_$base",0f).toDouble()
    var weightText by remember(base){ mutableStateOf((if(suggested>0)suggested else last).takeIf{it>0}?.clean() ?: "") }
    var sets by remember(base){ mutableStateOf(List(e.sets){false}) }
    var secs by remember{ mutableIntStateOf(0) }
    LaunchedEffect(secs){ if(secs>0){delay(1000);secs--} }
    val w=weightText.toDoubleOrNull()?:0.0; val done=sets.all{it}
    Card{ Column(Modifier.padding(16.dp)){
        Text(e.name,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)
        Text("${e.sets} × ${e.reps}" + if(e.note.isNotEmpty()) " • ${e.note}" else "")
        if(last>0 || pb>0) Text("Last: ${last.clean()} kg   •   PB: ${pb.clean()} kg",style=MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(weightText,{v->weightText=v.filter{it.isDigit()||it=='.'}},label={Text("Today's weight (kg)")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),modifier=Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp)); Text("Tap each set when complete")
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){ sets.forEachIndexed{i,c-> FilledTonalButton(onClick={sets=sets.toMutableList().also{it[i]=!it[i]}},modifier=Modifier.weight(1f),contentPadding=PaddingValues(vertical=14.dp)){Text(if(c)"✓" else "${i+1}",fontWeight=FontWeight.Bold)} } }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically){ Button(onClick={secs=120}){Text(if(secs>0)"Rest ${secs}s" else "Start 2:00 rest")}; OutlinedButton(onClick={
            prefs.edit().putFloat("last_$base",w.toFloat()).apply(); if(done && w>pb) prefs.edit().putFloat("pb_${e.name}",w.toFloat()).apply();
            val next=if(done && e.incrementKg>0) w+e.incrementKg else w; prefs.edit().putFloat("next_$base",next.toFloat()).apply(); saveLog(context,ExerciseLog(LocalDate.now().toString(),day,e.name,w,sets.count{it},e.sets,e.reps))
        }){Text("Save workout")}}
        if(done && w>0){ Spacer(Modifier.height(8.dp)); Text(if(e.incrementKg>0)"Next target: ${(w+e.incrementKg).clean()} kg" else "All sets completed",fontWeight=FontWeight.SemiBold) }
    }}
}

@Composable fun HistoryScreen(context: Context){ var logs by remember{ mutableStateOf(loadLogs(context)) }; Column(Modifier.padding(16.dp)){ Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text("Workout history",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);TextButton(onClick={logs=loadLogs(context)}){Text("Refresh")}}; if(logs.isEmpty()) Text("No saved workouts yet.") else LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)){items(logs.reversed()){l->Card{Column(Modifier.padding(12.dp)){Text(l.exercise,fontWeight=FontWeight.Bold);Text("${l.date} • ${l.day}");Text("${l.weight.clean()} kg • ${l.completedSets}/${l.targetSets} sets • ${l.reps} reps")}}}} } }

fun saveLog(context: Context,l:ExerciseLog){val p=context.getSharedPreferences("compound5x5",Context.MODE_PRIVATE);val a=JSONArray(p.getString("logs","[]"));a.put(JSONObject().apply{put("date",l.date);put("day",l.day);put("exercise",l.exercise);put("weight",l.weight);put("completedSets",l.completedSets);put("targetSets",l.targetSets);put("reps",l.reps)});p.edit().putString("logs",a.toString()).apply()}
fun loadLogs(context: Context):List<ExerciseLog>{val p=context.getSharedPreferences("compound5x5",Context.MODE_PRIVATE);val a=JSONArray(p.getString("logs","[]"));return (0 until a.length()).map{a.getJSONObject(it)}.map{o->ExerciseLog(o.optString("date"),o.optString("day"),o.optString("exercise"),o.optDouble("weight"),o.optInt("completedSets"),o.optInt("targetSets"),o.optInt("reps"))}}
fun Double.clean()=if(this%1.0==0.0)this.toInt().toString() else String.format("%.1f",this)
