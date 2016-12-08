package com.norandiaconu.wheresmycar;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class ParkingActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    Button saveButton, clearButton;
    double latitude, longitude;
    Location lastKnownLocation;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    MarkerOptions marker;
    String locationProvider;
    NumberPicker floorPicker;
    int floor = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ParkingActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        preferences = getPreferences(MODE_PRIVATE);
        if (savedInstanceState != null) {
            LatLng addMarker = new LatLng(savedInstanceState.getDouble("Latitude"), savedInstanceState.getDouble("Longitude"));
            mMap.addMarker(new MarkerOptions().position(addMarker).title("Parked car on floor " + savedInstanceState.getInt("Floor")));
        }

        saveButton = (Button) findViewById(R.id.savebutton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editor = preferences.edit();
                editor.putLong("Lati", Double.doubleToLongBits(getLat()));
                editor.putLong("Longi", Double.doubleToLongBits(getLong()));
                editor.putInt("floorNum", getFloor());
                editor.commit();
                LatLng addMarker = new LatLng(getLat(), getLong());
                marker = new MarkerOptions().position(addMarker).title("Parked car on floor " + getFloor());
                mMap.addMarker(marker);
            }
        });
        clearButton = (Button) findViewById(R.id.clearbutton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editor = preferences.edit();
                editor.clear();
                editor.commit();
                mMap.clear();
            }
        });

        floorPicker = (NumberPicker) findViewById(R.id.floorpicker);
        floorPicker.setMinValue(0);
        floorPicker.setMaxValue(10);
        floorPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i2) {
                Toast.makeText(ParkingActivity.this, "Please select a floor number\nYou are currently on floor " + i2, Toast.LENGTH_SHORT).show();
                floor = i2;
            }
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationProvider = LocationManager.NETWORK_PROVIDER;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
        latitude = lastKnownLocation.getLatitude();
        longitude = lastKnownLocation.getLongitude();

        LatLng initial = new LatLng(latitude, longitude);
        if (preferences.getLong("Lati", 0) != 0) {
            mMap.clear();
            double la = Double.longBitsToDouble(preferences.getLong("Lati", 0));
            double lo = Double.longBitsToDouble(preferences.getLong("Longi", 0));
            int fl = preferences.getInt("floorNum", -1);
            LatLng ll = new LatLng(la, lo);
            marker = new MarkerOptions().position(ll).title("Parked car on floor " + fl);
            mMap.addMarker(marker);
            Toast.makeText(getApplicationContext(), "LAT: " + la + "LON: " + lo, Toast.LENGTH_LONG).show();
        }
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(initial));
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
    }

    public double getLat() {
        return lastKnownLocation.getLatitude();
    }

    public double getLong() {
        return lastKnownLocation.getLongitude();
    }

    public int getFloor() {
        return floor;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putDouble("Latitude", getLat());
        savedInstanceState.putDouble("Longitude", getLong());
        savedInstanceState.putInt("Floor", getFloor());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Continue
                } else {
                    Toast.makeText(this, "Need location permission", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

}
