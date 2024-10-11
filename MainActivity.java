package com.example.perfume;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PerfumeAdapter adapter;
    private List<Perfume> perfumeList;

    // Firebase Database Reference
    private DatabaseReference ref;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // RecyclerView 초기화
        initRecyclerView();

        // Firebase Database 초기화
        // Firebase Realtime Database에서 perfumes라는 경로에 저장된 데이터를 참조 -> 맞게 변경하기
        ref = FirebaseDatabase.getInstance().getReference("perfumes");

        // 사용자가 선택한 키워드와 노트 예시 (이 부분은 실제 사용자 입력으로 바뀔 수 있음)
        String selectedKeyword = "romantic";  // 예시: romantic 카테고리
        String selectedNote = "Vanilla";      // 예시: Vanilla 노트

        // Firebase에서 필터링된 데이터 가져오기
        getPerfumesByKeywordAndNote(selectedKeyword, selectedNote);
    }

    // RecyclerView 초기화
    private void initRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        perfumeList = new ArrayList<>();
        adapter = new PerfumeAdapter(perfumeList);
        recyclerView.setAdapter(adapter);
    }

    // Firebase에서 키워드와 노트에 맞는 향수 데이터를 가져오는 메서드
    private void getPerfumesByKeywordAndNote(String keyword, String note) {
        Query query = ref.orderByChild("keywords").equalTo(keyword);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                perfumeList.clear();  // 데이터 초기화

                // Firebase에서 검색된 데이터 처리
                for (DataSnapshot perfumeSnapshot : dataSnapshot.getChildren()) {
                    String perfumeName = perfumeSnapshot.child("name").getValue(String.class);
                    String perfumeNotes = perfumeSnapshot.child("notes").getValue(String.class);
                    String perfumeImageUrl = perfumeSnapshot.child("imageUrl").getValue(String.class);

                    // 사용자가 선택한 노트와 일치하는지 확인
                    if (perfumeNotes != null && perfumeNotes.toLowerCase().contains(note.toLowerCase())) {
                        Perfume perfume = new Perfume(perfumeName, perfumeNotes, perfumeImageUrl);
                        perfumeList.add(perfume);

                        // 최대 5개의 향수만 추천
                        if (perfumeList.size() == 5) {
                            break;
                        }
                    }
                }

                // UI 업데이트
                if (!perfumeList.isEmpty()) {
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainActivity.this, "추천할 향수를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", "Database error: " + databaseError.getMessage());
            }
        });
    }
}
