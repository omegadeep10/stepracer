package edu.mga.stepracer

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle

class MainActivity : Activity() {
    var prefs: SharedPreferences? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("edu.mga.stepracer", 0)
        val username = prefs!!.getString("username", "")

        if (username.isEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
