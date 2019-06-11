package com.example.opencvtest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class JniJsonActivity extends AppCompatActivity {

    static {
        System.loadLibrary("jsoncpp");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jni_json);

        String outPutJson = outputJsonCode("xong", "21", "man", "code");
        String parseJson = parseJsonCode(outPutJson);

        TextView textView=(TextView)findViewById(R.id.json_text);
        textView.setText("生成的Json:\n" + outPutJson + "\n解析:" + parseJson);
    }

    public static native String outputJsonCode(String name, String age, String sex, String type);

    public static native String parseJsonCode(String json_str);
}
