package woli.grasshoppers.apppub

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import nl.dionsegijn.konfetti.KonfettiView
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class KnifeActivity : AppCompatActivity() {

    enum class Signal {
        INIT,
        EXIT
    }

    private lateinit var scoreTextView: TextView
    private lateinit var target: ImageView
    private lateinit var appleTextView: TextView
    private lateinit var screenView: View
    private lateinit var levelTextView: TextView
    private lateinit var linearLayout: LinearLayout
    private lateinit var backArrow: ImageView
    private lateinit var gameOverLayout: ConstraintLayout
    private lateinit var gameOverScoreTextView: TextView
    private lateinit var gameOverStageTextView: TextView
    private lateinit var animator: ObjectAnimator
    private lateinit var restartButton: TextView
    private lateinit var continueButton: LinearLayout
    private lateinit var continueCost: TextView
    private lateinit var konfettiView: KonfettiView
    private var score = 0
    private var applesCount = 0
    private var diffLevel: Int = 50
    private var targetY: Int = 200
    private val stuckKnives = mutableListOf<ImageView>()
    private var knifeAngle: Float = 0f
    private val knifeAngles = mutableListOf<Float>()
    private val appleAngles = mutableListOf<Float>()
    private val knives = mutableListOf<ImageView>()
    private val apples = mutableListOf<ImageView>()
    private var currentKnifeIndex = 0
    private val tolerance = 15.0
    private val appleTolerance = 15.0
    private var knivesAmount: Int = 1
    private var levelCount: Int = 0
    private var knifeThrown: Boolean = false
    private var knifeAmount: Int = 1
    private var appleAmount: Int = 0
    private var originalKnives: Int = 0
    private var duration: Int = 0
    private var rotation: Int = 0
    private var movementType: Int = 0
    private var variation: Int = 0
    private var bestScore: Int = 0
    private var knifeInitialX: Float = 0f
    private var knifeInitialY: Float = 0f
    private var targetRotationDirection = 0
    private val configuration = arrayOf(
        //knifeAmount, appleAmount, originalKnives, duration[ms], rotation[deg], movementType, variation
        arrayOf(7, 1, 0, 2000, 360, 0, 1),//1
        arrayOf(9, 2, 1, 3000, 360, 0, 1),//2
        arrayOf(10, 1, 3, 4000, 480, 0, 1),//3
        arrayOf(8, 2, 3, 5000, 860, 3, -1),//4
        arrayOf(10, 10, 0, 6000, 720, 0, 1),//5
        arrayOf(9, 1, 4, 4000, 400, 3, -1),//6
        arrayOf(11, 2, 2, 5000, 405, 3, 1),//7
        arrayOf(8, 3, 3, 3000, 540, 3, 1),//8
        arrayOf(10, 4, 1, 3000, 270, 3, -1),//9
        arrayOf(10, 10, 0, 6000, 720, 0, 1),//10
        arrayOf(10, 2, 1, 3000, 360, 0, -1),//11
        arrayOf(11, 5, 0, 1000, 75, 3, 1),//12
        arrayOf(9, 3, 3, 3000, 450, 3, 1),//13
        arrayOf(9, 2, 3, 3500, 540, 3, -1),//14
        arrayOf(10, 10, 0, 6000, 720, 0, 1),//15
        arrayOf(11, 2, 1, 3750, 450, 3, 1),//16
        arrayOf(12, 4, 0, 6000, 432, 3, 1),//17
        arrayOf(11, 1, 2, 3000, 730, 3, -1),//18
        arrayOf(12, 4, 2, 3000, 215, 3, 1),//19
        arrayOf(10, 10, 0, 6000, 720, 0, 1)//20 TODO:add more levels
    )

    //TODO: czemu czasami kolizje noży nie mają efektu? -> jakieś przesunięcie?
    //TODO: zapisywanie jabłek i wyniku podczas square and swipe oraz sleep button
    //TODO: czemu czasami noże na siebie zachodzą
    //TODO: difficulty

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_knife)

        hideSystemBars()
        diffLevel = intent.getIntExtra("knife_diff", 50)
        initLevel(levelCount)

        target.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                targetY = target.top
                target.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        applesCount = intent.getIntExtra("apple_amount", 0)
        bestScore = intent.getIntExtra("best_score", 0)
        appleTextView.text = applesCount.toString()
    }

    @Suppress("DEPRECATION")
    override fun onDestroy() {
        onBackPressed()
        super.onDestroy()
    }

    override fun onUserLeaveHint() {
        Toast.makeText(this, "onUserLeaveHint", Toast.LENGTH_SHORT).show()
        passScore(score)
        super.onUserLeaveHint()
    }

    private fun initLevel(levelIndex: Int) {
        if (levelIndex < configuration.size) {
            val levelConfig = configuration[levelIndex]

            knifeAmount = levelConfig[0]
            appleAmount = levelConfig[1]
            originalKnives = levelConfig[2]
            duration = levelConfig[3]
            rotation = levelConfig[4]
            movementType = levelConfig[5]
            variation = levelConfig[6]

            currentKnifeIndex = 0
            knivesAmount = knifeAmount

            initUI()

            initTarget(duration.toLong(), rotation.toFloat(), movementType)
        } else {
            makeKonfetti(konfettiView)
            Handler(Looper.getMainLooper()).postDelayed({
                Toast.makeText(this, "You finished the game, Sir", Toast.LENGTH_LONG).show()
                @Suppress("DEPRECATION")
                onBackPressed()
            }, 5000)
        }
    }

    private fun initUI() {
        target = findViewById(R.id.tarcza)
        scoreTextView = findViewById(R.id.score_text)
        appleTextView = findViewById(R.id.apple_text)
        screenView = findViewById(R.id.screen_view)
        levelTextView = findViewById(R.id.level_text)
        linearLayout = findViewById(R.id.linear_layout)
        backArrow = findViewById(R.id.backView)
        gameOverLayout = findViewById(R.id.gameOverLayout)
        gameOverScoreTextView = findViewById(R.id.gameOverScoreText)
        gameOverStageTextView = findViewById(R.id.gameOverStageText)
        restartButton = findViewById(R.id.restartButton)
        continueButton = findViewById(R.id.continueButton)
        continueCost = findViewById(R.id.continueCostTxt)
        konfettiView = findViewById(R.id.konfettiView)

        target.visibility = View.VISIBLE
        gameOverLayout.visibility = View.GONE
        gameOverLayout.isClickable = false

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

            val newKnifeIcon = ImageView(this).apply {
                layoutParams = ConstraintLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    val density = resources.displayMetrics.density
                    width = (20 * density).toInt()
                    height = (20 * density).toInt()
                }
                setImageResource(R.drawable.white_knife)
                visibility = View.VISIBLE
            }
            linearLayout.addView(newKnifeIcon, i)
        }

        knives[0].visibility = View.VISIBLE

        onLevelUp()

        screenView.setOnClickListener {
            if (currentKnifeIndex < knives.size && knives[currentKnifeIndex].visibility == View.VISIBLE) {
                throwKnife(knives[currentKnifeIndex])
            }
        }

        backArrow.setOnClickListener {
            @Suppress("DEPRECATION")
            onBackPressed()
        }

        restartButton.setOnClickListener {
            if (score > bestScore) {
                bestScore = score
            }
            levelCount = -1
            score = 0
            scoreTextView.text = score.toString()
            clearLevel(Signal.INIT)
        }

        continueButton.setOnClickListener {
            if (applesCount < diffLevel) {
                Toast.makeText(this, "Not enaf birs ju haf", Toast.LENGTH_LONG).show()
            }
            else {
                applesCount -= diffLevel
                setApples(applesCount)

                gameOverLayout.visibility = View.GONE
                gameOverLayout.isClickable = false

                knives[currentKnifeIndex].x = knifeInitialX
                knives[currentKnifeIndex].y = knifeInitialY
                knives[currentKnifeIndex].visibility = View.VISIBLE
            }
        }
    }

    private fun initTarget(
        duration: Long,
        rotation: Float,
        movementType: Int
    ) { //TODO: pewnie tutaj jedno z kilku użyć współczynnika trudności
        var finalRotation = if ((0..1).random() == 0) rotation else -rotation

        targetRotationDirection = if (rotation == finalRotation) {
            1
        } else {
            -1
        }

        val interpolator = when (movementType) {
            0 -> LinearInterpolator()
            1 -> AccelerateInterpolator()//deprecated -> do not use
            2 -> DecelerateInterpolator()//deprecated -> do not use
            3 -> AccelerateDecelerateInterpolator()
            else -> BounceInterpolator()
        }

        addOriginalKnives()

        createApples()

        fun startRotationAnimation() { //TODO: współczynnik trudności reguluje np. ilość obrotów do czasu
            val currentRotation = target.rotation
            finalRotation *= variation

            if (targetRotationDirection != 0) {
                targetRotationDirection *= variation
            }

            animator = ObjectAnimator.ofFloat(
                target,
                "rotation",
                currentRotation,
                currentRotation + finalRotation
            )
            animator.duration = duration
            animator.interpolator = interpolator

            animator.addUpdateListener {
                stuckKnives.forEachIndexed { index, knife ->
                    val centerX = target.x + target.width / 2
                    val centerY = target.y + target.height / 2
                    val radius = target.width / 2

                    val currentAngle =
                        Math.toRadians(target.rotation.toDouble() + knifeAngles[index])

                    val knifeX = centerX + radius * cos(currentAngle).toFloat() - knife.width / 2
                    val knifeY = centerY + radius * sin(currentAngle).toFloat() - knife.height / 2

                    knife.x = knifeX
                    knife.y = knifeY

                    knife.rotation = target.rotation + knifeAngles[index] - 90f
                }

                apples.forEachIndexed { index, apple ->
                    val centerX = target.x + target.width / 2
                    val centerY = target.y + target.height / 2
                    val radius = target.width / 2 + apple.height / 2
                    val appleAngle = Math.toRadians(target.rotation.toDouble() + appleAngles[index])

                    val appleX = centerX + radius * cos(appleAngle).toFloat() - apple.width / 2
                    val appleY = centerY + radius * sin(appleAngle).toFloat() - apple.height / 2

                    apple.x = appleX
                    apple.y = appleY

                    apple.rotation = target.rotation + appleAngles[index] + 90f
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
        target.y =
            (screenHeight * 37 / 100 - target.height / 2).toFloat() //TODO: for some reason has no impact on the first level probably because of its existence in xml -> maybe delete it in xml

        target.visibility = View.VISIBLE

        startRotationAnimation()
    }

    private fun createApples() {
        val numberOfApples = appleAmount
        val angleIncrement = 360f / numberOfApples

        for (i in 0 until numberOfApples) {
            val apple = ImageView(this).apply {
                layoutParams = ConstraintLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    val density = resources.displayMetrics.density
                    width = (50 * density).toInt()
                    height = (50 * density).toInt()
                    translationZ = 2F
                }
                setImageResource(R.drawable.beer_apple)
                visibility = View.VISIBLE
            }

            val angle = i * angleIncrement + 25
            appleAngles.add(angle)

            val centerX = target.x + target.width / 2
            val centerY = target.y + target.height / 2
            val radius = target.width / 2 + apple.height / 2

            val appleX = centerX + radius * cos(angle) - apple.width / 2
            val appleY = centerY + radius * sin(angle) - apple.height / 2

            apple.x = appleX
            apple.y = appleY

            (screenView as ViewGroup).addView(apple)
            apples.add(apple)
        }
    }

    private fun addOriginalKnives() {
        val numberOfOriginalKnives = originalKnives
        val angleIncrement = 360f / numberOfOriginalKnives

        for (i in 0 until numberOfOriginalKnives) {
            val originalKnife = ImageView(this).apply {
                layoutParams = ConstraintLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    val density = resources.displayMetrics.density
                    width = (125 * density).toInt()
                    height = (125 * density).toInt()
                }
                setImageResource(R.drawable.original_knife)
                visibility = View.VISIBLE
            }

            val angle = i * angleIncrement
            knifeAngles.add(angle)

            val centerX = target.x + target.width / 2
            val centerY = target.y + target.height / 2
            val radius = target.width / 2
            val knifeX =
                centerX + radius * cos(Math.toRadians(angle.toDouble())).toFloat() - originalKnife.width / 2
            val knifeY =
                centerY + radius * sin(Math.toRadians(angle.toDouble())).toFloat() - originalKnife.height / 2

            originalKnife.x = knifeX
            originalKnife.y = knifeY

            (screenView as ViewGroup).addView(originalKnife)
            stuckKnives.add(originalKnife)
        }
    }

    private fun throwKnife(knifeToThrow: ImageView) {
        if (!knifeThrown) {
            knifeToThrow.visibility = View.VISIBLE

            knifeInitialX = knifeToThrow.x
            knifeInitialY = knifeToThrow.y

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
                val offsetX = if (targetRotationDirection == 1) -750f else 750f
                val offsetY = 2f

                val targetX = knifeToCheck.x + offsetX
                val targetY = knifeToCheck.y + offsetY

                val knifeAnimator = ObjectAnimator.ofFloat(
                    knifeToCheck,
                    "translationX",
                    knifeToCheck.translationX,
                    targetX
                )
                val knifeAnimatorY = ObjectAnimator.ofFloat(
                    knifeToCheck,
                    "translationY",
                    knifeToCheck.translationY,
                    targetY
                )
                val rotationAnimator = ObjectAnimator.ofFloat(
                    knifeToCheck,
                    "rotation",
                    knifeToCheck.rotation,
                    knifeToCheck.rotation + 720
                )

                knifeAnimator.duration = 500
                knifeAnimatorY.duration = 500
                rotationAnimator.duration = 500

                knifeAnimator.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {}

                    @SuppressLint("SetTextI18n")
                    override fun onAnimationEnd(animation: Animator) {
                        knifeToCheck.visibility = View.GONE
                        knifeThrown = false
                        gameOverStageTextView.text = "Stage: " + (levelCount + 1).toString()
                        gameOverScoreTextView.text = score.toString()
                        gameOverLayout.visibility = View.VISIBLE
                        gameOverLayout.isClickable = true
                        continueCost.text = diffLevel.toString()
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

        for (applePos in appleAngles) {
            val centerX = target.x + target.width / 2
            val centerY = target.y + target.height / 2
            val dx = knifeToCheck.x + knifeToCheck.width / 2 - centerX
            val dy = knifeToCheck.y + knifeToCheck.height / 2 - centerY

            val relativeAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
            knifeAngle = (relativeAngle - target.rotation) % 360
            if (knifeAngle < 0) {
                knifeAngle += 360
            }

            val appleIndex = appleAngles.indexOf(applePos)
            val appleToDestroy = apples[appleIndex]

            if (knifeAngle in (applePos - appleTolerance)..(applePos + appleTolerance)) {
                appleToDestroy.setImageResource(R.drawable.beer_apple_squished)
                onAppleHit()

                val randomOffset = Random.nextInt(-100, 100)

                val jumpAnimator = ObjectAnimator.ofFloat(
                    appleToDestroy,
                    "translationY",
                    appleToDestroy.translationY,
                    appleToDestroy.translationY - 150
                )

                val jumpXAnimator = ObjectAnimator.ofFloat(
                    appleToDestroy,
                    "translationX",
                    appleToDestroy.translationX,
                    appleToDestroy.translationX + randomOffset
                )

                val fallAnimator = ObjectAnimator.ofFloat(
                    appleToDestroy,
                    "translationY",
                    appleToDestroy.translationY - 150,
                    appleToDestroy.translationY + screenView.height
                )

                val fallXAnimator = ObjectAnimator.ofFloat(
                    appleToDestroy,
                    "translationX",
                    appleToDestroy.translationX + randomOffset,
                    appleToDestroy.translationX + 2 * randomOffset
                )

                jumpAnimator.duration = 200
                fallAnimator.duration = 750

                jumpAnimator.interpolator = DecelerateInterpolator()
                fallAnimator.interpolator = AccelerateInterpolator()

                val animatorSet = AnimatorSet()
                animatorSet.playTogether(jumpXAnimator, jumpAnimator)
                animatorSet.playTogether(fallAnimator, fallXAnimator)
                animatorSet.play(fallAnimator).after(jumpAnimator)

                animatorSet.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {}

                    override fun onAnimationEnd(animation: Animator) {
                        appleToDestroy.visibility = View.GONE
                    }

                    override fun onAnimationCancel(animation: Animator) {}

                    override fun onAnimationRepeat(animation: Animator) {}
                })

                appleAngles.removeAt(appleIndex)
                apples.removeAt(appleIndex)

                animatorSet.start()

                break
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
            val dx = knifeToCheck.x + knifeToCheck.width / 2 - centerX //TODO: always 0
            val dy = knifeToCheck.y + knifeToCheck.height / 2 - centerY//TODO: const for each level: 1: 157 other: 159

            val relativeAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()//TODO: always 90 deg
            knifeAngle = (relativeAngle - target.rotation) % 360
            if (knifeAngle < 0) {
                knifeAngle += 360
            }
            knifeAngles.add(knifeAngle)

            val radius = target.width / 2
            val knifeX = centerX + radius * cos(Math.toRadians((knifeAngle + target.rotation).toDouble())).toFloat() - knifeToCheck.width / 2
            val knifeY = centerY + radius * sin(Math.toRadians((knifeAngle + target.rotation).toDouble())).toFloat() - knifeToCheck.height / 2

            knifeToCheck.x = knifeX
            knifeToCheck.y = knifeY

            onKnifeStuck()
            knifeThrown = false

            val knifeToChange: ImageView = linearLayout[currentKnifeIndex] as ImageView
            knifeToChange.apply { setImageResource(R.drawable.black_knife) }

            currentKnifeIndex++
            if (currentKnifeIndex < knives.size) {
                knives[currentKnifeIndex].visibility = View.VISIBLE
            } else {
                clearLevel(Signal.INIT)
            }
        }
    }

    private fun clearLevel(signal: Signal) {
        val targetClearAnimator = ObjectAnimator.ofFloat(
            target,
            "translationY",
            target.translationY,
            target.translationY + target.height + screenView.height
        )
        targetClearAnimator.duration = 500
        targetClearAnimator.interpolator = AccelerateInterpolator()

        targetClearAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                target.visibility = View.GONE

                for (knifeToDelete in knives) {
                    knifeToDelete.visibility = View.GONE
                }

                knives.clear()
                stuckKnives.clear()
                knifeAngles.clear()
                apples.clear()
                appleAngles.clear()
                linearLayout.removeAllViews()

                animator.pause()

                when (signal) {
                    Signal.INIT -> {
                        levelCount++
                        initLevel(levelCount)
                    }

                    Signal.EXIT -> {
                        @Suppress("DEPRECATION")
                        onBackPressed()
                    }
                }
            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationRepeat(animation: Animator) {}
        })

        targetClearAnimator.start()
    }

    private fun onKnifeStuck() {
        score++
        scoreTextView.text = score.toString()
    }

    private fun onAppleHit() {
        applesCount++
        appleTextView.text = applesCount.toString()
    }

    private fun setApples(amount: Int) {
        applesCount = amount
        appleTextView.text = applesCount.toString()
    }

    private fun onLevelUp() {
        val levelToSet = levelCount + 1
        levelTextView.text = levelToSet.toString()
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        passScore(score)
        clearLevel(Signal.EXIT)
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
        if (score > bestScore) {
            data.putExtra("score", score.toString())
        } else {
            data.putExtra("score", bestScore.toString())
        }
        data.putExtra("apple_amount", applesCount)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    private fun makeKonfetti(view: KonfettiView) {
        view.build()
            .addColors(Color.rgb(255, 36, 0), Color.rgb(120, 116, 242), Color.rgb(255, 232, 103))
            .setDirection(0.0, 359.0)
            .setSpeed(1f, 5f)
            .setFadeOutEnabled(true)
            .setTimeToLive(2000L)
            .addShapes(Shape.Square, Shape.Circle)
            .addSizes(Size(12))
            .setPosition(-50f, view.width + 50f, -50f, -50f)
            .streamFor(300, 5000L)
    }
}