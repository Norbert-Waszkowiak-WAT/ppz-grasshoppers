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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

//TODO list: levels, control knife numbers to throw -> small graphics, apples, difficulty, dziwne przesunięcie podczas rzutu po wbiciu, background image, knifes intersection, ładne odbijanie noża

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
    private var knivesAmount: Int = 7 //TODO: To powinna być tablica dla odpowiednich poziomów
    //TODO: tables for knives amount, apples amount, original knives, speed, movement type OR one configuration table
    //TODO: limit after the end of levels to end the whole game
    //TODO: disable throwing a knife after levels limit -> can achieve score 8 with 7 knifes
    //TODO: why aren't knew levels initialized
    private var levelCount: Int = 0
    private val configuration = arrayOf(
        //knifeAmount, appleAmount, originalKnives, duration[ms], rotation[deg], movementType
        arrayOf(7, 1, 0, 2000, 360, 0),//1 TODO: some movements changed the direction
        arrayOf(9, 2, 1, 3000, 360, 0),//2 TODO: rozmieszczenie noży
        arrayOf(10, 1, 3, 4000, 480, 0),//3
        arrayOf(8, 2, 3, 5000, 860, 3),//4
        arrayOf(10, 10, 0, 6000, 720, 3),//5
        arrayOf(9, 1, 4, 4000, 400, 3),//6
        arrayOf(11, 2, 2, 5000, 405, 3),//7
        arrayOf(8, 3, 3, 3000, 540, 3),//8
        arrayOf(10, 4, 1, 3000, 270, 3),//9
        arrayOf(10, 10, 0, 3000, 360, 0),//10
        arrayOf(10, 2, 1, 3000, 360, 0),//11
        arrayOf(11, 5, 0, 1000, 75, 3),//12 TODO:add more levels
        arrayOf(12, 3, 3, 3000, 450, 3),//13
        arrayOf(9, 2, 3, 3500, 540, 3),//14
        arrayOf(10, 10, 0, 4000, 225),//15
        arrayOf(11, 2, 1, 3750, 450, 3),//16
        arrayOf(12, 4, 0, 6000, 432, 3),//17
        arrayOf(11, 1, 2, 3000, 730, 3),//18
        arrayOf(12, 4, 2, 3000, 215, 3),//19
        arrayOf(10, 10, 0, 4000, 395, 3)//20
    )
    private var knifeThrown: Boolean = false

    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_knife)

        hideSystemBars()
        initUI()
        initTarget()
        //initKnife()
        //TODO: master function initLevel() which contains initTarget and initUI with params i.e. knife number, target motion type, speed and range of one rotation, apple number etc.
        //initUI contains the process of making amount of knives maybe move to one function -> will be easier to implement knives?

        diffLevel = getDiff()

        target.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                targetY = target.top
                target.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_knife)

        hideSystemBars()
        diffLevel = getDiff()
        initLevel(levelCount)
        //initUI()

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

            // Odczytanie wartości z tablicy configuration
            val knifeAmount = levelConfig[0] as Int
            val appleAmount = levelConfig[1] as Int
            val originalKnives = levelConfig[2] as Int
            val duration = levelConfig[3] as Int
            val rotation = levelConfig[4] as Int
            val movementType = levelConfig[5] as Int

            // Ustawienie liczby noży
            currentKnifeIndex = 0
            knivesAmount = knifeAmount

            // Inicjalizacja UI dla aktualnego poziomu
            initUI() // Możesz dostosować tę metodę, aby przyjmowała parametry

            // Ustawienie ruchu celu
            initTarget(duration.toLong(), rotation.toFloat(), movementType)
        } else {
            // Obsługa końca poziomów
            // Możesz tutaj dodać logikę, co się stanie, gdy wszystkie poziomy zostaną ukończone
        }
    }

    private fun initUI() {
        target = findViewById(R.id.tarcza)
        knife = findViewById(R.id.knife)
        scoreTextView = findViewById(R.id.score_text)
        screenView = findViewById(R.id.screen_view)

        target.visibility = View.VISIBLE
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

        screenView.setOnClickListener {
            if (currentKnifeIndex < knives.size && knives[currentKnifeIndex].visibility == View.VISIBLE) {
                throwKnife(knives[currentKnifeIndex])
            }
        }
    }

    private fun initTarget(duration: Long, rotation: Float, movementType: Int) { //TODO: pewnie tutaj jedno z kilku użyć współczynnika trudności
        /*val randomDuration = (1000..3000).random().toLong()
        val randomRotation = (30..720).random().toFloat()
        val finalRotation = if ((0..1).random() == 0) randomRotation else -randomRotation
        val interpolator = when ((0..4).random()) { //TODO: usunąć niektóre ruchy? przenieść do initLevel jako wybór a listy
            0 -> LinearInterpolator()
            1 -> AccelerateInterpolator()
            2 -> DecelerateInterpolator()
            3 -> AccelerateDecelerateInterpolator()
            else -> BounceInterpolator()
        }*/
        val finalRotation = if ((0..1).random() == 0) rotation else -rotation
        //val interpolator = when ((0..4).random()) {
        val interpolator = when (movementType) {
            0 -> LinearInterpolator()
            1 -> AccelerateInterpolator()
            2 -> DecelerateInterpolator()
            3 -> AccelerateDecelerateInterpolator()
            else -> BounceInterpolator()
        }

        fun startRotationAnimation() { //TODO: współczynnik trudności reguluje np. ilość obrotów do czasu
            val currentRotation = target.rotation
            val animator = ObjectAnimator.ofFloat(target, "rotation", currentRotation, currentRotation + finalRotation)
            //animator.duration = randomDuration
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

        // Wyśrodkowanie celu w poziomie
        val screenWidth = screenView.width//resources.displayMetrics.widthPixels
        val targetWidth = target.width
        target.x = (screenWidth / 2 - targetWidth / 2).toFloat()

        // Umieszczenie celu na wysokości 2/5 wysokości ekranu
        val screenHeight = screenView.height//resources.displayMetrics.heightPixels
        target.y = (screenHeight * 37 / 100 - target.height / 2).toFloat() //TODO: z jakiegoś powodu nic nie zmienia

        target.visibility = View.VISIBLE

        startRotationAnimation()

        //TODO: jeszcze trzeba noże doprowadzić do punktu początkowego
    }

    /*private fun initKnife() {
        screenView.setOnClickListener {
            if (currentKnifeIndex < knives.size && knives[currentKnifeIndex].visibility == View.VISIBLE) {
                throwKnife(knives[currentKnifeIndex])
            }
        }
    }*/

    private fun throwKnife(knifeToThrow: ImageView) {
        if (!knifeThrown) {//TODO: dostosować wysokość rzutu
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
        knifeToCheck.getGlobalVisibleRect(knifeBounds)//TODO: czy to powinna na pewno być ta funkcja...? raczej zdecydowanie nie

        //for (stuckKnife in stuckKnives) {
        for (stuckAngle in knifeAngles) {
            //val stuckKnifeBounds = Rect()
            //stuckKnife.getGlobalVisibleRect(stuckKnifeBounds)

            val centerX = target.x + target.width / 2
            val centerY = target.y + target.height / 2
            val dx = knifeToCheck.x + knifeToCheck.width / 2 - centerX
            val dy = knifeToCheck.y + knifeToCheck.height / 2 - centerY

            val relativeAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
            knifeAngle = (relativeAngle - target.rotation) % 360

            //if (Rect.intersects(knifeBounds, stuckKnifeBounds)) {//TODO: checking by knifeAngles
            if (knifeAngle == stuckAngle) { //TODO: some toleration
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
                levelCount++
                clearLevel()
                /*if (levelCount <= configuration.size) {
                    initLevel(levelCount)
                }
                else {
                    //TODO: end game
                }*/
            }
        }
    }

    private fun clearLevel() {
        //TODO: czyszczenie obrazu czyli usunięcie noży conajmniej być może i celu
        //TODO: tutaj rozpadanie tarczy i wypadanie noży
        // Stworzenie animatora dla tarczy
        val targetAnimator = ObjectAnimator.ofFloat(target, "translationY", target.translationY, target.translationY + target.height + screenView.height)
        targetAnimator.duration = 500 // Czas trwania animacji
        targetAnimator.interpolator = AccelerateInterpolator() // Możesz zmienić interpolator, jeśli chcesz

        // Stworzenie animatorów dla wbitych noży
        /*val knifeAnimators = stuckKnives.map { knife ->
            ObjectAnimator.ofFloat(knife, "translationY", knife.translationY, knife.translationY + knife.height)
        }*/

        // Ustawienie tego samego czasu trwania dla wszystkich noży
        //knifeAnimators.forEach { animator -> animator.duration = 500 }

        //TODO: disable the last knife to fly after clearLevel() called

        // Po zakończeniu animacji tarczy, ukryj ją i noże oraz wywołaj initLevel
        targetAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                // Ukrycie tarczy po animacji
                target.visibility = View.GONE

                for (knifeToDelete in knives) {
                    knifeToDelete.visibility = View.GONE
                }

                knives.clear()

                // Inicjalizacja nowego poziomu
                levelCount++
                if (levelCount < configuration.size) {
                    initLevel(levelCount)
                } else {
                    // TODO: Obsługa końca gry, gdy wszystkie poziomy zostały ukończone
                }

                // Ukrycie noży po animacji
                /*knifeAnimators.forEach { animator ->
                    animator.addListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {}

                        override fun onAnimationEnd(animation: Animator) {
                            knife.visibility = View.GONE
                        }

                        override fun onAnimationCancel(animation: Animator) {}

                        override fun onAnimationRepeat(animation: Animator) {}
                    })
                    animator.start() // Uruchomienie animacji dla każdego noża
                }*/

                // Po zakończeniu animacji noży, zainicjuj nowy poziom
                //initLevel(levelCount)
            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationRepeat(animation: Animator) {}
        })

        // Rozpocznij animację tarczy
        targetAnimator.start()
    }

    private fun updateScore() {
        score++
        scoreTextView.text = score.toString()
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