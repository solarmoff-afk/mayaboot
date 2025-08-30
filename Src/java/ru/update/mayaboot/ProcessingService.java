package ru.update.mayaboot;

import android.app.Service;

import android.content.Intent;

import android.os.IBinder;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.InputStream;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

import android.net.Uri;

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

                    final Uri apkUri = intent.getData();
                    if (apkUri == null) {
                        update("FATAL ERROR: No APK URI provided to the service");
                        stopSelf();
                        
                        return;
                    }

                    update("TASK: Copying selected APK...");
                    File targetApk = new File(getFilesDir(), "tmp/target.apk");
                    targetApk.getParentFile().mkdirs();
                    
                    try (InputStream in = getContentResolver().openInputStream(apkUri);
                        OutputStream out = new FileOutputStream(targetApk)) {
                        
                        byte[] buffer = new byte[8192];
                        int length;
                        while ((length = in.read(buffer)) > 0) {
                            out.write(buffer, 0, length);
                        }    
                    } catch (Exception e) {
                        update("ERROR: Failed to copy APK file. " + e.getMessage());
                        stopSelf();
                        return;
                    }

                    update("TASK: Run ApkTool...");
                    List<String> commandList = new ArrayList<>();
                    
                    /*
                        Аргументы:
                            d - Декомпиляция
                            -f - Перезаписать
                            -s - Не декомпилировать дексы в смали
                            -p - Путь к фреймворку
                            -o - Выходная директория 
                    */

                    File outputDir = new File(getFilesDir(), "tmp/decompiled_apk");
                    File frameworkDir = new File(getFilesDir(), "tmp/apktool_framework");

                    commandList.add("d");
                    commandList.add("-f");
                    commandList.add("-s");

                    commandList.add(targetApk.getAbsolutePath());
                    
                    /*
                        Не доверяем апктул самому создать папку для фреймворка, так
                        как он попытается создать её в data/data/com.termux/files/...
                        ибо OpenJDK 17 был взят прямо из термукса
                    */

                    commandList.add("-p");
                    commandList.add(frameworkDir.getAbsolutePath());

                    commandList.add("-o");
                    commandList.add(outputDir.getAbsolutePath());
                    
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