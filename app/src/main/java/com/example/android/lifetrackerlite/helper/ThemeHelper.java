package com.example.android.lifetrackerlite.helper;

import com.example.android.lifetrackerlite.R;

public final class ThemeHelper {

    //Class to help set menus, colors, etc... based on whichever theme the user has selected

    private static int mTheme;

    private ThemeHelper() {
    }

    public static void setTheme(int theme) {
        mTheme = theme;
    }

    public static int getTheme() {
        return mTheme;
    }

    public static int getPopUpTheme() {

        //Return the correct popUp menu theme based on user-selected theme

        if (mTheme == R.style.PinkAppTheme) {
            return R.style.PinkPopUpMenuTheme;
        } else if (mTheme == R.style.BlueAppTheme) {
            return R.style.BluePopUpMenuTheme;
        } else {
            return R.style.DefaultPopUpMenuTheme;
        }

    }


}
