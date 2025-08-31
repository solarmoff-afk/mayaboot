package ru.update.mayaui.widgets;

import android.content.Context;
import android.view.View;
import ru.update.mayaui.MNode;
import ru.update.mayaui.VirtualResources;

public interface IWidgetBuilder {
    View build(Context context, MNode node, VirtualResources resources);
}