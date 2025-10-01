package ru.update.mayaui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import ru.update.mayaui.MNode;
import ru.update.mayaui.MayaUI;
import ru.update.mayaui.VirtualResources;

/*
    Адаптер-мост - он является системным бейз адаптаромом, но внутри использует
    виртуальный IMayaAdapter для получения данных. Его главная задача - получить
    MNode от IMayaAdapter и построить из него реальное View с помощью
    MayaUI.createView
*/

public class MayaBridgeAdapter extends BaseAdapter {
    private final IMayaAdapter virtualAdapter;
    private final Context context;
    private final VirtualResources resources;

    public MayaBridgeAdapter(Context context, IMayaAdapter virtualAdapter, VirtualResources resources) {
        this.context = context;
        this.virtualAdapter = virtualAdapter;
        this.resources = resources;
    }

    @Override
    public int getCount() {
        return virtualAdapter.getCount();
    }

    @Override
    public Object getItem(int position) {
        return virtualAdapter.getNode(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MNode node = virtualAdapter.getNode(position);

        // TODO: В будущем для рецикле вью здесь будет логика переиспользования
        
        return MayaUI.createView(context, node, resources);
    }
}