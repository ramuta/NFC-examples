package com.example.NFCtagsTest.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.example.NFCtagsTest.R;
import com.example.NFCtagsTest.util.Debug;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    // buttons
    private Button writeButton;
    private Button readButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Debug.ON) {
            Log.i(TAG, "Main activity started");
        }

        writeButton = (Button) findViewById(R.id.mainWriteButton);
        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Debug.ON) {
                    Log.i(TAG, "Write button clicked");
                }

                Intent writeIntent = new Intent(MainActivity.this, WriteActivity.class);
                startActivity(writeIntent);
            }
        });

        readButton = (Button) findViewById(R.id.mainReadButton);
        readButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Debug.ON) {
                    Log.i(TAG, "Read button clicked");
                }

                Intent readIntent = new Intent(MainActivity.this, ReadActivity.class);
                startActivity(readIntent);
            }
        });
    }
}
