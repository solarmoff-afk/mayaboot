package ru.update.mayaboot;

import android.app.Activity;

import android.content.Intent;

import android.net.Uri;

import android.os.Bundle;

import android.util.Log;

import android.view.View;

import android.widget.Button;

public class MainActivity extends Activity {
    private static final String LOG_TAG = "MayaBoot_Main";
    private static final int FILE_SELECT_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button importButton = findViewById(R.id.button_import_apk);

        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("application/vnd.android.package-archive");
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                try {
                    startActivityForResult(
                        Intent.createChooser(intent, "Выберите APK для импорта"),
                        FILE_SELECT_CODE
                    );
                } catch (android.content.ActivityNotFoundException ex) {
                    Log.e(LOG_TAG, "On device not found file manager", ex);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_SELECT_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
    
                final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                getContentResolver().takePersistableUriPermission(uri, takeFlags);
    
                Intent intent = new Intent(this, ProcessingActivity.class);
                intent.setData(uri);
                startActivity(intent);
            }
        }
    }
}