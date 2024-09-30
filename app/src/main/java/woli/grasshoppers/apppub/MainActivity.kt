package woli.grasshoppers.apppub

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val birdButton = findViewById<Button>(R.id.buttonBird)
        val knifeButton = findViewById<Button>(R.id.buttonKnife)
        val snakeButton = findViewById<Button>(R.id.buttonSnake)
        val pacmanButton = findViewById<Button>(R.id.buttonPacman)

        birdButton.setOnClickListener {
            val intent = Intent(this, BirdActivity::class.java)
            startActivity(intent)
        }

        knifeButton.setOnClickListener {
            val intent = Intent(this, KnifeActivity::class.java)
            startActivity(intent)
        }

        snakeButton.setOnClickListener {
            val intent = Intent(this, SnakeActivity::class.java)
            startActivity(intent)
        }

        pacmanButton.setOnClickListener {
            val intent = Intent(this, PacmanActivity::class.java)
            startActivity(intent)
        }
    }
}
