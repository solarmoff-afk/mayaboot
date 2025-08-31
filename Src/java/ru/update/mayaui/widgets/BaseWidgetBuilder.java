package ru.update.mayaui.widgets;

import android.content.Context;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import android.util.TypedValue;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import ru.update.mayaui.DrawableInfo;
import ru.update.mayaui.MNode;
import ru.update.mayaui.VirtualResources;
import ru.update.mayaui.StyleInfo;

/*
    Это универсальный строитель для всех виджетов, он содержит все
    необходимые части для правильного строительства
*/

public abstract class BaseWidgetBuilder implements IWidgetBuilder {
    protected View applyBaseAttributes(View view, MNode node, VirtualResources resources) {
        String styleRef = node.attributes.get("style");
        if (styleRef != null && styleRef.startsWith("@style/")) {
            applyStyle(view, styleRef.substring(7), resources);
        }
        
        view.setLayoutParams(createLayoutParams(view.getContext(), node, resources));
        String backgroundRef = node.attributes.get("android:background");
        
        if (backgroundRef != null) {
            view.setBackground(resolveDrawable(backgroundRef, view.getContext(), resources));
        }
        
        return view;
    }

    private void applyStyle(View view, String styleName, VirtualResources resources) {
        StyleInfo style = resources.styles.get(styleName);
        if (style == null) {
            return;
        }

        if (style.parent != null) {
            String finalParentName = style.parent.substring(style.parent.lastIndexOf('/') + 1);
            applyStyle(view, finalParentName, resources);
        }

        if (view instanceof TextView) {
            applyTextViewAttributesFromStyle((TextView) view, style, resources);
        }
    }

    /*
        Обноявляем аттрибуты текста
    */
    
    protected View applyTextViewAttributes(TextView view, MNode node, VirtualResources resources) {
        applyBaseAttributes(view, node, resources);
        
        String textRef = node.attributes.get("android:text");
        if (textRef != null) {
            view.setText(resolveString(textRef, resources));
        }

        String colorRef = node.attributes.get("android:textColor");
        if (colorRef != null) {
            view.setTextColor(resolveColor(colorRef, resources));
        }

        String sizeRef = node.attributes.get("android:textSize");
        if (sizeRef != null) {
            view.setTextSize(TypedValue.COMPLEX_UNIT_PX, resolveDimen(sizeRef, view.getContext(), resources));
        }

        String gravity = node.attributes.get("android:gravity");
        if (gravity != null) {
            view.setGravity(parseGravity(gravity));
        }

        String styleAttr = node.attributes.get("android:textStyle");
        if (styleAttr != null && styleAttr.contains("bold")) {
            view.setTypeface(null, Typeface.BOLD);
        }
        
        return view;
    }

    private void applyTextViewAttributesFromStyle(TextView view, StyleInfo style, VirtualResources resources) {
        String colorRef = style.items.get("android:textColor");
        if (colorRef != null) {
            view.setTextColor(resolveColor(colorRef, resources));
        }
        
        String sizeRef = style.items.get("android:textSize");
        if (sizeRef != null) {
            view.setTextSize(TypedValue.COMPLEX_UNIT_PX, resolveDimen(sizeRef, view.getContext(), resources));
        }

        String gravity = style.items.get("android:gravity");
        if (gravity != null) {
            view.setGravity(parseGravity(gravity));
        }

        String styleAttr = style.items.get("android:textStyle");
        if (styleAttr != null && styleAttr.contains("bold")) {
            view.setTypeface(null, Typeface.BOLD);
        }
    }

    /*
        Создаём параметры лайаута на основе данных из декомпилированных
        xml файлов от apktool
    */

    protected ViewGroup.LayoutParams createLayoutParams(Context context, MNode node, VirtualResources resources) {
        int width = parseLayoutSize(node.attributes.get("android:layout_width"), context, resources);
        int height = parseLayoutSize(node.attributes.get("android:layout_height"), context, resources);
        
        return new ViewGroup.LayoutParams(width, height);
    }

