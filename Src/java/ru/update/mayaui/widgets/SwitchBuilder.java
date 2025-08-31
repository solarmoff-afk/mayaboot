package ru.update.mayaui.widgets;

import android.content.Context;
import android.view.View;
import android.widget.Switch;

import ru.update.mayaui.MNode;
import ru.update.mayaui.VirtualResources;

public class SwitchBuilder extends TextViewBuilder {
    @Override
    public View build(Context context, MNode node, VirtualResources resources) {
        Switch view = new Switch(context);
        applyTextViewAttributes(view, node, resources);
        
        return view;
    }
}