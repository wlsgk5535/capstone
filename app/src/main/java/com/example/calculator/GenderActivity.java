package com.example.calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.content.Intent;
import android.view.View;
import android.app.Application;
import com.google.firebase.FirebaseApp;




public class GenderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gender);

        Button nextButton = findViewById(R.id.button_next);

        // 버튼 클릭 시 CameraGalleryActivity로 이동
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // CameraGalleryActivity로 이동하는 인텐트
                Intent intent = new Intent(GenderActivity.this, GalleryActivity.class);
                startActivity(intent);
            }
        });

    }
}
