package ru.update.mayaui.widgets;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import ru.update.mayaui.MNode;
import ru.update.mayaui.VirtualResources;

public class ImageViewBuilder extends BaseWidgetBuilder {
    @Override
    public View build(Context context, MNode node, VirtualResources resources) {
        ImageView view = new ImageView(context);
        applyBaseAttributes(view, node, resources);
        
        String srcRef = node.attributes.get("android:src");
        
        if (srcRef != null) {
            view.setImageDrawable(resolveDrawable(srcRef, context, resources));
        }

        return view;
    }
}