package woli.grasshoppers.apppub

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
            Toast.makeText(this, "Bird button clicked!", Toast.LENGTH_SHORT).show()
        }

        knifeButton.setOnClickListener {
            Toast.makeText(this, "Knife button clicked!", Toast.LENGTH_SHORT).show()
        }

        snakeButton.setOnClickListener {
            Toast.makeText(this, "Snake button clicked!", Toast.LENGTH_SHORT).show()
        }

        pacmanButton.setOnClickListener {
            Toast.makeText(this, "Pacman button clicked!", Toast.LENGTH_SHORT).show()
        }
    }
}
