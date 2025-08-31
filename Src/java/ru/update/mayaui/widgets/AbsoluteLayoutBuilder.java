package ru.update.mayaui.widgets;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;

import ru.update.mayaui.MayaUI;
import ru.update.mayaui.MNode;
import ru.update.mayaui.VirtualResources;

/*
    Абсолютный лайаут - лайаут, который никак не подстраивает интерфейс
    под свои правила. Что ему дали, то он и построил
*/

@SuppressWarnings("deprecation")
public class AbsoluteLayoutBuilder extends BaseWidgetBuilder {
    @Override
    public View build(Context context, MNode node, VirtualResources resources) {
        AbsoluteLayout view = new AbsoluteLayout(context);
        applyBaseAttributes(view, node, resources);
        
        for (MNode child : node.children) {
            View childView = MayaUI.createView(context, child, resources);
            int x = (int) resolveDimen(child.attributes.get("android:layout_x"), context, resources);
            int y = (int) resolveDimen(child.attributes.get("android:layout_y"), context, resources);
            
            AbsoluteLayout.LayoutParams params = (AbsoluteLayout.LayoutParams) childView.getLayoutParams();
            
            if (params != null) {
                params.x = x;
                params.y = y;
                view.addView(childView, params);
            } else {
                view.addView(childView);
            }
        }
        
        return view;
    }

    @Override
    protected ViewGroup.LayoutParams createLayoutParams(Context context, MNode node, VirtualResources resources) {
        int width = parseLayoutSize(node.attributes.get("android:layout_width"), context, resources);
        int height = parseLayoutSize(node.attributes.get("android:layout_height"), context, resources);
        
        return new AbsoluteLayout.LayoutParams(width, height, 0, 0);
    }
}