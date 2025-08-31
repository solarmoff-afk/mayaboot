package ru.update.mayaui.widgets;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import ru.update.mayaui.MayaUI;
import ru.update.mayaui.MNode;
import ru.update.mayaui.VirtualResources;

public class FrameLayoutBuilder extends BaseWidgetBuilder {
    @Override
    public View build(Context context, MNode node, VirtualResources resources) {
        FrameLayout view = new FrameLayout(context);
        applyBaseAttributes(view, node, resources);
        
        for (MNode child : node.children) {
            view.addView(MayaUI.createView(context, child, resources));
        }

        return view;
    }
}