package woli.grasshoppers.apppub

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.TouchDelegate
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.LinkedList
import java.util.Queue
import java.util.Timer
import kotlin.math.abs

interface GhostBehavior {
    fun move(ghostX: Int, ghostY: Int, pacmanX: Int, pacmanY: Int, walls: Array<IntArray>, ghostPositions: Array<IntArray>): Pair<Int, Int>
}
class RandomMovement : GhostBehavior {
    override fun move(ghostX: Int, ghostY: Int, pacmanX: Int, pacmanY: Int, walls: Array<IntArray>, ghostPositions: Array<IntArray>): Pair<Int, Int> {
        val directions = arrayOf(
            intArrayOf(0, 1),  // Down
            intArrayOf(0, -1), // Up
            intArrayOf(1, 0),  // Right
            intArrayOf(-1, 0)  // Left
        )

        var newX: Int
        var newY: Int
        var isOverlapping: Boolean
        do {
            val direction = directions.random()
            newX = ghostX + direction[0]
            newY = ghostY + direction[1]

            isOverlapping = false
            ghostPositions.forEach { ghostPos ->
                if (ghostPos[0] == newX && ghostPos[1] == newY) {
                    isOverlapping = true
                }
            }
        } while (newX !in 0 until walls[0].size || newY !in 0 until walls.size || walls[newY][newX] != 0 || isOverlapping)

        return Pair(newX, newY)
    }
}
class ChasePacman : GhostBehavior {
    override fun move(ghostX: Int, ghostY: Int, pacmanX: Int, pacmanY: Int, walls: Array<IntArray>, ghostPositions: Array<IntArray>): Pair<Int, Int> {
        val path = bfs(ghostX, ghostY, pacmanX, pacmanY, walls)
        if (path.size <= 1) {
            return Pair(ghostX, ghostY)
        }
        ghostPositions.forEach { ghostPos ->
            if (ghostPos[0] == path[1].first && ghostPos[1] == path[1].second) {
                return Pair(ghostX, ghostY)
            }
        }
        return path[1]
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

                if (newY in walls.indices && newX in walls[0].indices && !visited[newY][newX] && walls[newY][newX] != 1) {
                    queue.add(Pair(newX, newY))
                    visited[newY][newX] = true
                    parent[Pair(newX, newY)] = Pair(x, y)
                }
            }
        }

        return emptyList()
    }
}
class RandomChase(chaseChances: Int) : GhostBehavior {
    private var directions: Array<Pair<Int,Int>> = arrayOf(
        Pair(0, 1),  // Down
        Pair(0, -1), // Up
        Pair(1, 0),  // Right
        Pair(-1, 0)  // Left
    )

    init {
        for (i in 0 until chaseChances) {
            val direction = arrayOf(0,1).random()
            directions += Pair(direction, direction)
        }

    }

