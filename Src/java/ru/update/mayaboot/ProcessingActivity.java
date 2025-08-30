package ru.update.mayaboot;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

public class ProcessingActivity extends Activity {
    private TextView logTextView;
    private ScrollView logScrollView;

    private BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(ProcessingService.EXTRA_LOG_MESSAGE);
            if (message != null) {
                logTextView.append(message + "\n");
                logScrollView.post(() -> logScrollView.fullScroll(View.FOCUS_DOWN));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proccesing);

        logTextView = findViewById(R.id.log_text_view);
        logScrollView = findViewById(R.id.log_scroll_view);

        Uri apkUri = getIntent().getData();
        if (apkUri != null) {
            Intent serviceIntent = new Intent(this, ProcessingService.class);
            serviceIntent.setAction(ProcessingService.ACTION_START);
            serviceIntent.setData(apkUri);

            startService(serviceIntent);
        } else {
            logTextView.setText("ERROR: NE UDALOS APK FILE PODCLUCKIT BLYAT");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(updateReceiver, new IntentFilter(ProcessingService.ACTION_UPDATE), Context.RECEIVER_NOT_EXPORTED);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(updateReceiver);
    }
}