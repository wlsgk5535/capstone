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

import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.content.ContentResolver;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner; // 추가된 부분

public class GalleryActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 1003;
    private ImageView imageView;
    private Uri imageUri;
    private String gender; // 성별 정보를 저장할 변수

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

        // GenderActivity에서 전달된 성별 정보 받기
        Intent intent = getIntent();
        gender = intent.getStringExtra("gender");
        Log.d("GalleryActivity", "받은 성별: " + gender);

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

                        // Capture and send image to Flask server
                        sendImageToFlaskServer(imageBitmap, gender); // 추가된 부분
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

                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                            sendImageToFlaskServer(bitmap, gender); // 추가된 부분
                        } catch (IOException e) {
                            e.printStackTrace();
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

    // Check Permissions
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


    private void sendImageToFlaskServer(Bitmap bitmap, String gender) {
        new Thread(() -> {
            try {
                // Bitmap을 JPEG로 압축하여 ByteArray로 변환
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageData = baos.toByteArray();

                // Flask 서버 URL 설정
                URL url = new URL("http://172.30.1.53:5000/upload");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=boundary");
                connection.setDoOutput(true);

                // OutputStream을 통해 이미지 및 성별 데이터 전송
                DataOutputStream os = new DataOutputStream(connection.getOutputStream());
                os.writeBytes("--boundary\r\n");
                os.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"image.jpg\"\r\n");
                os.writeBytes("Content-Type: image/jpeg\r\n\r\n");
                os.write(imageData);
                os.writeBytes("\r\n--boundary\r\n");

                // 성별 정보 전송
                os.writeBytes("Content-Disposition: form-data; name=\"gender\"\r\n\r\n");
                os.writeBytes(gender + "\r\n");
                os.writeBytes("--boundary--\r\n");
                os.flush();
                os.close();

                // 서버 응답 확인
                int responseCode = connection.getResponseCode();
                Log.d("FlaskServer", "Response Code: " + responseCode);

                // 서버 응답 데이터 읽기
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Scanner scanner = new Scanner(connection.getInputStream());
                    StringBuilder response = new StringBuilder();
                    while (scanner.hasNextLine()) {
                        response.append(scanner.nextLine());
                    }
                    scanner.close();

                    try {
                        JSONObject jsonResponse = new JSONObject(response.toString());
                        String predictedClass = jsonResponse.getString("predicted_class");
                        String filename = jsonResponse.getString("filename");
                        Log.d("FlaskServer", "Received Predicted Class: " + predictedClass);
                        Log.d("FlaskServer", "Received Filename: " + filename);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("FlaskServer", "JSON Parsing Error: " + e.getMessage());
                    }
                } else {
                    Log.e("FlaskServer", "Error Response Code: " + responseCode);
                }
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("FlaskServer", "Error: " + e.getMessage());
            }
        }).start();
    }






    // Firebase Storage에서 모델 파일을 다운로드하는 메서드
    public void downloadModelFile() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://fashion-item-system.appspot.com/style_model_Man_resnet_finetune_new_35.tflite");

        try {
            File localFile = File.createTempFile("style_model", "tflite");

            storageRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                Log.d("Firebase", "Download successful: " + localFile.getAbsolutePath());
                if (imageUri != null) {
                    loadModelAndRunInference(localFile, imageUri);
                }

            }).addOnFailureListener(exception -> {
                Log.d("Firebase", "Download failed", exception);
            });
        } catch (IOException e) {
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

            String[] labels = {"businesscasual", "casual", "dandy", "street"}; // 실제 라벨에 맞게 수정
            int maxIndex = getMaxIndex(output[0]);
            Log.d("TensorFlowLite", "Predicted label: " + labels[maxIndex] + " (" + output[0][maxIndex] + ")");
            // 결과를 UI에 업데이트 하거나 다른 처리 수행 가능

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Bitmap을 ByteBuffer로 변환하는 메서드
    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3);
        byteBuffer.order(ByteOrder.nativeOrder());

        int[] intValues = new int[224 * 224];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        for (int i = 0; i < intValues.length; i++) {
            int pixelValue = intValues[i];
            byteBuffer.putFloat(((pixelValue >> 16) & 0xFF) / 255.0f); // R
            byteBuffer.putFloat(((pixelValue >> 8) & 0xFF) / 255.0f); // G
            byteBuffer.putFloat((pixelValue & 0xFF) / 255.0f); // B
        }
        return byteBuffer;
    }

    // Output에서 최대값 인덱스를 찾는 메서드
    private int getMaxIndex(float[] output) {
        int maxIndex = 0;
        for (int i = 1; i < output.length; i++) {
            if (output[i] > output[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }
}
