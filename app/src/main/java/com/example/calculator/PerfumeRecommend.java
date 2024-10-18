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

public class PerfumeRecommend extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PerfumeAdapter adapter;
    private List<Perfume> perfumeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfume_recommend);

        recyclerView = findViewById(R.id.perfume_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // assets 폴더에서 JSON 파일 읽기
        String jsonString = loadJSONFromAsset("perfumes_keyword_data.json");
        if (jsonString != null) {
            perfumeList = parsePerfumeJson(jsonString);  // JSON 데이터를 파싱하여 리스트로 변환
            String userStyleCategory = "sporty";  // 예시: 사용자의 코디가 sporty로 분류되었을 때
            List<Perfume> recommendedPerfumes = filterPerfumesByCategory(perfumeList, userStyleCategory);
            adapter = new PerfumeAdapter(recommendedPerfumes, PerfumeRecommend.this);
            recyclerView.setAdapter(adapter);
        }
    }

    // assets 폴더에서 JSON 파일을 읽는 메소드
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
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray perfumesArray = jsonObject.getJSONArray("perfumes");
            for (int i = 0; i < perfumesArray.length(); i++) {
                JSONObject perfumeObject = perfumesArray.getJSONObject(i);

                // 키워드를 JSONArray에서 List로 변환
                JSONArray keywordsArray = perfumeObject.getJSONArray("keywords");
                List<String> keywords = new ArrayList<>();
                for (int j = 0; j < keywordsArray.length(); j++) {
                    keywords.add(keywordsArray.getString(j));
                }

                Perfume perfume = new Perfume(
                        perfumeObject.getString("name"),
                        perfumeObject.getString("brand"),
                        perfumeObject.getString("description"),
                        perfumeObject.getString("notes"),
                        perfumeObject.getString("ImageURL"),
                        keywords  // 키워드 전달
                );
                perfumes.add(perfume);
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
                filteredPerfumes.add(perfume);  // 카테고리가 일치하는 향수
            }
        }
        return filteredPerfumes;
    }
}

