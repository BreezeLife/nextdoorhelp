package com.weiqilab.hackathon.nextdoorhelp.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

import org.json.JSONException;
import org.json.JSONObject;

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

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_righttoleft, R.anim.exit_righttoleft);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

    }


}
