package ru.update.mayaui;

import java.util.HashMap;
import java.util.Map;

public class StyleInfo {
    public final String name;
    public final String parent;
    public final Map<String, String> items = new HashMap<>();

    public StyleInfo(String name, String parent) {
        this.name = name;
        this.parent = parent;
    }
}