package ru.update.mayaboot;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("MayaBoot_Main", "Starting installation check...");
        OpenJDK.install(getApplicationContext());

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("MayaBoot_Main", "Installation complete. Running ping-pong test...");
                String result = OpenJDK.run(getApplicationContext());

                Log.d("MayaBoot_FINAL_RESULT", "========================================");
                Log.d("MayaBoot_FINAL_RESULT", "              TEST COMPLETE             ");
                Log.d("MayaBoot_FINAL_RESULT", "========================================");
                Log.d("MayaBoot_FINAL_RESULT", "\n" + result); // \n для читаемости
                Log.d("MayaBoot_FINAL_RESULT", "========================================");
            }
        }).start();
    }
}