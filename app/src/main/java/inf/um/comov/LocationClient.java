package inf.um.comov;

import android.app.Activity;
import android.location.Location;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class LocationClient {
    public static Location location = null;

    public static FusedLocationProviderClient getLocationFusedInstance(Activity activity){
        return LocationServices.getFusedLocationProviderClient(activity);
    }
}
