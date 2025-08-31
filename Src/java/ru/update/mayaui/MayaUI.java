package ru.update.mayaui;

import android.content.Context;
import android.util.Log;
import android.view.View;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import ru.update.mayaui.widgets.IWidgetBuilder;

/*
    Мозг виртуалтзатора андроид экранов. Он является прорабом который
    говорит всем инструментам что делать.
*/

public class MayaUI {
    private static final String LOG_TAG = "MayaUI";

    public final VirtualResources resources = new VirtualResources();
    public final Map<String, MNode> layouts = new HashMap<>();

    /*
        Этот метод вызывается до старта приложения чтобы
        виртуализовать все строки, цвета, изображения и
        (самое главное) лайауты
    */

    public void parseDecompiledApk(File decompiledDir) {
        Log.d(LOG_TAG, "Starting to parse decompiled APK...");
        File resDir = new File(decompiledDir, "res");
        
        resources.loadValues(new File(resDir, "values"));
        resources.loadDrawables(resDir);

        File layoutDir = new File(resDir, "layout");
        
        if (layoutDir.exists() && layoutDir.isDirectory()) {
            File[] layoutFiles = layoutDir.listFiles();

            if (layoutFiles != null) {
                for (File file : layoutFiles) {
                    try {
                        MNode rootNode = parseLayoutFile(file);
                        String layoutName = file.getName().replace(".xml", "");
                        layouts.put(layoutName, rootNode);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Error parsing layout file: " + file.getName(), e);
                    }
                }
            }
        }

        Log.d(LOG_TAG, "Parsing complete. Loaded " + layouts.size() + " layouts.");
    }

    private MNode parseLayoutFile(File file) throws Exception {
        try (FileReader reader = new FileReader(file)) {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(reader);
            
            Stack<MNode> stack = new Stack<>();
            MNode rootNode = null;
            int eventType = xpp.getEventType();
            
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    MNode node = new MNode(xpp.getName());
                    
                    for (int i = 0; i < xpp.getAttributeCount(); i++) {
                        node.attributes.put(xpp.getAttributeName(i), xpp.getAttributeValue(i));
                    }
                    
                    if (!stack.isEmpty()) {
                        stack.peek().children.add(node);
                    } else {
                        rootNode = node;
                    }
                    
                    stack.push(node);
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (!stack.isEmpty()) {
                        stack.pop();
                    }
                }

                eventType = xpp.next();
            }

            return rootNode;
        }
    }

    /*
        Эти два метода строят рут лайаут и всех его
        детей который были виртуализированы до старта приложения 
    */

    public View build(Context context, String layoutName) {
        MNode rootNode = layouts.get(layoutName);
        
        if (rootNode == null) {
            Log.e(LOG_TAG, "Layout '" + layoutName + "' not found!");
            return null;
        }

        return createView(context, rootNode, resources);
    }

    public static View createView(Context context, MNode node, VirtualResources resources) {
        IWidgetBuilder builder = WidgetRegistry.getBuilder(node.tag);
        
        if (builder != null) {
            return builder.build(context, node, resources);
        } else {
            Log.w(LOG_TAG, "No builder found for tag: " + node.tag + ". Creating a placeholder View.");
            return new View(context);
        }
    }
}