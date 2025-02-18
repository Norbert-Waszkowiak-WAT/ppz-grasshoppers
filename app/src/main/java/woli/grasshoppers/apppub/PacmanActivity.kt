package woli.grasshoppers.apppub

import android.animation.ObjectAnimator
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
import java.util.LinkedList
import java.util.Queue
import java.util.Timer
import kotlin.math.abs

interface GhostBehavior {
    fun move(ghostX: Int, ghostY: Int, pacmanX: Int, pacmanY: Int, walls: Array<IntArray>): Pair<Int, Int>
}
class RandomMovement : GhostBehavior {
    override fun move(ghostX: Int, ghostY: Int, pacmanX: Int, pacmanY: Int, walls: Array<IntArray>): Pair<Int, Int> {
        val directions = arrayOf(
            intArrayOf(0, 1),  // Down
            intArrayOf(0, -1), // Up
            intArrayOf(1, 0),  // Right
            intArrayOf(-1, 0)  // Left
        )

        var newX: Int
        var newY: Int
        do {
            val direction = directions.random()
            newX = ghostX + direction[0]
            newY = ghostY + direction[1]
        } while (newX !in 0 until walls[0].size || newY !in 0 until walls.size || walls[newY][newX] != 0)

        return Pair(newX, newY)
    }
}
class ChasePacman : GhostBehavior {
    override fun move(ghostX: Int, ghostY: Int, pacmanX: Int, pacmanY: Int, walls: Array<IntArray>): Pair<Int, Int> {
        val path = bfs(ghostX, ghostY, pacmanX, pacmanY, walls)
        if (path.size > 1) {
            return path[1]
        }
        return Pair(ghostX, ghostY)
    }

