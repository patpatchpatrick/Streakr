package com.patrickdoyle30.android.streakr.helper;

import com.patrickdoyle30.android.streakr.R;

public final class PreferenceHelper {

    //Class to help set preferences throughout the app (track themes and in-app purchases
    //Class to help set menus, colors, etc... based on whichever theme the user has selected


    private static int mTheme;
    private static boolean mAdFree;

    private PreferenceHelper() {
    }

    public static void setAdFree(Boolean adFreePaidFor) {
        //Track if user has purchased "ad removal" for the app.  This check is used throughout the app
        //to determine whether or not to show ads.
        if (adFreePaidFor){
            mAdFree = true;
        } else {
            mAdFree = false;
        }
    }

    public static boolean getAdFree(){
        return mAdFree;
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
