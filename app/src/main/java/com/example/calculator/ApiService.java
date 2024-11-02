package com.example.calculator;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    // 서버의 특정 엔드포인트에 POST 요청을 보내는 메서드 정의
    @POST("/extract-items")  // Flask 서버에서 설정한 엔드포인트
    Call<ResponseData> processImageByName(@Body ImageNameRequest request);
}
