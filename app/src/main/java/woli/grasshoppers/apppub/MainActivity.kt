package woli.grasshoppers.apppub

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val sharedPreferences by lazy {
        getSharedPreferences("streak_prefs", Context.MODE_PRIVATE)
    }

    private val client = OkHttpClient()
    private val gson = Gson()

    private lateinit var birdButton: Button
    private lateinit var knifeButton: Button
    private lateinit var snakeButton: Button
    private lateinit var pacmanButton: Button
    private lateinit var streakImg: ImageView
    private lateinit var streakTxt: TextView
    private lateinit var getPlayingTxt: TextView
    private lateinit var jokeTxt: TextView
    private lateinit var knifeBestTxt: TextView
    private lateinit var snakeBestTxt: TextView
    private lateinit var birdBestTxt: TextView
    private lateinit var pacmanBestTxt: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initUIElements()

        handleStreak()

        handleJokes()

        setOnClickListeners()
    }

    private fun handleJokes() {
        if (isNetworkAvailable()) {
            fetchRandomJoke()
        } else {
            showJoke("No internet connection. Please check your network settings.")
        }
    }

    private fun initUIElements() {
        birdButton = findViewById(R.id.buttonBird)
        knifeButton = findViewById(R.id.buttonKnife)
        snakeButton = findViewById(R.id.buttonSnake)
        pacmanButton = findViewById(R.id.buttonPacman)
        streakImg = findViewById(R.id.streakImageView)
        streakTxt = findViewById(R.id.streakTextView)
        getPlayingTxt = findViewById(R.id.getPlayingTextView)
        jokeTxt = findViewById(R.id.jokeTextView)
        knifeBestTxt = findViewById(R.id.knifeScoreTextView)
        snakeBestTxt = findViewById(R.id.snakeScoreTextView)
        birdBestTxt = findViewById(R.id.birdScoreTextView)
        pacmanBestTxt= findViewById(R.id.pacmanScoreTextView)

        knifeBestTxt.text = sharedPreferences.getInt("knife_best_score", 0).toString()
        snakeBestTxt.text = sharedPreferences.getInt("snake_best_score", 0).toString()
        birdBestTxt.text = sharedPreferences.getInt("bird_best_score", 0).toString()
        pacmanBestTxt.text = sharedPreferences.getInt("pacman_best_score", 0).toString()
    }

    private fun handleStreak() {
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

            updateStreakUI(streakCount)
        } else {
            streakCount = 1
            updateStreakUI(streakCount)
        }

        sharedPreferences.edit().apply {
            putString("last_launch_date", currentDate)
            putInt("streak_count", streakCount)
            apply()
        }
    }

    private fun updateStreakUI(streakCount: Int) {
        streakImg.visibility = View.VISIBLE
        streakTxt.visibility = View.VISIBLE
        streakTxt.text = "$streakCount day streak"

        getPlayingTxt.visibility = if (streakCount == 0 || streakCount == 1) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun setOnClickListeners() {
        birdButton.setOnClickListener {
            startActivityForResult(Intent(this, BirdActivity::class.java), 0)
        }

        knifeButton.setOnClickListener {
            startActivityForResult(Intent(this, KnifeActivity::class.java), 1)
        }

        snakeButton.setOnClickListener {
            startActivityForResult(Intent(this, SnakeActivity::class.java), 2)
        }

        pacmanButton.setOnClickListener {
            startActivityForResult(Intent(this, PacmanActivity::class.java), 3)
        }

        birdBestTxt.setOnClickListener {
            showToast("That's your best score in this game, you bastard, get back playing!")
        }

        knifeBestTxt.setOnClickListener {
            showToast("That's your best score in this game, you bastard, get back playing!")
        }

        snakeBestTxt.setOnClickListener {
            showToast("That's your best score in this game, you bastard, get back playing!")
        }

        pacmanBestTxt.setOnClickListener {
            showToast("That's your best score in this game, you bastard, get back playing!")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)

            if (requestCode == 0) {
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        if (birdBestTxt.text.isNullOrEmpty() || birdBestTxt.text.isNullOrBlank() || birdBestTxt.text.toString().toInt() < data.getStringExtra("score").toString().toInt()) {
                            birdBestTxt.text = data.getStringExtra("score").toString()
                            sharedPreferences.edit().apply {
                                putInt("bird_best_score", data.getStringExtra("score").toString().toInt())
                                apply()
                            }
                        }
                    }
                }
            }
            if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    if (knifeBestTxt.text.isNullOrEmpty() || knifeBestTxt.text.isNullOrBlank() || knifeBestTxt.text.toString().toInt() < data.getStringExtra("score").toString().toInt()) {
                        knifeBestTxt.text = data.getStringExtra("score").toString()
                        sharedPreferences.edit().apply {
                            putInt("knife_best_score", data.getStringExtra("score").toString().toInt())
                            apply()
                        }
                    }
                }
            }
        }
        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    if (snakeBestTxt.text.isNullOrEmpty() || snakeBestTxt.text.isNullOrBlank() || snakeBestTxt.text.toString().toInt() < data.getStringExtra("score").toString().toInt()) {
                        snakeBestTxt.text = data.getStringExtra("score").toString()
                        sharedPreferences.edit().apply {
                            putInt("snake_best_score", data.getStringExtra("score").toString().toInt())
                            apply()
                        }
                    }
                }
            }
        }
        if (requestCode == 3) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    if (pacmanBestTxt.text.isNullOrEmpty() || pacmanBestTxt.text.isNullOrBlank() || pacmanBestTxt.text.toString().toInt() < data.getStringExtra("score").toString().toInt()) {
                        pacmanBestTxt.text = data.getStringExtra("score").toString()
                        sharedPreferences.edit().apply {
                            putInt("pacman_best_score", data.getStringExtra("score").toString().toInt())
                            apply()
                        }
                    }
                }
            }
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

    private fun fetchRandomJoke() {
        val request = Request.Builder()
            .url("https://official-joke-api.appspot.com/random_joke")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                //showToast("Failed to fetch joke")
                showJoke("Failed to fetch joke")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        //showToast("Failed to fetch joke: ${response.message}")
                        showJoke("Failed to fetch joke: ${response.message}")
                        return
                    }

                    val responseBody = response.body
                    if (responseBody != null) {
                        val randomJoke = gson.fromJson(responseBody.string(), RandomJoke::class.java)
                        runOnUiThread {
                            showJoke(randomJoke)
                        }
                    }
                }
            }
        })
    }

    private fun showJoke(joke: RandomJoke) {
        val jokeText = "${joke.setup}\n${joke.punchline}"
        showJoke(jokeText)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showJoke(joke: String) {
        jokeTxt.text = joke
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork?.let {
            connectivityManager.getNetworkCapabilities(it)
        }
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}
