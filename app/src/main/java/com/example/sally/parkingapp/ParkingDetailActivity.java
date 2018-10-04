package com.example.sally.parkingapp;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.example.sally.parkingapp.util.Network;
import com.example.sally.parkingapp.util.polyDecoder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ParkingDetailActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private String name,area,serviceTime,address;
    private TextView nameTv,areaTv,serviceTv,addressTv,routeBtn;
    private double lat,lon,nowLat,nowLon;
    private final int REQUEST_PERMISSION = 10;
    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingError = false;
    private FusedLocationProviderApi fusedLocationProviderApi;
    private LocationRequest locationRequest;
    private Location location;
    private long lastLocationTime = 0;
    private boolean focus = false;
    private Handler mHandler = new Handler();
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        name = bundle.getString("name");
        area = bundle.getString("area");
        serviceTime = bundle.getString("serviceTime");
        address = bundle.getString("address");
        lat = bundle.getDouble("lat");
        lon = bundle.getDouble("lon");
        setContentView(R.layout.activity_map);
        getSupportActionBar().hide();
        setView();
        initGps();
        checkPermission();
    }

    private void setView(){
        nameTv = (TextView) findViewById(R.id.parkingName);
        areaTv = (TextView) findViewById(R.id.parkingArea);
        serviceTv = (TextView) findViewById(R.id.parkingServiceTime);
        addressTv = (TextView) findViewById(R.id.parkingAddress);
        routeBtn = (TextView) findViewById(R.id.routeBtn);

        nameTv.setText(name);
        areaTv.setText(area);
        serviceTv.setText(serviceTime);
        addressTv.setText(address);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        routeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission();
                if(mGoogleApiClient.isConnected()) {
                    if (location != null) {
                        getRoute();
                    } else {
                        Toast.makeText(ParkingDetailActivity.this, "無法定位", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private void initGps(){
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(16);

        fusedLocationProviderApi = LocationServices.FusedLocationApi;

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(lat, lon))
                .title(name));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(),16));
        marker.showInfoWindow();
    }


    public void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            checkGps();
        }
        else{
            requestLocationPermission();
        }
    }


    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION);

        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,}, REQUEST_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkGps();
                return;
            } else {
                Toast.makeText(ParkingDetailActivity.this,"請授權", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void checkGps(){
        LocationManager status = (LocationManager) (this.getSystemService(Context.LOCATION_SERVICE));
        if(status.isProviderEnabled(LocationManager.GPS_PROVIDER)|| status.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        {
            if(!mGoogleApiClient.isConnected())
                startFusedLocation();
        } else {
            Toast.makeText(this, "請開啟定位服務", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)); //開啟設定頁面
        }
    }

    private void startFusedLocation(){
        // Connect the client.
        if (!mResolvingError) {
            // Connect the client.
            mGoogleApiClient.connect();
        }
    }

    private void stopFusedLocation(){
        // Disconnecting the client invalidates it.
        if(mGoogleApiClient.isConnected())
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location currentLocation = fusedLocationProviderApi.getLastLocation(mGoogleApiClient);

        if (currentLocation != null && currentLocation.getTime() > 20000) {
            location = currentLocation;
        } else {
            try {
                //
                fusedLocationProviderApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
                // Schedule a Thread to unregister location listeners
                Executors.newScheduledThreadPool(1).schedule(new Runnable() {
                    @Override
                    public void run() {
                        fusedLocationProviderApi.removeLocationUpdates(mGoogleApiClient, ParkingDetailActivity.this);
                    }
                }, 60000, TimeUnit.MILLISECONDS);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        lastLocationTime = location.getTime() - lastLocationTime;
        this.location = location;
    }


    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (connectionResult.hasResolution()) {

        } else {
            mResolvingError = true;
        }
    }

    private void getRoute(){
        final ProgressDialog dialog = ProgressDialog.show(this,
                "連線中","請稍後", true);

        String paramter = "?";

        paramter += "origin=" + String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude()) + "&";
        paramter += "destination=" + String.valueOf(lat) + "," + String.valueOf(lon) + "&";
        paramter += "key=" + getResources().getString(R.string.google_direction_key) ;

        Network.getData("" + getResources().getString(R.string.directionURL) + paramter, null , new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(dialog.isShowing())
                            dialog.dismiss();
                        showErrorDialog("連線錯誤");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseStr = response.body().string();
                    Log.d("Response", responseStr);
                    try {
                        JSONObject jo = new JSONObject(responseStr.toString());
                        final String status = jo.getString("status");
                        final JSONArray routes = jo.getJSONArray("routes");
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(dialog.isShowing())
                                    dialog.dismiss();
                                    if(status.equals("OK")){
                                        drawRoute(routes);
                                    }
                                    else{
                                        showErrorDialog(status);
                                    }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(dialog.isShowing())
                                    dialog.dismiss();
                                showErrorDialog("伺服器錯誤");
                            }
                        });
                    }
                }
                else {
                    String responseStr = response.body().string();
                    Log.d("Response", responseStr);
                }
            }
        });
    }

    private void drawRoute(JSONArray routes){
        try{
            JSONArray steps = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");
            PolylineOptions polylineOptions = new PolylineOptions();
            for (int i = 0; i < steps.length(); i++) {
                JSONObject step = steps.getJSONObject(i);
                String points = step.getJSONObject("polyline").getString("points");
                List<LatLng> pointsTodraw = polyDecoder.decodePoly(points);
                for(int j = 0; j < pointsTodraw.size(); j++){
                    polylineOptions.add(pointsTodraw.get(j));
                }
            }
            if(googleMap != null){
                googleMap.clear();
                googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lat, lon))
                        .title(name)).showInfoWindow();
                googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(location.getLatitude(), location.getLongitude()))
                        .title("當前位置"));
                googleMap.addPolyline(polylineOptions);
            }
        }catch (JSONException e){
            e.printStackTrace();
            showErrorDialog("解析錯誤");
        }
    }

    private void showErrorDialog(String errorMsg){
        new AlertDialog.Builder(ParkingDetailActivity.this)
                .setTitle("錯誤")
                .setMessage(errorMsg)
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    protected void onPause() {
        super.onPause();
            stopFusedLocation();
    }


}
