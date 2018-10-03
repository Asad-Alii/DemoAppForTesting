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

public class OtpFragment extends BaseRegFragment implements AsyncTaskCompleteListener {
    private EditText et_otp_mobile, user_otp;
    private String code = "";
    private TextInputLayout input_layout_otp;
    private ImageView close_sign, btn_edit_number;
    private TextView btn_resend, btn_confirm_otp;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.otp_verifier, container, false);

        et_otp_mobile = (EditText) view.findViewById(R.id.et_otp_mobile);
        user_otp = (EditText) view.findViewById(R.id.user_otp);
        input_layout_otp = (TextInputLayout) view.findViewById(R.id.input_layout_otp);
        close_sign = (ImageView) view.findViewById(R.id.close_sign);
        btn_resend = (TextView) view.findViewById(R.id.btn_resend);
        btn_confirm_otp = (TextView) view.findViewById(R.id.btn_confirm_otp);
        btn_edit_number = (ImageView) view.findViewById(R.id.btn_edit_number);

        close_sign.setOnClickListener(this);
        btn_edit_number.setOnClickListener(this);
        btn_confirm_otp.setOnClickListener(this);
        btn_resend.setOnClickListener(this);


        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = new Bundle();
        bundle = getArguments();
        if (bundle != null) {
            String mobile = bundle.getString("Phone");
            code = bundle.getString("code");
            et_otp_mobile.setText(mobile);

            user_otp.setText(code);
        }

    }

    private boolean isvalid() {
        if (TextUtils.isEmpty(user_otp.getText().toString())) {
            input_layout_otp.setError(getResources().getString(R.string.txt_otp_error));
            user_otp.requestFocus();
            return false;
        } else if (!user_otp.getText().toString().equals(code)) {
            input_layout_otp.setError(getResources().getString(R.string.txt_otp_wrong));
            user_otp.requestFocus();
            return false;
        } else {
            input_layout_otp.setError(null);
            return true;
        }
    }


    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.close_sign:
                startActivity(new Intent(activity, WelcomeActivity.class));
                break;
            case R.id.btn_edit_number:
                et_otp_mobile.setEnabled(true);
                et_otp_mobile.requestFocus();
                break;
            case R.id.btn_confirm_otp:
                if (isvalid()) {
                    Bundle mbundle = new Bundle();
                    SignUpNextFragment signupFragment = new SignUpNextFragment();
                    mbundle.putString("mobile", et_otp_mobile.getText().toString());
                    signupFragment.setArguments(mbundle);
                    activity.addFragment(signupFragment, false, "", true);
                }
                break;
            case R.id.btn_resend:
                getOTP();
                break;
        }
    }

    private void getOTP() {
        if (!AndyUtils.isNetworkAvailable(activity)) {
            return;
        }

        AndyUtils.showSimpleProgressDialog(activity, "Requesting Otp...", false);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.Params.URL, Const.ServiceType.GET_OTP + "&" + Const.Params.PHONE + "=" + et_otp_mobile.getText().toString());

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
                        code = job.optString("code");
                        user_otp.setText(code);
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
    public void onDestroy() {

        super.onDestroy();

    }
}
