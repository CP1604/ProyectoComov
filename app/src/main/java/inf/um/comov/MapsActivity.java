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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
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
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{
    private final int MY_APP_PERMISSIONS = 1;
    private static final String mylnikovURL = "https://api.mylnikov.org/";

    private BroadcastReceiver mReceiver;
    private Map<Location, Integer> locations;
    private Map<Location, String> towers;
    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap mMap;
    private LocationCallback locationCallback;
    private boolean readPhoneStatusGranted = false;
    private boolean requestingLocationUpdates = false;
    private boolean fine_permissions_granted = false;
    private boolean coarse_permissions_granted = false;
    private boolean writeExternal_granted = false;
    private TextView tower;

    //Valores permitidos: "2G", "3G", "4G"
    private int TECH = -1;
    private String technology;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Almacena los puntos del mapa
        locations = new HashMap<Location, Integer>();
        towers = new HashMap<Location, String>();

        //Obtenemos la tecnología con la que vamos a trabajar
        Bundle extras = getIntent().getExtras();
        technology = extras.getString("tecnology");
        if (extras != null) {
            //Map to numeric value
            switch (technology) {
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
                            TECH = 1;
                            break;
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        case TelephonyManager.NETWORK_TYPE_EVDO_A:
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                        case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        case TelephonyManager.NETWORK_TYPE_EHRPD:
                        case TelephonyManager.NETWORK_TYPE_HSPAP:
                            TECH = 2;
                            break;
                        case TelephonyManager.NETWORK_TYPE_LTE:
                        case TelephonyManager.NETWORK_TYPE_NR:
                            TECH = 3;
                            break;
                        default:
                            TECH = -1;
                            break;
                    }
                    break;
            }
        }
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

                    //Guardamos la ubicación junto con el valor del punto
                    if (v != 0 && v != -2) //No hay señal y no hay permisos
                        locations.put(location, v);
                }
            }
        };
        //Listener para el switch que controla el GPS
        Switch enabler = findViewById(R.id.enableGPS);
        enabler.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                requestingLocationUpdates = isChecked;

                //Si el switch está activado entonces debemos empezar a recibir actualizaciones de localización
                if (requestingLocationUpdates) {
                    checkPermissions();
                    //Funcionalidad disponible solo si se han dado los permisos correspondientes
                    if (coarse_permissions_granted) {
                        Log.e("Debug", "Empezando a actualizar ubicación");
                        startLocationUpdates();
                        //Comprobar que la ubicacion está activa
                        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                        System.out.println("Provider contains=> " + provider);
                        if (provider.contains("gps") || provider.contains("network")) {
                            //nada
                        } else
                            Toast.makeText(MapsActivity.this, "Necesitas ACTIVAR la ubicación", Toast.LENGTH_SHORT).show();

                    } else {
                        Log.e("DEBUG", "Switch.onCheckedChanged: Cannot update location because of permissions were denied");
                        Toast.makeText(MapsActivity.this, "Necesitas dar permisos de ubicación", Toast.LENGTH_SHORT).show();
                        enabler.setChecked(false);
                        checkPermissions();
                    }
                } else {
                    //Se ha desactivado el GPS: detenemos las actualizaciones de ubicación
                    stopLocationUpdates();
                }
            }
        });

        //Listener para el botón
        Button save = findViewById(R.id.saveTowers);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTowers();
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

        /*Necesario para poder tener InfoWindow personalizados
        Cuando el usuario pulsa sobre un circulo que representa a una torre se despliega la
        información de la torre.*/
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                //Habilitamos los InfoWindow multilínea (el Infowindow por defecto no lo permite)
                LinearLayout info = new LinearLayout(getApplicationContext());
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(getApplicationContext());
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(getApplicationContext());
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);
                return info;
            }
        });
        //Utilizamos la ubicación actual del dispositivo.
        Log.e("DEBUG", "Launching startShowingCurrentLocation().");
        Log.e("DEBUG", "Starting to show current location.");
        checkPermissions();
        startShowingCurrentLocation();
    }

    /* Comprueba los permisos de la aplicación: No solicita todos cada vez, solo los faltantes.
    Si el usuario ya dio un permiso anteriormente este no se pide.
     */
    public void checkPermissions() {
        Log.e("DEBUG", "Checking permissions");
        List<String> permissions = new ArrayList<String>();
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
            } else permissions.add(Manifest.permission.READ_PHONE_STATE);
        }

        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            } else permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }


        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (permissions.size() != 0) {
            String[] stringArray = permissions.toArray(new String[0]);
            ActivityCompat.requestPermissions(this, stringArray
                    , MY_APP_PERMISSIONS);
            return;
        }
        readPhoneStatusGranted = true;
        coarse_permissions_granted = true;
        writeExternal_granted = true;
        fine_permissions_granted = true;
        Log.e("DEBUG", "Permissions check has finished.");
    }

    @SuppressLint("MissingPermission")
    public void startShowingCurrentLocation() {
        if (fine_permissions_granted) {
            mMap.setMyLocationEnabled(true);
        } else mMap.setMyLocationEnabled(false);
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
                    }
                }
            }).addOnFailureListener(activity, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("DEBUG", "Something went wrong");
                }
            });
        } else
            Log.e("DEBUG", "getLastLocation: Cannot get last device location because of permissions were denied");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
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
                        writeExternal_granted = true;
                        onMapReady(mMap);
                    } else
                        Log.e("DEBUG", "There was a problem about permissions.");
                } else {
                    Log.e("DEBUG", "There was a problem about permissions.");
                }
                break;
            }
        }
    }

    /* Cuando la aplicación es pausada: pantalla bloqueada o salir de la app sin cerrar desactivamos las actualizaciones
    de ubicación */
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
        switch (color) {
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
                .radius(10)
                .strokeColor(Color.BLACK)
                .strokeWidth(2)
                .fillColor(c));
    }

    private void drawTowerOnMap(Location location, int mcc, int mnc, int lac, int cellid, int level) {
        Circle circle = mMap.addCircle(new CircleOptions()
                .center(new LatLng(location.getLatitude(), location.getLongitude()))
                .radius(50)
                .clickable(true)
                .strokeColor(Color.BLACK)
                .strokeWidth(2)
                .fillColor(Color.DKGRAY));

        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.android);
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 1, 1, false);
        BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(location.getLatitude(), location.getLongitude()))
                .alpha(0)
                .title("Tower")
                .snippet("Latitude: " + location.getLatitude() + "\n" + "Longitude: " + location.getLongitude()
                                        + "\n" + "mcc: " + mcc + "\n" + "mnc: " + mnc + "\n"
                                        + "cellid: " + cellid + "\n" + "Signal strength level: " + level)
                .icon(smallMarkerIcon));
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                return true;
            }
        });
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
                //Get 2G info
                if (info instanceof CellInfoWcdma && TECH == 2) {
                    CellInfoWcdma infoWcdma = (CellInfoWcdma) info;
                    CellIdentityWcdma id = infoWcdma.getCellIdentity();

                    text.append("WCDMA ID: {cid: ").append(id.getCid());
                    text.append(" mcc: ").append(id.getMcc());
                    text.append(" mnc: ").append(id.getMnc());
                    text.append(" lac: ").append(id.getLac());
                    text.append("} Level:  ").append(infoWcdma.getCellSignalStrength().getLevel())
                            .append("\n");
                    signalStrenght.add(infoWcdma.getCellSignalStrength().getLevel());
                    getGet(id.getMcc(), id.getMnc(), id.getLac(), id.getCid()
                            , infoWcdma.getCellSignalStrength().getLevel());
                }
                //Get 3G info
                else if (info instanceof CellInfoLte && TECH == 3) {
                    CellInfoLte infoLte = (CellInfoLte) info;
                    CellIdentityLte id = infoLte.getCellIdentity();

                    text.append("LTE ID: {ci: ").append(id.getCi());
                    text.append(" mcc: ").append(id.getMcc());
                    text.append(" mnc: ").append(id.getMnc());
                    text.append(" tac: ").append(id.getTac());
                    text.append("} Level:  ").append(infoLte.getCellSignalStrength().getLevel())
                            .append("\n");
                    signalStrenght.add(infoLte.getCellSignalStrength().getLevel());
                    getGet(id.getMcc(), id.getMnc(), id.getTac(), id.getCi()
                            , infoLte.getCellSignalStrength().getLevel());
                }
                //Get 4G info
                else if (info instanceof CellInfoGsm && TECH == 1) {
                    CellInfoGsm infoGsm = (CellInfoGsm) info;
                    CellIdentityGsm id = infoGsm.getCellIdentity();

                    text.append("GSM ID: {cid: ").append(id.getCid());
                    text.append(" mcc: ").append(id.getMcc());
                    text.append(" mnc: ").append(id.getMnc());
                    text.append(" lac: ").append(id.getLac());
                    text.append("} Level:  ").append(infoGsm.getCellSignalStrength().getLevel())
                            .append("\n");
                    signalStrenght.add(infoGsm.getCellSignalStrength().getLevel());
                    getGet(id.getMcc(), id.getMnc(), id.getLac(), id.getCid(),
                            infoGsm.getCellSignalStrength().getLevel());
                }
            }
            //Si no hay elementos en la lista puede ser que no esté activa la tecnologia o que no haya ninguna antena cerca
            if (signalStrenght.size() == 0) {
                Toast.makeText(MapsActivity.this, "Fuerza el uso de la red " + technology + " en ajutes.", Toast.LENGTH_SHORT).show();
                return 0;
            }
            int max = signalStrenght.stream().max(Comparator.comparing(Integer::valueOf)).get();
            return max;
        } else
            //No tenemos permisos y por tanto la funcionalidad no es accesible
            Toast.makeText(MapsActivity.this, "Necesitas dar los permisos necesarios.", Toast.LENGTH_SHORT).show();
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

    /* Cuando la aplicación se reanuda comprobamos si el switch del gps estaba activado. Si lo estaba entonces volvemos a solicitar
    las actualizaciones de ubicación (cuando la app se para las actualizaciones también)
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (requestingLocationUpdates)
            startLocationUpdates();
    }

    /* Método auxiliar para guardar la información recolectada por la app. Guarda tanto las ubicaciones del usuario y el nivel de señal
    de cada una de ellas, como las ubicaciones y la información asociada a las torres de telefonía encontrada
    El formato es un csv:
        Technology Used: 2/3/4G
        Longitude1:Latitude1
        Longitude2:Latitude2
        ...
        LongitudeN:LatitudeN
        Towers
        Longitude1;Latitude1;mcc1;mnc1;lac1;cellid1;levelstrenght1
        Longitude2:Latitude2;mcc2;mnc2;lac2;cellid2;levelstrenght2
        ...
        LongitudeN:LatitudeN;mccN;mncN;lacN;cellidN;levelstrenghtN
     */
    private void saveTowers() {
        //Si no hay puntos o solo hay 1 no se guarda
        //Solo guardamos si tenemos permisos para hacerlo
        if (locations.size() >= 2 && writeExternal_granted) {
            //Alamcenamos en el directorio /save_maps del almacenamiento externo de la app
            String root = Environment.getExternalStorageDirectory().toString();
            File dir = new File(root + "/saved_maps");
            //Si el directorio aún no existe se crea
            if (!dir.exists()) {
                dir.mkdir();
            }
            //Nombre: Fecha en la que se crea el fichero con extensión .txt
            String name = new Date().toString() + ".txt".trim();
            try {
                File storedFile = new File(dir, name);
                FileWriter writer = new FileWriter(storedFile);
                writer.append("Technology Used:" + technology + "\n");
                //Cada linea tiene la localización y el punto (latitude:longitude;color)
                for (Map.Entry<Location, Integer> entry : locations.entrySet()) {
                    writer.append(entry.getKey().getLatitude() + ":" + entry.getKey().getLongitude() + ";" + entry.getValue() + "\n");
                }
                if (towers.size() >  0) {
                    writer.append("Towers\n");
                    for (Map.Entry<Location, String> entry : towers.entrySet()) {
                        writer.append(entry.getKey().getLatitude() + ";" + entry.getKey().getLongitude() + ";" + entry.getValue() + "\n");
                    }
                }
                writer.flush();
                writer.close();
                Toast.makeText(MapsActivity.this, "Fichero " + name + " guardado correctamente."
                        , Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(MapsActivity.this, "Se ha producido un error al escribir el fichero."
                        , Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            return;
        } else {
            Toast.makeText(MapsActivity.this, "Se necesitan al menos 2 ubicaciones registradas."
                    , Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void getGet(int mcc, int mnc, int lac, int cellid, int level){
        Log.e("DEBUG", mcc + ":" + mnc + ":" + lac + ":" + cellid);
        Log.e("DEBUG", mylnikovURL);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(mylnikovURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mylnikov mylnikov = retrofit.create(inf.um.comov.mylnikov.class);

        Call<JsonObject> call = mylnikov.getGet("1.1", "open", mcc, mnc, lac, cellid);
        call.enqueue(new Callback<JsonObject>() {
         @Override
         public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
             JsonObject object = response.body();
             if (response.isSuccessful() && object.get("result").toString().equals("200")) {
                 JsonObject data = (JsonObject) object.get("data");
                 String lon = data.get("lon").toString();
                 String lat = data.get("lat").toString();
                 Location l = new Location("");
                 String s = Integer.toString(mcc) + ";" + Integer.toString(mnc)  + ";" + Integer.toString(lac)  + ";" + Integer.toString(cellid) + ";" + Integer.toString(level);
                 Log.e("DEBUG", s);
                 Log.e("DEBUG", lon +  ":"  + lat + ":" + s);
                 l.setLongitude(Double.parseDouble(lon));
                 l.setLatitude(Double.parseDouble(lat));
                 towers.put(l, s);
                 drawTowerOnMap(l, mcc, mnc, lac, cellid, level);
            }
         }

         @Override
         public void onFailure(Call<JsonObject> call, Throwable t) {
             Log.e("DEBUG", "Se ha producido un error al solicitar la ubicación de la torre");
         }
        });
    }
}