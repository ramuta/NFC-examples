package com.example.NFCtagsTest.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.NFCtagsTest.R;
import com.example.NFCtagsTest.util.Debug;
import com.example.NFCtagsTest.util.NFCHelper;

import java.io.IOException;

/**
 * Created by ramuta on 13/11/2014.
 */
public class WriteActivity extends Activity {
    private static final String TAG = "WriteActivity";

    // UI
    private Button writeButton;
    private EditText addText;

    // NFC
    private NFCHelper helper;
    private Tag myTag;
    private NfcAdapter adapter;
    private PendingIntent pendingIntent;
    private IntentFilter writeTagFilters[];
    private boolean writeMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        if(Debug.ON) {
            Log.i(TAG, "Write activity started");
        }

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        writeButton = (Button) findViewById(R.id.writeButton);
        addText = (EditText) findViewById(R.id.writeAddText);

        helper = new NFCHelper();
        adapter = NfcAdapter.getDefaultAdapter(WriteActivity.this);
        pendingIntent = PendingIntent.getActivity(WriteActivity.this, 0, new Intent(WriteActivity.this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[] {tagDetected};

        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = addText.getText().toString();

                if (myTag != null) {
                    try {
                        helper.write(text, myTag);

                        if(Debug.ON) {
                            Log.i(TAG, "Text written: " + text);
                        }
                    } catch (IOException ex1) {
                        Toast.makeText(WriteActivity.this, "IO ex: " + ex1.toString(), Toast.LENGTH_LONG).show();
                        ex1.printStackTrace();
                    } catch (FormatException ex2) {
                        Toast.makeText(WriteActivity.this, "Format ex: " + ex2.toString(), Toast.LENGTH_LONG).show();
                        ex2.printStackTrace();
                    }

                }

            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Toast.makeText(WriteActivity.this, myTag.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        WriteModeOff();
    }

    @Override
    public void onResume(){
        super.onResume();
        WriteModeOn();
    }

    private void WriteModeOn(){
        writeMode = true;
        adapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
    }

    private void WriteModeOff(){
        writeMode = false;
        adapter.disableForegroundDispatch(this);
    }
}