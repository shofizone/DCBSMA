package com.shofiul.sk.anondoride.anondoride;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import android.renderscript.RenderScript;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DriversMapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        RoutingListener {

    private static final String TAG = "DriversMapsActivity";

    private String customerID;


    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    String mUserId;


    private Button mLogout,mIncreasePassengers,mDecreasePassengers;
    private TextView mShowNop;
    private int mNumberOfPassengers = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drivers_maps);
        Log.d(TAG, "onCreate: Content view has been seted");

        polylines = new ArrayList<>(); //for direction Path way
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Log.d(TAG, "onCreate: Map Fragemnet Manager");
        init();
        //getAssignedCustomer();

    }

    void init(){

        mLogout = (Button) findViewById(R.id.bt_log_out);
        mIncreasePassengers = (Button) findViewById(R.id.increasePassenger);
        mDecreasePassengers = (Button) findViewById(R.id.decreasePassenger);
        mShowNop = (TextView) findViewById(R.id.tv_show_nop);


        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(DriversMapsActivity.this,MainActivity.class));
                finish();
                return;
            }
        });


        mIncreasePassengers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mNumberOfPassengers <20){
                    mNumberOfPassengers += 1;
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                            .child(getString(R.string.Users))
                            .child(getString(R.string.Drivers))
                            .child(uid)
                            .child(getString(R.string.availible_passengers));
                    reference.setValue(mNumberOfPassengers);
                    mShowNop.setText(mNumberOfPassengers+"");
                }
                if(mNumberOfPassengers == 20)
                    Toast.makeText(DriversMapsActivity.this, "Maximum Passenger Capacity is 20", Toast.LENGTH_SHORT).show();

            }
        });

        mDecreasePassengers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mNumberOfPassengers > 0){
                    mNumberOfPassengers -= 1;
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                            .child(getString(R.string.Users))
                            .child(getString(R.string.Drivers))
                            .child(uid)
                            .child(getString(R.string.availible_passengers));
                    reference.setValue(mNumberOfPassengers);
                    mShowNop.setText(mNumberOfPassengers+"");

                }



            }
        });

    }


    private void setBusStops(){
        LatLng busStopLocation1 = new LatLng(23.780544, 90.417846);
        mMap.addMarker(new MarkerOptions().position(busStopLocation1).title("Bus Stop 1").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_stop)));

        LatLng busStopLocation2 = new LatLng(23.780682, 90.405556);
        mMap.addMarker(new MarkerOptions().position(busStopLocation2).title("Bus Stop 2").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_stop)));

        LatLng busStopLocation3 = new LatLng(23.781855, 90.399082);
        mMap.addMarker(new MarkerOptions().position(busStopLocation3).title("Bus Stop 3").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_stop)));

        LatLng busStopLocation4 = new LatLng(23.794553, 90.401180);
        mMap.addMarker(new MarkerOptions().position(busStopLocation4).title("Bus Stop 4").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_stop)));

        LatLng busStopLocation5 = new LatLng(23.780648, 90.425613);
        mMap.addMarker(new MarkerOptions().position(busStopLocation5).title("Bus Stop 5").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_stop)));

    }
    private void updateNop(){

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.Users))
                .child(getString(R.string.Drivers))
                .child(uid)
                .child(getString(R.string.availible_passengers));
        reference.setValue(mNumberOfPassengers);
        mShowNop.setText(mNumberOfPassengers+"");

    }


    private void getAssignedCustomer() {
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.Users))
                .child(getString(R.string.Drivers))
                .child(driverId)
                .child(getString(R.string.customerTravleId));

        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                        customerID = dataSnapshot.getValue().toString();
                        getAssignedCustomerPickupLocation();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void getAssignedCustomerPickupLocation() {
        DatabaseReference assignedCustomerPickupLocationRef = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.customerRequest))
                .child(customerID)
                .child("l");
        assignedCustomerPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    List<Object> map =(List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if(map.get(0) != null){
                        locationLat =  Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationLng =  Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLatLan = new LatLng(locationLat,locationLng);
                   mMap.addMarker(new MarkerOptions().position(driverLatLan).title(getString(R.string.pickupLocation)));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onMapReady: Permission checked");
            return;
        }
        buildGoogleApiClient();
        Log.d(TAG, "onMapReady: BuildGoogleApiClient called");
        mMap.setMyLocationEnabled(true);
    }

    private synchronized void buildGoogleApiClient() {

        Log.d(TAG, "buildGoogleApiClient: Started to build google api client");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        Log.d(TAG, "buildGoogleApiClient: mGoogleApiClientConnecte");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected: Requested for loacation");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Log.d(TAG, "onConnected: Requesting Location update");
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        getRoutetoMarker();
        updateNop();
        setBusStops();
    }

    @Override
    public void onLocationChanged(Location location) {

        if(getApplicationContext() != null){

            mLastLocation =  location;
            LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12));

            mUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference refAvailible = FirebaseDatabase.getInstance().getReference(getString(R.string.driver_availible));

            GeoFire geoFireAvailible = new GeoFire(refAvailible);
                    geoFireAvailible.setLocation(
                            mUserId,
                            new GeoLocation(location.getLatitude(), location.getLongitude()),
                            new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {

                        }
                    });
        }


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

//        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(getString(R.string.driver_availible));
//        GeoFire geoFire = new GeoFire(reference);
//        geoFire.removeLocation(uid);
//

    }


    //Routing Path Way
    private void getRoutetoMarker(){
        LatLng start,end;
        start = new LatLng(23.781602,90.425751);
        end  = new LatLng(23.794434,90.400903);

        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(start, end)
                .key("AIzaSyBOnmafhNNRULhT0jxDXVRZtZgrqCYSutk")
                .build();
        routing.execute();

    }

    @Override
    public void onRoutingFailure(RouteException e) {

        // The Routing request failed
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorPrimary,R.color.colorPrimary,R.color.design_default_color_primary,R.color.colorAccent,R.color.primary_dark_material_light};


    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {


        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }


        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingCancelled() {

    }
}
