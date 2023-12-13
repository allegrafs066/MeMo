package com.polar.polarsdkecghrdemo

// HistoryActivity.kt
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class HistoryActivity : AppCompatActivity() {

    private lateinit var listViewHistory: ListView
    private lateinit var buttonBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        listViewHistory = findViewById(R.id.listViewHistory)
        buttonBack = findViewById(R.id.buttonBack)

        // Load and display the meditation history
        displayMeditationHistory()

        // Set click listener for the Back button
        buttonBack.setOnClickListener { finish() }
    }

    private fun displayMeditationHistory() {
        // Retrieve meditation history data (you can replace this with your data retrieval logic)
        val historyDataList = retrieveMeditationHistory()

        // Create an ArrayAdapter to display the data in the ListView
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            historyDataList.map { entry -> "Heart Rate: ${entry.heartRate}, Timestamp: ${entry.timestamp}" }
        )

        // Set the adapter to the ListView
        listViewHistory.adapter = adapter
    }

    private fun retrieveMeditationHistory(): List<MeditationHistoryEntry> {
        // Replace this with your logic to retrieve data from SharedPreferences or a local database
        // For simplicity, I'll create a dummy list here
        val dummyDataList = mutableListOf<MeditationHistoryEntry>()

        // Add some dummy entries
        dummyDataList.add(MeditationHistoryEntry(80, "2023-01-01 12:00:00"))
        dummyDataList.add(MeditationHistoryEntry(95, "2023-01-02 15:30:00"))
        dummyDataList.add(MeditationHistoryEntry(110, "2023-01-03 10:45:00"))

        return dummyDataList
    }
}

data class MeditationHistoryEntry(val heartRate: Int, val timestamp: String)