    protected int parseLayoutSize(String value, Context context, VirtualResources resources) {
        if (value == null) return ViewGroup.LayoutParams.WRAP_CONTENT;
        
        switch (value) {
            case "match_parent":
                return ViewGroup.LayoutParams.MATCH_PARENT;
            
            case "wrap_content":
                return ViewGroup.LayoutParams.WRAP_CONTENT;
            
            default:
                return (int) resolveDimen(value, context, resources);
        }
    }

    protected int parseGravity(String gravityValue) {
        int gravity = Gravity.NO_GRAVITY;
        
        if (gravityValue == null) {
            return gravity;
        }
        
        String[] gravities = gravityValue.split("\\|");
        
        for (String g : gravities) {
            switch (g.trim()) {
                case "center": gravity |= Gravity.CENTER; break;
                case "center_vertical": gravity |= Gravity.CENTER_VERTICAL; break;
                case "center_horizontal": gravity |= Gravity.CENTER_HORIZONTAL; break;
                case "top": gravity |= Gravity.TOP; break;
                case "bottom": gravity |= Gravity.BOTTOM; break;
                case "left": gravity |= Gravity.LEFT; break;
                case "right": gravity |= Gravity.RIGHT; break;
                case "fill": gravity |= Gravity.FILL; break;
            }
        }

        return gravity;
    }
    
    /*
        Обратаываем случаи обращения к строке, цвету, димену или
        изображению
    */

    protected String resolveString(String value, VirtualResources resources) {
        if (value.startsWith("@string/")) {
            return resources.strings.getOrDefault(value.substring(8), "");
        }
        
        return value;
    }

    protected int resolveColor(String value, VirtualResources resources) {
        if (value.startsWith("@color/")) {
            String colorName = value.substring(7);
            String colorValue = resources.colors.get(colorName);
            
            return Color.parseColor(colorValue != null ? colorValue : "#FF000000");
        }

        try {
            return Color.parseColor(value);
        } catch (Exception e) {
            return Color.BLACK;
        }
    }

    protected float resolveDimen(String value, Context context, VirtualResources resources) {
        if (value.startsWith("@dimen/")) {
            value = resources.dimens.getOrDefault(value.substring(7), "0dp");
        }
        
        float numericalValue = Float.parseFloat(value.replaceAll("[^\\d.-]", ""));
        
        /*
            Виртуальный пиксель, который используется для автоматической
            оптимизации под все экраны всех устройств. Его фишка
            в том, что он основан на плотности пикселей, а не на их количестве.
        */

        if (value.endsWith("dp"))
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, numericalValue, context.getResources().getDisplayMetrics());
        
        /*
            SP очень похож на DP, но используется исключительно для текста.
            Он учитывает пользовательские настройки, в том числе
            размер шрифта. Очень полезная метрика для адаптации
            интерфейса под настройки пользователя. Помогает людям
            с слабым зрением сразу получить большие тексты, а не
            искать в настройках изменение. (Если оно вообще есть)
        */

        if (value.endsWith("sp"))
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, numericalValue, context.getResources().getDisplayMetrics());
        
        /*
            Реальный, настоящий пиксель экрана устройства.
        */

        if (value.endsWith("px"))
            return numericalValue;
        
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, numericalValue, context.getResources().getDisplayMetrics());
    }
    
    protected Drawable resolveDrawable(String value, Context context, VirtualResources resources) {
        Drawable fallbackDrawable = new ColorDrawable(Color.TRANSPARENT);

        if (value == null) {
            return fallbackDrawable;
        }
        
        try {
            if (value.startsWith("@drawable/")) {
                DrawableInfo info = resources.drawables.get(value.substring(10));
                
                if (info == null) {
                    return fallbackDrawable;
                }
                
                switch (info.type) {
                    case BITMAP:
                        Drawable d = Drawable.createFromPath(info.value);
                        return (d != null) ? d : fallbackDrawable;
                    case COLOR:
                        return new ColorDrawable(resolveColor(info.value, resources));
                }
            } else if (value.startsWith("@color/")) {
                return new ColorDrawable(resolveColor(value, resources));
            } else {
                return new ColorDrawable(Color.parseColor(value));
            }
        } catch (Exception e) {
            return fallbackDrawable;
        }
        
        return fallbackDrawable;
    }
}