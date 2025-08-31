package ru.update.mayaui;

import android.util.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/*
    Инструмент для виртуализации ресурсов (Папка res в APK файле)
    мы не можем загрузить сырые ресурсы прямо из apk, поэтому
    у нас есть потребность в использование apktool с флагом d
    для декомпиляции

    Виртуализирует строки, цвета, изображения. Нужно загружать всю
    папку values (decompile_apk/res/values) для рекурсивного обхода
    и виртуализации
*/

public class VirtualResources {
    private static final String LOG_TAG = "MayaUI_Res";
    
    public final Map<String, String> strings = new HashMap<>();
    public final Map<String, String> colors = new HashMap<>();
    public final Map<String, String> dimens = new HashMap<>();
    public final Map<String, Integer> integers = new HashMap<>();
    public final Map<String, StyleInfo> styles = new HashMap<>();
    public final Map<String, DrawableInfo> drawables = new HashMap<>();

    /*
        Этот метод ничего не возвращает, так как он заполняет публичные
        карты для строк, цветов и так далее.

        Сами карты: strings, colors, dimens, styles, inegers и drawables
    */

    public void loadValues(File valuesDir) {
        if (!valuesDir.exists() || !valuesDir.isDirectory()) {
            return;
        }

        File[] valueFiles = valuesDir.listFiles();
        if (valueFiles == null) {
            return;
        }

        /*
            Определяем что за xml файл ресурсов мы парсим

            TODO: Добавь integers.xml
        */
        for (File file : valueFiles) {
            if (file.getName().startsWith("strings")) parseValueFile(file, strings, "string");
            else if (file.getName().startsWith("colors")) parseValueFile(file, colors, "color");
            else if (file.getName().startsWith("dimens")) parseValueFile(file, dimens, "dimen");
            else if (file.getName().startsWith("integers")) parseIntegersFile(file);
            else if (file.getName().startsWith("styles")) parseStylesFile(file);
        }

        Log.d(LOG_TAG, "Loaded " + strings.size() + " strings, " + colors.size() + " colors.");
    }

    private void parseIntegersFile(File file) {
        try (FileReader reader = new FileReader(file)) {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(reader);

            String currentName = null;
            int eventType = xpp.getEventType();
            
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && "integer".equals(xpp.getName())) {
                    currentName = xpp.getAttributeValue(null, "name");
                } else if (eventType == XmlPullParser.TEXT && currentName != null) {
                    integers.put(currentName, Integer.parseInt(xpp.getText()));
                    currentName = null;
                }

                eventType = xpp.next();
            }
        } catch (Exception e) {

        }
    }

    private void parseStylesFile(File file) {
        try (FileReader reader = new FileReader(file)) {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(reader);
            
            StyleInfo currentStyle = null;
            String currentItemName = null;
            
            int eventType = xpp.getEventType();
            
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String tagName = xpp.getName();
                    
                    if ("style".equals(tagName)) {
                        String name = xpp.getAttributeValue(null, "name");
                        String parent = xpp.getAttributeValue(null, "parent");
                        currentStyle = new StyleInfo(name, parent);
                    } else if ("item".equals(tagName) && currentStyle != null) {
                        currentItemName = xpp.getAttributeValue(null, "name");
                    }
                } else if (eventType == XmlPullParser.TEXT && currentItemName != null && currentStyle != null) {
                    currentStyle.items.put(currentItemName, xpp.getText());
                    currentItemName = null;
                } else if (eventType == XmlPullParser.END_TAG) {
                    if ("style".equals(xpp.getName()) && currentStyle != null) {
                        styles.put(currentStyle.name, currentStyle);
                        currentStyle = null;
                    }
                }

                eventType = xpp.next();
            }
        } catch (Exception e) {

        }
    }

    /*
        Отдельный метод для загрузки изображений (Drawables),
        передаём абсолютный путь к папке res

        decompile_apk/res/ 
    */

    public void loadDrawables(File resDir) {
        if (!resDir.exists() || !resDir.isDirectory()) {
            return;
        }

        File[] subDirs = resDir.listFiles();
        if (subDirs == null) {
            return;
        }

        for (File dir : subDirs) {
            if (dir.isDirectory() && dir.getName().startsWith("drawable")) {
                File[] files = dir.listFiles();
                if (files == null) {
                    continue;
                }
                
                for (File file : files) {
                    String name = file.getName().substring(0, file.getName().lastIndexOf('.'));
                    drawables.put(name, new DrawableInfo(DrawableInfo.Type.BITMAP, file.getAbsolutePath()));
                }
            } else if (dir.getName().equals("values")) {
                 File[] valueFiles = dir.listFiles();
                 if (valueFiles == null) {
                    continue;
                 }

                 for (File file : valueFiles) {
                     if(file.getName().startsWith("drawables") || file.getName().startsWith("colors")) {
                         parseDrawableValueFile(file);
                     }
                 }
            }
        }

        Log.d(LOG_TAG, "Loaded " + drawables.size() + " drawables.");
    }

    private void parseDrawableValueFile(File file) {
        try (FileReader reader = new FileReader(file)) {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(reader);
            
            String currentName = null;
            int eventType = xpp.getEventType();
            
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && "drawable".equals(xpp.getName())) {
                    currentName = xpp.getAttributeValue(null, "name");
                } else if (eventType == XmlPullParser.TEXT && currentName != null) {
                    drawables.put(currentName, new DrawableInfo(DrawableInfo.Type.COLOR, xpp.getText()));
                    currentName = null;
                }

                eventType = xpp.next();
            }
        } catch (Exception e) {

        }
    }

    private void parseValueFile(File file, Map<String, String> map, String tag) {
        try (FileReader reader = new FileReader(file)) {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(reader);
            
            String currentName = null;
            int eventType = xpp.getEventType();
            
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && tag.equals(xpp.getName())) {
                    currentName = xpp.getAttributeValue(null, "name");
                } else if (eventType == XmlPullParser.TEXT && currentName != null) {
                    map.put(currentName, xpp.getText());
                    currentName = null;
                }
                eventType = xpp.next();
            }
        } catch (Exception e) {

        }
    }
}