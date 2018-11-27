package com.shofiul.sk.anondoride.anondoride;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CustomerMapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        RoutingListener {

    private static final String TAG = "CustomerMapsActivity";


    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    //vars
    String mUserId;
    public int mRadius = 1;
    private boolean mDriverFound = false;
    private String mFoundDriverId;



    //widgets

    private Button mLogout,mRequest ;
    private LatLng mPickupLocation;
    private Marker mDriverMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_maps);

        polylines = new ArrayList<>(); //for direction Path way

        Log.d(TAG, "onCreate: Content view has been seted");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Log.d(TAG, "onCreate: Map Fragemnet Manager");
        mUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mLogout = (Button) findViewById(R.id.bt_log_out);
        mRequest = (Button) findViewById(R.id.bt_request);

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(CustomerMapsActivity.this,MainActivity.class));
                finish();
                return;
            }
        });

        mRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("customerRequest");
                GeoFire geoFire = new GeoFire(reference);
                geoFire.setLocation(mUserId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        Log.d(TAG, "onComplete: GeoFire: " + error);
                    }
                });
                mPickupLocation = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
                mMap.addMarker(new MarkerOptions().position(mPickupLocation).title("Pick me !"));
                mRequest.setText("Getting Service!");
               // getBusLocations();
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

    List<Marker> mMarkerList = new ArrayList<>();
    List<String> mKeyList = new ArrayList<>();
    private void showAvalibleBus(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(getString(R.string.driver_availible));
        GeoFire geoFire = new GeoFire(reference);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude() ),10000);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                for(Marker markerIt: mMarkerList){
                    if(markerIt.getTag().equals(key)){
                        return;
                    }
                }

                LatLng driverLocation = new LatLng(location.latitude,location.longitude);
                Marker driverMarker = mMap.addMarker(new MarkerOptions().position(driverLocation).title(key).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus)));
                driverMarker.setTag(key);
                mMarkerList.add(driverMarker);
                mKeyList.add(key);


            }
            @Override
            public void onKeyExited(String key) {
                for(Marker markerIt: mMarkerList){
                    if(markerIt.getTag().equals(key)){
                        mMarkerList.remove(markerIt);
                        markerIt.remove();
                        return;
                    }
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                for(Marker markerIt: mMarkerList){
                    if(markerIt.getTag().equals(key)){
                        markerIt.setPosition(new LatLng(location.latitude,location.longitude));

                    }
                }
            }
            @Override
            public void onGeoQueryReady() {
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
            }
        });

    }
    String[][] mListNop = new String[100][2];

    void setNop(String s, int nop){
         for(int i = 0;i<100;i++){
             mListNop[i][0] = s;
             mListNop[i][1] = nop+"";
         }


    }
    int getNop(String s){
        int nop=0;
        for(int i = 0;i<100;i++){
            if( mListNop[i][0] == s){
                nop =  Integer.parseInt(mListNop[i][1]);
            }else{

            }
        }
       return nop;
    }

    int mNop;
    String mCurrentKey;
    private void markerClick(){

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                for(Marker myMarker :mMarkerList){
                    for(String key :mKeyList){

                        if(myMarker.getTag() == key){
                            mCurrentKey = key;
                            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference()
                                    .child(getString(R.string.Users))
                                    .child(getString(R.string.Drivers))
                                    .child(key)
                                    .child(getString(R.string.availible_passengers));

                            reference1.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Log.d(TAG, "onDataChange: Getting NOP");
                                    if(dataSnapshot.exists()){
                                        mNop  =   Integer.parseInt(dataSnapshot.getValue().toString());
                                        setNop(mCurrentKey,mNop);
                                        Log.d(TAG, "onDataChange: NOP is: "+ mNop);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }


                            });
                            myMarker.setTitle("Bus Id: "+ key +" Capacity: "+(20-(getNop(key))));
                            Toast.makeText(CustomerMapsActivity.this, "Marker Clicked "+ key , Toast.LENGTH_SHORT).show();
                        }
                    }
                }


                return false;
            }
        });
    }





    private void getBusLocations(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(getString(R.string.driver_availible));
        Log.d(TAG, "getBusLocations: ");
        GeoFire geoFire = new GeoFire(reference);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(mPickupLocation.latitude,mPickupLocation.longitude),mRadius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!mDriverFound){
                    mDriverFound = true;
                    mFoundDriverId = key;
                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference()
                            .child(getString(R.string.Users))
                            .child(getString(R.string.Drivers))
                            .child(getString(R.string.driver_Found));
                    String customerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map =  new HashMap();
                    map.put(getString(R.string.customerTravleId),customerID);
                    driverRef.updateChildren(map);

                   // getDriverLocation();
                    mRequest.setText("Locking For Bus Location");
                }
            }
            @Override
            public void onKeyExited(String key) {
            }
            @Override
            public void onKeyMoved(String key, GeoLocation location) {
            }
            @Override
            public void onGeoQueryReady() {
                if(!mDriverFound){
                    mRadius ++;
                    getBusLocations();
                }
            }
            @Override
            public void onGeoQueryError(DatabaseError error) {

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

        setBusStops();
        getRoutetoMarker();
        markerClick();

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged: setting new Location ");
        mLastLocation =  location;
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
        Log.d(TAG, "onLocationChanged: "+ latLng);
        showAvalibleBus();



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
