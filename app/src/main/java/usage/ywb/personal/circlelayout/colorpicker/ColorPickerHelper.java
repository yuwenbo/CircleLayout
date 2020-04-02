package usage.ywb.personal.circlelayout.colorpicker;/*
 * Copyright (C) 2011 Sergey Margaritov
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


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import androidx.core.content.ContextCompat;

import usage.ywb.personal.circlelayout.R;
import usage.ywb.personal.circlelayout.utils.Constants;

/**
 * A preference type that allows a user to choose a time
 *
 * @author Sergey Margaritov
 */
public class ColorPickerHelper implements ColorPickerDialog.OnColorChangedListener {


    private Context context;
    private ColorPickerDialog mDialog;
    private int mValue;
    private boolean mAlphaSliderEnabled = false;
    private boolean mHexValueEnabled = false;

    public ColorPickerHelper(Context context) {
        this.context = context;
        SharedPreferences preferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        mValue = ContextCompat.getColor(context, R.color.main_color);
        mValue = preferences.getInt(Constants.KEY_THEME_COLOR, mValue);
    }

    @Override
    public void onColorChanged(int color) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        preferences.edit().putInt(Constants.KEY_THEME_COLOR, color).apply();
    }

    public void showDialog() {
        mDialog = new ColorPickerDialog(context, mValue);
        mDialog.setOnColorChangedListener(this);
        if (mAlphaSliderEnabled) {
            mDialog.setAlphaSliderVisible(true);
        }
        if (mHexValueEnabled) {
            mDialog.setHexValueEnabled(true);
        }
        mDialog.show();
    }

    /**
     * Toggle Alpha Slider visibility (by default it's disabled)
     */
    public void setAlphaSliderEnabled(boolean enable) {
        mAlphaSliderEnabled = enable;
    }

    /**
     * Toggle Hex Value visibility (by default it's disabled)
     */
    public void setHexValueEnabled(boolean enable) {
        mHexValueEnabled = enable;
    }

    /**
     * For custom purposes. Not used by ColorPickerPreferrence
     *
     * @author Unknown
     */
    public static String convertToARGB(int color) {
        String alpha = Integer.toHexString(Color.alpha(color));
        String red = Integer.toHexString(Color.red(color));
        String green = Integer.toHexString(Color.green(color));
        String blue = Integer.toHexString(Color.blue(color));

        if (alpha.length() == 1) {
            alpha = "0" + alpha;
        }

        if (red.length() == 1) {
            red = "0" + red;
        }

        if (green.length() == 1) {
            green = "0" + green;
        }

        if (blue.length() == 1) {
            blue = "0" + blue;
        }

        return "#" + alpha + red + green + blue;
    }

    /**
     * For custom purposes. Not used by ColorPickerHelper
     *
     * @return A string representing the hex value of color,
     * without the alpha value
     * @author Charles Rosaaen
     */
    public static String convertToRGB(int color) {
        String red = Integer.toHexString(Color.red(color));
        String green = Integer.toHexString(Color.green(color));
        String blue = Integer.toHexString(Color.blue(color));

        if (red.length() == 1) {
            red = "0" + red;
        }
        if (green.length() == 1) {
            green = "0" + green;
        }
        if (blue.length() == 1) {
            blue = "0" + blue;
        }
        return "#" + red + green + blue;
    }

    public static int convertToColorInt(String argb) throws IllegalArgumentException {
        if (!argb.startsWith("#")) {
            argb = "#" + argb;
        }
        return Color.parseColor(argb);
    }

}