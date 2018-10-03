package com.alqaswa.user.Fragment;

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

import com.hbb20.CountryCodePicker;
import com.alqaswa.user.HttpRequester.AsyncTaskCompleteListener;
import com.alqaswa.user.HttpRequester.VollyRequester;
import com.alqaswa.user.R;
import com.alqaswa.user.Utils.AndyUtils;
import com.alqaswa.user.Utils.Const;
import com.alqaswa.user.WelcomeActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Mahesh on 7/5/2017.
 */

public class SignupFragment extends BaseRegFragment implements AsyncTaskCompleteListener {
    private EditText user_mobile_nuber;
    ImageView clear_edit, close_sign;
    CountryCodePicker ccp;
    TextView btn_confirm_phone;
    private TextInputLayout input_layout_phone;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signup_layout, container, false);
        user_mobile_nuber = (EditText) view.findViewById(R.id.user_mobile_nuber);
        close_sign = (ImageView) view.findViewById(R.id.close_sign);
        ccp = (CountryCodePicker) view.findViewById(R.id.ccp);

        btn_confirm_phone = (TextView) view.findViewById(R.id.btn_confirm_phone);
        //  rp_close_sign.setOnClickListener(this);
        input_layout_phone = (TextInputLayout) view.findViewById(R.id.input_layout_phone);

        user_mobile_nuber.requestFocus();
        close_sign.setOnClickListener(this);
        btn_confirm_phone.setOnClickListener(this);


        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close_sign:
                startActivity(new Intent(activity, WelcomeActivity.class));
                break;
            case R.id.btn_confirm_phone:
                if (TextUtils.isEmpty(user_mobile_nuber.getText().toString())) {
                    input_layout_phone.setError(getResources().getString(R.string.txt_phone_error));
                    user_mobile_nuber.requestFocus();
                } else {
                    input_layout_phone.setError(null);
                    getOTP();

                }
                break;

        }
    }


    private void getOTP() {
        if (!AndyUtils.isNetworkAvailable(activity)) {
            return;
        }

        AndyUtils.showSimpleProgressDialog(activity, "Requesting Otp...", false);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.Params.URL, Const.ServiceType.GET_OTP + "&" + Const.Params.PHONE + "=" + ccp.getSelectedCountryCode() + user_mobile_nuber.getText().toString());

        Log.d("mahi", map.toString());
        new VollyRequester(activity, Const.GET, map, Const.ServiceCode.GET_OTP,
                this);
    }

    @Override
    public void onTaskCompleted(String response, int serviceCode) {
        switch (serviceCode) {
            case Const.ServiceCode.GET_OTP:
                Log.d("mahi", "OTP Response" + response);
                AndyUtils.removeProgressDialog();
                try {
                    JSONObject job = new JSONObject(response);
                    if (job.getString("success").equals("true")) {
                        String code = job.optString("code");
                        Bundle mbundle = new Bundle();
                        OtpFragment otpFragment = new OtpFragment();
                        mbundle.putString("Phone", ccp.getSelectedCountryCode() + user_mobile_nuber.getText().toString());
                        mbundle.putString("code", code);
                        otpFragment.setArguments(mbundle);
                        activity.addFragment(otpFragment, false, "", true);
                    } else {
                        String error = job.optString("message");
                        AndyUtils.showShortToast(error, activity);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();


    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroy() {

        super.onDestroy();


    }
}
