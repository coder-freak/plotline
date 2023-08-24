package com.example.myapplication.tip.tooltip;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.TextViewCompat;

import com.example.myapplication.R;
import com.example.myapplication.tip.ViewTreeObserverCompat;

public final class Tooltip {
    private static final String TAG = "Tooltip";

    private final boolean isDismissOnClick;

    private final int mGravity;

    private final float mMargin;

    private final View mAnchorView;
    private final PopupWindow mPopupWindow;

    private OnClickListener mOnClickListener;
    private OnLongClickListener mOnLongClickListener;
    private OnDismissListener mOnDismissListener;

    private LinearLayout mContentView;
    private ImageView mArrowView;

    private Tooltip(Builder builder) {
        isDismissOnClick = builder.isDismissOnClick;

        mGravity = Gravity.getAbsoluteGravity(builder.mGravity, ViewCompat.getLayoutDirection(builder.mAnchorView));
        mMargin = builder.mMargin;
        mAnchorView = builder.mAnchorView;
        mOnClickListener = builder.mOnClickListener;
        mOnLongClickListener = builder.mOnLongClickListener;
        mOnDismissListener = builder.mOnDismissListener;

        mPopupWindow = new PopupWindow(builder.mContext);
        mPopupWindow.setClippingEnabled(false);
        mPopupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setContentView(getContentView(builder));
        mPopupWindow.setBackgroundDrawable(new ColorDrawable());
        mPopupWindow.setOutsideTouchable(builder.isCancelable);
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mAnchorView.getViewTreeObserver().removeOnScrollChangedListener(mOnScrollChangedListener);
                mAnchorView.removeOnAttachStateChangeListener(mOnAttachStateChangeListener);

                if (mOnDismissListener != null) {
                    mOnDismissListener.onDismiss();
                }
            }
        });
    }

    private View getContentView(Builder builder) {
        TextView textView = new TextView(builder.mContext);

        TextViewCompat.setTextAppearance(textView, builder.mTextAppearance);
        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(textView, builder.mDrawableStart, builder.mDrawableTop, builder.mDrawableEnd, builder.mDrawableBottom);

        textView.setText(builder.mText);
        textView.setPadding(builder.mPadding, builder.mPadding, builder.mPadding, builder.mPadding);
        textView.setLineSpacing(builder.mLineSpacingExtra, builder.mLineSpacingMultiplier);
        textView.setTypeface(builder.mTypeface, builder.mTextStyle);
        textView.setCompoundDrawablePadding(builder.mDrawablePadding);

        if (builder.mMaxWidth >= 0) {
            textView.setMaxWidth(builder.mMaxWidth);
        }

        if (builder.mTextSize >= 0) {
            textView.setTextSize(TypedValue.TYPE_NULL, builder.mTextSize);
        }

        if (builder.mTextColor != null) {
            textView.setTextColor(builder.mTextColor);
        }

        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0);
        textViewParams.gravity = Gravity.CENTER;
        textView.setLayoutParams(textViewParams);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(builder.mBackgroundColor);
        drawable.setCornerRadius(builder.mCornerRadius);

        ViewCompat.setBackground(textView, drawable);

        mContentView = new LinearLayout(builder.mContext);
        mContentView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mContentView.setOrientation(Gravity.isHorizontal(mGravity) ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);

        if (builder.isArrowEnabled) {
            mArrowView = new ImageView(builder.mContext);
            mArrowView.setImageDrawable(builder.mArrowDrawable == null ? new ArrowDrawable(builder.mBackgroundColor, mGravity) : builder.mArrowDrawable);

            LinearLayout.LayoutParams arrowLayoutParams;
            if (Gravity.isVertical(mGravity)) {
                arrowLayoutParams = new LinearLayout.LayoutParams((int) builder.mArrowWidth, (int) builder.mArrowHeight, 0);
            } else {
                arrowLayoutParams = new LinearLayout.LayoutParams((int) builder.mArrowHeight, (int) builder.mArrowWidth, 0);
            }
            arrowLayoutParams.gravity = Gravity.CENTER;
            mArrowView.setLayoutParams(arrowLayoutParams);

            if (mGravity == Gravity.TOP || mGravity == Gravity.getAbsoluteGravity(Gravity.START, ViewCompat.getLayoutDirection(mAnchorView))) {
                mContentView.addView(textView);
                mContentView.addView(mArrowView);
            } else {
                mContentView.addView(mArrowView);
                mContentView.addView(textView);
            }
        } else {
            mContentView.addView(textView);
        }

        int padding = (int) Utils.dpToPx(5);
        switch (mGravity) {
            case Gravity.LEFT:
                mContentView.setPadding(padding, 0, 0, 0);
                break;
            case Gravity.TOP:
            case Gravity.BOTTOM:
                mContentView.setPadding(padding, 0, padding, 0);
                break;
            case Gravity.RIGHT:
                mContentView.setPadding(0, 0, padding, 0);
                break;
        }

        mContentView.setOnClickListener(mClickListener);
        mContentView.setOnLongClickListener(mLongClickListener);

        return mContentView;
    }
    public boolean isShowing() {
        return mPopupWindow.isShowing();
    }
    public void show() {
        if (!isShowing()) {
            mContentView.getViewTreeObserver().addOnGlobalLayoutListener(mLocationLayoutListener);

            mAnchorView.addOnAttachStateChangeListener(mOnAttachStateChangeListener);
            mAnchorView.post(new Runnable() {
                @Override
                public void run() {
                    if (mAnchorView.isShown()) {
                        mPopupWindow.showAsDropDown(mAnchorView);
                    } else {
                        Log.e(TAG, "Tooltip cannot be shown, root view is invalid or has been closed");
                    }
                }
            });
        }
    }
    public void dismiss() {
        mPopupWindow.dismiss();
    }
    public void setOnClickListener(OnClickListener listener) {
        mOnClickListener = listener;
    }
    public void setOnLongClickListener(OnLongClickListener listener) {
        mOnLongClickListener = listener;
    }
    public void setOnDismissListener(OnDismissListener listener) {
        mOnDismissListener = listener;
    }

    private PointF calculateLocation() {
        PointF location = new PointF();

        final RectF anchorRect = Utils.calculateRectInWindow(mAnchorView);
        final PointF anchorCenter = new PointF(anchorRect.centerX(), anchorRect.centerY());

        switch (mGravity) {
            case Gravity.LEFT:
                location.x = anchorRect.left - mContentView.getWidth() - mMargin;
                location.y = anchorCenter.y - mContentView.getHeight() / 2f;
                break;
            case Gravity.RIGHT:
                location.x = anchorRect.right + mMargin;
                location.y = anchorCenter.y - mContentView.getHeight() / 2f;
                break;
            case Gravity.TOP:
                location.x = anchorCenter.x - mContentView.getWidth() / 2f;
                location.y = anchorRect.top - mContentView.getHeight() - mMargin;
                break;
            case Gravity.BOTTOM:
                location.x = anchorCenter.x - mContentView.getWidth() / 2f;
                location.y = anchorRect.bottom + mMargin;
                break;
        }
        return location;
    }

    private final View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mOnClickListener != null) {
                mOnClickListener.onClick(Tooltip.this);
            }
            if (isDismissOnClick) {
                dismiss();
            }
        }
    };

    private final View.OnLongClickListener mLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            return mOnLongClickListener != null && mOnLongClickListener.onLongClick(Tooltip.this);
        }
    };

    private final ViewTreeObserver.OnGlobalLayoutListener mLocationLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            ViewTreeObserverCompat.removeOnGlobalLayoutListener(mContentView.getViewTreeObserver(), this);

            final ViewTreeObserver vto = mAnchorView.getViewTreeObserver();
            if (vto != null) {
                vto.addOnScrollChangedListener(mOnScrollChangedListener);
            }

            if (mArrowView != null) {
                mContentView.getViewTreeObserver().addOnGlobalLayoutListener(mArrowLayoutListener);
            }

            PointF location = calculateLocation();
            mPopupWindow.setClippingEnabled(true);
            mPopupWindow.update((int) location.x, (int) location.y, mPopupWindow.getWidth(), mPopupWindow.getHeight());
        }
    };

    private final ViewTreeObserver.OnGlobalLayoutListener mArrowLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            ViewTreeObserverCompat.removeOnGlobalLayoutListener(mContentView.getViewTreeObserver(), this);

            RectF anchorRect = Utils.calculateRectOnScreen(mAnchorView);
            RectF contentViewRect = Utils.calculateRectOnScreen(mContentView);
            float x, y;
            if (Gravity.isVertical(mGravity)) {
                x = mContentView.getPaddingLeft() + Utils.dpToPx(2);
                float centerX = (contentViewRect.width() / 2f) - (mArrowView.getWidth() / 2f);
                float newX = centerX - (contentViewRect.centerX() - anchorRect.centerX());
                if (newX > x) {
                    if (newX + mArrowView.getWidth() + x > contentViewRect.width()) {
                        x = contentViewRect.width() - mArrowView.getWidth() - x;
                    } else {
                        x = newX;
                    }
                }
                y = mArrowView.getTop();
                y = y + (mGravity == Gravity.TOP ? -1 : +1);
            } else {
                y = mContentView.getPaddingTop() + Utils.dpToPx(2);
                float centerY = (contentViewRect.height() / 2f) - (mArrowView.getHeight() / 2f);
                float newY = centerY - (contentViewRect.centerY() - anchorRect.centerY());
                if (newY > y) {
                    if (newY + mArrowView.getHeight() + y > contentViewRect.height()) {
                        y = contentViewRect.height() - mArrowView.getHeight() - y;
                    } else {
                        y = newY;
                    }
                }
                x = mArrowView.getLeft();
                x = x + (mGravity == Gravity.LEFT ? -1 : +1);
            }
            mArrowView.setX(x);
            mArrowView.setY(y);
        }
    };

    private final ViewTreeObserver.OnScrollChangedListener mOnScrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {
        @Override
        public void onScrollChanged() {
            PointF location = calculateLocation();
            mPopupWindow.update((int) location.x, (int) location.y, mPopupWindow.getWidth(), mPopupWindow.getHeight());
        }
    };

    private final View.OnAttachStateChangeListener mOnAttachStateChangeListener = new View.OnAttachStateChangeListener() {
        @Override
        public void onViewAttachedToWindow(View v) {

        }

        @Override
        public void onViewDetachedFromWindow(View v) {
            dismiss();
        }
    };

    public static final class Builder {
        private boolean isDismissOnClick;
        private boolean isCancelable;
        private boolean isArrowEnabled;

        private int mBackgroundColor;
        private int mGravity;
        private int mTextAppearance;
        private int mTextStyle;
        private int mPadding;
        private int mMaxWidth;
        private int mDrawablePadding;

        private float mCornerRadius;
        private float mArrowHeight;
        private float mArrowWidth;
        private float mMargin;
        private float mTextSize;
        private float mLineSpacingExtra;
        private float mLineSpacingMultiplier = 1f;

        private Drawable mDrawableBottom;
        private Drawable mDrawableEnd;
        private Drawable mDrawableStart;
        private Drawable mDrawableTop;

        private Drawable mArrowDrawable;
        private CharSequence mText;
        private ColorStateList mTextColor;
        private Typeface mTypeface = Typeface.DEFAULT;

        private Context mContext;
        private View mAnchorView;

        private OnClickListener mOnClickListener;
        private OnLongClickListener mOnLongClickListener;
        private OnDismissListener mOnDismissListener;

        public Builder(@NonNull MenuItem anchorMenuItem) {
            this(anchorMenuItem, 0);
        }

        public Builder(@NonNull MenuItem anchorMenuItem, @StyleRes int resId) {
            View anchorView = anchorMenuItem.getActionView();
            if (anchorView != null) {
                if (anchorView instanceof TooltipActionView) {
                    ((TooltipActionView) anchorView).setMenuItem(anchorMenuItem);
                }

                init(anchorView.getContext(), anchorView, resId);
            } else {
                throw new NullPointerException("anchor menuItem haven`t actionViewClass");
            }
        }

        public Builder(@NonNull View anchorView) {
            this(anchorView, 0);
        }

        public Builder(@NonNull View anchorView, @StyleRes int resId) {
            init(anchorView.getContext(), anchorView, resId);
        }

        private void init(@NonNull Context context, @NonNull View anchorView, @StyleRes int resId) {
            mContext = context;
            mAnchorView = anchorView;

            TypedArray a = context.obtainStyledAttributes(resId, R.styleable.Tooltip);

            isCancelable = a.getBoolean(R.styleable.Tooltip_cancelable, false);
            isDismissOnClick = a.getBoolean(R.styleable.Tooltip_dismissOnClick, false);
            isArrowEnabled = a.getBoolean(R.styleable.Tooltip_arrowEnabled, true);
            mBackgroundColor = a.getColor(R.styleable.Tooltip_backgroundColor, Color.GRAY);
            mCornerRadius = a.getDimension(R.styleable.Tooltip_cornerRadius, -1);
            mArrowHeight = a.getDimension(R.styleable.Tooltip_arrowHeight, -1);
            mArrowWidth = a.getDimension(R.styleable.Tooltip_arrowWidth, -1);
            mArrowDrawable = a.getDrawable(R.styleable.Tooltip_arrowDrawable);
            mMargin = a.getDimension(R.styleable.Tooltip_margin, -1);
            mPadding = a.getDimensionPixelSize(R.styleable.Tooltip_android_padding, -1);
            mGravity = a.getInteger(R.styleable.Tooltip_android_gravity, Gravity.BOTTOM);
            mMaxWidth = a.getDimensionPixelSize(R.styleable.Tooltip_android_maxWidth, -1);
            mDrawablePadding = a.getDimensionPixelSize(R.styleable.Tooltip_android_drawablePadding, 0);
            mDrawableBottom = a.getDrawable(R.styleable.Tooltip_android_drawableBottom);
            mDrawableEnd = a.getDrawable(R.styleable.Tooltip_android_drawableEnd);
            mDrawableStart = a.getDrawable(R.styleable.Tooltip_android_drawableStart);
            mDrawableTop = a.getDrawable(R.styleable.Tooltip_android_drawableTop);
            mTextAppearance = a.getResourceId(R.styleable.Tooltip_textAppearance, -1);
            mText = a.getString(R.styleable.Tooltip_android_text);
            mTextSize = a.getDimension(R.styleable.Tooltip_android_textSize, -1);
            mTextColor = a.getColorStateList(R.styleable.Tooltip_android_textColor);
            mTextStyle = a.getInteger(R.styleable.Tooltip_android_textStyle, -1);
            mLineSpacingExtra = a.getDimensionPixelSize(R.styleable.Tooltip_android_lineSpacingExtra, 0);
            mLineSpacingMultiplier = a.getFloat(R.styleable.Tooltip_android_lineSpacingMultiplier, mLineSpacingMultiplier);

            final String fontFamily = a.getString(R.styleable.Tooltip_android_fontFamily);
            final int typefaceIndex = a.getInt(R.styleable.Tooltip_android_typeface, -1);
            mTypeface = getTypefaceFromAttr(fontFamily, typefaceIndex, mTextStyle);

            a.recycle();
        }
        public Builder setCancelable(boolean cancelable) {
            isCancelable = cancelable;
            return this;
        }
        public Builder setDismissOnClick(boolean isDismissOnClick) {
            this.isDismissOnClick = isDismissOnClick;
            return this;
        }
        public Builder setArrowEnabled(boolean isArrowEnabled) {
            this.isArrowEnabled = isArrowEnabled;
            return this;
        }
        public Builder setBackgroundColor(@ColorInt int color) {
            mBackgroundColor = color;
            return this;
        }
        public Builder setCornerRadius(@DimenRes int resId) {
            return setCornerRadius(mContext.getResources().getDimension(resId));
        }
        public Builder setCornerRadius(float radius) {
            mCornerRadius = radius;
            return this;
        }
        public Builder setArrowHeight(@DimenRes int resId) {
            return setArrowHeight(mContext.getResources().getDimension(resId));
        }
        public Builder setArrowHeight(float height) {
            mArrowHeight = height;
            return this;
        }
        public Builder setArrowWidth(@DimenRes int resId) {
            return setArrowWidth(mContext.getResources().getDimension(resId));
        }
        public Builder setArrowWidth(float width) {
            mArrowWidth = width;
            return this;
        }
        public Builder setArrow(@DrawableRes int resId) {
            return setArrow(ResourcesCompat.getDrawable(mContext.getResources(), resId, null));
        }
        public Builder setArrow(Drawable arrowDrawable) {
            mArrowDrawable = arrowDrawable;
            return this;
        }
        public Builder setMargin(@DimenRes int resId) {
            return setMargin(mContext.getResources().getDimension(resId));
        }
        public Builder setMargin(float margin) {
            mMargin = margin;
            return this;
        }
        public Builder setPadding(int padding) {
            mPadding = padding;
            return this;
        }
        @Deprecated
        public Builder setPadding(float padding) {
            return setPadding((int) padding);
        }
        public Builder setGravity(int gravity) {
            mGravity = gravity;
            return this;
        }
        public Builder setMaxWidth(int maxWidth) {
            mMaxWidth = maxWidth;
            return this;
        }
        public Builder setDrawablePadding(int padding) {
            mDrawablePadding = padding;
            return this;
        }
        public Builder setDrawableBottom(@DrawableRes int resId) {
            return setDrawableBottom(ResourcesCompat.getDrawable(mContext.getResources(), resId, null));
        }
        public Builder setDrawableBottom(Drawable drawable) {
            mDrawableBottom = drawable;
            return this;
        }
        public Builder setDrawableEnd(@DrawableRes int resId) {
            return setDrawableBottom(ResourcesCompat.getDrawable(mContext.getResources(), resId, null));
        }
        public Builder setDrawableEnd(Drawable drawable) {
            mDrawableEnd = drawable;
            return this;
        }
        public Builder setDrawableStart(@DrawableRes int resId) {
            return setDrawableStart(ResourcesCompat.getDrawable(mContext.getResources(), resId, null));
        }
        public Builder setDrawableStart(Drawable drawable) {
            mDrawableStart = drawable;
            return this;
        }
        public Builder setDrawableTop(@DrawableRes int resId) {
            return setDrawableTop(ResourcesCompat.getDrawable(mContext.getResources(), resId, null));
        }
        public Builder setDrawableTop(Drawable drawable) {
            mDrawableTop = drawable;
            return this;
        }
        public Builder setTextAppearance(@StyleRes int resId) {
            mTextAppearance = resId;
            return this;
        }
        public Builder setText(@StringRes int resId) {
            return setText(mContext.getString(resId));
        }
        public Builder setText(CharSequence text) {
            mText = text;
            return this;
        }
        public Builder setTextSize(@DimenRes int resId) {
            mTextSize = mContext.getResources().getDimension(resId);
            return this;
        }
        public Builder setTextSize(float size) {
            mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, size, mContext.getResources().getDisplayMetrics());
            return this;
        }
        public Builder setTextColor(@ColorInt int color) {
            mTextColor = ColorStateList.valueOf(color);
            return this;
        }
        public Builder setTextStyle(int style) {
            mTextStyle = style;
            return this;
        }
        public Builder setLineSpacing(@DimenRes int addResId, float mult) {
            mLineSpacingExtra = mContext.getResources().getDimensionPixelSize(addResId);
            mLineSpacingMultiplier = mult;
            return this;
        }
        public Builder setLineSpacing(float add, float mult) {
            mLineSpacingExtra = add;
            mLineSpacingMultiplier = mult;
            return this;
        }
        public Builder setTypeface(Typeface typeface) {
            mTypeface = typeface;
            return this;
        }
        public Builder setOnClickListener(OnClickListener listener) {
            mOnClickListener = listener;
            return this;
        }
        public Builder setOnLongClickListener(OnLongClickListener listener) {
            mOnLongClickListener = listener;
            return this;
        }
        public Builder setOnDismissListener(OnDismissListener listener) {
            mOnDismissListener = listener;
            return this;
        }
        public Tooltip build() {
            if (mArrowHeight == -1) {
                mArrowHeight = mContext.getResources().getDimension(R.dimen.default_tooltip_arrow_height);
            }
            if (mArrowWidth == -1) {
                mArrowWidth = mContext.getResources().getDimension(R.dimen.default_tooltip_arrow_width);
            }
            if (mMargin == -1) {
                mMargin = mContext.getResources().getDimension(R.dimen.default_tooltip_margin);
            }
            if (mPadding == -1) {
                mPadding = mContext.getResources().getDimensionPixelSize(R.dimen.default_tooltip_padding);
            }
            return new Tooltip(this);
        }
        public Tooltip show() {
            Tooltip tooltip = build();
            tooltip.show();
            return tooltip;
        }
        private Typeface getTypefaceFromAttr(String familyName, int typefaceIndex, int styleIndex) {
            Typeface tf = null;
            if (familyName != null) {
                tf = Typeface.create(familyName, styleIndex);
                if (tf != null) {
                    return tf;
                }
            }
            switch (typefaceIndex) {
                case 1: // SANS
                    tf = Typeface.SANS_SERIF;
                    break;
                case 2: // SERIF
                    tf = Typeface.SERIF;
                    break;
                case 3: // MONOSPACE
                    tf = Typeface.MONOSPACE;
                    break;
            }
            return tf;
        }
    }
}
