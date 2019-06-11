package com.example.opencvtest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initWidgets();
    }

    private void initWidgets() {
        Button button_canny=(Button)findViewById(R.id.button_canny);
        button_canny.setOnClickListener(this);
        Button button_jni=(Button)findViewById(R.id.button_jni);
        button_jni.setOnClickListener(this);
        Button button_json=(Button)findViewById(R.id.button_jni_json);
        button_json.setOnClickListener(this);
        Button button_ndktest=(Button)findViewById(R.id.button_ndk_test);
        button_ndktest.setOnClickListener(this);
        Button button_rectify=(Button)findViewById(R.id.button_rectify);
        button_rectify.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent=null;
        switch (v.getId()){
            case R.id.button_canny:
                intent=new Intent(MainActivity.this,CannyActivity.class);
                break;
            case R.id.button_jni:
                intent=new Intent(MainActivity.this,JniTestActivity.class);
                break;
            case R.id.button_jni_json:
                intent=new Intent(MainActivity.this,JniJsonActivity.class);
                break;
            case R.id.button_ndk_test:
                intent=new Intent(MainActivity.this,NdkActivity.class);
                break;
            case R.id.button_rectify:
                intent=new Intent(MainActivity.this,RectificationActivity.class);
                break;
            default:
                break;
        }
        startActivity(intent);
    }
}
