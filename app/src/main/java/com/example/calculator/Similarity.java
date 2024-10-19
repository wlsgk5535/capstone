package com.example.calculator;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Core;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import java.io.IOException;
import java.util.List;

public class Similarity extends AppCompatActivity {

    static {
        // OpenCV 라이브러리 로드
        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "OpenCV initialization failed");
        } else {
            Log.d("OpenCV", "OpenCV initialization successful");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Intent에서 이미지 URI 가져오기
        Intent intent = getIntent();
        String imageUriString = intent.getStringExtra("imageUri");
        if (imageUriString != null) {
            Uri imageUri = Uri.parse(imageUriString);
            try {
                // URI로부터 비트맵 생성
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

                // 비트맵을 OpenCV의 Mat 형식으로 변환
                Mat matImage = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC3);
                org.opencv.android.Utils.bitmapToMat(bitmap, matImage);

                // 이미지 배경을 흰색으로 변경
                Mat resultImage = changeBackgroundToWhite(matImage);

                // 결과 이미지를 로그에 출력 (예시)
                Log.d("OpenCV", "Background changed to white");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 배경을 흰색으로 변경하는 메서드
    private Mat changeBackgroundToWhite(Mat image) {
        // 흰색 배경 생성
        Mat whiteBackground = new Mat(image.size(), image.type(), new Scalar(255, 255, 255));

        // Segmentation Mask 생성 (예시: 50% 임계값을 사용하여 마스크 생성)
        Mat mask = new Mat();
        Imgproc.cvtColor(image, mask, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(mask, mask, 127, 255, Imgproc.THRESH_BINARY);

        // 배경을 흰색으로 변경
        Mat resultImage = new Mat();
        Core.subtract(whiteBackground, image, resultImage, mask);
        Core.add(image, resultImage, resultImage);

        return resultImage;
    }
}


