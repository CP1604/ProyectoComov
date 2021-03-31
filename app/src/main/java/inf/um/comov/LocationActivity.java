package inf.um.comov;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;

public class LocationActivity extends Activity {
    /*GoogleMap fusedLocationClient;
    private LocationRequest mLocationRequest;
    private static final int MY_LOCATION_PERMISSION_FINE = 1;
    private Object Location;*/


/*
    public void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, MY_LOCATION_PERMISSION_FINE);
            }
            return;
        }
        fusedLocationClient.setMyLocationEnabled(true);
    }

    public void onRequestPermissionResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults){
        switch (requestCode) {
            case MY_LOCATION_PERMISSION_FINE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    getLastLocation(fusedLocationClient, this);
                } else {
                    //??
                }
            }
        }
    }

    private void getLastLocation(GoogleMap fusedLocationProviderClient, final inf.um.comov.Location activity) {
        fusedLocationProviderClient = LocationClient.getLocationFusedInstance(this);

        public void onSuccess(Location location) {
            if (location != null){
                LocationClient.location = location;
                LocationAddressModel address = LocationClient.getAddress(location.getLatitude(), location.getLongitude(), getApplicationContext());

            }
        }

    }

}*/
}