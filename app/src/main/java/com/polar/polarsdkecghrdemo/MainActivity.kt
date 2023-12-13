package com.polar.polarsdkecghrdemo

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "Polar_MainActivity"
        private const val SHARED_PREFS_KEY = "polar_device_id"
        private const val MEDITATION_HISTORY_KEY = "meditation_history"
        private const val PERMISSION_REQUEST_CODE = 1
        private const val MEDITATION_HISTORY_PREFS = "meditation_history_prefs"
        private const val PREF_NAME = "MeditationHistory"

    }

    data class MeditationHistoryEntry(val heartRate: Int, val timestamp: String)


    private lateinit var sharedPreferences: SharedPreferences
    private val bluetoothOnActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode != Activity.RESULT_OK) {
            Log.w(TAG, "Bluetooth off")
        }
    }
    private var deviceId: String? = null
    private lateinit var meditationHistory: MutableList<MeditationHistoryEntry>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPreferences = getPreferences(MODE_PRIVATE)
        deviceId = sharedPreferences.getString(SHARED_PREFS_KEY, "")

        val setIdButton: Button = findViewById(R.id.buttonSetID)
        val hrConnectButton: Button = findViewById(R.id.buttonConnectHr)
        val historyButton: Button = findViewById(R.id.buttonHistory)
        checkBT()

        setIdButton.setOnClickListener { onClickChangeID(it) }
        hrConnectButton.setOnClickListener { onClickConnectHr(it) }
        historyButton.setOnClickListener { showMeditationHistoryPopup() }
    }

    private fun showMeditationHistoryPopup() {
        val historyDataList = retrieveMeditationHistory()
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            historyDataList.map { entry -> "Heart Rate: ${entry.heartRate}, Timestamp: ${entry.timestamp}" }
        )

        val listView = ListView(this)
        listView.adapter = adapter

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Meditation History")
        builder.setView(listView)

        val dialog = builder.create()
        dialog.show()
    }

    private fun retrieveMeditationHistory(): List<MeditationHistoryEntry> {
        val prefs = getSharedPreferences(MainActivity.MEDITATION_HISTORY_PREFS, MODE_PRIVATE)
        val historyEntries = prefs.getStringSet("history_entries", setOf()) ?: setOf()

        return historyEntries.mapNotNull { entry ->
            val parts = entry.split(",")
            if (parts.size == 2) {
                val heartRate = parts[0].toIntOrNull()
                val timestamp = parts[1]
                if (heartRate != null) {
                    MeditationHistoryEntry(heartRate, timestamp)
                } else {
                    null
                }
            } else {
                null
            }
        }
    }
    fun onClickHistory(view: View) {
        // Create an Intent for HistoryActivity
        val intent = Intent(this, HistoryActivity::class.java)

        // Start HistoryActivity
        startActivity(intent)
    }

    private fun saveMeditationHistory() {
        // Save meditation history to SharedPreferences
        val json = Gson().toJson(meditationHistory)
        val editor = sharedPreferences.edit()
        editor.putString(MEDITATION_HISTORY_KEY, json)
        editor.apply()
    }

    private fun addMeditationEntry(entry: MeditationHistoryEntry) {
        meditationHistory.add(entry)
        saveMeditationHistory()
    }

    private fun onClickConnectHr(view: View) {
        checkBT()
        if (deviceId == null || deviceId == "") {
            deviceId = sharedPreferences.getString(SHARED_PREFS_KEY, "")
            showDialog(view)
        } else {
            showToast(getString(R.string.connecting) + " " + deviceId)
            // Save data to SharedPreferencesx
            saveMeditationHistoryEntry(System.currentTimeMillis(), getMockedHeartRate()) // Replace with actual values
            val intent = Intent(this, HRActivity::class.java)
            intent.putExtra("id", deviceId)
            startActivity(intent)
        }
    }

    private fun saveMeditationHistoryEntry(timestamp: Long, heartRate: Int) {
        val prefs = getSharedPreferences(MEDITATION_HISTORY_PREFS, MODE_PRIVATE)
        val editor = prefs.edit()

        // Retrieve existing entries
        val existingEntries = prefs.getStringSet("history_entries", mutableSetOf()) ?: mutableSetOf()

        // Add the new entry
        val newEntry = "$heartRate,$timestamp"
        existingEntries.add(newEntry)

        // Save the updated set back to SharedPreferences
        editor.putStringSet("history_entries", existingEntries)
        editor.apply()
    }

    private fun onClickChangeID(view: View) {
        showDialog(view)
    }

    private fun showDialog(view: View) {
        val dialog = AlertDialog.Builder(this, R.style.PolarTheme)
        dialog.setTitle("Enter your Polar device's ID")
        val viewInflated = LayoutInflater.from(applicationContext).inflate(R.layout.device_id_dialog_layout, view.rootView as ViewGroup, false)
        val input = viewInflated.findViewById<EditText>(R.id.input)
        if (deviceId?.isNotEmpty() == true) input.setText(deviceId)
        input.inputType = InputType.TYPE_CLASS_TEXT
        dialog.setView(viewInflated)
        dialog.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
            deviceId = input.text.toString().uppercase()
            val editor = sharedPreferences.edit()
            editor.putString(SHARED_PREFS_KEY, deviceId)
            editor.apply()
        }
        dialog.setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int -> dialogInterface.cancel() }
        dialog.show()
    }

    private fun checkBT() {
        val btManager = applicationContext.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter: BluetoothAdapter? = btManager.adapter
        if (bluetoothAdapter == null) {
            showToast("Device doesn't support Bluetooth")
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            bluetoothOnActivityResultLauncher.launch(enableBtIntent)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT), PERMISSION_REQUEST_CODE)
            } else {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
            }
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (index in 0..grantResults.lastIndex) {
                if (grantResults[index] == PackageManager.PERMISSION_DENIED) {
                    Log.w(TAG, "Needed permissions are missing")
                    showToast("Needed permissions are missing")
                    return
                }
            }
            Log.d(TAG, "Needed permissions are granted")
        }
    }

    private fun showToast(message: String) {
        val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_LONG)
        toast.show()
    }

    private fun getMockedHeartRate(): Int {
        // Replace this with actual sensor reading logic
        return (60..120).random() // Generates a random heart rate between 60 and 120 for testing
    }


}

