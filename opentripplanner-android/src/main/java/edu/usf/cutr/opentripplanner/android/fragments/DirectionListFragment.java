/*
 * Copyright 2012 University of South Florida
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package edu.usf.cutr.opentripplanner.android.fragments;

import org.opentripplanner.api.model.RelativeDirection;
import org.opentripplanner.api.model.WalkStep;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.api.model.Itinerary;
import org.opentripplanner.api.model.Leg;
import org.w3c.dom.Text;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.zhaoxiaodan.miband.ActionCallback;
import com.zhaoxiaodan.miband.MiBand;
import com.zhaoxiaodan.miband.listeners.NotifyListener;
import com.zhaoxiaodan.miband.listeners.RealtimeStepsNotifyListener;
import com.zhaoxiaodan.miband.model.VibrationMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;

import edu.usf.cutr.opentripplanner.android.GPSTracker;
import edu.usf.cutr.opentripplanner.android.Navigation;
import edu.usf.cutr.opentripplanner.android.OTPApp;
import edu.usf.cutr.opentripplanner.android.Passi;
import edu.usf.cutr.opentripplanner.android.R;
import edu.usf.cutr.opentripplanner.android.UserInfoActivity;
import edu.usf.cutr.opentripplanner.android.Utility;
import edu.usf.cutr.opentripplanner.android.listeners.OtpFragment;
import edu.usf.cutr.opentripplanner.android.model.Direction;
import edu.usf.cutr.opentripplanner.android.model.OTPBundle;
import edu.usf.cutr.opentripplanner.android.util.ConversionUtils;
import edu.usf.cutr.opentripplanner.android.util.DirectionExpandableListAdapter;
import edu.usf.cutr.opentripplanner.android.util.DirectionsGenerator;
import edu.usf.cutr.opentripplanner.android.util.ExpandableListFragment;
import gl.Color;
import gl.GL1Renderer;
import gl.GLCamera;
import gl.GLFactory;
import gl.scenegraph.MeshComponent;
import gl.textures.Texture;
import gl.textures.TextureManager;
import gl.textures.TexturedShape;
import system.ArActivity;
import system.DefaultARSetup;
import util.Log;
import util.Vec;
import worldData.Obj;
import worldData.World;

import edu.usf.cutr.opentripplanner.android.Navigation;
/**
 * This fragment shows the list of step-by-step directions for a planned trip
 *
 * @author Khoa Tran
 */

public class DirectionListFragment extends ExpandableListFragment {

    View header = null;

    private OtpFragment fragmentListener;

    private ExpandableListView elv;

    private boolean isFragmentFirstLoad = true;

    TextView fromHeader;

    TextView toHeader;

    TextView departureTimeHeader;

    TextView arrivalTimeHeader;

    OTPBundle otpBundle;

    ArrayList<Leg> currentItinerary;
    ArrayList<Direction> directions;
    LatLng end;

    TextToSpeech textToSpeech;

