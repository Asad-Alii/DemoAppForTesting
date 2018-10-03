package com.alqaswa.user.Models;

/**
 * Created by getit on 8/11/2016.
 */
public class UserSettings
{
    private int userSettingsIcon;
    private String userSettingsText;

    public UserSettings(int userSettingsIcon, String userSettingsText) {
        this.userSettingsIcon = userSettingsIcon;
        this.userSettingsText = userSettingsText;
    }

    public String getUserSettingsText() {
        return userSettingsText;
    }

    public void setUserSettingsText(String userSettingsText) {
        this.userSettingsText = userSettingsText;
    }

    public int getUserSettingsIcon() {
        return userSettingsIcon;
    }

    public void setUserSettingsIcon(int userSettingsIcon) {
        this.userSettingsIcon = userSettingsIcon;
    }




}
