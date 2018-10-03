package com.alqaswa.user.app;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.view.Menu;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.alqaswa.user.Utils.ForegroundListner;
import com.alqaswa.user.Utils.LruBitmapCache;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmConfiguration;


public class AppController extends Application{


    public static final String TAG = AppController.class.getSimpleName();
    private static double total = 0;
    public static ArrayList<Menu> menuListGlobal = new ArrayList<Menu>();

    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    LruBitmapCache mLruBitmapCache;

    private static AppController mInstance;

    public static double getTotal() {
        return total;
    }

    public static void setTotal(double total) {
        AppController.total = total;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this)
                .name(Realm.DEFAULT_REALM_NAME)
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
        ForegroundListner.init(this);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }


    public ImageLoader getImageLoader() {
        getRequestQueue();
        if (mImageLoader == null) {
            getLruBitmapCache();
            mImageLoader = new ImageLoader(this.mRequestQueue, mLruBitmapCache);
        }

        return this.mImageLoader;
    }

    public LruBitmapCache getLruBitmapCache() {
        if (mLruBitmapCache == null)
            mLruBitmapCache = new LruBitmapCache();
        return this.mLruBitmapCache;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);

    }


}

