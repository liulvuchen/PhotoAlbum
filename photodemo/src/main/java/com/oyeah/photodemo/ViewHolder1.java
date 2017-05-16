package com.oyeah.photodemo;

import android.util.SparseArray;
import android.view.View;

/**
 * Created by Administrator on 2016/11/28.
 */

public class ViewHolder1 {
    private final SparseArray<View> views;
    private View convertView;

    private ViewHolder1(View convertView) {
        this.views = new SparseArray<View>();
        this.convertView = convertView;
        convertView.setTag(this);
    }

    public static ViewHolder1 get(View convertView) {
        if (convertView == null) {
            return new ViewHolder1(convertView);
        }
        ViewHolder1 existedHolder = (ViewHolder1) convertView.getTag();
        return existedHolder;
    }

    @SuppressWarnings("unchecked")
    public static <T extends View> T get(View view, int id) {
        SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
        if (viewHolder == null) {
            viewHolder = new SparseArray<View>();
            view.setTag(viewHolder);
        }
        View childView = viewHolder.get(id);
        if (childView == null) {
            childView = view.findViewById(id);
            viewHolder.put(id, childView);
        }
        return (T) childView;
    }
}
