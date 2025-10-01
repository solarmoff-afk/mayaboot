package ru.update.mayaui.widgets;

import android.content.Context;
import android.widget.ProgressBar;
import android.view.View;

import ru.update.mayaui.MNode;
import ru.update.mayaui.VirtualResources;

public class ProgressBarBuilder extends BaseWidgetBuilder {
    @Override
    public View build(Context context, MNode node, VirtualResources resources) {
        String style = node.attributes.get("style");
        ProgressBar view;

        if (style != null && style.contains("progressBarStyleHorizontal")) {
            view = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        } else {
            view = new ProgressBar(context);
        }

        applyBaseAttributes(view, node, resources);
        applyProgressBarAttributes(view, node, resources);

        return view;
    }

    /*
        Защищенный метод для применения общих атрибутов ProgressBar
        (Он будет переиспользован в SeekBarBuilder)
    */

    protected void applyProgressBarAttributes(ProgressBar progressBar, MNode node, VirtualResources resources) {
        String max = node.attributes.get("android:max");
        if (max != null) {
            progressBar.setMax(Integer.parseInt(max));
        }

        String progress = node.attributes.get("android:progress");
        if (progress != null) {
            progressBar.setProgress(Integer.parseInt(progress));
        }

        String indeterminate = node.attributes.get("android:indeterminate");
        if (indeterminate != null) {
            progressBar.setIndeterminate(Boolean.parseBoolean(indeterminate));
        }

        String progressDrawable = node.attributes.get("android:progressDrawable");
        if (progressDrawable != null) {
            progressBar.setProgressDrawable(resolveDrawable(progressDrawable, progressBar.getContext(), resources));
        }
    }
}