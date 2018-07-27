package edu.mga.stepracer

import android.app.job.JobParameters
import android.app.job.JobService
import android.nfc.Tag
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class StepService: com.firebase.jobdispatcher.JobService() {
    var database: FirebaseDatabase = FirebaseDatabase.getInstance() // firebase ref

    override fun onStopJob(job: com.firebase.jobdispatcher.JobParameters): Boolean {
        println("TEST JOB END")
        return false;
    }

    override fun onStartJob(job: com.firebase.jobdispatcher.JobParameters): Boolean {
        var prefs = getSharedPreferences("edu.mga.stepracer", 0); // get prefs
        var username = prefs!!.getString("username", "")
        var today = SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().time)

        if (username.isEmpty()) return false

        val fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .build()

        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) return false

        var fitness_client = Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
        var result = fitness_client!!.readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)

        result
            .addOnSuccessListener {
                val steps = it.dataPoints.get(0).getValue(Field.FIELD_STEPS).asInt()
                Log.d("STEPS: ", steps.toString())
                database.getReference(username).child("history").child(today).setValue(steps)
                jobFinished(job, false)
            }
            .addOnFailureListener {
                println("Failed to obtain steps. Debug line 40 in StepService")
                jobFinished(job, true)
            }
        return true
    }
}