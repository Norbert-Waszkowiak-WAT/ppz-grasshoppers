package woli.grasshoppers.apppub

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.TouchDelegate
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs

class PacmanActivity : AppCompatActivity(){
    private lateinit var pacmanView: ImageView
    private lateinit var backgroundView: FrameLayout
    private lateinit var gameBoard: ImageView

    private lateinit var gestureDetector: GestureDetector

    private var gridX = 0f
    private var gridY = 0f
    private var gridWidth = 0
    private var gridHeight = 0
    private val gridCount = 20

    private var pacmanX = 0
    private var pacmanY = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pacman)

        hideSystemBars()
        val diffLevel = getDiff()

        backgroundView = findViewById(R.id.pacmanBackground)
        pacmanView = findViewById(R.id.pacman)
        gameBoard = findViewById(R.id.pacmanGameBoard)

        setupSwipeDetection()

        gridX = gameBoard.x
        gridY = gameBoard.y
        gameBoard.post {
            gridWidth = gameBoard.width
            gridHeight = gameBoard.height

            moveTo(0, 0)
        }
    }

    private fun setupSwipeDetection(){
        val delegate = TouchDelegate(
            Rect(0, 0, backgroundView.width, backgroundView.height),
            backgroundView
        )

        pacmanView.touchDelegate = delegate
        gameBoard.touchDelegate = delegate


        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(event: MotionEvent): Boolean {
                return true
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                // Detect swipe direction
                if (e1 != null && e2 != null) {
                    val diffX = e2.x - e1.x
                    val diffY = e2.y - e1.y

                    if (abs(diffX) > abs(diffY)) { // Horizontal swipe
                        if (diffX > 0) {
                            // Swipe Right
                            onSwipeRight()
                        } else {
                            // Swipe Left
                            onSwipeLeft()
                        }
                    } else { // Vertical swipe
                        if (diffY > 0) {
                            // Swipe Down
                            onSwipeDown()
                        } else {
                            // Swipe Up
                            onSwipeUp()
                        }
                    }
                }
                return true
            }
        })
        backgroundView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }
    }



    private fun onSwipeRight() {
        moveTo(pacmanX + 1, pacmanY)
    }

    private fun onSwipeLeft() {
        moveTo(pacmanX - 1, pacmanY)
    }

    private fun onSwipeUp() {
        moveTo(pacmanX, pacmanY - 1)
    }

    private fun onSwipeDown() {
        moveTo(pacmanX, pacmanY + 1)
    }


    private fun moveTo(x: Int, y: Int) {
        var actualX = x * (gridWidth / gridCount) + gridX
        var actualY = y * (gridHeight / gridCount) + gridY

        pacmanX = x
        pacmanY = y

        runOnUiThread {
            pacmanView.x = actualX
            pacmanView.y = actualY
        }
    }






    override fun onBackPressed() {
        passScore(12)//TODO: real score value
        super.onBackPressed()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
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

    private fun passScore(score: Int) {
        val data = Intent()
        data.putExtra("score", score.toString())
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    private fun getDiff(): Int {
        return intent.getIntExtra("pacman_diff", 50)
    }
}