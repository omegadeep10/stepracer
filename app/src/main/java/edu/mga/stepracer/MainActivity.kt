package edu.mga.stepracer

import android.animation.LayoutTransition
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

class MainActivity : Activity() {
    var prefs: SharedPreferences? = null
    var username = "" // current user's username (set by LoginActivity)
    var database: FirebaseDatabase = FirebaseDatabase.getInstance() // firebase ref
    var today = SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().time) // today's date for step counting purposes.
    var screen_width: Int = 400
    var step_max = 5000 // the max steps value to use initially.
    var myself_listener: ValueEventListener? = null;
    var friend_one_listener: ValueEventListener? = null;
    var friend_one_username: String? = null;
    var friend_two_listener: ValueEventListener? = null;
    var friend_two_username: String? = null;
    var friend_three_listener: ValueEventListener? = null;
    var friend_three_username: String? = null;
    var subscriptions_listener: ValueEventListener? = null;
    var history_listener: ValueEventListener? = null;
    var highest_record_listener: ValueEventListener? = null;
    var highest_record: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        your_steps_container.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        friend_one_container.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        friend_two_container.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        friend_three_container.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        history_container.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        // bug with the way it works, so disabled for now
        //database.setPersistenceEnabled(true)

        screen_width = resources.configuration.screenWidthDp - 100 // subtract 100 dp for space for name/steps
        prefs = getSharedPreferences("edu.mga.stepracer", 0); // get prefs
        username = prefs!!.getString("username", "")

        // force user to login first
        if (username.isEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        date.setText("Today - " + SimpleDateFormat("MMMM dd").format(Calendar.getInstance().time))
        // init current user
        initUser()

        // get subs and initialize their bars and data
        subscriptions_listener = database.getReference(username).child("subscriptions").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (!p0.exists()) return

                var fr_1 = p0.child("friend_one").getValue().toString()
                var fr_2 = p0.child("friend_two").getValue().toString()
                var fr_3 = p0.child("friend_three").getValue().toString()

                if (fr_1.isNotEmpty()) {
                    if (friend_one_listener != null) database.getReference(friend_one_username!!).child("history").child(today).removeEventListener(friend_one_listener!!)
                    friend_one_username = fr_1
                    friend_one_listener = initFriend(fr_1, friend_one_steps, friend_one_name, friend_one)
                }

                if (fr_2.isNotEmpty()) {
                    if (friend_two_listener != null) database.getReference(friend_two_username!!).child("history").child(today).removeEventListener(friend_two_listener!!)
                    friend_two_username = fr_2
                    friend_two_listener = initFriend(fr_2, friend_two_steps, friend_two_name, friend_two)
                }

                if (fr_3.isNotEmpty()) {
                    if (friend_three_listener != null) database.getReference(friend_three_username!!).child("history").child(today).removeEventListener(friend_three_listener!!)
                    friend_three_username = fr_3
                    friend_three_listener = initFriend(fr_3, friend_three_steps, friend_three_name, friend_three)
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.e("ERROR: ", p0.toString())
            }
        })

        // listen to changes on History and make sure challenges stay updated
        highest_record_listener = database.getReference(username).child("history").orderByValue().limitToLast(1).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    highest_record = dataSnapshot.children.first().getValue().toString().toInt()

                    if (highest_record >= 500) show_earned(steps_500_bg, steps_500, steps_500_desc) else hide_earned(steps_500_bg, steps_500, steps_500_desc)
                    if (highest_record >= 1000) show_earned(steps_1000_bg, steps_1000, steps_1000_desc) else hide_earned(steps_1000_bg, steps_1000, steps_1000_desc)
                    if (highest_record >= 2000) show_earned(steps_2000_bg, steps_2000, steps_2000_desc) else hide_earned(steps_2000_bg, steps_2000, steps_2000_desc)
                    if (highest_record >= 5000) show_earned(steps_5000_bg, steps_5000, steps_5000_desc) else hide_earned(steps_5000_bg, steps_5000, steps_5000_desc)
                    if (highest_record >= 7500) show_earned(steps_7500_bg, steps_7500, steps_7500_desc) else hide_earned(steps_7500_bg, steps_7500, steps_7500_desc)
                }
               //val x = dataSnapshot.getValue()
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.e("ERROR: ", p0.toString())
            }
        })


        history_listener = database.getReference(username).child("history").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    var cal = Calendar.getInstance()

                    val day_0_str = today
                    cal.add(Calendar.DATE, -1)
                    val day_1_str = SimpleDateFormat("yyyyMMdd").format(cal.time)
                    cal.add(Calendar.DATE, -1)
                    val day_2_str = SimpleDateFormat("yyyyMMdd").format(cal.time)
                    cal.add(Calendar.DATE, -1)
                    val day_3_str = SimpleDateFormat("yyyyMMdd").format(cal.time)
                    cal.add(Calendar.DATE, -1)
                    val day_4_str = SimpleDateFormat("yyyyMMdd").format(cal.time)
                    cal.add(Calendar.DATE, -1)
                    val day_5_str = SimpleDateFormat("yyyyMMdd").format(cal.time)
                    cal.add(Calendar.DATE, -1)
                    val day_6_str = SimpleDateFormat("yyyyMMdd").format(cal.time)


                    var day_0_num = 0
                    var day_1_num = 0
                    var day_2_num = 0
                    var day_3_num = 0
                    var day_4_num = 0
                    var day_5_num = 0
                    var day_6_num = 0

                    if (dataSnapshot.child(day_0_str).exists()) day_0_num = dataSnapshot.child(day_0_str).getValue().toString().toInt()
                    if (dataSnapshot.child(day_1_str).exists()) day_1_num = dataSnapshot.child(day_1_str).getValue().toString().toInt()
                    if (dataSnapshot.child(day_2_str).exists()) day_2_num = dataSnapshot.child(day_2_str).getValue().toString().toInt()
                    if (dataSnapshot.child(day_3_str).exists()) day_3_num = dataSnapshot.child(day_3_str).getValue().toString().toInt()
                    if (dataSnapshot.child(day_4_str).exists()) day_4_num = dataSnapshot.child(day_4_str).getValue().toString().toInt()
                    if (dataSnapshot.child(day_5_str).exists()) day_5_num = dataSnapshot.child(day_5_str).getValue().toString().toInt()
                    if (dataSnapshot.child(day_6_str).exists()) day_6_num = dataSnapshot.child(day_6_str).getValue().toString().toInt()

                    val max_steps = intArrayOf(day_0_num, day_1_num, day_2_num, day_3_num, day_4_num, day_5_num, day_6_num).max()

                    update_history(day_0, day_0_num, max_steps)
                    update_history(day_1, day_1_num, max_steps)
                    update_history(day_2, day_2_num, max_steps)
                    update_history(day_3, day_3_num, max_steps)
                    update_history(day_4, day_4_num, max_steps)
                    update_history(day_5, day_5_num, max_steps)
                    update_history(day_6, day_6_num, max_steps)
                }
                //val x = dataSnapshot.getValue()
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.e("ERROR: ", p0.toString())
            }
        })


        add_friend_button.setOnClickListener(object: View.OnClickListener {
            override fun onClick(p0: View?) {
                val friend = add_friend_input.text.toString()
                if (friend.isEmpty()) return Toast.makeText(baseContext, "Please enter a username", Toast.LENGTH_SHORT).show()

                database.getReference(username).child("subscriptions").addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot) {
                        var fr_1 = ""
                        var fr_2 = ""
                        var fr_3 = ""

                        if (p0.child("friend_one").exists()) fr_1 = p0.child("friend_one").getValue().toString()
                        if (p0.child("friend_two").exists()) fr_2 = p0.child("friend_two").getValue().toString()
                        if (p0.child("friend_three").exists()) fr_3 = p0.child("friend_three").getValue().toString()

                        if (fr_1.isEmpty()) database.getReference(username).child("subscriptions/friend_one").setValue(friend)
                        if (fr_2.isEmpty()) database.getReference(username).child("subscriptions/friend_two").setValue(friend)
                        if (fr_3.isEmpty()) database.getReference(username).child("subscriptions/friend_three").setValue(friend)

                        if (fr_1.isNotEmpty() && fr_2.isNotEmpty() && fr_3.isNotEmpty()) Toast.makeText(baseContext, "Friend limit reached! Tell Deep to fix his app.", Toast.LENGTH_SHORT).show()
                    }

                    override fun onCancelled(p0: DatabaseError) {
                        Log.e("ERROR: ", p0.toString())
                    }
                })
            }
        })
    }

    private fun update_history(bar: View, steps: Int, max: Int?) {
        val basic_steps = 100
        val height: Int = Math.round(maxOf(steps, basic_steps).toFloat() / maxOf(max!!, steps).toFloat() * 150.toFloat())
        bar.layoutParams.height = (height * baseContext.resources.displayMetrics.density).toInt()
        bar.requestLayout()
        // bar.setLayoutParams(RelativeLayout.LayoutParams(bar.width, (height * baseContext.resources.displayMetrics.density).toInt()))
    }

    private fun show_earned(bg: RelativeLayout, steps: TextView, desc: TextView) {
        bg.setBackgroundResource(R.drawable.border_colored)
        steps.setTextColor(ContextCompat.getColor(this, R.color.white))
        desc.setTextColor(ContextCompat.getColor(this, R.color.white))
    }

    private fun hide_earned(bg: RelativeLayout, steps: TextView, desc: TextView) {
        bg.setBackgroundResource(R.drawable.border)
        steps.setTextColor(ContextCompat.getColor(this, R.color.black))
        desc.setTextColor(ContextCompat.getColor(this, R.color.challengesgray))
    }

    fun initFriend(username: String, step_count: TextView, name: TextView, bar: View): ValueEventListener {
        database.getReference(username).child("name").addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                name.setText(p0.getValue().toString())
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.e("ERROR: ", p0.toString())
            }
        }) // set the name

        // init new listener, setup bars, and return the listener for deletion later
        return database.getReference(username).child("history").child(today).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val steps: Int = dataSnapshot.getValue().toString().toInt()
                    updateBar(steps, step_count, bar)
                } else {
                    val steps = 0
                    updateBar(steps, step_count, bar)
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.e("ERROR: ", p0.toString())
            }
        })
    }

    fun updateBar(steps: Int, step_count: TextView, bar: View) {
        step_count.setText(steps.toString() + " Steps")
        step_max = maxOf(step_max, steps) // if steps walked is more than step_max, set it as step_max
        val width: Int = Math.round(maxOf(100, steps).toFloat() / maxOf(step_max, steps).toFloat() * screen_width.toFloat()) // calc width of bar in dp
        bar.setLayoutParams(RelativeLayout.LayoutParams((width * baseContext.resources.displayMetrics.density).toInt(), your_steps.height)) //set bar width and height in pixels
    }

    // Inits the current user's steps and bar
    fun initUser() {
        myself_listener = database.getReference(username).child("history").child(today).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val steps: Int = dataSnapshot.getValue().toString().toInt()
                    step_count.setText(steps.toString() + " Steps")
                    updateBar(steps, your_steps_text, your_steps)
                } else {
                    val steps = 0
                    step_count.setText("0 Steps")
                    updateBar(steps, your_steps_text, your_steps)
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.e("ERROR: ", p0.toString())
            }
        })
    }


    override fun onDestroy() {
        super.onDestroy()
        if (myself_listener != null) database.getReference(username).child("history").child(today).removeEventListener(myself_listener!!)
        if (subscriptions_listener != null) database.getReference(username).child("subscriptions").removeEventListener(subscriptions_listener!!)
        if (highest_record_listener != null) database.getReference(username).child("history").removeEventListener(highest_record_listener!!)
        if (friend_one_listener != null) database.getReference(friend_one_username!!).child("history").child(today).removeEventListener(friend_one_listener!!)
        if (friend_two_listener != null) database.getReference(friend_two_username!!).child("history").child(today).removeEventListener(friend_two_listener!!)
        if (friend_three_listener != null) database.getReference(friend_three_username!!).child("history").child(today).removeEventListener(friend_three_listener!!)
        if (history_listener != null) database.getReference(username).child("history").removeEventListener(history_listener!!)

    }

// Was using this to test Login Activity. safe to delete
//    override fun onStart() {
//        super.onStart()
//        startActivity(Intent(this, LoginActivity::class.java))
//    }
}
