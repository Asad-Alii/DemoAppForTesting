package com.alqaswa.user;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.alqaswa.user.Fragment.ForgotpassFragment;
import com.alqaswa.user.Fragment.SignupFragment;
import com.alqaswa.user.HttpRequester.AsyncTaskCompleteListener;
import com.alqaswa.user.HttpRequester.MultiPartRequester;
import com.alqaswa.user.HttpRequester.VollyRequester;
import com.alqaswa.user.Models.SocialMediaProfile;
import com.alqaswa.user.Utils.AndyUtils;
import com.alqaswa.user.Utils.Commonutils;
import com.alqaswa.user.Utils.Const;
import com.alqaswa.user.Utils.ParseContent;
import com.alqaswa.user.Utils.PreferenceHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.TimeZone;

/**
 * Created by user on 1/4/2017.
 */

public class SignInActivity extends AppCompatActivity implements View.OnClickListener, AsyncTaskCompleteListener, GoogleApiClient.OnConnectionFailedListener {
    private ImageButton btn_cancel;
    private TextView btn_forgot_pass, btn_new_user;
    public static String currentfragment = "";
    private RelativeLayout log_layout;
    private Button btn_login_fb;
    private String loginType = Const.MANUAL;
    private String sFirstName, sLastName, sEmailId, sPassword, sUserName, sSocial_unique_id, pictureUrl;
    private CallbackManager callbackManager;
    private String sPictureUrl;
    private String sLoginUserId, sLoginPassword;

    private GoogleApiClient mGoogleApiClient;
    private String filePath = "";
    private SocialMediaProfile mediaProfile;
    private TextView login_btn;
    private EditText et_login_password, et_login_userid;
    private int mFragmentId = 0;
    private String mFragmentTag = null;
    private ImageButton loc_pass;
    private boolean isclicked = false;
    private ParseContent pcontent;
    private TextInputLayout input_layout_userid, input_layout_pass;
    private TextView btn_register_social;
    private static final int RC_SIGN_IN = 007;
    Dialog social_dialog;
    private ImageView social_img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        callbackManager = CallbackManager.Factory.create();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        facebookRegisterCallBack();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        setContentView(R.layout.login);
        pcontent = new ParseContent(this);
        btn_cancel = (ImageButton) findViewById(R.id.btn_cancel);
        social_img = (ImageView) findViewById(R.id.social_img);
        input_layout_userid = (TextInputLayout) findViewById(R.id.input_layout_userid);
        input_layout_pass = (TextInputLayout) findViewById(R.id.input_layout_pass);
        et_login_password = (EditText) findViewById(R.id.et_login_password);
        et_login_userid = (EditText) findViewById(R.id.et_login_userid);
        btn_forgot_pass = (TextView) findViewById(R.id.btn_forgot_pass);
        btn_new_user = (TextView) findViewById(R.id.btn_new_user);
        log_layout = (RelativeLayout) findViewById(R.id.log_layout);
        btn_login_fb = (Button) findViewById(R.id.btn_login_fb);
        login_btn = (TextView) findViewById(R.id.login_btn);
        loc_pass = (ImageButton) findViewById(R.id.loc_pass);

        (btn_register_social = (TextView) findViewById(R.id.btn_register_social)).setOnClickListener(this);

        btn_cancel.setOnClickListener(this);
        btn_login_fb.setOnClickListener(this);
        btn_new_user.setOnClickListener(this);
        btn_forgot_pass.setOnClickListener(this);
        login_btn.setOnClickListener(this);




