package edu.usf.cutr.opentripplanner.android;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by chiara on 12/09/16.
 */
public class Utility {

    private static final String MY_SHARED = "user";
    private static final String name = "";
    private static final String peso = "";
    private static final String altezza = "";
    private static final String sesso = "";
    private static final String age = "";

    public static SharedPreferences getSharedPreferences(Context context) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(MY_SHARED, Context.MODE_PRIVATE);
        return sharedPreferences;
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        return getSharedPreferences(context).edit();
    }

    public static String getName(Context context) {
        return getSharedPreferences(context).getString(name, "");
    }

    public static String getPeso(Context context) {
        return getSharedPreferences(context).getString(peso, "");
    }

    public static String getAltezza(Context context) {
        return getSharedPreferences(context).getString(altezza, "");
    }

    public static String getSesso(Context context) {
        return getSharedPreferences(context).getString(sesso, "");
    }

    public static String getAge(Context context) {
        return getSharedPreferences(context).getString(age, "");
    }

    public static void saveName (Context context, String text) {
            getEditor(context).putString(name, text).commit();
    }
    public static void savePeso (Context context, String text) {
        getEditor(context).putString(peso, text).commit();
    }
    public static void saveAltezza (Context context, String text) {
        getEditor(context).putString(altezza, text).commit();
    }
    public static void saveSesso (Context context, String text) {
        getEditor(context).putString(sesso, text).commit();
    }
    public static void saveAge (Context context, String text) {
        getEditor(context).putString(age, text).commit();
    }

}
