package woli.grasshoppers.apppub

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class KnifeActivity : AppCompatActivity() {

    private lateinit var knife: ImageView
    private lateinit var scoreTextView: TextView
    private lateinit var target: ImageView
    private lateinit var screenView: View
    private var isKnifeThrown = false
    private var score = 0
    private var diffLevel: Int = 50
    private var targetY: Int = 200
    private var isKnifeStuck: Boolean = false
    private var knifeAngle: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_knife)

        hideSystemBars()
        initUI()
        initTarget()
        initKnife()

        diffLevel = getDiff()

        target.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                targetY = target.top
                target.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    private fun initUI() {
        target = findViewById(R.id.tarcza)
        knife = findViewById(R.id.knife)
        scoreTextView = findViewById(R.id.score_text)
        screenView = findViewById(R.id.screen_view)
    }

    private fun initTarget() { //TODO: pewnie tutaj jedno z kilku użyć współczynnika trudności
        val randomDuration = (1000..3000).random().toLong()
        val randomRotation = (30..720).random().toFloat()
        val finalRotation = if ((0..1).random() == 0) randomRotation else -randomRotation
        val interpolator = when ((0..4).random()) { //TODO: usunąć niektóre ruchy
            0 -> LinearInterpolator()
            1 -> AccelerateInterpolator()
            2 -> DecelerateInterpolator()
            3 -> AccelerateDecelerateInterpolator()
            else -> BounceInterpolator()
        }

        fun startRotationAnimation() { //TODO: współczynnik trudności reguluje ilość obrotów do czasu
            val currentRotation = target.rotation
            val animator = ObjectAnimator.ofFloat(target, "rotation", currentRotation, currentRotation + finalRotation)
            animator.duration = randomDuration
            animator.interpolator = interpolator

            animator.addUpdateListener { animation ->
                if (isKnifeStuck) {
                    val centerX = target.x + target.width / 2
                    val centerY = target.y + target.height / 2
                    val radius = target.width / 2

                    val currentAngle = Math.toRadians(target.rotation.toDouble() + knifeAngle)

                    val knifeX = centerX + radius * Math.cos(currentAngle).toFloat() - knife.width / 2
                    val knifeY = centerY + radius * Math.sin(currentAngle).toFloat() - knife.height / 2

                    knife.x = knifeX
                    knife.y = knifeY

                    knife.rotation = target.rotation + knifeAngle - 90f
                }
            }

            animator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}

                override fun onAnimationEnd(animation: Animator) {
                    startRotationAnimation()
                }

                override fun onAnimationCancel(animation: Animator) {}

                override fun onAnimationRepeat(animation: Animator) {}
            })
            animator.start()
        }

        startRotationAnimation()
    }

    private fun initKnife() {
        screenView.setOnClickListener {
            if (!isKnifeThrown) {
                throwKnife()
            }
        }
    }

    private fun throwKnife() {
        isKnifeThrown = true
        val targetKnifePosition = targetY + knife.height
        val knifeAnimator = ObjectAnimator.ofFloat(knife, "translationY", knife.translationY, -targetKnifePosition.toFloat())
        knifeAnimator.duration = 200

        knifeAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                checkHit()
            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationRepeat(animation: Animator) {}
        })

        knifeAnimator.start()
    }

    private fun checkHit() {
        val knifeBounds = Rect()
        knife.getGlobalVisibleRect(knifeBounds)

        val targetBounds = Rect()
        target.getGlobalVisibleRect(targetBounds)

        if (Rect.intersects(knifeBounds, targetBounds)) {
            isKnifeStuck = true

            val centerX = target.x + target.width / 2
            val centerY = target.y + target.height / 2
            val dx = knife.x + knife.width / 2 - centerX
            val dy = knife.y + knife.height / 2 - centerY

            val relativeAngle = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
            knifeAngle = (relativeAngle - target.rotation) % 360

            val radius = target.width / 2
            val knifeX = centerX + radius * Math.cos(Math.toRadians((knifeAngle + target.rotation).toDouble())).toFloat() - knife.width / 2
            val knifeY = centerY + radius * Math.sin(Math.toRadians((knifeAngle + target.rotation).toDouble())).toFloat() - knife.height / 2

            knife.x = knifeX
            knife.y = knifeY

            updateScore()

            //TODO: New knife
        } else {
            resetKnife()
        }
    }

    private fun updateScore() {
        score++
        scoreTextView.text = score.toString()
    }

    private fun resetKnife() {
        knife.translationY = 0f
        isKnifeThrown = false
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        passScore(score)//TODO: pewnie kilka wartośći (wynik, ilość noży, jabłka)
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
        return intent.getIntExtra("knife_diff", 50)
    }
}