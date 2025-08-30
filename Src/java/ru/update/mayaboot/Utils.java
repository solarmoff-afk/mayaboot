package ru.update.mayaboot;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class Utils {
   public static boolean copyAssetToFile(Context context, String assetFileName, String destinationPath) {
        try {
            File destinationFile = new File(destinationPath);
            File parentDirectory = destinationFile.getParentFile();

            if (parentDirectory != null && !parentDirectory.exists()) {
                parentDirectory.mkdirs();
            }

            InputStream inputStream = context.getAssets().open(assetFileName);
            FileOutputStream outputStream = new FileOutputStream(destinationFile);

            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();

            destinationFile.setExecutable(true);
            destinationFile.setReadable(true);

            return true;
        } catch (Exception exception) {
            return false;
        }
    }
}