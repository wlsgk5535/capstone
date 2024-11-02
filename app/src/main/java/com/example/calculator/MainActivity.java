// MainActivity.java
package com.example.calculator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ResultActivity를 여는 버튼
        Button btnOpenResultActivity = findViewById(R.id.btn_open_result_activity);

        // 버튼 클릭 리스너 설정
        btnOpenResultActivity.setOnClickListener(v -> {
            // ResultActivity 호출을 위한 Intent 생성
            Intent intent = new Intent(MainActivity.this, ResultActivity.class);

            // ResultActivity로 이동
            startActivity(intent);
        });
    }
}

