package ru.update.mayaui;

public class DrawableInfo {
    public enum Type {
        BITMAP, COLOR, SHAPE
    }

    public final Type type;
    public final String value;
    
    public DrawableInfo(Type type, String value) {
        this.type = type;
        this.value = value;
    }
}