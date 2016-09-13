package edu.usf.cutr.opentripplanner.android;

/**
 * Created by chiara on 12/09/16.
 */
public class Passi {
    private static Passi mInstance = null;

    private int passi;

    private Passi(){
        passi = 0;
    }

    public static Passi getInstance(){
        if(mInstance == null)
        {
            mInstance = new Passi();
        }
        return mInstance;
    }

    public int getPassi(){
        return this.passi;
    }

    public void addPassi(){
        passi++;
    }
}