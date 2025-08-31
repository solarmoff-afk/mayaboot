package ru.update.mayaui.widgets;

import android.content.Context;
import android.view.View;
import android.widget.GridLayout;
import ru.update.mayaui.MayaUI;
import ru.update.mayaui.MNode;
import ru.update.mayaui.VirtualResources;

public class GridLayoutBuilder extends BaseWidgetBuilder {
    @Override
    public View build(Context context, MNode node, VirtualResources resources) {
        GridLayout view = new GridLayout(context);
        applyBaseAttributes(view, node, resources);
        
        for (MNode child : node.children) {
            view.addView(MayaUI.createView(context, child, resources));
        }
        
        return view;
    }
}