package com.example.NFCtagsTest.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.example.NFCtagsTest.R;
import com.example.NFCtagsTest.util.Debug;
import com.example.NFCtagsTest.util.NFCHelper;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Created by ramuta on 14/11/2014.
 */
public class ReadActivity extends Activity {
    private static final String TAG = "ReadActivity";
    private static final String MIME_TEXT_PLAIN = "text/plain";

    // UI
    private TextView readText;

    // NFC
    private NfcAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        if(Debug.ON) {
            Log.i(TAG, "Read activity started");
        }

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        readText = (TextView) findViewById(R.id.readText);
        adapter = NfcAdapter.getDefaultAdapter(ReadActivity.this);

        if (adapter == null) {
            Toast.makeText(this, "This device doesn't support NFC", Toast.LENGTH_LONG).show();
            ReadActivity.this.finish();
        }

        if (!adapter.isEnabled()) {
            readText.setText("Please enable NFC in the settings.");
        } else {
            // handle NFC intent
            handleIntent(getIntent());
        }
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask().execute(tag);
            } else {
                if (Debug.ON) {
                    Log.d(TAG, "Wrong mime type: " + type);
                }
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask().execute(tag);
                    break;
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupForegroundDispatch(this, adapter);
    }

    @Override
    protected void onPause() {
        stopForegroundDispatch(this, adapter);
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);

        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type, g.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {

        @Override
        protected String doInBackground(Tag... tags) {
            Tag tag = tags[0];

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                if (Debug.ON) {
                    Log.e(TAG, "NDEF is not supported by this tag.");
                }
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        NFCHelper helper = new NFCHelper();
                        return helper.readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        if (Debug.ON) {
                            Log.e(TAG, "Unsupported encoding", e);
                        }

                        e.getStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                readText.setText("NFC content: " + result);
            }
        }
    }
}