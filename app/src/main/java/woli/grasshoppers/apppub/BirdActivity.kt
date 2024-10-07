package woli.grasshoppers.apppub

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.util.Timer
import kotlin.random.Random

class BirdActivity : AppCompatActivity() {

    lateinit var bird: ImageView
    lateinit var background: FrameLayout
    lateinit var button: Button

    var tickTimer = Timer()
    var tickCount = 0

    var birdVelocity = 0f

    var pipes = mutableListOf<View>()
    var displayedPipes = mutableListOf<View>()

    var score = 0
    var isBetweenPipes = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bird)

        hideSystemBars()


        background = findViewById(R.id.birdBackground)
        bird = findViewById(R.id.bird)
        button = findViewById(R.id.birdButtonStartGame)


        bird.x = 50f

        val pipeWidth = 100
        val pipeHeight = resources.displayMetrics.heightPixels

        val params = FrameLayout.LayoutParams(pipeWidth,pipeHeight)

        for (i in 1..6 step 2){
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
        background.setOnClickListener {
            birdVelocity = 20f
        }

        tickTimer = kotlin.concurrent.timer(initialDelay = 50, period = 50){
            tick()
        }
    }

    fun endGame(){
        tickTimer.cancel()

        background.setOnClickListener{
            button.visibility = Button.VISIBLE
        }

        button.setOnClickListener {
            button.visibility = Button.INVISIBLE

            score = 0
            tickCount = 0
            bird.y = resources.displayMetrics.heightPixels / 2 +0f
            birdVelocity = 0f
            isBetweenPipes = false

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

        var pipeGapWidth = 600
        var topPipeOffset = Random.nextInt(
            from = -resources.displayMetrics.heightPixels + pipeGapWidth,
            until = -2 * pipeGapWidth
        )

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

        birdVelocity -= 1.5f

        for (pipe in displayedPipes){
            pipe.x -= 10
        }

        if (tickCount % 40 == 0){
            showPipe()
        }

        val upperPipe = displayedPipes[0]
        val lowerPipe = displayedPipes[1]

        if (lowerPipe.x + lowerPipe.width < 0){
            hidePipe(0)
        }


        if (upperPipe.x < bird.x + upperPipe.width && bird.x < upperPipe.x + upperPipe.width){
            if (bird.y < upperPipe.y + upperPipe.height || bird.y + bird.height > lowerPipe.y){
                endGame()
            }
            isBetweenPipes = true
        }
        else if (isBetweenPipes){
            score++
            isBetweenPipes = false
        }


        tickCount++
    }
}