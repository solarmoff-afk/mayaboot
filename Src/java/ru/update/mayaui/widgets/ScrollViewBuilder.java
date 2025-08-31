package ru.update.mayaui.widgets;

import android.content.Context;
import android.view.View;
import android.widget.ScrollView;

import ru.update.mayaui.MayaUI;
import ru.update.mayaui.MNode;
import ru.update.mayaui.VirtualResources;

public class ScrollViewBuilder extends FrameLayoutBuilder {
    @Override
    public View build(Context context, MNode node, VirtualResources resources) {
        ScrollView view = new ScrollView(context);
        applyBaseAttributes(view, node, resources);
        
        if (!node.children.isEmpty()) {
            view.addView(MayaUI.createView(context, node.children.get(0), resources));
        }

        return view;
    }
}