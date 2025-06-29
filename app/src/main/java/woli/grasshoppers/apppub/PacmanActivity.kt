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
    private var directions: Array<Pair<Int, Int>> = arrayOf(
        Pair(0, 1),  // Down
        Pair(0, -1), // Up
        Pair(1, 0),  // Right
        Pair(-1, 0)  // Left
    )

    init {
        for (i in 0 until chaseChances) {
            val direction = arrayOf(0, 1).random()
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
            } else if (direction.first == 1 && direction.second == 1) {
                var p = bfs(ghostX, ghostY, pacmanX + 1, pacmanY, walls)
                if (p.size <= 1) {
                    p = bfs(ghostX, ghostY, pacmanX - 1, pacmanY, walls)
                }
                if (p.size <= 1) {
                    p = bfs(ghostX, ghostY, pacmanX, pacmanY + 1, walls)
                }
                if (p.size <= 1) {
                    p = bfs(ghostX, ghostY, pacmanX, pacmanY - 1, walls)
                }
                if (p.size > 1) {
                    newX = p[1].first
                    newY = p[1].second
                } else {
                    newX = ghostX
                    newY = ghostY
                }
            } else {
                newX = ghostX + direction.first
                newY = ghostY + direction.second
            }

            isOverlapping = false
            ghostPositions.forEach { ghostPos ->
                if (ghostPos[0] == newX && ghostPos[1] == newY) {
                    isOverlapping = true
                }
            }
            if (isOverlapping) {
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

// Ghost state enum
enum class GhostState {
    NORMAL,
    FRIGHTENED,
    EATEN
}

// Frightened movement: ghosts run away from Pacman
class FrightenedMovement : GhostBehavior {
    override fun move(
        ghostX: Int, ghostY: Int, pacmanX: Int, pacmanY: Int,
        walls: Array<IntArray>, ghostPositions: Array<IntArray>
    ): Pair<Int, Int> {
        val directions = arrayOf(
            intArrayOf(0, 1), intArrayOf(0, -1),
            intArrayOf(1, 0), intArrayOf(-1, 0)
        )
        var maxDist = -1.0
        var bestMove = Pair(ghostX, ghostY)
        for (dir in directions) {
            val nx = ghostX + dir[0]
            val ny = ghostY + dir[1]
            if (ny in walls.indices && nx in walls[0].indices && walls[ny][nx] != 1) {
                // Avoid overlapping with other ghosts
                var isOverlapping = false
                ghostPositions.forEach { ghostPos ->
                    if (ghostPos[0] == nx && ghostPos[1] == ny) isOverlapping = true
                }
                if (isOverlapping) continue
                val dist = Math.hypot((nx - pacmanX).toDouble(), (ny - pacmanY).toDouble())
                if (dist > maxDist) {
                    maxDist = dist
                    bestMove = Pair(nx, ny)
                }
            }
        }
        return bestMove
    }
}

class PacmanActivity : AppCompatActivity() {
    private lateinit var pacmanView: ImageView
    private lateinit var backgroundView: FrameLayout
    private lateinit var gameBoard: ImageView
    private lateinit var scoreView: TextView
    private lateinit var levelView: TextView
    private lateinit var livesView: TextView
    private lateinit var progressView: TextView
    private lateinit var gameoverView: TextView

    private lateinit var gestureDetector: GestureDetector

    private var score = 0

    private var gridX = 0f
    private var gridY = 0f
    private var gridWidth = 0
    private var gridHeight = 0
    private val gridCountX = 28
    private val gridCountY = 31

    private var movementDuration = 250L
    private var ghostMovementDuration = 200L
    private var ghostFrightenedMovementDuration = 400L

    private var pacmanX = 13
    private var pacmanY = 23
    private var pacmanMoveTimer: Timer? = null
    private var pacmanAnimationTimer: Timer? = null

    private var wallsStart = arrayOf(
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1),
        intArrayOf(1, 3, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 3, 1),
        intArrayOf(1, 4, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 4, 1),
        intArrayOf(1, 3, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 3, 1),
        intArrayOf(1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1),
        intArrayOf(1, 3, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 3, 1),
        intArrayOf(1, 3, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 3, 1),
        intArrayOf(1, 3, 3, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 3, 3, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 3, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 3, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 3, 1, 1, 0, 1, 1, 1, 2, 2, 1, 1, 1, 0, 1, 1, 3, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 3, 1, 1, 0, 1, 2, 2, 2, 2, 2, 2, 1, 0, 1, 1, 3, 1, 1, 1, 1, 1, 1),
        intArrayOf(0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 1, 2, 2, 2, 2, 2, 2, 1, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0),
        intArrayOf(1, 1, 1, 1, 1, 1, 3, 1, 1, 0, 1, 2, 2, 2, 2, 2, 2, 1, 0, 1, 1, 3, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 3, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 3, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 3, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 3, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 3, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 3, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 3, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 3, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1),
        intArrayOf(1, 3, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 3, 1),
        intArrayOf(1, 3, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 3, 1),
        intArrayOf(1, 4, 3, 3, 1, 1, 3, 3, 3, 3, 3, 3, 3, 0, 0, 3, 3, 3, 3, 3, 3, 3, 1, 1, 3, 3, 4, 1),
        intArrayOf(1, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 1),
        intArrayOf(1, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 1),
        intArrayOf(1, 3, 3, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 3, 3, 1),
        intArrayOf(1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1),
        intArrayOf(1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1),
        intArrayOf(1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
    )
    private val walls = Array(wallsStart.size) { IntArray(wallsStart[0].size) }

    private lateinit var dotViews: Array<Array<View?>>

    private var eatenDotsCounter = 0
    private var allDots = 0

    private lateinit var ghostViews: Array<ImageView>
    private val ghostStartPositions = arrayOf(
        intArrayOf(12, 14),
        intArrayOf(11, 14),
        intArrayOf(14, 14),
        intArrayOf(13, 14)
    )
    private var ghostPositions = Array(4) { IntArray(2) }

    private val ghostAssets = arrayOf(
        arrayOf(R.drawable.pacman_ghost_red_right, R.drawable.pacman_ghost_red_left, R.drawable.pacman_ghost_red_down, R.drawable.pacman_ghost_red_up),
        arrayOf(R.drawable.pacman_ghost_cyan_right, R.drawable.pacman_ghost_cyan_left, R.drawable.pacman_ghost_cyan_down, R.drawable.pacman_ghost_cyan_up),
        arrayOf(R.drawable.pacman_ghost_pink_right, R.drawable.pacman_ghost_pink_left, R.drawable.pacman_ghost_pink_down, R.drawable.pacman_ghost_pink_up),
        arrayOf(R.drawable.pacman_ghost_yellow_right, R.drawable.pacman_ghost_yellow_left, R.drawable.pacman_ghost_yellow_down, R.drawable.pacman_ghost_yellow_up)
    )
    private lateinit var ghostBehaviors: Array<GhostBehavior>

    // Ghost frightened logic
    private var ghostStates = arrayOf(
        GhostState.NORMAL,
        GhostState.NORMAL,
        GhostState.NORMAL,
        GhostState.NORMAL
    )
    private var frightenedTimer: Timer? = null
    private var frightenedTimeLeft = 0L
    private val frightenedDuration = ghostFrightenedMovementDuration * 25
    private var frightenedBlinkCount = 0

    private var ghostMoveTimer: Timer? = null
    private var ghostFrightenedMoveTimer: Timer? = null

    private var level = 1
    private var lives = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pacman)

        hideSystemBars()
        setDifficulty()

        backgroundView = findViewById(R.id.pacmanBackground)
        pacmanView = findViewById(R.id.pacman)
        gameBoard = findViewById(R.id.pacmanGameBoard)
        scoreView = findViewById(R.id.pacmanScore)
        levelView = findViewById(R.id.pacmanLevel)
        livesView = findViewById(R.id.pacmanLives)
        progressView = findViewById(R.id.pacmanProgress)
        gameoverView = findViewById(R.id.pacmanGameOver)

        ghostViews = arrayOf(
            findViewById(R.id.pacmanGhost0),
            findViewById(R.id.pacmanGhost1),
            findViewById(R.id.pacmanGhost2),
            findViewById(R.id.pacmanGhost3)
        )
        ghostBehaviors = arrayOf(
            RandomChase(50),
            RandomChase(5),
            RandomChase(10),
            RandomChase(20)
        )

        dotViews = Array(walls.size) { Array<View?>(walls[0].size) { null } }

        setupSwipeDetection()

        gameBoard.post {
            gridX = gameBoard.x
            gridY = gameBoard.y
            gridWidth = gameBoard.width
            gridHeight = gameBoard.height

            startGame()
        }

        pacmanAnimationTimer = Timer()
        pacmanAnimationTimer?.schedule(object : java.util.TimerTask() {
            override fun run() {
                runOnUiThread {
                    if (pacmanView.drawable == null) {
                        pacmanView.setImageResource(R.drawable.pacman)
                    } else {
                        val currentDrawable = pacmanView.drawable
                        val nextDrawable = when (currentDrawable.constantState) {
                            getDrawable(R.drawable.pacman)?.constantState -> R.drawable.pacman_wide
                            getDrawable(R.drawable.pacman_wide)?.constantState -> R.drawable.pacman_full
                            getDrawable(R.drawable.pacman_full)?.constantState -> R.drawable.pacman
                            else -> R.drawable.pacman
                        }
                        pacmanView.setImageResource(nextDrawable)
                    }
                }

                updateStateStrings()
            }
        }, 0, 150)
    }

    private fun setDifficulty(){
        val diff = getDiff()

        lives = when {
            diff < 20 -> 5
            diff < 40 -> 4
            diff < 60 -> 3
            else -> 1
        }

        movementDuration = when {
            diff < 20 -> 300L
            diff < 40 -> 250L
            diff < 60 -> 200L
            else -> 150L // fastest
        }

        ghostMovementDuration = when {
            diff < 20 -> 400L
            diff < 40 -> 300L
            diff < 60 -> 200L
            else -> 100L // fastest
        }

        ghostFrightenedMovementDuration = when {
            diff < 20 -> 800L
            diff < 40 -> 600L
            diff < 60 -> 400L
            else -> 200L // fastest
        }

    }

    private fun updateStateStrings(){
        val progress = if (allDots == 0) 0
                       else (eatenDotsCounter / allDots.toFloat() * 100).toInt()

        runOnUiThread {
            scoreView.text = "Score: $score"
            levelView.text = "\nLevel: $level"
            progressView.text = "Progress: $progress%"
            livesView.text = "\nLives: $lives"
        }
    }

    private fun startGame(){
        // stop any existing timers
        ghostMoveTimer?.cancel()
        ghostFrightenedMoveTimer?.cancel()
        pacmanMoveTimer?.cancel()

        eatenDotsCounter = 0

        // start timers
        ghostMoveTimer = Timer()
        ghostMoveTimer?.schedule(object : java.util.TimerTask() {
            override fun run() {
                moveGhosts()
            }
        }, 100, ghostMovementDuration)

        ghostFrightenedMoveTimer = Timer()
        ghostFrightenedMoveTimer?.schedule(object : java.util.TimerTask() {
            override fun run() {
                moveFrightenedGhosts()
            }
        }, 100, ghostFrightenedMovementDuration)

        // move everything to starting positions
        pacmanMoveTo(13, 23)
        pacmanMoveUntil(1,0)

        for (i in ghostStartPositions.indices) {
            ghostPositions[i][0] = ghostStartPositions[i][0]
            ghostPositions[i][1] = ghostStartPositions[i][1]
        }

        for (i in ghostPositions.indices) {
            ghostViews[i].x = ghostPositions[i][0] * (gridWidth / gridCountX) + gridX
            ghostViews[i].y = ghostPositions[i][1] * (gridHeight / gridCountY) + gridY
        }

        // reset ghost states
        ghostStates = arrayOf(
            GhostState.NORMAL,
            GhostState.NORMAL,
            GhostState.NORMAL,
            GhostState.NORMAL
        )

        for (i in ghostViews.indices) {
            ghostViews[i].setImageResource(ghostAssets[i][0])
        }

        // reset walls and dots
        for (i in wallsStart.indices) {
            for (j in wallsStart[i].indices) {
                walls[i][j] = wallsStart[i][j]
            }
        }
        placeDotsAndEnergizers()
    }

    private fun gameOver() {
        pacmanMoveTimer?.cancel()
        pacmanAnimationTimer?.cancel()
        ghostMoveTimer?.cancel()
        ghostFrightenedMoveTimer?.cancel()

        runOnUiThread {
            for (i in ghostViews.indices) {
                ghostViews[i].visibility = View.INVISIBLE
            }

            gameoverView.visibility = View.VISIBLE
        }

        Timer().schedule(object : java.util.TimerTask() {
            override fun run() {
                runOnUiThread {
                    pacmanView.setImageResource(R.drawable.pacman_dying_1)
                }
            }
        }, 200)
        Timer().schedule(object : java.util.TimerTask() {
            override fun run() {
                runOnUiThread {
                    pacmanView.setImageResource(R.drawable.pacman_dying_2)
                }
            }
        }, 400)
        Timer().schedule(object : java.util.TimerTask() {
            override fun run() {
                runOnUiThread {
                    pacmanView.setImageResource(R.drawable.pacman_dying_3)
                }
            }
        }, 600)
        Timer().schedule(object : java.util.TimerTask() {
            override fun run() {
                runOnUiThread {
                    pacmanView.setImageResource(R.drawable.pacman_dying_4)
                }
            }
        }, 800)
        Timer().schedule(object : java.util.TimerTask() {
            override fun run() {
                runOnUiThread {
                    pacmanView.setImageResource(R.drawable.pacman_dying_5)
                }
            }
        }, 1000)
        Timer().schedule(object : java.util.TimerTask() {
            override fun run() {
                runOnUiThread {
                    pacmanView.setImageResource(R.drawable.pacman_dead)
                }
            }
        }, 1200)
        Timer().schedule(object : java.util.TimerTask() {
            override fun run() {
                runOnUiThread {
                    pacmanView.visibility = View.INVISIBLE
                    pacmanView.setImageResource(R.drawable.pacman)
                }
            }
        }, 1500)


        Timer().schedule(object : java.util.TimerTask() {
            override fun run() {
                runOnUiThread {
                    passScore(score)
                }
            }
        }, 4000)
    }

    private fun setupSwipeDetection() {
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

    private fun placeDotsAndEnergizers() {
        allDots = 0

        for (i in walls.indices) {
            for (j in walls[i].indices) {
                if (dotViews[i][j] != null) {
                    backgroundView.removeView(dotViews[i][j])
                    dotViews[i][j] = null
                }

                if (walls[i][j] == 4) {
                    val params = FrameLayout.LayoutParams(dpToPx(25f).toInt(), dpToPx(25f).toInt())
                    val energizer = LayoutInflater.from(this).inflate(R.layout.pacman_dot, null)

                    backgroundView.addView(energizer, params)
                    dotViews[i][j] = energizer

                    energizer.x = j * (gridWidth / gridCountX) + gridX
                    energizer.y = i * (gridHeight / gridCountY) + gridY
                    energizer.findViewById<ImageView>(R.id.pacmanDot).setImageResource(R.drawable.pacman_energizer)

                    allDots++
                }
                if (walls[i][j] == 3) {
                    val params = FrameLayout.LayoutParams(dpToPx(10f).toInt(), dpToPx(10f).toInt())
                    val dot = LayoutInflater.from(this).inflate(R.layout.pacman_dot, null)

                    backgroundView.addView(dot, params)
                    dotViews[i][j] = dot

                    dot.x = j * (gridWidth / gridCountX) + gridX
                    dot.y = i * (gridHeight / gridCountY) + gridY

                    allDots++
                }
            }
        }
    }

    private fun eatDotOrEnergizer(x: Int, y: Int) {
        if (walls[y][x] == 3) {
            walls[y][x] = 0
            backgroundView.removeView(dotViews[y][x])
            dotViews[y][x] = null

            eatenDotsCounter++

            score += 1
        } else if (walls[y][x] == 4) {
            walls[y][x] = 0
            backgroundView.removeView(dotViews[y][x])
            dotViews[y][x] = null

            eatenDotsCounter++

            score += 5
            triggerFrightenedMode()
        }

        if (eatenDotsCounter == allDots && allDots > 0) {
            // All dots eaten, trigger win condition
            level++
            runOnUiThread {
                startGame()
            }
        }
    }

    private fun triggerFrightenedMode() {
        frightenedBlinkCount = 0

        // Set all ghosts to frightened unless already eaten
        for (i in ghostStates.indices) {
            if (ghostStates[i] != GhostState.EATEN) {
                ghostStates[i] = GhostState.FRIGHTENED
            }
        }
        // Change ghost images to frightened
        runOnUiThread {
            for (i in ghostViews.indices) {
                if (ghostStates[i] == GhostState.FRIGHTENED) {
                    ghostViews[i].setImageResource(R.drawable.pacman_ghost_dead_blue)
                }
            }
        }
        // Reset timer
        frightenedTimer?.cancel()
        frightenedTimeLeft = frightenedDuration
        frightenedTimer = Timer()
        frightenedTimer?.schedule(object : java.util.TimerTask() {
            override fun run() {
                frightenedTimeLeft -= 100
                if (frightenedTimeLeft <= 0) {
                    endFrightenedMode()
                }
            }
        }, 0, 100)
    }

    private fun endFrightenedMode() {
        frightenedTimer?.cancel()
        for (i in ghostStates.indices) {
            if (ghostStates[i] == GhostState.FRIGHTENED) {
                ghostStates[i] = GhostState.NORMAL
            }
        }
        // Restore ghost images
        runOnUiThread {
            for (i in ghostViews.indices) {
                if (ghostStates[i] == GhostState.NORMAL) {
                    ghostViews[i].setImageResource(ghostAssets[i][0])
                }
            }
        }
    }

    private fun handleGhostCollisions() {
        for (i in ghostPositions.indices) {
            // Handle collision with Pacman
            if (ghostPositions[i][0] == pacmanX && ghostPositions[i][1] == pacmanY) {
                when (ghostStates[i]) {
                    GhostState.FRIGHTENED -> {
                        // Pacman eats ghost
                        ghostStates[i] = GhostState.EATEN
                        score += 20
                        runOnUiThread {
                            ghostViews[i].setImageResource(R.drawable.pacman_ghost_eyes)
                        }
                    }

                    GhostState.NORMAL -> {
                        lives--
                        if (lives < 0) {
                            gameOver()
                        } else {
                            pacmanMoveTo(13, 23) // Reset Pacman position
                            for (j in ghostPositions.indices) { // Reset ghost positions
                                ghostPositions[j][0] = ghostStartPositions[j][0]
                                ghostPositions[j][1] = ghostStartPositions[j][1]
                                ghostViews[j].x = ghostPositions[j][0] * (gridWidth / gridCountX) + gridX
                                ghostViews[j].y = ghostPositions[j][1] * (gridHeight / gridCountY) + gridY
                                ghostStates[j] = GhostState.NORMAL
                                ghostViews[j].setImageResource(ghostAssets[j][0])
                            }
                        }
                    }

                    GhostState.EATEN -> { /* do nothing */
                    }
                }
            }
        }
    }


    private fun pacmanMoveTo(x: Int, y: Int) {
        var actualX = x * (gridWidth / gridCountX) + gridX
        var actualY = y * (gridHeight / gridCountY) + gridY
        val currentActualX = pacmanX * (gridWidth / gridCountX) + gridX
        val currentActualY = pacmanY * (gridHeight / gridCountY) + gridY

        var xAnimator = ObjectAnimator.ofFloat(
            pacmanView, "translationX",
            currentActualX, actualX
        )
        var yAnimator = ObjectAnimator.ofFloat(
            pacmanView, "translationY",
            currentActualY, actualY
        )

        xAnimator.interpolator = android.view.animation.LinearInterpolator()
        yAnimator.interpolator = android.view.animation.LinearInterpolator()
        xAnimator.duration = movementDuration
        yAnimator.duration = movementDuration

        pacmanX = x
        pacmanY = y

        if (pacmanX in walls[0].indices && pacmanY in walls.indices) {
            eatDotOrEnergizer(pacmanX, pacmanY)
        }

        handleGhostCollisions()

        runOnUiThread {
            xAnimator.start()
            yAnimator.start()
        }
    }

    private fun pacmanMoveUntil(x: Int, y: Int) {
        if (pacmanX + x < 0 || pacmanX + x >= walls[0].size || pacmanY + y < 0 || pacmanY + y >= walls.size) {
            return
        }
        if (walls[pacmanY + y][pacmanX + x] == 1 || walls[pacmanY + y][pacmanX + x] == 2) {
            return
        }
        if (x == 1) pacmanView.rotation = 0f
        if (x == -1) pacmanView.rotation = 180f
        if (y == 1) pacmanView.rotation = 90f
        if (y == -1) pacmanView.rotation = 270f

        pacmanMoveTimer?.cancel()
        pacmanMoveTimer = Timer()
        pacmanMoveTimer?.schedule(object : java.util.TimerTask() {
            override fun run() {
                if ((pacmanX == 0 && x == -1)) {
                    pacmanMoveTo(-1, pacmanY)
                    pacmanX = 28
                    pacmanMoveTo(27, pacmanY)
                    return
                }
                if ((pacmanX == 27 && x == 1)) {
                    pacmanMoveTo(28, pacmanY)
                    pacmanX = -1
                    pacmanMoveTo(0, pacmanY)
                    return
                }

                if (walls[pacmanY + y][pacmanX + x] == 1) {
                    pacmanMoveTimer?.cancel()
                    return
                }

                runOnUiThread {
                    pacmanMoveTo(pacmanX + x, pacmanY + y)
                }
            }
        }, 0, movementDuration)
    }

    private fun moveGhostTo(ghostIndex: Int, newX: Int, newY: Int, duration: Long = ghostMovementDuration) {
        if (newX == ghostPositions[ghostIndex][0] && newY == ghostPositions[ghostIndex][1]) {
            return // No movement needed
        }

        val directionX = newX - ghostPositions[ghostIndex][0]
        val directionY = newY - ghostPositions[ghostIndex][1]
        val sprite = when {
            directionX > 0 -> ghostAssets[ghostIndex][0] // Right
            directionX < 0 -> ghostAssets[ghostIndex][1] // Left
            directionY > 0 -> ghostAssets[ghostIndex][2] // Down
            directionY < 0 -> ghostAssets[ghostIndex][3] // Up
            else -> ghostAssets[ghostIndex][0] // Default to right if no movement
        }

        val actualX = newX * (gridWidth / gridCountX) + gridX
        val actualY = newY * (gridHeight / gridCountY) + gridY

        runOnUiThread {
            val xAnimator = ObjectAnimator.ofFloat(ghostViews[ghostIndex], "translationX", ghostViews[ghostIndex].x, actualX)
            val yAnimator = ObjectAnimator.ofFloat(ghostViews[ghostIndex], "translationY", ghostViews[ghostIndex].y, actualY)
            xAnimator.interpolator = android.view.animation.LinearInterpolator()
            yAnimator.interpolator = android.view.animation.LinearInterpolator()
            xAnimator.duration = duration
            yAnimator.duration = duration
            xAnimator.start()
            yAnimator.start()
            ghostViews[ghostIndex].x = actualX
            ghostViews[ghostIndex].y = actualY
            if (ghostStates[ghostIndex] == GhostState.NORMAL) {
                ghostViews[ghostIndex].setImageResource(sprite) // Update ghost sprite
            }
        }

        ghostPositions[ghostIndex][0] = newX
        ghostPositions[ghostIndex][1] = newY
    }

    private fun moveGhosts() {
        for (i in ghostPositions.indices) {
            if (ghostStates[i] == GhostState.FRIGHTENED) {
                // Skip moving frightened ghosts, they are handled separately
                continue
            }

            var (newX, newY) = ghostBehaviors[i].move(
                ghostPositions[i][0], ghostPositions[i][1],
                pacmanX, pacmanY, walls, ghostPositions
            )


            if (ghostStates[i] == GhostState.EATEN) {
                // Move towards start position
                val path = bfs(ghostPositions[i][0], ghostPositions[i][1], ghostStartPositions[i][0], ghostStartPositions[i][1], walls)
                if (path.size > 1) {
                    newX = path[1].first
                    newY = path[1].second
                }
                else { // if path is empty, stay in place
                    newX = ghostPositions[i][0]
                    newY = ghostPositions[i][1]
                }
            }

            moveGhostTo(i, newX, newY)

            // If eaten ghost reached start, revive it
            if (ghostStates[i] == GhostState.EATEN &&
                ghostPositions[i][0] == ghostStartPositions[i][0] &&
                ghostPositions[i][1] == ghostStartPositions[i][1]) {
                ghostStates[i] = GhostState.NORMAL
                runOnUiThread {
                    ghostViews[i].setImageResource(ghostAssets[i][3])
                }
            }
        }

        handleGhostCollisions()
    }

    private fun moveFrightenedGhosts(){
        var isFrightened = false

        for (i in ghostPositions.indices) {
            if (ghostStates[i] == GhostState.FRIGHTENED) {
                isFrightened = true

                val behavior = FrightenedMovement()
                val (newX, newY) = behavior.move(
                    ghostPositions[i][0], ghostPositions[i][1],
                    pacmanX, pacmanY, walls, ghostPositions
                )
                moveGhostTo(i, newX, newY, ghostFrightenedMovementDuration)
            }
        }

        if (isFrightened){
            // Handle blinking effect for frightened ghosts
            frightenedBlinkCount++

            val totalBlinkCount = frightenedDuration / ghostFrightenedMovementDuration
            runOnUiThread {
                for (i in ghostViews.indices) {
                    if (ghostStates[i] == GhostState.FRIGHTENED) {
                        if (frightenedBlinkCount % 2 == 0 || frightenedBlinkCount >= totalBlinkCount - 4)
                            ghostViews[i].setImageResource(R.drawable.pacman_ghost_dead_blue)
                        else
                            ghostViews[i].setImageResource(R.drawable.pacman_ghost_dead_white)
                    }
                }
            }

        }
    }

    // BFS for ghost respawn
    private fun bfs(startX: Int, startY: Int, targetX: Int, targetY: Int, walls: Array<IntArray>): List<Pair<Int, Int>> {
        val queue: Queue<Pair<Int, Int>> = LinkedList()
        val visited = Array(walls.size) { BooleanArray(walls[0].size) }
        val parent = mutableMapOf<Pair<Int, Int>, Pair<Int, Int>?>()
        queue.add(Pair(startX, startY))
        visited[startY][startX] = true
        parent[Pair(startX, startY)] = null
        val directions = arrayOf(
            intArrayOf(0, 1), intArrayOf(0, -1),
            intArrayOf(1, 0), intArrayOf(-1, 0)
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
                val newX = x + direction[0]
                val newY = y + direction[1]
                if (newY in walls.indices && newX in walls[0].indices && !visited[newY][newX] && walls[newY][newX] != 1) {
                    queue.add(Pair(newX, newY))
                    visited[newY][newX] = true
                    parent[Pair(newX, newY)] = Pair(x, y)
                }
            }
        }
        return emptyList()
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