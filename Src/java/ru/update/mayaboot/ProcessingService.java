package ru.update.mayaboot;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import java.util.UUID;

public class ProcessingService extends Service {
    public static final String ACTION_START = "ru.update.mayaboot.action.START";
    public static final String ACTION_UPDATE = "ru.update.mayaboot.action.UPDATE";
    public static final String EXTRA_LOG_MESSAGE = "log_message";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_START.equals(intent.getAction())) {
            new Thread(() -> {
                try {
                    update("Task: Test1");
                    Thread.sleep(1500);

                    update("Task: Test2");
                    Thread.sleep(2000);

                    update("Task: Test3");
                    Thread.sleep(1000);

                    update("Task: Test4");
                    Thread.sleep(500);

                    update("Task: Test5");
                    Thread.sleep(1500);

                    update("Task: Task6...");
                    Thread.sleep(1000);

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