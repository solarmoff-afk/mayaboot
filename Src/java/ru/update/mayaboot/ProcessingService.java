package ru.update.mayaboot;

import android.app.Service;

import android.content.Intent;

import android.os.IBinder;

import android.util.Log;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

public class ProcessingService extends Service {
    public static final String ACTION_START = "ru.update.mayaboot.action.START";
    public static final String ACTION_UPDATE = "ru.update.mayaboot.action.UPDATE";
    public static final String EXTRA_LOG_MESSAGE = "log_message";
    private static final String LOG_TAG = "MayaBoot_Apk";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_START.equals(intent.getAction())) {
            new Thread(() -> {
                try {
                    if (!OpenJDK.installed(this)) {
                        update("TASK: Install OpenJDK 17...");
                        OpenJDK.install(this);
                    }

                    update("TASK: Run ApkTool...");
                    List<String> commandList = new ArrayList<>();
                    commandList.add("--version");
                    Log.d(LOG_TAG, OpenJDK.runJar(this, "apktool.jar", commandList));

                    update("Task: Just test");
                    Thread.sleep(1500);

                    update("\nDONE");
                } catch (InterruptedException e) {
                    update("ERROR: Procces terminate");
                } finally {
                    stopSelf();
                }
            }).start();
        }

        return START_NOT_STICKY;
    }

    private void update(String message) {
        Log.d("ProcessingService", "Sending update: " + message);
        
        Intent intent = new Intent(ACTION_UPDATE);
        intent.putExtra(EXTRA_LOG_MESSAGE, message);
        
        intent.setPackage(getPackageName());
        sendBroadcast(intent);
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}