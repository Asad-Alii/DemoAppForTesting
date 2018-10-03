package com.alqaswa.user;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.alqaswa.user.HttpRequester.AsyncTaskCompleteListener;
import com.alqaswa.user.HttpRequester.MultiPartRequester;
import com.alqaswa.user.HttpRequester.VollyRequester;
import com.alqaswa.user.Models.User;
import com.alqaswa.user.RealmController.RealmController;
import com.alqaswa.user.Utils.Commonutils;
import com.alqaswa.user.Utils.Const;
import com.alqaswa.user.Utils.ParseContent;
import com.alqaswa.user.Utils.PreferenceHelper;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.bumptech.glide.Glide;
import com.soundcloud.android.crop.Crop;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import io.realm.Realm;

/**
 * Created by user on 1/7/2017.
 */

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener, AsyncTaskCompleteListener {
    private Toolbar promainToolbar;
    private ImageButton profile_back;
    private TextView btn_edit_profile;
    private ImageView profile_image;
    private EditText et_firstname, et_lastname, et_profile_email, et_profile_mobile;
    private RadioGroup profile_radioGroup;
    private RadioButton rd_btn, radio_btn_male, radio_btn_female;
    private Realm realm;
    private AQuery aQuery;
    private String filePath = "";
    private File cameraFile;
    private Uri uri = null;
    private ParseContent pcontent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        aQuery = new AQuery(this);
        pcontent = new ParseContent(this);
        this.realm = RealmController.with(this).getRealm();
        RealmController.with(this).refresh();
        setContentView(R.layout.activity_profile);
        promainToolbar = (Toolbar) findViewById(R.id.toolbar_profile);

        setSupportActionBar(promainToolbar);
        getSupportActionBar().setTitle(null);

        profile_back = (ImageButton) findViewById(R.id.profile_back);
        btn_edit_profile = (TextView) findViewById(R.id.btn_edit_profile);
        profile_image = (ImageView) findViewById(R.id.profile_image);
        profile_radioGroup = (RadioGroup) findViewById(R.id.profile_radioGroup);

        et_firstname = (EditText) findViewById(R.id.et_firstname);
        et_lastname = (EditText) findViewById(R.id.et_lastname);
        et_profile_email = (EditText) findViewById(R.id.et_profile_email);
        et_profile_mobile = (EditText) findViewById(R.id.et_profile_mobile);
        radio_btn_male = (RadioButton) findViewById(R.id.radio_btn_male);
        radio_btn_female = (RadioButton) findViewById(R.id.radio_btn_female);
        profile_image.setOnClickListener(this);
        profile_back.setOnClickListener(this);
        btn_edit_profile.setOnClickListener(this);

        disableviews();
        setValues();

    }

    private void setValues() {
        User userprofile = RealmController.with(this).getUser(Integer.valueOf(new PreferenceHelper(this).getUserId()));
        if (userprofile != null) {
            et_firstname.setText(userprofile.getFname());
            et_lastname.setText(userprofile.getLname());
            et_profile_email.setText(userprofile.getEmail());
            et_profile_mobile.setText(userprofile.getPhone());
            Glide.with(this).load(userprofile.getProfileurl())
                    .error(R.drawable.defult_user)
                    .centerCrop()
                    .crossFade()
                    .placeholder(R.drawable.defult_user).into(profile_image);
            if (userprofile.getGender().equals(getString(R.string.txt_male))) {
                radio_btn_male.setChecked(true);

            } else {
                radio_btn_female.setChecked(true);

            }

            new AQuery(this).id(R.id.profile_image).image(userprofile.getProfileurl(), true, true,
                    200, 0, new BitmapAjaxCallback() {

                        @Override
                        public void callback(String url, ImageView iv, Bitmap bm,
                                             AjaxStatus status) {

                            if (url != null && !url.equals("")) {
                                filePath = aQuery.getCachedFile(url).getPath();


                            }

                        }

                    });

        }
    }

    private void disableviews() {
        profile_image.setEnabled(false);
        et_firstname.setEnabled(false);
        et_lastname.setEnabled(false);
        et_profile_email.setEnabled(false);
        et_profile_mobile.setEnabled(false);
        radio_btn_female.setEnabled(false);
        radio_btn_male.setEnabled(false);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.profile_back:
                onBackPressed();
                break;
            case R.id.btn_edit_profile:
                if (btn_edit_profile.getText().toString().equals(getString(R.string.btn_edit))) {
                    enableViews();
                    btn_edit_profile.setText(getString(R.string.btn_save));
                } else {
                    disableviews();
                    updateprofile();
                }
                break;
            case R.id.profile_image:
                showPictureDialog();
                break;
        }
    }

    private void updateprofile() {
        Commonutils.progressdialog_show(this, getResources().getString(R.string.updating_pro_load));

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.Params.URL, Const.ServiceType.UPDATE_PROFILE);
        map.put(Const.Params.FIRSTNAME, et_firstname.getText().toString());
        map.put(Const.Params.LAST_NAME, et_lastname.getText().toString());
        map.put(Const.Params.EMAIL, et_profile_email.getText().toString());
        map.put(Const.Params.ID, new PreferenceHelper(this).getUserId());
        map.put(Const.Params.TOKEN, new PreferenceHelper(this).getSessionToken());
        // map.put(Const.Params.PASSWORD, et_register_your_password.getText().toString());
        map.put(Const.Params.PICTURE, filePath);

        // map.put(Const.Params.SPECIALITY, String.valueOf(speclty.getId()));
        map.put(Const.Params.DEVICE_TOKEN,
                new PreferenceHelper(this).getDeviceToken());
        map.put(Const.Params.DEVICE_TYPE, Const.DEVICE_TYPE_ANDROID);

        map.put(Const.Params.LOGIN_BY, Const.MANUAL);

        map.put(Const.Params.PHONE, et_profile_mobile.getText().toString());

        // map.put(Const.Params.CURRENCEY, sp_curency_reg.getSelectedItem().toString());
        map.put(Const.Params.TIMEZONE, TimeZone.getDefault().getID());
        // map.put(Const.Params.COUNTRY, sp_country_reg.getSelectedItem().toString());

        int selectedId = profile_radioGroup.getCheckedRadioButtonId();
        rd_btn = (RadioButton) findViewById(selectedId);
        map.put(Const.Params.GENDER, rd_btn.getText().toString());
        Log.d("mahi", map.toString());
        if(filePath.equals("")||null==filePath){
            new VollyRequester(this,Const.POST, map, Const.ServiceCode.UPDATE_PROFILE,
                    this);
        } else {
            new MultiPartRequester(this, map, Const.ServiceCode.UPDATE_PROFILE,
                    this);
        }

    }

    private void showPictureDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getResources().getString(R.string.txt_slct_option));
        String[] items = {getResources().getString(R.string.txt_gellery), getResources().getString(R.string.txt_cameray)};
       // dialog.setMessage("*for your security reason we blocked!");
        dialog.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                switch (which) {
                    case 0:
                        choosePhotoFromGallary();
                        break;
                    case 1:
                        takePhotoFromCamera();
                        break;

                }
            }
        });
        dialog.show();

       /* final Dialog dialog = new Dialog(this, R.style.DialogThemeforview);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.fade_drawable));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.choose_picture_dialog);
        TextView btn_ok = (TextView) dialog.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();*/
    }

    private void choosePhotoFromGallary() {
        try {
            Intent i = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            startActivityForResult(i, Const.CHOOSE_PHOTO);
        } catch (Exception e) {
            e.printStackTrace();
            Commonutils.showtoast("Gallery not found!",this);
        }

    }

    private void takePhotoFromCamera() {
        Calendar cal = Calendar.getInstance();
        cameraFile = new File(Environment.getExternalStorageDirectory(),
                (cal.getTimeInMillis() + ".jpg"));


        if (!cameraFile.exists()) {
            try {
                cameraFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {

            cameraFile.delete();
            try {
                cameraFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        uri = Uri.fromFile(cameraFile);
        Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        i.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(i, Const.TAKE_PHOTO);
    }

    private void enableViews() {
        profile_image.setEnabled(true);
        et_firstname.setEnabled(true);
        et_lastname.setEnabled(true);
        et_profile_email.setEnabled(true);
        et_profile_mobile.setEnabled(true);
        radio_btn_female.setEnabled(true);
        radio_btn_male.setEnabled(true);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("mahi", "req code" + requestCode);

        switch (requestCode) {

            case Const.CHOOSE_PHOTO:
                if (data != null) {

                    uri = data.getData();
                    if (uri != null) {

                        beginCrop(uri);

                    } else {
                        Toast.makeText(this, getResources().getString(R.string.txt_img_error),
                                Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case Const.TAKE_PHOTO:


                if (uri != null) {
                    beginCrop(uri);
                } else {
                    Toast.makeText(this, getResources().getString(R.string.txt_img_error),
                            Toast.LENGTH_LONG).show();
                }

                break;
            case Crop.REQUEST_CROP:


                if (data != null)
                    handleCrop(resultCode, data);

                break;
        }
    }

    private void beginCrop(Uri source) {

        Uri outputUri = Uri.fromFile(new File(Environment
                .getExternalStorageDirectory(), (Calendar.getInstance()
                .getTimeInMillis() + ".jpg")));
        Crop.of(source, outputUri).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {

            filePath = getRealPathFromURI(Crop.getOutput(result));

            //.setImageURI(Crop.getOutput(result));
            Glide.with(this).load(filePath)
                    .error(R.drawable.defult_user)
                    .centerCrop()
                    .crossFade()
                    .placeholder(R.drawable.defult_user).into(profile_image);

        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null,
                null, null, null);

        if (cursor == null) { // Source is Dropbox or other similar local file
            // path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor
                    .getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    @Override
    public void onTaskCompleted(String response, int serviceCode) {
        switch (serviceCode) {

            case Const.ServiceCode.UPDATE_PROFILE:
                Log.d("mahi", "profile response" + response);

                if (response != null) {
                    try {

                        JSONObject job1 = new JSONObject(response);
                        if (job1.getString("success").equals("true")) {
                            Commonutils.progressdialog_hide();
                            try {
                                if (!filePath.equals("")) {

                                    File file = new File(filePath);
                                    file.getAbsoluteFile().delete();

                                }
                                if (cameraFile != null) {
                                    cameraFile.getAbsoluteFile().delete();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (pcontent.isSuccessWithStoreId(response)) {
                                pcontent.parseUserAndStoreToDb(response);
                                Commonutils.showtoast(getString(R.string.update_success_text), this);
                                btn_edit_profile.setText(getString(R.string.btn_edit));

                            } else {
                                Commonutils.progressdialog_hide();
                            }

                        } else {

                            String error = job1.getString("error_messages");
                            Commonutils.showtoast(error, this);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }


                break;


        }
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(this,MainActivity.class);
        startActivity(i);
    }
}
