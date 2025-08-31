package ru.update.mayaui.widgets;

import android.content.Context;
import android.view.View;
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

        for (MNode child : node.children) {
            view.addView(MayaUI.createView(context, child, resources));
        }

        return view;
    }
}