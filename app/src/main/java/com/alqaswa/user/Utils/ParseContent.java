package com.alqaswa.user.Utils;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import com.alqaswa.user.Models.AdsList;
import com.alqaswa.user.Models.RequestDetail;
import com.alqaswa.user.Models.User;
import com.alqaswa.user.RealmController.RealmController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

/**
 * Created by user on 8/22/2016.
 */
public class ParseContent {
    private Activity activity;
    private Realm mRealm;
    private PreferenceHelper preferenceHelper;
    private final String KEY_SUCCESS = "success";
    private final String KEY_ERROR = "error";
    private final String KEY_ERROR_CODE = "error_code";
    public static final String IS_CANCELLED = "is_cancelled";

    public ParseContent(Activity activity) {
        // TODO Auto-generated constructor stub
        this.activity = activity;
        preferenceHelper = new PreferenceHelper(activity);
    }


    public boolean isSuccessWithStoreId(String response) {

        if (TextUtils.isEmpty(response))
            return false;
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getBoolean(KEY_SUCCESS)) {
                preferenceHelper.putUserId(jsonObject
                        .getString(Const.Params.ID));
                preferenceHelper.putSessionToken(jsonObject
                        .getString(Const.Params.TOKEN));
                preferenceHelper.putUser_name(jsonObject
                        .getString(Const.Params.FIRSTNAME));
                preferenceHelper.putEmail(jsonObject
                        .optString(Const.Params.EMAIL));
                preferenceHelper.putPicture(jsonObject
                        .optString(Const.Params.PICTURE));
                preferenceHelper.putLoginBy(jsonObject
                        .getString(Const.Params.LOGIN_BY));
                if (!preferenceHelper.getLoginBy().equalsIgnoreCase(
                        Const.MANUAL)) {
                    preferenceHelper.putSocialId(jsonObject
                            .getString(Const.Params.SOCIAL_ID));
                }
                if(jsonObject.has("payment_mode_status")){
                    preferenceHelper.putPaymentMode(jsonObject
                            .getString("payment_mode_status"));
                }

                return true;
            } else {

                return false;

            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    public User parseUserAndStoreToDb(String response) {
        User user = null;
        try {
            JSONObject jsonObject = new JSONObject(response);

            if (jsonObject.getBoolean(KEY_SUCCESS)) {
                user = new User();
                mRealm = Realm.getInstance(activity);
                RealmController.with(activity).clearAll();
                user.setId(jsonObject.getInt(Const.Params.ID));
                user.setEmail(jsonObject.optString(Const.Params.EMAIL));
                user.setFname(jsonObject.getString(Const.Params.FIRSTNAME));
                user.setLname(jsonObject.getString(Const.Params.LAST_NAME));
                user.setProfileurl(jsonObject.getString(Const.Params.PICTURE));
                if(jsonObject.has("wallet_bay_key")) {
                    preferenceHelper.putWallet_key(jsonObject.optString("wallet_bay_key"));
                }

                user.setPhone(jsonObject.getString(Const.Params.PHONE));

                if (jsonObject.has(Const.Params.CURRENCEY)) {
                    user.setCurrency(jsonObject.getString(Const.Params.CURRENCEY));
                    preferenceHelper.putCurrency(jsonObject.getString(Const.Params.CURRENCEY));
                }

                user.setGender(jsonObject.getString(Const.Params.GENDER));
                if (jsonObject.has(Const.Params.COUNTRY)) {
                    user.setCountry(jsonObject.getString(Const.Params.COUNTRY));
                }


                mRealm.beginTransaction();
                mRealm.copyToRealm(user);
                mRealm.commitTransaction();


            } else {
                // AndyUtils.showToast(jsonObject.getString(KEY_ERROR),
                // activity);

            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return user;
    }


    public RequestDetail parseRequestStatus(String response) {

        if (TextUtils.isEmpty(response)) {
            return null;
        }
        RequestDetail requestDetail = new RequestDetail();
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getBoolean(KEY_SUCCESS)) {
                requestDetail.setCurrnecy_unit(jsonObject.optString("currency"));
                requestDetail.setCancellationFee(jsonObject.optString("cancellation_fine"));
                JSONArray jarray = jsonObject.getJSONArray("data");
                if (jarray.length() > 0) {
                    JSONObject driverdata = jarray.getJSONObject(0);
                    if (driverdata != null) {

                        if (driverdata.has("provider_status")) {
                            requestDetail.setTripStatus(driverdata.getInt("provider_status"));
                        }
                        new PreferenceHelper(activity).putRequestId(Integer.valueOf(driverdata.getString("request_id")));
                        requestDetail.setRequestId(Integer.valueOf(driverdata.getString("request_id")));
                        if (!driverdata.getString("provider_name").equals("null")) {
                            requestDetail.setDriver_name(driverdata
                                    .getString("provider_name"));
                        }
                        if (driverdata.has("status")) {
                            requestDetail.setDriverStatus(driverdata.getInt("status"));

                        }
                        if (!driverdata.getString("provider_mobile").equals("null")) {
                            requestDetail.setDriver_mobile(driverdata
                                    .getString("provider_mobile"));
                        }
                        if (!driverdata.getString("provider_picture").equals("null")) {
                            requestDetail.setDriver_picture(driverdata
                                    .getString("provider_picture"));
                        }
                        if (!driverdata.optString("car_image").equals("null")) {
                            requestDetail.setDriver_car_picture(driverdata
                                    .getString("car_image"));
                        }
                        if (!driverdata.optString("model").equals("null")) {
                            requestDetail.setDriver_car_model(driverdata
                                    .getString("model"));
                        }
                        if (!driverdata.getString("color").equals("null")) {
                            requestDetail.setDriver_car_color(driverdata
                                    .getString("color"));
                        }
                        if (!driverdata.getString("plate_no").equals("null")) {
                            requestDetail.setDriver_car_number(driverdata
                                    .getString("plate_no"));
                        }
                        requestDetail.setRequest_type(driverdata.optString("request_status_type"));
                        requestDetail.setNo_tolls(jsonObject.optString("number_tolls"));
                        requestDetail.setDriver_id(driverdata
                                .getString("provider_id"));
                        new PreferenceHelper(activity).putDriver_id(driverdata
                                .getString("provider_id"));
                        requestDetail.setDriver_rating(driverdata
                                .getString("rating"));
                        requestDetail.setS_address(driverdata
                                .getString("s_address"));
                        requestDetail.setD_address(driverdata
                                .getString("d_address"));
                        requestDetail.setS_lat(driverdata
                                .getString("s_latitude"));
                        requestDetail.setS_lon(driverdata
                                .getString("s_longitude"));
                        requestDetail.setD_lat(driverdata
                                .getString("d_latitude"));
                        requestDetail.setD_lon(driverdata
                                .getString("d_longitude"));
                        if (!driverdata.getString("driver_latitude").equals("null")) {
                            requestDetail.setDriver_latitude(Double.valueOf(driverdata
                                    .getString("driver_latitude")));
                        }
                        if (!driverdata.getString("driver_longitude").equals("null")) {
                            requestDetail.setDriver_longitude(Double.valueOf(driverdata
                                    .getString("driver_longitude")));
                        }
                        requestDetail.setVehical_img(driverdata
                                .getString("type_picture"));

                    }
                    JSONArray invoicejarray = jsonObject.getJSONArray("invoice");
                    if (invoicejarray.length() > 0) {
                        JSONObject invoiceobj = invoicejarray.getJSONObject(0);
                        requestDetail.setTrip_time(invoiceobj.getString("total_time"));
                        requestDetail.setPayment_mode(invoiceobj.getString("payment_mode"));
                        requestDetail.setTrip_base_price(invoiceobj.getString("base_price"));
                        requestDetail.setTrip_total_price(invoiceobj.getString("total"));
                        requestDetail.setDistance_unit(invoiceobj.optString("distance_unit"));


                        requestDetail.setTrip_distance(invoiceobj
                                .getString("distance_travel"));
                    }

                } else {


                    requestDetail.setTripStatus(Const.NO_REQUEST);

                    new PreferenceHelper(activity).putRequestId(Const.NO_REQUEST);

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return requestDetail;
    }

    public List<AdsList> parseAdsList(JSONArray jsonArray) {
        List<AdsList> adsLists = null;
        Log.e("asher", "region array " + jsonArray);

        adsLists = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            //Creating a json object of the current index

            try {
                //getting json object from current index
                JSONObject obj = jsonArray.getJSONObject(i);
                //getting subCategories from json object
                AdsList details = new AdsList();
                details.setAdDescription(obj.optString("description"));
                details.setAdId(obj.optString("id"));
                details.setAdImage(obj.optString("picture"));
                details.setAdUrl(obj.optString("url"));
                adsLists.add(details);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //Creating ListViewAdapter Object


        return adsLists;
    }

}
