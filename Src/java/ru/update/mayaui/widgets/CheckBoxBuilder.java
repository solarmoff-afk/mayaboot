package ru.update.mayaui.widgets;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import ru.update.mayaui.MNode;
import ru.update.mayaui.VirtualResources;

public class CheckBoxBuilder extends ButtonBuilder {
    @Override
    public View build(Context context, MNode node, VirtualResources resources) {
        CheckBox view = new CheckBox(context);
        applyTextViewAttributes(view, node, resources);
        
        return view;
    }
}