package inf.um.comov;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class LocationClient {
    public static Location location = null;

    public static FusedLocationProviderClient getLocationFusedInstance(Activity activity) {
        return LocationServices.getFusedLocationProviderClient(activity);
    }

    public static LocationRequest createLocationRequest() {
        LocationRequest request = LocationRequest.create();
        request.setInterval(10000);
        request.setFastestInterval(5000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setSmallestDisplacement(15);
        return request;
    }

    public static String locationToString(final Location location) {
        return Location.convert(location.getLatitude(), Location.FORMAT_DEGREES) + " " + Location.convert(location.getLongitude(), Location.FORMAT_DEGREES);
    }

    /*private void getLastLocation(FusedLocationProviderClient fusedLocationProviderClient, final Activity activity) {
        fusedLocationProviderClient = LocationClient.getLocationFusedInstance(this);

        @Override
        public void onSuccess(Location location) {
            if (location != null){
                LocationClient.location = location;
                LocationAddressModel address = LocationClient.getAddress
            }
        }

    }*/
}
