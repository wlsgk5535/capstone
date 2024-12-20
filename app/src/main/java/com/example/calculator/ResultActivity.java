package com.example.calculator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;
import okhttp3.OkHttpClient;

import java.util.List;
import java.util.concurrent.TimeUnit;
import android.widget.Toast;
public class ResultActivity extends AppCompatActivity {
    private RecyclerView recyclerView;    // 서버 응답 데이터를 표시할 RecyclerView
    private ResultAdapter adapter;        // RecyclerView에 연결할 어댑터
    private String predictedClass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        // Intent에서 sim_filename 받기
        String predictedClass = getIntent().getStringExtra("predicted_class");
        String simFilename = getIntent().getStringExtra("sim_filename");
        Log.d("ResultActivity", "받은 predicted_class: " + predictedClass);
        Log.d("ResultActivity", "받은 sim_filename: " + simFilename);

        // XML 파일에 정의된 RecyclerView와 연결
        recyclerView = findViewById(R.id.recycler_view);
        // RecyclerView 설정
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ResultAdapter();
        recyclerView.setAdapter(adapter);


        // Activity 시작 시 서버로 데이터 가져오기 요청
        fetchData(simFilename);

        // PerfumeRecommend로 이동하는 버튼 리스너 추가
        Button startPerfumeRecommendButton = findViewById(R.id.btn_start_perfume_recommend);
        startPerfumeRecommendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // PerfumeRecommend로 이동하며 predictedClass 전달
                Intent intent = new Intent(ResultActivity.this, PerfumeRecommend.class);
                intent.putExtra("predicted_class", predictedClass);
                startActivity(intent);
                finish();

            }
        });
    }

    // 서버 응답을 받은 후 데이터를 ResultAdapter에 설정하는 메서드
// 서버 응답을 받은 후 데이터를 ResultAdapter에 설정하는 메서드
    private void handleServerResponse(ResponseData responseData) {
        if (responseData == null || responseData.getItems() == null) {
            Log.e("ResultActivity", "서버 응답 또는 항목 목록이 null입니다.");
            Toast.makeText(this, "서버에서 데이터를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Item> items = responseData.getItems();

        // 항목 목록이 비어 있는지 확인
        if (items.isEmpty()) {
            Log.e("ResultActivity", "서버 응답 목록이 비어 있습니다.");
            Toast.makeText(this, "서버에서 빈 데이터를 받았습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 어댑터의 setItems 메서드를 통해 RecyclerView에 데이터 업데이트
        adapter.setItems(items);
        Log.d("ResultActivity", "Items set in adapter: " + items.size());  // 데이터가 어댑터에 설정됐는지 로그 확인


    }


    // 서버에서 데이터를 가져오는 메서드
    private void fetchData(String imageName) {
        //String imageName = "30119"; // 하드코딩된 이미지 ID
        String pageUrl = "https://www.musinsa.com/app/styles/views/" + imageName;
        ImageNameRequest request = new ImageNameRequest(imageName, pageUrl);

        // 로그 추가
        Log.d("fetchData", "Sending request with imageName: " + imageName + ", pageUrl: " + pageUrl);

        // OkHttpClient 설정
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS) // 연결 타임아웃 60초
                .readTimeout(60, TimeUnit.SECONDS) // 읽기 타임아웃 60초
                .writeTimeout(60, TimeUnit.SECONDS) // 쓰기 타임아웃 60초
                .build();

        // Retrofit 설정
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.0.93:5000/")
                //.baseUrl("http://172.30.1.97:5000/")//본인 ip로 바꾸기(이건 실제 기기인 경우의 ip)
                //.baseUrl("http://10.0.2.2:5000/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // ApiService 생성
        ApiService apiService = retrofit.create(ApiService.class);
        Call<ResponseData> call = apiService.processImageByName(request);

        // 서버 요청 처리
        call.enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 서버 응답 성공 시
                    handleServerResponse(response.body());

                } else {
                    Log.d("ResultActivity", "서버 응답 오류");
                }
            }

            @Override
            public void onFailure(Call<ResponseData> call, Throwable t) {
                // 서버 요청 실패 시
                Log.d("ResultActivity", "네트워크 오류: " + t.getMessage());
            }
        });
    }
}
