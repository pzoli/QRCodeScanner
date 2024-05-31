package hu.infokristaly.forrasadmin.qrcodescanner.components;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

import hu.infokristaly.forrasadmin.qrcodescanner.R;

public final class ThemeSetup {

    private ThemeSetup() {
    }

    public static void applyTheme(String mode, Context context) {
        if (context.getString(R.string.settings_theme_value_dark).equals(mode)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if (context.getString(R.string.settings_theme_value_light).equals(mode)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

}