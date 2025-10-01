package ru.update.mayaui.widgets;

import android.content.Context;

import android.view.View;
import android.view.inputmethod.EditorInfo;

import android.widget.EditText;

import android.text.InputType;

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

        String inputType = node.attributes.get("android:inputType");
        if (inputType != null) {
            view.setInputType(parseInputType(inputType));
        }

        String imeOptions = node.attributes.get("android:imeOptions");
        if (imeOptions != null) {
            view.setImeOptions(parseImeOptions(imeOptions));
        }
        
        return view;
    }

    private int parseInputType(String value) {
        int result = InputType.TYPE_NULL;
        
        String[] flags = value.split("\\|");
        
        for (String flag : flags) {
            switch (flag.trim()) {
                case "text": result |= InputType.TYPE_CLASS_TEXT; break;
                case "textCapSentences": result |= InputType.TYPE_TEXT_FLAG_CAP_SENTENCES; break;
                case "textPassword": result |= InputType.TYPE_TEXT_VARIATION_PASSWORD; break;
                case "textVisiblePassword": result |= InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD; break;
                case "textEmailAddress": result |= InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS; break;
                case "number": result |= InputType.TYPE_CLASS_NUMBER; break;
                case "numberPassword": result |= InputType.TYPE_NUMBER_VARIATION_PASSWORD; break;
                case "phone": result |= InputType.TYPE_CLASS_PHONE; break;
            }
        }

        return result;
    }

    private int parseImeOptions(String value) {
        switch(value) {
            case "actionDone": return EditorInfo.IME_ACTION_DONE;
            case "actionGo": return EditorInfo.IME_ACTION_GO;
            case "actionNext": return EditorInfo.IME_ACTION_NEXT;
            case "actionSearch": return EditorInfo.IME_ACTION_SEARCH;
            case "actionSend": return EditorInfo.IME_ACTION_SEND;
            default: return EditorInfo.IME_ACTION_UNSPECIFIED;
        }
    }
}