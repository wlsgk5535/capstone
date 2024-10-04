package com.example.calculator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText editText1; EditText editText2;
    Button button_add; Button button_sub; Button button_mul; Button button_div;
    TextView textResult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calculator);


        button_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {}

        });



    }
}