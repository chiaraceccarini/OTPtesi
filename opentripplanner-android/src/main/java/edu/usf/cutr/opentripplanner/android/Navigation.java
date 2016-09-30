package edu.usf.cutr.opentripplanner.android;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import org.opentripplanner.api.model.Leg;

import java.util.ArrayList;

import util.Log;

/**
 * Created by chiara on 12/08/16.
 */
public class Navigation {

    private ArrayList<Leg> itinerary;
    private double distanceTot;
    int index = 0;


    public Navigation (ArrayList<Leg> currentItinerary) {
        this.itinerary = currentItinerary;
        this.distanceTot = currentItinerary.get(0).distance;

    }

    //controlla se la direzione è giusta o devo andare dalla parte opposta
    public boolean checkDirection (LatLng punto, LatLng to) {
        if (index==0) {
            distanceTot = calculateDistance(punto, to);
        }
        index++;
        //+5 per avere un pò di margine (se devi usare un percorso più lungo)
        Log.d("DISTANZA DA POSIZIONE A FINE",""+calculateDistance(punto, to));
        Log.d("DISTANZA TOTALE",""+distanceTot);
        return calculateDistance(punto, to)<=distanceTot+15;
    }

    //calcola distanza tra un punto e quello successivo
    public double calculateDistance(LatLng puntoA, int index, boolean goOn) {
        //Log.d("PUNTOa", ""+puntoA.latitude);
        LatLng puntoB = new LatLng(0,0);
        if (goOn) {
            puntoB = new LatLng(itinerary.get(0).getSteps().get(index).getLat(), itinerary.get(0).getSteps().get(index).getLon());
        } else {
            if (index+1<itinerary.get(0).getSteps().size()) {
                puntoB = new LatLng(itinerary.get(0).getSteps().get(index+1).getLat(), itinerary.get(0).getSteps().get(index+1).getLon());

            }
        }
        Location from = new Location("");
        Location to = new Location("");

        from.setLatitude(puntoA.latitude);
        from.setLongitude(puntoA.longitude);
        to.setLongitude(puntoB.longitude);
        to.setLatitude(puntoB.latitude);



        Log.d("RADICE PUNTO INDEX", "" + from.distanceTo(to));

        /*double distanceLat = (puntoA.latitude-puntoB.latitude)*(puntoA.latitude-puntoB.latitude);
        double distanceLong = (puntoA.longitude-puntoB.longitude)*(puntoA.longitude-puntoB.longitude);
        double distance= distanceLat+distanceLong;
        //radice è la distanza fra i due punti
        double radice = Math.sqrt(distance);
        Log.d("RADICE",""+radice);
       /* if (radice == 0) {
            changeIndex();
        }*/
        //return radice;
        return from.distanceTo(to);
    }

    public double calculateDistance(LatLng puntoA, LatLng puntoB) {
        Location from = new Location("");
        Location to = new Location("");

        from.setLatitude(puntoA.latitude);
        from.setLongitude(puntoA.longitude);
        to.setLongitude(puntoB.longitude);
        to.setLatitude(puntoB.latitude);

        /*double distanceLat = (puntoA.latitude-puntoB.latitude)*(puntoA.latitude-puntoB.latitude);
        double distanceLong = (puntoA.longitude-puntoB.longitude)*(puntoA.longitude-puntoB.longitude);
        double distance= distanceLat+distanceLong;
        //radice è la distanza fra i due punti
        double radice = Math.sqrt(distance);*/
        Log.d("RADICE PUNTO PUNTO",""+from.distanceTo(to));
        return from.distanceTo(to);
    }

    //se la distanza è 5m deve apparire la freccia
    public boolean changeDirection(LatLng puntoA, int index) {
        double distance = calculateDistance(puntoA, index, false);
        Log.d("DISTANCEchange", "" + distance);
        //return calculateDistance(puntoA, index)<=5;
        //return distance<=0.0003;
        return distance<=30;
    }

    public boolean changeDirection(LatLng puntoA, LatLng puntoB) {
        double distance = calculateDistance(puntoA, puntoB);
        Log.d("DISTANCEpunti", ""+distance);
        //return calculateDistance(puntoA, index)<=5;
        return distance<=10;
    }

    public boolean goOn (LatLng puntoA, int index) {
        if (index>0) {
            Log.d("goOn","goOn");
            return calculateDistance(puntoA,index, true)<10;
        }
        return  false;
    }

    /*public void changeIndex () {
        this.index++;
    }*/

    public double getDistanceTot (){
        return this.distanceTot;
    }




}