    private fun bfs(startX: Int, startY: Int, targetX: Int, targetY: Int, walls: Array<IntArray>): List<Pair<Int, Int>> {
        val queue: Queue<Pair<Int, Int>> = LinkedList()
        val visited = Array(walls.size) { BooleanArray(walls[0].size) }
        val parent = mutableMapOf<Pair<Int, Int>, Pair<Int, Int>?>()

        queue.add(Pair(startX, startY))
        visited[startY][startX] = true
        parent[Pair(startX, startY)] = null

        val directions = arrayOf(
            intArrayOf(0, 1),  // Down
            intArrayOf(0, -1), // Up
            intArrayOf(1, 0),  // Right
            intArrayOf(-1, 0)  // Left
        )

        while (queue.isNotEmpty()) {
            val (x, y) = queue.remove()

            if (x == targetX && y == targetY) {
                val path = mutableListOf<Pair<Int, Int>>()
                var current: Pair<Int, Int>? = Pair(x, y)
                while (current != null) {
                    path.add(current)
                    current = parent[current]
                }
                path.reverse()
                return path
            }

            for (direction in directions) {
                var newX = x + direction[0]
                var newY = y + direction[1]

                // Handle teleportation
                newX = if (newX < 0) walls[0].size - 1 else if (newX >= walls[0].size) 0 else newX
                newY = if (newY < 0) walls.size - 1 else if (newY >= walls.size) 0 else newY

                if (!visited[newY][newX] && walls[newY][newX] == 0) {
                    queue.add(Pair(newX, newY))
                    visited[newY][newX] = true
                    parent[Pair(newX, newY)] = Pair(x, y)
                }
            }
        }

        return emptyList()
    }
}


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

    private val movementDuration = 400L
    private val ghostMovementDuration = 600L

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

    private lateinit var ghostViews: Array<ImageView>
    private var ghostPositions = arrayOf(
        intArrayOf(1, 5),
        intArrayOf(5, 1)
    )
    private lateinit var ghostBehaviors: Array<GhostBehavior>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pacman)

        hideSystemBars()
        val diffLevel = getDiff()

        backgroundView = findViewById(R.id.pacmanBackground)
        pacmanView = findViewById(R.id.pacman)
        gameBoard = findViewById(R.id.pacmanGameBoard)

        ghostViews = arrayOf(
            findViewById(R.id.pacmanGhost0),
            findViewById(R.id.pacmanGhost1)
        )
        ghostBehaviors = arrayOf(
            RandomMovement(),
            ChasePacman()
        )

        setupSwipeDetection()

        gameBoard.post {
            pacmanView.x = 0f
            pacmanView.y = 0f

            gridX = gameBoard.x
            gridY = gameBoard.y
            gridWidth = gameBoard.width
            gridHeight = gameBoard.height

            pacmanMoveTo(1, 1)

            for (i in ghostPositions.indices) {
                ghostViews[i].x = ghostPositions[i][0] * (gridWidth / gridCount) + gridX
                ghostViews[i].y = ghostPositions[i][1] * (gridHeight / gridCount) + gridY
            }
        }

        val ghostMoveTimer = Timer()
        ghostMoveTimer.schedule(object : java.util.TimerTask() {
            override fun run() {
                moveGhosts()
            }
        }, 0, ghostMovementDuration)
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
        pacmanMoveUntil(1, 0)
    }

    private fun onSwipeLeft() {
        pacmanMoveUntil(-1, 0)
    }

    private fun onSwipeUp() {
        pacmanMoveUntil(0, -1)
    }

    private fun onSwipeDown() {
        pacmanMoveUntil(0, 1)
    }

    private fun pacmanMoveTo(x: Int, y: Int) {
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

    private fun pacmanMoveUntil(x: Int, y: Int) {
        if (pacmanX + x < 0 || pacmanX + x >= walls[0].size || pacmanY + y < 0 || pacmanY + y >= walls.size){
            return
        }
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
                    pacmanMoveTo(-1,pacmanY)
                    pacmanX = 7
                    pacmanMoveTo(6, pacmanY)
                    return
                }
                if ((pacmanX == 6 && x == 1)){
                    pacmanMoveTo(7,pacmanY)
                    pacmanX = -1
                    pacmanMoveTo(0, pacmanY)
                    return
                }

                if (walls[pacmanY+y][pacmanX+x] == 1){
                    pacmanMoveTimer.cancel()
                    return
                }

                runOnUiThread {
                    pacmanMoveTo(pacmanX + x, pacmanY + y)
                }
            }
        }, 0, movementDuration)
    }

    private fun moveGhosts() {
        for (i in ghostPositions.indices) {
            val (newX, newY) = ghostBehaviors[i].move(ghostPositions[i][0], ghostPositions[i][1], pacmanX, pacmanY, walls)
            if (newX != ghostPositions[i][0] || newY != ghostPositions[i][1]) {
                val directionX = newX - ghostPositions[i][0]
                val directionY = newY - ghostPositions[i][1]

                val rotation = when {
                    directionX > 0 -> 0f    // Right
                    directionX < 0 -> 180f  // Left
                    directionY > 0 -> 90f   // Down
                    directionY < 0 -> 270f  // Up
                    else -> ghostViews[i].rotation
                }

                ghostPositions[i][0] = newX
                ghostPositions[i][1] = newY

                val actualX = newX * (gridWidth / gridCount) + gridX
                val actualY = newY * (gridHeight / gridCount) + gridY

                runOnUiThread {
                    val xAnimator = ObjectAnimator.ofFloat(ghostViews[i], "translationX", ghostViews[i].x, actualX)
                    val yAnimator = ObjectAnimator.ofFloat(ghostViews[i], "translationY", ghostViews[i].y, actualY)

                    xAnimator.interpolator = android.view.animation.LinearInterpolator()
                    yAnimator.interpolator = android.view.animation.LinearInterpolator()
                    xAnimator.duration = ghostMovementDuration
                    yAnimator.duration = ghostMovementDuration

                    xAnimator.start()
                    yAnimator.start()
                    ghostViews[i].rotation = rotation
                }
            }
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