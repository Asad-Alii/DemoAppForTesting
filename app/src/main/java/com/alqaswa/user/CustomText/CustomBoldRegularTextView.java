package com.alqaswa.user.CustomText;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.alqaswa.user.R;

/**
 * Created by user on 1/7/2017.
 */

public class CustomBoldRegularTextView extends TextView
{
    private static final String TAG = "TextView";

    private Typeface typeface;

    public CustomBoldRegularTextView(Context context) {
        super(context);
    }

    public CustomBoldRegularTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont(context, attrs);
    }

    public CustomBoldRegularTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setCustomFont(context, attrs);
    }

    private void setCustomFont(Context ctx, AttributeSet attrs) {
        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.app);
        String customFont = a.getString(R.styleable.app_customFont);
        setCustomFont(ctx, customFont);
        a.recycle();
    }

    private boolean setCustomFont(Context ctx, String asset) {
        try {
            if (typeface == null) {
                // Log.i(TAG, "asset:: " + "fonts/" + asset);
                typeface = Typeface.createFromAsset(ctx.getAssets(),
                        "SourceSansPro-Bold.otf");
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Log.e(TAG, "Could not get typeface: " + e.getMessage());
            return false;
        }

        setTypeface(typeface);
        return true;
    }
}
