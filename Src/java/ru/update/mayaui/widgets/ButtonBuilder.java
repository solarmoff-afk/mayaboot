package ru.update.mayaui.widgets;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import ru.update.mayaui.MNode;
import ru.update.mayaui.VirtualResources;

public class ButtonBuilder extends TextViewBuilder {
    @Override
    public View build(Context context, MNode node, VirtualResources resources) {
        Button view = new Button(context);
        applyTextViewAttributes(view, node, resources);
        
        return view;
    }
}