
package com.example.candycrush;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int NO_OF_BLOCKS = 8;
    private static final int INTERVAL = 200;
    private static final int INITIAL_TIME = 60;
    private static final int SCORE_INCREMENT = 3;
    private static final int TIME_BONUS = 2;


    private final int[] items = {
            R.drawable.blue,
            R.drawable.green,
            R.drawable.red,
            R.drawable.purple,
            R.drawable.yellow,
            R.drawable.orange
    };
    private final int notItem = R.drawable.transparent;
    private final ArrayList<ImageView> itemViews = new ArrayList<>();
    private int widthOfBlock, widthOfScreen;
    private int itemToBeDragged, itemToBeReplaced;
    private Handler gameHandler;
    private TextView scoreResult;
    private int score = 0;
    private TextView timerTextView;
    private int remainingTime = INITIAL_TIME;
    private Handler timerHandler;
    private Runnable timerRunnable;
    private boolean isGameOver = false;
    private Random random = new Random();
    private ImageView clearButton;
    private boolean isSpkOn = true; // Trạng thái ban đầu
    private MediaPlayer mediaPlayer;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setUpBoard();
        setUpTimer();
        setUpTouchListeners();

        clearButton = findViewById(R.id.clearButton);
        mediaPlayer = MediaPlayer.create(this, R.raw.nen); // Thay background_music bằng tên file nhạc của bạn
        mediaPlayer.start();
        mediaPlayer.setLooping(true); // Để nhạc phát lặp lại

        // Đặt sự kiện click cho ImageView
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSpkOn) {
                    clearButton.setImageResource(R.drawable.spkoff); // Đổi sang icon tắt
                    mediaPlayer.pause(); // Tắt nhạc
                } else {
                    clearButton.setImageResource(R.drawable.spkon); // Đổi sang icon bật
                    mediaPlayer.start(); // Mở nhạc
                }
                isSpkOn = !isSpkOn; // Đổi trạng thái
            }
        });

        gameHandler = new Handler();
        startGameLoop();
    }

    private void initializeViews() {
        scoreResult = findViewById(R.id.score);
        timerTextView = findViewById(R.id.timer);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        widthOfScreen = displayMetrics.widthPixels;
        widthOfBlock = widthOfScreen / NO_OF_BLOCKS;
    }

    private void setUpBoard() {
        GridLayout gridLayout = findViewById(R.id.board);
        gridLayout.setRowCount(NO_OF_BLOCKS);
        gridLayout.setColumnCount(NO_OF_BLOCKS);
        gridLayout.getLayoutParams().width = widthOfScreen;
        gridLayout.getLayoutParams().height = widthOfScreen;

        for (int i = 0; i < NO_OF_BLOCKS * NO_OF_BLOCKS; i++) {
            ImageView imageView = new ImageView(this);
            imageView.setId(i);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(widthOfBlock, widthOfBlock));
            imageView.setMaxHeight(widthOfBlock);
            imageView.setMaxWidth(widthOfBlock);
            int randomItem = random.nextInt(items.length);
            imageView.setImageResource(items[randomItem]);
            imageView.setTag(items[randomItem]);
            itemViews.add(imageView);
            gridLayout.addView(imageView);
        }
    }

private void setUpTimer() {
    timerHandler = new Handler();
    timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (remainingTime > 0) {
                remainingTime--;
                timerTextView.setText("Time: " + remainingTime);
                timerHandler.postDelayed(this, 1000);
            } else {
                gameOver();
            }
        }
    };
    timerHandler.postDelayed(timerRunnable, 1000);
}
    @SuppressLint("ClickableViewAccessibility")
    private void setUpTouchListeners() {
        for (final ImageView imageView : itemViews) {
            imageView.setOnTouchListener(new OnSwipeListener(this) {
                @Override
                void onSwipeLeft() {
                    performSwipe(imageView.getId(), -1);
                }

                @Override
                void onSwipeRight() {
                    performSwipe(imageView.getId(), 1);
                }

                @Override
                void onSwipeTop() {
                    performSwipe(imageView.getId(), -NO_OF_BLOCKS);
                }

                @Override
                void onSwipeBottom() {
                    performSwipe(imageView.getId(), NO_OF_BLOCKS);
                }
            });
        }
    }


    private void performSwipe(int id, int offset) {
        itemToBeDragged = id;
        itemToBeReplaced = itemToBeDragged + offset;
        if (isValidSwap(itemToBeDragged, itemToBeReplaced)) {
            itemInterchange();
            if (!checkForMatches()) {
                // Nếu không có kết hợp, hoàn tác việc hoán đổi
                itemInterchange();
            }
        }
    }

    private boolean isValidSwap(int first, int second) {
        // Kiểm tra xem việc hoán đổi có hợp lệ không
        int firstRow = first / NO_OF_BLOCKS;
        int firstCol = first % NO_OF_BLOCKS;
        int secondRow = second / NO_OF_BLOCKS;
        int secondCol = second % NO_OF_BLOCKS;

        return Math.abs(firstRow - secondRow) + Math.abs(firstCol - secondCol) == 1;
    }

    private boolean checkForMatches() {
        boolean hasMatches = false;
        // Kiểm tra kết hợp theo hàng
        for (int i = 0; i < NO_OF_BLOCKS * NO_OF_BLOCKS - 2; i++) {
            if (i % NO_OF_BLOCKS < NO_OF_BLOCKS - 2) {
                hasMatches |= checkAdjacentItems(i, i + 1, i + 2);
            }
        }
        // Kiểm tra kết hợp theo cột
        for (int i = 0; i < NO_OF_BLOCKS * (NO_OF_BLOCKS - 2); i++) {
            hasMatches |= checkAdjacentItems(i, i + NO_OF_BLOCKS, i + 2 * NO_OF_BLOCKS);
        }
        return hasMatches;
    }

    private boolean checkAdjacentItems(int first, int second, int third) {
        int firstItem = (int) itemViews.get(first).getTag();
        return firstItem != notItem &&
                firstItem == (int) itemViews.get(second).getTag() &&
                firstItem == (int) itemViews.get(third).getTag();
    }

    private void startGameLoop() {
        gameHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!isGameOver) {
                    checkMatches();
                    moveDownItems();
                    gameHandler.postDelayed(this, INTERVAL);
                }
            }
        });
    }

    private void checkMatches() {
        checkRowsForMatches();
//        checkColumnForThree();
        checkColumnsForMatches();
    }

