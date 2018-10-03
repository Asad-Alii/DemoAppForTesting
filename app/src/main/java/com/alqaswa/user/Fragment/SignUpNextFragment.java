package com.alqaswa.user.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.braintreepayments.api.BraintreePaymentActivity;
import com.braintreepayments.api.PaymentRequest;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.alqaswa.user.HttpRequester.AsyncTaskCompleteListener;
import com.alqaswa.user.HttpRequester.VollyRequester;
import com.alqaswa.user.MainActivity;
import com.alqaswa.user.R;
import com.alqaswa.user.Utils.AndyUtils;
import com.alqaswa.user.Utils.Commonutils;
import com.alqaswa.user.Utils.Const;
import com.alqaswa.user.Utils.ParseContent;
import com.alqaswa.user.Utils.PreferenceHelper;
import com.alqaswa.user.WelcomeActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.TimeZone;

/**
 * Created by Mahesh on 7/5/2017.
 */

public class SignUpNextFragment extends BaseRegFragment implements AsyncTaskCompleteListener {

    private EditText user_fname, user_lname, user_email, user_password, user_referral_code;
    int i = 0;
    private TextView btn_next;
    private TextInputLayout input_layout_fname, input_layout_lname, input_layout_email, input_layout_pass, input_layout_referral_code;
    private static final int REQUEST_CODE = 133;
    private String mobile = "";
    private ImageView close_sign;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signup2, container, false);
        user_fname = (EditText) view.findViewById(R.id.user_fname);
        user_lname = (EditText) view.findViewById(R.id.user_lname);
        user_email = (EditText) view.findViewById(R.id.user_email);
        user_referral_code = (EditText) view.findViewById(R.id.user_referral_code);
        btn_next = (TextView) view.findViewById(R.id.btn_next);
        user_password = (EditText) view.findViewById(R.id.user_password);
        close_sign = (ImageView) view.findViewById(R.id.close_sign);
        input_layout_fname = (TextInputLayout) view.findViewById(R.id.input_layout_fname);
        input_layout_lname = (TextInputLayout) view.findViewById(R.id.input_layout_lname);
        input_layout_email = (TextInputLayout) view.findViewById(R.id.input_layout_email);
        input_layout_pass = (TextInputLayout) view.findViewById(R.id.input_layout_pass);
        input_layout_referral_code = (TextInputLayout) view.findViewById(R.id.input_layout_referral_code);

        close_sign.setOnClickListener(this);
        btn_next.setOnClickListener(this);


        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle nbundel = new Bundle();
        nbundel = getArguments();
        if (nbundel != null) {
            mobile = nbundel.getString("mobile");
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.close_sign:
                startActivity(new Intent(activity, WelcomeActivity.class));
                break;
            case R.id.btn_next:
                switch (i) {
                    case 0:
                        if (isvalid()) {
                            i++;
                            input_layout_fname.setVisibility(View.GONE);
                            input_layout_lname.setVisibility(View.GONE);
                            input_layout_referral_code.setVisibility(View.GONE);
                            input_layout_email.setVisibility(View.VISIBLE);
                            user_email.setVisibility(View.VISIBLE);

                        }
                        break;
                    case 1:
                        if (isEmailvalid()) {
                            i++;
                            input_layout_email.setVisibility(View.GONE);
                            input_layout_pass.setVisibility(View.VISIBLE);
                            input_layout_referral_code.setVisibility(View.VISIBLE);
                            user_password.setVisibility(View.VISIBLE);
                            user_referral_code.setVisibility(View.VISIBLE);
                            btn_next.setText(getResources().getString(R.string.txt_finish));
                        }
                        break;
                    case 2:
                        if (TextUtils.isEmpty(user_password.getText().toString())) {
                            input_layout_pass.setError(getResources().getString(R.string.txt_pass_error));
                            user_password.requestFocus();

                        } else {
                            i++;
                            input_layout_pass.setVisibility(View.GONE);
                            input_layout_referral_code.setVisibility(View.GONE);
                            registerManual();

                        }
                        break;

                }
                break;
        }
    }


    private void registerManual() {

        Commonutils.progressdialog_show(activity, getResources().getString(R.string.reg_load));

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.Params.URL, Const.ServiceType.REGISTER);
        map.put(Const.Params.FIRSTNAME, user_fname.getText().toString());
        map.put(Const.Params.LAST_NAME, user_lname.getText().toString());
        map.put(Const.Params.EMAIL, user_email.getText().toString());
        map.put(Const.Params.PASSWORD, user_password.getText().toString());
        map.put(Const.Params.PHONE, mobile);
        map.put(Const.Params.CURRENCEY, "1");
        // map.put(Const.Params.SPECIALITY, String.valueOf(speclty.getId()));
        map.put(Const.Params.DEVICE_TOKEN,
                new PreferenceHelper(activity).getDeviceToken());
        map.put(Const.Params.DEVICE_TYPE, Const.DEVICE_TYPE_ANDROID);

        map.put(Const.Params.LOGIN_BY, Const.MANUAL);
        /*String[] items1 = sp_code.getSelectedItem().toString().split(" ");
        String country = items1[0];
        String code = items1[1];
        */

        //
        map.put(Const.Params.TIMEZONE, TimeZone.getDefault().getID());
        map.put(Const.Params.REFERRAL_CODE, user_referral_code.getText().toString());
        //map.put(Const.Params.COUNTRY, sp_country_reg.getSelectedItem().toString());

        Log.d("mahi", map.toString());
        new VollyRequester(activity, Const.POST, map, Const.ServiceCode.REGISTER,
                this);

    }

    private boolean isvalid() {
        if (TextUtils.isEmpty(user_fname.getText().toString())) {
            input_layout_fname.setError(getResources().getString(R.string.txt_fname_error));
            user_fname.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(user_lname.getText().toString())) {
            input_layout_lname.setError(getResources().getString(R.string.txt_lname_error));
            user_lname.requestFocus();
            return false;
        } else {
            input_layout_fname.setError(null);
            input_layout_lname.setError(null);
            return true;
        }
    }

    private boolean isEmailvalid() {
        if (TextUtils.isEmpty(user_email.getText().toString())) {
            input_layout_email.setError(getResources().getString(R.string.txt_email_error));
            user_email.requestFocus();
            return false;
        } else if (!AndyUtils.eMailValidation(user_email.getText().toString())) {
            input_layout_email.setError(getResources().getString(R.string.txt_incorrect_error));
            user_email.requestFocus();
            return false;
        } else {
            input_layout_email.setError(null);
            return true;
        }
    }


    @Override
    public void onTaskCompleted(String response, int serviceCode) {
        switch (serviceCode) {

            case Const.ServiceCode.REGISTER:

                Log.d("mahi", "reg response" + response);
                if (response != null)
                    try {

                        JSONObject job1 = new JSONObject(response);
                        if (job1.getString("success").equals("true")) {
                            if (new ParseContent(activity).isSuccessWithStoreId(response)) {
                                new ParseContent(activity).parseUserAndStoreToDb(response);
                                new PreferenceHelper(activity).putPassword(user_password.getText()
                                        .toString());
                                Commonutils.progressdialog_hide();
                                // getBrainTreeClientToken();
                                startActivity(new Intent(activity, MainActivity.class));
                                activity.finish();


                            } else {

                            }

                        } else {
                            Commonutils.progressdialog_hide();
                            if (job1.has("error_code")) {
                                if (job1.optString("error_code").equals("168")) {
                                    i = 2;
                                    input_layout_email.setVisibility(View.GONE);
                                    input_layout_pass.setVisibility(View.VISIBLE);
                                    input_layout_referral_code.setVisibility(View.VISIBLE);
                                    user_password.setVisibility(View.VISIBLE);
                                    user_referral_code.setVisibility(View.VISIBLE);
                                    btn_next.setText(getResources().getString(R.string.txt_finish));
                                } else {
                                    i = 1;
                                    input_layout_fname.setVisibility(View.GONE);
                                    input_layout_lname.setVisibility(View.GONE);
                                    input_layout_email.setVisibility(View.VISIBLE);
                                    user_email.setVisibility(View.VISIBLE);
                                    btn_next.setText(getResources().getString(R.string.txt_next));
                                }
                            }
                            
                            if (job1.has("error_messages")) {
                                String error = job1.getString("error_messages");
                                Commonutils.showtoast(error, activity);
                            } else {
                                String error = job1.getString("error");
                                Commonutils.showtoast(error, activity);
                            }

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                break;
            case Const.ServiceCode.CREATE_ADD_CARD_URL:
                AndyUtils.appLog("Ashutosh", "BrainTreeClientTokenResponse" + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getString("success").equals("true")) {
                        AndyUtils.showShortToast(getResources().getString(R.string.txt_card_success), activity);
                        Commonutils.progressdialog_hide();
                        startActivity(new Intent(activity, MainActivity.class));
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case Const.ServiceCode.GET_BRAIN_TREE_TOKEN_URL:
                AndyUtils.appLog("mahi", "BrainTreeClientTokenResponse" + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getString("success").equals("true")) {
                        String clientToken = jsonObject.optString("client_token");

                        PaymentRequest paymentRequest = new PaymentRequest()
                                .clientToken(clientToken)
                                .disablePayPal()
                                .secondaryDescription(getResources().getString(R.string.txt_braintree1))
                                .primaryDescription(getResources().getString(R.string.txt_braintree2))
                                .submitButtonText(getResources().getString(R.string.btn_add_card));
                        startActivityForResult(paymentRequest.getIntent(activity), REQUEST_CODE);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;


            default:
                break;
        }
    }

    private void getBrainTreeClientToken() {
        if (!AndyUtils.isNetworkAvailable(activity)) {

            return;
        }

        HashMap<String, String> map = new HashMap<>();
        map.put(Const.Params.URL, Const.ServiceType.GET_BRAIN_TREE_TOKEN_URL);
        map.put(Const.Params.ID, new PreferenceHelper(activity).getUserId());
        map.put(Const.Params.TOKEN, new PreferenceHelper(activity).getSessionToken());

        AndyUtils.appLog("mahi", "BrainTreeClientTokenMap" + map);

        new VollyRequester(activity, Const.POST, map, Const.ServiceCode.GET_BRAIN_TREE_TOKEN_URL, this);

    }

    void postNonceToServer(String nonce) {
        if (!AndyUtils.isNetworkAvailable(activity)) {

            return;
        }
        Commonutils.progressdialog_show(activity, "Adding Card...");
        HashMap<String, String> map = new HashMap<>();
        map.put(Const.Params.URL, Const.ServiceType.CREATE_ADD_CARD_URL);
        map.put(Const.Params.ID, new PreferenceHelper(activity).getUserId());
        map.put(Const.Params.TOKEN, new PreferenceHelper(activity).getSessionToken());
        map.put(Const.Params.PAYMENT_METHOD_NONCE, nonce);

        AndyUtils.appLog("mahi", "BrainTreeADDCARDMap" + map);

        new VollyRequester(activity, Const.POST, map, Const.ServiceCode.CREATE_ADD_CARD_URL, this);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentMethodNonce paymentMethodNonce = data.getParcelableExtra(
                        BraintreePaymentActivity.EXTRA_PAYMENT_METHOD_NONCE
                );

                String nonce = paymentMethodNonce.getNonce();
                // Log.d("mahi","none value"+nonce);
                // Send the nonce to your server.
                postNonceToServer(nonce);
            } else {
                // handle errors here, an exception may be available in
                Exception error = null;
                try {
                    error = (Exception) data.getSerializableExtra(BraintreePaymentActivity.EXTRA_ERROR_MESSAGE);
                    Log.d("mahi", "error message" + error);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }
}
