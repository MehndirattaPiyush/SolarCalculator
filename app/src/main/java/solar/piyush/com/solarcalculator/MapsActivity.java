package solar.piyush.com.solarcalculator;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;

import org.shredzone.commons.suncalc.MoonPhase;
import org.shredzone.commons.suncalc.MoonPosition;
import org.shredzone.commons.suncalc.MoonTimes;
import org.shredzone.commons.suncalc.SunPosition;
import org.shredzone.commons.suncalc.SunTimes;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private boolean mLocationPermissionGranted = false;
    private String TAG = "MainActivity";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100;
    private LatLng mDefaultLocation = new LatLng(10.0,10.0);
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastKnownLocation;
    private GoogleMap.OnCameraIdleListener onCameraIdleListener;
    private TextView mDate;
    private TextView mMoonrise;
    private TextView mSunrise;
    private TextView mSunset;
    private TextView mMoonset;
    private String date = "";
    private double currentLat ;
    private double currentLng ;
    private String sunrise ;
    private String sunset ;
    private String moonrise ;
    private String moonset ;
    private SupportMapFragment mapFragment;
    LocationRoomDatabase locationRoomDatabase;
    private Polyline sunriseLine;
    private Polyline sunsetLine;
    private Polyline moonriseLine;
    private Polyline moonsetLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mDate = findViewById(R.id.tv_date);
        mDate.setText(getDate(0));

        mSunrise = findViewById(R.id.tv_sunrise);
        mSunset = findViewById(R.id.tv_sunset);
        mMoonrise = findViewById(R.id.tv_moonrise);
        mMoonset = findViewById(R.id.tv_moonset);

        locationRoomDatabase = LocationRoomDatabase.getDatabase(MapsActivity.this);