    private MiBand miband;
    HashMap<String, BluetoothDevice> devices = new HashMap<String, BluetoothDevice>();




    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            setFragmentListener((OtpFragment) activity);
        } catch (ClassCastException e) {
            throw new ClassCastException(
                    activity.toString() + " must implement OtpFragment");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View mainView = inflater.inflate(R.layout.direction, container, false);

        header = inflater.inflate(R.layout.list_direction_header, null);

        Button ARButton = (Button) header.findViewById(R.id.ar);
        Button mibandButton = (Button) header.findViewById(R.id.mb);
        ImageButton userInfo = (ImageButton) header.findViewById(R.id.userInfo);

        userInfo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                getActivity().startActivity(intent);
            }
        });

        textToSpeech = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    if (textToSpeech.isLanguageAvailable(Locale.ITALY) == TextToSpeech.LANG_AVAILABLE ) {
                        textToSpeech.setLanguage(Locale.ITALY);
                        Log.d("error", "sono dentro all'if");
                    }else{
                        Log.d("Language", "lingua non disponibile");
                    }
                    //textToSpeech.speak(textSpeech, TextToSpeech.QUEUE_FLUSH, null, "voce");
                }else{
                    Log.e("error", "Inizializzazione fallita");
                    Log.d("ErrorCode", "" + status);
                }
            }
        });

        miband = new MiBand(getActivity());

        final ProgressDialog pd = ProgressDialog.show(getActivity(), "", "Sto cercando...");
        pd.dismiss();

        final ArrayAdapter adapterMiBand = new ArrayAdapter<String>(getActivity(), R.layout.item, new ArrayList<String>());

        final ScanCallback scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                BluetoothDevice device = result.getDevice();
                String item = device.getName() + "|" + device.getAddress();
                if (!devices.containsKey(item)) {
                    devices.put(item, device);
                    adapterMiBand.add(item);
                    pd.dismiss();
                }

            }
        };


        mibandButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                pd.show();
                MiBand.startScan(scanCallback);
            }
        });


        ListView lv = (ListView) header.findViewById(R.id.device);
        lv.setAdapter(adapterMiBand);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = ((TextView) view).getText().toString();
                if (devices.containsKey(item)) {

                    MiBand.stopScan(scanCallback);

                    pd.dismiss();
                    final ProgressDialog pd = ProgressDialog.show(getActivity(), "", "Sto connettendo...");

                    BluetoothDevice device = devices.get(item);

                    miband.connect(device, new ActionCallback() {

                        @Override
                        public void onSuccess(Object data) {

                            pd.dismiss();
                            Log.d("CONNETCT", "connesso");

                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    new AlertDialog.Builder(getActivity())
                                            .setTitle("Connesso")
                                            .setMessage("Mi Band connesso correttamente!")
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            }).show();


                                }
                            });

                            miband.setDisconnectedListener(new NotifyListener() {
                                @Override
                                public void onNotify(byte[] data) {

                                }
                            });
                        }

                        @Override
                        public void onFail(int errorCode, String msg) {
                            pd.dismiss();

                            Log.d("CONNETCT", "fallito");

                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    new AlertDialog.Builder(getActivity())
                                            .setTitle("Errore")
                                            .setMessage("Mi Band non connesso!")
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            }).show();
                                }
                            });

                        }
                    });
                }
            }
        });


        final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if ( manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            ARButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {

                    Navigation nav = new Navigation(currentItinerary);
                    //LatLng latLng = getLocation();


//                if (nav.checkDirection(latLng)) {
                    DefaultARSetup setup = new DefaultARSetup() {

                        @Override
                        public void addObjectsTo(GL1Renderer renderer, World world, GLFactory objectFactory) {
                            MeshComponent arrow = objectFactory.newTreangle(null);
                           /* MeshComponent text = objectFactory.newTextObject("prova", new Vec(0, 1, -10), getActivity(), camera);
                            text.setRotation(new Vec(90, 0, 180));*/
                            Log.d("END", ""+end.latitude);

                            TimerThread p = new TimerThread(currentItinerary, arrow, world, getActivity(), camera, end, textToSpeech, miband);
                            p.start();



                           /* Intent intent = new Intent(getActivity(), GPSScheduler.class);
                            intent.putExtra("step", ""+currentItinerary.get(0).getSteps());
                            intent.putExtra("testo", ""+text);
                            intent.putExtra("arrow", ""+arrow);
                            intent.putExtra("world", ""+world);
                            getActivity().startService(intent);

                            JobScheduler mJobScheduler = (JobScheduler)
                                   getActivity().getSystemService(Context.JOB_SCHEDULER_SERVICE);


                            JobInfo.Builder builder = new JobInfo.Builder( 1,
                                    new ComponentName( getActivity().getPackageName(),
                                            GPSScheduler.class.getName() ) );



                            builder.setPeriodic( 3000 );

                            if( mJobScheduler.schedule( builder.build() ) <= 0 ) {
                                //If something goes wrong
                            }*/

                        }

                    };
                    ArActivity.startWithSetup(getActivity(),setup);


//                }


                }
            });

        } else {
            buildAlertMessageNoGps();
        }


        return mainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ImageButton btnDisplayMap = (ImageButton) header.findViewById(R.id.btnDisplayMap);
        final OtpFragment ofl = this.getFragmentListener();
        final DirectionListFragment dlf = this;
        OnClickListener oclDisplayDirection = new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                ofl.onSwitchedToMainFragment(dlf);
            }
        };
        btnDisplayMap.setOnClickListener(oclDisplayDirection);

        fromHeader = (TextView) header.findViewById(R.id.fromHeader);
        toHeader = (TextView) header.findViewById(R.id.toHeader);
        departureTimeHeader = (TextView) header.findViewById(R.id.departureTimeHeader);
        arrivalTimeHeader = (TextView) header.findViewById(R.id.arrivalTimeHeader);

        if (savedInstanceState != null) {
            otpBundle = (OTPBundle) savedInstanceState
                    .getSerializable(OTPApp.BUNDLE_KEY_OTP_BUNDLE);
            fragmentListener.setOTPBundle(otpBundle);
        } else {
            otpBundle = fragmentListener.getOTPBundle();
        }

        Log.d("fromHeader", otpBundle.getFromText());
        fromHeader.setText(otpBundle.getFromText());
        toHeader.setText(otpBundle.getToText());
        setDepartureArrivalHeaders();

        currentItinerary = new ArrayList<Leg>();

        currentItinerary.addAll(fragmentListener.getCurrentItinerary());

        ArrayList<Itinerary> itineraryList = new ArrayList<Itinerary>();
        itineraryList.addAll(fragmentListener.getCurrentItineraryList());
        int currentItineraryIndex = fragmentListener.getCurrentItineraryIndex();

         directions = new ArrayList<Direction>();

        DirectionsGenerator dirGen = new DirectionsGenerator(currentItinerary,
                getActivity().getApplicationContext());
        ArrayList<Direction> tempDirections = dirGen.getDirections();
        if (tempDirections != null && !tempDirections.isEmpty()) {
            directions.addAll(tempDirections);
        }



        final Activity activity = this.getActivity();
        Spinner itinerarySelectionSpinner = (Spinner) header.findViewById(R.id.itinerarySelection);

        String[] itinerarySummaryList = new String[itineraryList.size()];

        boolean isTransitIsTagSet = false;
        for (int i = 0; i < itinerarySummaryList.length; i++) {
            isTransitIsTagSet = false;
            Itinerary it = itineraryList.get(i);

            for (Leg leg : it.legs) {
                TraverseMode traverseMode = TraverseMode.valueOf(leg.mode);
                if (traverseMode.isTransit()) {
                    itinerarySummaryList[i] = ConversionUtils
                            .getRouteShortNameSafe(leg.routeShortName,
                                    leg.routeLongName,
                                    getActivity().getApplicationContext()) + ". ";
                    isTransitIsTagSet = true;

                    break;
                }
                end = new LatLng(leg.to.getLat(), leg.to.getLon());
            }
            if (!isTransitIsTagSet) {
                itinerarySummaryList[i] = Integer.toString(i + 1)
                        + ".   ";//Shown index is i + 1, to use 1-based indexes for the UI instead of 0-based
            }
        }

        for (int i = 0; i < itinerarySummaryList.length; i++) {
            Itinerary it = itineraryList.get(i);
            long tripDuration;
            if (PreferenceManager.getDefaultSharedPreferences(
                    getActivity().getApplicationContext())
                    .getInt(OTPApp.PREFERENCE_KEY_API_VERSION, OTPApp.API_VERSION_V1)
                    == OTPApp.API_VERSION_V1){
                tripDuration = it.duration;
            }
            else{
                tripDuration = it.duration /*/ 1000*/;
            }
            itinerarySummaryList[i] += getString(R.string.step_by_step_total_duration) + " " + ConversionUtils
                    .getFormattedDurationTextNoSeconds(tripDuration, false,
                            getActivity().getApplicationContext());
            if (isTransitIsTagSet) {
                itinerarySummaryList[i] += "   " + getString(R.string.step_by_step_walking_duration) + " "
                        + ConversionUtils.getFormattedDurationTextNoSeconds(it.walkTime, false,
                        getActivity().getApplicationContext());
            }
        }

        //quello che clicchi con i numeri
        ArrayAdapter<String> itineraryAdapter = new ArrayAdapter<String>(activity,
                android.R.layout.simple_spinner_item, itinerarySummaryList);

        itineraryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        itinerarySelectionSpinner.setAdapter(itineraryAdapter);

        AdapterView.OnItemSelectedListener itinerarySpinnerListener
                = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (fragmentListener.getCurrentItineraryIndex() != position) {
                    fragmentListener.onItinerarySelected(position, 3);
                }

                setDepartureArrivalHeaders();

                if (!isFragmentFirstLoad) {
                    ArrayList<Direction> directions = new ArrayList<Direction>();
                    DirectionsGenerator dirGen = new DirectionsGenerator(
                            fragmentListener.getCurrentItinerary(),
                            getActivity().getApplicationContext());
                    ArrayList<Direction> tempDirections = dirGen.getDirections();
                    if (tempDirections != null && !tempDirections.isEmpty()) {
                        directions.addAll(tempDirections);
                    }

                    Direction direction_data[] = directions
                            .toArray(new Direction[directions.size()]);

                    DirectionExpandableListAdapter adapter = new DirectionExpandableListAdapter(
                            DirectionListFragment.this.getActivity(),
                            R.layout.list_direction_item, R.layout.list_subdirection_item,
                            direction_data);

                    elv.setAdapter(adapter);

                }
                openIfNonTransit();

                isFragmentFirstLoad = false;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
        itinerarySelectionSpinner.setSelection(currentItineraryIndex);
        itinerarySelectionSpinner.setOnItemSelectedListener(itinerarySpinnerListener);

        // Populate list with our static array of titles.
        elv = getExpandableListView();

        Direction direction_data[] = directions.toArray(new Direction[directions.size()]);
        Log.d("DataSize",""+direction_data.length);
        Log.d("DataSizeDirections", "" + directions.size());



        DirectionExpandableListAdapter adapter = new DirectionExpandableListAdapter(
                this.getActivity(),
                R.layout.list_direction_item, R.layout.list_subdirection_item, direction_data);

        elv.addHeaderView(header);

        elv.setAdapter(adapter);

        elv.setGroupIndicator(null); // Get rid of the down arrow

        openIfNonTransit();

        if (savedInstanceState == null) {
            if (otpBundle.isFromInfoWindow()) {
                elv.expandGroup(otpBundle.getCurrentStepIndex());
                elv.setSelectedGroup(otpBundle.getCurrentStepIndex());
                otpBundle.setFromInfoWindow(false);
            }
        }
    }

    private void openIfNonTransit() {
        List<Leg> legsList = fragmentListener.getCurrentItinerary();

        if (legsList.size() == 1) {
            Leg firstLeg = legsList.get(0);
            TraverseMode traverseMode = TraverseMode.valueOf(firstLeg.mode);
            if (!traverseMode.isTransit()) {
                elv.expandGroup(0);
            }
        }
    }

    private void setDepartureArrivalHeaders() {
        Itinerary actualItinerary = fragmentListener.getCurrentItineraryList()
                .get(fragmentListener.getCurrentItineraryIndex());

        if (!actualItinerary.legs.isEmpty()) {
            Leg firstLeg = actualItinerary.legs.get(0);
            Leg lastLeg = actualItinerary.legs.get((actualItinerary.legs.size() - 1));
            int agencyTimeZoneOffset = firstLeg.agencyTimeZoneOffset;
            long startTimeInSeconds = Long.parseLong(firstLeg.startTime);
            long endTimeInSeconds = Long.parseLong(lastLeg.endTime);

            departureTimeHeader.setText(ConversionUtils
                    .getTimeWithContext(getActivity().getApplicationContext(), agencyTimeZoneOffset,
                            startTimeInSeconds, false));

            arrivalTimeHeader.setText(ConversionUtils
                    .getTimeWithContext(getActivity().getApplicationContext(), agencyTimeZoneOffset,
                            endTimeInSeconds, false));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        bundle.putSerializable(OTPApp.BUNDLE_KEY_OTP_BUNDLE, otpBundle);
    }

    /**
     * @return the fragmentListener
     */
    public OtpFragment getFragmentListener() {
        return fragmentListener;
    }

    /**
     * @param fragmentListener the fragmentListener to set
     */
    public void setFragmentListener(OtpFragment fragmentListener) {
        this.fragmentListener = fragmentListener;
    }




    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    @Override
    public void onDestroy() {
        textToSpeech.shutdown();
        super.onDestroy();
    }
}

