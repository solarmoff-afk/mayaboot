package ru.update.mayaui.widgets;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import ru.update.mayaui.MNode;
import ru.update.mayaui.VirtualResources;

public class TextViewBuilder extends BaseWidgetBuilder {
    @Override
    public View build(Context context, MNode node, VirtualResources resources) {
        TextView view = new TextView(context);
        applyTextViewAttributes(view, node, resources);
        
        return view;
    }
}