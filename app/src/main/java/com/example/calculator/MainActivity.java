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

        editText1 = (EditText) findViewById(R.id.edit_text1);
        editText2 = (EditText) findViewById(R.id.edit_text2);
        button_add = (Button) findViewById(R.id.button_Add);
        button_sub = (Button) findViewById(R.id.button_Sub);
        button_mul = (Button) findViewById(R.id.button_Mul);
        button_div = (Button) findViewById(R.id.button_Div);
        textResult = (TextView) findViewById(R.id.text_view1);

        button_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String number1 = editText1.getText().toString();
                String number2 = editText2.getText().toString();
                Double result = Double.parseDouble(number1) + Double.parseDouble(number2);

                textResult.setText("Result: " + result.toString());
            }
        });
        button_sub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String number1 = editText1.getText().toString();
                String number2 = editText2.getText().toString();
                Double result = Double.parseDouble(number1) - Double.parseDouble(number2);

                textResult.setText("Result: " + result.toString());

            }
        });
        button_mul.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String number1 = editText1.getText().toString();
                String number2 = editText2.getText().toString();
                Double result = Double.parseDouble(number1) * Double.parseDouble(number2);
                textResult.setText("Result: " + result.toString());
            }
        });
        button_div.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String number1 = editText1.getText().toString();
                String number2 = editText2.getText().toString();
                Double result = Double.parseDouble(number1 )/ Double.parseDouble(number2);
                textResult.setText("Result: " + result.toString());
            }
        });


    }
}