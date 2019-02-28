package com.lzp.lib.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.v7.widget.AppCompatTextView;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.widget.TextView;

import com.lzp.lib.R;

/**
 * textColorHighlight: 点击提示文本后背景颜色
 * ellipsis_textColor: 提示文本字体颜色
 * ellipsis_backgroundColor: 提示文本背景颜色
 * ellipsis_text: 提示文本
 * max_line: 最多能够显示多少行
 */
public class EllipsizeTextView extends AppCompatTextView {
    private String mEllipsisText;
    private OnClickListener mClickListener;
    private int mEllipsisColor;
    private int mEllipsisBgColor;
    private int mEllipsisClickedBgColor;

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

        mEllipsisBgColor = a.getColor(R.styleable.EllipsizeTextView_ellipsis_backgroundColor,Color.TRANSPARENT);

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

        Layout layout = createStaticLayout();

        int lineCount = layout.getLineCount();
        int ellipsisCount = layout.getEllipsisCount(lineCount - 1);//最后一行省略多少个字符
        if (ellipsisCount > 0) {
            int lineStart = layout.getLineStart(lineCount - 1);//最后一行首字符的位置
            int lineEnd = layout.getLineEnd(lineCount - 1);//最后一行末尾字符(包括被省略的字符)的位置
            CharSequence visiableCharSequence = getText().subSequence(lineStart, lineEnd - ellipsisCount);//展示的屏幕上的最后一行显示的字符（不包括被省略的字符）
//            Log.e("Test", "1111=" + visiableCharSequence.toString());

            float width = layout.getWidth();//Layout最大宽度
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

    /**
     * 设置了MovementMethod后，TextView会创建DynamicLayout，DynamicLayout是允许滚动的，所以设置的ellipsize不会起作用。
     * 通过跟踪源码发现，外部传去的spannablestring和spannedstring都是创建StaticLayout，只有在设置MovementMethod后，才会创建DynamicLayout。
     * 源码位置：
     * private void setText(CharSequence text, BufferType type,
     * boolean notifyBefore, int oldlen) {
     * ....
     * else if (type == BufferType.SPANNABLE || mMovement != null) {
     * text = mSpannableFactory.newSpannable(text);
     * } else if (!(text instanceof CharWrapper)) {
     * text = TextUtils.stringOrSpannedString(text);
     * }
     * ....
     * setTextInternal(text);
     * }
     * <p>
     * 因为对于过长的文本我们是要做截断的，所以DynamicLayout对我们来说没有意义。因此我们自己创建一个StaticLayout，根据StaticLayout计算出来的省略值，进行相关截断。
     */
    private Layout createStaticLayout() {
        Layout layout;
        if (!(getLayout() instanceof DynamicLayout)) {
            layout = getLayout();
        } else {
            StaticLayout.Builder builder = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                builder = StaticLayout.Builder.obtain(getText(), 0, getText().length(), getPaint(), getLayout().getWidth())
                        .setAlignment(getLayout().getAlignment())
                        .setLineSpacing(getLineSpacingExtra(), getLineSpacingMultiplier())
                        .setIncludePad(getIncludeFontPadding())
                        .setBreakStrategy(getBreakStrategy())
                        .setHyphenationFrequency(getHyphenationFrequency())
                        .setMaxLines(getMaxLines() == -1 ? Integer.MAX_VALUE : getMaxLines());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    builder.setJustificationMode(getJustificationMode());
                }

                if (getEllipsize() != null) {
                    builder.setEllipsize(getEllipsize())
                            .setEllipsizedWidth(getLayout().getEllipsizedWidth());
                }
                layout = builder.build();
            } else {
                layout = new StaticLayout(getText(), 0, getText().length(), getPaint(), getLayout().getWidth(), getLayout().getAlignment(),
                        getLineSpacingMultiplier(), getLineSpacingExtra(), getIncludeFontPadding(),
                        getEllipsize(), getLayout().getEllipsizedWidth());
            }
        }
        return layout;
    }

    @Override
    public float getLineSpacingMultiplier() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return super.getLineSpacingMultiplier();
        }
        return 1.0f;
    }

    @Override
    public float getLineSpacingExtra() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return super.getLineSpacingExtra();
        }
        return 0.0f;
    }

    @Override
    public boolean getIncludeFontPadding() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return super.getIncludeFontPadding();
        }
        return true;
    }
}
