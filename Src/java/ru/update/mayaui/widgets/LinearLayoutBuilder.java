package ru.update.mayaui.widgets;

import android.content.Context;

import android.view.View;
import android.view.ViewGroup;

import android.widget.LinearLayout;

import ru.update.mayaui.MayaUI;
import ru.update.mayaui.MNode;
import ru.update.mayaui.VirtualResources;

/*
    Линейный лайаут - лайаут, в котором объекты построены
    в ряд (Вертикальный или горизонтальный)
*/

public class LinearLayoutBuilder extends BaseWidgetBuilder {
    @Override
    public View build(Context context, MNode node, VirtualResources resources) {
        LinearLayout view = new LinearLayout(context);
        applyBaseAttributes(view, node, resources);
        
        String orientation = node.attributes.get("android:orientation");
        
        if ("vertical".equals(orientation)) {
            view.setOrientation(LinearLayout.VERTICAL);
        } else {
            view.setOrientation(LinearLayout.HORIZONTAL);
        }

        String gravity = node.attributes.get("android:gravity");
        if (gravity != null) {
            view.setGravity(parseGravity(gravity));
        }

        for (MNode child : node.children) {
            View childView = MayaUI.createView(context, child, resources);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(childView.getLayoutParams());

            String weight = child.attributes.get("android:layout_weight");
            if (weight != null) {
                params.weight = Float.parseFloat(weight);
            }

            String layoutGravity = child.attributes.get("android:layout_gravity");
            if (layoutGravity != null) {
                params.gravity = parseGravity(layoutGravity);
            }

            view.addView(childView, params);
        }

        return view;
    }
}