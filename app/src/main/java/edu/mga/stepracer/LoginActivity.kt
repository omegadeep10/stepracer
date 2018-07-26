package edu.mga.stepracer

import android.os.Bundle
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.View
import android.widget.Toast

import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : Activity() {
    var prefs: SharedPreferences? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        prefs = getSharedPreferences("edu.mga.stepracer", 0)

        get_started_btn.setOnClickListener(View.OnClickListener {
            val user = username.text.toString() // The username they entered. SHOULD hopefully exist in firebase
            val name = first_name.text.toString()
            if (user.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Please enter a username and first name", Toast.LENGTH_SHORT).show()
            } else {
                prefs!!.edit().putString("username", user).apply()
                prefs!!.edit().putString("first_name", name).apply()
                startActivity(Intent(this, MainActivity::class.java))
            }
        })
    }

}
