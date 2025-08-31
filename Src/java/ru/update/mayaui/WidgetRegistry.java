package ru.update.mayaui;

import java.util.HashMap;
import java.util.Map;
import ru.update.mayaui.widgets.*;

/*
    Статическая регистрация виджетов, регестрируются НЕ ВСЕ
    ибо реализация некоторых сложна и избыточна для
    минмального рабочего прототипа
*/

public class WidgetRegistry {
    private static final Map<String, IWidgetBuilder> builders = new HashMap<>();
    static {
        builders.put("TextView", new TextViewBuilder());
        builders.put("Button", new ButtonBuilder());
        builders.put("CheckBox", new CheckBoxBuilder());
        builders.put("EditText", new EditTextBuilder());
        builders.put("Switch", new SwitchBuilder());
        builders.put("ScrollView", new ScrollViewBuilder());
        builders.put("FrameLayout", new FrameLayoutBuilder());
        builders.put("LinearLayout", new LinearLayoutBuilder());
        builders.put("AbsoluteLayout", new AbsoluteLayoutBuilder());
        builders.put("GridLayout", new GridLayoutBuilder());
        builders.put("ImageView", new ImageViewBuilder());
    }

    public static IWidgetBuilder getBuilder(String tag) {
        return builders.get(tag);
    }
}