class TimerThread extends Thread {

    List <WalkStep> ws;
    MeshComponent arrow;
    World world;
    Activity myActivity;
    GLCamera camera;
    ArrayList <Leg> direction;
    LatLng end;
    TextToSpeech textToSpeech;
    MiBand miband;
    boolean canSpeak = true;
    boolean speak = true;
    boolean can = true;




    TimerThread(ArrayList<Leg> itinerary, MeshComponent arrow, World world, Activity activity, GLCamera camera, LatLng end, TextToSpeech tts, MiBand miband) {
        this.ws = itinerary.get(0).getSteps();
        this.arrow = arrow;
        this.world = world;
        this.myActivity = activity;
        this.camera = camera;
        this.direction = itinerary;
        this.end = end;
        this.textToSpeech = tts;
        this.miband = miband;

    }



    public void run() {



        miband.setRealtimeStepsNotifyListener(new RealtimeStepsNotifyListener() {

            @Override
            public void onNotify(int steps) {
                Passi.getInstance().addPassi();
               //passi.add(steps);
                Log.d("PASSI",""+steps);
            }
        });



        int index = 0;
        boolean possible = true;
        boolean sbaglio = false;
        boolean goOn = false;
        boolean canContinue = true;
        String ultimaStrada = "";

       /*Navigation nav = new Navigation(direction);
        //GPSTracker gps = new GPSTracker(myActivity);

        Log.d("END", ""+end.latitude);
        while (index < ws.size()) {

            MeshComponent cartelloGrande = GLFactory.getInstance().newSquare(Color.white());
            MeshComponent cartellopiccl = GLFactory.getInstance().newSquare(Color.blue());
            MeshComponent cartellopiccl1 = GLFactory.getInstance().newSquare(Color.blue());
            MeshComponent scritta1 = GLFactory.getInstance().newTextObject("10000000 passi", new Vec(0, 1, -10), myActivity, camera, Color.blue());
            MeshComponent scritta= GLFactory.getInstance().newTextObject("10000000 calorie", new Vec(0, 1, -10), myActivity, camera, Color.blue());

            scritta.setRotation(new Vec(90, 0, 180));
            scritta1.setRotation(new Vec(90, 0, 180));
            scritta.setPosition(new Vec(-1.2f, -0.7f, -1));
            scritta1.setPosition(new Vec(-1.2f, -0.3f, -1));



            cartellopiccl.setPosition(new Vec(-1.7f, -0.9f, -1));
            cartellopiccl1.setPosition(new Vec(-1.6f, -1f, -1));
            cartelloGrande.setPosition(new Vec(-1.7f, -1f, -1));


            world.add(cartellopiccl);
            world.add(cartellopiccl1);
            world.add(cartelloGrande);
            world.add(scritta);
            world.add(scritta1);

            LatLng punto = getLocation();

           // Log.d("PUNTOlat",""+punto.latitude);

            if (nav.checkDirection(punto, end)) {
                if (index+1<ws.size()) {
                    if (nav.changeDirection(punto, index)) {
                        index++;
                        goOn = false;
                        canSpeak = true;
                        canContinue = true;
                        Log.d("INDEX", "" + index);
                        try {
                            world.clear();
                        } catch (NullPointerException e) {
                            Log.d("","");
                        }

                    }
                }  else {
                    possible = false;
                }

                if (sbaglio) {
                    try {
                        world.clear();
                    } catch (NullPointerException e) {
                        Log.d("","");
                    }
                    sbaglio = false;
                }

                //fai le cose che fai sotto
                String indicazione = "";
                //indicazione
                //String testo = directions.get(0).getDirectionText().toString();
                Log.d("TESTO2", ws.get(0).getRelativeDirection().toString());


                RelativeDirection relDir = ws.get(index).getRelativeDirection();
                Log.d("TESTO2", ws.get(index).getRelativeDirection().toString());


                if (relDir.equals(RelativeDirection.CIRCLE_CLOCKWISE)) {
                    //arrow.setRotation(new Vec(180, 0, 90));
                } else if (relDir.equals(RelativeDirection.CIRCLE_COUNTERCLOCKWISE)) {
                    // arrow.setRotation(new Vec(180, 0, 90));
                } else if (relDir.equals(RelativeDirection.CONTINUE)) {
                    arrow.setRotation(new Vec(0, 0, -135));
                    arrow.setPosition(new Vec(0, -0.5f, -1));
                    indicazione += "Continua su ";
                } else if (relDir.equals(RelativeDirection.DEPART)) {
                    arrow.setRotation(new Vec(0, 0, -135));
                    arrow.setPosition(new Vec(0, -0.5f, -1));
                    indicazione += "Parti su ";
                } else if (relDir.equals(RelativeDirection.ELEVATOR)) {
                    //arrow.setRotation(new Vec(180, 0, 90));
                } else if (relDir.equals(RelativeDirection.HARD_LEFT)) {
                    arrow.setRotation(new Vec(180, 0, 45));
                    arrow.setPosition(new Vec(-1.7f, 0, -1));
                    indicazione += "Gira a sinistra su ";
                } else if (relDir.equals(RelativeDirection.HARD_RIGHT)) {
                    arrow.setRotation(new Vec(0, 180, 45));
                    arrow.setPosition(new Vec(1.7f, 0, -1));
                    indicazione += "Gira a destra su ";
                } else if (relDir.equals(RelativeDirection.LEFT)) {
                    arrow.setRotation(new Vec(180, 0, 45));
                    arrow.setPosition(new Vec(-1.7f, 0, -1));
                    indicazione += "Gira a sinistra su ";
                } else if (relDir.equals(RelativeDirection.RIGHT)) {
                    arrow.setRotation(new Vec(0, 180, 45));
                    arrow.setPosition(new Vec(1.7f, 0, -1));
                    indicazione += "Gira a destra su ";
                } else if (relDir.equals(RelativeDirection.SLIGHTLY_LEFT)) {
                    arrow.setRotation(new Vec(180, 0, 45));
                    arrow.setPosition(new Vec(-1.7f, 0, -1));
                    indicazione += "Svolta leggermente a sinistra su ";
                } else if (relDir.equals(RelativeDirection.SLIGHTLY_RIGHT)) {
                    arrow.setRotation(new Vec(0, 180, 45));
                    arrow.setPosition(new Vec(1.7f, 0, -1));
                    indicazione += "Svolta leggermente a destra su ";
                } else if (relDir.equals(RelativeDirection.UTURN_LEFT)) {
                    arrow.setRotation(new Vec(180, 0, 45));
                    arrow.setPosition(new Vec(-1.7f, 0, -1));
                    indicazione += "Svolta a u a sinistra su ";
                } else if (relDir.equals(RelativeDirection.UTURN_RIGHT)) {
                    arrow.setRotation(new Vec(0, 180, 45));
                    arrow.setPosition(new Vec(1.7f, 0, -1));
                    indicazione += "Svolta a u a destra su ";
                } else {
                    //arrow.setRotation(new Vec(180, 0, 90));
                }

                String strada = ws.get(index).getStreetName();
                if (strada.contains("path")) {
                    indicazione += "percorso";
                } else if (strada.contains("steps")) {
                    indicazione += "scalino";
                } else {
                    indicazione += strada;
                }
                ultimaStrada = strada;

                indicazione += " e prosegui";

                if (nav.goOn(punto, index) || goOn) {
                    arrow.setRotation(new Vec(0, 0, -135));
                    arrow.setPosition(new Vec(0, -0.5f, -1));

                    indicazione = "Continua su " + strada;

                    if (canContinue) {
                        canSpeak = true;

                        try {
                            world.clear();
                        } catch (NullPointerException e) {
                            Log.d("","");
                        }
                    }

                    canContinue = false;

                    goOn = true;
                }


                MeshComponent text = GLFactory.getInstance().newTextObject(indicazione, new Vec(0, 1, -10), myActivity, camera, true);

                text.setRotation(new Vec(90, 0, 180));
                text.setPosition(new Vec(0, 0.7f, -1));
                //index++;


                world.add(arrow);
                world.add(text);



                if (canSpeak) {
                    final String textSpeech = indicazione;

                    parla(textSpeech);

                    canSpeak = false;
                }




                if (!possible) {
                    index ++;
                }
            } else  {
                MeshComponent text = GLFactory.getInstance().newTextObject("Stai andando nella direzione opposta!", new Vec(0, 1, -10), myActivity, camera, false);
                Log.d("SBAGLIO", "sono nello sbaglio direzione");
                text.setRotation(new Vec(90, 0, 180));
                text.setPosition(new Vec(0, 0.7f, -1));
                if (speak) {
                    final String textSpeech = "Stai andando nella direzione opposta!";

                    parla(textSpeech);

                    speak = false;
                }

                try {
                    world.clear();
                } catch (NullPointerException e) {
                    Log.d("SBAGLIO", "" + e);
                }

                world.add(text);
                sbaglio = true;
            }

            try {
                sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }


        boolean cambia = true;

        while (cambia) {
            LatLng punto = getLocation();

            String indicazione ="";

            if (nav.goOn(punto, index-1) || goOn) {
                arrow.setRotation(new Vec(0, 0, -135));
                arrow.setPosition(new Vec(0, -0.5f, -1));

                indicazione = "Continua su " + ultimaStrada;

                if (canContinue) {
                    canSpeak = true;

                    try {
                        world.clear();
                    } catch (NullPointerException e) {
                        Log.d("","");
                    }
                }

                canContinue = false;

                goOn = true;

                MeshComponent text = GLFactory.getInstance().newTextObject(indicazione, new Vec(0, 1, -10), myActivity, camera, true);

                text.setRotation(new Vec(90, 0, 180));
                text.setPosition(new Vec(0, 0.7f, -1));
                //index++;


                world.add(arrow);
                world.add(text);
            }

            if (nav.changeDirection(punto, end)) {
                Log.d("SBAGLIOdentroS", "" + ws.size());
                Log.d("SBAGLIOdentro", "" + index);



                MeshComponent destinazione = GLFactory.getInstance().newTextObject("Destinazione raggiunta!", new Vec(0, 1, -10), myActivity, camera);

                destinazione.setRotation(new Vec(90, 0, 180));
                destinazione.setPosition(new Vec(0, 0.7f, -1));


                world.clear();
                world.add(destinazione);

                if (Passi.getInstance().getPassi()!=0) {
                    miband.startVibration(VibrationMode.VIBRATION_WITH_LED);
                    MeshComponent numPassi = GLFactory.getInstance().newTextObject("" + Passi.getInstance().getPassi() + " passi!", new Vec(0, 1, -10), myActivity, camera);

                    numPassi.setRotation(new Vec(90, 0, 180));
                    numPassi.setPosition(new Vec(0, 0f, -1));

                    world.add(numPassi);

                    if (!Utility.getSharedPreferences(myActivity).getString("peso", "").equals("")) {
                        MeshComponent kcal = GLFactory.getInstance().newTextObject("Hai bruciato " + calculateKCal(Passi.getInstance().getPassi()) + " calorie!", new Vec(0, 1, -10), myActivity, camera);

                        kcal.setRotation(new Vec(90, 0, 180));
                        kcal.setPosition(new Vec(0, -0.7f, -1));

                        world.add(kcal);
                    }

                    MeshComponent cartelloGrande = GLFactory.getInstance().newSquare(Color.blue());
                    MeshComponent cartelloPiccolo = GLFactory.getInstance().newSquare(Color.white());

                    cartelloGrande.setText(""+Passi.getInstance().getPassi()+" passi, bruciando "+calculateKCal(Passi.getInstance().getPassi())+" calorie");

                    cartelloGrande.setPosition(new Vec(0.7f,-0.7f,-1));
                }

                //Log.d("PASSI TOT",""+passi.size());

                if (can) {
                    final String textSpeech = "Destinazione raggiunta!";

                    parla(textSpeech);

                    can = false;
                }
                cambia = false;
            }

            try {
                sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }*/





        //versione a tempo
        //int index = 0;


        while (index < ws.size()) {

            String indicazione = "";

            //indicazione
            //String testo = directions.get(0).getDirectionText().toString();

            Log.d("TESTO2", ws.get(0).getRelativeDirection().toString());


            RelativeDirection relDir = ws.get(index).getRelativeDirection();
            Log.d("TESTO2", ws.get(index).getRelativeDirection().toString());


            if (relDir.equals(RelativeDirection.CIRCLE_CLOCKWISE)) {
                //arrow.setRotation(new Vec(180, 0, 90));
            } else if (relDir.equals(RelativeDirection.CIRCLE_COUNTERCLOCKWISE)) {
                // arrow.setRotation(new Vec(180, 0, 90));
            } else if (relDir.equals(RelativeDirection.CONTINUE)) {
                arrow.setRotation(new Vec(0, 0, -135));
                arrow.setPosition(new Vec(0, -0.5f, -1));
                indicazione += "Continua su ";
            } else if (relDir.equals(RelativeDirection.DEPART)) {
                arrow.setRotation(new Vec(0, 0, -135));
                arrow.setPosition(new Vec(0, -0.5f, -1));
                indicazione += "Parti su ";
            } else if (relDir.equals(RelativeDirection.ELEVATOR)) {
                //arrow.setRotation(new Vec(180, 0, 90));
            } else if (relDir.equals(RelativeDirection.HARD_LEFT)) {
                arrow.setRotation(new Vec(180, 0, 45));
                arrow.setPosition(new Vec(-1.7f, 0, -1));
                indicazione += "Gira a sinistra su ";
            } else if (relDir.equals(RelativeDirection.HARD_RIGHT)) {
                arrow.setRotation(new Vec(0, 180, 45));
                arrow.setPosition(new Vec(1.7f, 0, -1));
                indicazione += "Gira a destra su ";
            } else if (relDir.equals(RelativeDirection.LEFT)) {
                arrow.setRotation(new Vec(180, 0, 45));
                arrow.setPosition(new Vec(-1.7f, 0, -1));
                indicazione += "Gira a sinistra su ";
            } else if (relDir.equals(RelativeDirection.RIGHT)) {
                arrow.setRotation(new Vec(0, 180, 45));
                arrow.setPosition(new Vec(1.7f, 0, -1));
                indicazione += "Gira a destra su ";
            } else if (relDir.equals(RelativeDirection.SLIGHTLY_LEFT)) {
                arrow.setRotation(new Vec(180, 0, 45));
                arrow.setPosition(new Vec(-1.7f, 0, -1));
                indicazione += "Svolta leggermente a sinistra su ";
            } else if (relDir.equals(RelativeDirection.SLIGHTLY_RIGHT)) {
                arrow.setRotation(new Vec(0, 180, 45));
                arrow.setPosition(new Vec(1.7f, 0, -1));
                indicazione += "Svolta leggermente a destra su ";
            } else if (relDir.equals(RelativeDirection.UTURN_LEFT)) {
                arrow.setRotation(new Vec(180, 0, 45));
                arrow.setPosition(new Vec(-1.7f, 0, -1));
                indicazione += "Svolta a u a sinistra su ";
            } else if (relDir.equals(RelativeDirection.UTURN_RIGHT)) {
                arrow.setRotation(new Vec(0, 180, 45));
                arrow.setPosition(new Vec(1.7f, 0, -1));
                indicazione += "Svolta a u a destra su ";
            } else {
                //arrow.setRotation(new Vec(180, 0, 90));
            }


            String strada = ws.get(index).getStreetName();
            if (strada.contains("path")) {
                indicazione += "percorso";
            } else if (strada.contains("steps")) {
                indicazione += "scalino";
            } else {
                indicazione += strada;
            }

            MeshComponent text = GLFactory.getInstance().newTextObject(indicazione, new Vec(0, 1, -10), myActivity, camera);

            text.setRotation(new Vec(90, 0, 180));
            text.setPosition(new Vec(0, 0.7f, -1));

            //index++;


            world.add(arrow);
            world.add(text);


            try {
                sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            index++;
            world.remove(text);
        }

        Log.d("END1", "" + ws.get(ws.size() - 1).getLat());

        MeshComponent endText = GLFactory.getInstance().newTextObject("Destinazione raggiunta!", new Vec(0, 1, -10), myActivity, camera);

        endText.setRotation(new Vec(90, 0, 180));
        endText.setPosition(new Vec(0, 0.7f, -1));

        world.clear();
        world.add(endText);

         MeshComponent cartelloGrande = GLFactory.getInstance().newSquare(Color.blackTransparent());
            MeshComponent cartellopiccl = GLFactory.getInstance().newSquare(Color.blue());
            MeshComponent cartellopiccl1 = GLFactory.getInstance().newSquare(Color.blue());
            MeshComponent cartellopiccl2 = GLFactory.getInstance().newSquare(Color.whiteTransparent());
            MeshComponent scritta1 = GLFactory.getInstance().newTextObject("10000000 passi", new Vec(0, 1, -10), myActivity, camera, Color.whiteTransparent());
            MeshComponent scritta= GLFactory.getInstance().newTextObject("10000000 calorie", new Vec(0, 1, -10), myActivity, camera, Color.whiteTransparent());

            scritta.setRotation(new Vec(90, 0, 180));
            scritta1.setRotation(new Vec(90, 0, 180));
            scritta.setPosition(new Vec(-1.2f, -0.8f, -1));
            scritta1.setPosition(new Vec(-1.2f, -0.6f, -1));



            cartellopiccl.setPosition(new Vec(-1.7f, -1.3f, -1));
            cartellopiccl1.setPosition(new Vec(-1.6f, -1.4f, -1));
            cartelloGrande.setPosition(new Vec(-1.7f, -1.4f, -1));
            cartellopiccl2.setPosition(new Vec(-1.6f, -1.3f, -1));
            cartellopiccl2.setPosition(new Vec(-1.65f, -1.35f, -1));



            world.add(cartellopiccl2);
            world.add(cartelloGrande);
            world.add(scritta);
            world.add(scritta1);

        if (Passi.getInstance().getPassi()!=0) {
            MeshComponent numPassi = GLFactory.getInstance().newTextObject("" + Passi.getInstance().getPassi() + " passi!", new Vec(0, 1, -10), myActivity, camera);

            numPassi.setRotation(new Vec(90, 0, 180));
            numPassi.setPosition(new Vec(0, 0f, -1));

            world.add(numPassi);

            if (!Utility.getSharedPreferences(myActivity).getString("peso", "").equals("")) {
                // MeshComponent kcal = GLFactory.getInstance().newTextObject("Hai bruciato " + calculateKCal(nav.getDistanceTot()) + " kcal!", new Vec(0, 1, -10), myActivity, camera);
                //MeshComponent kcal = GLFactory.getInstance().newTextObject("Hai bruciato " + calculateKCal(ws.get(0).getDistance()) + " kcal!", new Vec(0, 1, -10), myActivity, camera);
                MeshComponent kcal = GLFactory.getInstance().newTextObject("Hai bruciato " + calculateKCal(Passi.getInstance().getPassi()) + " cal!", new Vec(0, 1, -10), myActivity, camera);

                kcal.setRotation(new Vec(90, 0, 180));
                kcal.setPosition(new Vec(0, -0.7f, -1));

                world.add(kcal);
            }
        }





    }

    private void parla(String indicazione) {
        textToSpeech.speak(indicazione, TextToSpeech.QUEUE_FLUSH, null, "indicazione");

        Log.d("TextToSpeech", "sono dentro al parla");
    }

    private String calculateKCal (int passi) {
        String p = Utility.getSharedPreferences(myActivity).getString("peso", "");
        String sesso = Utility.getSharedPreferences(myActivity).getString("sesso", "");
        String alt = Utility.getSharedPreferences(myActivity).getString("altezza", "");

        double param = 0;
        if (sesso.equals("Donna")) {
            param = 0.413;
        } else {
            param = 0.415;
        }

        double altezza = Double.parseDouble(alt);
        double peso = Double.parseDouble(p);

        //return String.format("%.2f", metri*peso*0.45/1000);
        return String.format("%.2f", altezza*param*passi*peso/100);

    }

    public LatLng getLocation() {
        // Get the location manager
        LocationManager locationManager = (LocationManager) myActivity.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(bestProvider);
        Double lat,lon;
        try {
            lat = location.getLatitude ();
            lon = location.getLongitude ();
            return new LatLng(lat, lon);
        }
        catch (NullPointerException e){
            e.printStackTrace();
            return null;
        }
    }




}



