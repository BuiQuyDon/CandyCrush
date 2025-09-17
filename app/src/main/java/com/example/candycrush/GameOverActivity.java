package com.example.candycrush;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class GameOverActivity extends AppCompatActivity {

    Button playAgainButton, highScoreButton;
    TextView scoreText;
    int score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        playAgainButton = findViewById(R.id.playAgainButton);
        highScoreButton = findViewById(R.id.highScoreButton);
        scoreText = findViewById(R.id.scoreTextView);

        // Lấy điểm từ Intent
        score = getIntent().getIntExtra("SCORE", 0);
        scoreText.setText("Your Score: " + score);

        // Xử lý sự kiện khi nhấn nút Play Again
        playAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GameOverActivity.this, MainActivity.class);
                startActivity(intent);
                finish();  // Đóng GameOverActivity
            }
        });

        // Xử lý sự kiện khi nhấn nút High Score
        highScoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GameOverActivity.this, HighScoreActivity.class);
                startActivity(intent);
                finish();  // Đóng GameOverActivity
            }
        });
    }
}
