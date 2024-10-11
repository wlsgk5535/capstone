package com.example.calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.content.Intent;
import android.view.View;
import android.app.Application;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;




public class GenderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gender);

        // FirebaseOptions 객체 생성
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApplicationId("1:694503342101:android:b9367f132ff50fe79d0e1c") // Firebase 콘솔에서 앱의 Application ID
                .setApiKey("AIzaSyAWsD-XvC5g_IPyHBq_2swBrbqDCiyuLK0") // Firebase 콘솔에서 API Key
                .setDatabaseUrl("https://fashion-item-system-default-rtdb.firebaseio.com") // 실시간 데이터베이스 URL
                .setStorageBucket("fashion-item-system.appspot.com") // 스토리지 버킷
                .setProjectId("fashion-item-system") // Firebase 콘솔에서 프로젝트 ID
                .build();

        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this,options);
        }

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
