package ru.update.mayaui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
    Нода - основной строительный блок экранов, которые виртуализируются
    через MayaUI. Нода может быть как виджетом, так и лайаутом.
    Для простоты разделения на виджеты и лайауты НЕТ (Как в 
    нативном Android UI). 

    MNode -> MayaNode
*/

public class MNode {
    public MNode(String tag) {
        this.tag = tag;
    }

    public final String tag;
    public final Map<String, String> attributes = new HashMap<>();
    public final List<MNode> children = new ArrayList<>();
}