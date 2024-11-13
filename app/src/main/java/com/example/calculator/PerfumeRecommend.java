package com.example.calculator;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import java.util.Arrays;

public class PerfumeRecommend extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PerfumeAdapter adapter;
    private List<Perfume> perfumeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfume_recommend);

        String predictedClass = getIntent().getStringExtra("predicted_class");
        String userStyleCategory = predictedClass != null ? predictedClass : "default_category";

        recyclerView = findViewById(R.id.perfume_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // assets 폴더에서 JSON 파일 읽기
        String jsonString = loadJSONFromAsset("cleaned_perfume_keyword_data.json");
        if (jsonString != null && userStyleCategory != null) {
            perfumeList = parsePerfumeJson(jsonString);  // JSON 데이터를 파싱하여 리스트로 변환
            // 예측된 스타일 키워드를 기반으로 필터링
            List<Perfume> recommendedPerfumes = filterPerfumesByCategory(perfumeList, userStyleCategory);
            adapter = new PerfumeAdapter(recommendedPerfumes, PerfumeRecommend.this);
            recyclerView.setAdapter(adapter);
        }
    }

    // assets 폴더에서 JSON 파일을 읽는 메서드
    private String loadJSONFromAsset(String fileName) {
        String json = null;
        try {
            InputStream is = getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return json;
    }

    // JSON 데이터를 파싱하여 Perfume 리스트로 변환
    private List<Perfume> parsePerfumeJson(String jsonString) {
        List<Perfume> perfumes = new ArrayList<>();
        try {
            // JSON 데이터가 배열로 시작하므로 JSONArray로 바로 파싱
            JSONArray perfumesArray = new JSONArray(jsonString);  // JSONObject가 아닌 JSONArray 사용

            for (int i = 0; i < perfumesArray.length(); i++) {
                JSONObject perfumeObject = perfumesArray.getJSONObject(i);

                // "Top Two Keywords"에서 키워드를 List로 변환
                String[] keywordsArray = perfumeObject.getString("Top Two Keywords").split(", ");
                List<String> keywords = new ArrayList<>(Arrays.asList(keywordsArray));

                try {
                    String name = perfumeObject.getString("Name");
                    String brand = perfumeObject.getString("Brand");
                    String description = perfumeObject.getString("Description");
                    String notes = perfumeObject.getString("Notes");
                    String imageURL = perfumeObject.getString("Image URL");

                    Perfume perfume = new Perfume(name, brand, description, notes, imageURL, keywords);
                    perfumes.add(perfume);

                } catch (JSONException e) {
                    Log.e("PerfumeRecommend", "Invalid value in JSON object", e);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return perfumes;
    }



    // 카테고리에 따라 향수 필터링
    private List<Perfume> filterPerfumesByCategory(List<Perfume> perfumes, String category) {
        List<Perfume> filteredPerfumes = new ArrayList<>();
        for (Perfume perfume : perfumes) {
            if (perfume.getKeywords().contains(category)) {
                filteredPerfumes.add(perfume);  // 카테고리가 일치하는 향수만 추가
            }
        }
        return filteredPerfumes;
    }
}
