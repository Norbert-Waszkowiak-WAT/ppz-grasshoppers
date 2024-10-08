package woli.grasshoppers.apppub

import android.app.Activity
import android.content.Intent
import android.os.Bundle
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

    lateinit var bird: ImageView
    lateinit var background: FrameLayout
    lateinit var button: Button
    lateinit var scoreView: TextView

    val tickPeriod = 50
    var tickTimer = Timer()
    var tickCount = 0
    var nextPipeTick = 0

    var birdVelocity = 0f

    var pipes = mutableListOf<View>()
    var displayedPipes = mutableListOf<View>()

    var score = 0
    var isBetweenPipes = false

    var difficulty = 50

    val birdSize = 200
    var pipeGapWidth = 0f
    var speed = 10
    var gravity = 1.5f
    var jumpVelocity = 20f
    var maxPipeDistance = 0
    var minPipeDistance = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bird)

        hideSystemBars()


        background = findViewById(R.id.birdBackground)
        bird = findViewById(R.id.bird)
        button = findViewById(R.id.birdButtonStartGame)
        scoreView = findViewById(R.id.birdScoreTextView)


        difficulty = getDiff()

        gravity = 1.5f
        jumpVelocity = 20f
        speed = 10

        val jumpHeight = (gravity * (jumpVelocity/gravity) * (jumpVelocity/gravity)) / 2
        pipeGapWidth = jumpHeight + birdSize * (1.5f + ((50 - difficulty) / 100))

        maxPipeDistance = birdSize * (4 + ((100-difficulty) / 100))
        minPipeDistance = birdSize * (3 - (difficulty / 100))


        bird.x = 50f

        val pipeWidth = 100
        val pipeHeight = resources.displayMetrics.heightPixels

        val params = FrameLayout.LayoutParams(pipeWidth,pipeHeight)

        val pipeCount = resources.displayMetrics.widthPixels / pipeWidth
        for (i in 1..pipeCount){
            val newPipe1 = LayoutInflater.from(this).inflate(R.layout.bird_pipe, null)
            val newPipe2 = LayoutInflater.from(this).inflate(R.layout.bird_pipe, null)

            background.addView(newPipe1, params)
            background.addView(newPipe2, params)
            pipes.add(newPipe1)
            pipes.add(newPipe2)


            newPipe1.rotation = 180f
            newPipe1.x = resources.displayMetrics.widthPixels + 0f
            newPipe2.x = resources.displayMetrics.widthPixels + 0f
        }

        button.setOnClickListener {
            button.visibility = Button.INVISIBLE
            startGame()
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

    fun startGame(){
        score = 0
        tickCount = 0
        nextPipeTick = 0
        bird.y = resources.displayMetrics.heightPixels / 2 +0f
        birdVelocity = 0f
        isBetweenPipes = false


        background.setOnClickListener {
            birdVelocity = jumpVelocity
        }

        tickTimer = kotlin.concurrent.timer(initialDelay = tickPeriod.toLong(), period = tickPeriod.toLong()){
            tick()
        }
    }

    fun endGame(){
        tickTimer.cancel()

        this.runOnUiThread{
            button.visibility = Button.VISIBLE
            button.text = "Restart"
        }

        button.setOnClickListener {
            button.visibility = Button.INVISIBLE

            for (i in 0 .. displayedPipes.size step 2){
                hidePipe(0)
            }

            startGame()
        }
    }

    fun showPipe(){
        if (pipes.size < 2){
            return
        }

        val upperPipe = pipes[0]
        val lowerPipe = pipes[1]

        val maxPipeOffset = -1.5f * pipeGapWidth
        val minPipeOffset = -resources.displayMetrics.heightPixels + 0.5f * pipeGapWidth
        var randomFrom = 0f
        var randomUntil = 0f

        if (displayedPipes.size < 2){
            randomUntil = maxPipeOffset
            randomFrom = minPipeOffset
        }
        else {
            val lastUpperPipe = displayedPipes[displayedPipes.size-2]

            val time = (resources.displayMetrics.widthPixels - lastUpperPipe.x - birdSize) / speed
            val maxFallDistance = (gravity * time * time) / 2
            val maxJumpDistance = jumpVelocity * time

            randomFrom = lastUpperPipe.y - maxJumpDistance + ((100 - difficulty) / 100) * birdSize
            randomUntil = lastUpperPipe.y + maxFallDistance - ((100 - difficulty) / 100) * birdSize

            if (randomFrom >= randomUntil) {
                randomFrom = lastUpperPipe.y - pipeGapWidth
                randomUntil = lastUpperPipe.y + pipeGapWidth
            }
        }

        if (randomFrom < minPipeOffset) { randomFrom = minPipeOffset }
        if (randomUntil > maxPipeOffset) { randomUntil = maxPipeOffset }


        val topPipeOffset = Random.nextInt(randomFrom.toInt(), randomUntil.toInt())
        upperPipe.y = topPipeOffset + 0f
        lowerPipe.y = pipes[0].y + pipes[0].height + pipeGapWidth + 0f

        upperPipe.x = resources.displayMetrics.widthPixels + 0f
        lowerPipe.x = resources.displayMetrics.widthPixels + 0f

        displayedPipes.add(upperPipe)
        displayedPipes.add(lowerPipe)

        pipes.remove(upperPipe)
        pipes.remove(lowerPipe)
    }

    fun hidePipe(i: Int){
        if (displayedPipes.size < 2){
            return
        }

        val upperPipe = displayedPipes[i]
        val lowerPipe = displayedPipes[i+1]

        upperPipe.x = resources.displayMetrics.widthPixels + 0f
        lowerPipe.x = resources.displayMetrics.widthPixels + 0f


        displayedPipes.remove(upperPipe)
        displayedPipes.remove(lowerPipe)

        pipes.add(upperPipe)
        pipes.add(lowerPipe)
    }


    fun tick() {
        if (tickCount % 4 == 0) {
            bird.setImageResource(R.drawable.bird_texture_wings_up)
        } else if (tickCount % 4 == 2) {
            bird.setImageResource(R.drawable.bird_texture_wings_down)
        }


        if (bird.y >= background.bottom - bird.height){
            endGame()
        }

        if (birdVelocity < 0){
            bird.rotation = 15f
        }
        else if (birdVelocity > 0){
            bird.rotation = -15f
        }

        bird.y -= birdVelocity

        birdVelocity -= gravity

        for (pipe in displayedPipes){
            pipe.x -= speed
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
            if (bird.y < upperPipe.y + upperPipe.height || bird.y + bird.height > lowerPipe.y) {
                endGame()
            }
            isBetweenPipes = true
        } else if (isBetweenPipes) {
            score++
            isBetweenPipes = false
        }

        var i = 0
        while (i < displayedPipes.size-1) {
            upperPipe = displayedPipes[i]
            lowerPipe = displayedPipes[i + 1]

            if (bird.x + bird.width > upperPipe.x  && bird.x < upperPipe.x + upperPipe.width) {
                if (bird.y < upperPipe.y + upperPipe.height || bird.y + bird.height > lowerPipe.y) {
                    endGame()
                }
            }

            i+=2
        }

        this.runOnUiThread {
            scoreView.text = score.toString()
        }


        tickCount++
    }
}