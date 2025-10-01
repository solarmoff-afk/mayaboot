package ru.update.mayaui.widgets;

import android.content.Context;
import android.view.View;
import android.widget.ListView;

import ru.update.mayaui.MNode;
import ru.update.mayaui.VirtualResources;

public class ListViewBuilder extends BaseWidgetBuilder {
    @Override
    public View build(Context context, MNode node, VirtualResources resources) {
        ListView view = new ListView(context);
        applyBaseAttributes(view, node, resources);
        
        String divider = node.attributes.get("android:divider");
        if (divider != null) {
            view.setDivider(resolveDrawable(divider, context, resources));
        }
        
        return view;
    }
}