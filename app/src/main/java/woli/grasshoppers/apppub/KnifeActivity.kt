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

//TODO list: levels, control knife numbers to throw -> small graphics, level number, apples, difficulty, background image, ładne odbijanie noża

class KnifeActivity : AppCompatActivity() {

    private lateinit var scoreTextView: TextView
    private lateinit var target: ImageView
    private lateinit var appleTextView: TextView
    private lateinit var screenView: View
    private var score = 0
    private var apples = 0
    private var diffLevel: Int = 50
    private var targetY: Int = 200
    private val stuckKnives = mutableListOf<ImageView>()
    private var knifeAngle: Float = 0f
    private val knifeAngles = mutableListOf<Float>()
    private val knives = mutableListOf<ImageView>()
    private var currentKnifeIndex = 0
    private val tolerance = 15.0
    private var knivesAmount: Int = 7
    //TODO: use of tables for knives amount, apples amount, original knives, speed, movement type OR one configuration table
    //TODO: limit after the end of levels to end the whole game
    //TODO: why aren't knew levels initialized fully -> probably some trash from the previous level
    //TODO: the knife should be under the target
    //TODO: show levels number in UI
    private var levelCount: Int = 0
    private val configuration = arrayOf(
        //knifeAmount, appleAmount, originalKnives, duration[ms], rotation[deg], movementType
        arrayOf(7, 1, 0, 2000, 360, 0),//1 TODO: some movements changed the direction -> as a new field in conf table
        arrayOf(9, 2, 1, 3000, 360, 0),//2 TODO: rozmieszczenie noży i jabłek: równomierne: ilość/360deg = co ile deg
        arrayOf(10, 1, 3, 4000, 480, 0),//3
        arrayOf(8, 2, 3, 5000, 860, 3),//4
        arrayOf(10, 10, 0, 6000, 720, 3),//5
        arrayOf(9, 1, 4, 4000, 400, 3),//6
        arrayOf(11, 2, 2, 5000, 405, 3),//7
        arrayOf(8, 3, 3, 3000, 540, 3),//8
        arrayOf(10, 4, 1, 3000, 270, 3),//9
        arrayOf(10, 10, 0, 3000, 360, 0),//10
        arrayOf(10, 2, 1, 3000, 360, 0),//11
        arrayOf(11, 5, 0, 1000, 75, 3),//12
        arrayOf(12, 3, 3, 3000, 450, 3),//13
        arrayOf(9, 2, 3, 3500, 540, 3),//14
        arrayOf(10, 10, 0, 4000, 225),//15
        arrayOf(11, 2, 1, 3750, 450, 3),//16
        arrayOf(12, 4, 0, 6000, 432, 3),//17
        arrayOf(11, 1, 2, 3000, 730, 3),//18
        arrayOf(12, 4, 2, 3000, 215, 3),//19
        arrayOf(10, 10, 0, 4000, 395, 3)//20 TODO:add more levels
    )
    private var knifeThrown: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_knife)

        hideSystemBars()
        diffLevel = getDiff()
        initLevel(levelCount)

        target.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                targetY = target.top
                target.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    private fun initLevel(levelIndex: Int) {
        if (levelIndex < configuration.size) {
            val levelConfig = configuration[levelIndex]

            val knifeAmount = levelConfig[0]
            val appleAmount = levelConfig[1]
            val originalKnives = levelConfig[2]
            val duration = levelConfig[3]
            val rotation = levelConfig[4]
            val movementType = levelConfig[5]

            currentKnifeIndex = 0
            knivesAmount = knifeAmount

            initUI()

            initTarget(duration.toLong(), rotation.toFloat(), movementType)
        } else {
            //TODO: here initialize what happens when all levels are finished, and propably no where else
        }
    }

    private fun initUI() {
        target = findViewById(R.id.tarcza)
        scoreTextView = findViewById(R.id.score_text)
        appleTextView = findViewById(R.id.apple_text)
        screenView = findViewById(R.id.screen_view)

        target.visibility = View.VISIBLE

        for (i in 0 until knivesAmount) {
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

        knives[0].visibility = View.VISIBLE

        //TODO: retrieve apple amount and set its text as so: apples = retrieved amount; updateApples(apples) where retrieved by a get function as at the end

        screenView.setOnClickListener {
            if (currentKnifeIndex < knives.size && knives[currentKnifeIndex].visibility == View.VISIBLE) {
                throwKnife(knives[currentKnifeIndex])
            }
        }
    }

    private fun initTarget(duration: Long, rotation: Float, movementType: Int) { //TODO: pewnie tutaj jedno z kilku użyć współczynnika trudności
        val finalRotation = if ((0..1).random() == 0) rotation else -rotation
        val interpolator = when (movementType) { //TODO: clear this value or delete old something because the target on upcomming levels are mixing motions as I think and knives are mixed I think
            0 -> LinearInterpolator()
            1 -> AccelerateInterpolator()//deprecated -> do not use
            2 -> DecelerateInterpolator()//deprecated -> do not use
            3 -> AccelerateDecelerateInterpolator()
            else -> BounceInterpolator()
        }

        fun startRotationAnimation() { //TODO: współczynnik trudności reguluje np. ilość obrotów do czasu
            val currentRotation = target.rotation
            val animator = ObjectAnimator.ofFloat(target, "rotation", currentRotation, currentRotation + finalRotation)
            animator.duration = duration
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

        val screenWidth = screenView.width
        val targetWidth = target.width
        target.x = (screenWidth / 2 - targetWidth / 2).toFloat()

        val screenHeight = screenView.height
        target.y = (screenHeight * 37 / 100 - target.height / 2).toFloat() //TODO: for some reason has no impact on the first level propably because of its existance in xml -> maybe delete it in xml

        target.visibility = View.VISIBLE

        startRotationAnimation()

        //TODO: jeszcze trzeba noże doprowadzić do punktu początkowego i usunąć jakieś chyba duchy
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
            knifeThrown = true

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
        knifeToCheck.getGlobalVisibleRect(knifeBounds)//TODO: czy to powinna na pewno być ta funkcja...? raczej zdecydowanie nie ale jest jeszcze używana do kontaktu z tarczą

        for (stuckAngle in knifeAngles) {

            val centerX = target.x + target.width / 2
            val centerY = target.y + target.height / 2
            val dx = knifeToCheck.x + knifeToCheck.width / 2 - centerX
            val dy = knifeToCheck.y + knifeToCheck.height / 2 - centerY

            val relativeAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
            knifeAngle = (relativeAngle - target.rotation) % 360

            if (knifeAngle in (stuckAngle - tolerance)..(stuckAngle + tolerance)) {
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
                levelCount++
                clearLevel()
            }
        }
    }

    private fun clearLevel() {
        val targetAnimator = ObjectAnimator.ofFloat(target, "translationY", target.translationY, target.translationY + target.height + screenView.height)
        targetAnimator.duration = 500
        targetAnimator.interpolator = AccelerateInterpolator()

        targetAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                target.visibility = View.GONE

                for (knifeToDelete in knives) {
                    knifeToDelete.visibility = View.GONE
                }

                knives.clear()

                levelCount++
                initLevel(levelCount)
            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationRepeat(animation: Animator) {}
        })

        targetAnimator.start()
    }

    private fun updateScore() {
        score++
        scoreTextView.text = score.toString()
    }

    private fun updateApples(amount: Int) {
        apples = amount
        appleTextView.text = apples.toString()
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        passScore(score)//TODO: pewnie kilka wartośći (wynik, ilość noży, jabłka) ale tylko jedna wyświetlana
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