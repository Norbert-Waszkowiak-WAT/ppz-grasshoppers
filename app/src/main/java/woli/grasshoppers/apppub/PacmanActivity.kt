package woli.grasshoppers.apppub

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.TouchDelegate
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.util.Timer
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

    private val movementDuration = 200L

    private var pacmanX = 0
    private var pacmanY = 0
    private var pacmanMoveTimer: Timer = Timer()

    private val walls = arrayOf(
        intArrayOf(1,1,1,1,1,1,1),
        intArrayOf(1,0,0,0,1,0,1),
        intArrayOf(1,0,1,1,1,0,1),
        intArrayOf(0,0,0,0,0,0,0),
        intArrayOf(1,0,1,1,1,1,1),
        intArrayOf(1,0,0,0,0,1,1),
        intArrayOf(1,1,1,1,1,1,1)
        )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pacman)

        hideSystemBars()
        val diffLevel = getDiff()

        backgroundView = findViewById(R.id.pacmanBackground)
        pacmanView = findViewById(R.id.pacman)
        gameBoard = findViewById(R.id.pacmanGameBoard)

        setupSwipeDetection()

        gameBoard.post {
            pacmanView.x = 0f
            pacmanView.y = 0f

            gridX = gameBoard.x
            gridY = gameBoard.y
            gridWidth = gameBoard.width
            gridHeight = gameBoard.height

            moveTo(1, 1)
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
        moveUntil(1, 0)
    }

    private fun onSwipeLeft() {
        moveUntil(-1, 0)
    }

    private fun onSwipeUp() {
        moveUntil(0, -1)
    }

    private fun onSwipeDown() {
        moveUntil(0, 1)
    }


    private fun moveTo(x: Int, y: Int) {
        var actualX = x * (gridWidth / gridCount) + gridX
        var actualY = y * (gridHeight / gridCount) + gridY
        val currentActualX = pacmanX * (gridWidth / gridCount) + gridX
        val currentActualY = pacmanY * (gridHeight / gridCount) + gridY

        var xAnimator = ObjectAnimator.ofFloat(pacmanView, "translationX",
            currentActualX, actualX)
        var yAnimator = ObjectAnimator.ofFloat(pacmanView, "translationY",
            currentActualY, actualY)

        xAnimator.interpolator = android.view.animation.LinearInterpolator()
        yAnimator.interpolator = android.view.animation.LinearInterpolator()
        xAnimator.duration = movementDuration
        yAnimator.duration = movementDuration

        pacmanX = x
        pacmanY = y

        runOnUiThread {
            xAnimator.start()
            yAnimator.start()
        }
    }

    private fun moveUntil(x: Int, y: Int) {
        if (walls[pacmanY+y][pacmanX+x] == 1){
            return
        }
        if (x == 1) pacmanView.rotation = 0f
        if (x == -1) pacmanView.rotation = 180f
        if (y == 1) pacmanView.rotation = 90f
        if (y == -1) pacmanView.rotation = 270f

        pacmanMoveTimer.cancel()
        pacmanMoveTimer = Timer()
        pacmanMoveTimer.schedule(object : java.util.TimerTask() {
            override fun run() {
                if ((pacmanX == 0 && x == -1)){
                    moveTo(-1,pacmanY)
                    pacmanX = 7
                    moveTo(6, pacmanY)
                    return
                }
                if ((pacmanX == 6 && x == 1)){
                    moveTo(7,pacmanY)
                    pacmanX = -1
                    moveTo(0, pacmanY)
                    return
                }

                if (walls[pacmanY+y][pacmanX+x] == 1){
                    pacmanMoveTimer.cancel()
                    return
                }

                runOnUiThread {
                    moveTo(pacmanX + x, pacmanY + y)
                }
            }
        }, 0, movementDuration)
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