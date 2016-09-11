package com.weiqilab.hackathon.nextdoorhelp.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.weiqilab.hackathon.nextdoorhelp.R;
import com.weiqilab.hackathon.nextdoorhelp.extras.Keys;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    LoginButton loginButton;
    CallbackManager callbackManager;

    private AccessToken socialNetwokrAccessToken;

    private String str_loginReqField;
    private String str_userToken;
    private String str_oauthSocialNetworkAccessToken;
    private String str_loginType;
    private String str_purpose;
    private String str_userId;
    private String str_SocialNetwork_fullname;
    private String str_SocialNetwork_email;

    private String str_LastName;
    private String str_FirstName;
    private String str_FullName;
    private String str_SportType;
    private String str_SkiLevel;
    private String str_Birthday;
    private String str_gender;
    private String str_ProfilePicUrl;
    private String str_SocialNetworkId;
    private String str_getProfileBySocialNetworkId_ReqField;
    private String str_aboutMe;
    private String str_lastUpdatedTime;
    private String str_email;
    private String str_gcm_regId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();


        setContentView(R.layout.activity_login);


        // Facebook Button Init
        LoginButton fbLoginButton = (LoginButton) findViewById(R.id.btn_fb_login_button);

        // Permission for Facebook
        fbLoginButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_birthday", "user_friends"));
        fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // get token
                socialNetwokrAccessToken = loginResult.getAccessToken();
                //Log.i("token", socialNetwokrAccessToken.getToken());

                // App code
                // Get Facebook profile info: email, id, profile images
                GraphRequest request = GraphRequest.newMeRequest(
                        socialNetwokrAccessToken,
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.i("profile object", "" + object);

                                // Application code
                                try {

                                    str_oauthSocialNetworkAccessToken = socialNetwokrAccessToken.getToken();
                                    str_SocialNetworkId = object.getString("id");
                                    str_loginType = "FACEBOOK";
                                    str_loginReqField = "";
                                    str_ProfilePicUrl = object.getJSONObject("picture").getJSONObject("data").getString("url");
                                    str_SocialNetwork_email = object.getString("email");
                                    str_SocialNetwork_fullname = object.getString("name");

                                    // check if the new registration
                                    str_getProfileBySocialNetworkId_ReqField = "userId";
                                    //userProfileGetBySocialNetworkId(str_getProfileBySocialNetworkId_ReqField, str_SocialNetworkId, str_loginType);

                                    saveProfileData();
                                    goToNextPage();

                                } catch (JSONException e ){
                                    e.printStackTrace();
                                } catch (FacebookException e) {
                                    e.printStackTrace();
                                    new AlertDialog.Builder(getBaseContext())
                                            .setMessage("Cannot Log in with Facebook")
                                            .setCancelable(true)
                                            .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                }
                                            })
                                            .show();
                                }
                            }
                        });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,picture.width(300).height(300)");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    private String getKeyHash () {
        String keyHash = "";
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.weiqilab.hackathon.nextdoorhelp",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT);
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
        return keyHash;
    }


    private void saveProfileData() {
        // Save profile data into share preference
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.shared_pref_name), MODE_PRIVATE);

        // save to share preference
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Keys.ExtraGeneral.EXTRA_LOGIN_TYPE, str_loginType);

        if(str_gender != null && str_gender.length() != 0) {
            editor.putString(Keys.ExtraGeneral.EXTRA_PROFILE_GENDER, str_gender);
        }

        if(str_loginReqField != null && str_loginReqField.length() != 0) {
            editor.putString(Keys.ExtraGeneral.EXTRA_LOGIN_REQUEST_FIELD, str_loginReqField);
        }

        if(str_oauthSocialNetworkAccessToken != null && str_oauthSocialNetworkAccessToken.length() != 0) {
            editor.putString(Keys.ExtraGeneral.EXTRA_PROFILE_USER_SOCIAL_TOKEN, str_oauthSocialNetworkAccessToken);
        }

        if(str_userId != null && str_userId.length() != 0) {
            editor.putString(Keys.ExtraGeneral.EXTRA_PROFILE_USER_ID, str_userId);
        }

        if(str_SocialNetworkId != null && str_SocialNetworkId.length() != 0) {
            editor.putString(Keys.ExtraGeneral.EXTRA_OAUTH_SOCIALNETWORK_USERID, str_SocialNetworkId);
        }

        if(str_FullName != null && str_FullName.length() != 0) {
            editor.putString(Keys.ExtraGeneral.EXTRA_PROFILE_FULL_NAME, str_FullName);
        }

        if(str_Birthday != null && str_Birthday.length() != 0) {
            // Log.i("loginLoadProfile", str_Birthday);
            editor.putString(Keys.ExtraGeneral.EXTRA_PROFILE_BIRTHDAY, str_Birthday);
        }

        if(str_SkiLevel != null && str_SkiLevel.length() != 0) {
            editor.putString(Keys.ExtraGeneral.EXTRA_PROFILE_SKI_LEVEL, str_SkiLevel);
        }

        if(str_SportType != null && str_SportType.length() != 0) {
            editor.putString(Keys.ExtraGeneral.EXTRA_PROFILE_SPORT_TYPE, str_SportType);
        }

        if(str_ProfilePicUrl != null && str_ProfilePicUrl.length() != 0) {
            editor.putString(Keys.ExtraGeneral.EXTRA_PROFILE_PHOTO_URL, str_ProfilePicUrl);
        }

        if(str_FirstName != null && str_FirstName.length() != 0) {
            editor.putString(Keys.ExtraGeneral.EXTRA_PROFILE_FIRST_NAME, str_FirstName);
        }

        if(str_LastName != null && str_LastName.length() != 0) {
            editor.putString(Keys.ExtraGeneral.EXTRA_PROFILE_LAST_NAME, str_LastName);
        }

        if(str_aboutMe != null && str_aboutMe.length() != 0) {
            editor.putString(Keys.ExtraGeneral.EXTRA_PROFILE_ABOUT_ME, str_aboutMe);
        }

        if(str_lastUpdatedTime != null && str_lastUpdatedTime.length() != 0) {
            editor.putString(Keys.ExtraGeneral.EXTRA_PROFILE_LAST_UPDATED_TIME, str_lastUpdatedTime);
        }

        if(str_email != null && str_email.length() != 0) {
            editor.putString(Keys.ExtraGeneral.EXTRA_PROFILE_EMAIL, str_lastUpdatedTime);
        }
        editor.commit();
    }

    private void goToNextPage() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.enter_righttoleft, R.anim.exit_righttoleft);

        finish();
    }
}
