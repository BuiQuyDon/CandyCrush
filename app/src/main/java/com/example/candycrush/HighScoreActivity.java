package com.example.candycrush;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HighScoreActivity extends AppCompatActivity {

    TextView highScoreTextView;  // Khai báo TextView
    Button playAgainButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_score);  // Thiết lập layout

        highScoreTextView = findViewById(R.id.highScoreTextView);  // Ánh xạ TextView
        playAgainButton = findViewById(R.id.playAgainButton);

        // Lấy điểm cao nhất từ SQLite
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.openOrCreateDatabase("highscores", MODE_PRIVATE, null);
            cursor = db.rawQuery("SELECT MAX(score) AS highscore FROM highscores", null);

            if (cursor.moveToFirst()) {
                @SuppressLint("Range") int highScore = cursor.getInt(cursor.getColumnIndex("highscore"));
                highScoreTextView.setText("Highest Score: " + highScore);  // Hiển thị điểm cao nhất
            } else {
                highScoreTextView.setText("Highest Score: 0");  // Nếu không có điểm
            }
        } catch (Exception e) {
            highScoreTextView.setText("Highest Score: 0");  // Hiển thị số 0 nếu có lỗi
            e.printStackTrace();  // Ghi lỗi ra logcat (tùy chọn)
        } finally {
            if (cursor != null) {
                cursor.close();  // Đảm bảo cursor được đóng
            }
            if (db != null) {
                db.close();  // Đảm bảo database được đóng
            }
        }

        playAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HighScoreActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

}



