package com.polar.polarsdkecghrdemo

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity



class hasil : AppCompatActivity() {
    companion object {
        private const val TAG = "hasil"
        private const val PREF_NAME = "MeditationHistory"
        private const val HEART_RATE_KEY = "heartRate"
        private const val TIMESTAMP_KEY = "timestamp"
    }

    private lateinit var textViewAverage: TextView
    private lateinit var textViewResult: TextView
    private var averageHeartRate: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.meditateresult)
        textViewAverage = findViewById(R.id.textViewAverage)
        textViewResult = findViewById(R.id.textViewResult)

        // Retrieve averageHR from intent extras
        averageHeartRate = intent.getIntExtra("averageHR", 0)

        // Update text views with averageHR
        updateTextViews(averageHeartRate)

        // ... other initialization code ...

        val homeButton: Button = findViewById(R.id.buttonHome)
        homeButton.setOnClickListener { onClickHome(it) }

        storeMeditationData(averageHeartRate)
    }


    // ... other methods ...

    fun updateTextViews(hrValue: Int) {
        // Update the HR TextView
        textViewAverage.text = hrValue.toString()

        // Check the heart rate and display a message
        when {
            hrValue < 70 -> {
                textViewResult.text = "Exercise a bit more"
            }
            hrValue > 120 -> {
                textViewResult.text = "You need to relax"
            }
            else -> {
                textViewResult.text = "You're doing great!"
            }
        }
    }
    private fun onClickHome(view: View) {
        // Create an Intent for MainActivity
        val intent = Intent(this, MainActivity::class.java)

        // Start MainActivity
        startActivity(intent)

        // Finish the current activity (hasil)
        finish()

    }

    private fun storeMeditationData(heartRate: Int) {
        val sharedPreferences: SharedPreferences =
            getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        // Retrieve existing meditation history
        val existingHistory = sharedPreferences.getStringSet("meditation_history", mutableSetOf()) ?: mutableSetOf()

        // Add the new entry to the history
        val timestamp: String = System.currentTimeMillis().toString()
        val newEntry = "$heartRate,$timestamp"
        existingHistory.add(newEntry)

        // Save the updated set back to SharedPreferences
        editor.putStringSet("meditation_history", existingHistory)
        editor.apply()
    }

}