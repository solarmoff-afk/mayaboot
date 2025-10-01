package ru.update.mayaui.widgets;

import android.content.Context;
import android.content.res.ColorStateList;

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

        String scaleType = node.attributes.get("android:scaleType");
        if (scaleType != null) {
            switch (scaleType) {
                case "center": view.setScaleType(ImageView.ScaleType.CENTER); break;
                case "centerCrop": view.setScaleType(ImageView.ScaleType.CENTER_CROP); break;
                case "centerInside": view.setScaleType(ImageView.ScaleType.CENTER_INSIDE); break;
                case "fitCenter": view.setScaleType(ImageView.ScaleType.FIT_CENTER); break;
                case "fitStart": view.setScaleType(ImageView.ScaleType.FIT_START); break;
                case "fitEnd": view.setScaleType(ImageView.ScaleType.FIT_END); break;
                case "fitXY": view.setScaleType(ImageView.ScaleType.FIT_XY); break;
                case "matrix": view.setScaleType(ImageView.ScaleType.MATRIX); break;
            }
        }

        String tint = node.attributes.get("android:tint");
        if (tint != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            view.setImageTintList(ColorStateList.valueOf(resolveColor(tint, resources)));
        }

        String adjustViewBounds = node.attributes.get("android:adjustViewBounds");
        if ("true".equalsIgnoreCase(adjustViewBounds)) {
            view.setAdjustViewBounds(true);
        }

        return view;
    }
}