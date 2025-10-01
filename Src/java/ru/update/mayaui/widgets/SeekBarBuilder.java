package ru.update.mayaui.widgets;

import android.content.Context;
import android.widget.SeekBar;
import android.view.View;

import ru.update.mayaui.MNode;
import ru.update.mayaui.VirtualResources;

public class SeekBarBuilder extends ProgressBarBuilder {
    @Override
    public View build(Context context, MNode node, VirtualResources resources) {
        SeekBar view = new SeekBar(context);

        applyBaseAttributes(view, node, resources);
        applyProgressBarAttributes(view, node, resources);

        String thumb = node.attributes.get("android:thumb");
        if (thumb != null) {
            view.setThumb(resolveDrawable(thumb, context, resources));
        }

        return view;
    }
}