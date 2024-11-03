package com.example.calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.widget.ImageView;
import android.widget.Button;
import android.provider.MediaStore;
import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.widget.Toast;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class GalleryActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 1003;
    private ImageView imageView;
    private Uri imageUri;
    private String gender; // 성별 정보를 저장할 변수
    private String sim_filename; //유사한 이미지 파일 이름

    // ActivityResultLauncher for Camera and Gallery
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_acttivity);

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
                        sendImageToFlaskServer(imageBitmap, gender);
                    }
                }
        );

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData(); // 선택된 이미지 URI 저장
                        imageView.setImageURI(imageUri);  // 이미지 표시

                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                            sendImageToFlaskServer(bitmap, gender);
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
                //URL url = new URL("http://172.30.1.53:5000/upload");
                URL url = new URL("http://172.30.1.97:5000/upload");//본인 ip로 바꾸기
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
                        // .jpg 확장자를 제거하여 숫자만 남기기
                        if (filename.endsWith(".jpg")) {
                            sim_filename = filename.substring(0, filename.length() - 4);
                        }

                        Log.d("FlaskServer", "Received Predicted Class: " + predictedClass);
                        Log.d("FlaskServer", "Received Filename: " + filename);
                        // ResultActivity를 시작하고 예측된 클래스와 sim_filename 전달
                        Intent intent = new Intent(GalleryActivity.this, ResultActivity.class);
                        intent.putExtra("predicted_class", predictedClass); // 예측된 클래스 추가
                        intent.putExtra("sim_filename", sim_filename); // sim_filename 추가
                        startActivity(intent); // ResultActivity 시작

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
}
