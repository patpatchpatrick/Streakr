package com.patrickdoyle30.android.streakr.helper;

import com.patrickdoyle30.android.streakr.R;

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
        } else if (mTheme == R.style.RedAppTheme) {
            return R.style.RedPopUpMenuTheme;
        } else if (mTheme == R.style.BlackAppTheme) {
            return R.style.BlackPopUpMenuTheme;
        } else {
            return R.style.DefaultPopUpMenuTheme;
        }

    }


}
