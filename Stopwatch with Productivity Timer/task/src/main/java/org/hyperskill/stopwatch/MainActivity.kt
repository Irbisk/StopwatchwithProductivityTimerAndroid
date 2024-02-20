package org.hyperskill.stopwatch

import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import org.hyperskill.stopwatch.databinding.ActivityMainBinding
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private val CHANNEL_ID = "org.hyperskill"
    private lateinit var binding: ActivityMainBinding
    private val handler = Handler(Looper.getMainLooper())

    private var isRunning = false
    private lateinit var timer: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var upperLimit = 0
        var notified = false

        createNotificationChannel()
        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        binding.settingsButton.setOnClickListener {
            val contentView = LayoutInflater.from(this).inflate(R.layout.alert_dialog_edit_text, null, false)
            AlertDialog.Builder(this)
                .setTitle("Set upper limit in seconds")
                .setView(contentView)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val editText = contentView.findViewById<EditText>(R.id.upperLimitEditText)
                    upperLimit = editText.text.toString().toInt()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        binding.startButton.setOnClickListener {
            if (!isRunning) {
                isRunning = true
                binding.settingsButton.isEnabled = false
                binding.progressBar.visibility = View.VISIBLE

                timer = object : Runnable {
                    val startTime = System.currentTimeMillis()

                    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                    override fun run() {

                        val seconds = (System.currentTimeMillis() - startTime) / 1000
                        val formatted = "${(seconds / 60).toString().padStart(2, '0')}:${
                            (seconds % 60).toString().padStart(2, '0')
                        }"
                        if (upperLimit in 1 until seconds && !notified) {
                            binding.textView.setTextColor(Color.RED)
                            val notification = getNotificationBuilder().build()
                            notification.flags = Notification.FLAG_INSISTENT or Notification.FLAG_ONLY_ALERT_ONCE

                            notificationManager.notify(393939, notification)
                            Log.i("NOTIFIED","NOTIFIED")
                            notified = true
                        }
                        binding.textView.text = formatted

                        handler.postDelayed(this, 1000)
                        binding.progressBar.indeterminateTintList = getRandomColor()

                    }
                }
                handler.post(timer)
            }
        }


        binding.resetButton.setOnClickListener {
            if (isRunning) {
                handler.removeCallbacks(timer)
                binding.progressBar.visibility = View.INVISIBLE
                binding.textView.setTextColor(Color.GRAY)
                binding.textView.text = "00:00"
                binding.settingsButton.isEnabled = true
                notified = false
                isRunning = false
            }
        }

    }

    fun getRandomColor(): ColorStateList {
        val red = Random.nextInt(255)
        val green = Random.nextInt(255)
        val blue = Random.nextInt(255)
        return ColorStateList.valueOf(Color.rgb(red, green, blue))
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "hypperskill"
            val descriptionText = "task channel"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system.
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getNotificationBuilder(): NotificationCompat.Builder {

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("ATTENTION")
            .setContentText("Upper limit has been reached!")
            .setStyle(NotificationCompat.BigTextStyle())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
    }
}