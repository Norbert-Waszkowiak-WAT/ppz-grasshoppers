package woli.grasshoppers.apppub

import android.annotation.SuppressLint
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
import android.widget.SeekBar
import android.widget.ImageButton
import com.google.android.material.bottomsheet.BottomSheetDialog

//TODO: clear the code

class MainActivity : AppCompatActivity() {

    private val sharedPreferences by lazy {
        getSharedPreferences("streak_prefs", Context.MODE_PRIVATE)
    }

    private val knifePreferences by lazy {
        getSharedPreferences("knife_preferences", Context.MODE_PRIVATE)
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
    private lateinit var diffLevelBtn: ImageButton

    private var knifeDiff: Int = 0
    private var snakeDiff: Int = 0
    private var birdDiff: Int = 0
    private var pacmanDiff: Int = 0

    private var knifeApples: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        knifeApples = knifePreferences.getInt("apple_amount", 0)

        initUIElements()

        handleStreak()

        handleJokes()

        setOnClickListeners()
    }

    @SuppressLint("SetTextI18n")
    private fun handleJokes() {
        jokeTxt.text = "Lołding jor dżołk"
        if (isNetworkAvailable()) {
            fetchRandomJoke()
        } else {
            showJoke("No internet connection. Please check your network settings.")
        }
    }

    @SuppressLint("SetTextI18n")
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
        pacmanBestTxt = findViewById(R.id.pacmanScoreTextView)
        diffLevelBtn = findViewById(R.id.diffLevelBtn)

        knifeBestTxt.text = sharedPreferences.getInt("knife_best_score", 0).toString()
        snakeBestTxt.text = sharedPreferences.getInt("snake_best_score", 0).toString()
        birdBestTxt.text = sharedPreferences.getInt("bird_best_score", 0).toString()
        pacmanBestTxt.text = sharedPreferences.getInt("pacman_best_score", 0).toString()

        jokeTxt.text = "Lołding jor dżołk"

        knifeDiff = sharedPreferences.getInt("knife_diff_level", 50)
        snakeDiff = sharedPreferences.getInt("snake_diff_level", 50)
        birdDiff = sharedPreferences.getInt("bird_diff_level", 50)
        pacmanDiff = sharedPreferences.getInt("pacman_diff_level", 50)
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

    @SuppressLint("SetTextI18n")
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

    @Suppress("DEPRECATION")
    private fun setOnClickListeners() {
        birdButton.setOnClickListener {
            val intent = Intent(this, BirdActivity::class.java)
            intent.putExtra("bird_diff", birdDiff)
            startActivityForResult(intent, 0)
        }

        knifeButton.setOnClickListener {
            val intent = Intent(this, KnifeActivity::class.java)
            intent.putExtra("knife_diff", knifeDiff)
            intent.putExtra("apple_amount", knifeApples)
            intent.putExtra("best_score", knifeBestTxt.text.toString().toInt())
            startActivityForResult(intent, 1)
        }

        snakeButton.setOnClickListener {
            val intent = Intent(this, SnakeActivity::class.java)
            intent.putExtra("snake_diff", snakeDiff)
            startActivityForResult(intent, 2)
        }

        pacmanButton.setOnClickListener {
            val intent = Intent(this, PacmanActivity::class.java)
            intent.putExtra("pacman_diff", pacmanDiff)
            startActivityForResult(intent, 3)
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

        diffLevelBtn.setOnClickListener {
            showBottomSheetMenu()
        }

        jokeTxt.setOnClickListener {
            handleJokes()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    if (birdBestTxt.text.isNullOrEmpty() || birdBestTxt.text.isNullOrBlank() || birdBestTxt.text.toString()
                            .toInt() < data.getStringExtra("score").toString().toInt()
                    ) {
                        birdBestTxt.text = data.getStringExtra("score").toString()
                        sharedPreferences.edit().apply {
                            putInt(
                                "bird_best_score",
                                data.getStringExtra("score").toString().toInt()
                            )
                            apply()
                        }
                    }
                }
            }
        }
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    if (knifeBestTxt.text.isNullOrEmpty() || knifeBestTxt.text.isNullOrBlank() || knifeBestTxt.text.toString()
                            .toInt() < data.getStringExtra("score").toString().toInt()
                    ) {
                        knifeBestTxt.text = data.getStringExtra("score").toString()
                        sharedPreferences.edit().apply {
                            putInt(
                                "knife_best_score",
                                data.getIntExtra("score", 0)
                            )
                            apply()
                        }
                    }
                    knifeApples = data.getIntExtra("apple_amount", 0)
                    knifePreferences.edit().apply {
                        putInt(
                            "apple_amount",
                            knifeApples
                        )
                        apply()
                    }
                }
            }
        }
        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    if (snakeBestTxt.text.isNullOrEmpty() || snakeBestTxt.text.isNullOrBlank() || snakeBestTxt.text.toString()
                            .toInt() < data.getStringExtra("score").toString().toInt()
                    ) {
                        snakeBestTxt.text = data.getStringExtra("score").toString()
                        sharedPreferences.edit().apply {
                            putInt(
                                "snake_best_score",
                                data.getStringExtra("score").toString().toInt()
                            )
                            apply()
                        }
                    }
                }
            }
        }
        if (requestCode == 3) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    if (pacmanBestTxt.text.isNullOrEmpty() || pacmanBestTxt.text.isNullOrBlank() || pacmanBestTxt.text.toString()
                            .toInt() < data.getStringExtra("score").toString().toInt()
                    ) {
                        pacmanBestTxt.text = data.getStringExtra("score").toString()
                        sharedPreferences.edit().apply {
                            putInt(
                                "pacman_best_score",
                                data.getStringExtra("score").toString().toInt()
                            )
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
        calendar.time = last as Date
        calendar.add(Calendar.DAY_OF_YEAR, 1)

        return current == calendar.time
    }

    private fun isSameDay(lastDate: String, currentDate: String): Boolean {
        return lastDate == currentDate
    }

    @Suppress("DEPRECATION")
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
                        val randomJoke =
                            gson.fromJson(responseBody.string(), RandomJoke::class.java)
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

    @Suppress("SameParameterValue")
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showJoke(joke: String) {
        jokeTxt.text = joke
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork?.let {
            connectivityManager.getNetworkCapabilities(it)
        }
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    @SuppressLint("InflateParams")
    private fun showBottomSheetMenu() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_menu, null)

        val seekBar1 = view.findViewById<SeekBar>(R.id.seekBar1)
        val seekBar2 = view.findViewById<SeekBar>(R.id.seekBar2)
        val seekBar3 = view.findViewById<SeekBar>(R.id.seekBar3)
        val seekBar4 = view.findViewById<SeekBar>(R.id.seekBar4)

        seekBar1.progress = knifeDiff
        seekBar2.progress = snakeDiff
        seekBar3.progress = birdDiff
        seekBar4.progress = pacmanDiff

        seekBar1.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                knifeDiff = seekBar1.progress
                sharedPreferences.edit().apply {
                    putInt("knife_diff_level", knifeDiff)
                    apply()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                snakeDiff = seekBar2.progress
                sharedPreferences.edit().apply {
                    putInt("snake_diff_level", snakeDiff)
                    apply()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBar3.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                birdDiff = seekBar3.progress
                sharedPreferences.edit().apply {
                    putInt("bird_diff_level", birdDiff)
                    apply()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBar4.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                pacmanDiff = seekBar4.progress
                sharedPreferences.edit().apply {
                    putInt("pacman_diff_level", pacmanDiff)
                    apply()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }
}
