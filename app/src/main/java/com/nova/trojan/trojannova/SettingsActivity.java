package com.nova.trojan.trojannova;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;

import com.nova.trojan.trojannova.appmngr.AppManagerActivity;
import com.nova.trojan.trojannova.lockscreen.LockScreenService;
import com.nova.trojan.trojannova.utils.CacheStorage;
import com.nova.trojan.trojannova.utils.ContextUtils;

import org.jsoup.helper.StringUtil;

import java.util.HashMap;

public class SettingsActivity extends Activity {


    public static final String PREF_NAME = "com.nova.trojan.trojannova";
    public static final String PREF_NAME_LOCK_SCREEN_DISPLAY_USER_NAME = "lockscreen_username";
    public static final String PREF_NAME_LOCK_SCREEN_DISPLAY_IMAGE_PATH = "lockscreen_userimagepath";
    public static final String PREF_NAME_LOCK_SCREEN_WALLPAPER_PATH = "lockscreen_wallpaperpath";
    public static final String PREF_NAME_WALLPAPAER_IMAGE_PATH = "wallpaper_imagepath";


    private static final int INTENT_ID_SELECT_LOCK_SCREEN_IMAGE = 10;
    private static final int INTENT_ID_SELECT_LOCK_SCREEN_WALL_PAPER = 12;
    private static final int INTENT_ID_SELECT_WALL_PAPER_IMAGE = 11;

    private static HashMap<String,Integer> KeyToIntenIdMap = new HashMap<String,Integer>(){{
        put("unlock_screen_image",INTENT_ID_SELECT_LOCK_SCREEN_IMAGE);
        put("unlock_screen_wallpaper_image",INTENT_ID_SELECT_LOCK_SCREEN_WALL_PAPER);
        put("wallpaper_image",INTENT_ID_SELECT_WALL_PAPER_IMAGE);
    }};

