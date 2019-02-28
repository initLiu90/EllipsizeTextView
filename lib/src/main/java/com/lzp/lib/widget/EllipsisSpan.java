package com.lzp.lib.widget;

import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

public class EllipsisSpan extends ClickableSpan {
    private int color;
    private View.OnClickListener listener;

    public EllipsisSpan(int color, View.OnClickListener listener) {
        this.color = color;
        this.listener = listener;
    }

    @Override
    public void onClick(View widget) {
        if (listener != null) {
            listener.onClick(widget);
        }
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setColor(color);
        ds.setUnderlineText(false);//不显示下划线
    }
}
