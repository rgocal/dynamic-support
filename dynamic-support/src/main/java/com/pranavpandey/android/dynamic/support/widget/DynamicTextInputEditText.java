/*
 * Copyright 2018 Pranav Pandey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pranavpandey.android.dynamic.support.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;

import com.pranavpandey.android.dynamic.support.R;
import com.pranavpandey.android.dynamic.support.theme.DynamicColorType;
import com.pranavpandey.android.dynamic.support.theme.DynamicTheme;
import com.pranavpandey.android.dynamic.support.utils.DynamicInputUtils;
import com.pranavpandey.android.dynamic.utils.DynamicColorUtils;

/**
 * A TextInputEditText to change its color according to the
 * supplied {@link R.attr#ads_colorType}.
 */
public class DynamicTextInputEditText extends TextInputEditText implements DynamicWidget {

    /**
     * Color type applied to this view.
     *
     * @see DynamicColorType
     */
    private @DynamicColorType int mColorType;

    /**
     * Background color type for this view so that it will remain in
     * contrast with this color type.
     */
    private @DynamicColorType int mContrastWithColorType;

    /**
     * Color applied to this view.
     */
    private @ColorInt int mColor;

    /**
     * Background color for this view so that it will remain in
     * contrast with this color.
     */
    private @ColorInt int mContrastWithColor;

    /**
     * {@code true} if this view will change its color according
     * to the background. It was introduced to provide better legibility for
     * colored texts and to avoid dark text on dark background like situations.
     *
     * <p>If this boolean is set then, it will check for the contrast color and
     * do color calculations according to that color so that this text view will
     * always be visible on that background. If no contrast color is found then,
     * it will take default background color.</p>
     *
     * @see #mContrastWithColor
     */
    private boolean mBackgroundAware;

    public DynamicTextInputEditText(@NonNull Context context) {
        this(context, null);
    }

    public DynamicTextInputEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        loadFromAttributes(attrs);
    }

    public DynamicTextInputEditText(@NonNull Context context,
                                    @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        loadFromAttributes(attrs);
    }

    @Override
    public void loadFromAttributes(@Nullable AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.DynamicTheme);

        try {
            mColorType = a.getInt(R.styleable.DynamicTheme_ads_colorType,
                    DynamicColorType.ACCENT);
            mContrastWithColorType = a.getInt(
                    R.styleable.DynamicTheme_ads_contrastWithColorType,
                    DynamicColorType.BACKGROUND);
            mColor = a.getColor(R.styleable.DynamicTheme_ads_color,
                    WidgetDefaults.ADS_COLOR_UNKNOWN);
            mContrastWithColor = a.getColor(R.styleable.DynamicTheme_ads_contrastWithColor,
                    WidgetDefaults.getDefaultContrastWithColor(getContext()));
            mBackgroundAware = a.getBoolean(
                    R.styleable.DynamicTheme_ads_backgroundAware,
                    WidgetDefaults.ADS_BACKGROUND_AWARE);
        } finally {
            a.recycle();
        }

        initialize();
    }

    @Override
    public void initialize() {
        if (mColorType != DynamicColorType.NONE
                && mColorType != DynamicColorType.CUSTOM) {
            mColor = DynamicTheme.getInstance().getColorFromType(mColorType);
        }

        if (mContrastWithColorType != DynamicColorType.NONE
                && mContrastWithColorType != DynamicColorType.CUSTOM) {
            mContrastWithColor = DynamicTheme.getInstance()
                    .getColorFromType(mContrastWithColorType);
        }

        setColor();
    }

    @Override
    public @DynamicColorType int getColorType() {
        return mColorType;
    }

    @Override
    public void setColorType(@DynamicColorType int colorType) {
        this.mColorType = colorType;

        initialize();
    }

    @Override
    public @DynamicColorType int getContrastWithColorType() {
        return mContrastWithColorType;
    }

    @Override
    public void setContrastWithColorType(@DynamicColorType int contrastWithColorType) {
        this.mContrastWithColorType = contrastWithColorType;

        initialize();
    }

    @Override
    public @ColorInt int getColor() {
        return mColor;
    }

    @Override
    public void setColor(@ColorInt int color) {
        this.mColorType = DynamicColorType.CUSTOM;
        this.mColor = color;

        setColor();
    }

    @Override
    public @ColorInt int getContrastWithColor() {
        return mContrastWithColor;
    }

    @Override
    public void setContrastWithColor(@ColorInt int contrastWithColor) {
        this.mContrastWithColorType = DynamicColorType.CUSTOM;
        this.mContrastWithColor = contrastWithColor;

        setColor();
    }

    @Override
    public boolean isBackgroundAware() {
        return mBackgroundAware;
    }

    @Override
    public void setBackgroundAware(boolean backgroundAware) {
        this.mBackgroundAware = backgroundAware;

        setColor();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        setAlpha(enabled ? WidgetDefaults.ADS_ALPHA_ENABLED : WidgetDefaults.ADS_ALPHA_DISABLED);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, @ViewCompat.FocusDirection int direction,
                                  @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);

        if (gainFocus) {
            setColor();
        }
    }

    @Override
    public void setColor() {
        if (mColor != WidgetDefaults.ADS_COLOR_UNKNOWN) {
            if (mBackgroundAware && mContrastWithColor != WidgetDefaults.ADS_COLOR_UNKNOWN) {
                mColor = DynamicColorUtils.getContrastColor(mColor, mContrastWithColor);
            }

            post(new Runnable() {
                @Override
                public void run() {
                    DynamicInputUtils.setColor(DynamicTextInputEditText.this, mColor);
                }
            });
        }
    }
}