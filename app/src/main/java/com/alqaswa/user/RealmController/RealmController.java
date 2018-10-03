package com.alqaswa.user.RealmController;

import android.app.Activity;
import android.app.Application;
import android.support.v4.app.Fragment;

import com.alqaswa.user.Models.User;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by user on 8/22/2016.
 */
public class
RealmController {
    private static RealmController instance;
    private final Realm realm;

    public RealmController(Application application) {
        realm = Realm.getDefaultInstance();
    }

    public static RealmController with(Fragment fragment) {

        if (instance == null) {
            instance = new RealmController(fragment.getActivity().getApplication());
        }
        return instance;
    }

    public static RealmController with(Activity activity) {

        if (instance == null) {
            instance = new RealmController(activity.getApplication());
        }
        return instance;
    }

    public static RealmController with(Application application) {

        if (instance == null) {
            instance = new RealmController(application);
        }
        return instance;
    }

    public static RealmController getInstance() {


        return instance;
    }

    public Realm getRealm() {

        return realm;
    }

    //Refresh the realm istance
    public void refresh() {

        realm.refresh();
    }

    public void clearAll() {

        realm.beginTransaction();
        realm.clear(User.class);
        realm.commitTransaction();
    }






    public RealmResults<User> getusers() {

        return realm.where(User.class).findAll();
    }


    public User getUser(int id) {

        return realm.where(User.class).equalTo("id", id).findFirst();
    }
}