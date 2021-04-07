package inf.um.comov;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private final int MY_APP_PERMISSIONS = 1;

    private BroadcastReceiver mReceiver;

    private List<Location> locations;
    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap mMap;
    private LocationCallback locationCallback;
    private boolean readPhoneStatusGranted = false;
    private boolean requestingLocationUpdates = false;
    private boolean fine_permissions_granted = false;
    private boolean coarse_permissions_granted = false;

    //Valores permitidos: "2G", "3G", "4G"
    private int TECH = -1;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Obtenemos la tecnología con la que vamos a trabajar
        Bundle extras = getIntent().getExtras();
        /*if (extras != null) {
            String tech = extras.getString("tecnology");
            //Map to numeric value
            switch (tech){
                case "2G":
                    TECH = 1;
                    break;
                case "3G":
                    TECH = 2;
                    break;
                case "4G":
                    TECH = 3;
                    break;
                default:
                    //Por defecto utilizamos la red que está utilizando actualmente el usuario
                    TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext()
                            .getSystemService(Context.TELEPHONY_SERVICE);
                    checkPermissions();
                    int networkType = telephonyManager.getNetworkType();
                    switch (networkType) {
                        case TelephonyManager.NETWORK_TYPE_GPRS:
                        case TelephonyManager.NETWORK_TYPE_EDGE:
                        case TelephonyManager.NETWORK_TYPE_CDMA:
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                        case TelephonyManager.NETWORK_TYPE_IDEN:
                            TECH = 1; break;
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        case TelephonyManager.NETWORK_TYPE_EVDO_A:
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                        case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        case TelephonyManager.NETWORK_TYPE_EHRPD:
                        case TelephonyManager.NETWORK_TYPE_HSPAP:
                            TECH = 2; break;
                        case TelephonyManager.NETWORK_TYPE_LTE:
                            TECH = 3; break;
                        case TelephonyManager.NETWORK_TYPE_NR:
                            TECH = 3; break;
                        default:
                            TECH = -1; break;
                    }
                    break;
            }
        }*/

        fusedLocationClient = LocationClient.getLocationFusedInstance(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                TextView text = findViewById(R.id.textLocation);
                if (locationResult == null) {
                    Log.e("DEBUG", "LOCATIONCALLBACK: NULL LOCATION");
                }
                for (Location location : locationResult.getLocations()) {
                    LocationClient.location = location;
                    Log.e("DEBUG", LocationClient.locationToString(location));
                    text.setText(LocationClient.locationToString(location));
                    int v = getCellInfo();
                    drawCircleOnMap(location, v);
                }
            }
        };
        Switch enabler = findViewById(R.id.enableGPS);
        enabler.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                requestingLocationUpdates = isChecked;

                if (requestingLocationUpdates) {
                    checkPermissions();
                    if (coarse_permissions_granted) {
                        Log.e("Debug", "Empezando a actualizar ubicación");
                        startLocationUpdates();
                        //Comprobar que la ubicacion está activa
                        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                        System.out.println("Provider contains=> " + provider);
                        if (provider.contains("gps") || provider.contains("network")){
                            //nada
                        } else Toast.makeText(MapsActivity.this, "Necesitas ACTIVAR la ubicación", Toast.LENGTH_SHORT).show();

                    } else {
                        Log.e("DEBUG", "Switch.onCheckedChanged: Cannot update location because of permissions were denied");
                        Toast.makeText(MapsActivity.this, "Necesitas dar permisos de ubicación", Toast.LENGTH_SHORT).show();
                        enabler.setChecked(false);
                        checkPermissions();
                    }
                }
                else {
                    stopLocationUpdates();
                }
            }
        });

      /*  IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);

        //Manejamos las acciones de encendido y apagado de pantalla
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Si se apaga no ejecutamos acciones adicionales
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    Log.v("DEBUG", "In Method:  ACTION_SCREEN_OFF");
                }
                //Si la pantalla se enciende comprobamos el estadod el switch
                else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                    Log.v("DEBUG", "In Method:  ACTION_SCREEN_ON");
                    if (requestingLocationUpdates){
                        startLocationUpdates();
                    }
                }
            }
        };
        registerReceiver(mReceiver, filter);*/

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Log.e("DEBUG", "Activity created correctly.");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //Utilizamos la ubicación actual del dispositivo.
        Log.e("DEBUG", "Launching startShowingCurrentLocation().");
        Log.e("DEBUG", "Starting to show current location.");
        checkPermissions();
        startShowingCurrentLocation();
    }

    public void checkPermissions() {
        Log.e("DEBUG", "Checking permissions");
        List<String> permissions = new ArrayList<String>();
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
            }
            else permissions.add(Manifest.permission.READ_PHONE_STATE);
        }


        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            }
            else permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }


        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            }
            else permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (permissions.size() != 0) {
            String[] stringArray = permissions.toArray(new String[0]);
            ActivityCompat.requestPermissions(this, stringArray
            , MY_APP_PERMISSIONS);
            return;
        }
        readPhoneStatusGranted = true;
        coarse_permissions_granted = true;
        fine_permissions_granted = true;

        Log.e("DEBUG", "Permissions check has finished.");
    }

    @SuppressLint("MissingPermission")
    public void startShowingCurrentLocation() {
        if (fine_permissions_granted) {
            mMap.setMyLocationEnabled(true);
        }
        else mMap.setMyLocationEnabled(false);
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation(FusedLocationProviderClient fusedLocationProviderClient, final Activity activity) {
        fusedLocationProviderClient = LocationClient.getLocationFusedInstance(this);
        if (coarse_permissions_granted) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        LocationClient.location = location;
                        locations.add(location);
                    }
                }
            }).addOnFailureListener(activity, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("DEBUG", "Something went wrong");
                }
            });
        }
        else Log.e("DEBUG", "getLastLocation: Cannot get last device location because of permissions were denied");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults){
        switch (requestCode) {
            case MY_APP_PERMISSIONS: {
                if (grantResults.length > 0) {
                    boolean granted = true;
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            granted = false;
                            break;
                        }
                    }
                    if (granted) {
                        Log.e("DEBUG", "Permissions Granted.");
                        readPhoneStatusGranted = true;
                        coarse_permissions_granted = true;
                        fine_permissions_granted = true;
                        onMapReady(mMap);
                    }
                    else
                        Log.e("DEBUG", "There was a problem about permissions.");
                } else {
                    Log.e("DEBUG", "There was a problem about permissions.");
                }
                break;
            }
        }
    }

    /*public void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, MY_LOCATION_PERMISSION_FINE);
            }
            return;
        }
        fusedLocationClient.requestLocationUpdates(LocationClient.createLocationRequest(), locationCallback, null);
    }*/

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(LocationClient.createLocationRequest(), locationCallback, null);
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
        TextView text = findViewById(R.id.textLocation);
        text.setText("GPS Disabled");
    }

    /*
     * Mapear valores del parámetro color a Colores
     * case 1:
     *      Color = Rojo: Intensidad de señal baja
     * case 2:
     *      Color = Amarillo: Intensidad de señal baja
     * case 3:
     *      Color = Azul: Intensidad de señal baja
     * case 4:
     *      Color = Verde claro: Intensidad de señal baja
     * defautl: //Cualquier otro valor
     *      Color = Blanco: Desconocido
     */
    private void drawCircleOnMap(Location location, int color) {
        int c = -1;
        switch (color){
            case -2: //No tenemos permisos...
                c = Color.BLACK;
                break;
            case 1:
                c = Color.RED;
                break;
            case 2:
                c = Color.YELLOW;
                break;
            case 3:
                c = Color.BLUE;
                break;
            case 4:
                c = Color.GREEN;
                break;
            default:
                c = Color.WHITE;
        }
        Circle circle = mMap.addCircle(new CircleOptions()
                .center(new LatLng(location.getLatitude(), location.getLongitude()))
                .radius(5)
                .strokeColor(c)
                .fillColor(c));
    }

    /*
     * A tener en cuenta:
     * - SIGNAL_STRENGTH_GREAT = 4
     * - SIGNAL_STRENGTH_GOOD = 3
     * - SIGNAL_STRENGTH_MODERATE = 2
     * - SIGNAL_STRENGTH_POOR = 1
     * - SIGNAL_STRENGTH_NONE_OR_UNKNOWN = 0
     */
    @SuppressLint("MissingPermission")
    public int getCellInfo() {
        //Check permissions
        if (readPhoneStatusGranted && coarse_permissions_granted) {
            //Obtenemos información

            TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext()
                    .getSystemService(Context.TELEPHONY_SERVICE);
            /*if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
                //List<NeighboringCellInfo> neighCells = telephonyManager.getNeighboringCellInfo();
            }
            else {*/
            List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();
            //LOG
            StringBuilder text = new StringBuilder();
            text.append("Found").append(cellInfoList.size()).append(" cells\n");

            List<Integer> signalStrenght = new ArrayList<Integer>();
            for (CellInfo info : cellInfoList) {
                if (info instanceof CellInfoWcdma) {
                    CellInfoWcdma infoWcdma = (CellInfoWcdma) info;
                    CellIdentityWcdma id = infoWcdma.getCellIdentity();

                    text.append("WCDMA ID: {cid: ").append(id.getCid());
                    text.append(" mcc: ").append(id.getMcc());
                    text.append(" mnc: ").append(id.getMnc());
                    text.append(" lac: ").append(id.getLac());
                    text.append("} Level:  ").append(infoWcdma.getCellSignalStrength().getLevel())
                            .append("\n");
                    signalStrenght.add(infoWcdma.getCellSignalStrength().getLevel());
                }
                else if (info instanceof CellInfoLte) {
                    CellInfoLte infoLte = (CellInfoLte) info;
                    CellIdentityLte id = infoLte.getCellIdentity();

                    text.append("LTE ID: {ci: ").append(id.getCi());
                    text.append(" mcc: ").append(id.getMcc());
                    text.append(" mnc: ").append(id.getMnc());
                    text.append(" tac: ").append(id.getTac());
                    text.append("} Level:  ").append(infoLte.getCellSignalStrength().getLevel())
                            .append("\n");
                    signalStrenght.add(infoLte.getCellSignalStrength().getLevel());
                }

                else if (info instanceof CellInfoGsm) {
                    CellInfoGsm infoGsm = (CellInfoGsm) info;
                    CellIdentityGsm id = infoGsm.getCellIdentity();

                    text.append("GSM ID: {cid: ").append(id.getCid());
                    text.append(" mcc: ").append(id.getMcc());
                    text.append(" mnc: ").append(id.getMnc());
                    text.append(" lac: ").append(id.getLac());
                    text.append("} Level:  ").append(infoGsm.getCellSignalStrength().getLevel())
                            .append("\n");
                    signalStrenght.add(infoGsm.getCellSignalStrength().getLevel());
                }
            }
            if (signalStrenght.size() == 0)
                return 0;
            int max = signalStrenght.stream().max(Comparator.comparing(Integer::valueOf)).get();
            return max;
        }
        else
            //No tenemos permisos y por tanto la funcionalidad no es accesible
            return -2;
    }

    /*//Anula el registro del Receiver: necesario para evitar el lanzamiento de excepciones
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        if (requestingLocationUpdates)
            startLocationUpdates();
    }
}


/*
import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import android.location.Location;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

/**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     *//*

/*
public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        getSupportActionBar().setTitle("Map Location Activity");

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(120000); // two minute interval
        mLocationRequest.setFastestInterval(120000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        }
        else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);
                Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                mLastLocation = location;
                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker.remove();
                }

                //Place current location marker
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("Current Position");
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);

                //move map camera
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));
            }
        }
    };

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mGoogleMap.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
*/
