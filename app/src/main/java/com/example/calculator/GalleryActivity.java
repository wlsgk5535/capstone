package com.example.calculator;

import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.Button;
import android.provider.MediaStore;
import android.content.Intent;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.os.Bundle;

import com.google.firebase.Firebase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.File;
import java.io.IOException;
import com.google.firebase.FirebaseApp;

// Firebase Storage 관련 import

import com.google.firebase.storage.FileDownloadTask;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import android.util.Log;

// 필요한 라이브러리 임포트
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import org.tensorflow.lite.Interpreter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GalleryActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 1003;
    private ImageView imageView;
    private Uri imageUri;

    // ActivityResultLauncher for Camera and Gallery
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_acttivity);

        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
        }

        // ImageView and Buttons
        imageView = findViewById(R.id.imageView);
        Button cameraButton = findViewById(R.id.button_camera);
        Button galleryButton = findViewById(R.id.button_gallery);

        // Initialize ActivityResultLaunchers
        setupActivityResultLaunchers();

        // Set up Camera button listener
        cameraButton.setOnClickListener(v -> {
            if (checkPermissions()) {
                openCamera();
            } else {
                requestPermissions();
            }
        });

        // Set up Gallery button listener
        galleryButton.setOnClickListener(v -> {
            if (checkPermissions()) {
                openGallery();
            } else {
                requestPermissions();
            }
        });
    }

    // Initialize ActivityResultLaunchers
    private void setupActivityResultLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        imageView.setImageBitmap(imageBitmap);  // Show captured image
                    }
                }
        );

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                        imageUri = result.getData().getData(); // 선택된 이미지 URI 저장
                        imageView.setImageURI(imageUri);  // 이미지 표시
                        downloadModelFile();
                    }
                }
        );
    }

    // Open Camera
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(cameraIntent);
        }
    }

    // Open Gallery
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
        //downloadModelFile();
    }

    // Check Permissions
    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    // Request Permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSIONS_REQUEST_CODE);
    }

    // Handle the permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                // Permission denied, handle accordingly
            }
        }
    }

    // Firebase Storage에서 모델 파일을 다운로드하는 메서드
    public void downloadModelFile() {
        // Firebase Storage 인스턴스 가져오기
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://fashion-item-system.appspot.com/style_model_Man_resnet_finetune_new_35.tflite");

        try {
            // 임시 파일 생성
            File localFile = File.createTempFile("style_model", "tflite");

            // 파일 다운로드
            storageRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                Log.d("Firebase", "Download successful: " + localFile.getAbsolutePath());
                // 모델 다운로드 후 이미지 예측 수행 (예시로 imageUri 사용)
                if (imageUri != null) {
                    loadModelAndRunInference(localFile, imageUri); // 다운로드 후 이미지 예측 수행
                }

            }).addOnFailureListener(exception -> {
                Log.d("Firebase", "Download failed", exception);
            });
        } catch (IOException e) {
            // 예외 처리
            e.printStackTrace();
        }
    }

    // TensorFlow Lite 모델 로드 및 이미지 예측 메서드
    private void loadModelAndRunInference(File modelFile, Uri imageUri) {
        try {
            Interpreter tflite = new Interpreter(modelFile); // TFLite 모델 로드
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true); // 이미지 크기 조정 (모델과 일치하게)

            ByteBuffer inputBuffer = convertBitmapToByteBuffer(resizedBitmap);

            float[][] output = new float[1][4]; // Output array size (adjust based on your model output size)
            tflite.run(inputBuffer, output);

            // 결과 처리
            String[] labels = {"businesscasual", "casual", "dandy", "street"}; // 실제 라벨에 맞게 수정
            int maxIndex = getMaxIndex(output[0]);
            Log.d("TensorFlowLite", "Predicted label: " + labels[maxIndex] + " (" + output[0][maxIndex] * 100 + "%)");

            tflite.close(); // 모델 해제

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3); // 이미지 크기에 맞게 조정
        byteBuffer.order(ByteOrder.nativeOrder());

        // 이미지 크기 조정
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);
        int[] intValues = new int[224 * 224];
        scaledBitmap.getPixels(intValues, 0, scaledBitmap.getWidth(), 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight());

        // ImageNet의 평균값: BGR 순서로 설정
        float[] mean = {103.939f, 116.779f, 123.68f};

        // BGR 순서로 각 픽셀 값에서 평균값을 빼고 ByteBuffer에 추가
        for (int pixel : intValues) {
            int b = (pixel) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int r = (pixel >> 16) & 0xFF;

            byteBuffer.putFloat((b - mean[0]));
            byteBuffer.putFloat((g - mean[1]));
            byteBuffer.putFloat((r - mean[2]));
        }

        return byteBuffer;
    }



    // 결과 배열에서 최댓값의 인덱스를 구하는 메서드
    private int getMaxIndex(float[] arr) {
        int index = 0;
        float max = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > max) {
                max = arr[i];
                index = i;
            }
        }
        return index;
    }



}




