package com.example.calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Environment;
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
import androidx.core.content.FileProvider;

import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;

public class GalleryActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 1003;
    private ImageView imageView;
    private Uri imageUri;
    private String gender; // 성별 정보를 저장할 변수
    private String sim_filename; // 유사한 이미지 파일 이름
    private ImageView similarImageView;
    private String predictedClass; // 예측된 클래스 저장 변수
    private Button itemButton; // Result로 이동 버튼
    private TextView predictedClassTextView;

    private Uri photoUri;

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
        similarImageView = findViewById(R.id.similar_image_view);
        Button cameraButton = findViewById(R.id.button_camera);
        Button galleryButton = findViewById(R.id.button_gallery);
        itemButton = findViewById(R.id.button_item); // 새로 추가한 버튼
        predictedClassTextView = findViewById(R.id.textView_predictedClass);

        // 초기에는 Result 버튼을 비활성화
        itemButton.setEnabled(false);

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

        // Set up Result button listener
        itemButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent(GalleryActivity.this, ResultActivity.class);
            resultIntent.putExtra("predicted_class", predictedClass); // 예측된 클래스 전달
            resultIntent.putExtra("sim_filename", sim_filename); // sim_filename 전달
            startActivity(resultIntent);
        });
    }

    // Initialize ActivityResultLaunchers
    private void setupActivityResultLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        try {
                            Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                            // 회전이 필요한 경우 회전 후 이미지 적용
                            imageBitmap = rotateImageIfRequired(imageBitmap, photoUri);
                            imageView.setImageBitmap(imageBitmap); // Show captured image
                            sendImageToFlaskServer(imageBitmap, gender); // Send image to Flask server
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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
            // 고해상도 이미지를 저장할 파일 URI 생성
            File photoFile = createImageFile();
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this, "com.example.calculator.fileprovider", photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                cameraLauncher.launch(cameraIntent);
            }
        }
    }

    private File createImageFile() {
        // 파일 이름 생성 (날짜를 기반으로)
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            // 임시 파일 생성
            image = File.createTempFile(
                    imageFileName,  /* 파일 이름 */
                    ".jpg",         /* 파일 확장자 */
                    storageDir      /* 저장할 디렉토리 */
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    private Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {
        ExifInterface exif = new ExifInterface(getContentResolver().openInputStream(selectedImage));
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
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
                //URL url = new URL("http://192.168.0.32:5000/upload");//본인 ip로 바꾸기
                URL url = new URL("http://192.168.0.93:5000/upload"); //본인 ip로 바꾸기
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
                        predictedClass = jsonResponse.getString("predicted_class");
                        String filename = jsonResponse.getString("filename");
                        // .jpg 확장자를 제거하여 숫자만 남기기
                        if (filename.endsWith(".jpg")) {
                            sim_filename = filename.substring(0, filename.length() - 4);
                        }

                        Log.d("FlaskServer", "Received Predicted Class: " + predictedClass);
                        Log.d("FlaskServer", "Received Filename: " + filename);

                        // Glide로 유사 이미지 로드
                        runOnUiThread(() -> {
                            String imageURL = "http://192.168.0.93:5000/get_image/" + filename;
                            GlideApp.with(this)
                                    .load(imageURL)
                                    .into(similarImageView);

                            predictedClassTextView.setText("예측된 스타일: " + predictedClass);

                            // Result 버튼 활성화
                            itemButton.setEnabled(true);
                        });

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