//    private void checkRowForThree() {
//        for (int i = 0; i < 62; i++) {
//            int chosenItem = (int) itemViews.get(i).getTag();
//            boolean isBlank = chosenItem == notItem;
//            Integer[] notValid = {6, 7, 14, 15, 22, 23, 30, 31, 38, 39, 46, 47, 54, 55};
//            List<Integer> list = Arrays.asList(notValid);
//            if (!list.contains(i) && !isBlank) {
//                int x = i;
//                if ((int) itemViews.get(x + 1).getTag() == chosenItem &&
//                        (int) itemViews.get(x + 2).getTag() == chosenItem) {
//                    updateScore();
//                    clearItems(x, x + 1, x + 2);
//                }
//            }
//        }
//    }
private void checkRowsForMatches() {
    // Check for matches of 5
    for (int i = 0; i < 59; i++) {
        int chosenItem = (int) itemViews.get(i).getTag();
        boolean isBlank = chosenItem == notItem;
        // Calculate if we're near the right edge
        int row = i / NO_OF_BLOCKS;
        int col = i % NO_OF_BLOCKS;

        // Only check if we have room for 5 items and not at edge
        if (col <= NO_OF_BLOCKS - 5 && !isBlank) {
            if (checkSequenceHorizontal(i, 5, chosenItem)) {
                updateScoreWithMultiplier(5);
                clearItems(i, i + 1, i + 2, i + 3, i + 4);
                continue; // Skip checking for smaller matches if we found a match of 5
            }
        }

        // Check for matches of 4
        if (col <= NO_OF_BLOCKS - 4 && !isBlank) {
            if (checkSequenceHorizontal(i, 4, chosenItem)) {
                updateScoreWithMultiplier(4);
                clearItems(i, i + 1, i + 2, i + 3);
                continue; // Skip checking for matches of 3 if we found a match of 4
            }
        }

        // Check for matches of 3
        if (col <= NO_OF_BLOCKS - 3 && !isBlank) {
            if (checkSequenceHorizontal(i, 3, chosenItem)) {
                updateScoreWithMultiplier(3);
                clearItems(i, i + 1, i + 2);
            }
        }
    }
}

    // Helper method to check a horizontal sequence of matching items
    private boolean checkSequenceHorizontal(int startIndex, int count, int targetItem) {
        for (int i = 1; i < count; i++) {
            if ((int) itemViews.get(startIndex + i).getTag() != targetItem) {
                return false;
            }
        }
        return true;
    }

    private void checkColumnsForMatches() {
        // Kiểm tra cho 5 phần tử
        for (int i = 0; i < 24; i++) {  // 24 = (8*8) - (8*3), vì cần 4 ô phía dưới
            int chosenItem = (int) itemViews.get(i).getTag();
            if (chosenItem != notItem &&
                    checkSequence(i, NO_OF_BLOCKS, 5, chosenItem)) {
                updateScoreWithMultiplier(5);
                clearItems(i, i + NO_OF_BLOCKS, i + 2 * NO_OF_BLOCKS,
                        i + 3 * NO_OF_BLOCKS, i + 4 * NO_OF_BLOCKS);
                continue;  // Tránh đếm trùng với matches nhỏ hơn
            }
        }

        // Kiểm tra cho 4 phần tử
        for (int i = 0; i < 32; i++) {  // 32 = (8*8) - (8*2), vì cần 3 ô phía dưới
            int chosenItem = (int) itemViews.get(i).getTag();
            if (chosenItem != notItem &&
                    checkSequence(i, NO_OF_BLOCKS, 4, chosenItem)) {
                updateScoreWithMultiplier(4);
                clearItems(i, i + NO_OF_BLOCKS, i + 2 * NO_OF_BLOCKS,
                        i + 3 * NO_OF_BLOCKS);
                continue;
            }
        }

        // Kiểm tra cho 3 phần tử
        for (int i = 0; i < 47; i++) {
            int chosenItem = (int) itemViews.get(i).getTag();
            if (chosenItem != notItem &&
                    checkSequence(i, NO_OF_BLOCKS, 3, chosenItem)) {
                updateScoreWithMultiplier(3);
                clearItems(i, i + NO_OF_BLOCKS, i + 2 * NO_OF_BLOCKS);
            }
        }
    }

    // Hàm helper để kiểm tra một chuỗi các phần tử giống nhau
    private boolean checkSequence(int startIndex, int step, int count, int targetItem) {
        for (int i = 1; i < count; i++) {
            int nextIndex = startIndex + (step * i);
            if ((int) itemViews.get(nextIndex).getTag() != targetItem) {
                return false;
            }
        }
        return true;
    }

    // Cập nhật điểm với множитель dựa trên số lượng phần tử matching
    private void updateScoreWithMultiplier(int matchCount) {
        int baseScore = SCORE_INCREMENT;
        int multiplier;
        int timeBonus;

        switch (matchCount) {
            case 5:
                multiplier = 9;  // Triple điểm cho 5 matching
                timeBonus = 5;   // Thêm 5 giây
                break;
            case 4:
                multiplier = 6;  // Double điểm cho 4 matching
                timeBonus = 3;   // Thêm 3 giây
                break;
            default:
                multiplier = 3;  // Điểm thông thường cho 3 matching
                timeBonus = TIME_BONUS;
        }

        score += ( multiplier);
        remainingTime += timeBonus;
        scoreResult.setText(String.valueOf(score));
    }

