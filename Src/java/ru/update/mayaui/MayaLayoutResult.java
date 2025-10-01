package ru.update.mayaui;

import android.view.View;
import java.util.Map;

/*
    Класс обертка для результата построения лайаута это чудо
    содержит корневой вью и карту всех вьювов у которых был
    задан айди
*/

public class MayaLayoutResult {
    public final View rootView;
    private final Map<String, View> viewMap;

    public MayaLayoutResult(View rootView, Map<String, View> viewMap) {
        this.rootView = rootView;
        this.viewMap = viewMap;
    }

    @SuppressWarnings("unchecked")
    public <T extends View> T findViewById(String id) {
        return (T) viewMap.get(id);
    }
}