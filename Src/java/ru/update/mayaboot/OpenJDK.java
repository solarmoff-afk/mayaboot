/*
    OpenJDK - Класс для установки и запуска бинарника java

    Обновление: Я отказался от OpenJDK 11 из того репозитория и просто взял
    OpenJDK 17 прямо из термукса
*/

package ru.update.mayaboot;

import android.content.Context;
import android.content.res.AssetManager;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileDescriptor;
import java.io.PrintWriter;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class OpenJDK {
    private static final String JAVA_EXECUTABLE_SUBPATH = "usr/bin/java";
    private static final String LOG_TAG = "MayaBoot_OpenJDK";

    /*
        Проверяет, установлен ли OpenJDK, по наличию главного исполняемого файла.
        вернёт установлен ли JDK (true/false) в data/data/бла-бла-бла/files/usr/bin/
    */

    public static boolean installed(Context context) {
        File javaExecutable = new File(context.getFilesDir(), JAVA_EXECUTABLE_SUBPATH);
        return javaExecutable.exists();
    }

    /*
        Устанавливает OpenJDK (копирует пакет из assets и распаковывает его в data/data)
    */

    public static boolean install(Context context) {
        AssetManager assetManager = context.getAssets();

        File homeDir = new File(context.getFilesDir(), "home");
        if (!homeDir.exists()) {
            homeDir.mkdirs();
        }

        String[] packageFiles = {"OpenJDK.pkg", "OpenJDK_lib.pkg"};

        for (String packageFile : packageFiles) {
            File outFile = new File(context.getFilesDir(), packageFile.replace(".pkg", ".zip"));

            try (InputStream inputStream = assetManager.open("packages/" + packageFile);
                OutputStream outputStream = new FileOutputStream(outFile)) {

                byte[] buffer = new byte[8192];
                int length;

                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                Log.d(LOG_TAG, packageFile + " install code 1, done: " + outFile.getAbsolutePath());
            } catch (IOException e) {
                Log.e(LOG_TAG, packageFile + " install code 2, error: ", e);
                return false;
            }

            try {
                unpackZip(outFile, context.getFilesDir());
                Log.d(LOG_TAG, packageFile + " unpacking status: 1, done");
            } catch (IOException e) {
                Log.e(LOG_TAG, packageFile + " unpacking status: 2, error: ", e);
                return false;
            } finally {
                outFile.delete();
            }
        }

        File javaExecutable = new File(context.getFilesDir(), JAVA_EXECUTABLE_SUBPATH);
        if (javaExecutable.exists()) {
            javaExecutable.setReadable(true, false);
            if (javaExecutable.setExecutable(true, false)) {
                Log.d(LOG_TAG, "Set executable for " + JAVA_EXECUTABLE_SUBPATH + " status: done");
            } else {
                Log.d(LOG_TAG, "Set executable for " + JAVA_EXECUTABLE_SUBPATH + " status: error, MayaBoot can't work");
            }
        } else {
            Log.e(LOG_TAG, "OpenJDK not found nahuy, chto za huynya blyat, derji drevo filov mojet pomojet :3");

            File filesDir = context.getFilesDir();
            List<String> fileTree = getFileTree(filesDir, 0);
            for (String fileInfo : fileTree) {
                Log.e(LOG_TAG, fileInfo);
            }

            return false;
        }

        /*
            Наш тестовый джар - Это просто тест сокетов. Блоки
            с ним скоро будет удалены. Мы будем брать dex2jar
            чтобы превратить все дексы в классы и слить в 1
            джар файл который мы будем использовать
        */

        try (InputStream in = context.getAssets().open("guest.jar");
                OutputStream out = new FileOutputStream(new File(context.getFilesDir(), "home/guest.jar"))) {
            byte[] buffer = new byte[1024];
            int length;

            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            Log.d(LOG_TAG, "Guest uspeshno skopirovan v home/");
        } catch (IOException e) {
            Log.e(LOG_TAG, "Guest.jar otkazalsa kopirovatsya, nu i poshel on", e);
            return false;
        }

        return true;
    }

    /*
        Распаковывает zip-архив в указанную директорию

        Закрываем дыру с Zip skip, забота:)
    */

    private static void unpackZip(File zipFile, File destDirectory) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = new File(destDirectory, zipEntry.getName());

                if (!newFile.getCanonicalPath().startsWith(destDirectory.getCanonicalPath() + File.separator)) {
                    throw new IOException("Zip Slip found, urod: " + zipEntry.getName());
                }

                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed create directory " + newFile);
                    }
                } else {
                    File parent = newFile.getParentFile();

                    if (parent != null) {
                        if (!parent.isDirectory() && !parent.mkdirs()) {
                            throw new IOException("Failed create directory " + parent);
                        }
                    }

                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[8192];
                        int len;

                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }

                zipEntry = zis.getNextEntry();
            }
        }
    }

    /*
        Код для того, что отправить "ping" в виртуалку где запущен
        тестовый джарник. Если мы вернули pong - отлично
    */

    public static String run(Context context) {
        File appFilesDir = context.getFilesDir();
        File usrDir = new File(appFilesDir, "usr");
        File javaExecutable = new File(appFilesDir, JAVA_EXECUTABLE_SUBPATH);
        File guestJar = new File(new File(appFilesDir, "home"), "guest.jar");

        if (!javaExecutable.exists() || !guestJar.exists()) {
            return "CRITICAL ERROR: OpenJDK or guest.jar not found. Please try reinstalling the application.";
        }

        if (!javaExecutable.exists() || !javaExecutable.canExecute() || !guestJar.exists()) {
            String errorMessage = "Error load: OpenJDK/Guest.jar don't found or hasn't execution permissions.";
            Log.e(LOG_TAG, errorMessage);
            return errorMessage;
        }
        
        /*
            Небольшая пасхалОчка, android 1.0 был представлен
            5 (05) ноября (11) 2007 года (7)

            05117 - Дата рождения андроедика :3
        */

        final String[] guestResponse = {"NO_RESPONSE_YET"};
        final int PORT = 05117;
        final String HOST = "127.0.0.1";
        final CountDownLatch serverReadyLatch = new CountDownLatch(1);

        Thread serverThread = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT, 1, InetAddress.getByName(HOST))) {
                serverSocket.setSoTimeout(10000);
                
                Log.d(LOG_TAG, "SERVER: Waiting for connection on TCP " + HOST + ":" + PORT);
                
                serverReadyLatch.countDown();

                try (Socket clientSocket = serverSocket.accept()) {
                    Log.d(LOG_TAG, "SERVER: Client connected!");
                    
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    
                    out.println("ping");
                    guestResponse[0] = in.readLine();
                    
                    Log.d(LOG_TAG, "SERVER: Response received: '" + guestResponse[0] + "'");
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "SERVER: Error!", e);
                guestResponse[0] = "SERVER_ERROR: " + e.getMessage();
                
                if (serverReadyLatch.getCount() > 0) {
                    serverReadyLatch.countDown();
                }
            }
        });

        /*
            Запуск сервера. Он отправит клиету ping и будет ожидать
            ответ pong
        */

        serverThread.start();

        try {
            if (!serverReadyLatch.await(5, TimeUnit.SECONDS)) {
                return "FAIL: Server did not start in 5 seconds.";
            }
        } catch (InterruptedException e) {
            return "FAIL: Interrupted while waiting for server.";
        }
        
        if (guestResponse[0].startsWith("SERVER_ERROR")) {
            return "FAIL! Server could not start: " + guestResponse[0];
        }

        Map<String, String> envMap = new HashMap<>();
        envMap.put("HOME", new File(appFilesDir, "home").getAbsolutePath());
        envMap.put("PREFIX", usrDir.getAbsolutePath());
        envMap.put("TMPDIR", new File(appFilesDir, "tmp").getAbsolutePath());
        envMap.put("PATH", new File(usrDir, "bin").getAbsolutePath() + ":" + System.getenv("PATH"));
        
        File libDir = new File(usrDir, "lib");
        envMap.put("LD_LIBRARY_PATH", libDir.getAbsolutePath());
        
        File termuxExecSo = new File(libDir, "libtermux-exec.so");
        envMap.put("LD_PRELOAD", termuxExecSo.getAbsolutePath());
        
         String[] systemVarsToCopy = {
            "ANDROID_ART_ROOT", "ANDROID_ASSETS", "ANDROID_DATA", "ANDROID_I18N_ROOT",
            "ANDROID_ROOT", "ANDROID_RUNTIME_ROOT", "ANDROID_STORAGE", "ANDROID_TZDATA_ROOT",
            "BOOTCLASSPATH", "DEX2OATBOOTCLASSPATH", "EXTERNAL_STORAGE", "SYSTEMSERVERCLASSPATH"
        };

        for (String var : systemVarsToCopy) {
            String value = System.getenv(var);
            if (value != null) {
                envMap.put(var, value);
            }
        }

        List<String> commandList = new ArrayList<>();
        
        String linkerPath = "/system/bin/linker" + (android.os.Process.is64Bit() ? "64" : "");
        commandList.add(linkerPath);
        commandList.add(javaExecutable.getAbsolutePath());
        commandList.add("-jar");
        commandList.add(guestJar.getAbsolutePath());
        
        int exitCode = -1;
        StringBuilder guestProcessOutput = new StringBuilder();
        
        try {
            ProcessBuilder pb = new ProcessBuilder(commandList);
            pb.environment().putAll(envMap);
            pb.redirectErrorStream(true);

            Log.d(LOG_TAG, "STARTING GUEST JAR COMMAND: " + pb.command());
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    guestProcessOutput.append(line).append("\n");
                }
            }
            
            exitCode = process.waitFor();
            Log.d(LOG_TAG, "Guest process finished with code: " + exitCode);
        } catch (IOException | InterruptedException e) {
            Log.e(LOG_TAG, "Blyat kak ti umudrilsa etu huynyu poymat? Pizdet ti", e);
            return "Error in start procces block, Msg:\n" + e.getMessage();
        }

        if ("pong".equals(guestResponse[0])) {
            return "Bridge is work"; 
        } else {
            return "Bridge don't work";    
        }
    }

    /*
        По факту бесполезный (для общего фунуционала) функционала метод, но он
        ползен при отладке. ;)

        Выводит древо файлов
    */

    private static List<String> getFileTree(File directory, int depth) {
        List<String> result = new ArrayList<>();
        String indent = "  ".repeat(depth);

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    String fileInfo = indent + "├── " + file.getName();

                    if (file.isDirectory()) {
                        fileInfo += " (dir)";
                    } else {
                        fileInfo += " (file, " + file.length() + " bytes)";
                    }

                    result.add(fileInfo);

                    if (file.isDirectory()) {
                        result.addAll(getFileTree(file, depth + 1));
                    }
                }
            }
        } else {
            result.add(indent + "└── DIRECTORY NOT FOUND :( " + directory.getAbsolutePath());
        }

        return result;
    }
}