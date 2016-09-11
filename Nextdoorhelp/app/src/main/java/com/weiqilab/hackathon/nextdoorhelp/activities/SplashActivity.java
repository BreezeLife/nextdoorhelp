package com.weiqilab.hackathon.nextdoorhelp.activities;

import android.app.Activity;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.weiqilab.hackathon.nextdoorhelp.R;
import com.weiqilab.hackathon.nextdoorhelp.extras.Constants;
import com.weiqilab.hackathon.nextdoorhelp.extras.Keys;

public class SplashActivity extends Activity {
    // Splash screen timer
    // AccessTokenTracker accessTokenTracker;
    TaskStackBuilder taskStackBuilder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        /*
         * Correct logic should be:
         * Only need to check local session, facebook sdk should not be consider as
         *
         */
/*
        //Facebook SDK init
        FacebookSdk.sdkInitialize(getApplicationContext());

        //Facebook SDK accessToken init
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken) {
                updateWithToken(newAccessToken);
            }
        };
        updateWithToken(AccessToken.getCurrentAccessToken());
*/
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // This method will be executed once the timer is over
                SharedPreferences setting = getApplicationContext().getSharedPreferences(getString(R.string.shared_pref_name), MODE_PRIVATE); // 0 - for private mode
                String userSocialToken = setting.getString(Keys.ExtraGeneral.EXTRA_PROFILE_USER_SOCIAL_TOKEN, "");

                Intent intent = null;
                if (userSocialToken.isEmpty() || userSocialToken.length() == 0) {
                    // show case reset
                    intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.enter_righttoleft, R.anim.exit_righttoleft);
                    finish();
                } else {
                    intent = new Intent(SplashActivity.this, NearbyActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.enter_righttoleft, R.anim.exit_righttoleft);
                    finish();
                }

                // close this activity
                finish();
            }
        }, Constants.EXTRA_SPLASH_TIME_OUT);
    }
}