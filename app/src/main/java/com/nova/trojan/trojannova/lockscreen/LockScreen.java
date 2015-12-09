package com.nova.trojan.trojannova.lockscreen;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nova.trojan.trojannova.R;
import com.nova.trojan.trojannova.SettingsActivity;
import com.nova.trojan.trojannova.utils.ContextUtils;

import org.jsoup.helper.StringUtil;

public class LockScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //remove action bar
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();

        //Set up our Lockscreen
        makeFullScreen();
        startService(new Intent(this, LockScreenService.class));

        setContentView(R.layout.lockscreen);
        updateLayout();
    }

    private void updateLayout(){
        SharedPreferences shared = getSharedPreferences(SettingsActivity.PREF_NAME, MODE_PRIVATE);

        //set the user name
        ((TextView)findViewById(R.id.username_textView)).setText(
                shared.getString(SettingsActivity.PREF_NAME_LOCK_SCREEN_DISPLAY_USER_NAME, ""));
        // set the user display image
        String displayimage = shared.getString(SettingsActivity.PREF_NAME_LOCK_SCREEN_DISPLAY_IMAGE_PATH, "");
        if(!StringUtil.isBlank(displayimage))
                ((ImageView)findViewById(R.id.userpicture_imageView)).setImageBitmap(ContextUtils.getScaledBitmap(displayimage, 4));

        // set the wallpaper image
        String wallpaper = shared.getString(SettingsActivity.PREF_NAME_LOCK_SCREEN_WALLPAPER_PATH, "");
        if(!StringUtil.isBlank(wallpaper))
            findViewById(R.id.background).setBackground(BitmapDrawable.createFromPath(wallpaper));
    }

    /**
     * A simple method that sets the screen to fullscreen.  It removes the Notifications bar,
     *   the Actionbar and the virtual keys (if they are on the phone)
     */
    public void makeFullScreen() {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if(Build.VERSION.SDK_INT < 19) { //View.SYSTEM_UI_FLAG_IMMERSIVE is only on API 19+
            this.getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        } else {
            this.getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }
    }

    @Override
    public void onBackPressed() {
        return; //Do nothing!
    }

    public void unlockScreen(View view) {
        /* Get Button Object */
        Button iv = (Button) view.findViewById(R.id.button);

        /* Create Animation */
        Animation rotation = AnimationUtils.loadAnimation(this, R.anim.button_rotate);

        /* start Animation */
        iv.startAnimation(rotation);

        //Instead of using finish(), this totally destroys the process
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