//        getDate(0);
        // Construct a FusedLocationProviderClient.
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
//
//        configureCameraIdle();
//
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                setMarker(place.getName().toString(),place.getLatLng() );
            }

            @Override
            public void onError(Status status) {

            }
        });



    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                Log.d(TAG, "onMarkerDragStart: here");
                removeLines();
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                Log.d(TAG, "onMarkerDrag: here");
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                Log.d(TAG, "onMarkerDragEnd: here");
                currentLat = marker.getPosition().latitude;
                currentLng = marker.getPosition().longitude;
                setTime();

            }
        });
        updateLocationUI();
        try{
            String name = getIntent().getStringExtra("name");
            double lat = getIntent().getDoubleExtra("lat" , 1.0);
            double lng = getIntent().getDoubleExtra("lng" ,1.0);
            LatLng latLng = new LatLng(lat , lng);
            if(name==null){
                getDeviceLocation();
            } else {
               setMarker(name , latLng);

            }
        }catch (Exception e){
            getDeviceLocation();
        }



    }
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Log.d(TAG, "getDeviceLocation: here");
                Task locationResult = mFusedLocationClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: success");
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = (Location) task.getResult();
                            setMarker("You" , new LatLng(
                                    mLastKnownLocation.getLatitude(),
                                    mLastKnownLocation.getLongitude())
                            );
                            setAlarm(
                                    mLastKnownLocation.getLatitude(),
                                    mLastKnownLocation.getLongitude()
                            );


                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                           setMarker("error" , new LatLng(0.0,0.0));
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();

            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            showMessageOKCancel("App require to know your location.");

        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        getDeviceLocation();
    }

    public void onDateButtonClick(View view){

        switch (view.getId()){
            case R.id.btn_prev:
                mDate.setText(getDate(-1));
                setTime();
                break;
            case R.id.btn_reset:
                mDate.setText(getDate(0));
                setTime();
                break;
            case R.id.btn_next:
                mDate.setText(getDate(1));
                setTime();
                break;

            case R.id.btn_pin:

                showAlert();

                break;
            case R.id.btn_see_pinned:
                Intent intent = new Intent(this , PinnedLocations.class);
                startActivity(intent);
                break;
        }
    }

    private String getDate(int i){
        // func to get the current date
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");

        if(mDate.getText()!=null && i != 0){
            try {
                cal.setTime(df.parse(mDate.getText().toString()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        cal.add(Calendar.DATE,i);
        Date c = cal.getTime();
        date = df.format(c);
        Log.d(TAG, "getDate: "+date);
        return date;
    }



    private void setMarker(String title,LatLng latLng){
        currentLat = latLng.latitude;
        currentLng = latLng.longitude;
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng).title(title)).setDraggable(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f));

        setTime();


    }

    public void showAlert(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set prompts.xml to alertdialog builder
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.prompts_view, null);

        alertDialogBuilder.setView(promptsView);

        final EditText userInput =  promptsView
                .findViewById(R.id.et_DialogUserInput);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                // edit text
                                pinLocation(new LocationEntity(
                                        currentLat,
                                        currentLng,
                                        userInput.getText().toString(),
                                        0
                                ));
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }

    public void pinLocation(LocationEntity locationEntity) {

//        LocationViewModel mViewModel = ViewModelProviders.of(this).get(LocationViewModel.class);
//        mViewModel.insert(locationEntity);
//
//        Toast.makeText(
//                getApplicationContext(),
//                "Location Saved - "+locationEntity.getName(),
//                Toast.LENGTH_LONG).show();

        new InsertTask(MapsActivity.this,locationEntity).execute();

    }

    private static class InsertTask extends AsyncTask<Void,Void,Boolean> {

        private WeakReference<MapsActivity> activityReference;
        private LocationEntity entity;
        Context context;
        // only retain a weak reference to the activity
        InsertTask(MapsActivity context, LocationEntity entity) {
            activityReference = new WeakReference<>(context);
            this.context = context;
            this.entity = entity;
        }
            long   id2 =0;
        // doInBackground methods runs on a worker thread
        @Override
        protected Boolean doInBackground(Void... objs) {
            long id = activityReference.get().locationRoomDatabase.locationDao().insert(entity);
            id2=id;
            //Log.d("asdf", "doInBackground: "+id); 1 aya value matlab insert hua pehli baar dubara krne pe bhi 1 hi ayi

//            Toast.makeText(context, "" + id, Toast.LENGTH_SHORT).show(); //ek seckarun run?
            return true;

        }

        // onPostExecute runs on main thread
        @Override
        protected void onPostExecute(Boolean bool) {
            if (bool){
                Toast.makeText(
                        context,
                        "Location Saved - "+entity.getName() +" "+id2,
                        Toast.LENGTH_LONG)
                        .show();
            }
        }

    }

    private void showMessageOKCancel(String message) {
        new AlertDialog.Builder(MapsActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(MapsActivity.this,
                                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void setTime(){

        //setting sun and moon times.


        Calendar cal = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");

        if(mDate.getText()!=null){
            try {
                cal.setTime(df.parse(mDate.getText().toString()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

//        sunrise = calculator.getNauticalSunriseForDate(cal);
//        sunset = calculator.getNauticalSunsetForDate(cal);
//
//        mSunrise.setText(sunrise);
//        mSunset.setText(sunset);
//        setMoonTime();
        SimpleDateFormat df2 = new SimpleDateFormat("HH:mm");

        SunTimes sunTimes = SunTimes.compute().on(cal.getTime())
                .at(currentLat,currentLng)
                .execute();
        sunrise = df2.format(sunTimes.getRise());
        sunset = df2.format(sunTimes.getSet());
        mSunrise.setText(sunrise);
        mSunset.setText(sunset);

        MoonTimes moonTimes = MoonTimes.compute().on(cal.getTime())
                .at(currentLat,currentLng)
                .execute();
        moonrise = df2.format(moonTimes.getRise());
        moonset = df2.format(moonTimes.getSet());

        mMoonrise.setText(moonrise);
        mMoonset.setText(moonset);

        SunPosition sunPositionRise = SunPosition.compute().on(sunTimes.getRise())
                .at(currentLat , currentLng).execute();

        SunPosition sunPositionSet = SunPosition.compute().on(sunTimes.getSet())
                .at(currentLat , currentLng).execute();

        MoonPosition moonPositionRise = MoonPosition.compute().on(moonTimes.getRise())
                .at(currentLat , currentLng).execute();

        MoonPosition moonPositionSet = MoonPosition.compute().on(moonTimes.getSet())
                .at(currentLat , currentLng).execute();

        drawLines(
                sunPositionRise.getAzimuth(),
                sunPositionSet.getAzimuth(),
                moonPositionRise.getAzimuth(),
                moonPositionSet.getAzimuth()
        );


    }
    private Calendar getGoldCal(double lat , double lng){

        // calculating golden hour for shoot.
        SunTimes sunTimes = SunTimes.compute().on(Calendar.getInstance().getTime())
                .at(lat,lng)
                .execute();
        Calendar c = Calendar.getInstance();
        c.setTime(sunTimes.getSet());
        c.add(Calendar.HOUR, -1);

        if(c.before(Calendar.getInstance())){
            c = Calendar.getInstance();
            c.add(Calendar.DATE,1);
            sunTimes = SunTimes.compute().on(c.getTime())
                    .at(lat,lng)
                    .execute();
        }
        c.setTime(sunTimes.getSet());
        c.add(Calendar.HOUR,-1);

        return c;

    }
    private void setAlarm(double lat , double lng){

        //setting up alarm using alarm manager.

        boolean alarmUp = (PendingIntent.getBroadcast(getApplicationContext(), 100,
                new Intent(getApplicationContext(), NotifReciever.class),
                PendingIntent.FLAG_UPDATE_CURRENT) != null);

        if(alarmUp) {

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent notificationIntent = new Intent(getApplicationContext(), NotifReciever.class);
            // device locations lat &lng
            notificationIntent.putExtra("lat" , lat);
            notificationIntent.putExtra("lng", lng);

            notificationIntent.addCategory("android.intent.category.DEFAULT");

            PendingIntent broadcast = PendingIntent.getBroadcast(this, 100, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Calendar cal = getGoldCal(lat , lng);
//            Calendar test = Calendar.getInstance();
//            test.add(Calendar.SECOND,2);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), broadcast);
            Toast.makeText(getApplicationContext(), "Notification set for "+cal.getTime() , Toast.LENGTH_LONG).show();

        }
    }

    private void drawLines(double sr ,double ss , double mr , double ms){


        if(sunriseLine!=null){
            removeLines();
        }

          sunriseLine = mMap.addPolyline(
                  new PolylineOptions()
                          .add(new LatLng(currentLat, currentLng), getPoint(sr))
                          .color(0xffff9933)
          );
          sunsetLine = mMap.addPolyline(
                new PolylineOptions()
                        .add(new LatLng(currentLat, currentLng),getPoint(ss))
                        .color(0xffffbf80)
        );
          moonriseLine = mMap.addPolyline(
                new PolylineOptions()
                        .add(new LatLng(currentLat, currentLng),getPoint(mr))
                        .color(0xff0099ff)
        );
          moonsetLine = mMap.addPolyline(
                new PolylineOptions()
                        .add(new LatLng(currentLat, currentLng), getPoint(ms))
                        .color(0xff99d6ff)
        );


    }
    private void removeLines(){
        sunriseLine.remove();
        sunsetLine.remove();
        moonriseLine.remove();
        moonsetLine.remove();
    }

    private LatLng getPoint(double angle){
        // getting point to plot the line..
        // using y = mx eq of line
        double y = 0.0;
        double m =  Math.tan(angle);
        double c = 0.0;
        double x = (Math.sqrt(1/(1+(m*m))));

        c = currentLat - 10.0*x;
        y = currentLng - (10*m)*x;

        return new LatLng(c ,y);

    }

}