package com.example.dragostrett.tripbud;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.example.dragostrett.tripbud.Background.GetAllUsersLocBG;
import com.example.dragostrett.tripbud.Background.UpdateLocationBG;
import com.example.dragostrett.tripbud.Background.refreshUserData;
import com.example.dragostrett.tripbud.BasicInfo.MeetInfo;
import com.example.dragostrett.tripbud.BasicInfo.TripInfo;
import com.example.dragostrett.tripbud.BasicInfo.UserInfo;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static GoogleMap mMap;
    public static GoogleApiClient mGoogleApiClient;
    public static Location mLastLocation;
    public static Marker user, meet;
    public static Context context;
    public static Activity cont;
    public static boolean k;
    private static boolean started = false;
    private static Handler handler = new Handler();
    public static Circle circle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //ending previous activity
        context = this;
        cont = this;
        if (UserInfo.isLogedIn()) {
            if (!k)
                RegisterActivity.fa.finish();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Button action = (Button) findViewById(R.id.action_settings);
        toolbar.setTitle("TravelBud");
        float opacity = (float) 1;
        toolbar.setAlpha(opacity);
        setSupportActionBar(toolbar);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);//add map
        mapFragment.getMapAsync(this);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        buildGoogleApiClient();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        } else
            Toast.makeText(this, "Not connected...", Toast.LENGTH_SHORT).show();

        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //start location listener
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100,
                1, mLocationListener);

    }

    private static Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //Log.e("refresh", "refreshed again");
            if(started) {
                refresh();
                start();
            }
            else return;
        }
    };

    public static void stop() {
        started = false;
        //handler.removeCallbacks(runnable);
    }

    public static void start() {
        started = true;
        handler.postDelayed(runnable, 5000);
    }
    public void onDestroy() {
        super.onDestroy();
        //stop the runnable when app is closed for batery reasons
        LogInActivity.cancelNotification();
        stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //starts the runnable once nedded
        start();
    }

    @Override
    protected void onPause() {
        //stop the runnable for baterry save
        super.onPause();
        stop();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            //log out the app
            mMap.clear();
            UserInfo.setAutoLogIn(false);
            MainActivity.mMap.clear();
            LogInActivity.pref.edit().putString("username", UserInfo.getUsername()).putString("password", UserInfo.getPassword()).putBoolean("autoLogIn", false).commit();
            this.finishAndRemoveTask();
            return true;
        } else if (id == R.id.action_refresh) {
            //exit
            stop();
            mMap.clear();
            LogInActivity.pref.edit().putString("username", UserInfo.getUsername()).putString("password", UserInfo.getPassword()).putBoolean("autoLogIn", true).commit();
            LogInActivity.fa.finish();
            this.finishAndRemoveTask();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        stop();
        if (id == R.id.nav_manage_trip) {
            //change activity to trip manager
            Intent intent = new Intent(this, TripActivity.class);
            this.startActivity(intent);
        } else if (id == R.id.nav_manage_account) {
            //change activity to account manager
            Intent intent = new Intent(this, ManageAcountActivity.class);
            this.startActivity(intent);
        } else if (id == R.id.nav_meting_point) {
            //change activity to meeting point manager
            if (TripInfo.isInATrip()) {
                if(UserInfo.getCurentDate().before(TripInfo.getStartDate()) || UserInfo.getCurentDate().after(TripInfo.getEndDate())){
                    Toast.makeText(this, "Trip out of date",
                            Toast.LENGTH_SHORT).show();
                }else {
                    Intent intent = new Intent(this, MeetingActivity.class);
                    this.startActivity(intent);
                }
            } else Toast.makeText(this, "You are not part of any trip",
                    Toast.LENGTH_SHORT).show();
        }
        else if (id == R.id.nav_announcements) {
            //change activity to announcements screen
            if(TripInfo.isInATrip()){
                if(UserInfo.getCurentDate().before(TripInfo.getStartDate()) || UserInfo.getCurentDate().after(TripInfo.getEndDate())){
                    Toast.makeText(this, "Trip out of date",
                            Toast.LENGTH_SHORT).show();
                }
                else{
                    Intent intent = new Intent(this, AnnouncementsActivity.class);
                    this.startActivity(intent);
                }
            }
            else{
                Toast.makeText(this, "You are not part of any trip",
                        Toast.LENGTH_SHORT).show();
            }
        }else if (id == R.id.nav_notifications) {//open notifications tab
            Intent intent = new Intent(this, NotificationActivity.class);
            this.startActivity(intent);
        }
        else if (id == R.id.nav_chat) {//this is hidden and only for dev testing
            Intent intent = new Intent(this, TestingActivity.class);
            this.startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //ad user to the map
        mMap = googleMap;
        user= mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).visible(false));
        if (UserInfo.isLocation()) {
            //if user has gave location permision show him on the map
            mMap.moveCamera(CameraUpdateFactory.newLatLng(UserInfo.getUserLoc()));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(UserInfo.getUserLoc(), 13));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(UserInfo.getUserLoc())// Sets the center of the map to location user
                    .zoom(12)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));//move camera on current user
            user = mMap.addMarker(new MarkerOptions().position(UserInfo.getUserLoc()).title(UserInfo.getUsername()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            if (!TripInfo.getMeet().equals("")) {
                //add meeting point if existent
                meet = mMap.addMarker(new MarkerOptions().position(MeetInfo.getPosition()).title(TripInfo.getMeet()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            }
            if(!UserInfo.getTrip().equals("") && UserInfo.isShowEveryThing())//add other users if the user is in a trip
            new GetAllUsersLocBG(this, mMap).execute();//refresh all data for the map and others
        }
        start();//start the new runnable to make connection real time
    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Requesting location",//getting permision to location
                        Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions( this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            //set user current location
            UserInfo.setUserLoc(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
            UserInfo.setLocation(true);
            LatLng sydney = UserInfo.getUserLoc();
            if(k){
                user.remove();
            //replace user position with current location
            user = mMap.addMarker(new MarkerOptions().position(sydney).title(UserInfo.getUsername()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(UserInfo.getUserLoc(), 13));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(UserInfo.getUserLoc())      // Sets the center of the map to location user
                    .zoom(15)                   // Sets the zoom
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));}
            new UpdateLocationBG(this).execute();//update location in DB
        } else {Toast.makeText(this, "Location Failure",
                Toast.LENGTH_SHORT).show();
                UserInfo.setLocation(false);}
        LocationRequest mLocationRequest = new LocationRequest();
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //Toast.makeText(context, "Location changed",
                //        Toast.LENGTH_SHORT).show();
                mLastLocation=location;
                UserInfo.setUserLoc(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                Log.e("location", String.valueOf(mLastLocation.getLatitude())+" "+ String.valueOf(mLastLocation.getLongitude()));
                new UpdateLocationBG(MainActivity.context).execute();

                //refresh();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        //request manager
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Location granted",
                            Toast.LENGTH_SHORT).show();

                } else {

                    Toast.makeText(this, "Location denied",
                            Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Location Failure",//no location activated
                Toast.LENGTH_SHORT).show();
    }

    public static void refresh(){
        new refreshUserData(context, mMap).execute();//refresh user important data
        if(!UserInfo.getTrip().equals("") && UserInfo.isShowEveryThing()){//double check for showing important staff on the map
            if(TripInfo.isInATrip()){
                 new GetAllUsersLocBG(context, mMap).execute();//if so, show everything
            }
        }
    }

    private final android.location.LocationListener mLocationListener = new android.location.LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            UserInfo.setUserLoc(new LatLng(location.getLatitude(), location.getLongitude()));//updates local save
            mLastLocation=location;
            new UpdateLocationBG(context).execute();//updates location to the server
            user.remove();//places the user to the new location
            user = MainActivity.mMap.addMarker(new MarkerOptions().position(UserInfo.getUserLoc()).title(UserInfo.getUsername()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            Log.e("location", "location changed "+ location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

}