//    private void updateScore() {
//        score += SCORE_INCREMENT;
//        remainingTime += TIME_BONUS;
//        scoreResult.setText(String.valueOf(score));
//    }

    private void clearItems(int... positions) {
        for (int position : positions) {
            itemViews.get(position).setImageResource(notItem);
            itemViews.get(position).setTag(notItem);
        }
    }

    private void moveDownItems() {
        boolean itemsMoved = false;
        for (int i = 55; i >= 0; i--) {
            if ((int) itemViews.get(i + NO_OF_BLOCKS).getTag() == notItem) {
                itemViews.get(i + NO_OF_BLOCKS).setImageResource((int) itemViews.get(i).getTag());
                itemViews.get(i + NO_OF_BLOCKS).setTag(itemViews.get(i).getTag());
                itemViews.get(i).setImageResource(notItem);
                itemViews.get(i).setTag(notItem);
                itemsMoved = true;
            }
        }

        for (int i = 0; i < NO_OF_BLOCKS; i++) {
            if ((int) itemViews.get(i).getTag() == notItem) {
                int randomItem = random.nextInt(items.length);
                itemViews.get(i).setImageResource(items[randomItem]);
                itemViews.get(i).setTag(items[randomItem]);
                itemsMoved = true;
            }
        }

        if (itemsMoved) {
            checkMatches();
        }
    }

    private void itemInterchange() {
        int background = (int) itemViews.get(itemToBeReplaced).getTag();
        int background1 = (int) itemViews.get(itemToBeDragged).getTag();
        itemViews.get(itemToBeDragged).setImageResource(background);
        itemViews.get(itemToBeReplaced).setImageResource(background1);
        itemViews.get(itemToBeDragged).setTag(background);
        itemViews.get(itemToBeReplaced).setTag(background1);
    }

    private void gameOver() {
        isGameOver = true;
        timerHandler.removeCallbacks(timerRunnable);
        gameHandler.removeCallbacksAndMessages(null);

        SQLiteDatabase db = this.openOrCreateDatabase("highscores", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS highscores (score INTEGER)");
        db.execSQL("INSERT INTO highscores (score) VALUES (" + score + ")");

        Intent intent = new Intent(MainActivity.this, GameOverActivity.class);
        intent.putExtra("SCORE", score);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
        gameHandler.removeCallbacksAndMessages(null);
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Dừng nhạc
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isSpkOn = true;
        }
        else {
            isSpkOn = false;
        };
        // Dừng bộ đếm thời gian
        timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tiếp tục phát nhạc
        if (mediaPlayer != null && isSpkOn) {
            mediaPlayer.start();
        }
        else {
            mediaPlayer.pause();
        };
        timerHandler.postDelayed(timerRunnable, 1000);
    }

}