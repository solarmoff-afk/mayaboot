package ru.update.mayaui.adapters;

import ru.update.mayaui.MNode;

public interface IMayaAdapter {
    int getCount();
    MNode getNode(int position);
}