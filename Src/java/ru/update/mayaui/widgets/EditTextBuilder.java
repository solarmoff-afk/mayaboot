package ru.update.mayaui.widgets;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import ru.update.mayaui.MNode;
import ru.update.mayaui.VirtualResources;

public class EditTextBuilder extends TextViewBuilder {
    @Override
    public View build(Context context, MNode node, VirtualResources resources) {
        EditText view = new EditText(context);
        
        applyTextViewAttributes(view, node, resources);
        String hint = node.attributes.get("android:hint");
        
        if (hint != null) {
            view.setHint(resolveString(hint, resources));
        }
        
        return view;
    }
}