    override fun move(ghostX: Int, ghostY: Int, pacmanX: Int, pacmanY: Int, walls: Array<IntArray>, ghostPositions: Array<IntArray>): Pair<Int, Int> {
        var newX: Int
        var newY: Int
        var isOverlapping: Boolean
        var overlappingCycles = 0
        do {
            val direction = directions.random()
            val path = bfs(ghostX, ghostY, pacmanX, pacmanY, walls)

            if (direction.first == 0 && direction.second == 0 && path.size > 1) {
                newX = path[1].first
                newY = path[1].second
            }
            else if (direction.first == 1 && direction.second == 1) {
                var p = bfs(ghostX, ghostY, pacmanX+1, pacmanY, walls)
                if (p.size <= 1) {
                    p = bfs(ghostX, ghostY, pacmanX-1, pacmanY, walls)
                }
                if (p.size <= 1) {
                    p = bfs(ghostX, ghostY, pacmanX, pacmanY+1, walls)
                }
                if (p.size <= 1) {
                    p = bfs(ghostX, ghostY, pacmanX, pacmanY-1, walls)
                }
                if (p.size > 1) {
                    newX = p[1].first
                    newY = p[1].second
                } else {
                    newX = ghostX
                    newY = ghostY
                }
            }
            else {
                newX = ghostX + direction.first
                newY = ghostY + direction.second
            }

            isOverlapping = false
            ghostPositions.forEach { ghostPos ->
                if (ghostPos[0] == newX && ghostPos[1] == newY) {
                    isOverlapping = true
                }
            }
            if (isOverlapping){
                overlappingCycles++
                if (overlappingCycles > 10) {
                    return Pair(ghostX, ghostY)
                }
            } else {
                overlappingCycles = 0
            }
        } while (newX !in walls[0].indices || newY !in walls.indices || walls[newY][newX] == 1 || isOverlapping)

        return Pair(newX, newY)
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

                if (newY in walls.indices && newX in walls[0].indices && !visited[newY][newX] && walls[newY][newX] != 1) {
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
    private lateinit var scoreView: TextView

    private lateinit var gestureDetector: GestureDetector

    private var score = 0

    private var gridX = 0f
    private var gridY = 0f
    private var gridWidth = 0
    private var gridHeight = 0
    private val gridCountX = 28
    private val gridCountY = 31

    private val movementDuration = 300L
    private val ghostMovementDuration = 280L

    private var pacmanX = 0
    private var pacmanY = 0
    private var pacmanMoveTimer: Timer = Timer()

    private var walls = arrayOf(
        intArrayOf(1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1),
        intArrayOf(1,3,3,3,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,3,3,3,1),
        intArrayOf(1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1),
        intArrayOf(1,4,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,4,1),
        intArrayOf(1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1),
        intArrayOf(1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1),
        intArrayOf(1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1),
        intArrayOf(1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1),
        intArrayOf(1,3,3,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,3,3,1),
        intArrayOf(1,1,1,1,1,1,3,1,1,1,1,1,0,1,1,0,1,1,1,1,1,3,1,1,1,1,1,1),
        intArrayOf(1,1,1,1,1,1,3,1,1,1,1,1,0,1,1,0,1,1,1,1,1,3,1,1,1,1,1,1),
        intArrayOf(1,1,1,1,1,1,3,1,1,0,0,0,0,0,0,0,0,0,0,1,1,3,1,1,1,1,1,1),
        intArrayOf(1,1,1,1,1,1,3,1,1,0,1,1,1,2,2,1,1,1,0,1,1,3,1,1,1,1,1,1),
        intArrayOf(1,1,1,1,1,1,3,1,1,0,1,2,2,2,2,2,2,1,0,1,1,3,1,1,1,1,1,1),
        intArrayOf(0,0,0,0,0,0,3,0,0,0,1,2,2,2,2,2,2,1,0,0,0,3,0,0,0,0,0,0),
        intArrayOf(1,1,1,1,1,1,3,1,1,0,1,2,2,2,2,2,2,1,0,1,1,3,1,1,1,1,1,1),
        intArrayOf(1,1,1,1,1,1,3,1,1,0,1,1,1,1,1,1,1,1,0,1,1,3,1,1,1,1,1,1),
        intArrayOf(1,1,1,1,1,1,3,1,1,0,0,0,0,0,0,0,0,0,0,1,1,3,1,1,1,1,1,1),
        intArrayOf(1,1,1,1,1,1,3,1,1,0,1,1,1,1,1,1,1,1,0,1,1,3,1,1,1,1,1,1),
        intArrayOf(1,1,1,1,1,1,3,1,1,0,1,1,1,1,1,1,1,1,0,1,1,3,1,1,1,1,1,1),
        intArrayOf(1,3,3,3,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,3,3,3,1),
        intArrayOf(1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1),
        intArrayOf(1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1),
        intArrayOf(1,4,3,3,1,1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1,1,3,3,4,1),
        intArrayOf(1,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,1),
        intArrayOf(1,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,1),
        intArrayOf(1,3,3,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,3,3,1),
        intArrayOf(1,3,1,1,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,1,1,3,1),
        intArrayOf(1,3,1,1,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,1,1,3,1),
        intArrayOf(1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1),
        intArrayOf(1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1),
    )

    private lateinit var dotViews: Array<Array<View>>
    private lateinit var energizerViews: Array<Array<View>>

    private lateinit var ghostViews: Array<ImageView>
    private var ghostPositions = arrayOf(
        intArrayOf(13, 14),
        intArrayOf(11, 14),
        intArrayOf(15, 14)
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
        scoreView = findViewById(R.id.pacmanScore)

        ghostViews = arrayOf(
            findViewById(R.id.pacmanGhost0),
            findViewById(R.id.pacmanGhost1),
            findViewById(R.id.pacmanGhost2)
        )
        ghostBehaviors = arrayOf(
            RandomChase(50),
            RandomChase(5),
            RandomChase(10)
        )

        dotViews = Array(walls.size) { Array<View>(walls[0].size) { View(this) } }
        energizerViews = Array(walls.size) { Array<View>(walls[0].size) { View(this) } }

        setupSwipeDetection()

        gameBoard.post {
            pacmanView.x = 0f
            pacmanView.y = 0f

            gridX = gameBoard.x
            gridY = gameBoard.y
            gridWidth = gameBoard.width
            gridHeight = gameBoard.height

            pacmanMoveTo(13, 23)

            for (i in ghostPositions.indices) {
                ghostViews[i].x = ghostPositions[i][0] * (gridWidth / gridCountX) + gridX
                ghostViews[i].y = ghostPositions[i][1] * (gridHeight / gridCountY) + gridY
            }

            placeDotsAndEnergizers()
        }

        val ghostMoveTimer = Timer()
        ghostMoveTimer.schedule(object : java.util.TimerTask() {
            override fun run() {
                moveGhosts()
            }
        }, 100, ghostMovementDuration)
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
                if (e1 != null && e2 != null) {
                    val diffX = e2.x - e1.x
                    val diffY = e2.y - e1.y

                    if (abs(diffX) > abs(diffY)) {
                        if (diffX > 0) {
                            onSwipeRight()
                        } else {
                            onSwipeLeft()
                        }
                    } else {
                        if (diffY > 0) {
                            onSwipeDown()
                        } else {
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

    fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
    }

    private fun placeDotsAndEnergizers(){
        for (i in walls.indices) {
            for (j in walls[i].indices) {
                if (walls[i][j] == 4) {
                    val params = FrameLayout.LayoutParams(dpToPx(25f).toInt(), dpToPx(25f).toInt())
                    val energizer = LayoutInflater.from(this).inflate(R.layout.pacman_dot, null)

                    backgroundView.addView(energizer, params)
                    energizerViews[i][j] = energizer

                    energizer.x = j * (gridWidth / gridCountX) + gridX
                    energizer.y = i * (gridHeight / gridCountY) + gridY
                }
                if (walls[i][j] == 3) {
                    val params = FrameLayout.LayoutParams(dpToPx(10f).toInt(), dpToPx(10f).toInt())
                    val dot = LayoutInflater.from(this).inflate(R.layout.pacman_dot, null)

                    backgroundView.addView(dot, params)
                    dotViews[i][j] = dot

                    dot.x = j * (gridWidth / gridCountX) + gridX
                    dot.y = i * (gridHeight / gridCountY) + gridY
                    dot.findViewById<ImageView>(R.id.pacmanDot).setImageResource(R.drawable.baseline_settings_24)
                }
            }
        }
    }

    private fun eatDotOrEnergizer(x: Int, y: Int) {
        if (walls[y][x] == 3) {
            walls[y][x] = 0
            backgroundView.removeView(dotViews[y][x])
            dotViews[y][x] = View(this)

            score += 10
        }
        else if (walls[y][x] == 4) {
            walls[y][x] = 0
            backgroundView.removeView(energizerViews[y][x])
            energizerViews[y][x] = View(this)

            score += 50
        }

        scoreView.text = "Score: $score"
    }

    private fun pacmanMoveTo(x: Int, y: Int) {
        var actualX = x * (gridWidth / gridCountX) + gridX
        var actualY = y * (gridHeight / gridCountY) + gridY
        val currentActualX = pacmanX * (gridWidth / gridCountX) + gridX
        val currentActualY = pacmanY * (gridHeight / gridCountY) + gridY

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

        if (pacmanX in walls[0].indices && pacmanY in walls.indices) {
            eatDotOrEnergizer(pacmanX, pacmanY)
        }

        runOnUiThread {
            xAnimator.start()
            yAnimator.start()
        }
    }

    private fun pacmanMoveUntil(x: Int, y: Int) {
        if (pacmanX + x < 0 || pacmanX + x >= walls[0].size || pacmanY + y < 0 || pacmanY + y >= walls.size){
            return
        }
        if (walls[pacmanY+y][pacmanX+x] == 1 || walls[pacmanY+y][pacmanX+x] == 2){
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
                    pacmanX = 28
                    pacmanMoveTo(27, pacmanY)
                    return
                }
                if ((pacmanX == 27 && x == 1)){
                    pacmanMoveTo(28,pacmanY)
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
            val (newX, newY) = ghostBehaviors[i].move(ghostPositions[i][0], ghostPositions[i][1], pacmanX, pacmanY, walls, ghostPositions)
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

                val actualX = newX * (gridWidth / gridCountX) + gridX
                val actualY = newY * (gridHeight / gridCountY) + gridY

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
        passScore(score)
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