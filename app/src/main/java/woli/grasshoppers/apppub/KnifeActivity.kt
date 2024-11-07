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
import androidx.constraintlayout.widget.ConstraintLayout
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

//TODO list: levels, control knife numbers to through -> small graphics, apples, difficulty, dziwne przesunięcie podczas rzutu po wbiciu, background image, knifes intersection

class KnifeActivity : AppCompatActivity() {

    private lateinit var knife: ImageView
    private lateinit var scoreTextView: TextView
    private lateinit var target: ImageView
    private lateinit var screenView: View
    private var score = 0
    private var diffLevel: Int = 50
    private var targetY: Int = 200
    private val stuckKnives = mutableListOf<ImageView>()
    private var knifeAngle: Float = 0f
    private val knifeAngles = mutableListOf<Float>()
    private val knives = mutableListOf<ImageView>()
    private var currentKnifeIndex = 0
    private val knivesAmount: Int = 20 //TODO: niemożność rzucenia większej ilości //To powinna być tablica dla odpowiednich poziomów
    private var knifeThrown: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_knife)

        hideSystemBars()
        initUI()
        initTarget()
        initKnife()
        //TODO: master function initLevel() which contains initTarget and initKnife

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

        knives.add(0, knife)

        for (i in 0 until knivesAmount-1) {
            val newKnife = ImageView(this).apply {
                layoutParams = ConstraintLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                    startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                    endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                    val density = resources.displayMetrics.density
                    bottomMargin = (32 * density).toInt()
                    horizontalBias = 0.5f
                }
                setImageResource(R.drawable.knifebeer)
                visibility = View.GONE
            }
            (screenView as ViewGroup).addView(newKnife)
            knives.add(newKnife)
        }
    }

    private fun initTarget() { //TODO: pewnie tutaj jedno z kilku użyć współczynnika trudności
        val randomDuration = (1000..3000).random().toLong()
        val randomRotation = (30..720).random().toFloat()
        val finalRotation = if ((0..1).random() == 0) randomRotation else -randomRotation
        val interpolator = when ((0..4).random()) { //TODO: usunąć niektóre ruchy?
            0 -> LinearInterpolator()
            1 -> AccelerateInterpolator()
            2 -> DecelerateInterpolator()
            3 -> AccelerateDecelerateInterpolator()
            else -> BounceInterpolator()
        }

        fun startRotationAnimation() { //TODO: współczynnik trudności reguluje np. ilość obrotów do czasu
            val currentRotation = target.rotation
            val animator = ObjectAnimator.ofFloat(target, "rotation", currentRotation, currentRotation + finalRotation)
            animator.duration = randomDuration
            animator.interpolator = interpolator

            animator.addUpdateListener {
                stuckKnives.forEachIndexed { index, knife ->
                    val centerX = target.x + target.width / 2
                    val centerY = target.y + target.height / 2
                    val radius = target.width / 2

                    val currentAngle = Math.toRadians(target.rotation.toDouble() + knifeAngles[index])

                    val knifeX = centerX + radius * cos(currentAngle).toFloat() - knife.width / 2
                    val knifeY = centerY + radius * sin(currentAngle).toFloat() - knife.height / 2

                    knife.x = knifeX
                    knife.y = knifeY

                    knife.rotation = target.rotation + knifeAngles[index] - 90f
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
            if (currentKnifeIndex < knives.size && knives[currentKnifeIndex].visibility == View.VISIBLE) {
                throwKnife(knives[currentKnifeIndex])
            }
        }
    }

    private fun throwKnife(knifeToThrow: ImageView) {
        if (!knifeThrown) {
            knifeToThrow.visibility = View.VISIBLE
            val targetKnifePosition = targetY + knifeToThrow.height
            val knifeAnimator = ObjectAnimator.ofFloat(
                knifeToThrow,
                "translationY",
                knifeToThrow.translationY,
                -targetKnifePosition.toFloat()
            )
            knifeAnimator.duration = 100
            knifeThrown = true;

            knifeAnimator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}

                override fun onAnimationEnd(animation: Animator) {
                    checkHit(knifeToThrow)
                }

                override fun onAnimationCancel(animation: Animator) {}

                override fun onAnimationRepeat(animation: Animator) {}
            })

            knifeAnimator.start()
        }
    }

    private fun checkHit(knifeToCheck: ImageView) {
        val knifeBounds = Rect()
        knifeToCheck.getGlobalVisibleRect(knifeBounds)//TODO: czy to powinna na pewno być ta funkcja...? raczej zdecydowanie nie

        for (stuckKnife in stuckKnives) {
            val stuckKnifeBounds = Rect()
            stuckKnife.getGlobalVisibleRect(stuckKnifeBounds)

            if (Rect.intersects(knifeBounds, stuckKnifeBounds)) {
                val isRotatingClockwise = target.rotation > 0
                val offsetX = if (isRotatingClockwise) -1000f else 1000f
                val offsetY = 2f

                val targetX = knifeToCheck.x + offsetX
                val targetY = knifeToCheck.y + offsetY

                val knifeAnimator = ObjectAnimator.ofFloat(knifeToCheck, "translationX", knifeToCheck.translationX, targetX)
                val knifeAnimatorY = ObjectAnimator.ofFloat(knifeToCheck, "translationY", knifeToCheck.translationY, targetY)
                val rotationAnimator = ObjectAnimator.ofFloat(knifeToCheck, "rotation", knifeToCheck.rotation, knifeToCheck.rotation + 720)

                knifeAnimator.duration = 500
                knifeAnimatorY.duration = 500
                rotationAnimator.duration = 500

                knifeAnimator.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {}

                    override fun onAnimationEnd(animation: Animator) {
                        knifeToCheck.visibility = View.GONE
                        knifeThrown = false
                        //TODO: jakieś zakończenie lub możliwość kontynuowania za piwa
                    }

                    override fun onAnimationCancel(animation: Animator) {}

                    override fun onAnimationRepeat(animation: Animator) {}
                })

                knifeAnimator.start()
                knifeAnimatorY.start()
                rotationAnimator.start()

                return
            }
        }

        val targetBounds = Rect()
        target.getGlobalVisibleRect(targetBounds)

        if (Rect.intersects(knifeBounds, targetBounds)) {
            if (!stuckKnives.contains(knifeToCheck)) {
                stuckKnives.add(knifeToCheck)
            }

            val centerX = target.x + target.width / 2
            val centerY = target.y + target.height / 2
            val dx = knifeToCheck.x + knifeToCheck.width / 2 - centerX
            val dy = knifeToCheck.y + knifeToCheck.height / 2 - centerY

            val relativeAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
            knifeAngle = (relativeAngle - target.rotation) % 360
            knifeAngles.add(knifeAngle)

            val radius = target.width / 2
            val knifeX = centerX + radius * cos(Math.toRadians((knifeAngle + target.rotation).toDouble())).toFloat() - knifeToCheck.width / 2
            val knifeY = centerY + radius * sin(Math.toRadians((knifeAngle + target.rotation).toDouble())).toFloat() - knifeToCheck.height / 2

            knifeToCheck.x = knifeX
            knifeToCheck.y = knifeY

            updateScore()
            knifeThrown = false

            currentKnifeIndex++
            if (currentKnifeIndex < knives.size) {
                knives[currentKnifeIndex].visibility = View.VISIBLE
            }
            else {
                //TODO: peaceful level end but above if must be to levels knife number (this happens when all knifes are thrown)
            }
        }
    }

    private fun updateScore() {
        score++
        scoreTextView.text = score.toString()
    }

    @Suppress("DEPRECATION")
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

    @Suppress("DEPRECATION")
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