       /* et_login_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loc_pass.setVisibility(View.VISIBLE);
                loc_pass.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isclicked == false) {
                            et_login_password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            et_login_password.setSelection(et_login_password.getText().length());
                            isclicked = true;
                            loc_pass.setVisibility(View.VISIBLE);

                        } else {
                            isclicked = false;
                            et_login_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            loc_pass.setVisibility(View.VISIBLE);

                            et_login_password.setSelection(et_login_password.getText().length());
                        }
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
*/

    }

    public void addFragment(Fragment fragment, boolean addToBackStack,
                            String tag, boolean isAnimate) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        if (isAnimate) {
            ft.setCustomAnimations(R.anim.slide_in_right,
                    R.anim.slide_out_left, R.anim.slide_in_left,
                    R.anim.slide_out_right);

        }

        if (addToBackStack) {
            ft.addToBackStack(tag);
        }
        ft.replace(R.id.frame_login, fragment, tag);
        ft.commitAllowingStateLoss();
    }


    @Override
    public void onResume() {
        super.onResume();
        currentfragment = "";
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_cancel:
                Intent i = new Intent(getApplicationContext(), WelcomeActivity.class);
                startActivity(i);
                break;
            case R.id.btn_new_user:
                addFragment(new SignupFragment(), false, Const.REGISTER_FRAGMENT, true);
                log_layout.setVisibility(View.GONE);
                break;
            case R.id.btn_login_fb:
                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email", "user_birthday", "user_photos", "user_location"));
                loginType = Const.SOCIAL_FACEBOOK;
                break;
            case R.id.btn_register_social:
                showSocialPopUP();
                break;
            case R.id.login_btn:
                if (validate()) {
                    UserLogin(Const.MANUAL);
                }
                break;
            case R.id.btn_forgot_pass:
                addFragment(new ForgotpassFragment(), false, Const.FORGOT_PASSWORD_FRAGMENT, true);
                log_layout.setVisibility(View.GONE);
                break;


        }
    }

    private void showSocialPopUP() {
        social_dialog = new Dialog(SignInActivity.this, R.style.DialogThemeforview);
        social_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        social_dialog.setCancelable(true);
        social_dialog.setContentView(R.layout.social_connect_popup);
        ImageView back_social = (ImageView) social_dialog.findViewById(R.id.back_social);
        LinearLayout lay_google = (LinearLayout) social_dialog.findViewById(R.id.lay_google);
        LinearLayout lay_fb = (LinearLayout) social_dialog.findViewById(R.id.lay_fb);
        back_social.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                social_dialog.dismiss();
            }
        });
        lay_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndyUtils.showSimpleProgressDialog(SignInActivity.this, getResources().getString(R.string.txt_gmail), false);
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
        lay_fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(SignInActivity.this, Arrays.asList("public_profile", "email", "user_birthday", "user_photos", "user_location"));
                loginType = Const.SOCIAL_FACEBOOK;
            }
        });
        social_dialog.show();
    }

    private void getLoginDetails() {
        sLoginUserId = et_login_userid.getText().toString().trim();
        sLoginPassword = et_login_password.getText().toString().trim();
    }

    private boolean validate() {
        getLoginDetails();
        if (sLoginUserId.length() == 0) {
            input_layout_userid.setError(getResources().getString(R.string.txt_email_error));
            et_login_userid.requestFocus();
            return false;
        } else if (sLoginPassword.length() == 0) {
            input_layout_pass.setError(getResources().getString(R.string.txt_pass_error));
            et_login_password.requestFocus();
            return false;
        } else {
            input_layout_userid.setError(null);
            input_layout_pass.setError(null);
            return true;

        }
    }


    public void startActivityForResult(Intent intent, int requestCode,
                                       String fragmentTag) {

        mFragmentTag = fragmentTag;
        mFragmentId = 0;
        super.startActivityForResult(intent, requestCode);
    }

    private void facebookRegisterCallBack() {
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            //    private FacebookCallback<LoginResult> callback = new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                                if (jsonObject != null && graphResponse != null) {
                                    AndyUtils.appLog("Json Object", jsonObject.toString());
                                    AndyUtils.appLog("Graph response", graphResponse.toString());
                                    try {
                                        sUserName = jsonObject.getString("name");
                                        sEmailId = jsonObject.getString("email");
                                        sSocial_unique_id = jsonObject.getString("id");
                                        sPictureUrl = "https://graph.facebook.com/" + sSocial_unique_id + "/picture?type=large";
                                        mediaProfile = new SocialMediaProfile();

                                        new AQuery(SignInActivity.this).id(R.id.social_img).image(sPictureUrl, true, true,
                                                200, 0, new BitmapAjaxCallback() {

                                                    @Override
                                                    public void callback(String url, ImageView iv, Bitmap bm,
                                                                         AjaxStatus status) {

                                                        if (url != null && !url.equals("")) {
                                                            sPictureUrl = new AQuery(SignInActivity.this).getCachedFile(url).getPath();
                                                            mediaProfile.setPictureUrl(sPictureUrl);
                                                        }

                                                    }

                                                });

                                        if (sUserName != null) {
                                            String[] name = sUserName.split(" ");
                                            if (name[0] != null) {
                                                mediaProfile.setFirstName(name[0]);
                                            }
                                            if (name[1] != null) {
                                                mediaProfile.setLastName(name[1]);
                                            }
                                        }
                                        mediaProfile.setEmailId(sEmailId);
                                        mediaProfile.setSocialUniqueId(sSocial_unique_id);

                                        mediaProfile.setLoginType(Const.SOCIAL_FACEBOOK);

                                        AndyUtils.appLog("all details", sUserName + "" + sEmailId + " " + " " + sPictureUrl);
                                        if (sSocial_unique_id != null) {
                                            loginType = Const.SOCIAL_FACEBOOK;
                                            UserLogin(Const.SOCIAL_FACEBOOK);
                                        } else {
                                            AndyUtils.showShortToast("Invalidate Data", SignInActivity.this);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }
                        }

                );
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,link,locale,hometown,email,gender,birthday,location");
                request.setParameters(parameters);
                request.executeAsync();
            }


            @Override
            public void onCancel() {
                AndyUtils.showLongToast(getString(R.string.login_cancelled), SignInActivity.this);
            }

            @Override
            public void onError(FacebookException error) {
                AndyUtils.showLongToast(getString(R.string.login_failed), SignInActivity.this);
                AndyUtils.appLog("login failed Error", error.toString());
            }
        });
    }

    private void UserLogin(String logintype) {

        Commonutils.progressdialog_show(this, getResources().getString(R.string.txt_signin));

        if (logintype.equalsIgnoreCase(Const.MANUAL)) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Const.Params.URL, Const.ServiceType.LOGIN);
            map.put(Const.Params.EMAIL, sLoginUserId);
            map.put(Const.Params.PASSWORD, sLoginPassword);
            map.put(Const.Params.DEVICE_TOKEN, new PreferenceHelper(this).getDeviceToken());
            map.put(Const.Params.DEVICE_TYPE, Const.DEVICE_TYPE_ANDROID);
            map.put(Const.Params.LOGIN_BY, Const.MANUAL);
            Log.d("mahi", map.toString());
            new VollyRequester(this, Const.POST, map, Const.ServiceCode.LOGIN,
                    this);
        } else {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Const.Params.URL, Const.ServiceType.LOGIN);
            map.put(Const.Params.SOCIAL_ID, sSocial_unique_id);

            map.put(Const.Params.DEVICE_TOKEN, new PreferenceHelper(this).getDeviceToken());
            map.put(Const.Params.DEVICE_TYPE, Const.DEVICE_TYPE_ANDROID);
            map.put(Const.Params.LOGIN_BY, logintype);

            Log.d("mahi", "social" + map.toString());
            new VollyRequester(this, Const.POST, map, Const.ServiceCode.LOGIN,
                    this);
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("Activity Res", "" + requestCode);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        Fragment fragment = null;

        if (mFragmentId > 0) {
            fragment = getSupportFragmentManager().findFragmentById(
                    mFragmentId);
        } else if (mFragmentTag != null
                && !mFragmentTag.equalsIgnoreCase("")) {
            fragment = getSupportFragmentManager().findFragmentByTag(
                    mFragmentTag);
        }
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            AndyUtils.removeProgressDialog();
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            mediaProfile = new SocialMediaProfile();
            String personPhotoUrl;
            Log.e("mahi", "display name: " + acct.getDisplayName());

            String personName = acct.getDisplayName();
            if (null != acct.getPhotoUrl()) {
                personPhotoUrl = acct.getPhotoUrl().toString();

            } else {
                personPhotoUrl = "";
            }

            String email = acct.getEmail();

            Log.e("mahi", "Name: " + personName + ", email: " + email
                    + ", Image: " + personPhotoUrl);

            loginType = "google";

            if (personName.contains(" ")) {
                String[] split = personName.split(" ");
                mediaProfile.setFirstName(split[0]);
                mediaProfile.setLastName(split[1]);
            } else {
                mediaProfile.setFirstName(personName);
            }
            if (!TextUtils.isEmpty(personPhotoUrl)
                    || !personPhotoUrl.equalsIgnoreCase("null")) {
                sPictureUrl = personPhotoUrl;
                new AQuery(SignInActivity.this).id(R.id.social_img).image(sPictureUrl, true, true,
                        200, 0, new BitmapAjaxCallback() {

                            @Override
                            public void callback(String url, ImageView iv, Bitmap bm,
                                                 AjaxStatus status) {

                                if (url != null && !url.equals("")) {
                                    sPictureUrl = new AQuery(SignInActivity.this).getCachedFile(url).getPath();
                                    mediaProfile.setPictureUrl(sPictureUrl);
                                }

                            }

                        });
            } else {
                mediaProfile.setPictureUrl("");
            }
            mediaProfile.setEmailId(email);
            sSocial_unique_id = acct.getId();
            mediaProfile.setSocialUniqueId(sSocial_unique_id);

            mediaProfile.setLoginType("google");

            sEmailId = email;
            UserLogin(loginType);

        } else {
            AndyUtils.removeProgressDialog();
        }
    }

    @Override
    public void onBackPressed() {

        if (log_layout.getVisibility() == View.GONE) {
            startActivity(new Intent(SignInActivity.this, SignInActivity.class));
        } else {
            startActivity(new Intent(SignInActivity.this, WelcomeActivity.class));
            finish();
        }
    }

    @Override
    public void onTaskCompleted(String response, int serviceCode) {
        switch (serviceCode) {
            case Const.ServiceCode.LOGIN:
                Log.d("mahi", "" + response);

                if (response != null) {

                    try {
                        JSONObject job1 = new JSONObject(response);
                        if (job1.getString("success").equals("true")) {
                            Commonutils.progressdialog_hide();
                            if (pcontent.isSuccessWithStoreId(response)) {

                                pcontent.parseUserAndStoreToDb(response);
                                new PreferenceHelper(this).putPassword(et_login_password.getText()
                                        .toString());
                                startActivity(new Intent(this, MainActivity.class));
                                this.finish();
                            } else {

                            }

                        } else {
                            Commonutils.progressdialog_hide();
                            if (job1.getString("error_code").equals("125")) {

                                if (mediaProfile != null) {
                                /*    RegisterFragment regFragment = new RegisterFragment();
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("social_profile", mediaProfile);
                                    regFragment.setArguments(bundle);
                                    addFragment(regFragment, false, Const.REGISTER_FRAGMENT,
                                            true);
                                    log_layout.setVisibility(View.GONE);*/
                                    registerSocial(mediaProfile);

                                }

                            } else {
                                String error = job1.getString("error");
                                Commonutils.showtoast(error, this);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case Const.ServiceCode.REGISTER:
                Commonutils.progressdialog_hide();
                Log.d("mahi", "reg response" + response);
                if (response != null)
                    try {

                        JSONObject job1 = new JSONObject(response);
                        if (job1.getString("success").equals("true")) {


                            if (pcontent.isSuccessWithStoreId(response)) {
                                pcontent.parseUserAndStoreToDb(response);
                                if (null != social_dialog && social_dialog.isShowing()) {
                                    social_dialog.dismiss();
                                }
                                startActivity(new Intent(SignInActivity.this, MainActivity.class));
                                this.finish();

                            } else {

                            }

                        } else {

                            String error = job1.getString("error_messages");
                            Commonutils.showtoast(error, this);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                break;

        }
    }

    private void registerSocial(SocialMediaProfile mediaProfile) {

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.Params.URL, Const.ServiceType.REGISTER);
        map.put(Const.Params.FIRSTNAME, mediaProfile.getFirstName());
        map.put(Const.Params.LAST_NAME, mediaProfile.getLastName());
        map.put(Const.Params.EMAIL, mediaProfile.getEmailId());
        if (null != mediaProfile.getPictureUrl()) {
            map.put(Const.Params.PICTURE, mediaProfile.getPictureUrl());
        } else {
            map.put(Const.Params.PICTURE, "");
        }

        // map.put(Const.Params.SPECIALITY, String.valueOf(speclty.getId()));
        map.put(Const.Params.DEVICE_TOKEN,
                new PreferenceHelper(this).getDeviceToken());
        map.put(Const.Params.DEVICE_TYPE, Const.DEVICE_TYPE_ANDROID);

        map.put(Const.Params.LOGIN_BY, mediaProfile.getLoginType());
        map.put(Const.Params.SOCIAL_ID, mediaProfile.getSocialUniqueId());

        map.put(Const.Params.PHONE, "");

        map.put(Const.Params.CURRENCEY, "");
        map.put(Const.Params.TIMEZONE, TimeZone.getDefault().getID());
        map.put(Const.Params.COUNTRY, "");

        map.put(Const.Params.GENDER, "");


        Log.d("mahi", "social reg" + map.toString());
        if (null != mediaProfile.getPictureUrl()) {

            new MultiPartRequester(this, map, Const.ServiceCode.REGISTER,
                    this);
        } else {
            new VollyRequester(this, Const.POST, map, Const.ServiceCode.REGISTER,
                    this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LoginManager.getInstance().logOut();

    }

    @Override
    public void onStart() {
        super.onStart();

     /*   OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            //  Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            AndyUtils.showSimpleProgressDialog(SignInActivity.this, getResources().getString(R.string.txt_gmail), false);
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    AndyUtils.removeProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }*/
    }


    @Override
    public void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("mahi", "onConnectionFailed:" + connectionResult);
    }


}
