package com.example.calculator;

import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.Button;
import android.provider.MediaStore;
import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.widget.Toast;
import android.util.Log;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import org.tensorflow.lite.Interpreter;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import android.graphics.BitmapFactory;
import android.os.Build;

public class GalleryActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 1003;
    private ImageView imageView;
    private Uri imageUri;

    // ActivityResultLauncher for Camera and Gallery
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    File localFile;
    String gender;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_acttivity);

        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
        }

        // 이전 Activity에서 성별 정보 가져오기
        gender = getIntent().getStringExtra("gender");
        // 성별에 따라 모델 파일 다운로드
        downloadModelFile(gender);

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
                        if (imageUri != null) {
                            loadModelAndRunInference(localFile, imageUri,gender);
                        }
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
    }

    // Check Permissions (API 레벨 33 이상에서는 READ_MEDIA_IMAGES 권한을 확인)
    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        }
    }

    // Request Permissions
    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CODE);
        }
    }

    // Handle the permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                Toast.makeText(this, "권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Firebase Storage에서 모델 파일을 다운로드하는 메서드

    public void downloadModelFile(String gender) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        String modelPath = gender.equals("male")
                ? "style_model_Man_resnet_finetune_new_35.tflite"
                : "style_model_resnet_finetune_new_women.tflite";

        StorageReference storageRef = storage.getReferenceFromUrl("gs://fashion-item-system.appspot.com/" + modelPath);

        try {
             localFile = File.createTempFile("style_model", "tflite");

            storageRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                Log.d("Firebase", "Download successful: " + localFile.getAbsolutePath());

            }).addOnFailureListener(exception -> {
                Log.d("Firebase", "Download failed", exception);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // TensorFlow Lite 모델 로드 및 이미지 예측 메서드
// TensorFlow Lite 모델 로드 및 이미지 예측 메서드 (여자 모델)
    private void loadModelAndRunInference(File modelFile, Uri imageUri, String gender) {
        try {
            Interpreter tflite = new Interpreter(modelFile); // TFLite 모델 로드
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true); // 이미지 크기 조정 (모델과 일치하게)

            ByteBuffer inputBuffer = convertBitmapToByteBuffer(resizedBitmap);

            // 여자와 남자 모델에 따라 다른 출력 크기 및 라벨 설정
            float[][] output;
            String[] labels;

            if (gender.equals("male")) {
                output = new float[1][4]; // 남자 모델의 출력 크기 (4 클래스)
                labels = new String[]{"businesscasual", "casual", "dandy", "street"}; // 남자용 라벨
            } else {
                output = new float[1][6]; // 여자 모델의 출력 크기 (6 클래스)
                labels = new String[]{"businesscasual", "casual", "chic", "girlish", "romantic", "street"}; // 여자용 라벨
            }

            tflite.run(inputBuffer, output);

            // 결과 처리
            int maxIndex = getMaxIndex(output[0]);
            Log.d("TensorFlowLite", "Predicted label: " + labels[maxIndex] + " (" + output[0][maxIndex] * 100 + "%)");

            tflite.close(); // 모델 해제

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 최대값 인덱스를 가져오는 메서드
    private int getMaxIndex(float[] probabilities) {
        int maxIndex = 0;
        float maxProb = probabilities[0];
        for (int i = 1; i < probabilities.length; i++) {
            if (probabilities[i] > maxProb) {
                maxProb = probabilities[i];
                maxIndex = i;
            }
        }
        return maxIndex;
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







}




