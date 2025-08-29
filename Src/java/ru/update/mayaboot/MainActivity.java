package ru.update.mayaboot;

import android.app.Activity;
import android.os.Bundle;

import android.util.Log;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OpenJDK.install(this);
        Log.d("MayaBoot_Main", OpenJDK.run(this));
    }
}