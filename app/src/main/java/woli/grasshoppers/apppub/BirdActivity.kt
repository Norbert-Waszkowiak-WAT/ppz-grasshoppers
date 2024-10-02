package woli.grasshoppers.apppub

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.util.Timer
import kotlin.random.Random

class BirdActivity : AppCompatActivity() {

    var tickTimer = Timer()
    var tickCount = 0

    var birdVelocity = 0f

    var pipes = mutableListOf<View>()

    val random = Random(100) // todo: use time as seed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bird)

        hideSystemBars()

        tickTimer = kotlin.concurrent.timer(initialDelay = 100, period = 50){
            tick()
        }

        val base = findViewById<FrameLayout>(R.id.birdGameBase)
        val bird = findViewById<ImageView>(R.id.bird)

        base.setOnClickListener {
            birdVelocity = 20f
        }


        bird.x = 50f

        val pipeWidth = 100
        val pipeHeight = resources.displayMetrics.heightPixels

        val params = FrameLayout.LayoutParams(pipeWidth,pipeHeight)

        for (i in 1..2 step 2){
            val newPipe1 = LayoutInflater.from(this).inflate(R.layout.bird_pipe, null)
            val newPipe2 = LayoutInflater.from(this).inflate(R.layout.bird_pipe, null)

            base.addView(newPipe1, params)
            base.addView(newPipe2, params)
            pipes.add(newPipe1)
            pipes.add(newPipe2)


            newPipe1.rotation = 180f
            newPipe1.x = resources.displayMetrics.widthPixels + 0f
            newPipe2.x = resources.displayMetrics.widthPixels + 0f
        }


    }

    override fun onBackPressed() {
        passScore(10)//TODO: real score value
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

    fun tick(){
        val bird = findViewById<ImageView>(R.id.bird)
        val base = findViewById<FrameLayout>(R.id.birdGameBase)

        if (tickCount % 4 == 0){
            bird.setImageResource(R.drawable.bird_texture_wings_up)
        }
        else if (tickCount % 4 == 2){
            bird.setImageResource(R.drawable.bird_texture_wings_down)
        }


        if (birdVelocity < 0 && bird.y < base.bottom - bird.height){
            bird.y -= birdVelocity
            bird.rotation = 15f
        }
        else if (birdVelocity > 0 && bird.y > base.top){
            bird.y -= birdVelocity
            bird.rotation = -15f
        }

        birdVelocity -= 1.5f

        for (pipe in pipes){
            pipe.x -= 10
        }

        var pipeGapWidth = 300
        var topPipeOffset = random.nextInt(
            from = -resources.displayMetrics.heightPixels + pipeGapWidth,
            until = -2 * pipeGapWidth
        )

        if (tickCount == 0){
            pipes[0].y = topPipeOffset + 0f
            pipes[1].y = pipes[0].y + pipes[0].height + pipeGapWidth + 0f
        }

        if (pipes[0].x < 0){
            pipes[0].x = resources.displayMetrics.widthPixels + 0f
            pipes[1].x = resources.displayMetrics.widthPixels + 0f

            pipes[0].y = topPipeOffset + 0f
            pipes[1].y = pipes[0].y + pipes[0].height + pipeGapWidth + 0f
        }


        tickCount++
    }
}