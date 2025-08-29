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

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

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
        Запускает джаву, метод блокирующий поэтому вызывать только в фоне
    */

    public static String run(Context context) {
        File appFilesDir = context.getFilesDir();
        File usrDir = new File(appFilesDir, "usr");
        File javaExecutable = new File(appFilesDir, JAVA_EXECUTABLE_SUBPATH);

        if (!javaExecutable.exists() || !javaExecutable.canExecute()) {
            String errorMessage = "Error load: OpenJDK don't found or has no execution permissions.";
            Log.e(LOG_TAG, errorMessage);
            return errorMessage;
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
        commandList.add("-version");

        try {
            ProcessBuilder pb = new ProcessBuilder(commandList);
            pb.environment().putAll(envMap);
            pb.redirectErrorStream(true);

            Log.d(LOG_TAG, "STARTING FINAL, TRUE COMMAND: " + pb.command());
            Log.d(LOG_TAG, "WITH TRUE ENVIRONMENT: " + pb.environment());

            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            Log.d(LOG_TAG, "Process finish with code: " + exitCode);

            if (exitCode == 0) {
                return output.toString();
            } else {
                return "Procces finish with error (code " + exitCode + ") ;( \n" + output.toString();
            }
        } catch (IOException | InterruptedException e) {
            Log.e(LOG_TAG, "Blyat kak ti umudrilsa etu huynyu poymat? Pizdet ti", e);
            return "Errror in start procces block, Msg:\n" + e.getMessage();
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