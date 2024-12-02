package woli.grasshoppers.apppub

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Timer
import kotlin.random.Random


class BirdActivity : AppCompatActivity() {

    private lateinit var bird: ImageView
    private lateinit var background: FrameLayout
    private lateinit var button: ImageView
    private lateinit var scoreView: TextView
    private lateinit var pauseBack: ImageView

    private val tickPeriod = 50
    private var tickTimer = Timer()
    private var tickCount = 0
    private var nextPipeTick = 0

    private var birdVelocity = 0f

    private var pipes = mutableListOf<View>()
    private var displayedPipes = mutableListOf<View>()

    private var score = 0
    private var maxScore = 0
    private var isBetweenPipes = false

    private var difficulty = 50

    private var birdHeight = 0
    private var birdWidth = 0
    private var pipeWidth = 0
    private var foamHeight = 0
    private var foamOffset = 0
    private var handleWidth = 0
    private var pipeGapWidth = 0f
    private var speed = 0
    private var gravity = 0f
    private var jumpVelocity = 20f
    private var maxPipeDistance = 0
    private var minPipeDistance = 0

    private var screenHeight = 0
    private var screenWidth = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bird)

        hideSystemBars()


        background = findViewById(R.id.birdBackground)
        bird = findViewById(R.id.bird)
        button = findViewById(R.id.birdButtonStartGame)
        scoreView = findViewById(R.id.birdScoreTextView)
        pauseBack = findViewById(R.id.birdButtonPauseBack)


        pauseBack.setOnClickListener{
            onBackPressed()
        }

        screenWidth = resources.displayMetrics.widthPixels
        screenHeight = resources.displayMetrics.heightPixels
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0){
            screenHeight += resources.getDimensionPixelSize(resourceId)
        }

        birdHeight = dpToPx(52f).toInt()
        birdWidth = dpToPx(75f).toInt()
        pipeWidth = dpToPx(100f).toInt()
        foamHeight = dpToPx(100f).toInt()
        foamOffset = dpToPx(50f).toInt()
        handleWidth = dpToPx(20f).toInt()

        difficulty = getDiff()

        gravity = dpToPx(1f)
        jumpVelocity = dpToPx(12f)

        speed = dpToPx(5f).toInt() * (1 + (difficulty / 100))

        val jumpHeight = (gravity * (jumpVelocity/gravity) * (jumpVelocity/gravity)) / 2
        pipeGapWidth = birdHeight + jumpHeight * (1.5f + ((50 - difficulty) / 100))

        maxPipeDistance = birdWidth * (4 + ((100-difficulty) / 100))
        minPipeDistance = birdWidth * (3 - (difficulty / 100))


        bird.x = dpToPx(20f)

        val pipeHeight = screenHeight

        val params1 = FrameLayout.LayoutParams(pipeWidth,pipeHeight)
        val params2 = FrameLayout.LayoutParams(pipeWidth,pipeHeight)

        val pipeCount = 6
        for (i in 1..pipeCount){
            val newPipe1 = LayoutInflater.from(this).inflate(R.layout.bird_pipe, null)
            val newPipe2 = LayoutInflater.from(this).inflate(R.layout.bird_pipe, null)

            background.addView(newPipe1, params1)
            background.addView(newPipe2, params2)
            pipes.add(newPipe1)
            pipes.add(newPipe2)


            newPipe1.rotation = 180f
            newPipe1.x = screenWidth + 0f
            newPipe2.x = screenWidth + 0f
        }

        button.setOnClickListener {
            button.visibility = Button.INVISIBLE
            startGame()
        }
    }

    fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
    }


    override fun onBackPressed() {
        passScore(maxScore)
        super.onBackPressed()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }

    private fun passScore(score: Int) {
        val data = Intent()
        data.putExtra("score", score.toString())
        setResult(Activity.RESULT_OK, data)
        finish()
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

    private fun getDiff(): Int {
        return intent.getIntExtra("bird_diff", 50)
    }

    private fun startGame(){
        score = 0
        tickCount = 0
        nextPipeTick = 0
        bird.y = screenHeight / 2 +0f
        bird.rotation = 0f
        birdVelocity = 0f
        isBetweenPipes = false

        this.runOnUiThread {
            pauseBack.visibility = ImageView.INVISIBLE
        }

        background.setOnClickListener {
            birdVelocity = jumpVelocity
        }

        tickTimer.cancel()
        tickTimer = kotlin.concurrent.timer(initialDelay = tickPeriod.toLong(), period = tickPeriod.toLong()){
            tick()
        }
    }

    private fun endGame(){
        tickTimer.cancel()

        tickTimer = kotlin.concurrent.timer(initialDelay = tickPeriod.toLong(), period = tickPeriod.toLong()){
            if (bird.y + bird.height > screenHeight){
                tickTimer.cancel()
                tickTimer = Timer()
            }
            else{
                bird.y += 100
                if (bird.rotation < 90){
                    bird.rotation += 10f
                }

            }
        }

        if (score > maxScore){
            maxScore = score
        }

        this.runOnUiThread{
            button.visibility = Button.VISIBLE
            button.setImageResource(R.drawable.bird_restart)

            pauseBack.visibility = ImageView.VISIBLE
        }

        button.setOnClickListener {
            button.visibility = Button.INVISIBLE

            for (i in 0 .. displayedPipes.size step 2){
                hidePipe(0)
            }

            startGame()
        }
    }

    private fun showPipe(){
        if (pipes.size < 2){
            return
        }

        val upperPipe = pipes[0]
        val lowerPipe = pipes[1]

        val minUpperPipeHeight = 1f * pipeGapWidth
        val maxUpperPipeHeight = screenHeight - 1.5f * pipeGapWidth
        var randomFrom: Float
        var randomUntil: Float

        if (displayedPipes.size < 2){
            randomUntil = maxUpperPipeHeight
            randomFrom = minUpperPipeHeight
        }
        else {
            val lastUpperPipe = displayedPipes[displayedPipes.size-2]

            val time = (screenWidth - lastUpperPipe.x - birdHeight) / speed
            val maxFallDistance = (gravity * time * time) / 2
            val maxJumpDistance = jumpVelocity * time

            randomFrom = lastUpperPipe.height - maxJumpDistance + ((100 - difficulty) / 100) * birdHeight
            randomUntil = lastUpperPipe.height + maxFallDistance - ((100 - difficulty) / 100) * birdHeight

            if (randomFrom >= randomUntil) {
                randomFrom = lastUpperPipe.height - pipeGapWidth
                randomUntil = lastUpperPipe.height + pipeGapWidth
            }
        }

        if (randomFrom < minUpperPipeHeight) { randomFrom = minUpperPipeHeight }
        if (randomUntil > maxUpperPipeHeight) { randomUntil = maxUpperPipeHeight }


        val upperPipeHeight = Random.nextInt(randomFrom.toInt(), randomUntil.toInt())

        runOnUiThread {
            upperPipe.y = 0f
            lowerPipe.y = upperPipeHeight + pipeGapWidth - 2 * foamHeight + 0f


            val lowerParams = FrameLayout.LayoutParams(pipeWidth, screenHeight - lowerPipe.y.toInt())
            val upperParams = FrameLayout.LayoutParams(pipeWidth, upperPipeHeight)
            lowerPipe.layoutParams = lowerParams
            upperPipe.layoutParams = upperParams

            upperPipe.x = screenWidth + 0f
            lowerPipe.x = screenWidth + 0f
        }

        displayedPipes.add(upperPipe)
        displayedPipes.add(lowerPipe)

        pipes.remove(upperPipe)
        pipes.remove(lowerPipe)
    }

    private fun hidePipe(i: Int){
        if (displayedPipes.size < 2){
            return
        }

        val upperPipe = displayedPipes[i]
        val lowerPipe = displayedPipes[i+1]

        runOnUiThread {
            upperPipe.x = screenWidth + 0f
            lowerPipe.x = screenWidth + 0f
        }


        displayedPipes.remove(upperPipe)
        displayedPipes.remove(lowerPipe)

        pipes.add(upperPipe)
        pipes.add(lowerPipe)
    }


    private fun tick() {
        if (tickCount % 6 == 0) {
            bird.setImageResource(R.drawable.bird_wings_flat)
        } else if (tickCount % 6 == 2) {
            bird.setImageResource(R.drawable.bird_wings_up)
        } else if (tickCount % 6 == 5){
            bird.setImageResource(R.drawable.bird_wings_down)
        }


        if (bird.y >= background.bottom - bird.height){
            endGame()
        }

        runOnUiThread {
            if (birdVelocity < 0) {
                bird.rotation = 15f
            } else if (birdVelocity > 0) {
                bird.rotation = -15f
            }
        }

        runOnUiThread {
            bird.y -= birdVelocity
        }


        birdVelocity -= gravity

        for (pipe in displayedPipes) {
            runOnUiThread {
                pipe.x -= speed
            }
        }

        if (nextPipeTick == tickCount) {
            showPipe()

            val currentPipeDistance = tickCount * speed

            nextPipeTick = Random.nextInt(currentPipeDistance + minPipeDistance, currentPipeDistance + maxPipeDistance) / speed
        }


        var upperPipe = displayedPipes[0]
        var lowerPipe = displayedPipes[1]

        if (lowerPipe.x + lowerPipe.width < 0) {
            hidePipe(0)
        }

        if (bird.x + bird.width > upperPipe.x  && bird.x < upperPipe.x + upperPipe.width) {
            isBetweenPipes = true
        } else if (isBetweenPipes) {
            score++
            isBetweenPipes = false
        }

        var i = 0
        while (i < displayedPipes.size-1) {
            upperPipe = displayedPipes[i]
            lowerPipe = displayedPipes[i + 1]

            if (bird.x + bird.width > upperPipe.x + handleWidth  && bird.x < upperPipe.x + upperPipe.width - handleWidth) {
                if (bird.y < upperPipe.y + upperPipe.height - foamHeight - dpToPx(20f) || bird.y + bird.height > lowerPipe.y + foamHeight + dpToPx(20f)) {
                    endGame()
                }
            }

            i+=2
        }

        runOnUiThread {
            scoreView.text = score.toString()
        }


        tickCount++
    }
}