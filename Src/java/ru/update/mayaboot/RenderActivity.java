package ru.update.mayaboot;

import android.app.Activity;

import android.os.Bundle;

import android.util.Log;

import android.view.View;

import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.File;

import ru.update.mayaui.MayaLayoutResult;
import ru.update.mayaui.MayaUI;

public class RenderActivity extends Activity {
    public static final String EXTRA_DECOMPILED_PATH = "decompiled_path";
    private static final String LOG_TAG = "MayaBoot_Render";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_render);

        FrameLayout container = findViewById(R.id.render_container);

        String decompiledPath = getIntent().getStringExtra(EXTRA_DECOMPILED_PATH);
        if (decompiledPath == null) {
            Log.e(LOG_TAG, "Decompiled path not provided!");
            showError("ОШИБКА: Путь к декомпилированному APK не был передан.", container);
            return;
        }

        File decompiledDir = new File(decompiledPath);
        if (!decompiledDir.exists()) {
            Log.e(LOG_TAG, "Decompiled directory does not exist: " + decompiledPath);
            showError("ОШИБКА: Папка с ресурсами не найдена по пути: " + decompiledPath, container);
            return;
        }

        Log.d(LOG_TAG, "Начинаем процесс рендеринга для: " + decompiledPath);

        try {
            MayaUI mayaUI = new MayaUI();

            Log.d(LOG_TAG, "Шаг 1: Парсинг ресурсов приложения...");
            mayaUI.parseDecompiledApk(decompiledDir);
            Log.d(LOG_TAG, "Парсинг ресурсов завершен.");

            // В БУДУЩЕМ СДЕЛАТЬ НЕ ЧЕРЕЗ ЖОПУ!
            String mainLayoutName = "activity_main";

            Log.d(LOG_TAG, "Шаг 2: Построение layout'а '" + mainLayoutName + "'...");
            MayaLayoutResult result = mayaUI.build(this, mainLayoutName);

            if (result != null && result.rootView != null) {
                Log.d(LOG_TAG, "Шаг 3: Успех! Отображаем результат на экране.");
                container.addView(result.rootView);
            } else {
                Log.e(LOG_TAG, "Сборка провалена! MayaUI вернул null для layout'а: " + mainLayoutName);
                showError("ОШИБКА: Не удалось построить layout '" + mainLayoutName + "'. Возможно, файл не найден или содержит неподдерживаемые теги.", container);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Произошло исключение в процессе рендеринга", e);
            showError("КРИТИЧЕСКАЯ ОШИБКА: " + e.getMessage(), container);
        }
    }

    private void showError(String message, FrameLayout container) {
        TextView errorView = new TextView(this);
        errorView.setText(message);
        errorView.setTextColor(0xFFFF0000); // это красный кста
        errorView.setPadding(32, 32, 32, 32);
        container.addView(errorView);
    }
}