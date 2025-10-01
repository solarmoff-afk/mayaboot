package ru.update.mayaui.widgets;

import android.content.Context;

import android.os.Build;

import android.view.View;
import android.view.ViewGroup;

import android.widget.RelativeLayout;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import ru.update.mayaui.MayaUI;
import ru.update.mayaui.MNode;
import ru.update.mayaui.VirtualResources;

public class RelativeLayoutBuilder extends BaseWidgetBuilder {
    @Override
    public View build(Context context, MNode node, VirtualResources resources) {
        RelativeLayout layout = new RelativeLayout(context);
        applyBaseAttributes(layout, node, resources);

        Map<String, View> viewIdMap = new HashMap<>();
        List<View> childrenViews = new ArrayList<>();
        
        for (MNode childNode : node.children) {
            View childView = MayaUI.createView(context, childNode, resources);
            childView.setId(View.generateViewId());
            
            String idAttr = childNode.attributes.get("android:id");
            if (idAttr != null) {
                String resourceId = idAttr.substring(idAttr.indexOf('/') + 1);
                viewIdMap.put(resourceId, childView);
            }
            
            childrenViews.add(childView);
        }

        for (int i = 0; i < childrenViews.size(); i++) {
            View childView = childrenViews.get(i);
            MNode childNode = node.children.get(i);
            
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) createLayoutParams(context, childNode, resources);

            for (Map.Entry<String, String> attribute : childNode.attributes.entrySet()) {
                String attrName = attribute.getKey();
                String attrValue = attribute.getValue();
                int rule = getRuleVerb(attrName);

                if (rule == -1) continue;

                if (attrValue.startsWith("@id/")) {
                    String targetId = attrValue.substring(4);
                    View targetView = viewIdMap.get(targetId);
                    
                    if (targetView != null) {
                        params.addRule(rule, targetView.getId());
                    }
                } else if (isBooleanRule(rule)) {
                    if ("true".equalsIgnoreCase(attrValue)) {
                        params.addRule(rule);
                    }
                }
            }
            
            layout.addView(childView, params);
        }

        return layout;
    }

    /*
        Преобразует строковое имя атрибута в целочисленную константу релативе лайаут
    */

    private int getRuleVerb(String attributeName) {
        switch (attributeName) {
            case "android:layout_toRightOf": return RelativeLayout.RIGHT_OF;
            case "android:layout_toLeftOf": return RelativeLayout.LEFT_OF;
            case "android:layout_above": return RelativeLayout.ABOVE;
            case "android:layout_below": return RelativeLayout.BELOW;
            case "android:layout_alignTop": return RelativeLayout.ALIGN_TOP;
            case "android:layout_alignBottom": return RelativeLayout.ALIGN_BOTTOM;
            case "android:layout_alignLeft": return RelativeLayout.ALIGN_LEFT;
            case "android:layout_alignRight": return RelativeLayout.ALIGN_RIGHT;
            case "android:layout_alignStart": return RelativeLayout.ALIGN_START;
            case "android:layout_alignEnd": return RelativeLayout.ALIGN_END;
            case "android:layout_alignParentTop": return RelativeLayout.ALIGN_PARENT_TOP;
            case "android:layout_alignParentBottom": return RelativeLayout.ALIGN_PARENT_BOTTOM;
            case "android:layout_alignParentLeft": return RelativeLayout.ALIGN_PARENT_LEFT;
            case "android:layout_alignParentRight": return RelativeLayout.ALIGN_PARENT_RIGHT;
            case "android:layout_alignParentStart": return RelativeLayout.ALIGN_PARENT_START;
            case "android:layout_alignParentEnd": return RelativeLayout.ALIGN_PARENT_END;
            case "android:layout_centerInParent": return RelativeLayout.CENTER_IN_PARENT;
            case "android:layout_centerHorizontal": return RelativeLayout.CENTER_HORIZONTAL;
            case "android:layout_centerVertical": return RelativeLayout.CENTER_VERTICAL;
            default: return -1;
        }
    }

    /*
        Проверяет, является ли правило булевым (не требует айди другого вью)
    */

    private boolean isBooleanRule(int rule) {
        return rule >= RelativeLayout.ALIGN_PARENT_TOP && rule <= RelativeLayout.CENTER_VERTICAL;
    }

    @Override
    protected ViewGroup.LayoutParams createLayoutParams(Context context, MNode node, VirtualResources resources) {
        int width = parseLayoutSize(node.attributes.get("android:layout_width"), context, resources);
        int height = parseLayoutSize(node.attributes.get("android:layout_height"), context, resources);
        
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
        
        int marginLeft = 0;
        int marginTop = 0;
        int marginRight = 0;
        int marginBottom = 0;
        
        String margin = node.attributes.get("android:layout_margin");
        
        if (margin != null) {
            int m = (int) resolveDimen(margin, context, resources);
            marginLeft = marginTop = marginRight = marginBottom = m;
        }
        
        String marginH = node.attributes.get("android:layout_marginHorizontal");
        if (marginH != null) {
            int m = (int) resolveDimen(marginH, context, resources);
            marginLeft = marginRight = m;
        }

        String marginV = node.attributes.get("android:layout_marginVertical");
        if (marginV != null) {
            int m = (int) resolveDimen(marginV, context, resources);
            marginTop = marginBottom = m;
        }
        
        String mLeft = node.attributes.get("android:layout_marginLeft");
        if (mLeft != null) {
            marginLeft = (int) resolveDimen(mLeft, context, resources);
        }

        String mStart = node.attributes.get("android:layout_marginStart");
        if (mStart != null) {
            marginLeft = (int) resolveDimen(mStart, context, resources);
        }

        String mTop = node.attributes.get("android:layout_marginTop");
        if (mTop != null) {
            marginTop = (int) resolveDimen(mTop, context, resources);
        }

        String mRight = node.attributes.get("android:layout_marginRight");
        if (mRight != null) {
            marginRight = (int) resolveDimen(mRight, context, resources);
        }

        String mEnd = node.attributes.get("android:layout_marginEnd");
        if (mEnd != null) {
            marginRight = (int) resolveDimen(mEnd, context, resources);
        }

        String mBottom = node.attributes.get("android:layout_marginBottom");
        if (mBottom != null) {
            marginBottom = (int) resolveDimen(mBottom, context, resources);
        }

        params.setMargins(marginLeft, marginTop, marginRight, marginBottom);
        
        return params;
    }
}