    private static  SharedPreferences sharedPreferences ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        CacheStorage.setupCache(this);
        ContextUtils.setupUtils(this);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }


    public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
            updatePreferences();
        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
        }


        private void updatePreferences() {
            for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); ++i) {
                Preference preference = getPreferenceScreen().getPreference(i);
                if (preference instanceof PreferenceGroup) {
                    PreferenceGroup preferenceGroup = (PreferenceGroup) preference;
                    for (int j = 0; j < preferenceGroup.getPreferenceCount(); ++j) {
                        updatePreference(preferenceGroup.getPreference(j), false);
                    }
                } else {
                    updatePreference(preference, false);
                }
            }
        }

        private void updatePreference(Preference p, boolean changed) {
            if (p instanceof EditTextPreference) {
                EditTextPreference editTextPref = (EditTextPreference) p;
                p.setSummary(editTextPref.getText());

                // if preference was changed then update it
                if(changed && p.getKey().equals("unlock_screen_display_name")) {
                    // update the display name in shared preferences so that it can be read from lock screen
                    sharedPreferences.edit().putString(PREF_NAME_LOCK_SCREEN_DISPLAY_USER_NAME, editTextPref.getText()).commit();
                }

            }else if (p instanceof SwitchPreference) {
                SwitchPreference switchPreference = (SwitchPreference) p;
                if(p.getKey().equals("unlock_screen_enable")) {

                    // if preference was changed then update it else find the value by checking is service is running
                    if(changed){
                        if(!switchPreference.isChecked()){
                            stopService(new Intent(SettingsActivity.this, LockScreenService.class));
                        }else {
                            startService(new Intent(SettingsActivity.this, LockScreenService.class));
                        }
                    }else{
                        switchPreference.setChecked(LockScreenService.isServiceRunning(SettingsActivity.this));
                    }
                }
            }

            // update icons on image prefs if set
            if(p.getKey().equals("unlock_screen_image")){
                String path = sharedPreferences.getString(PREF_NAME_LOCK_SCREEN_DISPLAY_IMAGE_PATH, "");
                if(!StringUtil.isBlank(path)) p.setIcon(ContextUtils.getSmallDrawable(path));
            }else if (p.getKey().equals("unlock_screen_wallpaper_image")){
                String path = sharedPreferences.getString(PREF_NAME_LOCK_SCREEN_WALLPAPER_PATH, "");
                if(!StringUtil.isBlank(path)){
                    p.setIcon(ContextUtils.getSmallDrawable(path));
                }else{
                    p.setIcon(R.drawable.usc_color);
                }
            }else if (p.getKey().equals("wallpaper_image")){
                String path = sharedPreferences.getString(PREF_NAME_WALLPAPAER_IMAGE_PATH, "");
                if(!StringUtil.isBlank(path)) p.setIcon(ContextUtils.getSmallDrawable(path));
            }
        }




        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if(preference.getKey().endsWith("_image")){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, KeyToIntenIdMap.get(preference.getKey()));
            }else if(preference.getKey().equals("widget_install")){

                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setTitle("Trojan Widget - How to");
                builder.setMessage("Go to your usual widget list and select TrojanNova widget from the list");
                builder.setNeutralButton("Will do", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { dialog.cancel(); }
                });
                builder.show();
            }

            else {
                return super.onPreferenceTreeClick(preferenceScreen, preference);
            }
            return true;
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            updatePreference(findPreference(key), true);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (resultCode == RESULT_OK) {

                switch (requestCode) {
                    case INTENT_ID_SELECT_LOCK_SCREEN_IMAGE:
                        Uri imageUri = data.getData();
                        // put the image path in sharedPreferences pref so that lockscreen can read it when starting
                        sharedPreferences.edit().putString(PREF_NAME_LOCK_SCREEN_DISPLAY_IMAGE_PATH, ContextUtils.getRealPathFromUri(imageUri)).commit();

                        break;
                    case INTENT_ID_SELECT_LOCK_SCREEN_WALL_PAPER:
                        imageUri = data.getData();
                        // put the image path in sharedPreferences pref so that lockscreen can read it when starting
                        sharedPreferences.edit().putString(PREF_NAME_LOCK_SCREEN_WALLPAPER_PATH, ContextUtils.getRealPathFromUri(imageUri)).commit();

                        break;
                    case INTENT_ID_SELECT_WALL_PAPER_IMAGE:
                        imageUri = data.getData();
                        //do with uri what you like
                        String path = ContextUtils.getRealPathFromUri(imageUri);

                        // update wallpaper
                        ContextUtils.setWallpaper(path);

                        // put the wallpaper image in sharedPreferences pref so that we can read when required
                        sharedPreferences.edit().putString(PREF_NAME_WALLPAPAER_IMAGE_PATH, path).commit();
                        break;
                }


            } else if (resultCode == RESULT_CANCELED) {
//                System.out.print("image cancelled: ");
            }

            super.onActivityResult(requestCode, resultCode, data);
        }

        private void addShortcut() {
            //Adding shortcut for MainActivity
            //on Home screen
            Intent shortcutIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
            shortcutIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 1024);

            Intent addIntent = new Intent();
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "Add Trojan Widget");
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                    Intent.ShortcutIconResource.fromContext(getApplicationContext(),
                            R.drawable.trojan_icon));

            addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            getApplicationContext().sendBroadcast(addIntent);
        }

        private void removeShortcut() {

            //Deleting shortcut for MainActivity
            //on Home screen
            Intent shortcutIntent = new Intent(getApplicationContext(), AppManagerActivity.class);
            shortcutIntent.setAction(Intent.ACTION_MAIN);

            Intent addIntent = new Intent();
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "HelloWorldShortcut");

            addIntent.setAction("com.android.launcher.action.UNINSTALL_SHORTCUT");
            getApplicationContext().sendBroadcast(addIntent);
        }
    }
}
