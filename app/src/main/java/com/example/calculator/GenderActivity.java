package com.example.calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.content.Intent;
import android.view.View;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import android.widget.RadioGroup;
import android.util.Log;



public class GenderActivity extends AppCompatActivity {

     RadioGroup radioGroupGender;

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
        radioGroupGender = findViewById(R.id.radioGroup_gender);
        Button nextButton = findViewById(R.id.button_next);



        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 라디오 버튼 선택 여부 확인
                int selectedId = radioGroupGender.getCheckedRadioButtonId();

                if (selectedId == -1) {
                    // 선택된 라디오 버튼이 없는 경우 로그 출력
                    Log.e("GenderActivity", "성별을 선택하지 않았습니다.");
                } else {
                    // 성별 정보 가져오기
                    String gender = (selectedId == R.id.radio_male) ? "Men" : "Women";
                    Log.d("GenderActivity", "선택된 성별: " + gender);

                    // Intent에 성별 정보를 추가하여 GalleryActivity로 이동
                    Intent intent = new Intent(GenderActivity.this, GalleryActivity.class);
                    intent.putExtra("gender", gender);  // 성별 정보 추가
                    startActivity(intent);

                    // 디버깅을 위해 Intent 전송 직전 로그 추가
                    Log.d("GenderActivity", "Intent 전송 완료, 성별: " + gender);
                }
            }
        });

    }
}
