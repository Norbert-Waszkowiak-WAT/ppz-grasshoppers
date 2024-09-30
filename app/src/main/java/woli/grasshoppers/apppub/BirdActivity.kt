package woli.grasshoppers.apppub

import android.app.Activity
import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.util.Timer

class BirdActivity : AppCompatActivity() {

    var tickTimer = Timer()
    var tickCount = 0

    var birdVelocity = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bird)

        hideSystemBars()

        tickTimer = kotlin.concurrent.timer(initialDelay = 100, period = 100){
            tick()
        }

        val base = findViewById<FrameLayout>(R.id.birdGameBase)

        base.setOnClickListener {
            birdVelocity = 40f
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

        if (tickCount % 2 == 1){
            bird.setImageResource(R.drawable.bird_texture_wings_up)
        }
        else if (tickCount % 2 == 0){
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

        birdVelocity -= 2


        tickCount++
    }
}