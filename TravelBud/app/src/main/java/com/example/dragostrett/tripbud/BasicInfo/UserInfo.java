package com.example.dragostrett.tripbud.BasicInfo;

import com.google.android.gms.maps.model.LatLng;

import java.sql.Date;

/**
 * Created by DragosTrett on 23.05.2017.
 * class for local saving the user information
 */

public class UserInfo {
    //class for easy acces to user details
    public static String username;
    public static String email;
    public static String password;
    public static String trip;
    public static String id;
    public static String type;
    public static boolean logedIn;
    public static boolean location;
    public static String notification="";
    public static boolean visible=false;
    public static LatLng userLoc;
    public static Date curentDate;
    public static boolean showEveryThing=false;

    public static boolean isShowEveryThing() {
        return showEveryThing;
    }

    public static void setShowEveryThing(boolean showEveryThing) {
        UserInfo.showEveryThing = showEveryThing;
    }

    public static Date getCurentDate() {
        return curentDate;
    }

    public static void setCurentDate(Date curentDate) {
        UserInfo.curentDate = curentDate;
    }

    public static LatLng getUserLoc() {
        return userLoc;
    }

    public static void setUserLoc(LatLng userLoc) {
        UserInfo.userLoc = userLoc;
    }

    public static boolean isAutoLogIn() {
        return AutoLogIn;
    }

    public static void setAutoLogIn(boolean autoLogIn) {
        AutoLogIn = autoLogIn;
    }

    public static boolean AutoLogIn=true;

    public static boolean isVisible() {
        return visible;
    }

    public static void setVisible(boolean visible) {
        UserInfo.visible = visible;
    }

    public static String getNotification() {
        return notification;
    }

    public static void setNotification(String notification) {
        UserInfo.notification = notification;
    }


    public static boolean isLocation() {
        return location;
    }

    public static void setLocation(boolean location) {
        UserInfo.location = location;
    }

    public static boolean isLogedIn() {return logedIn;}

    public static void setLogedIn(boolean logedIn) {
        UserInfo.logedIn = logedIn;
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        UserInfo.username = username;
    }

    public static String getEmail() {
        return email;
    }

    public static void setEmail(String email) {
        UserInfo.email = email;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        UserInfo.password = password;
    }

    public static String getTrip() {
        return trip;
    }

    public static void setTrip(String trip) {
        UserInfo.trip = trip;
    }

    public static String getId() {
        return id;
    }

    public static void setId(String id) {
        UserInfo.id = id;
    }

    public static String getType() {
        return type;
    }

    public static void setType(String type) {
        UserInfo.type = type;
    }

}
