package com.essam.chatapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.essam.chatapp.models.Profile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class SharedPrefrence {
    public static SharedPreferences myPrefs;
    public static SharedPreferences.Editor prefsEditor;

    public static SharedPrefrence myObj;

    private SharedPrefrence() {

    }

    public void clearAllPreferences() {
        prefsEditor = myPrefs.edit();
        prefsEditor.clear();
        prefsEditor.commit();
    }


    public static SharedPrefrence getInstance(Context ctx) {
        if (myObj == null) {
            myObj = new SharedPrefrence();
            myPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
            prefsEditor = myPrefs.edit();
        }
        return myObj;
    }

    public void clearPreferences(String key) {
        prefsEditor.remove(key);
        prefsEditor.commit();
    }

    public void setMyProfile(Profile profile){
        setValue(Consts.USER_UID, profile.getId());
        setValue(Consts.USER_NAME, profile.getUserName());
        setValue(Consts.PHONE, profile.getPhone());
        setValue(Consts.AVATAR, profile.getAvatar());
        setValue(Consts.STATUS, profile.getStatus());
        setValue(Consts.FCM_TOKEN, profile.getToken());
    }

    public Profile getMyProfile(){
        return new Profile(
                getValue(Consts.USER_UID),
                getValue(Consts.USER_NAME),
                getValue(Consts.PHONE),
                getValue(Consts.AVATAR),
                getValue(Consts.STATUS),
                true,
                getValue(Consts.FCM_TOKEN));
    }

    public void setIntValue(String Tag, int value) {
        prefsEditor.putInt(Tag, value);
        prefsEditor.apply();
    }

    public int getIntValue(String Tag) {
        return myPrefs.getInt(Tag, 0);
    }

    public void setLongValue(String Tag, long value) {
        prefsEditor.putLong(Tag, value);
        prefsEditor.apply();
    }

    public long getLongValue(String Tag) {
        return myPrefs.getLong(Tag, 0);
    }


    public void setValue(String Tag, String token) {
        prefsEditor.putString(Tag, token);
        prefsEditor.commit();
    }


    public String getValue(String Tag) {
        if (Tag.equalsIgnoreCase(Consts.LATITUDE))
            return myPrefs.getString(Tag, "22.7497853");
        else if (Tag.equalsIgnoreCase(Consts.LONGITUDE))
            return myPrefs.getString(Tag, "75.8989044");
        return myPrefs.getString(Tag, "");
    }

    public boolean getBooleanValue(String Tag) {
        return myPrefs.getBoolean(Tag, false);

    }

    public void setBooleanValue(String Tag, boolean token) {
        prefsEditor.putBoolean(Tag, token);
        prefsEditor.commit();
    }

    public void setListString(String Tag, List<String> token) {
        String[] items = token.toArray(new String[token.size()]);
        Set<String> set = new HashSet<>(Arrays.asList(items));

        prefsEditor.putStringSet(Tag, set);
        prefsEditor.commit();
    }

    public List<String> getListString(String Tag) {
        return new ArrayList<>(myPrefs.getStringSet(Tag,new HashSet<String>()));
    }

//    public void setParentUser(UserDTO userDTO, String tag) {
//
//        Gson gson = new Gson();
//        String hashMapString = gson.toJson(userDTO);
//
//        prefsEditor.putString(tag, hashMapString);
//        prefsEditor.apply();
//    }
//
//    public UserDTO getParentUser(String tag) {
//        String obj = myPrefs.getString(tag, "defValue");
//        if (obj.equals("defValue")) {
//            return new UserDTO();
//        } else {
//            Gson gson = new Gson();
//            String storedHashMapString = myPrefs.getString(tag, "");
//            Type type = new TypeToken<UserDTO>() {
//            }.getType();
//            UserDTO testHashMap = gson.fromJson(storedHashMapString, type);
//            return testHashMap;
//        }
//    }
}
