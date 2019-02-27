package com.lzp.lib.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.v7.widget.AppCompatTextView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import com.lzp.lib.R;

public class EllipsizeTextView extends AppCompatTextView {
    private String mEllipsisText;
    private int mMaxLines = -1;
    private OnClickListener mClickListener;
    private int mEllipsisColor;

    public EllipsizeTextView(Context context) {
        this(context, null);
    }

    public EllipsizeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EllipsizeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setEllipsize(TextUtils.TruncateAt.END);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.EllipsizeTextView, defStyleAttr, 0);
        mEllipsisText = a.getString(R.styleable.EllipsizeTextView_ellipsis_text);
        if (mEllipsisText == null || mEllipsisText.equals("")) {
            mEllipsisText = "...";
        }

        int maxLines = a.getInt(R.styleable.EllipsizeTextView_max_line, 1);
        super.setMaxLines(maxLines);

        mEllipsisColor = a.getColor(R.styleable.EllipsizeTextView_ellipsis_textColor, Color.BLACK);

        setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void setMaxEms(int maxEms) {
    }

    /**
     * Textview singleLine属性会设置setHorizontallyScrolling(true)，导致layout的width为VERY_WIDE = 1024 * 1024
     * 导致不能正确处理截断，因此屏蔽了singleLine属性
     */
    @Override
    public void setSingleLine() {
    }

    @Override
    public void setHorizontallyScrolling(boolean whether) {
    }

    @Override
    public void setText(CharSequence text, TextView.BufferType type) {
        super.setText(text, type);
    }

    public void setOnEllipsisClickListener(OnClickListener listener) {
        mClickListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        ellipsis();
    }

    private void ellipsis() {
        if (getLayout() == null) return;

        int lineCount = getLineCount();
        int ellipsisCount = getLayout().getEllipsisCount(lineCount - 1);//最后一行省略多少个字符
        if (ellipsisCount > 0) {
            int lineStart = getLayout().getLineStart(lineCount - 1);//最后一行首字符的位置
            int lineEnd = getLayout().getLineEnd(lineCount - 1);//最后一行末尾字符(包括被省略的字符)的位置
            CharSequence visiableCharSequence = getText().subSequence(lineStart, lineEnd - ellipsisCount);//展示的屏幕上的最后一行显示的字符（不包括被省略的字符）
//            Log.e("Test", "1111=" + visiableCharSequence.toString());

            float width = getLayout().getWidth();//Layout最大宽度
            int len = EllipsisUtil.calculate(visiableCharSequence, mEllipsisText, width, getPaint());

            /**
             * 根据上面计算出来的 最后一行能够显示的原文本的长度，截取原文本，然后添加提示文本，调用setText()方法展示。
             */
            CharSequence text = getText().subSequence(0, lineStart + len);
            SpannableStringBuilder ssb = new SpannableStringBuilder();
            ssb.append(text).append(mEllipsisText);

            ssb.setSpan(new EllipsisSpan(mEllipsisColor, mClickListener), lineStart + len, ssb.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

            setText(ssb);
        }
    }
}
