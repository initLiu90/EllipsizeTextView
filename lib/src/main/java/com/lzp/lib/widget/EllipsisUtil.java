package com.lzp.lib.widget;

import android.text.Layout;
import android.text.TextPaint;

public class EllipsisUtil {
    /**
     * 需要把原始文本截断多少 才能在available 宽度内以一行的方式显示original+append
     *
     * @param original  原始文本
     * @param append    追加文本
     * @param available 可用宽度
     * @param paint
     * @return 截断后的原始文本的长度。-1 追加文本太长一行展示不下
     */
    public static int calculate(final CharSequence original, final CharSequence append, final float available, final TextPaint paint) {
        CharSequence originalCharSequence = original;
        /*
         *Layout.getDesiredWidth的作用是：以一段落一行这种方式展示文本，需要的宽度。其中段落是以'\n',区分。
         *实际上就是以从头开始变量文本，当访问到'\n'时认为是一个段落，然后计算一下把这一段显示在一行需要的宽度width。
         *继续下一段落的计算，比较这个段落需要的宽度是否比上一段落的需要的宽度大，保留较大的。以这种方式遍历至末尾，就能得到需要的宽度。
         */
        float usedWidth = Layout.getDesiredWidth(originalCharSequence, paint);//计算一下把最后一行的文本，以一行的方式显示出来需要的宽度。
        float ellipseWidth = Layout.getDesiredWidth(append, paint);//计算一下需要显示的提示文本，以一行的方式显示出来需要的宽度。

        if (ellipseWidth > available) {
            return -1;
        }

        /**
         * 不断的减少最后一行显示的文本长度，直到最后一行显示的文本 + 提示文本 能够在一行显示出来。
         */
        //表示最后一行能够显示的原文本的长度
        int len = originalCharSequence.length();
        while (usedWidth + ellipseWidth > available) {
            len--;
            originalCharSequence = originalCharSequence.subSequence(0, len);
            usedWidth = Layout.getDesiredWidth(originalCharSequence, paint);
        }
        return len;
    }
}
