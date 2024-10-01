package woli.grasshoppers.apppub

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val sharedPreferences by lazy {
        getSharedPreferences("streak_prefs", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val birdButton = findViewById<Button>(R.id.buttonBird)
        val knifeButton = findViewById<Button>(R.id.buttonKnife)
        val snakeButton = findViewById<Button>(R.id.buttonSnake)
        val pacmanButton = findViewById<Button>(R.id.buttonPacman)
        val streakImg = findViewById<ImageView>(R.id.streakImageView)
        val streakTxt = findViewById<TextView>(R.id.streakTextView)
        val getPlayingTxt = findViewById<TextView>(R.id.getPlayingTextView)

        val currentDate = getCurrentDate()

        val lastLaunchDate = sharedPreferences.getString("last_launch_date", null)
        var streakCount = sharedPreferences.getInt("streak_count", 0)

        if (lastLaunchDate != null) {
            val streakContinues = isNextDay(lastLaunchDate, currentDate)

            if (streakContinues) {
                streakCount++
            } else if (!isSameDay(lastLaunchDate, currentDate)) {
                streakCount = 0
            }

            streakImg.visibility = ImageView.VISIBLE
            streakTxt.visibility = TextView.VISIBLE
            streakTxt.text = "$streakCount day streak"

            if (streakCount == 0 || streakCount == 1) {
                getPlayingTxt.visibility = TextView.VISIBLE
            } else {
                getPlayingTxt.visibility = TextView.GONE
            }

        } else {
            streakCount = 1
            streakImg.visibility = ImageView.VISIBLE
            streakTxt.visibility = TextView.VISIBLE
            streakTxt.text = "$streakCount day streak"
            getPlayingTxt.visibility = TextView.VISIBLE
        }

        sharedPreferences.edit().apply {
            putString("last_launch_date", currentDate)
            putInt("streak_count", streakCount)
            apply()
        }

        hideSystemBars()

        birdButton.setOnClickListener {
            val intent = Intent(this, BirdActivity::class.java)
            startActivity(intent)
        }

        knifeButton.setOnClickListener {
            val intent = Intent(this, KnifeActivity::class.java)
            startActivity(intent)
        }

        snakeButton.setOnClickListener {
            val intent = Intent(this, SnakeActivity::class.java)
            startActivity(intent)
        }

        pacmanButton.setOnClickListener {
            val intent = Intent(this, PacmanActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun isNextDay(lastDate: String, currentDate: String): Boolean {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val last = dateFormat.parse(lastDate)
        val current = dateFormat.parse(currentDate)

        val calendar = Calendar.getInstance()
        calendar.time = last
        calendar.add(Calendar.DAY_OF_YEAR, 1)

        return current == calendar.time
    }

    private fun isSameDay(lastDate: String, currentDate: String): Boolean {
        return lastDate == currentDate
    }

    private fun hideSystemBars() {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
        )
    }
}
